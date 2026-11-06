package com.ruoyi.project.tool.excel.service.impl;

import com.alibaba.excel.EasyExcel;
import com.ruoyi.common.utils.uuid.SnowflakeIdUtils;
import com.ruoyi.project.tool.excel.domain.ExcelVo;
import com.ruoyi.project.tool.excel.service.IExcelProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(ExcelProcessServiceImpl.class);


    @Override
    public byte[] process(InputStream inputStream, String insertFentryIds, String updateFentryIds) throws IOException {
        // 1. 按 ExcelVo 的 @ExcelProperty(index) 读入；默认跳过第 1 行表头
        List<ExcelVo> rows = EasyExcel.read(inputStream)
                .head(ExcelVo.class)
                .autoCloseStream(false)
                .sheet()
                .doReadSync();

        // 2. 业务处理（唯一扩展点）
        List<ExcelVo> result = doBusiness(rows, insertFentryIds, updateFentryIds);

        // 3. 写出到内存流，由 Controller 以附件形式返回；写出时按 ExcelVo 的 value 生成表头
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        EasyExcel.write(outputStream)
                .head(ExcelVo.class)
                .sheet("处理结果")
                .doWrite(result == null ? Collections.<ExcelVo>emptyList() : result);
        return outputStream.toByteArray();
    }

    @Override
    public List<ExcelVo> doBusiness(List<ExcelVo> rows, String insertFentryIds, String updateFentryIds) {
        System.out.println("进入成功");
        // updateFentryIds 本次仅接收预留：updateSql 公式逻辑后续补充，不参与计算
        Set<String> insertIdSet = parseFentryIds(insertFentryIds);
        if (rows == null || rows.isEmpty()) {
            return rows;
        }

        HashMap<String, Integer> integerHashMap = new HashMap<>();
        for (int i = 0; i < rows.size(); i++) {
            ExcelVo row = rows.get(i);
            if (row == null) {
                continue;
            }

            // 原有逻辑：fdetailid 填雪花 ID，fseq 按 fentryid 分组从 1 递增
            row.setFdetailid(String.valueOf(SnowflakeIdUtils.nextId()));
            if (integerHashMap.containsKey(row.getFentryid())) {
                Integer current = integerHashMap.get(row.getFentryid());
                Integer add = current + 1;
                row.setFseq(add);
                integerHashMap.put(row.getFentryid(), add);
            } else {
                integerHashMap.put(row.getFentryid(), 1);
                row.setFseq(1);
            }

            // 新增 fentryid 命中本行时，按输出行号生成 insertSql 公式；未命中置空
            if (insertIdSet.contains(normalizeFentryId(row.getFentryid()))) {
                String insertSql = "INSERT INTO public.tk_usp_packagelist (fentryid, fdetailid, fseq, fk_usp_si_lotno, fk_usp_boxno, fk_usp_packageno, fk_usp_package_weight, fk_usp_packageunit, fk_usp_reno, fk_usp_si_rod_crush_date) VALUES ('" + row.getFentryid() + "','" + row.getFdetailid() + "','" + row.getFseq() + "','" + row.getFk_usp_si_lotno() + "','" + row.getFk_usp_boxno() + "','" + row.getFk_usp_packageno() + "','" + row.getFk_usp_package_weight() + "','" + row.getFk_usp_packageunit() + "','" + row.getFk_usp_reno() + "','" + row.getFk_usp_si_lotno() + "','" + row.getFk_usp_si_rod_crush_date() + "');";
                row.setInsertSql(insertSql);
            } else {
                row.setInsertSql(null);
            }
            //修改语句
            if (updateFentryIds.contains(normalizeFentryId(row.getFentryid()))) {
                String updateSql = "UPDATE public.tk_usp_packagelist SET fk_usp_si_lotno = '" + row.getFk_usp_si_lotno() + "', fk_usp_boxno = '" + row.getFk_usp_boxno() + "' WHERE fentryid = " + row.getFentryid() + " AND fk_usp_packageno = '" + row.getFk_usp_packageno() + "';";
                row.setUpdateSql(updateSql);
            } else {
                row.setUpdateSql(null);
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

    /**
     * 拆分「新增/修改 fentryid」输入：英文逗号、中文逗号、空白、换行均可作为分隔符。
     * 结果去空、去重并做数值归一化。
     */
    private Set<String> parseFentryIds(String raw) {
        Set<String> ids = new HashSet<>();
        if (raw == null || raw.trim().isEmpty()) {
            return ids;
        }
        for (String part : raw.split("[,，\\s]+")) {
            String id = normalizeFentryId(part);
            if (id != null && !id.isEmpty()) {
                ids.add(id);
            }
        }
        return ids;
    }

    /**
     * fentryid 归一化：去首尾空白，并把 Excel 数值单元格可能读出的 "1001.0" 归一为 "1001"。
     */
    private String normalizeFentryId(String fentryId) {
        if (fentryId == null) {
            return null;
        }
        String value = fentryId.trim();
        if (value.matches("-?\\d+\\.0+")) {
            value = value.substring(0, value.indexOf('.'));
        }
        return value;
    }
}
