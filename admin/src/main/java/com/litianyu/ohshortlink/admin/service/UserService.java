package com.litianyu.ohshortlink.admin.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.litianyu.ohshortlink.admin.dao.entity.UserDO;
import com.litianyu.ohshortlink.admin.dto.resp.UserRespDTO;

/**
 * 用户接口层
 */
public interface UserService extends IService<UserDO> { // mybatis plus 提供的一系列 CRUD 模板

    UserRespDTO getUserByUsername(String username);
}
