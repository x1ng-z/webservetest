package com.test.webservetest.model.bean;

import lombok.Data;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/7/21 8:49
 */

@Data
public class UploadNewData {
        /**
         * 数据时间，ms
         */
        private Long time;

        /**
         * 点位值
         */
        private Object value;

        /**
         * 具体点位
         */
        private String measurePoint;

        /**
         * 节点编码
         */
        private String node;

}
