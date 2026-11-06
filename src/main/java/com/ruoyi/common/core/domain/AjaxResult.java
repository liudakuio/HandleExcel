package com.ruoyi.common.core.domain;

import java.util.HashMap;

/**
 * 若依风格简化统一返回体，本示例仅用于校验失败与异常提示。
 * 成功场景直接返回 Excel 文件流，不经过此类。
 *
 * @author ruoyi
 */
public class AjaxResult extends HashMap<String, Object> {

    private static final long serialVersionUID = 1L;

    public static final int SUCCESS_CODE = 200;
    public static final int ERROR_CODE = 500;

    private AjaxResult(int code, String msg, Object data) {
        super.put("code", code);
        super.put("msg", msg);
        if (data != null) {
            super.put("data", data);
        }
    }

    public static AjaxResult success() {
        return new AjaxResult(SUCCESS_CODE, "操作成功", null);
    }

    public static AjaxResult success(Object data) {
        return new AjaxResult(SUCCESS_CODE, "操作成功", data);
    }

    public static AjaxResult error(String msg) {
        return new AjaxResult(ERROR_CODE, msg, null);
    }

    public static AjaxResult error(int code, String msg) {
        return new AjaxResult(code, msg, null);
    }

    @Override
    public AjaxResult put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
