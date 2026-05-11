package com.example.eatwise.core.i18n

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val promptName: String,
) {
    ZhHans("zh-Hans", "简体中文", "Simplified Chinese"),
    ZhHant("zh-Hant", "繁體中文", "Traditional Chinese"),
    En("en", "English", "English"),
    Ja("ja", "日本語", "Japanese");

    companion object {
        val default = ZhHans

        fun fromCode(code: String?): AppLanguage =
            values().firstOrNull { it.code == code } ?: default
    }
}

object MealLanguageText {
    fun defaultUserGoal(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "我想保持饮食均衡，尽量吃得清淡、丰富一点。"
        AppLanguage.ZhHant -> "我想保持飲食均衡，盡量吃得清淡、豐富一點。"
        AppLanguage.En -> "I want to keep my meals balanced, lighter, and more varied."
        AppLanguage.Ja -> "食事のバランスを保ち、できるだけ軽めで品目を増やしたい。"
    }

    fun disclaimer(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "以上仅基于图片做饮食参考，不替代专业建议。"
        AppLanguage.ZhHant -> "以上僅基於圖片作飲食參考，不替代專業建議。"
        AppLanguage.En -> "This is image-based dietary guidance only and does not replace professional advice."
        AppLanguage.Ja -> "これは画像に基づく食事の参考情報であり、専門的な助言の代わりではありません。"
    }

    fun languageInstruction(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "请使用简体中文输出所有用户可见的 JSON 字符串，包括餐食名称、摘要、建议、标签和免责声明。"
        AppLanguage.ZhHant -> "請使用繁體中文輸出所有使用者可見的 JSON 字串，包括餐食名稱、摘要、建議、標籤和免責聲明。"
        AppLanguage.En -> "Write every user-visible JSON string in English, including meal name, summary, advice, tags, and disclaimer."
        AppLanguage.Ja -> "食事名、要約、提案、タグ、免責文を含む、ユーザーに見える JSON 文字列はすべて日本語で書いてください。"
    }

    fun eatingAdviceOptions(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "只能尝一小口、需要严格控量、可以适量吃、可以适量多吃"
        AppLanguage.ZhHant -> "只能嚐一小口、需要嚴格控量、可以適量吃、可以適量多吃"
        AppLanguage.En -> "Taste only a bite, Needs portion control, Moderate amount is fine, You can eat a bit more"
        AppLanguage.Ja -> "一口だけ味見、量をしっかり控える、適量ならよい、少し多めでもよい"
    }

    fun suggestionExamples(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "先吃蔬菜蛋白、主食少几口、酱料少放、汤汁少喝、甜饮别叠加、下餐清淡一点"
        AppLanguage.ZhHant -> "先吃蔬菜蛋白、主食少幾口、醬料少放、湯汁少喝、甜飲別疊加、下餐清淡一點"
        AppLanguage.En -> "Eat vegetables/protein first, reduce staple bites, use less sauce, drink less soup, skip sugary drinks, keep next meal lighter"
        AppLanguage.Ja -> "野菜とたんぱく質を先に食べる、主食を少し減らす、ソースを少なめにする、汁を控える、甘い飲み物を足さない、次の食事を軽めにする"
    }

    fun tagExamples(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "油脂高、油炸、重口味、糖偏高、钠偏高、蔬菜少、蛋白足、有蔬菜、轻负担、少油控脂、控量"
        AppLanguage.ZhHant -> "油脂高、油炸、重口味、糖偏高、鈉偏高、蔬菜少、蛋白足、有蔬菜、負擔輕、少油控脂、控量"
        AppLanguage.En -> "High oil, Fried, Heavy seasoning, High sugar, High sodium, Low vegetables, Enough protein, Has vegetables, Light burden, Lower-oil choice, Portion control"
        AppLanguage.Ja -> "油多め、揚げ物、味濃いめ、糖質高め、塩分高め、野菜少なめ、たんぱく質十分、野菜あり、軽め、油控えめ、量控えめ"
    }

    fun lowValueTagExamples(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "常规食材、常规分量、普通、一般、粗估、蛋白来源、油脂调味"
        AppLanguage.ZhHant -> "常規食材、常規份量、普通、一般、粗估、蛋白來源、油脂調味"
        AppLanguage.En -> "Regular ingredient, normal portion, ordinary, rough estimate, protein source, oil seasoning"
        AppLanguage.Ja -> "一般的な食材、普通量、ふつう、概算、たんぱく源、油の調味"
    }

    fun promptPreview(userGoal: String, language: AppLanguage): String {
        val goal = userGoal.trim().replace(Regex("\\s+"), " ").ifBlank { defaultUserGoal(language) }.take(46)
        return when (language) {
            AppLanguage.ZhHans -> "这张餐食 + 目标「$goal」：识别主要菜品，判断是否适合，并给出短标签和可执行建议。"
            AppLanguage.ZhHant -> "這張餐食 + 目標「$goal」：識別主要菜品，判斷是否適合，並給出短標籤和可執行建議。"
            AppLanguage.En -> "Meal + goal \"$goal\": identify dishes, judge fit, and return short tags with actionable tips."
            AppLanguage.Ja -> "この食事 + 目標「$goal」：主な料理を識別し、合うかを判断して短いタグと実行しやすい提案を返す。"
        }
    }

    fun analysisStageTitle(index: Int, language: AppLanguage): String = when (index) {
        0 -> when (language) {
            AppLanguage.ZhHans -> "准备分析"
            AppLanguage.ZhHant -> "準備分析"
            AppLanguage.En -> "Preparing"
            AppLanguage.Ja -> "準備中"
        }
        1 -> when (language) {
            AppLanguage.ZhHans -> "处理餐食图片"
            AppLanguage.ZhHant -> "處理餐食圖片"
            AppLanguage.En -> "Processing image"
            AppLanguage.Ja -> "画像を処理中"
        }
        2 -> when (language) {
            AppLanguage.ZhHans -> "正在分析"
            AppLanguage.ZhHant -> "正在分析"
            AppLanguage.En -> "Analyzing"
            AppLanguage.Ja -> "分析中"
        }
        else -> when (language) {
            AppLanguage.ZhHans -> "整理结果"
            AppLanguage.ZhHant -> "整理結果"
            AppLanguage.En -> "Preparing result"
            AppLanguage.Ja -> "結果を整理中"
        }
    }

    fun analysisStageDetail(index: Int, language: AppLanguage): String = when (index) {
        0 -> when (language) {
            AppLanguage.ZhHans -> "第 1/4 步：检查 API Key、模型和饮食目标"
            AppLanguage.ZhHant -> "第 1/4 步：檢查 API Key、模型和飲食目標"
            AppLanguage.En -> "Step 1/4: Checking API key, model, and meal goal"
            AppLanguage.Ja -> "1/4：API Key、モデル、食事目標を確認中"
        }
        1 -> when (language) {
            AppLanguage.ZhHans -> "第 2/4 步：压缩图片并保持清晰度"
            AppLanguage.ZhHant -> "第 2/4 步：壓縮圖片並保持清晰度"
            AppLanguage.En -> "Step 2/4: Compressing the photo while keeping it clear"
            AppLanguage.Ja -> "2/4：見やすさを保ちながら画像を圧縮中"
        }
        2 -> when (language) {
            AppLanguage.ZhHans -> "第 3/4 步：识别菜品并整理建议，这一步通常最久"
            AppLanguage.ZhHant -> "第 3/4 步：識別菜品並整理建議，這一步通常最久"
            AppLanguage.En -> "Step 3/4: Identifying dishes and writing tips; this usually takes longest"
            AppLanguage.Ja -> "3/4：料理を識別して提案を整理中。ここが一番長くかかります"
        }
        else -> when (language) {
            AppLanguage.ZhHans -> "第 4/4 步：整理标签、建议和展示内容"
            AppLanguage.ZhHant -> "第 4/4 步：整理標籤、建議和展示內容"
            AppLanguage.En -> "Step 4/4: Cleaning up tags, tips, and display text"
            AppLanguage.Ja -> "4/4：タグ、提案、表示内容を整理中"
        }
    }

    fun analysisStepText(index: Int, language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "第 ${index + 1}/4 步 · ${analysisStageTitle(index, language)}"
        AppLanguage.ZhHant -> "第 ${index + 1}/4 步 · ${analysisStageTitle(index, language)}"
        AppLanguage.En -> "Step ${index + 1}/4 · ${analysisStageTitle(index, language)}"
        AppLanguage.Ja -> "${index + 1}/4 · ${analysisStageTitle(index, language)}"
    }

    fun missingApiKey(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "请先在设置中填写 API Key。"
        AppLanguage.ZhHant -> "請先在設定中填寫 API Key。"
        AppLanguage.En -> "Please enter an API key in Settings first."
        AppLanguage.Ja -> "先に設定で API Key を入力してください。"
    }

    fun missingModel(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "请先在设置中填写模型名称。"
        AppLanguage.ZhHant -> "請先在設定中填寫模型名稱。"
        AppLanguage.En -> "Please enter a model name in Settings first."
        AppLanguage.Ja -> "先に設定でモデル名を入力してください。"
    }

    fun imageFailed(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "图片处理失败，请重试。"
        AppLanguage.ZhHant -> "圖片處理失敗，請重試。"
        AppLanguage.En -> "Image processing failed. Please try again."
        AppLanguage.Ja -> "画像処理に失敗しました。もう一度お試しください。"
    }

    fun requestFailed(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "请求失败，请检查网络、Base URL 或模型设置。"
        AppLanguage.ZhHant -> "請求失敗，請檢查網路、Base URL 或模型設定。"
        AppLanguage.En -> "Request failed. Check the network, Base URL, or model settings."
        AppLanguage.Ja -> "リクエストに失敗しました。ネットワーク、Base URL、モデル設定を確認してください。"
    }

    fun parseFailed(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "结果格式异常，请重新分析。"
        AppLanguage.ZhHant -> "結果格式異常，請重新分析。"
        AppLanguage.En -> "The result format was invalid. Please analyze again."
        AppLanguage.Ja -> "結果の形式が正しくありません。もう一度分析してください。"
    }

    fun savingRecord(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "正在保存到饮食记录..."
        AppLanguage.ZhHant -> "正在儲存到飲食記錄..."
        AppLanguage.En -> "Saving to meal history..."
        AppLanguage.Ja -> "食事記録に保存中..."
    }

    fun savedRecord(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "已保存到饮食记录"
        AppLanguage.ZhHant -> "已儲存到飲食記錄"
        AppLanguage.En -> "Saved to meal history"
        AppLanguage.Ja -> "食事記録に保存しました"
    }

    fun saveFailed(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "保存失败，可重新分析一次。"
        AppLanguage.ZhHant -> "儲存失敗，可重新分析一次。"
        AppLanguage.En -> "Save failed. You can analyze it again."
        AppLanguage.Ja -> "保存に失敗しました。もう一度分析できます。"
    }

    fun isSaveFailure(value: String): Boolean =
        listOf("失败", "失敗", "failed", "失敗しました").any { value.contains(it, ignoreCase = true) }

    fun displayAdvice(value: String, language: AppLanguage): String =
        translateKnown(value, language, adviceMap) ?: value

    fun displayTag(value: String, language: AppLanguage): String =
        translateKnown(value, language, tagMap) ?: value

    fun compactTag(value: String, language: AppLanguage): String {
        val translated = displayTag(value.trim(), language).replace(Regex("\\s+"), if (language == AppLanguage.En) " " else "")
        val exact = compactTagMap[translated] ?: compactTagMap[value.trim()]
        if (exact != null) return displayTag(exact, language)
        val limit = if (language == AppLanguage.En) 18 else 6
        return if (translated.length <= limit) translated else translated.take(limit)
    }

    fun isRiskTag(value: String): Boolean = anyKnown(value, riskTags)

    fun isPositiveTag(value: String): Boolean = anyKnown(value, positiveTags)

    fun isWarningTag(value: String): Boolean = anyKnown(value, warningTags)

    private fun translateKnown(
        value: String,
        language: AppLanguage,
        source: Map<String, Map<AppLanguage, String>>,
    ): String? {
        val clean = value.trim()
        source[clean]?.get(language)?.let { return it }
        val canonical = source.entries.firstOrNull { (_, labels) ->
            labels.values.any { it.equals(clean, ignoreCase = true) }
        }?.key
        return canonical?.let { source[it]?.get(language) }
    }

    private fun anyKnown(value: String, known: Set<String>): Boolean {
        val clean = value.trim()
        return known.any { token ->
            clean.contains(token, ignoreCase = true) ||
                tagMap[token]?.values.orEmpty().any { clean.contains(it, ignoreCase = true) } ||
                adviceMap[token]?.values.orEmpty().any { clean.contains(it, ignoreCase = true) }
        }
    }

    private val adviceMap = mapOf(
        "只能尝一小口" to labels("只能尝一小口", "只能嚐一小口", "Taste only a bite", "一口だけ味見"),
        "需要严格控量" to labels("需要严格控量", "需要嚴格控量", "Needs portion control", "量をしっかり控える"),
        "可以适量吃" to labels("可以适量吃", "可以適量吃", "Moderate amount is fine", "適量ならよい"),
        "可以适量多吃" to labels("可以适量多吃", "可以適量多吃", "You can eat a bit more", "少し多めでもよい"),
    )

    private val tagMap = mapOf(
        "适量吃" to labels("适量吃", "適量吃", "Moderate", "適量"),
        "控量" to labels("控量", "控量", "Portion control", "量控えめ"),
        "浅尝" to labels("浅尝", "淺嚐", "Taste only", "少しだけ"),
        "可多吃" to labels("可多吃", "可多吃", "Can eat more", "少し多め可"),
        "推荐" to labels("推荐", "推薦", "Recommended", "おすすめ"),
        "适合" to labels("适合", "適合", "Good fit", "合う"),
        "部分适合" to labels("部分适合", "部分適合", "Partial fit", "一部合う"),
        "不适合" to labels("不适合", "不適合", "Poor fit", "合いにくい"),
        "不太适合" to labels("不太适合", "不太適合", "Poor fit", "合いにくい"),
        "无法判断" to labels("无法判断", "無法判斷", "Unknown", "判断不可"),
        "油脂高" to labels("油脂高", "油脂高", "High oil", "油多め"),
        "油炸" to labels("油炸", "油炸", "Fried", "揚げ物"),
        "重口味" to labels("重口味", "重口味", "Heavy seasoning", "味濃いめ"),
        "糖偏高" to labels("糖偏高", "糖偏高", "High sugar", "糖質高め"),
        "钠偏高" to labels("钠偏高", "鈉偏高", "High sodium", "塩分高め"),
        "油盐高" to labels("油盐高", "油鹽高", "High oil/salt", "油塩高め"),
        "蔬菜少" to labels("蔬菜少", "蔬菜少", "Low vegetables", "野菜少なめ"),
        "蛋白足" to labels("蛋白足", "蛋白足", "Enough protein", "たんぱく質十分"),
        "有蛋白" to labels("有蛋白", "有蛋白", "Has protein", "たんぱく質あり"),
        "有蔬菜" to labels("有蔬菜", "有蔬菜", "Has vegetables", "野菜あり"),
        "轻负担" to labels("轻负担", "負擔輕", "Light burden", "軽め"),
        "少油控脂" to labels("少油控脂", "少油控脂", "Lower-oil choice", "油控えめ"),
        "负担高" to labels("负担高", "負擔高", "Heavy burden", "負担重め"),
        "碳水多" to labels("碳水多", "碳水多", "High carbs", "炭水化物多め"),
        "蛋白少" to labels("蛋白少", "蛋白少", "Low protein", "たんぱく質少なめ"),
        "减脂友好" to labels("减脂友好", "減脂友好", "Fat-loss friendly", "減量向き"),
        "减脂谨慎" to labels("减脂谨慎", "減脂謹慎", "Fat-loss caution", "減量注意"),
        "主食" to labels("主食", "主食", "Staple", "主食"),
        "甜品" to labels("甜品", "甜品", "Dessert", "デザート"),
        "快餐" to labels("快餐", "速食", "Fast food", "ファストフード"),
        "多菜品" to labels("多菜品", "多菜品", "Multiple dishes", "複数料理"),
        "复合餐" to labels("复合餐", "複合餐", "Mixed meal", "複合食"),
    )

    private val compactTagMap = mapOf(
        "可以适量吃" to "适量吃",
        "可以適量吃" to "适量吃",
        "Moderate amount is fine" to "适量吃",
        "適量ならよい" to "适量吃",
        "需要严格控量" to "控量",
        "需要嚴格控量" to "控量",
        "Needs portion control" to "控量",
        "量をしっかり控える" to "控量",
        "只能尝一小口" to "浅尝",
        "只能嚐一小口" to "浅尝",
        "Taste only a bite" to "浅尝",
        "一口だけ味見" to "浅尝",
        "可以适量多吃" to "可多吃",
        "可以適量多吃" to "可多吃",
        "You can eat a bit more" to "可多吃",
        "少し多めでもよい" to "可多吃",
    )

    private val riskTags = setOf("控量", "浅尝", "不适合", "不太适合", "油炸", "油脂高", "糖偏高", "钠偏高", "油盐高", "重口味")
    private val positiveTags = setOf("轻负担", "蛋白足", "有蔬菜", "有蛋白", "少糖", "清淡", "推荐", "适合", "减脂友好")
    private val warningTags = setOf("负担高", "碳水多", "蔬菜少", "蛋白少", "少油控脂", "减脂谨慎", "甜品", "快餐")

    private fun labels(zhHans: String, zhHant: String, en: String, ja: String): Map<AppLanguage, String> = mapOf(
        AppLanguage.ZhHans to zhHans,
        AppLanguage.ZhHant to zhHant,
        AppLanguage.En to en,
        AppLanguage.Ja to ja,
    )
}
