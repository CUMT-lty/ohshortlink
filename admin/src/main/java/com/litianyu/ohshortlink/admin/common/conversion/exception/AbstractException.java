package com.litianyu.ohshortlink.admin.common.conversion.exception;

import com.litianyu.ohshortlink.admin.common.conversion.errorcode.IErrorCode;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 抽象项目中三类异常体系，客户端异常、服务端异常以及远程服务调用异常
 *
 * @see ClientException (@see 标签允许用户引用其他类的文档)
 * @see ServiceException
 * @see RemoteException
 */
@Getter // lombok 提供的注解，自动生成 getter 方法
public abstract class AbstractException extends RuntimeException {

    public final String errorCode;

    public final String errorMessage;

    public AbstractException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message, throwable);
        this.errorCode = errorCode.code();
        this.errorMessage = Optional.ofNullable(StringUtils.hasLength(message) ? message : null).orElse(errorCode.message());
    }
}
