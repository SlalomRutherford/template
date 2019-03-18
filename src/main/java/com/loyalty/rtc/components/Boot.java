package com.loyalty.rtc.components;

import org.slf4j.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Boot {

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger("splunk.logger");
        logger.info("This is a test");
    }

    public String testFunc() {
        return "correct";
    }

}