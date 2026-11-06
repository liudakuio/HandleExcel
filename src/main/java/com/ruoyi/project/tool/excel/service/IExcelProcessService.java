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
     * @param inputStream 上传文件输入流，由调用方负责关闭
     * @return 处理后 Excel 文件的字节内容
     */
    byte[] process(InputStream inputStream) throws IOException;

    /**
     * 【唯一业务扩展点】使用者在此补充详细业务逻辑，默认原样返回。
     *
     * @param rows 按 ExcelVo 字段映射读入的行数据
     * @return 需要写出的行数据
     */
    List<ExcelVo> doBusiness(List<ExcelVo> rows);
}
