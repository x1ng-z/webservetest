package com.test.webservetest.service;

import com.alibaba.fastjson.JSONObject;

import com.test.webservetest.model.Dto.OpcMeasurePointDto;
import com.test.webservetest.model.bean.UploadNewData;
import com.test.webservetest.model.bean.event.InfluxWriteEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/28 10:24
 */
@Data
@Builder
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class ClusterSchduleJob implements Runnable {

    private ScheduledFuture<?> future;
    private OpcConnectManger opcConnectManger;
    private ExecutorService service;
    private InfluxdbWrite influxdbWrite;
    /**
     * map<resourceid,map<tag,measurepoint>>
     */
    private final Map<Long, Map<String, OpcMeasurePointDto>> resourceidKeysameratemapping = new ConcurrentHashMap<>();

    @Override
    public void run() {
        try {
            log.debug("context:" + resourceidKeysameratemapping.toString());
            resourceidKeysameratemapping.forEach((iotId, measurePointsConf) -> {
                ReadThead thead = new ReadThead(influxdbWrite,opcConnectManger, measurePointsConf, iotId);
                service.execute(thead);
                try {
                    thead.join();
                } catch (InterruptedException e) {
                    return;
                }
            });

            //如果没有要调度查询的点位，就停止
            if (isNeedCancelSchdule()) {
                if (future != null) {
                    future.cancel(true);
                    future = null;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    /**
     * 添加点位
     *
     * @param resourceid iot数据源id
     * @param point
     */
    public synchronized void addmeasurepoint(Long resourceid, OpcMeasurePointDto point) {

        if (resourceidKeysameratemapping.containsKey(resourceid)) {
            Map<String, OpcMeasurePointDto> secmap = resourceidKeysameratemapping.get(resourceid);
            secmap.put(point.getOpcTag(), point);
        } else {
            Map<String, OpcMeasurePointDto> secmap = new ConcurrentHashMap<>();
            secmap.put(point.getOpcTag(), point);
            resourceidKeysameratemapping.put(resourceid, secmap);
        }
    }


    /**
     * 查询是否存在需要调度的点号
     */
    public boolean isNeedCancelSchdule() {
        long totalsize = resourceidKeysameratemapping.values().stream().mapToLong(t -> t.values().size()).sum();
        return totalsize == 0;
    }

    /**
     * 取消调度
     */
    public void cancel() {
        if (future != null) {
            future.cancel(true);
            future = null;//help gc
        }
    }

    static private class ReadThead extends Thread {
        private Map<String, OpcMeasurePointDto> mappingConfigMap;
        private Long iotId;
        private OpcConnectManger opcConnectManger;
        private InfluxdbWrite influxdbWrite;

        public ReadThead(
                InfluxdbWrite influxdbWrite,
                OpcConnectManger opcConnectManger,
                Map<String, OpcMeasurePointDto> mappingConfigMap,
                Long iotId) {
            this.influxdbWrite = influxdbWrite;
            this.mappingConfigMap = mappingConfigMap;
            this.iotId = iotId;
            this.opcConnectManger = opcConnectManger;
        }

        @Override
        public void run() {
            Map<String, UploadNewData> pointscache = new HashMap<>();

            //获取对应数据源的opcgroup
            OpcConnectWatchDog opcConnectWatchDog = opcConnectManger.getOpcWatchDog(iotId);

            JSONObject readResult;
            Long now = Instant.now().toEpochMilli();
            String method = "";
            if (opcConnectWatchDog.getIsReConnOpc().get()) {
                log.warn("opc need reconnect,give up read this time!");
                return;
            }
            log.debug("readOpc");
            readResult = opcConnectWatchDog.readOpc();
            method = "readOpc";

            log.info("this time read tag={},cosettime={},method={}", mappingConfigMap.size(), Instant.now().toEpochMilli() - now, method);

            //读取数据出错就进行重连；
            if (ObjectUtils.isEmpty(readResult)) {
                synchronized (opcConnectWatchDog){

                    synchronized (opcConnectWatchDog.getLock()) {
                        if (!opcConnectWatchDog.getIsReConnOpc().get()) {
                            opcConnectWatchDog.getIsReConnOpc().set(true);
                            opcConnectWatchDog.getLock().notifyAll();
                            log.info("opc serve id={} name={} ip={} notify reconnect", opcConnectWatchDog.getOpcServeInfo().getServeid(), opcConnectWatchDog.getOpcServeInfo().getServename(),opcConnectWatchDog.getOpcServeInfo().getServeip());
                        }
                        return;
                    }
                }

            }

            Instant readTime = Instant.now();
            for (String tag : mappingConfigMap.keySet()) {
                if (readResult.containsKey(tag)) {
                    UploadNewData pcell = new UploadNewData();
                    pcell.setTime(readTime.toEpochMilli());
                    pcell.setMeasurePoint(tag);
                    pcell.setNode(mappingConfigMap.get(tag).getNode());
                    pcell.setValue(readResult.getDoubleValue(tag));
                    pointscache.put(tag, pcell);
                }
            }
            //从group中获取已经注册的opc点号对象，然后将点位缓存起来
            if (pointscache.size() > 0) {
                log.debug(pointscache.size() + "--" + pointscache.toString());
                opcConnectManger.updateNewData(iotId, pointscache);


                if (influxdbWrite != null) {
                    pointscache.values().forEach(p->{
                        JSONObject influxwritedata = new JSONObject();
                        influxwritedata.put(p.getMeasurePoint(),p.getValue());
                        InfluxWriteEvent event = new InfluxWriteEvent();
                        event.setData(influxwritedata);
                        event.setTimestamp(System.currentTimeMillis());
                        event.setMeasurement(InfluxdbOperateService.DCSMEASUERMENT);
                        influxdbWrite.addEvent(event);
                    });

                }
            }
        }
    }

}
