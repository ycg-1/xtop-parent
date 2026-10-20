package com.xtop.common.util.log;

/**
 * @author 杨春光
 * Project logger wrapper that only exposes INFO and ERROR logging methods.
 */
public class Logger {

    private final org.slf4j.Logger delegate;

    public Logger(org.slf4j.Logger delegate) {
        this.delegate = delegate;
    }

    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    public void info(String message) {
        delegate.info(message);
    }

    public void info(String format, Object... arguments) {
        delegate.info(format, arguments);
    }

    public void error(String message) {
        delegate.error(message);
    }

    public void error(String message, Throwable throwable) {
        delegate.error(message, throwable);
    }

    public void error(String format, Object... arguments) {
        delegate.error(format, arguments);
    }
}
