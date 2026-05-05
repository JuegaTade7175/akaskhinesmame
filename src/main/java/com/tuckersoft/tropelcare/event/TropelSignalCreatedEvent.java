package com.tuckersoft.tropelcare.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TropelSignalCreatedEvent extends ApplicationEvent {

    private final Long signalId;

    public TropelSignalCreatedEvent(Object source, Long signalId) {
        super(source);
        this.signalId = signalId;
    }
}