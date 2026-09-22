package com.tcyao.nid.common.messaging;

public interface EventPublisher<T> {
    void publish(T event);
}