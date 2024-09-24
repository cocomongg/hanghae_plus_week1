package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.point.model.UserPoint;
import java.util.Optional;

public interface UserPointRepository {
    Optional<UserPoint> selectById(long id);
    UserPoint insertOrUpdate(UserPoint userPoint);
}
