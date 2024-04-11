package com.litianyu.ohshortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.litianyu.ohshortlink.admin.dao.entity.UserDO;
import com.litianyu.ohshortlink.admin.dao.mapper.UserMapper;
import com.litianyu.ohshortlink.admin.dto.resp.UserRespDTO;
import com.litianyu.ohshortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;


/**
 * 用户接口实现层
 */

@Service // 标记为一个 bean
@RequiredArgsConstructor  // Lombok 提供的注解，会生成一个包含常量，和标识了 NotNull 的变量的构造方法
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    RBloomFilter<String> userRegisterCachePenetrationBloomFilter; // redisson 提供的布隆过滤器

    @Override
    public UserRespDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class) // 规定好要查询的实体对象
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper); // ServiceImpl 中有 baseMapper
        UserRespDTO result = new UserRespDTO();
        BeanUtils.copyProperties(userDO, result); // springboot 提供的方法，将 dao 转为 dto
        return result;
    }

    @Override
    public Boolean hasUsername(String username) {
        return !userRegisterCachePenetrationBloomFilter.contains(username);
    }
}
