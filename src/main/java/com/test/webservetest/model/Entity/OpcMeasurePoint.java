package com.test.webservetest.model.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/7/13 14:34
 */
@Data
@Builder
@TableName("point")
public class OpcMeasurePoint {
    @TableId(value = "pointid",type = IdType.AUTO)
    private Long pointid;
    private String tag;
    private String notion;
    private int writeable;
    private Long opcserveid;
    private String resouce;
    private String standard;
    private String type;
}
