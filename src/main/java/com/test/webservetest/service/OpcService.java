package com.test.webservetest.service;

import com.alibaba.fastjson.JSONObject;

import com.test.webservetest.config.OpcWebServeConfig;
import com.test.webservetest.contant.OpcOperateEnum;
import com.test.webservetest.contant.OpcWebHttpStatusEnum;
import com.test.webservetest.model.Dto.OpcOperateDto;
import com.test.webservetest.model.Dto.OpcWebReturnDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/7/13 15:37
 */
@Service
@Slf4j
public class OpcService {

    @Autowired
    private HttpClientService httpClientService;

    @Autowired
    private OpcWebServeConfig opcWebServeconfig;

    public boolean opcRegister(OpcOperateDto opcOperateDto) {
        log.debug("opcRegister post={}", opcOperateDto);

        opcOperateDto.getData().setOperate(OpcOperateEnum.OPC_OPERATE_REGISTER.getCode());

        String opcWebReturnDto = httpClientService.postForEntity(opcWebServeconfig.getRoot() + opcWebServeconfig.getOpc(),
                opcOperateDto,
                String.class);
        log.debug("opcRegister return={}", opcWebReturnDto);
        if (ObjectUtils.isEmpty(opcWebReturnDto)) {
            return false;
        } else {
            OpcWebReturnDto opcWebReturn = null;
            try {
                opcWebReturn = JSONObject.parseObject(opcWebReturnDto, OpcWebReturnDto.class);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return false;
            }
            if (opcWebReturn.getOperate().equals(OpcWebHttpStatusEnum.OPC_WEB_HTTP_STATUC_OK.getStatus())) {
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean opcUnRegister(OpcOperateDto opcOperateDto) {
        log.debug("opcUnRegister post={}", opcOperateDto);
        opcOperateDto.getData().setOperate(OpcOperateEnum.OPC_OPERATE_UNREGISTER.getCode());
        String opcWebReturnDto = httpClientService.postForEntity(opcWebServeconfig.getRoot() + opcWebServeconfig.getOpc(),
                opcOperateDto,
                String.class);
        log.debug("opcUnRegister return={}", opcWebReturnDto);
        if (ObjectUtils.isEmpty(opcWebReturnDto)) {
            return false;
        } else {
            OpcWebReturnDto opcWebReturn = null;
            try {
                opcWebReturn = JSONObject.parseObject(opcWebReturnDto, OpcWebReturnDto.class);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return false;
            }
            if (opcWebReturn.getOperate().equals(OpcWebHttpStatusEnum.OPC_WEB_HTTP_STATUC_OK.getStatus())) {
                return true;
            } else {
                return false;
            }
        }
    }

    public OpcWebReturnDto opcAddItems(OpcOperateDto opcOperateDto) {
        log.debug("opcAddItems post={}", opcOperateDto);
        opcOperateDto.getData().setOperate(OpcOperateEnum.OPC_OPERATE_ADD_MEASUREPOINTS.getCode());
        String opcWebReturnDto = httpClientService.postForEntity(opcWebServeconfig.getRoot() + opcWebServeconfig.getOpc(),
                opcOperateDto,
                String.class);
        log.debug("opcAddItems return={}", opcWebReturnDto);
        if (ObjectUtils.isEmpty(opcWebReturnDto)) {
            return null;
        } else {

            OpcWebReturnDto opcWebReturn = null;
            try {
                opcWebReturn = JSONObject.parseObject(opcWebReturnDto, OpcWebReturnDto.class);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return null;
            }
            return opcWebReturn;

//            if (opcWebReturn.getOperate().equals(OpcWebHttpStatusEnum.OPC_WEB_HTTP_STATUC_OK.getStatus())) {
//                JSONObject addItemresult = JSONObject.parseObject(opcWebReturn.getData());
//                return addItemresult;
//            } else {
//                return null;
//            }
        }
    }

    public OpcWebReturnDto opcRemoveItems(OpcOperateDto opcOperateDto) {
        opcOperateDto.getData().setOperate(OpcOperateEnum.OPC_OPERATE_REMOVE_MEASUREPOINTS.getCode());
        String opcWebReturnDto = httpClientService.postForEntity(opcWebServeconfig.getRoot() + opcWebServeconfig.getOpc(),
                opcOperateDto,
                String.class);
        log.debug("opcRemoveItems return={}", opcWebReturnDto);

        if (ObjectUtils.isEmpty(opcWebReturnDto)) {
            return null;
        } else {
            OpcWebReturnDto opcWebReturn = null;
            try {
                opcWebReturn = JSONObject.parseObject(opcWebReturnDto, OpcWebReturnDto.class);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return null;
            }
            return opcWebReturn;
        }
    }

    public OpcWebReturnDto opcReadItems(OpcOperateDto opcOperateDto) {
        opcOperateDto.getData().setOperate(OpcOperateEnum.OPC_OPERATE_READ_MEASUREPOINTS.getCode());

        String opcWebReturnDto = httpClientService.postForEntity(opcWebServeconfig.getRoot() + opcWebServeconfig.getOpc(),
                opcOperateDto,
                String.class);
        log.debug("opcReadItems return={}", opcWebReturnDto);

        if (ObjectUtils.isEmpty(opcWebReturnDto)) {
            return null;
        } else {
            opcWebReturnDto = opcWebReturnDto.replace(":nan,", ":0,").replace(":inf,", ":" + Double.MAX_VALUE + ",").replace(":-inf,", ":" + Double.MIN_VALUE + ",");

            OpcWebReturnDto opcWebReturn = null;
            try {
                opcWebReturn = JSONObject.parseObject(opcWebReturnDto, OpcWebReturnDto.class);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return null;
            }
            return opcWebReturn;
        }
    }

    public OpcWebReturnDto opcWriteItems(OpcOperateDto opcOperateDto) {
        opcOperateDto.getData().setOperate(OpcOperateEnum.OPC_OPERATE_WRITE_MEASUREPOINTS.getCode());
        String opcWebReturnDto = httpClientService.postForEntity(opcWebServeconfig.getRoot() + opcWebServeconfig.getOpc(),
                opcOperateDto,
                String.class);
        log.debug("opcWriteItems return={}", opcWebReturnDto);

        if (ObjectUtils.isEmpty(opcWebReturnDto)){
            return null;
        } else {
            OpcWebReturnDto opcWebReturn = null;
            try {
                opcWebReturn = JSONObject.parseObject(opcWebReturnDto, OpcWebReturnDto.class);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return null;
            }
            return opcWebReturn;
        }
    }

    public OpcWebReturnDto opcReadSpecialItems(OpcOperateDto opcOperateDto) {
        opcOperateDto.getData().setOperate(OpcOperateEnum.OPC_OPERATE_READ_SPECIAL_MEASUREPOINTS.getCode());
        String opcWebReturnDto = httpClientService.postForEntity(opcWebServeconfig.getRoot() + opcWebServeconfig.getOpc(),
                opcOperateDto,
                String.class);
        log.debug("opcReadItems return={}", opcWebReturnDto);

        if (ObjectUtils.isEmpty(opcWebReturnDto)){
            return null;
        } else {
            OpcWebReturnDto opcWebReturn = null;
            try {
                opcWebReturnDto = opcWebReturnDto.replace(":nan,", ":0,").replace(":inf,", ":" + Double.MAX_VALUE + ",").replace(":-inf,", ":" + Double.MIN_VALUE + ",");
                opcWebReturn = JSONObject.parseObject(opcWebReturnDto, OpcWebReturnDto.class);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return null;
            }
            return opcWebReturn;
        }
    }
}
