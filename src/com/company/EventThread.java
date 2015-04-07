package com.company;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Семён on 07.04.2015.
 */
public class EventThread extends Thread {

    public static class Event {

        private String type;

        private Object data;

        private Handler handler;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }

        private Event(String type, Object data, Handler handler) {
            this.type = type;
            this.data = data;
            this.handler = handler;
        }

    }

    private BlockingQueue<Event> events = new ArrayBlockingQueue<Event>(30);

    @Override
    public void run() {
        while (true) {
            try {
                Event event = events.take();
                event.handler.handle(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addEvent(final String type, final Object data, final Handler handler) {
        events.add(new Event(type, data, handler));
    }

}