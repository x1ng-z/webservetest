package com.test.webservetest.service;


import com.test.webservetest.model.bean.event.Event;
import com.test.webservetest.model.bean.event.InfluxWriteEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/5 1:08
 */
@Service
@Slf4j
public class InfluxdbWrite implements Runnable {


    private InfluxdbOperateService influxdbOperateService;

    private ExecutorService executorService;

    private LinkedBlockingQueue<Event> eventLinkedBlockingQueue = new LinkedBlockingQueue();

    @Autowired
    public InfluxdbWrite(ExecutorService executorService, InfluxdbOperateService influxdbOperateService) {
        this.influxdbOperateService=influxdbOperateService;
        this.executorService=executorService;
        executorService.execute(this);
    }


    public void addEvent(Event event){
        try {
            eventLinkedBlockingQueue.put(event);
        } catch (InterruptedException e) {
            log.error(e.getMessage(),e);
        }
    }


    @Override
    public void run() {

        while (!Thread.currentThread().isInterrupted()){

            try {
                Event event=eventLinkedBlockingQueue.take();
                if(event instanceof InfluxWriteEvent){
                    InfluxWriteEvent influxWriteEvent=(InfluxWriteEvent) event;
                    influxdbOperateService.writeData(influxWriteEvent.getData(),influxWriteEvent.getMeasurement(),influxWriteEvent.getTimestamp());
                }
            } catch (InterruptedException e) {
                log.error(e.getMessage(),e);
            }

        }
    }
}
