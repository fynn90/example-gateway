package com.starwars.gateway.common.utils;


import com.starwars.gateway.common.enums.CommonStatusCodeEnum;
import com.starwars.gateway.common.enums.ResultStatusCode;
import com.starwars.gateway.common.vo.ResultVO;

import java.util.Map;

/**
 * @description: 统一封装返回工具类
 * @version: 1.0.0
 */
public class ResultVoUtil<T> {

    /**
     @description 操作成功时无数据返回
     */
    public static <T> ResultVO<T> success(){
        return new ResultVO<>(CommonStatusCodeEnum.SUCCESS.getCode(),
                CommonStatusCodeEnum.SUCCESS.getMsg(),true);
    }

    /**
     @description 操作成功并返回数据
     */
    public static <T> ResultVO<T> success(T data){
        return new ResultVO<>(
                CommonStatusCodeEnum.SUCCESS.getCode(),
                CommonStatusCodeEnum.SUCCESS.getMsg(),
                true,
                data
        );
    }

    /**
     @description 操作错误返回
     */
    public static <T> ResultVO<T> error(){
        return new ResultVO<>(
                CommonStatusCodeEnum.SERVER_ERROR.getCode(),
                CommonStatusCodeEnum.SERVER_ERROR.getMsg(),
                false
        );
    }

    /**
     * @description 操作失败时返回提示信息
     */
    public static <T> ResultVO<T> failed(ResultStatusCode resultStatusCode){
        return new ResultVO<>(resultStatusCode.getCode(),resultStatusCode.getMsg(),false);
    }
}
