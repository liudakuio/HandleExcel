package com.ruoyi.project.tool.excel.service;

import com.ruoyi.project.tool.excel.domain.ExcelVo;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Excel 处理服务
 *
 * @author ruoyi
 */
public interface IExcelProcessService {

    /**
     * 读入 Excel → 业务处理 → 写出 xlsx 字节
     *
     * @param inputStream     上传文件输入流，由调用方负责关闭
     * @param insertFentryIds 「新增 fentryid」输入值，多个值以逗号/空白分隔，可为空
     * @param updateFentryIds 「修改 fentryid」输入值，本次仅接收预留，不参与计算
     * @return 处理后 Excel 文件的字节内容
     */
    byte[] process(InputStream inputStream, String insertFentryIds, String updateFentryIds) throws IOException;

    /**
     * 【唯一业务扩展点】使用者在此补充详细业务逻辑，默认原样返回。
     * <p>
     * insertFentryIds 命中行生成 insertSql 公式，未命中行 insertSql 置空；
     * updateFentryIds 本次仅预留，updateSql 逻辑后续补充。
     *
     * @param rows            按 ExcelVo 字段映射读入的行数据
     * @param insertFentryIds 需要生成 insertSql 公式的 fentryid，多个值以逗号/空白分隔，可为空
     * @param updateFentryIds 需要生成 updateSql 公式的 fentryid，本次仅预留
     * @return 需要写出的行数据
     */
    List<ExcelVo> doBusiness(List<ExcelVo> rows, String insertFentryIds, String updateFentryIds);
}
