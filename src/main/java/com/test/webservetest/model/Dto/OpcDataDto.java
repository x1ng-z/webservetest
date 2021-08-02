package com.test.webservetest.model.Dto;

import lombok.Builder;
import lombok.Data;

import java.util.Collection;
import java.util.List;

/**
 * \{data:\{
 *  * 		operate: register,
 *  * 		opcserveInfo:
 *  * 					\{
 *  * 						opcServeId:123,
 *  * 						opcServeIp:192.168.10.212,
 *  * 						opcServeName:JXServe
 *  * 					\},
 *  * 	    opcServeMeasurePoints:
 *  * 							\[
 *  * 								\{
 *  * 									opcTag:apc.value,
 *  * 									opcWriteEnable:1,
 *  * 									opcWriteValue:1.2
 *  * 								\},
 *  * 								\{...\}
 *  *
 *  * 							\]
 *  * 	\}
 *  * \}
 * @author zzx
 * @version 1.0
 * @date 2021/7/13 13:03
 */
@Data
@Builder
public class OpcDataDto
{
    private String operate;
    private OpcServeInfoDto opcserveInfo;
    Collection<OpcMeasurePointDto> opcServeMeasurePoints;
}
