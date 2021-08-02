package com.test.webservetest.model.Dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/7/13 12:59
 */
@Data
@Builder
public class OpcServeInfoDto {
    private String opcServeId;
    private String opcServeIp;
    private String opcServeName;
}
