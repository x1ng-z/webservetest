package com.test.webservetest.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/7/17 9:45
 */
@Configuration
@Slf4j
public class TerminalSignalHandlerConfig implements SignalHandler {
    @Autowired
    private ConfigurableApplicationContext context;
    public TerminalSignalHandlerConfig() {
        Signal.handle(new Signal("INT"), this);

        Signal.handle(new Signal("TERM"), this);
    }

    @Override
    public void handle(Signal signal) {
        log.info("iot drive exit now!");
        context.close();
        System.exit(-1);
    }
}
