package com.test.webservetest.contant;

import lombok.Getter;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/7/13 14:46
 */
@Getter
public enum OpcWebHttpStatusEnum {
    /***/
    OPC_WEB_HTTP_STATUC_OK("200 OK",""),
    OPC_WEB_HTTP_STATUC_BAD_REQUEST("400 Bad Request",""),
    OPC_WEB_HTTP_STATUC_UNAUTHORIZED("401 Unauthorized",""),
    OPC_WEB_HTTP_STATUC_FORBIDDEN("403 Forbidden",""),
    OPC_WEB_HTTP_STATUC_NO_FOUND("404 Not Found",""),
    OPC_WEB_HTTP_STATUC_UNSUPPORTED("415 Unsupported Media Type",""),
    OPC_WEB_HTTP_STATUC_SERVE_ERROR("500 Internal Server Error","")
    ;

    OpcWebHttpStatusEnum(String status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    private String status;
    private String desc;

}
