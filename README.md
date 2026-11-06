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
| 请求 | `multipart/form-data`，字段 `file`（`.xlsx` / `.xls`，上限 20MB）、`insertFentryIds`（新增 fentryid，可空）、`updateFentryIds`（修改 fentryid，可空） |
| 成功 | `200` + `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` 附件流，文件名 `处理结果.xlsx` |
| 失败 | `400` / `500` + `AjaxResult` JSON：`{"code":400,"msg":"错误原因"}` |

## 新增 / 修改 fentryid 规则

- 两个输入框均支持多个 fentryid：英文逗号、中文逗号、空格、换行都可作为分隔符，后端拆分去重后与第 1 列 fentryid 精确匹配。
- 命中「新增 fentryid」的行：`insertSql` 列（第 11 列）写入 INSERT 公式，公式中的 `A2`~`J2` 等单元格引用按该行在结果文件中的实际行号生成（第 1 条数据为第 2 行），Excel 打开即自动算出完整 SQL。
- 未命中行 `insertSql` 置空；新增输入框为空时所有行都不生成公式。
- 「修改 fentryid」本次仅随请求提交并预留参数，`updateSql` 公式逻辑尚未实现，该列不会被改写。
- 导出的 `insertSql` 是公式单元格；若需要纯 SQL 文本，可复制该列后「选择性粘贴 → 值」。

## 补充业务逻辑的位置

数据模型：`src/main/java/com/ruoyi/project/tool/excel/domain/ExcelVo.java`，字段 `column1`~`column10`
与 `insertSql`、`updateSql`，通过 `@ExcelProperty(value = "表头名", index = N)` 依次对应 Excel 第 1~12 列。
按实际业务改字段名、改表头 `value`、追加字段并同步 `index` 即可（列数不足时多出的列会被忽略）。

业务逻辑集中在
`src/main/java/com/ruoyi/project/tool/excel/service/impl/ExcelProcessServiceImpl.java` 的
`doBusiness(List<ExcelVo> rows, String insertFentryIds, String updateFentryIds)` 方法：

- `rows.get(i).getColumn1()` 读取第 i 行第 1 列，用 `setColumn1(...)` 写回；空单元格通常为 `null`，按需判空
- `process` 写出时注册 `InsertSqlFormulaWriteHandler`，把 `insertSql` 列的公式文本转成真正的公式单元格
- 读入/写出、响应头、异常处理都已封装好，补充逻辑时无需改动 IO 代码
- 读入默认跳过第 1 行表头、写出会带表头；若上传文件无表头，在 `process` 的读入链上加
  `.headRowNumber(0)`，并在写出链上加 `.needHead(false)`

## 已知限制

- 采用 `head(ExcelVo.class)` + `doReadSync()` 全量读入 + 内存写出，示例级数据量（20MB 以内）适用；
  大文件建议改为 `AnalysisEventListener` 分批读 + `ExcelWriter` 流式写出。
- `ExcelVo` 固定映射 12 列（含 `insertSql` / `updateSql`），列数由注解决定，不再是任意列通用结构。
- 「修改 fentryid」对应的 `updateSql` 公式尚未实现，当前仅接收参数且不改写该列数据。
- 成功返回文件流，因此不套 `AjaxResult` 包装；错误分支仍使用 `AjaxResult`，前端按 `response.ok` 分流。
- 未包含登录鉴权、数据库、全局异常处理、单元测试等工程能力。
