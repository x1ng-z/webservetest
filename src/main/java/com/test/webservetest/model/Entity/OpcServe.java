package com.test.webservetest.model.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/7/13 14:30
 */
@Data
@Builder
@TableName("opcserve")
public class OpcServe {
    @TableId(value = "serveid",type = IdType.AUTO)
    private Long serveid;
    private String servename;
    private String serveip;
}
