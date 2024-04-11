package com.litianyu.ohshortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.litianyu.ohshortlink.admin.dao.entity.UserDO;
import com.litianyu.ohshortlink.admin.dao.mapper.UserMapper;
import com.litianyu.ohshortlink.admin.dto.resp.UserRespDTO;
import com.litianyu.ohshortlink.admin.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;


/**
 * 用户接口实现层
 */

@Service // 标记为一个 bean
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 用户返回实体
     */
    @Override
    public UserRespDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class) // 规定好要查询的实体对象
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper); // ServiceImpl 中有 baseMapper
        UserRespDTO result = new UserRespDTO();
        BeanUtils.copyProperties(userDO, result); // springboot 提供的方法，将 dao 转为 dto
        return result;
    }
}
