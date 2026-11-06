package com.ruoyi.project.tool.excel.controller;

import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.project.tool.excel.service.IExcelProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Excel 处理接口：一次请求完成「上传 → 处理 → 下载结果文件」。
 * 成功返回 xlsx 文件流，失败返回 AjaxResult JSON。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/tool/excel")
public class ExcelProcessController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ExcelProcessController.class);

    private static final String XLSX_MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String RESULT_FILE_NAME = "处理结果.xlsx";

    private final IExcelProcessService excelProcessService;

    public ExcelProcessController(IExcelProcessService excelProcessService) {
        this.excelProcessService = excelProcessService;
    }

    @PostMapping("/process")
    public ResponseEntity<?> process(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "insertFentryIds", required = false, defaultValue = "") String insertFentryIds,
            @RequestParam(value = "updateFentryIds", required = false, defaultValue = "") String updateFentryIds) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(error("请选择要上传的 Excel 文件"));
        }
        if (!isExcel(file)) {
            return ResponseEntity.badRequest().body(error("仅支持 .xlsx / .xls 格式的文件"));
        }

        try (InputStream inputStream = file.getInputStream()) {
            byte[] data = excelProcessService.process(inputStream, insertFentryIds, updateFentryIds);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(XLSX_MEDIA_TYPE));
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(RESULT_FILE_NAME, StandardCharsets.UTF_8)
                    .build());
            headers.set(HttpHeaders.CACHE_CONTROL, "no-store");
            headers.setContentLength(data.length);
            return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Excel 处理失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error("Excel 处理失败，请检查文件内容是否正确"));
        }
    }

    private boolean isExcel(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return false;
        }
        String lowerName = fileName.toLowerCase(Locale.ROOT);
        return lowerName.endsWith(".xlsx") || lowerName.endsWith(".xls");
    }
}
