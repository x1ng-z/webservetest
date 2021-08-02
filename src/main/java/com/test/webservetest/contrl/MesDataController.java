package com.test.webservetest.contrl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.test.webservetest.contant.DataResourceEnum;
import com.test.webservetest.mapperService.OpcMeasurePointServiceImpl;
import com.test.webservetest.model.Entity.OpcMeasurePoint;
import com.test.webservetest.model.bean.event.InfluxWriteEvent;
import com.test.webservetest.service.InfluxdbOperateService;
import com.test.webservetest.service.InfluxdbWrite;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/8 12:12
 */
@Controller
@RequestMapping("/data")
@Slf4j
public class MesDataController {

    @Autowired
    private InfluxdbWrite influxdbWrite;

    @Autowired
    private OpcMeasurePointServiceImpl opcMeasurePointService;

    @RequestMapping("/test")
    @ResponseBody
    public String testmes() {
        return "hello";
    }


    @RequestMapping("/savedata")
    @ResponseBody
    public String savemesdata(@RequestBody String mesdata) {
        JSONObject result = new JSONObject();
        result.put("msg", "success");
        log.info("the context is" + mesdata);
        /**
         "points": [
         {
         "time": 1607427246000,
         "measurePoint": "OPC.TS.FN2RD",
         "value": 100
         }
         ]
         */
        try {
            JSONObject mesration = JSONObject.parseObject(mesdata);

            JSONArray jsonArray = mesration.getJSONArray("points");

            List<OpcMeasurePoint> mesAlreadyExistTags = opcMeasurePointService.list();
            Map<String, OpcMeasurePoint> mesAlreadyExistTagsMap = mesAlreadyExistTags.stream().collect(Collectors.toMap(OpcMeasurePoint::getTag, v -> v, (o, n) -> n));
            for (int index = 0; index < jsonArray.size(); index++) {

                JSONObject subjson = jsonArray.getJSONObject(index);

                String influxdbkey = subjson.getString("measurePoint");
                Double influxdbvalue = subjson.getDouble("value");
                Long time=subjson.getLong("time");
                //不包含的点号存储起来
                if (!mesAlreadyExistTagsMap.containsKey(subjson.getString("measurePoint"))) {

                    OpcMeasurePoint point = OpcMeasurePoint.builder()
                            .opcserveid(0L)
                            .resouce(DataResourceEnum.DATA_RESOURCE_MES.getCode())
                            .tag(influxdbkey)
                            .type("float")
                            .build();
                    opcMeasurePointService.save(point);
                } else {
                    OpcMeasurePoint exitpoint = mesAlreadyExistTagsMap.get(subjson.getString("measurePoint"));
                    influxdbkey = ((exitpoint.getStandard() == null) || (exitpoint.getStandard().equals(""))) ? exitpoint.getTag() : exitpoint.getStandard();
                }
                JSONObject influxwritedata = new JSONObject();
                influxwritedata.put(influxdbkey, influxdbvalue);
                InfluxWriteEvent event = new InfluxWriteEvent();
                event.setData(influxwritedata);
                event.setTimestamp(time);
                event.setMeasurement(InfluxdbOperateService.MESMEASUERMENT);
                influxdbWrite.addEvent(event);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.put("msg", "faild");
        }
        return result.toJSONString();
    }


}
