package com.litianyu.ohshortlink.admin.controller;

import com.litianyu.ohshortlink.admin.common.conversion.result.Result;
import com.litianyu.ohshortlink.admin.common.conversion.result.Results;
import com.litianyu.ohshortlink.admin.dto.resp.UserRespDTO;
import com.litianyu.ohshortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

}
