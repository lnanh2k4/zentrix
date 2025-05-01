package com.zentrix.model.utils;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date February 26, 2025
 */
public class LogUtils {
    static Logger logger;

    public static void init() {
        logger = Logger.getLogger(LogUtils.class);
        BasicConfigurator.configure();
    }

    public static void info(String serviceCode, Object object) {
        logger.info(new StringBuilder().append("[").append(serviceCode).append("]: ").append(object));
    }

    public static void info(Object object) {
        logger.info(object);
    }

    public static void debug(Object object) {
        logger.debug(object);
    }

    public static void error(Object object) {
        logger.error(object);
    }

    // public static void error(Object object) {
    // logger.error(object);
    // }

    public static void warn(Object object) {
        logger.warn(object);
    }
}
