package com.litianyu.ohshortlink.admin.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.litianyu.ohshortlink.admin.dao.entity.UserDO;

/**
 * 用户持久层
 */
public interface UserMapper extends BaseMapper<UserDO> { // TODO：mybatis 实现的动态代理
    // BaseMapper 点进去可以看到有很多 insert、select 等操作，省去了很多不必要的代码
}
