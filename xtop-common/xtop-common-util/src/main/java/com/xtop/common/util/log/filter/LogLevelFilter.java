package com.xtop.common.util.log.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author 杨春光
 * Accepts INFO, ERROR and selected DEBUG logs based on configuration.
 */
public class LogLevelFilter extends Filter<ILoggingEvent> {

    private static final String DEFAULT_SQL_LOGGER_PREFIXES = "com.xquant.xtop.dao,org.apache.ibatis";
    private static final String DEFAULT_DEBUG_LOGGER_PREFIXES = "";

    private boolean acceptInfo = true;
    private boolean acceptError;
    private boolean acceptSqlDebug = true;
    private String sqlLoggerPrefixes = DEFAULT_SQL_LOGGER_PREFIXES;
    private String debugLoggerPrefixes = DEFAULT_DEBUG_LOGGER_PREFIXES;
    private List<String> sqlLoggerPrefixList = Collections.emptyList();
    private List<String> debugLoggerPrefixList = Collections.emptyList();

    @Override
    public void start() {
        sqlLoggerPrefixList = parsePrefixes(sqlLoggerPrefixes);
        debugLoggerPrefixList = parsePrefixes(debugLoggerPrefixes);
        super.start();
    }

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (!isStarted() || event == null) {
            return FilterReply.NEUTRAL;
        }

        Level level = event.getLevel();
        if (acceptInfo && Level.INFO.equals(level)) {
            return FilterReply.ACCEPT;
        }
        if (acceptError && Level.ERROR.equals(level)) {
            return FilterReply.ACCEPT;
        }
        if (acceptSqlDebug && Level.DEBUG.equals(level)
                && (isSqlLogger(event.getLoggerName()) || isDebugLogger(event.getLoggerName()))) {
            return FilterReply.ACCEPT;
        }
        return FilterReply.DENY;
    }

    public void setAcceptInfo(boolean acceptInfo) {
        this.acceptInfo = acceptInfo;
    }

    public void setAcceptError(boolean acceptError) {
        this.acceptError = acceptError;
    }

    public void setAcceptSqlDebug(boolean acceptSqlDebug) {
        this.acceptSqlDebug = acceptSqlDebug;
    }

    public void setSqlLoggerPrefixes(String sqlLoggerPrefixes) {
        this.sqlLoggerPrefixes = sqlLoggerPrefixes;
    }

    public void setDebugLoggerPrefixes(String debugLoggerPrefixes) {
        this.debugLoggerPrefixes = debugLoggerPrefixes;
    }

    private boolean isSqlLogger(String loggerName) {
        return matchesPrefix(sqlLoggerPrefixList, loggerName);
    }

    private boolean isDebugLogger(String loggerName) {
        return matchesPrefix(debugLoggerPrefixList, loggerName);
    }

    private List<String> parsePrefixes(String prefixes) {
        if (prefixes == null || prefixes.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String[] values = prefixes.split(",");
        List<String> result = new ArrayList<>(values.length);
        for (String value : values) {
            String prefix = value == null ? "" : value.trim();
            if (!prefix.isEmpty()) {
                result.add(prefix);
            }
        }
        return result;
    }

    private boolean matchesPrefix(List<String> prefixes, String loggerName) {
        if (loggerName == null || prefixes.isEmpty()) {
            return false;
        }
        for (String prefix : prefixes) {
            if (loggerName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
