package com.starwars.gateway.common.enums;

/**
 * @description
 */
public interface ResultStatusCode{

    /**
     * 获取返回状态码
     * @return code
     */
    int getCode();

    /**
     * 获取返回消息
     * @return msg
     */
    String getMsg();
}
