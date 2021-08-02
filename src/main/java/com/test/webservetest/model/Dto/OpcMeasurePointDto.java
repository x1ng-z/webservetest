package com.test.webservetest.model.Dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/7/13 13:01
 */
@Data
@Builder
public class OpcMeasurePointDto {
    private String  opcTag;
    @JSONField(serialize = false)
    private String node;
    private int opcWriteEnable;
    private double opcWriteValue;
}
