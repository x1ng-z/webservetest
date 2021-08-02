package com.test.webservetest.model.bean;

import com.test.webservetest.model.Dto.OpcMeasurePointDto;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/4 14:37
 */
@Data
public class OpcServeInfo {
    private static AtomicInteger generator =new AtomicInteger(10000);
    /**opc serve id 不同的opc此id不可相同*/
    private final Integer serveid= generator.addAndGet(1);
    /**opc serve name*/
    private String servename;
    /**opc serve ip*/
    private String serveip;

    private Map<String, OpcMeasurePointDto> opcMeasurePointDtos=new HashMap<>();

    private Long iotid;
}
