package com.test.webservetest.contant;

import lombok.Getter;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/7/13 13:05
 */
@Getter
public enum OpcOperateEnum {
    /***/
    OPC_OPERATE_REGISTER("register","注册"),
    OPC_OPERATE_UNREGISTER("unRegister","解注册"),
    OPC_OPERATE_ADD_MEASUREPOINTS("addMeasurePoints","添加点位"),
    OPC_OPERATE_REMOVE_MEASUREPOINTS("removeMeasurePoints","移除点位"),
    OPC_OPERATE_READ_MEASUREPOINTS("readMeasurePoints","读注册点位数据"),
    OPC_OPERATE_READ_SPECIAL_MEASUREPOINTS("readSpecialMeasurePoints","读special注册点位数据"),
    OPC_OPERATE_WRITE_MEASUREPOINTS("writeMeasurePoints","写注册点位数据")
    ;
    OpcOperateEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    private String code;
    private String desc;


}
