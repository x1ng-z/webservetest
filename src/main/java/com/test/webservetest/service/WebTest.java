package com.test.webservetest.service;

import com.alibaba.fastjson.JSONObject;
import com.test.webservetest.config.OpcWebServeConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/7/7 20:53
 */
//@Service
@Slf4j
public class WebTest {
    private OpcWebServeConfig opcWebServeConfig;
    private ExecutorService executorService;
    private RestTemplate restTemplate;


    @Autowired
    public WebTest(ExecutorService executorService, RestTemplate restTemplate, OpcWebServeConfig opcWebServeConfig) {
        this.executorService = executorService;
        this.opcWebServeConfig = opcWebServeConfig;
        this.restTemplate=restTemplate;

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (Thread.interrupted()){

                    try {
                        JSONObject jsonObject=new JSONObject();
                        jsonObject.put("hello","OpcWebServe3");
                        ResponseEntity<String> rep=restTemplate.postForEntity(opcWebServeConfig.getRoot()+ opcWebServeConfig.getIndex(),jsonObject,String.class);
                        log.info(rep.getBody());
                        JSONObject object =JSONObject.parseObject(rep.getBody());
                        try {
                            TimeUnit.MILLISECONDS.sleep(1000);
                        } catch (InterruptedException e) {
                            log.error(e.getMessage(),e);
                        }
                    } catch (RestClientException e) {
                        log.error(e.getMessage(),e);
                    }
                }

            }
        });

    }
}
