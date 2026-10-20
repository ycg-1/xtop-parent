package com.xtop.common.model;
import lombok.Data;
@Data
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMsg("操作成功");
        r.setData(data);
        return r;
    }

    public static Result<Object> error(String msg) {
        Result<Object> r = new Result<>();
        r.setCode(500);
        r.setMsg(msg);
        return r;
    }
}