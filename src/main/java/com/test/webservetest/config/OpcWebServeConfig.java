package com.test.webservetest.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/7/7 21:00
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "webserve")
public class OpcWebServeConfig {
    private String root;
    private String index;
    private String opc;
}
