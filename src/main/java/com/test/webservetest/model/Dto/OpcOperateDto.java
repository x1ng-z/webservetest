package com.test.webservetest.model.Dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/7/13 16:22
 */
@Data
@Builder
public class OpcOperateDto {

    private OpcDataDto data;
}
