package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.point.model.UserPoint;

public interface UserPointRepository {
    UserPoint selectById(long id);
    UserPoint insertOrUpdate(UserPoint userPoint);
}
