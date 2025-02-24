package com.bt.yevhentucha.utils;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.util.ArrayList;
import java.util.List;

public class InMemoryAppender extends AppenderBase<ILoggingEvent> {

    private final List<String> log = new ArrayList<>();

    @Override
    protected void append(ILoggingEvent eventObject) {
        log.add(eventObject.getFormattedMessage());
    }

    @Override public synchronized void doAppend(ILoggingEvent eventObject) {
        super.doAppend(eventObject);
        log.add(eventObject.getFormattedMessage());
    }

    public List<String> getLog() {
        return log;
    }

    public void clear() {
        log.clear();
    }
}
