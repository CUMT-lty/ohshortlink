package com.litianyu.ohshortlink.admin.dto.resp;

import lombok.Data;

/**
 * 用户返回参数响应
 */
@Data // 和 lombok 插件，有关，省去大量的 get()、 set()、 toString() 等方法
public class UserRespDTO {

    /**
     * id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String mail;
}

