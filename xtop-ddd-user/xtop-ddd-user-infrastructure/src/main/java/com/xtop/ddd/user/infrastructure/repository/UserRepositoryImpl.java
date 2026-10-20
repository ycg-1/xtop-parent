package com.xtop.ddd.user.infrastructure.repository;

import com.xtop.ddd.user.domain.model.UserEntity;
import com.xtop.ddd.user.domain.repository.UserRepository;
import org.springframework.stereotype.Repository;

/**
 * 订单领域模型的持久化实现（Infrastructure 实现 Domain 定义的接口）
 */
@Repository
public class UserRepositoryImpl implements UserRepository {
    @Override
    public void save(UserEntity order) {

    }

    @Override
    public UserEntity findById(String orderId) {
        return null;
    }

    // 这里注入具体的 Mapper，比如 @Autowired private OrderMapper orderMapper;

}