package com.example.data

data class DietTargetMetric(
    val label: String,
    val value: String,
    val unitNote: String
)

data class FoodTrafficItem(
    val name: String,
    val portionOrMetric: String,
    val reason: String
)

data class ExerciseWorkoutItem(
    val name: String,
    val fittSpec: String, // Frequency / Intensity / Time
    val mechanismAndTip: String
)

data class ExercisePrescription(
    val title: String,
    val guidelineSource: String,
    val clinicalBenefit: String,
    val targetHeartRateOrIntensity: String,
    val bestTimingRule: String,
    val recommendedWorkouts: List<ExerciseWorkoutItem>,
    val contraindications: String
)

data class DietPlanProfile(
    val id: String,
    val shortTabName: String,
    val title: String,
    val guidelineSource: String,
    val clinicalBenefit: String,
    val accentColorHex: Long,
    val coreMetrics: List<DietTargetMetric>,
    val mealOrderTip: String,
    val greenFoods: List<FoodTrafficItem>,
    val yellowFoods: List<FoodTrafficItem>,
    val redFoods: List<FoodTrafficItem>,
    val exercisePrescription: ExercisePrescription,
    val drugFoodWarning: String,
    val mythDebunkTitle: String,
    val mythDebunkContent: String,
    val dailyChecklist: List<String>
)

object EvidenceDietCatalog {

    val dietProfiles = listOf(
        // ==================== 1. 高血压人群 (DASH 得舒饮食 + 降压运动处方) ====================
        DietPlanProfile(
            id = "hypertension_dash",
            shortTabName = "高血压 (DASH)",
            title = "DASH 得舒降压饮食与运动处方",
            guidelineSource = "NEJM DASH 随机对照试验 · BJSM 270项RCT运动降压循证指南",
            clinicalBenefit = "DASH 饮食可降收缩压 8~14 mmHg，联合规律有氧+等长运动可额外再降 5~8 mmHg",
            accentColorHex = 0xFFFF2D55L,
            coreMetrics = listOf(
                DietTargetMetric("每日钠摄入", "< 2000 mg", "折合食盐 < 5g（优选低钠富钾盐）"),
                DietTargetMetric("每日钾摄入", "3500-4700 mg", "促钠排泄、舒张血管内皮"),
                DietTargetMetric("每周有氧运动", "150-210 分钟", "心率控制 (220-年龄)×60%~70%")
            ),
            mealOrderTip = "【厨房核心替换】若肾功能正常，强烈建议将家中普通精制盐替换为「低钠富钾盐」（75%氯化钠 + 25%氯化钾）。《新英格兰医学杂志》(NEJM) 中国 2.1 万人 SSaSS 试验证实：仅换用低钠盐即可降低 14% 脑卒中与 12% 全因死亡率！",
            greenFoods = listOf(
                FoodTrafficItem("深绿色叶菜 (菠菜/油菜/芥蓝)", "每日 ≥ 500g", "高钾高镁高叶酸，天然拮抗钠离子升压效应"),
                FoodTrafficItem("高钾食材 (口蘑/紫菜/牛油果/香蕉)", "每日 1-2 份", "每100g口蘑含钾高达 3106mg，促尿钠排泄"),
                FoodTrafficItem("全谷物与杂豆 (燕麦/糙米/红芸豆)", "替代 1/2 精白米面", "富含可溶性膳食纤维与植物镁"),
                FoodTrafficItem("深海富脂鱼类 (三文鱼/青花鱼/鲈鱼)", "每周 2-3 次", "富含 Omega-3 (EPA/DHA)，改善血管内皮顺应性"),
                FoodTrafficItem("无盐原味坚果 & 低脂牛奶", "坚果15g/天 · 奶300ml", "补充生物高利用度钙与不饱和脂肪酸")
            ),
            yellowFoods = listOf(
                FoodTrafficItem("瘦猪牛羊肉 (红肉)", "每日 ≤ 50g (掌心大小)", "优先选择去皮禽肉与鱼虾替代红肉"),
                FoodTrafficItem("鸡蛋", "每日 1 个全蛋", "优质蛋白来源，无需丢弃蛋黄（除非重度高胆固醇）"),
                FoodTrafficItem("植物烹调油 (橄榄油/菜籽油)", "每日 ≤ 25g", "拒绝猪油、牛油、黄油及棕榈油")
            ),
            redFoods = listOf(
                FoodTrafficItem("隐形高钠调料与加工食品", "严格限制", "酱油(5ml≈1g盐)、蚝油、豆瓣酱、腐乳、榨菜、火腿肠、挂面"),
                FoodTrafficItem("西柚 / 葡萄柚 (服药期绝对禁忌)", "严禁食用", "含呋喃香豆素，强效抑制肠道 CYP3A4 酶，导致氨氯地平/非洛地平血药浓度暴增引发低血压休克！"),
                FoodTrafficItem("浓肉汤、咸汤与卤味熟食", "禁止饮汤", "一碗餐馆紫菜蛋花汤或骨头汤含盐量即高达 3-5g"),
                FoodTrafficItem("任何酒精饮品 (含红酒/药酒)", "安全剂量为 0", "酒精在短期扩管后会引发剧烈的交感兴奋与反跳性血压飙升")
            ),
            exercisePrescription = ExercisePrescription(
                title = "高血压循证运动处方 (有氧 + 等长静力训练)",
                guidelineSource = "《British Journal of Sports Medicine》270项RCT对网Meta分析",
                clinicalBenefit = "等长静力运动平均降压 8.24/4.0 mmHg，有氧运动平均降压 4.49/2.53 mmHg",
                targetHeartRateOrIntensity = "中等强度：靶心率 = (220 - 年龄) × 60%~70%，运动时微喘但能完整说出一句话",
                bestTimingRule = "【黄金时段：下午 16:00 - 19:00】严禁在清晨 6:00-9:00 空腹剧烈晨练！清晨人体交感神经兴奋、儿茶酚胺激增，是一天中血压最高峰与心梗脑出血高发时段。",
                recommendedWorkouts = listOf(
                    ExerciseWorkoutItem(
                        name = "靠墙静蹲 (等长静力运动之王)",
                        fittSpec = "每周 3 次 · 每次 4 组 × 2 分钟（组间休息 2 分钟）",
                        mechanismAndTip = "BJSM 研究证实降压效果排名第一！后背贴墙、小腿垂直地面（大腿与小腿夹角 110°-130° 即可，勿深蹲过低伤膝），全程保持均匀呼吸、绝对不要憋气！"
                    ),
                    ExerciseWorkoutItem(
                        name = "快步走 / 慢跑 / 功率自行车",
                        fittSpec = "每周 5-7 天 · 每次 30-45 分钟",
                        mechanismAndTip = "刺激血管内皮释放一氧化氮 (NO)，降低外周血管阻力，运动后即可产生长达 12-22 小时的“运动后低血压效应 (PEH)”。"
                    ),
                    ExerciseWorkoutItem(
                        name = "恒温游泳 / 水中健步走",
                        fittSpec = "每周 2-3 次 · 每次 30-40 分钟",
                        mechanismAndTip = "水的浮力减轻膝关节负担，静水压力促进下肢静脉回流并抑制肾素-血管紧张素系统活性（注意水温勿过冷以防血管痉挛）。"
                    )
                ),
                contraindications = "1. 【严禁憋气发力 (瓦氏动作)】举重物、大重量硬拉深蹲或用力憋气会使收缩压瞬间飙升突破 250 mmHg，极易诱发脑血管破裂；\n2. 【严禁急停与头低位动作】避免突然低头系鞋带、倒立或过山车式剧烈变位；\n3. 若安静未服药血压 ≥ 180/110 mmHg，应暂停一切运动，先通过药物平稳控压！"
            ),
            drugFoodWarning = "1. 服用「氨氯地平、硝苯地平、非洛地平」期间严禁吃西柚（葡萄柚）；\n2. 服用「沙坦类 (缬沙坦/厄贝沙坦)、普利类、螺内酯、非奈利酮」且伴有慢性肾脏病 (eGFR<45) 的患者，不宜盲目使用高钾低钠盐，以防高钾血症。",
            mythDebunkTitle = "破除降压谣言：喝醋软化血管？清晨猛练能摆脱降压药？",
            mythDebunkContent = "食醋进入胃肠道后即被碱性消化液中和，绝不可能溶解血管钙化斑块！同时，许多老年高血压患者迷信“凌晨五点起床迎着寒风剧烈晨练”，结果在清晨血压浪潮与寒冷血管收缩双重刺激下突发脑溢血或心梗。科学降压必须坚持晨起第一时间服长效降压药，把运动放在傍晚！",
            dailyChecklist = listOf(
                "今日做菜使用限盐勺或低钠富钾盐，未喝咸肉汤",
                "今日摄入满 1 斤蔬菜，且服药期未食用西柚与酒精",
                "今日在傍晚/餐后完成了 30 分钟快走或 4 组靠墙静蹲",
                "运动全程保持自然呼吸，未做憋气猛起或清晨寒风剧烈晨练"
            )
        ),

        // ==================== 2. 糖尿病人群 (211低GI饮食 + 餐后消峰运动处方) ====================
        DietPlanProfile(
            id = "diabetes_low_gi",
            shortTabName = "糖尿病 (低GI)",
            title = "211 低 GI 饮食与餐后消峰运动处方",
            guidelineSource = "ADA 美国糖尿病学会 · CDS 中国2型糖尿病运动治疗指南",
            clinicalBenefit = "211进餐顺序 + 餐后30分钟运动可削平餐后血糖峰值 35%~45%，综合降低 HbA1c 0.7%~1.5%",
            accentColorHex = 0xFF34C759L,
            coreMetrics = listOf(
                DietTargetMetric("211 餐盘比例", "2菜 : 1肉 : 1粗粮", "先吃菜肉、最后吃主食"),
                DietTargetMetric("餐后黄金运动窗", "餐后 30 分钟启动", "每次20-30分钟，直接拦截血糖波峰"),
                DietTargetMetric("有氧 + 抗阻增肌", "每周150分+2次力量", "肌肉是人体最大的“血糖储存水库”")
            ),
            mealOrderTip = "【循证进餐顺序疗法】发表于《Diabetes Care》的随机交叉试验证实：哪怕吃完全相同的食物，只要严格按照「①先喝清汤/吃绿叶蔬菜 → ②再吃鱼肉蛋豆制品 → ③最后吃米饭杂粮主食」的顺序进餐，餐后 2 小时血糖峰值即可下降 37%，胰岛素波动显著平缓！",
            greenFoods = listOf(
                FoodTrafficItem("非淀粉类绿叶/十字花科蔬菜", "每餐占餐盘 1/2", "菠菜、西兰花、生菜、黄瓜、番茄、菌菇（GI < 15，富含膳食纤维包裹淀粉）"),
                FoodTrafficItem("低 GI 全谷物与杂豆主食", "每餐 1 拳头大小", "燕麦粒、黑米、藜麦、荞麦、鹰嘴豆、绿豆（缓慢释放葡萄糖）"),
                FoodTrafficItem("优质蛋白 (鱼虾/去皮鸡胸/北豆腐/鸡蛋)", "每餐 1 掌心大小", "延缓胃排空速度，刺激内源性 GLP-1 分泌"),
                FoodTrafficItem("低 GI 整果 (苹果/蓝莓/草莓/樱桃/柚子)", "两餐之间 ≤ 150g", "血糖平稳时作为加餐整吃，绝不榨汁")
            ),
            yellowFoods = listOf(
                FoodTrafficItem("根茎类淀粉蔬菜 (土豆/山药/芋头/莲藕/南瓜)", "吃它必须扣减主食", "本质是主食淀粉而非蔬菜！吃 100g 土豆或山药需少吃半碗米饭"),
                FoodTrafficItem("精白米饭 / 白面馒头", "混合 1/3 以上杂粮同煮", "米饭建议偏硬颗粒分明，蒸煮时间越长糊化度越高、升糖越快"),
                FoodTrafficItem("坚果 (核桃/巴旦木)", "每日 ≤ 10-15g", "虽不直接升糖，但热量密度高，过量会加重胰岛素抵抗")
            ),
            redFoods = listOf(
                FoodTrafficItem("白米粥 / 烂面条 / 糯米制品 / 肠粉", "餐后血糖核弹", "熬煮糊化的白粥 GI 高达 88（几乎等同于直接喝葡萄糖水），吸收极快！"),
                FoodTrafficItem("伪“无糖食品” (无糖饼干/无糖月饼/无糖麦片)", "隐形精制淀粉与油脂", "虽不含蔗糖，但底座全是高 GI 精白面粉、糊精与起酥油，升糖依然极其猛烈"),
                FoodTrafficItem("鲜榨果汁 / 蜂蜜 / 红枣 / 桂圆 / 荔枝", "严禁食用", "榨汁破坏了细胞壁膳食纤维，游离果糖与葡萄糖瞬间涌入肝脏"),
                FoodTrafficItem("糯玉米 / 烤红薯 / 油条 / 煎饼果子", "高 GI + 高油脂双杀", "糯玉米富含支链淀粉，极易被淀粉酶瞬间水解消峰")
            ),
            exercisePrescription = ExercisePrescription(
                title = "糖尿病循证运动处方 (GLUT4 非胰岛素依赖摄糖)",
                guidelineSource = "ADA 糖尿病医学诊疗标准 · ACSM 美国运动医学会联合共识",
                clinicalBenefit = "肌肉主动收缩可直接激活 AMPK 通路，无需胰岛素即可将 GLUT4 转运蛋白移至细胞膜吞噬血糖，单次运动改善胰岛素敏感性长达 24-48 小时",
                targetHeartRateOrIntensity = "中等强度有氧 + 大肌群抗阻训练（相邻两次运动间隔不超过 48 小时，以维持胰岛素敏感性）",
                bestTimingRule = "【黄金消峰窗口：第一口饭后 30-45 分钟开始运动】此时食物葡萄糖刚进入血液爬升，立即进行 20-30 分钟快走或抗阻练习，可直接将血糖消耗在肌肉中，削平餐后峰值 2.0~3.5 mmol/L！",
                recommendedWorkouts = listOf(
                    ExerciseWorkoutItem(
                        name = "三餐后快步走 / 室内踏步 / 骑行",
                        fittSpec = "每日三餐后 30 分钟开始 · 每次 20-30 分钟",
                        mechanismAndTip = "步频保持在 100-120 步/分钟，直接拦截餐后 1 小时与 2 小时血糖洪峰。"
                    ),
                    ExerciseWorkoutItem(
                        name = "全身大肌群抗阻训练 (深蹲/弹力带/哑铃)",
                        fittSpec = "每周 2-3 次（隔天进行）· 每次 20-30 分钟",
                        mechanismAndTip = "人体 80% 的餐后葡萄糖由骨骼肌摄取储存！锻炼大腿股四头肌、臀肌与背阔肌等于直接扩建身体的“血糖水库”。"
                    ),
                    ExerciseWorkoutItem(
                        name = "办公/居家「比目鱼肌提踵 (踮脚尖)」",
                        fittSpec = "久坐时每 30 分钟进行 3-5 分钟持续提踵",
                        mechanismAndTip = "小腿比目鱼肌富含慢肌氧化纤维，即使坐姿持续提踵也能高效氧化血液中的葡萄糖与甘油三酯，打破久坐危害。"
                    )
                ),
                contraindications = "1. 【严禁空腹饥饿状态下剧烈运动】尤其是注射胰岛素或服用磺脲类/格列奈类药物的患者，运动时口袋必须随身携带 3-4 颗方糖或葡萄糖片（若服阿卡波糖者发生低血糖，必须吃纯葡萄糖片，吃饼干无效）；\n2. 穿浅色透气棉袜与合脚跑鞋，每日运动后检查双足有无水泡或破损（预防糖尿病足）！"
            ),
            drugFoodWarning = "1. 服用「阿卡波糖 (拜唐苹)」必须与第一口主食一同嚼服（若不吃主食则当餐不服）；\n2. 服用「二甲双胍缓释片」建议随晚餐整片吞服；\n3. 服用「达格列净/恩格列净」期间切勿采取极端生酮绝碳水饮食（以防诱发正常血糖性酮症酸中毒）！",
            mythDebunkTitle = "破除控糖谣言：吃南瓜降糖？光靠饿肚子不运动能控糖？",
            mythDebunkContent = "熟南瓜 GI 高达 75，吃南瓜只会升糖！此外，许多糖友靠“极端节食不吃肉不运动”来降糖，结果导致严重的肌肉流失（肌少症）——肌肉越少，身体储存消耗葡萄糖的能力越差，最终变成“皮包骨头但餐后血糖依然爆表”的恶性循环。吃够优质蛋白 + 抗阻增肌才是控糖正道！",
            dailyChecklist = listOf(
                "三餐严格执行「先吃菜 → 再吃肉蛋 → 最后吃粗粮主食」顺序",
                "今日未喝白米粥、含糖饮料，未吃伪无糖糕点",
                "午/晚餐后 30 分钟完成了 20 分钟以上快走或抗阻练习",
                "全天避免连续久坐超过 30 分钟，口袋常备应急葡萄糖片"
            )
        ),

        // ==================== 3. 普通健康人群 (哈佛膳食 + WHO 长寿心肺处方) ====================
        DietPlanProfile(
            id = "general_mediterranean",
            shortTabName = "普通人群",
            title = "哈佛健康餐盘与心肺抗衰运动处方",
            guidelineSource = "哈佛公共卫生学院 · WHO《关于身体活动和久坐行为指南》",
            clinicalBenefit = "提升最大摄氧量 (VO2max) 与骨骼肌质量，可降低 31%~40% 全因死亡率、心血管早发风险与肿瘤发病率",
            accentColorHex = 0xFFAF52DEL,
            coreMetrics = listOf(
                DietTargetMetric("每日食材种类", "≥ 12 种 / 周 ≥ 25 种", "天然食物多样化，拒绝深加工零食"),
                DietTargetMetric("Zone 2 有氧运动", "每周 150-300 分钟", "燃脂强心，提升线粒体密度"),
                DietTargetMetric("抗阻力量训练", "每周 2-3 次", "预防30岁后每年1%的肌肉与骨量流失")
            ),
            mealOrderTip = "【科学三餐黄金法则】早餐重高蛋白与慢碳水（鸡蛋+牛奶+燕麦+果蔬，唤醒代谢不犯困）；午餐八分饱、粗细搭配防餐后食困；晚餐距离入睡至少 3 小时以上，清淡少油，减轻夜间胃食管反流与肝脂合成负担。",
            greenFoods = listOf(
                FoodTrafficItem("彩虹深色蔬果 (菠菜/紫甘蓝/圣女果/蓝莓)", "每日蔬菜400g + 水果250g", "富含天然植物多酚、叶黄素、花青素与维生素 C"),
                FoodTrafficItem("深海鱼虾与大豆制品", "每周鱼类 ≥ 2次 · 每日豆制品", "优质低饱和脂肪蛋白，提供 DHA 与大豆异黄酮"),
                FoodTrafficItem("发酵乳制品 (无糖希腊酸奶 / 鲜牛奶)", "每日 300-500 ml", "最经济高效的膳食钙与活性益生菌来源"),
                FoodTrafficItem("全谷物 (燕麦/藜麦/全麦面包/黑米)", "占每日谷物 1/3~1/2", "富含 B 族维生素与不可溶性膳食纤维，预防便秘与结直肠肿瘤")
            ),
            yellowFoods = listOf(
                FoodTrafficItem("猪牛羊红肉", "每周 ≤ 350-500g", "WHO 将过量红肉列为 2A 类致癌风险，优选里脊等瘦肉部位"),
                FoodTrafficItem("黑咖啡与纯茶 (无糖绿茶/乌龙茶)", "每日咖啡因 ≤ 300mg", "循证医学证实每日 2-3 杯无糖黑咖啡或清茶有益心血管与肝脏健康"),
                FoodTrafficItem("精白米面", "适量搭配蔬菜蛋白同食", "避免单吃一碗阳春面或一盘炒饭的纯碳水饮食")
            ),
            redFoods = listOf(
                FoodTrafficItem("加工肉制品 (培根/火腿/腊肉/香肠/午餐肉)", "WHO I 类致癌物", "富含亚硝酸盐与多环芳烃，明确增加胃癌与结直肠癌风险"),
                FoodTrafficItem("含糖奶茶、碳酸饮料与代可可脂甜品", "促炎与脂肪肝元凶", "高果糖玉米糖浆直接在肝脏转化为内脏脂肪，加速皮肤糖化衰老"),
                FoodTrafficItem("高温油炸、烧烤与隔夜霉变坚果/谷物", "远离黄曲霉毒素与丙烯酰胺", "发苦的瓜子花生含剧毒 I 类致癌物黄曲霉毒素 B1，必须立刻吐掉漱口！")
            ),
            exercisePrescription = ExercisePrescription(
                title = "普通人群 WHO 黄金运动处方 (心肺 VO2max + 抗肌少症)",
                guidelineSource = "WHO 身体活动指南 · 《JAMA》万人心肺耐力与长寿队列研究",
                clinicalBenefit = "心肺耐力 (VO2max) 每提升 1 MET，全因死亡率下降 12%~15%；每周 2 次力量训练显著提升骨密度",
                targetHeartRateOrIntensity = "Zone 2 有氧心率 = (220 - 年龄) × 60%~70%（能用鼻子呼吸、能正常交谈的轻松慢跑/快走强度）",
                bestTimingRule = "【碎片化打破久坐 + 规律训练】每天走 7500~8500 步即可获得最佳心血管长寿收益（无需盲目暴走 2 万步伤膝）；睡前 2 小时内避免高强度剧烈运动以免影响深睡眠。",
                recommendedWorkouts = listOf(
                    ExerciseWorkoutItem(
                        name = "Zone 2 有氧耐力 (慢跑/快走/游泳/骑行)",
                        fittSpec = "每周 3-5 次 · 每次 35-50 分钟",
                        mechanismAndTip = "最大化刺激慢肌纤维线粒体生物合成与毛细血管网增生，高效燃烧内脏脂肪且皮质醇压力低。"
                    ),
                    ExerciseWorkoutItem(
                        name = "三大复合力量训练 (深蹲/俯卧撑/划船)",
                        fittSpec = "每周 2-3 次 · 每次 3-4 组 × 8-12 次",
                        mechanismAndTip = "对骨骼施加机械应力以刺激成骨细胞沉积钙质（比单纯吃钙片更关键），预防中老年骨质疏松与肌少症。"
                    ),
                    ExerciseWorkoutItem(
                        name = "柔韧性与核心平衡训练 (拉伸/平板支撑)",
                        fittSpec = "每次运动后 5-10 分钟",
                        mechanismAndTip = "缓解办公族颈椎腰椎肌筋膜紧张，增强核心稳定性并预防运动损伤。"
                    )
                ),
                contraindications = "1. 【警惕“周末战士”突击剧烈运动】平时久坐不动、周末突然高强度打球/跑马拉松极易导致跟腱断裂、半月板撕裂或运动性猝死；\n2. 感冒发烧期间严禁跑步或力量训练（病毒感染期剧烈运动极易诱发致命性暴发性心肌炎）！"
            ),
            drugFoodWarning = "日常健康人群无需盲目服用“护肝片、排毒胶囊、复合中草药茶”。室内办公族唯一建议常规补充的是「维生素 D3 (400-1000 IU/天)」。",
            mythDebunkTitle = "破除大众养生伪科学：骨头汤补钙？日行两万步最健康？",
            mythDebunkContent = "1. 骨头汤含钙仅 2-4mg/碗，全是脂肪与嘌呤，真正补钙靠的是牛奶+维D3+抗阻力量训练！\n2. 《Lancet Public Health》大型荟萃分析证实：每日步数在 7500-8500 步时全因死亡率下降已达平台期，盲目在硬水泥地上日行 2-3 万步反而会导致膝关节软骨不可逆磨损与滑膜炎！",
            dailyChecklist = listOf(
                "今日饮水满 1500-2000 ml，摄入鸡蛋、牛奶与 12 种以上天然食材",
                "今日未喝含糖奶茶饮料，未吃培根火腿等深加工肉类",
                "今日累计步数达 7000-8500 步或完成 30 分钟有氧/力量运动",
                "每久坐 45 分钟已起身活动拉伸 3 分钟"
            )
        ),

        // ==================== 4. 高血脂与护心 (TLC 降脂饮食 + 脂酶激活运动处方) ====================
        DietPlanProfile(
            id = "hyperlipidemia_tlc",
            shortTabName = "高血脂护心",
            title = "TLC 治疗性降脂饮食与燃脂运动处方",
            guidelineSource = "NCEP-ATP III · ESC 欧洲心脏病学会血脂管理指南",
            clinicalBenefit = "TLC 饮食 + 规律有氧运动可使甘油三酯 (TG) 下降 20%~40%、LDL-C 下降 10%~20%，并显著升高好胆固醇 (HDL-C)",
            accentColorHex = 0xFF5856D6L,
            coreMetrics = listOf(
                DietTargetMetric("饱和脂肪酸", "< 总热量 7%", "严控肥肉、内脏、黄油与椰子油"),
                DietTargetMetric("可溶性膳食纤维", "10-25 g / 天", "燕麦 β-葡聚糖肠道结合胆固醇"),
                DietTargetMetric("持续有氧燃脂", "每次 ≥ 30-45 分钟", "激活脂蛋白脂酶 (LPL) 清除血脂")
            ),
            mealOrderTip = "【血脂分型精准饮食】若体检是「低密度脂蛋白 (LDL-C) 偏高」，重点是减少猪牛肥肉、内脏、酥皮点心，多吃燕麦、豆类；若体检是「甘油三酯 (TG) 偏高」，重点则是戒酒、戒甜饮料、减少精白米面主食并坚持有氧运动！",
            greenFoods = listOf(
                FoodTrafficItem("燕麦麸皮 / 纯燕麦片 (富含 β-葡聚糖)", "每日早餐 40-50g", "FDA 认证可溶性纤维 β-葡聚糖可在肠道形成凝胶阻断胆固醇重吸收"),
                FoodTrafficItem("大豆蛋白 (豆腐/豆浆/毛豆) & 魔芋", "每日 25g 大豆蛋白", "植物甾醇与大豆球蛋白竞争性抑制肠道胆固醇转运"),
                FoodTrafficItem("深海青背鱼 (三文鱼/沙丁鱼/秋刀鱼)", "每周 2-3 次", "Omega-3 脂肪酸显著抑制肝脏极低密度脂蛋白 (VLDL) 合成"),
                FoodTrafficItem("特级初榨橄榄油 / 双低菜籽油 / 紫苏油", "低温烹调 ≤ 25g/天", "单不饱和脂肪酸 (油酸) 替代饱和脂肪，保护高密度脂蛋白 (HDL)")
            ),
            yellowFoods = listOf(
                FoodTrafficItem("去皮鸡鸭肉、瘦牛里脊", "每日 50-75g", "烹饪前彻底剔除可见肥油与禽类外皮"),
                FoodTrafficItem("水煮蛋", "每日 1 个或隔日 1 个", "合并冠心病或高胆固醇血症者可每日吃蛋白、隔日吃1个全蛋")
            ),
            redFoods = listOf(
                FoodTrafficItem("动物脑花、肝肾肥肠、蟹黄鱼籽、鱿鱼", "超高胆固醇炸弹", "每100g猪脑含胆固醇高达 2571mg（一口即超标12倍）！"),
                FoodTrafficItem("五花肉、排骨汤、猪油、黄油、椰子油", "高饱和脂肪酸", "饱和脂肪酸会直接下调肝细胞表面 LDL 受体活性，使血液坏胆固醇飙升"),
                FoodTrafficItem("牛角包、蛋挞、起酥面包、植脂末奶茶", "富含反式脂肪酸", "反式脂肪酸既升高坏胆固醇 (LDL-C) 又降低好胆固醇 (HDL-C)，双重促发心梗")
            ),
            exercisePrescription = ExercisePrescription(
                title = "高血脂与逆转脂肪肝运动处方 (LPL 脂蛋白脂酶激活)",
                guidelineSource = "ESC/EAS 血脂异常管理指南 · 中华医学会脂肪肝防治指南",
                clinicalBenefit = "持续 30 分钟以上的中等强度有氧运动可成倍激活骨骼肌毛细血管内皮的脂蛋白脂酶 (LPL)，直接分解血液中的乳糜微粒与甘油三酯，并提升好胆固醇 (HDL-C) 5%~10%",
                targetHeartRateOrIntensity = "中等偏持续有氧强度：每次持续运动 ≥ 30-45 分钟（脂肪动员供能比例在 25 分钟后达到高峰）",
                bestTimingRule = "【每周累计 ≥ 180-250 分钟】针对甘油三酯偏高及非酒精性脂肪肝人群，只要通过有氧+控碳将体重减轻 5%~10%，肝内脂肪含量即可逆转下降 30%~50%！",
                recommendedWorkouts = listOf(
                    ExerciseWorkoutItem(
                        name = "中长距离快走 / 椭圆机 / 划船机",
                        fittSpec = "每周 5 次 · 每次 40-50 分钟",
                        mechanismAndTip = "调动全身大肌群持续氧化游离脂肪酸，对大体重高血脂人群膝关节友好。"
                    ),
                    ExerciseWorkoutItem(
                        name = "有氧骑行 / 游泳",
                        fittSpec = "每周 3-4 次 · 每次 45 分钟",
                        mechanismAndTip = "显著提高高密度脂蛋白 (HDL-C) 颗粒的胆固醇逆向转运能力（将血管壁胆固醇运回肝脏代谢）。"
                    ),
                    ExerciseWorkoutItem(
                        name = "间歇性自重循环训练 (开合跳/徒手深蹲/高抬腿)",
                        fittSpec = "每周 2 次 · 每次 20 分钟",
                        mechanismAndTip = "产生运动后过量氧耗 (EPOC)，在运动结束后数小时内持续消耗血液甘油三酯。"
                    )
                ),
                contraindications = "1. 【他汀服药期肌肉监测】服用阿托伐他汀/瑞舒伐他汀期间可正常进行中等强度运动，但应避免突然进行从未练过的超负荷离心力量训练（如百次负重深蹲）；若出现非运动引起的全身对称性肌肉酸痛、无力或酱油色尿，应立即就医查肌酸激酶 (CK)；\n2. 合并冠心病患者运动前需做 5-10 分钟充分热身，避免寒冷刺激。"
            ),
            drugFoodWarning = "服用「阿托伐他汀、辛伐他汀」期间严禁饮用西柚汁；切勿擅自购买服用“红曲米保健品”（红曲含有天然洛伐他汀且剂量不均，与处方他汀同服极易引发横纹肌溶解与急性肾衰）！",
            mythDebunkTitle = "破除降脂谣言：吃木耳洋葱能清血管？裹保鲜膜暴汗能减脂？",
            mythDebunkContent = "木耳多糖只能留在肠道，进不了血管去刮脂质斑块！同时，穿暴汗服或裹保鲜膜运动减掉的 100% 是水分和电解质，不但不能多燃脂一克，反而会导致血液浓缩、血黏度飙升，对高血脂伴冠心病患者极易诱发急性血栓！",
            dailyChecklist = listOf(
                "早餐已食用纯燕麦片或杂豆，全天未吃肥肉、内脏与酥皮点心",
                "完成了 40 分钟持续中等强度有氧运动（快走/骑行/游泳）",
                "运动前后及时补充温水，未穿暴汗服避免血液浓缩",
                "按时服用他汀等降脂药，未饮酒、未联用不明红曲保健品"
            )
        ),

        // ==================== 5. 痛风与高尿酸 (低嘌呤饮食 + 温和排酸运动处方) ====================
        DietPlanProfile(
            id = "gout_low_purine",
            shortTabName = "痛风低嘌呤",
            title = "低嘌呤零果糖饮食与温和排酸运动",
            guidelineSource = "ACR 美国风湿病学会痛风指南 · 中国高尿酸血症与痛风诊疗指南",
            clinicalBenefit = "戒果糖酒精 + 每日饮水 >2000ml + 温和低冲击有氧运动，协同药物降低痛风复发率",
            accentColorHex = 0xFFFF9500L,
            coreMetrics = listOf(
                DietTargetMetric("每日饮水量", "2000-3000 ml", "运动中每15分钟额外补水150ml"),
                DietTargetMetric("果糖与酒精", "0 摄入 (头号帮凶)", "果糖加速 ATP 降解产尿酸"),
                DietTargetMetric("运动强度红线", "严禁剧烈无氧冲刺", "无氧乳酸会霸占尿酸排泄通道")
            ),
            mealOrderTip = "【痛风饮食最大认知刷新】1. 比海鲜更隐蔽的痛风元凶是「果糖」（可乐、果汁、奶茶、蜂蜜）——果糖在肝脏磷酸化瞬间消耗大量 ATP，直接生成大量内源性尿酸！2. 豆腐、豆浆（已经过水加工稀释嘌呤）及香菇、芦笋等植物性嘌呤，大型队列研究证实并不增加痛风发作风险！",
            greenFoods = listOf(
                FoodTrafficItem("脱脂或低脂牛奶、酸奶", "每日 300-500 ml", "牛奶完全不含细胞核（0嘌呤），且乳清蛋白与酪蛋白可促进肾脏排泄尿酸！"),
                FoodTrafficItem("鸡蛋 (蛋白与蛋黄)", "每日 1-2 个", "单个细胞构成的完美零嘌呤优质蛋白来源"),
                FoodTrafficItem("绝大部分新鲜蔬菜与樱桃/草莓", "每日 ≥ 500g", "樱桃富含花青素，临床研究显示有助于降低痛风急性发作频率"),
                FoodTrafficItem("白开水、无糖苏打水、淡茶水、黑咖啡", "每日 ≥ 2000 ml", "保证每日尿量在 2000ml 以上，稀释尿液防肾结石")
            ),
            yellowFoods = listOf(
                FoodTrafficItem("猪牛瘦肉、去皮禽肉、淡水鱼", "缓解期每日 ≤ 100g", "切块后先沸水焯水 3-5 分钟（让 50% 水溶性嘌呤溶入水中），弃汤食肉"),
                FoodTrafficItem("北豆腐、豆浆", "缓解期适量食用", "加工过程中绝大部分水溶性嘌呤已随黄浆水流失，无需盲目禁食豆制品")
            ),
            redFoods = listOf(
                FoodTrafficItem("动物内脏 (猪肝/腰花/脑花/肥肠/鸡胗)", "极高嘌呤 (>300mg/100g)", "痛风急性期与缓解期均需严格禁食"),
                FoodTrafficItem("老火浓肉汤、骨头汤、海鲜火锅汤、鸡精", "液态嘌呤浓缩液", "嘌呤极易溶于水，久炖肉汤是诱发夜间急性痛风发作的头号导火索"),
                FoodTrafficItem("啤酒、白酒、黄酒及一切含酒精饮品", "产酸 + 抑排双重暴击", "啤酒不仅含鸟苷酸（极易产尿酸），酒精代谢产生的乳酸还会霸占肾小管排泄通道"),
                FoodTrafficItem("含糖饮料、鲜榨果汁、蜂蜜水", "高果糖促内源尿酸合成", "喝一瓶甜饮料或鲜榨橙汁后 1 小时内血尿酸即可明显飙升")
            ),
            exercisePrescription = ExercisePrescription(
                title = "高尿酸与痛风缓解期运动处方 (低冲击有氧 · 避开乳酸陷阱)",
                guidelineSource = "中国高尿酸血症与痛风诊疗指南 · 风湿免疫科临床运动共识",
                clinicalBenefit = "规律温和有氧运动可减轻体重与胰岛素抵抗，使血尿酸平稳下降；但必须严格避开产生大量乳酸的剧烈无氧运动",
                targetHeartRateOrIntensity = "中低强度纯有氧：心率控制在 (220 - 年龄) × 50%~65%，全程不喘粗气、不产生肌肉酸胀感",
                bestTimingRule = "【边运动边补水原则】痛风患者运动大量出汗会导致血液浓缩、尿量减少，极易诱发尿酸结晶沉积！必须在运动前 30 分钟喝水 300ml，运动中每 15 分钟喝白水/弱碱苏打水 150ml（严禁喝含果糖的运动功能饮料）！",
                recommendedWorkouts = listOf(
                    ExerciseWorkoutItem(
                        name = "平地休闲骑行 / 室内动感单车 (低阻力)",
                        fittSpec = "缓解期每周 4-5 次 · 每次 30-45 分钟",
                        mechanismAndTip = "双脚第一跖趾关节与踝膝关节承受冲击力极小，不磨损已有尿酸盐结晶沉积的关节滑膜。"
                    ),
                    ExerciseWorkoutItem(
                        name = "温水池游泳 (水温 ≥ 26℃)",
                        fittSpec = "缓解期每周 2-3 次 · 每次 30 分钟",
                        mechanismAndTip = "全身零负重有氧运动；注意切勿在冰冷泳池中游泳（低温会显著降低尿酸溶解度，诱发足趾关节尿酸盐析出结晶）！"
                    ),
                    ExerciseWorkoutItem(
                        name = "平地健步走 / 太极柔韧练习",
                        fittSpec = "每日 30-40 分钟 · 穿厚底缓震运动鞋",
                        mechanismAndTip = "温和促进外周血液循环与代谢，不引起 ATP 剧烈分解。"
                    )
                ),
                contraindications = "1. 【严禁剧烈无氧运动 (百米冲刺/高强度篮球/大重量力竭撸铁)】剧烈无氧运动一方面使肌肉 ATP 瞬间大量分解产生内源性腺嘌呤，另一方面产生大量「乳酸」——乳酸在肾小管会死死霸占尿酸排泄通道 (URAT1)，导致运动当晚血尿酸暴涨并诱发剧烈痛风！\n2. 【痛风急性发作期绝对制动】关节红肿剧痛期严禁下地行走或运动，应卧床抬高患肢并局部冷敷（严禁热敷或红花油揉搓）！"
            ),
            drugFoodWarning = "1. 服用「非布司他、别嘌醇、苯溴马隆」期间必须保证每日饮水 2000ml 以上；\n2. 痛风急性红肿剧痛期（前3-5天），若之前未服降尿酸药，切勿在剧痛当天突然加服非布司他（血尿酸骤降会导致关节内结晶崩解引发更剧烈的“溶晶痛”），应先用依托考昔/秋水仙碱消炎止痛，缓解后再平稳启动降尿酸！",
            mythDebunkTitle = "破除痛风谣言：痛风发作去跑步发汗能排酸？关节痛拿热水泡脚？",
            mythDebunkContent = "痛风患者去剧烈跑步暴汗，等于“血液脱水浓缩 + 乳酸阻断肾脏排酸 + 足趾关节机械撞击导致微结晶脱落”三重暴击，几乎百发百中诱发急性痛风！而急性期拿热水泡脚或热敷，会使局部血管扩张、炎性介质渗出翻倍，疼痛瞬间加剧。急性期唯一正确物理处理是抬高患肢 + 冰袋冷敷！",
            dailyChecklist = listOf(
                "今日累计饮用白开水/无糖苏打水已达 2000 ml 以上",
                "今日滴酒未沾，且未喝任何果汁、奶茶、可乐或含糖运动饮料",
                "今日进行了 30 分钟温和有氧运动（骑行/快走），未做剧烈无氧冲刺",
                "运动前中后已分次补充纯水，未喝老火肉汤或火锅汤"
            )
        )
    )
}
