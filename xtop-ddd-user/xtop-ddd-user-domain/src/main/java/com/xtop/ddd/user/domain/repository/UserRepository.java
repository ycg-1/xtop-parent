package com.xtop.ddd.user.domain.repository;

import com.xtop.ddd.user.domain.model.UserEntity;

/**
 * 订单仓储接口
 * 由 Infrastructure 层负责具体实现（如 MyBatis）
 */
public interface UserRepository {
    /**
     * 保存或更新订单
     */
    void save(UserEntity order);

    /**
     * 根据订单ID查询
     */
    UserEntity findById(String orderId);
}