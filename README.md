# 吃得明白 EatWise

吃得明白是一个个人实验性的 Android 饮食拍照分析 App。它不做登录、后端或云同步，只负责图片输入、AI 请求、结果展示和本地历史保存。

## 功能

- 配置 OpenAI-compatible API：Base URL、模型名称、API Key、个人饮食目标
- 从相册选择餐食图片或使用 CameraX 拍照
- 压缩图片后以 base64 data URL 发送给用户配置的模型
- 展示餐食名称、热量、三大营养素、目标匹配、建议、多食材明细和短标签
- 支持多个菜品或复合食材的分析结果展示，食材可按菜品归属分组
- 保存本地历史记录，支持查看详情、收藏和删除
- API Key、模型不支持图片、网络失败、JSON 解析失败等场景有明确提示

## 技术栈

- Kotlin
- Jetpack Compose + Material 3
- Navigation Compose
- DataStore Preferences
- Room
- OkHttp
- kotlinx.serialization
- Coil
- CameraX

## 运行

1. 用 Android Studio 打开项目。
2. 确认本机 Android SDK 可用。
3. 运行：

```powershell
.\gradlew.bat test
.\gradlew.bat assembleDebug
```

更完整的编译、Debug 和日志采集流程见 [docs/MAINTENANCE.md](docs/MAINTENANCE.md)。

如果命令行提示 `JAVA_HOME` 未设置，可以临时使用 Android Studio 自带 JDK：

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug
```

## 配置模型

默认 Base URL：

```text
https://openrouter.ai/api/v1
```

需要在设置页填写：

- API Key
- 支持图片输入的模型名称
- 用户饮食目标

App 会请求：

```text
POST {baseUrl}/chat/completions
```

## 隐私说明

- API Key 只保存在本机 DataStore 中。
- App 不打印 API Key、Authorization header 或完整 base64 图片内容。
- 图片保存到 App 私有目录。
- 除用户配置的大模型 API 外，App 不上传数据到其他服务。

## 工程治理

- AI prompt 集中在 `OpenAiCompatibleClient`，当前 `promptVersion = 2`。
- 结果 JSON 保持向后兼容，新增字段优先设默认值。
- 提交代码前运行 `.\gradlew.bat test assembleDebug`。
- 编译、Debug、日志采集和 AI 接手维护流程见 [docs/MAINTENANCE.md](docs/MAINTENANCE.md)。

## 常见错误

- “请先在设置中配置 API Key”：设置页未填写 Key。
- “请先在设置中填写模型名称”：设置页未填写模型。
- “当前模型可能不支持图片分析”：请更换支持视觉输入的模型。
- “AI 返回格式异常”：模型没有稳定返回 JSON，可更换模型或重试。
- “网络请求失败”：检查网络、Base URL 或 API 配置。
