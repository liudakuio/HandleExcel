package com.ruoyi.framework.web.controller;

import com.ruoyi.common.core.domain.AjaxResult;

/**
 * 若依风格简化控制器基类，仅保留常用返回快捷方法。
 *
 * @author ruoyi
 */
public class BaseController {

    protected AjaxResult success() {
        return AjaxResult.success();
    }

    protected AjaxResult success(Object data) {
        return AjaxResult.success(data);
    }

    protected AjaxResult error(String msg) {
        return AjaxResult.error(msg);
    }

    protected AjaxResult error(int code, String msg) {
        return AjaxResult.error(code, msg);
    }

    protected AjaxResult toAjax(boolean result) {
        return result ? success() : error("操作失败");
    }

    protected AjaxResult toAjax(int rows) {
        return rows > 0 ? success() : error("操作失败");
    }
}
