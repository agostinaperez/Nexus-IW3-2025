package edu.iua.nexus.events;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetailEvent extends ApplicationEvent{
    public enum TypeEvent {
        SAVE_DETAIL
    }

    public DetailEvent(Object source, TypeEvent typeEvent) {
        super(source);
        this.typeEvent = typeEvent;
    }

    private TypeEvent typeEvent;
    private Object extraData;
}

