# 维护说明

## API Key 管理

- Key 只保存在本地设置。
- 不提交 Key。
- 不打印 Key、Authorization header 或完整请求体。
- 如怀疑泄露，立即更换 Key。

## Release 签名与发布

- 正式包使用本机 release keystore 签名，keystore、密码和构建产物都不能提交。
- 签名配置从 `local.properties`、Gradle 属性或环境变量读取，字段名保持一致：
  - `EATWISE_RELEASE_STORE_FILE`
  - `EATWISE_RELEASE_STORE_PASSWORD`
  - `EATWISE_RELEASE_KEY_ALIAS`
  - `EATWISE_RELEASE_KEY_PASSWORD`
- `EATWISE_RELEASE_STORE_FILE` 建议写相对项目根目录的路径，例如 `eatwise-release.keystore`。
- 发布前运行 `.\gradlew.bat lintDebug test assembleRelease`。
- 生成 APK 后使用 `apksigner verify --verbose --print-certs` 验证签名，并为上传的 APK 生成 SHA-256 校验文件。
- 只在 GitHub Release 上传已签名 APK 和校验文件，不上传 keystore、`local.properties` 或任何包含密码的文件。
- release keystore 丢失后无法再用同一签名升级旧安装包，务必离线备份。

## 模型维护

- 模型名称由用户在设置页填写。
- 模型必须支持图片输入。
- 当前实现不依赖 `response_format`，使用 prompt 约束模型只返回 JSON；分析请求使用流式输出提升等待体验，最终仍按完整 JSON 解析。
- 如果模型返回不稳定，优先调整 prompt、降低 temperature 或更换模型，不要先引入复杂兼容层。
- AI 约束治理细则见 `docs/AI_GOVERNANCE.md`，这里仅保留维护入口和版本信息。

## Prompt 维护

- 系统提示词集中在 `OpenAiCompatibleClient`。
- 当前 `promptVersion = 11`。
- `promptVersion = 11` 要求模型识别多个菜品和复合食材，并按当前 App 语言输出所有用户可见字段。
- 当前支持语言：简体中文、繁体中文、英语、日语。
- 建议必须是可执行小动作，避免只输出“注意控制、保持均衡、控制频率”等抽象提醒。
- 食材条目中尽量填写可选 `dish` 字段。
- 不要求模型返回食材重量、卡路里或宏量营养素数值；`Ingredient` 不保留分量和热量字段。
- `eating_advice` 是核心建议字段，只允许“只能尝一小口、需要严格控量、可以适量吃、可以适量多吃”四类。
- 标签必须短、生活化、有明确决策价值；不要输出“常规食材、常规分量、普通、粗估”等低信息量标签。
- 新增或修改标签时同步维护 `MealLanguageText` 中四种语言的标签映射、风险色彩和 prompt 示例。
- 修改按菜建议的本地兜底提示时，同步覆盖中文、英文和日文常见食材关键词，避免非中文结果退回普通提示。
- 标签颜色语义为：绿色代表推荐或正向，黄色代表需要注意，红色代表明显风险或不推荐。
- 建议必须明确、可执行，优先给普通用户能做到的小调整。
- 解析后的 `MealAnalysisPolisher` 会对标签和建议做轻量清洗，但不要依赖它修复完全不合理的 prompt 输出。
- 修改 prompt 后，同步更新模型、测试和文档，避免保留废弃字段。

## 饮食目标预设维护

- 设置页的目标预设只是填写 `userGoal` 的快捷文本，不新增数据库字段。
- 用户始终可以手动编辑目标文本，预设不能替代个性化目标。
- 预设文案必须保持生活化和非诊断语气，例如“少油控脂”“少糖稳糖”，不要写成医疗建议或治疗方案。
- 新增预设时优先覆盖常见饮食管理场景，避免为少数特殊疾病引入复杂规则。
- 修改预设不会影响历史记录；历史详情以 `userGoalSnapshot` 展示当时保存的目标。

## JSON 结构维护

- 当前结果结构以 `promptVersion = 11` 管理，Room 数据库是 version 2。
- `Ingredient.dish` 用于多菜品分组，`Ingredient` 不保留重量、分量或热量字段。
- 新增 JSON 字段前先确认 UI 或业务确实需要，不为“可能用到”提前扩展 schema。
- 删除字段时同步更新 prompt、模型、测试和手工验收清单。
- `aiResultJson` 保存当前原始 JSON，方便复查模型输出。

## 工程治理原则

- 这是个人项目，优先保持主链路清晰，不为少量兼容问题引入大型框架。
- UI、ViewModel、Repository、UseCase、core 工具的简单分层已经足够，新增抽象前必须能减少真实重复或复杂度。
- 修改 AI 输出结构时直接维护当前 schema；数据库 Entity 字段变化时升级 Room version。当前个人项目允许破坏式重建本地记录库，避免为废弃字段保留兼容分支。
- 不提交本地调试产物、日志、截图、真实 Key 或用户图片。
- 每次代码提交前至少运行 `.\gradlew.bat test assembleDebug`；只改文档时可说明未运行构建。
- 发布正式包前运行 `.\gradlew.bat lintDebug test assembleRelease` 并验证 APK 签名。
- 修复 UI 时必须用模拟器或真机截图复核，重点看文字是否截断、标签是否换行、按钮是否被系统导航栏遮挡。
- 等待页改动必须复核空输出、分析中和后台分析三种状态，避免进度提示占用过多首屏空间。
- 对外协作入口以根目录文档为准：贡献流程见 `CONTRIBUTING.md`，安全策略见 `SECURITY.md`，变更记录见 `CHANGELOG.md`，许可证见 `LICENSE`。

## 数据库维护

- 当前 Room version = 2。
- 个人项目当前阶段允许 destructive migration，避免为旧分析字段保留兼容分支。
- 后续如果开始长期保留真实历史记录，再按实际需要补充 Migration。

## 图片维护

- 图片存放在 App 私有目录。
- 删除记录时后续可同步删除对应图片文件。
- 可增加孤立图片清理逻辑。
- 不要把图片复制到公共相册，除非用户明确要求。

## 日志维护

- 不记录 API Key。
- 不记录完整 base64。
- 不记录完整 Authorization header。
- 网络错误只记录状态码和简短错误。

## 编译流程

本项目以本地可重复构建为准。AI 或人工修改代码后，至少运行以下命令：

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat test
.\gradlew.bat assembleDebug
```

如果只改了文档，可以不跑构建，但最终交付前仍建议跑一次 `test assembleDebug`。

常用命令：

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat clean
.\gradlew.bat test
.\gradlew.bat assembleDebug
.\gradlew.bat assembleRelease
.\gradlew.bat installDebug
```

编译失败时的处理顺序：

1. 先看第一个 Kotlin/Gradle 编译错误，不要被后续级联错误干扰。
2. 如果是依赖或 Gradle 版本问题，优先保持当前轻量技术栈，不为单个问题引入大型框架。
3. 如果是 Android SDK/JDK 环境问题，先确认 `JAVA_HOME`、`sdk.dir` 和 Android Studio SDK 配置。
4. 修复后重新运行 `.\gradlew.bat test assembleDebug`。

当前命令行环境如果没有全局 Java，可使用 Android Studio 自带 JDK：

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
```

Debug APK 输出位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

Release APK 输出位置：

```text
app/build/outputs/apk/release/app-release.apk
```

## Debug 调试流程

优先使用 Android Studio 调试：

1. 用 Android Studio 打开项目根目录。
2. 选择 `app` 配置。
3. 连接真机或启动模拟器。
4. 点击 Run 验证主链路，点击 Debug 进入断点调试。

重点调试路径：

- 设置保存：`SettingsStore`、`SettingsRepository`、`SettingsViewModel`
- 图片导入：`ImageStorage`、`ImageCompressor`、`HomeViewModel`
- AI 请求：`AnalyzeMealUseCase`、`OpenAiCompatibleClient`
- JSON 解析：`JsonUtils`、`MealAnalysisResult`
- 历史记录：`MealRepository`、`MealRecordDao`
- 拍照：`CameraScreen`

排查顺序：

1. 先确认 UI 是否拿到正确状态。
2. 再确认 ViewModel 是否调用了正确 use case 或 repository。
3. 再确认本地存储、网络请求或数据库层是否返回异常。
4. 最后再改 UI，避免用界面逻辑掩盖数据层问题。

AI 相关问题优先看三类信息：

- 设置是否完整：`baseUrl`、`modelName`、`apiKey`
- HTTP 状态码：401 多半是 Key 问题，400 多半是模型或请求格式不兼容
- 返回内容是否为可解析 JSON

## 日志采集流程

本项目默认不要求引入复杂日志框架。排查问题时优先使用 Android Studio Logcat 或 adb：

```powershell
adb logcat
```

如需导出日志：

```powershell
adb logcat -d > logs\logcat.txt
```

导出前先创建目录：

```powershell
New-Item -ItemType Directory -Force logs
```

日志采集边界：

- 可以记录页面名、状态变化、HTTP 状态码、错误类型、图片文件大小、压缩前后尺寸。
- 不可以记录 API Key、Authorization header、完整请求体、完整响应体、完整 base64 图片。
- AI 返回 JSON 如需排查，只能短暂在本地手动查看，不能提交到仓库，不能长期保存在日志文件中。
- 提交问题或交给 AI 分析前，必须先删除或打码任何 Key、用户目标中的隐私内容、图片路径中的个人信息。

建议的临时日志格式：

```text
AnalyzeMeal status=400 modelConfigured=true imageBytes=245120 message="模型可能不支持图片"
```

不允许的日志格式：

```text
Authorization: Bearer <redacted>
data:image/jpeg;base64,/9j/4AAQ...
完整 AI 请求体或完整响应体
```

`logs/` 目录只用于本地临时排查，日志文件不应提交。

## AI 接手维护流程

后续让 AI 修改项目时，建议按以下顺序给任务：

1. 说明要解决的具体问题和验收标准。
2. 要求 AI 先阅读相关文件，不要全局重构。
3. 要求 AI 修改后运行 `.\gradlew.bat test assembleDebug`。
4. 要求 AI 汇报修改文件、验证命令、失败原因和未完成事项。

AI 修改代码时必须遵守：

- 先阅读 `AGENTS.md` 和 `docs/AI_GOVERNANCE.md`。
- 不读取项目外敏感文件。
- 不写入真实 API Key。
- 不把用户图片、base64、完整 AI 请求体写入日志或文档。
- 不引入不必要依赖。
- 优先保持当前简单分层：UI、ViewModel、Repository、UseCase、core 工具。

## 依赖维护

- 定期升级 Gradle、AGP、Kotlin、Compose、Room、CameraX。
- 升级后运行 `assembleDebug` 和基础测试。
- 保持依赖数量少。
- AGP 9 已内置 Android Kotlin 支持，不要重新添加 `org.jetbrains.kotlin.android` 插件。

## 安全边界

- App 只做饮食记录参考。
- 不做医疗诊断。
- 不给药物建议。
- 不替代医生或营养师。
- UI 中保留免责声明。
- 不根据图片估算重量、卡路里或宏量营养素，避免给用户过度精确的错觉。
- `android:allowBackup` 必须保持为 `false`，备份规则文件也应显式排除所有本地数据，避免 API Key、图片和分析记录被系统备份。

## 后续可扩展方向

- 一周饮食总结
- 根据今日历史推荐下一餐
- 重新根据新目标评价历史记录
- 导出 JSON/CSV
- 搜索历史
- 按标签或适合程度筛选历史
- 更多模型 Provider 预设
