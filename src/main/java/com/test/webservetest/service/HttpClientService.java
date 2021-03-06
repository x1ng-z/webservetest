package com.test.webservetest.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * create by yjq on 2019-9-21
 */
@Slf4j
@Service
public class HttpClientService {
    public  <T> T postForEntity(String url, Object myclass, Class<T> returnClass) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost posturl = new HttpPost(url);

        String jsonSting = JSON.toJSONString(myclass);
        StringEntity entity = new StringEntity(jsonSting, "UTF-8");
        posturl.setEntity(entity);
        posturl.setHeader("Content-Type", "application/json;charset=utf8");
        // 响应模型
        CloseableHttpResponse response = null;
        try {
            // 由客户端执行(发送)Post请求
            response = httpClient.execute(posturl);
            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                String stringValue=EntityUtils.toString(responseEntity);
                return JSONObject.parseObject(stringValue,returnClass);
            }
        } catch (Exception e) {
           log.error(e.getMessage(),e);
        } finally {
            try {
                // 释放资源
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
