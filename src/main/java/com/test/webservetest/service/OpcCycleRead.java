package com.test.webservetest.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.test.webservetest.config.OpcWebServeConfig;
import com.test.webservetest.mapperService.OpcMeasurePointServiceImpl;
import com.test.webservetest.mapperService.OpcServeMapperServiceImpl;
import com.test.webservetest.model.Dto.*;
import com.test.webservetest.model.Entity.OpcMeasurePoint;
import com.test.webservetest.model.Entity.OpcServe;
import com.test.webservetest.model.bean.OpcServeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/7/13 13:11
 */
@Service
@Slf4j
public class OpcCycleRead {

    private OpcWebServeConfig opcWebServeConfig;

    private ExecutorService executorService;

    private HttpClientService httpClientService;

    private OpcServeMapperServiceImpl opcServeMapperService;

    private OpcMeasurePointServiceImpl opcMeasurePointService;

    private OpcService opcService;

    private OpcConnectManger opcConnectManger;

    private ThreadPoolTaskScheduler taskScheduler;

    private InfluxdbWrite influxdbWrite;

    private final Map<Long, ClusterSchduleJob> rateKeyJobMap = new ConcurrentHashMap<>();

    @Autowired
    public OpcCycleRead(OpcWebServeConfig opcWebServeConfig,
                        ExecutorService executorService,
                        HttpClientService httpClientService,
                        OpcServeMapperServiceImpl opcServeMapperService,
                        OpcMeasurePointServiceImpl opcMeasurePointService,
                        OpcService opcService,
                        OpcConnectManger opcConnectManger,
                        ThreadPoolTaskScheduler taskScheduler,
                        InfluxdbWrite influxdbWrite
    ) {
        this.opcWebServeConfig = opcWebServeConfig;
        this.executorService = executorService;
        this.httpClientService = httpClientService;
        this.opcMeasurePointService = opcMeasurePointService;
        this.opcServeMapperService = opcServeMapperService;
        this.opcService = opcService;
        this.opcConnectManger = opcConnectManger;
        this.taskScheduler = taskScheduler;
        this. influxdbWrite= influxdbWrite;
        combineOpcServe();
    }


    private void combineOpcServe() {
        List<OpcServe> opcServeList = opcServeMapperService.list();
        if (CollectionUtils.isNotEmpty(opcServeList)) {
            opcServeList.forEach(opcserve -> {
                OpcServeInfoDto opcserveinfo = OpcServeInfoDto.builder()
                        .opcServeId(opcserve.getServeid().toString())
                        .opcServeIp(opcserve.getServeip())
                        .opcServeName(opcserve.getServename())
                        .build();
                OpcServeInfo opcServeInfo = new OpcServeInfo();
                opcServeInfo.setServeip(opcserve.getServeip());
                opcServeInfo.setIotid(opcserve.getServeid());
                opcServeInfo.setServename(opcserve.getServename());

                OpcConnectWatchDog opcConnectWatchDog = new OpcConnectWatchDog(opcServeInfo, opcService, executorService);

                opcConnectManger.addOpcWatchDog(opcConnectWatchDog);
                List<OpcMeasurePoint> opcMeasurePoints = opcMeasurePointService.list(Wrappers.<OpcMeasurePoint>lambdaQuery().eq(OpcMeasurePoint::getOpcserveid, opcserve.getServeid()));
                if (CollectionUtils.isNotEmpty(opcMeasurePoints)) {

                    //激活opc服务
                    if (!opcConnectWatchDog.getIsActive().get()) {
                        opcConnectManger.activeOpcWatchDog(opcserve.getServeid());
                    }

                    //创建定时取数服务
                    ClusterSchduleJob clusterSchduleJob = ClusterSchduleJob.builder()
                            .opcConnectManger(opcConnectManger)
                            .service(executorService)
                            .influxdbWrite(influxdbWrite)
                            .build();
                    rateKeyJobMap.put(opcServeInfo.getIotid(), clusterSchduleJob);

//                    List<OpcMeasurePointDto> opcMeasurePointDtos = new ArrayList<>();
                    Map<String, OpcMeasurePointDto> opcMeasurePointDtoHashMap = new HashMap<>();
                    opcMeasurePoints.stream().forEach(p -> {
                        OpcMeasurePointDto opcMeasurePointDto = OpcMeasurePointDto.builder()
                                .opcWriteEnable(1)
                                .opcWriteValue(1)
                                .opcTag(p.getTag()).build();
//                        opcMeasurePointDtos.add(opcMeasurePointDto);
                        opcMeasurePointDtoHashMap.put(p.getTag(), opcMeasurePointDto);
                        clusterSchduleJob.addmeasurepoint(opcServeInfo.getIotid(), opcMeasurePointDto);
                    });

                    opcConnectWatchDog.setOpcMeasurePointDtoHashMap(opcMeasurePointDtoHashMap);
                    opcServeInfo.setOpcMeasurePointDtos(opcMeasurePointDtoHashMap);
                    //加点服务
                    Set<String> tags=opcMeasurePoints.stream().map(OpcMeasurePoint::getTag).collect(Collectors.toSet());
                    opcConnectWatchDog.addItemsOpc(tags);

                }

            });

            rateKeyJobMap.forEach((k, v) -> {
                if (null == v.getFuture()) {
                    Trigger periodicTrigger = new PeriodicTrigger(3, TimeUnit.SECONDS);
                    ScheduledFuture<?> future = taskScheduler.schedule(v, periodicTrigger);
                    v.setFuture(future);
                }
            });
        }


    }


    public void test(boolean isclose, int readcount) {

        List<OpcServe> opcServeList = opcServeMapperService.list();
        if (CollectionUtils.isNotEmpty(opcServeList)) {

            opcServeList.forEach(opcserve -> {
                OpcServeInfoDto opcserveinfo = OpcServeInfoDto.builder()
                        .opcServeId(opcserve.getServeid().toString())
                        .opcServeIp(opcserve.getServeip())
                        .opcServeName(opcserve.getServename())
                        .build();
                List<OpcMeasurePoint> opcMeasurePoints = opcMeasurePointService.list(Wrappers.<OpcMeasurePoint>lambdaQuery().eq(OpcMeasurePoint::getOpcserveid, opcserve.getServeid()));
                if (CollectionUtils.isNotEmpty(opcMeasurePoints)) {
                    List<OpcMeasurePointDto> opcMeasurePointDtos = new ArrayList<>();

                    opcMeasurePoints.stream().forEach(p -> {


                        OpcMeasurePointDto opcMeasurePointDto = OpcMeasurePointDto.builder()
                                .opcWriteEnable(1)
                                .opcWriteValue(1)
                                .opcTag(p.getTag()).build();
                        opcMeasurePointDtos.add(opcMeasurePointDto);
                    });
                    //connect


                    OpcDataDto opcDataDto = OpcDataDto.builder()
                            .operate("")
                            .opcserveInfo(opcserveinfo)
                            .opcServeMeasurePoints(new ArrayList<OpcMeasurePointDto>())
                            .build();
                    OpcOperateDto opcOperateDto = OpcOperateDto.builder().build();

                    opcOperateDto.setData(opcDataDto);

                    boolean registerresult = false;


                    while (!registerresult) {
                        try {
                            registerresult = opcService.opcRegister(opcOperateDto);
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }

                    log.info("opc serve={} register success", opcserveinfo.getOpcServeId());

                    opcDataDto.setOpcServeMeasurePoints(opcMeasurePointDtos);

                    opcOperateDto.setData(opcDataDto);
                    OpcWebReturnDto addreuslt = null;
                    while (addreuslt == null) {
                        try {
                            addreuslt = opcService.opcAddItems(opcOperateDto);
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                    log.info(addreuslt.getData());
                    int localcunt = readcount;
                    while (!Thread.interrupted()) {

                        opcDataDto.setOpcServeMeasurePoints(new ArrayList<OpcMeasurePointDto>());
                        opcOperateDto.setData(opcDataDto);
                        try {
                            OpcWebReturnDto readresult = opcService.opcReadItems(opcOperateDto);
                            if (readresult != null) {
                                log.info(readresult.getData());
                                if (isclose && ((localcunt--) < 0)) {
                                    boolean close = false;
                                    int retry = 3;
                                    while ((!close) && ((retry--) > 0)) {
                                        try {
                                            close = opcService.opcUnRegister(opcOperateDto);
                                            return;
                                        } catch (Exception e) {
                                            log.error(e.getMessage(), e);
                                        }

                                    }

                                }
                            }
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }

                        try {
                            TimeUnit.MILLISECONDS.sleep(100);
                        } catch (InterruptedException e) {
                            log.error(e.getMessage(), e);
                            return;
                        }
                    }


                }

            });


        }
    }
}
