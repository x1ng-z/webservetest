package com.test.webservetest.contrl;

import com.alibaba.fastjson.JSONObject;

import com.test.webservetest.model.Dto.OpcMeasurePointDto;
import com.test.webservetest.service.OpcConnectManger;
import com.test.webservetest.service.OpcConnectWatchDog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/11/30 1:19
 */
@RestController
@RequestMapping("/opc")
public class OPCValueController {
    private Logger logger = LoggerFactory.getLogger(OPCValueController.class);

    @Autowired
    private OpcConnectManger opcConnectManger;


    /**
     * {tag:value,
     * tag:value
     * }
     * */
    @RequestMapping("/write")
    public String writeopctags(@RequestParam("tagvalue") String tags) {
        logger.info("/write"+tags);
        JSONObject msg = JSONObject.parseObject(tags);
        JSONObject result = new JSONObject();
        boolean writeresult = true;
        try {
            for (String tag : msg.keySet()) {
                for (OpcConnectWatchDog opcConnectWatchDog : opcConnectManger.getOpcservepool().values()) {
                    if (opcConnectWatchDog.getOpcMeasurePointDtoHashMap().containsKey(tag)) {
                        if(!opcConnectWatchDog.getIsReConnOpc().get()&&opcConnectWatchDog.getIsActive().get()) {
                            OpcMeasurePointDto opcMeasurePointDto = OpcMeasurePointDto.builder().opcWriteEnable(1).opcWriteValue(msg.getFloat(tag)).opcTag(tag).node("").build();
                            opcConnectWatchDog.writeOpc(Arrays.asList(opcMeasurePointDto));
                        }else {
                            continue;
                        }

                    }

                }

            }
            if (writeresult) {
                result.put("msg", "success");
            } else {
                result.put("msg", "failed");
            }
            return result.toJSONString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        result.put("msg", "failed");
        return result.toJSONString();
    }


}
