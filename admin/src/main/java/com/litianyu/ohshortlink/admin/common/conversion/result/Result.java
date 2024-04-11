package com.litianyu.ohshortlink.admin.common.conversion.result;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 全局返回对象
 */
@Data
@Accessors(chain = true) // lombok 提供的链式访问功能，setter 方法返回的是 this，而不是 void，这样方法就可以进行链式调用
public class Result<T> implements Serializable {


    @Serial // 指定类的序列化版本号，以确保序列化对象和反序列化对象的版本一致
    private static final long serialVersionUID = 5679018624309023727L;

    /**
     * 正确返回码
     */
    public static final String SUCCESS_CODE = "0";

    /**
     * 返回码
     */
    private String code;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 请求ID
     */
    private String requestId; // TODO: 复杂系统中，和全链路 id 绑定（后续应该要用分布式雪花算法来生成）

    /**
     * 成功标识，以 id 开头的方法，在 json 反序列化的时候会被序列化成字段
     */
    public boolean isSuccess() {
        return SUCCESS_CODE.equals(code);
    }
}
