package com.ruoyi.project.tool.excel.domain;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * Excel 行数据对象：按 @ExcelProperty 的 index 与上传文件的列一一对应。
 * 【按需扩展】示例提供 5 列，请把 column1~column5 改成真实业务字段；
 * 列数不足时追加字段并同步 index，value 为导出时的表头名称。
 *
 * @author ruoyi
 */
@Data
public class ExcelVo {

    /** 第 1 列 */
    @ExcelProperty(value = "fentryid", index = 0)
    private String fentryid;

    /** 第 2 列 */
    @ExcelProperty(value = "fdetailid", index = 1)
    private String fdetailid;

    /** 第 3 列 */
    @ExcelProperty(value = "fseq", index = 2)
    private Integer fseq;

    /** 第 4 列 */
    @ExcelProperty(value = "fk_usp_si_lotno", index = 3)
    private String fk_usp_si_lotno;

    /** 第 5 列 */
    @ExcelProperty(value = "fk_usp_boxno", index = 4)
    private String fk_usp_boxno;

    /** 第 6 列 */
    @ExcelProperty(value = "fk_usp_packageno", index = 5)
    private String fk_usp_packageno;

    /** 第 7 列 */
    @ExcelProperty(value = "fk_usp_package_weight", index = 6)
    private String fk_usp_package_weight;

    /** 第 8 列 */
    @ExcelProperty(value = "fk_usp_packageunit", index = 7)
    private String fk_usp_packageunit;

    /** 第 9 列 */
    @ExcelProperty(value = "fk_usp_reno", index = 8)
    private String fk_usp_reno;

    /** 第 10 列 */
    @ExcelProperty(value = "fk_usp_si_rod_crush_date", index = 9)
    private String fk_usp_si_rod_crush_date;

    /** 第 11 列 */
    @ExcelProperty(value = "insertSql", index = 10)
    private String insertSql;

    /** 第 12 列 */
    @ExcelProperty(value = "updateSql", index = 11)
    private String updateSql;
}
