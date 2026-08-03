package com.tcyao.nid.common.infra.cache;

import java.time.Duration;


public interface ICacheService {
    void put(String key, Object value, Duration ttl);

    <T> T get(String key, Class<T> type);

    void delete(String key);
}
