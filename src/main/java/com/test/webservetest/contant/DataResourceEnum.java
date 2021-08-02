package com.test.webservetest.contant;

import lombok.Getter;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/7/21 8:26
 */
@Getter
public enum DataResourceEnum {
    DATA_RESOURCE_MES("mes","mes"),
    DATA_RESOURCE_OPC("opc","opc")
    ;
    private String code;
    private String desc;

    DataResourceEnum(java.lang.String code, java.lang.String desc) {
        this.code = code;
        this.desc = desc;
    }
}
