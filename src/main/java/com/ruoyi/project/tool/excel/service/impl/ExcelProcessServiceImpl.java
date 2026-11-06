package com.ruoyi.project.tool.excel.service.impl;

import com.alibaba.excel.EasyExcel;
import com.ruoyi.common.utils.uuid.SnowflakeIdUtils;
import com.ruoyi.project.tool.excel.domain.ExcelVo;
import com.ruoyi.project.tool.excel.service.IExcelProcessService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Excel 处理服务实现：只负责读写与调用业务扩展点，不含任何业务逻辑。
 *
 * @author ruoyi
 */
@Service
public class ExcelProcessServiceImpl implements IExcelProcessService {

    @Override
    public byte[] process(InputStream inputStream) throws IOException {
        // 1. 按 ExcelVo 的 @ExcelProperty(index) 读入；默认跳过第 1 行表头
        List<ExcelVo> rows = EasyExcel.read(inputStream)
                .head(ExcelVo.class)
                .autoCloseStream(false)
                .sheet()
                .doReadSync();

        // 2. 业务处理（唯一扩展点）
        List<ExcelVo> result = doBusiness(rows);

        // 3. 写出到内存流，由 Controller 以附件形式返回；写出时按 ExcelVo 的 value 生成表头
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        EasyExcel.write(outputStream)
                .head(ExcelVo.class)
                .sheet("处理结果")
                .doWrite(result == null ? Collections.<ExcelVo>emptyList() : result);
        return outputStream.toByteArray();
    }

    @Override
    public List<ExcelVo> doBusiness(List<ExcelVo> rows) {
        System.out.println("进入成功");
        // TODO 业务处理逻辑由使用者补充：例如 getColumn1() 读取、setColumn1() 写回、
        // 增删行、校验数据、调用其他服务等。
        // 当前默认原样返回，保证「上传 → 处理 → 下载」链路可直接跑通。
        // 提示：rows 为可变列表，可就地修改后返回，也可构造新的 List<ExcelVo> 返回。
        HashMap<String, Integer> integerHashMap = new HashMap<>();
        for (ExcelVo row : rows) {
            row.setColumn2(String.valueOf(SnowflakeIdUtils.nextId()));
            if (integerHashMap.containsKey(row.getColumn1())) {
                Integer i = integerHashMap.get(row.getColumn1());
                Integer add = i + 1;
                row.setColumn3(add);
                integerHashMap.put(row.getColumn1(),add);
            } else {
                integerHashMap.put(row.getColumn1(), 1);
                row.setColumn3(1);
            }
        }
        System.out.println("=====打印去重后的entrtyid======");
        Set<Map.Entry<String, Integer>> entries = integerHashMap.entrySet();
        for (Map.Entry<String, Integer> entry : entries) {
            String key = entry.getKey();
            System.out.println(key);
        }
        return rows;
    }
}
