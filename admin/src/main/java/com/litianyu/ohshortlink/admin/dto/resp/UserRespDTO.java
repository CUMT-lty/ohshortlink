package com.litianyu.ohshortlink.admin.dto.resp;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.litianyu.ohshortlink.admin.common.serialize.PhoneDesensitizationSerializer;
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
     * 手机号，此字段在返回的时候需要做数据脱敏处理
     */
    @JsonSerialize(using = PhoneDesensitizationSerializer.class) // jackson 提供的注解，对注解字段进行解析
    private String phone;

    /**
     * 邮箱
     */
    private String mail;
}

