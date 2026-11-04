package com.example.data

data class CatalogDrug(
    val id: String,
    val name: String,
    val genericName: String,
    val atcCode: String,
    val category: String,
    val defaultDosage: String,
    val form: String,
    val recommendedTime: String,
    val mealInstruction: String,
    val evidenceLevel: String,
    val mechanism: String,
    val safetyNote: String,
    val colorHex: Long
)

data class ToxicHerbAlert(
    val id: String,
    val title: String,
    val toxicCompound: String,
    val hazardType: String,
    val commonSources: String,
    val clinicalEvidence: String,
    val severity: String
)

data class EvidencePyramidTier(
    val level: String,
    val title: String,
    val subtitle: String,
    val reliabilityScore: Int,
    val description: String
)

object EvidenceDrugCatalog {

    val categories = listOf(
        "全部",
        "心血管与降压",
        "内分泌与代谢",
        "解热镇痛抗炎",
        "消化系统用药",
        "呼吸与抗过敏",
        "抗感染与抗病毒",
        "神经、睡眠与心理",
        "循证营养补充"
    )

    private val coreCatalogDrugs = listOf(
        // ==================== 心血管与降压 ====================
        CatalogDrug(
            id = "atorvastatin",
            name = "阿托伐他汀钙片",
            genericName = "Atorvastatin Calcium",
            atcCode = "C10AA05",
            category = "心血管与降压",
            defaultDosage = "20 mg (1片)",
            form = "片剂",
            recommendedTime = "21:30",
            mealInstruction = "睡前服用",
            evidenceLevel = "Grade A • WHO基本药物",
            mechanism = "高选择性、竞争性 HMG-CoA 还原酶抑制剂，显著降低 LDL-C 并逆转/稳定动脉粥样硬化斑块，获全球多项万人级双盲 RCT (ASCOT-LLA / PROVE-IT) 证实可显著降低心梗与脑卒中死亡率。",
            safetyNote = "服药期间严禁饮用西柚汁（抑制 CYP3A4）；切勿联用“红曲米/血脂康”或不明降脂中药，以防横纹肌溶解与叠加肝损伤。",
            colorHex = 0xFFAF52DEL
        ),
        CatalogDrug(
            id = "rosuvastatin",
            name = "瑞舒伐他汀钙片",
            genericName = "Rosuvastatin Calcium",
            atcCode = "C10AA07",
            category = "心血管与降压",
            defaultDosage = "10 mg (1片)",
            form = "片剂",
            recommendedTime = "21:30",
            mealInstruction = "睡前或任意时间",
            evidenceLevel = "Grade A • 强效亲水性他汀",
            mechanism = "亲水性强效 HMG-CoA 还原酶抑制剂，肝脏选择性高，不经 CYP3A4 代谢，药物相互作用更少，降 LDL-C 幅度可达 45%-55% (JUPITER 研究)。",
            safetyNote = "首次服药后 4-8 周复查血脂、转氨酶与肌酸激酶 (CK)。拒绝用三七粉、丹参粉替代他汀类正规抗动脉硬化治疗。",
            colorHex = 0xFF5856D6L
        ),
        CatalogDrug(
            id = "amlodipine",
            name = "苯磺酸氨氯地平片",
            genericName = "Amlodipine Besylate",
            atcCode = "C08CA01",
            category = "心血管与降压",
            defaultDosage = "5 mg (1片)",
            form = "片剂",
            recommendedTime = "07:30",
            mealInstruction = "空腹或餐后",
            evidenceLevel = "Grade A • 一线长效降压基药",
            mechanism = "长效二氢吡啶类钙通道阻滞剂 (CCB)，半衰期长达 35-50 小时，直接舒张血管平滑肌，平稳控制 24 小时血压并预防清晨血压高峰。",
            safetyNote = "严禁用所谓“平肝潜阳中药/罗布麻茶”替代规范降压药；拒绝含有麻黄碱、甘草成分的中成药，以免引发反跳性高血压。",
            colorHex = 0xFFFF2D55L
        ),
        CatalogDrug(
            id = "valsartan",
            name = "缬沙坦胶囊",
            genericName = "Valsartan",
            atcCode = "C09CA03",
            category = "心血管与降压",
            defaultDosage = "80 mg (1粒)",
            form = "胶囊",
            recommendedTime = "07:30",
            mealInstruction = "空腹或餐后",
            evidenceLevel = "Grade A • ARB 降压护肾基石",
            mechanism = "选择性阻断血管紧张素 II 的 AT1 受体，抑制血管收缩与醛固酮释放，在平稳降压的同时降低尿蛋白、延缓肾功能减退并逆转左心室肥厚 (Val-HeFT 研究)。",
            safetyNote = "妊娠期禁用；定期监测血钾与血肌酐。无干咳副作用（优于早期 ACEI 类药物）。",
            colorHex = 0xFFFF2D55L
        ),
        CatalogDrug(
            id = "irbesartan",
            name = "厄贝沙坦片",
            genericName = "Irbesartan",
            atcCode = "C09CA04",
            category = "心血管与降压",
            defaultDosage = "150 mg (1片)",
            form = "片剂",
            recommendedTime = "07:30",
            mealInstruction = "空腹或餐后",
            evidenceLevel = "Grade A • 高血压伴糖尿病肾病首选",
            mechanism = "长效强效 AT1 受体拮抗剂，生物利用度高达 60%-80% 且不受食物影响。IDNT 与 IRMA-2 大型随机双盲试验证实其具有独立于降压作用之外的肾脏靶器官保护作用。",
            safetyNote = "保护肾脏靠严格控压控糖与 RASI 靶向阻断，切勿盲目服用含马兜铃酸或不明植物成分的“保肾中药”，以免加速进展至尿毒症！",
            colorHex = 0xFFFF3B30L
        ),
        CatalogDrug(
            id = "metoprolol_succinate",
            name = "琥珀酸美托洛尔缓释片",
            genericName = "Metoprolol Succinate ER",
            atcCode = "C07AB02",
            category = "心血管与降压",
            defaultDosage = "47.5 mg (1片)",
            form = "缓释片",
            recommendedTime = "07:30",
            mealInstruction = "空腹或随餐",
            evidenceLevel = "Grade A • 冠心病与心衰金三角基石",
            mechanism = "高选择性 β1 肾上腺素受体阻滞剂，采用多单位微囊缓释技术，24 小时恒速释放，降低静息心率与心肌耗氧量，预防心源性猝死 (MERIT-HF 研究)。",
            safetyNote = "微囊缓释片可沿刻痕掰开服用，但严禁咀嚼或压碎！严禁突然停药，以防诱发心绞痛反弹或恶性心律失常。",
            colorHex = 0xFFAF52DEL
        ),
        CatalogDrug(
            id = "aspirin",
            name = "阿司匹林肠溶片",
            genericName = "Aspirin Enteric-Coated",
            atcCode = "B01AC06",
            category = "心血管与降压",
            defaultDosage = "100 mg (1片)",
            form = "片剂",
            recommendedTime = "07:00",
            mealInstruction = "空腹服用",
            evidenceLevel = "Grade A • 心脑血管二级预防基石",
            mechanism = "不可逆乙酰化血小板环氧合酶-1 (COX-1) 丝氨酸残基，阻断血栓素 A2 (TXA2) 生成，抑制血小板聚集，预防动脉粥样硬化血栓形成。",
            safetyNote = "精确肠溶包衣片必须在餐前空腹用足量温水整片吞服（加快胃排空，减少胃内停留）。严禁与丹参、三七、银杏叶、水蛭等“活血中药”联用，以免诱发消化道大出血或脑出血！",
            colorHex = 0xFFFF2D55L
        ),
        CatalogDrug(
            id = "clopidogrel",
            name = "硫酸氢氯吡格雷片",
            genericName = "Clopidogrel Bisulfate",
            atcCode = "B01AC04",
            category = "心血管与降压",
            defaultDosage = "75 mg (1片)",
            form = "片剂",
            recommendedTime = "08:00",
            mealInstruction = "空腹或餐后",
            evidenceLevel = "Grade A • P2Y12 抗血小板标准药物",
            mechanism = "选择性且不可逆地抑制血小板二磷酸腺苷 (ADP) P2Y12 受体，阻断糖蛋白 GPIIb/IIIa 复合物活化，是冠脉支架植入术后及脑梗死二级预防核心用药 (CURE / CAPRIE 研究)。",
            safetyNote = "如需联用抑酸药护胃，应优先选用雷贝拉唑或泮托拉唑，避免联用奥美拉唑（会抑制 CYP2C19 降低氯吡格雷活化）。",
            colorHex = 0xFFFF3B30L
        ),
        CatalogDrug(
            id = "rivaroxaban",
            name = "利伐沙班片",
            genericName = "Rivaroxaban",
            atcCode = "B01AF01",
            category = "心血管与降压",
            defaultDosage = "15 mg / 20 mg (1片)",
            form = "片剂",
            recommendedTime = "18:30",
            mealInstruction = "随餐服用",
            evidenceLevel = "Grade A • 新型口服直接 Xa 因子抗凝药",
            mechanism = "高选择性直接抑制游离和结合的凝血因子 Xa，阻断凝血酶生成，无需常规监测 INR，广泛用于非瓣膜性房颤卒中预防及深静脉血栓/肺栓塞治疗 (ROCKET-AF 研究)。",
            safetyNote = "15mg 与 20mg 规格必须与食物同服，以使生物利用度从 66% 提升至接近 100%！严禁擅自联用活血化瘀类中药汤剂。",
            colorHex = 0xFF5856D6L
        ),

        // ==================== 内分泌与代谢 ====================
        CatalogDrug(
            id = "metformin",
            name = "盐酸二甲双胍缓释片",
            genericName = "Metformin Hydrochloride XR",
            atcCode = "A10BA02",
            category = "内分泌与代谢",
            defaultDosage = "500 mg (1片)",
            form = "缓释片",
            recommendedTime = "18:30",
            mealInstruction = "随餐服用",
            evidenceLevel = "Grade A • 全球2型糖尿病一线首选用药",
            mechanism = "激活 AMPK 通路，抑制肝糖原异生与肝糖输出，改善外周骨骼肌胰岛素敏感性；单用不增加低血糖风险，且具有明确的心血管长期获益证据 (UKPDS 研究)。",
            safetyNote = "整片吞服勿嚼碎（粪便中若出现空药壳属正常现象）。警惕号称“纯中药根治糖尿病”的非法制剂（常非法掺入格列本脲或苯乙双胍，极易诱发致死性低血糖或乳酸酸中毒）。",
            colorHex = 0xFF34C759L
        ),
        CatalogDrug(
            id = "dapagliflozin",
            name = "达格列净片",
            genericName = "Dapagliflozin",
            atcCode = "A10BK01",
            category = "内分泌与代谢",
            defaultDosage = "10 mg (1片)",
            form = "片剂",
            recommendedTime = "08:00",
            mealInstruction = "空腹或餐后",
            evidenceLevel = "Grade A • 降糖/心衰/护肾三栖基石",
            mechanism = "高选择性抑制肾脏近端小管钠-葡萄糖协同转运蛋白 2 (SGLT2)，促进尿糖与尿钠排泄，降低肾小球囊内高压。DAPA-HF 与 DAPA-CKD 双盲试验证实其可大幅降低心衰住院与肾衰竭风险。",
            safetyNote = "服药期间建议每日多饮温水（增加 300-500ml）并保持会阴部清洁，以预防泌尿生殖道感染。",
            colorHex = 0xFF34C759L
        ),
        CatalogDrug(
            id = "empagliflozin",
            name = "恩格列净片",
            genericName = "Empagliflozin",
            atcCode = "A10BK03",
            category = "内分泌与代谢",
            defaultDosage = "10 mg (1片)",
            form = "片剂",
            recommendedTime = "08:00",
            mealInstruction = "空腹或餐后",
            evidenceLevel = "Grade A • EMPA-REG 心血管获益认证",
            mechanism = "高选择性 SGLT-2 抑制剂，通过非胰岛素依赖途径排糖降压减重，显著降低 2 型糖尿病合并心血管高危患者 38% 的心血管死亡风险。",
            safetyNote = "每日固定清晨服用；如遇大型手术或禁食状态需遵医嘱暂停，预防正常血糖性酮症酸中毒。",
            colorHex = 0xFF34C759L
        ),
        CatalogDrug(
            id = "semaglutide",
            name = "司美格鲁肽注射液",
            genericName = "Semaglutide Injection",
            atcCode = "A10BJ06",
            category = "内分泌与代谢",
            defaultDosage = "0.5 mg / 1.0 mg (每周1次)",
            form = "注射剂",
            recommendedTime = "09:00",
            mealInstruction = "不限饮食",
            evidenceLevel = "Grade A • 长效 GLP-1 受体激动剂",
            mechanism = "与人类 GLP-1 具有 94% 同源性，以葡萄糖浓度依赖方式促进胰岛素分泌、抑制胰高血糖素分泌并延缓胃排空，兼具强效控糖、科学减重及降低主要心血管不良事件 (SUSTAIN-6 / SELECT 研究)。",
            safetyNote = "每周固定同一天皮下注射一次；有甲状腺髓样癌 (MTC) 个人或家族史者禁用。切勿购买非正规渠道分装的“减肥针”。",
            colorHex = 0xFFAF52DEL
        ),
        CatalogDrug(
            id = "levothyroxine",
            name = "左甲状腺素钠片",
            genericName = "Levothyroxine Sodium",
            atcCode = "H03AA01",
            category = "内分泌与代谢",
            defaultDosage = "50 μg (1片)",
            form = "片剂",
            recommendedTime = "06:45",
            mealInstruction = "空腹服用",
            evidenceLevel = "Grade A • 甲减替代治疗唯一国际标准",
            mechanism = "人工合成的左旋甲状腺素 (T4)，与人体内源性分泌的甲状腺素在分子结构上完全一致，在外周组织转化为活性 T3 发挥生理代谢调节作用。",
            safetyNote = "必须在早餐前至少 30-60 分钟空腹用白水送服；与豆浆、牛奶、铁剂、钙片必须间隔 4 小时以上！切勿相信“中药散结消甲减”而停药。",
            colorHex = 0xFFFF9500L
        ),
        CatalogDrug(
            id = "febuxostat",
            name = "非布司他片",
            genericName = "Febuxostat",
            atcCode = "M04AA03",
            category = "内分泌与代谢",
            defaultDosage = "20 mg / 40 mg (1片)",
            form = "片剂",
            recommendedTime = "08:30",
            mealInstruction = "空腹或餐后",
            evidenceLevel = "Grade A • 痛风降尿酸靶向抑制剂",
            mechanism = "高选择性非嘌呤类黄嘌呤氧化酶 (XO) 抑制剂，同时抑制氧化型和还原型 XO，强效阻断次黄嘌呤向尿酸转化；主要经肝脏代谢，轻中度肾功能不全患者无需调整剂量。",
            safetyNote = "从低剂量（20mg）起始以防血尿酸骤降诱发溶晶痛。痛风本质是高尿酸血症晶体沉积，喝所谓“排酸中药茶”无法替代靶向降尿酸治疗。",
            colorHex = 0xFF5856D6L
        ),

        // ==================== 解热镇痛抗炎 ====================
        CatalogDrug(
            id = "acetaminophen",
            name = "对乙酰氨基酚片",
            genericName = "Paracetamol / Acetaminophen",
            atcCode = "N02BE01",
            category = "解热镇痛抗炎",
            defaultDosage = "500 mg (1片)",
            form = "片剂",
            recommendedTime = "14:00",
            mealInstruction = "餐后或按需",
            evidenceLevel = "Grade A • WHO 解热镇痛一线基石",
            mechanism = "选择性抑制中枢神经系统下丘脑体温调节中枢的前列腺素合成，发挥精准退热与镇痛作用；无外周抗炎作用，对胃肠道黏膜与血小板凝血功能几乎无影响，是孕期、哺乳期及儿童退热首选用药。",
            safetyNote = "成人每日累计摄入总量严禁超过 4000mg（通常不超过 2000mg）！严禁同时服用感冒灵颗粒、维C银翘片等复方中成药（内含隐匿对乙酰氨基酚，极易叠加过量引发急性重症肝衰竭）！",
            colorHex = 0xFFFF2D55L
        ),
        CatalogDrug(
            id = "ibuprofen",
            name = "布洛芬缓释胶囊",
            genericName = "Ibuprofen Sustained-Release",
            atcCode = "M01AE01",
            category = "解热镇痛抗炎",
            defaultDosage = "300 mg (1粒)",
            form = "胶囊",
            recommendedTime = "08:30",
            mealInstruction = "餐后服用",
            evidenceLevel = "Grade A • WHO基本药物",
            mechanism = "非甾体抗炎药 (NSAID)，通过抑制环氧化酶 (COX-1/COX-2) 阻断前列腺素合成，精准缓解发热、痛经、牙痛与肌肉关节炎性疼痛。",
            safetyNote = "餐后服用以减少胃黏膜刺激。切勿与对乙酰氨基酚或其它 NSAIDs 交替混用，严禁饮酒。",
            colorHex = 0xFFFF9500L
        ),
        CatalogDrug(
            id = "celecoxib",
            name = "塞来昔布胶囊",
            genericName = "Celecoxib",
            atcCode = "M01AH01",
            category = "解热镇痛抗炎",
            defaultDosage = "200 mg (1粒)",
            form = "胶囊",
            recommendedTime = "08:30",
            mealInstruction = "餐后或随餐",
            evidenceLevel = "Grade A • 高选择性 COX-2 抑制剂",
            mechanism = "特异性抑制炎性部位诱导型环氧化酶-2 (COX-2)，而对保护胃黏膜的生理性 COX-1 几乎无抑制作用，在强效缓解骨关节炎、类风湿关节炎疼痛的同时显著降低消化道溃疡风险。",
            safetyNote = "对磺胺类药物过敏者禁用。相比非法添加地塞米松/双氯芬酸的所谓“秘制纯中药风湿丸”，塞来昔布成分透明、剂量可控。",
            colorHex = 0xFFAF52DEL
        ),
        CatalogDrug(
            id = "etoricoxib",
            name = "依托考昔片",
            genericName = "Etoricoxib",
            atcCode = "M01AH05",
            category = "解热镇痛抗炎",
            defaultDosage = "60 mg / 120 mg (1片)",
            form = "片剂",
            recommendedTime = "09:00",
            mealInstruction = "空腹或餐后",
            evidenceLevel = "Grade A • 急性痛风性关节炎一线药",
            mechanism = "高选择性 COX-2 抑制剂（对 COX-2 的选择性高达 106 倍），口服吸收极快（约 1 小时达峰），是急性痛风性关节炎发作期快速消炎止痛的指南一线推荐。",
            safetyNote = "痛风急性发作期 120mg 每日 1 次最长使用不超过 8 天；未控制的高血压或活动性消化道溃疡患者禁用。",
            colorHex = 0xFFFF3B30L
        )
    )

    val catalogDrugs: List<CatalogDrug> =
        EvidenceDrugCatalogExtra.extraCatalogDrugs + coreCatalogDrugs + EvidenceDrugCatalogMore.moreCatalogDrugs

    private val coreToxicHerbAlerts = listOf(
        ToxicHerbAlert(
            id = "aristolochic",
            title = "马兜铃酸 (Aristolochic Acid)",
            toxicCompound = "硝基菲羧酸类化合物 (I类人类致癌物)",
            hazardType = "不可逆肾间质纤维化 & 上尿路移行细胞癌",
            commonSources = "关木通、广防己、青木香、马兜铃、寻骨风、天仙藤、细辛（以及曾含该成分的龙胆泻肝丸等）",
            clinicalEvidence = "《Science Translational Medicine》全外显子测序研究证实：马兜铃酸可与人类 DNA 形成永久性加合物 (AA-DNA adducts)，诱发极高频率的 A:T → T:A 特征性基因突变，不存在所谓“安全剂量阈值”。",
            severity = "WHO I类致癌物 • 零容忍禁用"
        ),
        ToxicHerbAlert(
            id = "polygonum",
            title = "何首乌 / 制首乌 (Polygonum multiflorum)",
            toxicCompound = "蒽醌类衍生物、二苯乙烯苷及顺式异构体",
            hazardType = "急性药物性肝损伤 (DILI) • 重症肝衰竭",
            commonSources = "生何首乌、制何首乌、首乌丸、乌发保健品、养血生发胶囊",
            clinicalEvidence = "中国药物性肝损伤 (DILI) 流行病学多中心研究显示，中草药与膳食补充剂占全国 DILI 病因首位，其中何首乌无论生用或炮制（制首乌）均可引发免疫特异质型与直接肝细胞毒性。",
            severity = "NMPA 多次通报肝毒性修订警示"
        ),
        ToxicHerbAlert(
            id = "gynura",
            title = "土三七 (Gynura segetum)",
            toxicCompound = "吡咯里西定生物碱 (Pyrrolizidine Alkaloids, PAs)",
            hazardType = "肝窦阻塞综合征 (HSOS / 肝小静脉闭塞病)",
            commonSources = "民间跌打药酒、散装“三七粉”误用、千里光、佩兰、款冬花",
            clinicalEvidence = "吡咯里西定生物碱经肝脏 CYP3A4 代谢后生成高活性脱氢吡咯代谢物，直接破坏肝窦内皮细胞，导致顽固性腹水、黄疸及不可逆肝硬化，临床死亡率极高。",
            severity = "极高危肝窦毒性 • 严禁内服"
        ),
        ToxicHerbAlert(
            id = "psoralea",
            title = "补骨脂 (Psoralea corylifolia)",
            toxicCompound = "补骨脂素 (Psoralen)、异补骨脂素、补骨脂酚",
            hazardType = "胆汁淤积型肝损伤 & 光毒性皮炎",
            commonSources = "壮骨关节丸、仙灵骨葆、白癜风丸、固肾偏方",
            clinicalEvidence = "多项临床毒理学及国家药品不良反应监测中心通报证实，补骨脂提取物可干扰肝细胞胆汁酸转运蛋白 (BSEP)，引发重度黄疸与转氨酶急剧升高。",
            severity = "国家药监局专刊通报高风险品种"
        ),
        ToxicHerbAlert(
            id = "hidden_western",
            title = "中成药隐匿添加西药成分风险",
            toxicCompound = "对乙酰氨基酚、马来酸氯苯那敏、格列本脲、氢氯噻嗪等",
            hazardType = "重复用药过量导致急性肝坏死或重度低血糖休克",
            commonSources = "感冒灵颗粒、维C银翘片、消渴丸、珍菊降压片、鼻炎康片",
            clinicalEvidence = "许多患者误以为中成药“温和无西药成分”，在服用感冒中成药同时又服用泰诺/必理通，导致对乙酰氨基酚日摄入量突破 4000mg 极量，诱发急性肝衰竭。",
            severity = "临床最常见隐匿药物过量陷阱"
        )
    )

    val toxicHerbAlerts: List<ToxicHerbAlert> = coreToxicHerbAlerts + EvidenceDrugCatalogMore.moreToxicHerbAlerts

    val evidencePyramid = listOf(
        EvidencePyramidTier(
            level = "Level I (最高证据)",
            title = "系统评价与 Meta 分析 (Cochrane / 临床指南)",
            subtitle = "综合全球多项高质量双盲随机对照试验的定量汇总",
            reliabilityScore = 99,
            description = "现代循证医学的黄金标准。排除单一试验偶然性与偏倚，给出具有明确统计学显著性和置信区间的临床治疗推荐。"
        ),
        EvidencePyramidTier(
            level = "Level II",
            title = "大样本随机双盲安慰剂对照试验 (RCT)",
            subtitle = "随机分组 + 受试者与研究者双盲 + 安慰剂对照",
            reliabilityScore = 92,
            description = "现代新药获批上市（FDA/EMA/NMPA）的强制门槛。能够严格剥离疾病自愈倾向与心理安慰剂效应，验证真实药效与不良反应发生率。"
        ),
        EvidencePyramidTier(
            level = "Level III",
            title = "前瞻性队列研究与病例对照研究",
            subtitle = "真实世界流行病学追踪与药物警戒数据",
            reliabilityScore = 75,
            description = "用于追踪长期用药安全性、罕见不良反应（如马兜铃酸致肾癌流行病学关联）及特殊人群预后。"
        ),
        EvidencePyramidTier(
            level = "Level IV (不可作为有效性依据)",
            title = "个案报道、专家主观经验与古籍轶事",
            subtitle = "无对照组、无盲法、幸存者偏差重灾区",
            reliabilityScore = 15,
            description = "传统经验医学（如“某人喝了某方好了”、“古籍记载”）无法区分疾病自愈（如普通感冒 7 天自愈）与真实药效，且常掩盖慢性肝肾毒性。"
        )
    )

    fun defaultSampleMedications(): List<Medication> = listOf(
        Medication(
            name = "苯磺酸氨氯地平片",
            genericName = "Amlodipine Besylate",
            dosage = "5 mg (1片)",
            form = "片剂",
            time = "07:30",
            period = "晨起",
            mealInstruction = "空腹或餐后",
            frequency = "每天",
            evidenceLevel = "Grade A • 一线降压基药",
            clinicalNote = "长效二氢吡啶类钙拮抗剂 (CCB)，半衰期 35-50 小时，平稳控制 24 小时血压。请勿用任何无循证依据的草药茶替代。",
            isTakenToday = false,
            stockCount = 24,
            streakDays = 14,
            color = 0xFFFF2D55.toInt()
        ),
        Medication(
            name = "维生素 D3 软胶囊",
            genericName = "Cholecalciferol (Vit D3)",
            dosage = "1000 IU (1粒)",
            form = "胶囊",
            time = "12:30",
            period = "午餐",
            mealInstruction = "随餐服用",
            frequency = "每天",
            evidenceLevel = "Grade A • 指南推荐补充",
            clinicalNote = "脂溶性维生素，随午餐油脂同服可提升约 50% 吸收率，科学促进钙吸收与骨健康。",
            isTakenToday = false,
            stockCount = 45,
            streakDays = 14,
            color = 0xFFFF9500.toInt()
        ),
        Medication(
            name = "盐酸二甲双胍缓释片",
            genericName = "Metformin HCl XR",
            dosage = "500 mg (1片)",
            form = "缓释片",
            time = "18:30",
            period = "晚餐",
            mealInstruction = "随餐服用",
            frequency = "每天",
            evidenceLevel = "Grade A • 全球一线基石",
            clinicalNote = "随晚餐整片吞服，切勿掰开或嚼碎。拒绝任何号称“根治血糖”的非法添加中成药。",
            isTakenToday = false,
            stockCount = 5,
            streakDays = 21,
            color = 0xFF34C759.toInt()
        ),
        Medication(
            name = "阿托伐他汀钙片",
            genericName = "Atorvastatin Calcium",
            dosage = "20 mg (1片)",
            form = "片剂",
            time = "21:30",
            period = "睡前",
            mealInstruction = "睡前服用",
            frequency = "每天",
            evidenceLevel = "Grade A • WHO基本药物",
            clinicalNote = "HMG-CoA还原酶抑制剂，稳定斑块降低 LDL-C。服药期间严禁饮用西柚汁，切勿联用红曲或不明草药以防肌溶解与肝损伤。",
            isTakenToday = false,
            stockCount = 18,
            streakDays = 30,
            color = 0xFFAF52DE.toInt()
        )
    )
}
