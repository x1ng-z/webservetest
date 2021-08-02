package com.test.webservetest.model.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/7/13 15:12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpcWebReturnDto {
     private String data;
     private String message;
     private String operate;
     private String timestamp;
}
