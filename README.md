# 若依风格 Excel 处理示例（最轻量）

单页 HTML 上传 Excel → 后端 EasyExcel 读入 → 预留业务处理 TODO → 写出 xlsx 文件流 → 浏览器自动下载。
除了这条主链路，其余业务逻辑由使用者补充。

## 环境

- JDK 8、Maven 3.9
- Spring Boot 2.5.15、EasyExcel 3.3.4
- 依赖仅 `spring-boot-starter-web` + `easyexcel` + `lombok`（provided，编译期生效），无数据库、无安全框架
- `ExcelVo` 用 `@Data` 由 Lombok 生成 getter/setter；IDE 需安装 Lombok 插件并开启注解处理

## 运行

```bash
mvn "-Dmaven.resolver.transport=wagon" spring-boot:run
```

> JDK 8 + Maven 3.9 环境下，默认 resolver transport 下载 jar 阶段可能报 `Could not transfer artifact`，
> 必须加 `-Dmaven.resolver.transport=wagon`（PowerShell 下该参数必须带引号，否则会被拆成两个参数）；
> 打包同理：`mvn "-Dmaven.resolver.transport=wagon" package`。

启动后访问 <http://localhost:8080/index.html>。

## 接口

| 项 | 说明 |
| --- | --- |
| 地址 | `POST /tool/excel/process` |
| 请求 | `multipart/form-data`，字段名 `file`，支持 `.xlsx` / `.xls`，上限 20MB |
| 成功 | `200` + `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` 附件流，文件名 `处理结果.xlsx` |
| 失败 | `400` / `500` + `AjaxResult` JSON：`{"code":400,"msg":"错误原因"}` |

## 补充业务逻辑的位置

数据模型：`src/main/java/com/ruoyi/project/tool/excel/domain/ExcelVo.java`，字段 `column1`~`column5`
通过 `@ExcelProperty(value = "列N", index = N-1)` 对应 Excel 第 1~5 列。按实际业务改字段名、
改表头 `value`、追加字段并同步 `index` 即可（超过 5 列必须补字段，否则多出的列会被忽略）。

唯一业务扩展点在
`src/main/java/com/ruoyi/project/tool/excel/service/impl/ExcelProcessServiceImpl.java` 的
`doBusiness(List<ExcelVo> rows)` 方法，方法内已标 `// TODO`。

- `rows.get(i).getColumn1()` 读取第 i 行第 1 列，用 `setColumn1(...)` 写回；空单元格通常为 `null`，按需判空
- 默认实现原样返回，保证示例链路可直接跑通；可就地修改后返回，也可构造新的 `List<ExcelVo>` 返回
- 读入/写出、响应头、异常处理都已封装好，补充逻辑时无需改动 IO 代码
- 读入默认跳过第 1 行表头、写出会带表头；若上传文件无表头，在 `process` 的读入链上加
  `.headRowNumber(0)`，并在写出链上加 `.needHead(false)`

## 已知限制

- 采用 `head(ExcelVo.class)` + `doReadSync()` 全量读入 + 内存写出，示例级数据量（20MB 以内）适用；
  大文件建议改为 `AnalysisEventListener` 分批读 + `ExcelWriter` 流式写出。
- `ExcelVo` 示例只映射 5 列，列数由注解决定，不再是任意列通用结构。
- 成功返回文件流，因此不套 `AjaxResult` 包装；错误分支仍使用 `AjaxResult`，前端按 `response.ok` 分流。
- 未包含登录鉴权、数据库、全局异常处理、单元测试等工程能力。
