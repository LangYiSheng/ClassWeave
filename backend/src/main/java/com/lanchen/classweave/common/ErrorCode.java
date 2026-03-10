package com.lanchen.classweave.common;

import lombok.Getter;

@Getter
public enum ErrorCode {
    BAD_REQUEST(40000, "请求参数错误"),
    USERNAME_EXISTS(40001, "用户名已存在"),
    INVALID_CREDENTIALS(40002, "用户名或密码错误"),
    INVALID_TOKEN(40100, "登录状态无效"),
    FORBIDDEN(40300, "无权访问"),
    RESOURCE_NOT_FOUND(40400, "资源不存在"),
    SHARE_LINK_EXPIRED(41000, "分享链接已失效");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
