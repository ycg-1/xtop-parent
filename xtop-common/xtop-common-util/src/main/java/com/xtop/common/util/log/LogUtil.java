package com.xtop.common.util.log;

import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.StopWatch;

import java.util.function.Supplier;

/**
 * @author 杨春光
 * Unified log utility for the  project.
 */
public final class LogUtil {

    private static final String TRADE_NO_KEY = "tradeNo";
    private static final String STACK_PACKAGE_XQUANT = "com.xquant";
    private static final String STACK_PACKAGE_GPAM = "com.gpam";
    private static final String SELF_CLASS_NAME = LogUtil.class.getName();

    public static final Logger INFO_LOG = new Logger(LoggerFactory.getLogger("InfoLog_Proceed"));
    public static final Logger ERROR_LOG = new Logger(LoggerFactory.getLogger("ErrorLog_Proceed"));

    private LogUtil() {
    }

    public static void setTradeNo(String tradeNo) {
        if (tradeNo == null || tradeNo.trim().isEmpty()) {
            MDC.remove(TRADE_NO_KEY);
            return;
        }
        MDC.put(TRADE_NO_KEY, tradeNo);
    }

    public static void clearTradeNo() {
        MDC.remove(TRADE_NO_KEY);
    }

    public static <T> T logTakeTime(Supplier<T> task, String taskName, Logger logger) {
        StopWatch stopWatch = new StopWatch(taskName);
        String fullTaskName = buildTaskNameWithStackTrace(taskName);
        stopWatch.start(fullTaskName);
        try {
            return task.get();
        } finally {
            stopWatch.stop();
            LogUtil.INFO_LOG.info(stopWatch.prettyPrint());
        }
    }

    public static void logTakeTime(Runnable task, String taskName, Logger logger) {
        StopWatch stopWatch = new StopWatch(taskName);
        String fullTaskName = buildTaskNameWithStackTrace(taskName);
        stopWatch.start(fullTaskName);
        try {
            task.run();
        } finally {
            stopWatch.stop();
            LogUtil.INFO_LOG.info(stopWatch.prettyPrint());
        }
    }

    private static String buildTaskNameWithStackTrace(String taskName) {
        StringBuilder stackBuilder = new StringBuilder();
        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
            if (stackTraceElement == null) {
                continue;
            }
            String stackInfo = stackTraceElement.toString();
            if (stackInfo == null || stackInfo.trim().isEmpty()) {
                continue;
            }
            if (!stackInfo.contains(STACK_PACKAGE_XQUANT) && !stackInfo.contains(STACK_PACKAGE_GPAM)) {
                continue;
            }
            if (stackInfo.contains(SELF_CLASS_NAME)) {
                continue;
            }
            stackBuilder.append(stackInfo).append('\n');
        }
        if (stackBuilder.length() == 0) {
            return taskName;
        }
        return taskName + "\nStackTrace:\n" + stackBuilder;
    }
}
