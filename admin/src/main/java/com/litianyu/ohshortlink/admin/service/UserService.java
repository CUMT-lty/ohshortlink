package com.litianyu.ohshortlink.admin.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.litianyu.ohshortlink.admin.dao.entity.UserDO;
import com.litianyu.ohshortlink.admin.dto.req.UserRegisterReqDTO;
import com.litianyu.ohshortlink.admin.dto.req.UserUpdateReqDTO;
import com.litianyu.ohshortlink.admin.dto.resp.UserRespDTO;

/**
 * 用户接口层
 */
public interface UserService extends IService<UserDO> { // mybatis plus 提供的一系列 CRUD 模板

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return 用户返回实体
     */
    UserRespDTO getUserByUsername(String username);

    /**
     * 查询用户名是否存在
     *
     * @param username 用户名
     * @return 用户名存在返回 True，不存在返回 False
     */
    Boolean hasUsername(String username);

    /**
     * 注册用户
     *
     * @param requestParam 注册用户请求参数
     */
    void register(UserRegisterReqDTO requestParam);

    /**
     * 根据用户名修改用户
     *
     * @param requestParam 修改用户请求参数
     */
    void update(UserUpdateReqDTO requestParam);
}
