package com.test.webservetest.mapperService;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.test.webservetest.mapper.OpcServeMapper;
import com.test.webservetest.model.Entity.OpcServe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OpcServeMapperServiceImpl extends ServiceImpl<OpcServeMapper, OpcServe> {
    @Autowired
    private OpcServeMapper opcServeMapper;





}