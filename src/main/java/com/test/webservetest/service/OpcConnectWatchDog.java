package com.test.webservetest.service;

import com.alibaba.fastjson.JSONObject;

import com.test.webservetest.contant.OpcWebHttpStatusEnum;
import com.test.webservetest.model.Dto.*;
import com.test.webservetest.model.bean.OpcServeInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/7/15 14:16
 */
@Slf4j
@Data
public class OpcConnectWatchDog implements Runnable {
    private OpcServeInfo opcServeInfo;
    //是否开始重新连接
    private AtomicBoolean isReConnOpc = new AtomicBoolean(false);

    private OpcService opcService;

    private ExecutorService executorService;

    private Object lock = new Object();
    private  Map<String, OpcMeasurePointDto> opcMeasurePointDtoHashMap=new ConcurrentHashMap<>();
    //是否取消
    private AtomicBoolean isCancel = new AtomicBoolean(false);

    //是否激活
    private AtomicBoolean isActive = new AtomicBoolean(false);

    //是否opc连接成功
    public AtomicBoolean isOpcConn = new AtomicBoolean(false);


    public OpcConnectWatchDog(OpcServeInfo opcServeInfo, OpcService opcService, ExecutorService executorService) {
        this.opcServeInfo = opcServeInfo;
        this.opcService = opcService;
        this.executorService = executorService;
    }

    @Override
    public void run() {
        while ((!Thread.interrupted()) && (!isCancel.get())) {

            try {
                synchronized (lock) {
                    while ((!isReConnOpc.get()) && (!isCancel.get())) {
                        lock.wait();
                        log.info("********* is need reconn isReConnOpc={},isCancel={}",isReConnOpc.get(),isCancel.get());
                    }
                }
                log.info("********* try to reconn serve={},ip={}",opcServeInfo.getServename(),opcServeInfo.getServeip());

                if (isCancel.get()) {
                    return;
                }
                //进行重新连接；
                synchronized (this) {

                    /***********************one step unregister opc***********************/
                    unRegisterOpc();
                    /*************************two step register opc*****************************************/
                    TimeUnit.SECONDS.sleep(3);
                    registerOpc();
                    TimeUnit.SECONDS.sleep(3);



                    //注册一个点，然后再进行重连
                    if (!CollectionUtils.isEmpty(opcServeInfo.getOpcMeasurePointDtos())) {

                        Set<String> testTag=new HashSet<>();
                        List<String> tagList=new ArrayList<>();
                        tagList.addAll(opcServeInfo.getOpcMeasurePointDtos().keySet());
                        if(tagList.size()>3) {
                            Random rand = new Random(opcServeInfo.getOpcMeasurePointDtos().size());
                            int retry=20;
                            while((testTag.size()<3)&&(retry>0)){
                                int randpos=rand.nextInt(opcServeInfo.getOpcMeasurePointDtos().size());
                                testTag.add(tagList.get(randpos));
                                retry--;
                            }

                        }else{
                            //小于三个，那就直接全部加入进去
                            testTag.addAll(tagList);
                        }
                        //加点
                        addItemsOpc(testTag);
                        //读取
                        readOpc();
                    }
                    //把数据读取一次后，然后再进行重连
                    /***********************one step unregister opc***********************/
                    unRegisterOpc();
                    /*************************two step register opc*****************************************/
                    TimeUnit.SECONDS.sleep(3);
                    registerOpc();
                    TimeUnit.SECONDS.sleep(3);



                    log.info("opc serve id= {},name={} register success", opcServeInfo.getServeid(), opcServeInfo.getServename());
                    if (CollectionUtils.isEmpty(opcServeInfo.getOpcMeasurePointDtos())) {
                        log.warn("opc serve id= {},name={} have no any tags", opcServeInfo.getServeid(), opcServeInfo.getServename());
                        isReConnOpc.set(false);
                        continue;
                    }

                    /*************************two step add opc tags*****************************************/
                    addItemsOpc();
                    isReConnOpc.set(false);
                }


            } catch (InterruptedException e) {
                log.error(e.getMessage(),e);
            }

        }
    }


    synchronized public boolean powoffWatchDog() {
        OpcServeInfoDto opcserveinfodto = OpcServeInfoDto.builder()
                .opcServeId(this.getOpcServeInfo().getServeid().toString())
                .opcServeIp(this.getOpcServeInfo().getServeip())
                .opcServeName(this.getOpcServeInfo().getServename())
                .build();

        OpcDataDto opcDataDto = OpcDataDto.builder()
                .operate("")
                .opcserveInfo(opcserveinfodto)
                .opcServeMeasurePoints(new ArrayList<OpcMeasurePointDto>())
                .build();
        OpcOperateDto opcOperateDto = OpcOperateDto.builder().build();

        opcOperateDto.setData(opcDataDto);

        /***********************one step unregister opc***********************/
        boolean close = false;
        while (!close) {
            try {
                close = opcService.opcUnRegister(opcOperateDto);
            } catch (Exception e) {
                log.error("receive no/invalid response from server.powoffWatchDog");
            }
        }
        this.getIsCancel().set(true);
        synchronized (this.getLock()) {
            this.getLock().notifyAll();
        }
        return true;
    }

    synchronized public void activeWatchDog() {
        if (!isActive.get()) {
            unRegisterOpc();
            registerOpc();
            executorService.execute(this);
            isActive.set(true);
        }
    }

    synchronized public boolean unRegisterOpc() {
        OpcServeInfoDto opcserveinfodto = OpcServeInfoDto.builder()
                .opcServeId(this.getOpcServeInfo().getServeid().toString())
                .opcServeIp(this.getOpcServeInfo().getServeip())
                .opcServeName(this.getOpcServeInfo().getServename())
                .build();

        OpcDataDto opcDataDto = OpcDataDto.builder()
                .operate("")
                .opcserveInfo(opcserveinfodto)
                .opcServeMeasurePoints(new ArrayList<OpcMeasurePointDto>())
                .build();
        OpcOperateDto opcOperateDto = OpcOperateDto.builder().build();

        opcOperateDto.setData(opcDataDto);

        /***********************one step unregister opc***********************/
        boolean close = false;
        while (!close) {

            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                log.error(e.getMessage(),e);
            }

            try {
                close = opcService.opcUnRegister(opcOperateDto);
            } catch (Exception e) {
                log.error("receive no/invalid response from server.unRegisterOpc");
            }

        }
        isOpcConn.set(close);
        return true;
    }

    synchronized public boolean registerOpc() {
        OpcServeInfoDto opcserveinfodto = OpcServeInfoDto.builder()
                .opcServeId(this.getOpcServeInfo().getServeid().toString())
                .opcServeIp(this.getOpcServeInfo().getServeip())
                .opcServeName(this.getOpcServeInfo().getServename())
                .build();

        OpcDataDto opcDataDto = OpcDataDto.builder()
                .operate("")
                .opcserveInfo(opcserveinfodto)
                .opcServeMeasurePoints(new ArrayList<OpcMeasurePointDto>())
                .build();
        OpcOperateDto opcOperateDto = OpcOperateDto.builder().build();

        opcOperateDto.setData(opcDataDto);

        /***********************one step unregister opc***********************/
        boolean close = false;
        while (!close) {
            try {
                close = opcService.opcRegister(opcOperateDto);
            } catch (Exception e) {
                log.error("receive no/invalid response from server.registerOpc");
            }


            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
               log.error(e.getMessage(),e);
            }
        }
        isOpcConn.set(close);
        return true;
    }

    synchronized public boolean addItemsOpc() {
        OpcServeInfoDto opcserveinfodto = OpcServeInfoDto.builder()
                .opcServeId(this.getOpcServeInfo().getServeid().toString())
                .opcServeIp(this.getOpcServeInfo().getServeip())
                .opcServeName(this.getOpcServeInfo().getServename())
                .build();

        OpcDataDto opcDataDto = OpcDataDto.builder()
                .operate("")
                .opcserveInfo(opcserveinfodto)
                .opcServeMeasurePoints(opcServeInfo.getOpcMeasurePointDtos().values())
                .build();
        OpcOperateDto opcOperateDto = OpcOperateDto.builder().build();
        opcOperateDto.setData(opcDataDto);

        OpcWebReturnDto opcWebReturnDto = null;
        while (opcWebReturnDto == null) {
            try {
                opcWebReturnDto = opcService.opcAddItems(opcOperateDto);
            } catch (Exception e) {
                log.error("receive no/invalid response from server.addItemsOpc");
            }


        }

        if (opcWebReturnDto.getOperate().equals(OpcWebHttpStatusEnum.OPC_WEB_HTTP_STATUC_OK.getStatus())) {
            return true;
        } else {
            return false;
        }
    }

    synchronized public boolean addItemsOpc(Set<String> items) {
        List<OpcMeasurePointDto> opcMeasurePointDtoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(items)) {
            items.forEach(i -> {
                OpcMeasurePointDto opcMeasurePointDto = OpcMeasurePointDto.builder().opcTag(i).opcWriteEnable(0).opcWriteValue(0).build();
                opcServeInfo.getOpcMeasurePointDtos().put(i, opcMeasurePointDto);
                opcMeasurePointDtoList.add(opcMeasurePointDto);
            });
        } else {
            return true;
        }

        OpcServeInfoDto opcserveinfodto = OpcServeInfoDto.builder()
                .opcServeId(this.getOpcServeInfo().getServeid().toString())
                .opcServeIp(this.getOpcServeInfo().getServeip())
                .opcServeName(this.getOpcServeInfo().getServename())
                .build();

        OpcDataDto opcDataDto = OpcDataDto.builder()
                .operate("")
                .opcserveInfo(opcserveinfodto)
                .opcServeMeasurePoints(new ArrayList<OpcMeasurePointDto>())
                .build();
        OpcOperateDto opcOperateDto = OpcOperateDto.builder().build();
        opcOperateDto.setData(opcDataDto);

        opcDataDto.setOpcServeMeasurePoints(opcMeasurePointDtoList);
        OpcWebReturnDto opcWebReturnDto = null;
        while (opcWebReturnDto == null) {
            try {
                opcWebReturnDto = opcService.opcAddItems(opcOperateDto);
            } catch (Exception e) {
                log.error("receive no/invalid response from server.addItemsOpc");
            }


        }

        if (opcWebReturnDto.getOperate().equals(OpcWebHttpStatusEnum.OPC_WEB_HTTP_STATUC_OK.getStatus())) {
            return true;
        } else {
            return false;
        }
    }

    synchronized public boolean removeItemsOpc() {
        OpcServeInfoDto opcserveinfodto = OpcServeInfoDto.builder()
                .opcServeId(this.getOpcServeInfo().getServeid().toString())
                .opcServeIp(this.getOpcServeInfo().getServeip())
                .opcServeName(this.getOpcServeInfo().getServename())
                .build();

        OpcDataDto opcDataDto = OpcDataDto.builder()
                .operate("")
                .opcserveInfo(opcserveinfodto)
                .opcServeMeasurePoints(opcServeInfo.getOpcMeasurePointDtos().values())
                .build();
        OpcOperateDto opcOperateDto = OpcOperateDto.builder().build();
        opcOperateDto.setData(opcDataDto);

        OpcWebReturnDto opcWebReturnDto = null;
        while (opcWebReturnDto == null) {
            try {
                opcWebReturnDto = opcService.opcRemoveItems(opcOperateDto);
            } catch (Exception e) {
                log.error("receive no/invalid response from server.removeItemsOpc");
            }


        }

        if (opcWebReturnDto.getOperate().equals(OpcWebHttpStatusEnum.OPC_WEB_HTTP_STATUC_OK.getStatus())) {
            return true;
        } else {
            return false;
        }

    }

    synchronized public boolean removeItemsOpc(Set<String> items) {
        List<OpcMeasurePointDto> opcMeasurePointDtoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(items)) {
            items.forEach(i -> {
                opcServeInfo.getOpcMeasurePointDtos().remove(i);
                OpcMeasurePointDto opcMeasurePointDto = OpcMeasurePointDto.builder().opcTag(i).opcWriteEnable(0).opcWriteValue(0).build();
                opcMeasurePointDtoList.add(opcMeasurePointDto);
            });
        } else {
            return true;
        }

        OpcServeInfoDto opcserveinfodto = OpcServeInfoDto.builder()
                .opcServeId(this.getOpcServeInfo().getServeid().toString())
                .opcServeIp(this.getOpcServeInfo().getServeip())
                .opcServeName(this.getOpcServeInfo().getServename())
                .build();

        OpcDataDto opcDataDto = OpcDataDto.builder()
                .operate("")
                .opcserveInfo(opcserveinfodto)
                .opcServeMeasurePoints(opcMeasurePointDtoList)
                .build();
        OpcOperateDto opcOperateDto = OpcOperateDto.builder().build();
        opcOperateDto.setData(opcDataDto);

        OpcWebReturnDto opcWebReturnDto = null;
        while (opcWebReturnDto == null) {
            try {
                opcWebReturnDto = opcService.opcRemoveItems(opcOperateDto);
            } catch (Exception e) {
                log.error("receive no/invalid response from server.removeItemsOpc");
            }


        }

        if (opcWebReturnDto.getOperate().equals(OpcWebHttpStatusEnum.OPC_WEB_HTTP_STATUC_OK.getStatus())) {
            return true;
        } else {
            return false;
        }

    }


    synchronized public JSONObject readOpc() {
        OpcServeInfoDto opcserveinfodto = OpcServeInfoDto.builder()
                .opcServeId(this.getOpcServeInfo().getServeid().toString())
                .opcServeIp(this.getOpcServeInfo().getServeip())
                .opcServeName(this.getOpcServeInfo().getServename())
                .build();

        OpcDataDto opcDataDto = OpcDataDto.builder()
                .operate("")
                .opcserveInfo(opcserveinfodto)
                .opcServeMeasurePoints(new ArrayList<OpcMeasurePointDto>())
                .build();
        OpcOperateDto opcOperateDto = OpcOperateDto.builder().build();

        opcOperateDto.setData(opcDataDto);

        /***********************one step unregister opc***********************/
        OpcWebReturnDto opcWebReturnDto = null;
        while (opcWebReturnDto == null) {
            try {

                opcWebReturnDto = opcService.opcReadItems(opcOperateDto);
            } catch (Exception e) {
                log.error("receive no/invalid response from server.readOpc");
            }

        }

        if (opcWebReturnDto.getOperate().equals(OpcWebHttpStatusEnum.OPC_WEB_HTTP_STATUC_OK.getStatus())) {
            JSONObject addItemresult = JSONObject.parseObject(opcWebReturnDto.getData());
            return addItemresult;
        } else {
            log.warn("read data error");
            return null;
        }
    }



    synchronized public JSONObject writeOpc(Collection<OpcMeasurePointDto> opcMeasurePointDtos) {
        OpcServeInfoDto opcserveinfodto = OpcServeInfoDto.builder()
                .opcServeId(this.getOpcServeInfo().getServeid().toString())
                .opcServeIp(this.getOpcServeInfo().getServeip())
                .opcServeName(this.getOpcServeInfo().getServename())
                .build();

        OpcDataDto opcDataDto = OpcDataDto.builder()
                .operate("")
                .opcserveInfo(opcserveinfodto)
                .opcServeMeasurePoints(opcMeasurePointDtos)
                .build();
        OpcOperateDto opcOperateDto = OpcOperateDto.builder().build();

        opcOperateDto.setData(opcDataDto);

        /***********************one step unregister opc***********************/
        OpcWebReturnDto opcWebReturnDto = null;
        while (opcWebReturnDto == null) {
            try {
                opcWebReturnDto = opcService.opcWriteItems(opcOperateDto);
            } catch (Exception e) {
                log.error("receive no/invalid response from server.writeOpc");
            }
        }
        if (opcWebReturnDto.getOperate().equals(OpcWebHttpStatusEnum.OPC_WEB_HTTP_STATUC_OK.getStatus())) {
            JSONObject addItemresult = JSONObject.parseObject(opcWebReturnDto.getData());
            return addItemresult;
        } else {
            return null;
        }
    }


    synchronized public JSONObject readSpecialOpc(Collection<OpcMeasurePointDto> opcMeasurePointDtos) {
        OpcServeInfoDto opcserveinfodto = OpcServeInfoDto.builder()
                .opcServeId(this.getOpcServeInfo().getServeid().toString())
                .opcServeIp(this.getOpcServeInfo().getServeip())
                .opcServeName(this.getOpcServeInfo().getServename())
                .build();

        OpcDataDto opcDataDto = OpcDataDto.builder()
                .operate("")
                .opcserveInfo(opcserveinfodto)
                .opcServeMeasurePoints(opcMeasurePointDtos)
                .build();
        OpcOperateDto opcOperateDto = OpcOperateDto.builder().build();

        opcOperateDto.setData(opcDataDto);

        /***********************one step unregister opc***********************/
        OpcWebReturnDto opcWebReturnDto = null;
        while (opcWebReturnDto == null) {
            try {
                opcWebReturnDto = opcService.opcReadSpecialItems(opcOperateDto);
            } catch (Exception e) {
                log.error("receive no/invalid response from server.writeOpc");
            }
        }
        if (opcWebReturnDto.getOperate().equals(OpcWebHttpStatusEnum.OPC_WEB_HTTP_STATUC_OK.getStatus())) {
            JSONObject addItemresult = JSONObject.parseObject(opcWebReturnDto.getData());
            return addItemresult;
        } else {
            log.warn("read data error");
            return null;
        }
    }

}
