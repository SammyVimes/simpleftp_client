package com.company;

/**
 * Created by Семён on 07.04.2015.
 */
public abstract class Handler {

    private EventThread eventThread;

    public abstract void handle(final EventThread.Event event);

    protected Handler(EventThread eventThread) {
        this.eventThread = eventThread;
    }

    public void addEvent(String type, Object data) {
        eventThread.addEvent(type, data, this);
    }

}
