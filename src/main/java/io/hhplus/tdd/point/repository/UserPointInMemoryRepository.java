package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.model.UserPoint;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class UserPointInMemoryRepository implements UserPointRepository{

    private final UserPointTable userPointTable;

    @Override
    public Optional<UserPoint> selectById(long id) {
        return Optional.ofNullable(userPointTable.selectById(id));
    }

    @Override
    public UserPoint insertOrUpdate(UserPoint userPoint) {
        return userPointTable.insertOrUpdate(userPoint.id(), userPoint.point());
    }
}
