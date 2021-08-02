package com.test.webservetest.service;


import com.test.webservetest.model.bean.UploadNewData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/4 13:38
 */
@Component
@Slf4j
@Data
public class OpcConnectManger {
    /*key=iot opcserveid*/
    private Map<Long, OpcConnectWatchDog> opcservepool = new ConcurrentHashMap();
    /*key=iot opcserveid*/
    private Map<Long,Map<String, UploadNewData>> newdata=new ConcurrentHashMap();

    private OpcService opcService;

    private ExecutorService executorService;

    @Autowired
    public OpcConnectManger(ExecutorService executorService, OpcService opcService) {
        this.executorService = executorService;
        this.opcService= opcService;
    }

    public  boolean activeOpcWatchDog(Long iotid){
        OpcConnectWatchDog opcConnectWatchDog=opcservepool.get(iotid);
        if(ObjectUtils.isEmpty(opcConnectWatchDog)){
            return false;
        }else {
            if(!opcConnectWatchDog.getIsActive().get()){
                opcConnectWatchDog.activeWatchDog();
            }
            return true;
        }
    }

    public boolean powerOffOpcWatchDog(Long iotid){
        OpcConnectWatchDog opcConnectWatchDog=opcservepool.remove(iotid);
        if(ObjectUtils.isEmpty(opcConnectWatchDog)){
            return true;
        }else {
           return opcConnectWatchDog.powoffWatchDog();
        }
    }

    public void addOpcWatchDog(OpcConnectWatchDog opcConnectWatchDog){
        opcservepool.put(opcConnectWatchDog.getOpcServeInfo().getIotid(), opcConnectWatchDog);
    }

    public OpcConnectWatchDog getOpcWatchDog(Long iotid){
        return opcservepool.get(iotid);
    }

    public OpcConnectWatchDog removeOpcWatchDog(Long iotid){
        return opcservepool.remove(iotid);
    }

    @PreDestroy
    void close() {
        log.info("opc connect try to shutdown");
        if(!CollectionUtils.isEmpty(opcservepool)){
            opcservepool.forEach((k,v)->{
                v.powoffWatchDog();
            });
        }
    }

    public void updateNewData(Long iotId,Map<String,UploadNewData> dataMap){
            newdata.put(iotId,dataMap);
    }

}
