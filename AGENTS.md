# AI 代理维护指南

EatWise 是个人开源 Android AI 项目。维护目标是保持主链路清晰、可验证、隐私边界明确，不做企业级过度设计。

## 先读这些文件

1. `README.md`
2. `docs/AI_GOVERNANCE.md`
3. `docs/MAINTENANCE.md`
4. `SECURITY.md`
5. `CONTRIBUTING.md`

## 工作原则

- 优先做小而明确的改动，不夹带无关重构。
- 代码清晰直观，不为“以后可能用到”提前抽象。
- 不引入新依赖，除非能明显减少复杂度或维护风险。
- 不回退或覆盖用户已有未提交变更。
- 修改 prompt、JSON schema、Room Entity、隐私边界或外部请求时，同步更新文档和测试。
- 发布版本涉及 README、Release notes、截图说明或其他面向用户的说明文档时，必须同步提供中文和英文两个版本；不要只更新中文或只更新英文。

## AI 输出约束

- 当前 prompt 由 `OpenAiCompatibleClient.promptVersion` 管理。
- 结果结构以当前 schema 为准，不为废弃字段保留兼容分支。
- 不根据图片估算重量、卡路里或宏量营养素。
- 不输出医疗诊断、药物建议或治疗方案。
- 食用建议保持生活化、可执行，优先使用“只能尝一小口、需要严格控量、可以适量吃、可以适量多吃”等明确表达。
- 标签必须短、有决策价值；不要输出“常规食材、常规分量、普通、粗估”等低信息量标签。

## 隐私红线

不得提交、记录或贴到 Issue/PR/文档中：

- 真实 API Key、Authorization header、签名密码、keystore。
- `local.properties` 或任何本机私密配置。
- 用户照片、完整 base64、完整 AI 请求体或完整响应体。
- 未脱敏日志、设备上的完整个人路径、包含个人信息的饮食目标。

可以使用合成示例、脱敏片段和内置测试图片说明问题。

## 验证要求

常规代码改动至少运行：

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat test assembleDebug
```

发布或构建链路改动运行：

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat clean lintDebug test assembleRelease --warning-mode all
```

无法运行时必须说明原因。
