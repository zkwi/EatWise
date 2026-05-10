# 贡献指南

EatWise 是个人项目，治理目标是保持主链路清晰、可验证、易维护。提交改动前请优先选择小而明确的变更，避免为了“以后可能用到”提前抽象。

## 开发流程

1. 先阅读 [README.md](README.md) 和 [docs/MAINTENANCE.md](docs/MAINTENANCE.md)。
2. 明确本次改动的用户价值和验收标准。
3. 保持改动范围集中；UI、ViewModel、UseCase、Repository、core 工具的现有分层已经足够。
4. 修改 AI 输出结构时保持向后兼容，新增字段需要默认值。
5. 提交前运行：

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat test assembleDebug
```

## 代码要求

- Kotlin 代码优先清晰直观，不新增不必要的抽象层。
- 中文注释只解释原因，不重复代码正在做什么。
- 不提交真实 API Key、用户图片、完整 AI 请求体、完整响应体或本地日志。
- 不引入新依赖，除非能明显减少复杂度或维护风险。
- 数据库 Entity 变化必须升级 Room version 并补 Migration。

## 提交信息

提交信息使用简短祈使句，例如：

```text
Improve analysis result polish
Add project governance docs
```

## PR 检查清单

- [ ] 改动范围和需求一致，没有夹带无关重构。
- [ ] `.\gradlew.bat test assembleDebug` 已通过，或明确说明无法运行的原因。
- [ ] 涉及 UI 的改动已在真机或模拟器上检查主要页面。
- [ ] 涉及 prompt、JSON、Room 或隐私边界的改动已同步文档。
- [ ] 没有提交本地配置、日志、截图、密钥或用户数据。
