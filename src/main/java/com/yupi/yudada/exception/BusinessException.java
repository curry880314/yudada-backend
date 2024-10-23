package com.yupi.yudada.exception;

import com.yupi.yudada.common.ErrorCode;

/**
 * 自定义异常类
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    //可以手动指定错误码和错误消息时使用，而不依赖于预定义的ErrorCode枚举。
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    // 使用预定义的ErrorCode枚举时，会自动获取错误码和错误消息。
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    // 使用预定义的ErrorCode枚举，并自定义错误消息时使用。同一个错误码可能在不同的业务场景中需要不同的描述。
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public int getCode() {
        return code;
    }
}
