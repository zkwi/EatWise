package com.example.eatwise.core.i18n

import java.util.Locale

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
            fromCodeOrNull(code) ?: default

        fun fromCodeOrNull(code: String?): AppLanguage? =
            values().firstOrNull { it.code == code }

        fun fromLocale(locale: Locale): AppLanguage {
            val language = locale.language.lowercase(Locale.ROOT)
            val script = locale.script.lowercase(Locale.ROOT)
            val country = locale.country.uppercase(Locale.ROOT)
            return when (language) {
                "zh" -> if (script == "hant" || country in setOf("TW", "HK", "MO")) ZhHant else ZhHans
                "en" -> En
                "ja" -> Ja
                else -> default
            }
        }
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
        AppLanguage.ZhHans -> "先吃蔬菜蛋白、主食少几口、重油菜少吃几口、清淡菜多吃几口、吃到七八分饱、下餐清淡一点"
        AppLanguage.ZhHant -> "先吃蔬菜蛋白、主食少幾口、重油菜少吃幾口、清淡菜多吃幾口、吃到七八分飽、下餐清淡一點"
        AppLanguage.En -> "Eat vegetables/protein first, reduce staple bites, take fewer bites of oily dishes, eat more lighter dishes, stop when comfortably full, keep next meal lighter"
        AppLanguage.Ja -> "野菜とたんぱく質を先に食べる、主食を少し減らす、油多めの料理を少し控える、軽めの料理を少し多めにする、腹八分目で止める、次の食事を軽めにする"
    }

    fun tagExamples(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "油脂高、油炸、重口味、糖偏高、钠偏高、蔬菜少、蛋白足、有蔬菜、轻负担、少糖、少盐、控量"
        AppLanguage.ZhHant -> "油脂高、油炸、重口味、糖偏高、鈉偏高、蔬菜少、蛋白足、有蔬菜、負擔輕、少糖、少鹽、控量"
        AppLanguage.En -> "High oil, Fried, Heavy flavor, High sugar, High sodium, Low vegetables, Enough protein, Vegetables, Light choice, Lower sugar, Lower sodium, Portion control"
        AppLanguage.Ja -> "油多め、揚げ物、味濃いめ、糖分多め、塩分高め、野菜少なめ、たんぱく十分、野菜あり、軽め、糖分控えめ、塩分控えめ、量控えめ"
    }

    fun lowValueTagExamples(language: AppLanguage): String = when (language) {
        AppLanguage.ZhHans -> "常规食材、常规分量、普通、一般、粗估、蛋白来源、油脂调味"
        AppLanguage.ZhHant -> "常規食材、常規份量、普通、一般、粗估、蛋白來源、油脂調味"
        AppLanguage.En -> "Regular ingredient, normal portion, ordinary, rough estimate, protein source, oil seasoning"
        AppLanguage.Ja -> "一般的な食材、普通量、ふつう、概算、たんぱく源、油調味"
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

    fun nutritionPromptPreview(userGoal: String, language: AppLanguage): String {
        val goal = userGoal.trim().replace(Regex("\\s+"), " ").ifBlank { defaultUserGoal(language) }.take(46)
        return when (language) {
            AppLanguage.ZhHans -> "这张餐食 + 目标「$goal」：按常见份量估算热量和营养结构，只给粗略区间。"
            AppLanguage.ZhHant -> "這張餐食 + 目標「$goal」：按常見份量估算熱量和營養結構，只給粗略區間。"
            AppLanguage.En -> "Meal + goal \"$goal\": estimate calories and nutrition structure as rough ranges only."
            AppLanguage.Ja -> "この食事 + 目標「$goal」：一般的な量からカロリーと栄養構成を大まかな範囲で推定する。"
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
            ?: when {
                source === adviceMap -> canonicalAdvice(clean)
                source === tagMap -> canonicalTag(clean)
                else -> null
            }
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

    private fun canonicalAdvice(value: String): String? {
        val clean = value.trim()
        val lower = clean.lowercase(Locale.ROOT)
        return when {
            clean.hasAny("尝", "嚐", "一小口", "浅尝", "淺嚐", "一口") ||
                lower.hasAny("taste", "bite", "small amount") ->
                "只能尝一小口"
            clean.hasAny("严格", "嚴格", "控量", "量を", "量控え", "控える") ||
                lower.hasAny("portion", "control", "strict", "limit") ->
                "需要严格控量"
            clean.hasAny("多吃", "多め") || lower.hasAny("eat more", "bit more", "more ok") ->
                "可以适量多吃"
            clean.hasAny("适量", "適量", "正常") || lower.hasAny("moderate", "fine", "ok") ->
                "可以适量吃"
            else -> null
        }
    }

    private fun canonicalTag(value: String): String? {
        val clean = value.trim()
        val lower = clean.lowercase(Locale.ROOT)
        return when {
            clean.hasAny("油炸", "炸物", "煎炸", "揚げ物") || lower.hasAny("fried", "deep-fried") ->
                "油炸"
            clean.hasAny("重口味", "口味重", "重辣", "麻辣", "味濃い") ||
                lower.hasAny("heavy flavor", "strong flavor", "heavy seasoning", "spicy") ->
                "重口味"
            clean.hasAny("油脂", "重油", "油腻", "油膩", "脂質多", "脂質高", "油多") ||
                lower.hasAny("high oil", "greasy", "oily", "high fat", "fatty") ->
                "油脂高"
            clean.hasAny("油盐", "油鹽", "油塩") && clean.hasAny("高", "多", "重", "偏高", "高め") ->
                "油盐高"
            clean.hasAny("少糖", "低糖", "糖分控え") ||
                lower.hasAny("low sugar", "lower sugar", "less sugar", "low-sugar", "lower-sugar") ->
                "少糖"
            clean.hasAny("糖", "甜", "甘さ") && clean.hasAny("高", "多", "偏高", "高め", "強め") ||
                lower.hasAny("high sugar", "high-sugar", "sugary", "sweet") ->
                "糖偏高"
            clean.hasAny("少盐", "少鹽", "低盐", "低鹽", "塩分控え", "薄味") ||
                lower.hasAny(
                    "low sodium",
                    "lower sodium",
                    "low-sodium",
                    "lower-sodium",
                    "low salt",
                    "lower salt",
                    "less salt",
                    "less salty",
                ) ->
                "少盐"
            clean.hasAny("盐", "鹽", "钠", "鈉", "咸", "鹹", "塩分") ||
                lower.hasAny("high sodium", "high-sodium", "salty", "salt") ->
                "钠偏高"
            clean.hasAny("蔬菜", "野菜", "纤维", "纖維") && clean.hasAny("少", "低", "不足", "缺", "少なめ") ||
                lower.hasAny("low vegetables", "few vegetables", "not enough vegetables") ->
                "蔬菜少"
            clean.hasAny("蔬菜", "野菜", "纤维", "纖維") || lower.hasAny("vegetable", "fiber") ->
                "有蔬菜"
            clean.hasAny("蛋白", "たんぱく") && clean.hasAny("少", "低", "不足", "少なめ") ||
                lower.hasAny("low protein", "low-protein", "not enough protein") ->
                "蛋白少"
            clean.hasAny("蛋白", "たんぱく") && clean.hasAny("足", "高", "充足", "十分") ||
                lower.hasAny("enough protein", "high protein", "high-protein", "protein-rich") ->
                "蛋白足"
            clean.hasAny("蛋白", "たんぱく") || lower == "protein" ->
                "有蛋白"
            clean.hasAny("轻负担", "負擔輕", "清淡", "軽め") ||
                lower.hasAny("light choice", "light meal", "lighter") ->
                "轻负担"
            clean.hasAny("负担高", "負擔高", "負担重", "熱量高", "能量高") ||
                lower.hasAny("heavy meal", "heavy burden", "calorie-dense", "high calorie") ->
                "负担高"
            clean.hasAny("碳水", "主食", "炭水化物") && clean.hasAny("高", "多", "偏高", "高め", "多め") ||
                lower.hasAny("high carbs", "high-carb", "carb-heavy") ->
                "碳水多"
            clean.hasAny("控脂", "少油", "油控え") || lower.hasAny("lower oil", "low oil", "less oil") ->
                "少油控脂"
            clean.hasAny("控量", "量控え", "量を控") || lower.hasAny("portion control", "control portion") ->
                "控量"
            clean.hasAny("浅尝", "淺嚐", "一口") || lower.hasAny("taste only") ->
                "浅尝"
            clean.hasAny("主食") || lower == "staple" ->
                "主食"
            clean.hasAny("甜品", "デザート") || lower.hasAny("dessert") ->
                "甜品"
            clean.hasAny("快餐", "速食", "ファストフード") || lower.hasAny("fast food") ->
                "快餐"
            clean.hasAny("多菜品", "複数料理") || lower.hasAny("multiple dishes") ->
                "多菜品"
            clean.hasAny("复合餐", "複合餐", "複合食") || lower.hasAny("mixed meal") ->
                "复合餐"
            else -> null
        }
    }

    private fun String.hasAny(vararg keywords: String): Boolean =
        keywords.any { contains(it, ignoreCase = true) }

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
        "可多吃" to labels("可多吃", "可多吃", "Can eat more", "多めOK"),
        "推荐" to labels("推荐", "推薦", "Recommended", "おすすめ"),
        "适合" to labels("适合", "適合", "Good fit", "合う"),
        "部分适合" to labels("部分适合", "部分適合", "Partial fit", "一部合う"),
        "不适合" to labels("不适合", "不適合", "Poor fit", "合いにくい"),
        "不太适合" to labels("不太适合", "不太適合", "Poor fit", "合いにくい"),
        "无法判断" to labels("无法判断", "無法判斷", "Unknown", "判断不可"),
        "油脂高" to labels("油脂高", "油脂高", "High oil", "油多め"),
        "油炸" to labels("油炸", "油炸", "Fried", "揚げ物"),
        "重口味" to labels("重口味", "重口味", "Heavy flavor", "味濃いめ"),
        "糖偏高" to labels("糖偏高", "糖偏高", "High sugar", "糖分多め"),
        "钠偏高" to labels("钠偏高", "鈉偏高", "High sodium", "塩分高め"),
        "油盐高" to labels("油盐高", "油鹽高", "High oil/salt", "油塩高め"),
        "蔬菜少" to labels("蔬菜少", "蔬菜少", "Low vegetables", "野菜少なめ"),
        "蛋白足" to labels("蛋白足", "蛋白足", "Enough protein", "たんぱく十分"),
        "有蛋白" to labels("有蛋白", "有蛋白", "Protein", "たんぱくあり"),
        "有蔬菜" to labels("有蔬菜", "有蔬菜", "Vegetables", "野菜あり"),
        "轻负担" to labels("轻负担", "負擔輕", "Light choice", "軽め"),
        "清淡" to labels("清淡", "清淡", "Light flavor", "薄味"),
        "少糖" to labels("少糖", "少糖", "Lower sugar", "糖分控えめ"),
        "少盐" to labels("少盐", "少鹽", "Lower sodium", "塩分控えめ"),
        "少油控脂" to labels("少油控脂", "少油控脂", "Lower oil", "油控えめ"),
        "负担高" to labels("负担高", "負擔高", "Heavy meal", "負担重め"),
        "碳水多" to labels("碳水多", "碳水多", "High carbs", "炭水化物多め"),
        "蛋白少" to labels("蛋白少", "蛋白少", "Low protein", "たんぱく質少なめ"),
        "减脂友好" to labels("减脂友好", "減脂友好", "Fat-loss friendly", "減量向き"),
        "减脂谨慎" to labels("减脂谨慎", "減脂謹慎", "Fat-loss caution", "減量注意"),
        "主食" to labels("主食", "主食", "Staple", "主食"),
        "甜品" to labels("甜品", "甜品", "Dessert", "デザート"),
        "快餐" to labels("快餐", "速食", "Fast food", "ファストフード"),
        "多菜品" to labels("多菜品", "多菜品", "Multiple dishes", "複数料理"),
        "复合餐" to labels("复合餐", "複合餐", "Mixed meal", "複合食"),
        "Heavy seasoning" to labels("重口味", "重口味", "Heavy flavor", "味濃いめ"),
        "Has protein" to labels("有蛋白", "有蛋白", "Protein", "たんぱくあり"),
        "Has vegetables" to labels("有蔬菜", "有蔬菜", "Vegetables", "野菜あり"),
        "Light burden" to labels("轻负担", "負擔輕", "Light choice", "軽め"),
        "Low sugar" to labels("少糖", "少糖", "Lower sugar", "糖分控えめ"),
        "Low sodium" to labels("少盐", "少鹽", "Lower sodium", "塩分控えめ"),
        "Less salty" to labels("少盐", "少鹽", "Lower sodium", "塩分控えめ"),
        "Lower-oil choice" to labels("少油控脂", "少油控脂", "Lower oil", "油控えめ"),
        "Heavy burden" to labels("负担高", "負擔高", "Heavy meal", "負担重め"),
        "糖質高め" to labels("糖偏高", "糖偏高", "High sugar", "糖分多め"),
        "たんぱく質十分" to labels("蛋白足", "蛋白足", "Enough protein", "たんぱく十分"),
        "たんぱく質あり" to labels("有蛋白", "有蛋白", "Protein", "たんぱくあり"),
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

    private val riskTags = setOf(
        "控量", "浅尝", "不适合", "不太适合", "油炸", "油脂高", "糖偏高", "钠偏高", "油盐高", "重口味",
        "Portion control", "Taste only", "Poor fit", "Fried", "High oil", "High sugar", "High sodium", "Heavy flavor",
        "量控えめ", "少しだけ", "合いにくい", "揚げ物", "油多め", "糖分多め", "塩分高め", "味濃いめ",
    )
    private val positiveTags = setOf(
        "轻负担", "蛋白足", "有蔬菜", "有蛋白", "少糖", "清淡", "推荐", "适合", "减脂友好",
        "Light choice", "Light flavor", "Lower sugar", "Lower sodium", "Enough protein", "Vegetables", "Protein", "Recommended", "Good fit", "Fat-loss friendly",
        "軽め", "薄味", "糖分控えめ", "塩分控えめ", "たんぱく十分", "野菜あり", "たんぱくあり", "おすすめ", "合う", "減量向き",
    )
    private val warningTags = setOf(
        "负担高", "碳水多", "蔬菜少", "蛋白少", "少油控脂", "减脂谨慎", "甜品", "快餐",
        "Heavy meal", "High carbs", "Low vegetables", "Low protein", "Lower oil", "Fat-loss caution", "Dessert", "Fast food",
        "負担重め", "炭水化物多め", "野菜少なめ", "たんぱく質少なめ", "油控えめ", "減量注意", "デザート", "ファストフード",
    )

    private fun labels(zhHans: String, zhHant: String, en: String, ja: String): Map<AppLanguage, String> = mapOf(
        AppLanguage.ZhHans to zhHans,
        AppLanguage.ZhHant to zhHant,
        AppLanguage.En to en,
        AppLanguage.Ja to ja,
    )
}
