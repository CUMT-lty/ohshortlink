package com.litianyu.ohshortlink.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.litianyu.ohshortlink.admin.common.conversion.result.Result;
import com.litianyu.ohshortlink.admin.common.conversion.result.Results;
import com.litianyu.ohshortlink.admin.dto.resp.UserActualRespDTO;
import com.litianyu.ohshortlink.admin.dto.resp.UserRespDTO;
import com.litianyu.ohshortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理控制层
 */
@RestController
@RequiredArgsConstructor // 使用 lombok 插件提供的构造器的方式注入
public class UserController {

    private final UserService userService;

    /**
     * 根据用户名查询用户信息
     */
    @GetMapping("/api/short-link/admin/v1/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username") String username)  {
        return Results.success(userService.getUserByUsername(username)); // 全局返回对象应该只是 Controller 层的概念
        // 如果 getUserByUsername 返回的结果是 null，就会走全局异常拦截器
    }

    /**
     * 根据用户名查询无脱敏用户信息
     */
    @GetMapping("/api/short-link/admin/v1/actual/user/{username}")
    public Result<UserActualRespDTO> getActualUserByUsername(@PathVariable("username") String username) {
        return Results.success(BeanUtil.toBean(userService.getUserByUsername(username), UserActualRespDTO.class));
    }

    /**
     * 查询用户名是否存在
     */
    @GetMapping("/api/short-link/admin/v1/user/has-username")
    public Result<Boolean> hasUsername(@RequestParam("username") String username) {
        return Results.success(userService.hasUsername(username));
    }

}
