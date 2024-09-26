package io.hhplus.tdd.point.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Component;

@Component
public class SelectiveLockFactory {
    private final ConcurrentHashMap<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public ReentrantLock getLock(long id) {
        return lockMap.computeIfAbsent(id, key -> new ReentrantLock());
    }
}
