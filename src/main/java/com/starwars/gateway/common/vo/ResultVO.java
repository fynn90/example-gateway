package com.starwars.gateway.common.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResultVO<T> {

    private Integer code;

    private String msg;

    private Boolean success;

    private T data;

    public ResultVO(Integer code, String msg, Boolean success, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.success = success;
    }

    public ResultVO(Integer code, String msg, Boolean success) {
        this.code = code;
        this.msg = msg;
        this.success = success;
    }


}
