package com.test.webservetest.mapperService;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.test.webservetest.mapper.OpcMeasurePointMapper;
import com.test.webservetest.model.Entity.OpcMeasurePoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/7/13 14:41
 */
@Service
public class OpcMeasurePointServiceImpl extends ServiceImpl<OpcMeasurePointMapper, OpcMeasurePoint> {

    @Autowired
    private OpcMeasurePointMapper opcMeasurePointMapper;


}
