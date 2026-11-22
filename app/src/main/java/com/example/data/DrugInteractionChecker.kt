package com.example.data

enum class InteractionSeverity {
    HIGH,
    MODERATE
}

data class DrugInteractionAlert(
    val id: String,
    val severity: InteractionSeverity,
    val title: String,
    val involvedDrugs: List<String>,
    val mechanism: String,
    val recommendation: String
)

data class PersonalizedDietAlert(
    val title: String,
    val detail: String,
    val matchedDrugs: List<String>
)

private enum class PharmClass(val label: String) {
    STATIN("他汀类降脂药"),
    FIBRATE("贝特类降脂药"),
    CCB("二氢吡啶类钙拮抗剂 (地平类)"),
    ARB("血管紧张素受体拮抗剂 (沙坦类)"),
    ACEI("血管紧张素转换酶抑制剂 (普利类)"),
    ARNI("脑啡肽酶/血管紧张素双重抑制剂 (ARNI)"),
    BETA_BLOCKER("β受体阻滞剂 (洛尔类)"),
    MRA("盐皮质激素受体拮抗剂 (保钾)"),
    ASPIRIN("阿司匹林"),
    P2Y12("P2Y12 抗血小板药"),
    ANTICOAGULANT("直接口服抗凝药 (DOAC)"),
    NSAID("非甾体抗炎药 (NSAIDs)"),
    PPI_CYP2C19("强 CYP2C19 抑制型质子泵抑制剂"),
    PPI_OTHER("其他质子泵抑制剂"),
    METFORMIN("二甲双胍"),
    SGLT2I("SGLT2 抑制剂 (列净类)"),
    DPP4I("DPP-4 抑制剂 (列汀类)"),
    INSULIN_OR_SU("胰岛素/磺脲类降糖药"),
    XOI_GOUT("黄嘌呤氧化酶抑制剂 (降尿酸)"),
    LEVOTHYROXINE("左甲状腺素钠"),
    CALCIUM_OR_ANTACID("钙剂/铝镁抗酸剂")
}

/**
 * Evidence-based clinical interaction, duplicate-therapy (including hidden fixed-dose combos),
 * and medication-to-nutrition matcher.
 */
object DrugInteractionChecker {

    private fun classify(med: Medication): Set<PharmClass> {
        val text = "${med.name} ${med.genericName}".lowercase()
        val classes = mutableSetOf<PharmClass>()

        // Statins & Fibrates
        if (text.contains("他汀") || text.contains("statin")) classes += PharmClass.STATIN
        if (text.contains("非诺贝特") || text.contains("fenofibrate")) classes += PharmClass.FIBRATE

        // Antihypertensives (explicitly handles fixed-dose combinations like 缬沙坦氨氯地平 / 沙库巴曲缬沙坦 / 培哚普利氨氯地平)
        if (text.contains("氨氯地平") || text.contains("硝苯地平") || text.contains("非洛地平") ||
            text.contains("amlodipine") || text.contains("nifedipine") || text.contains("felodipine")
        ) {
            classes += PharmClass.CCB
        }
        if (text.contains("沙库巴曲") || text.contains("sacubitril")) {
            classes += PharmClass.ARNI
        }
        if (text.contains("沙坦") || text.contains("sartan")) {
            classes += PharmClass.ARB
        }
        if (text.contains("普利") || text.contains("pril")) {
            classes += PharmClass.ACEI
        }
        if (text.contains("洛尔") || text.contains("olol")) {
            classes += PharmClass.BETA_BLOCKER
        }
        if (text.contains("非奈利酮") || text.contains("螺内酯") || text.contains("finerenone") || text.contains("spironolactone")) {
            classes += PharmClass.MRA
        }

        // Antithrombotics & NSAIDs
        if (text.contains("阿司匹林") || text.contains("aspirin")) {
            classes += PharmClass.ASPIRIN
        }
        if (text.contains("氯吡格雷") || text.contains("替格瑞洛") || text.contains("clopidogrel") || text.contains("ticagrelor")) {
            classes += PharmClass.P2Y12
        }
        if (text.contains("利伐沙班") || text.contains("阿哌沙班") || text.contains("达比加群") ||
            text.contains("rivaroxaban") || text.contains("apixaban")
        ) {
            classes += PharmClass.ANTICOAGULANT
        }
        if (text.contains("布洛芬") || text.contains("塞来昔布") || text.contains("依托考昔") ||
            text.contains("双氯芬酸") || text.contains("ibuprofen") || text.contains("celecoxib") || text.contains("etoricoxib")
        ) {
            classes += PharmClass.NSAID
        }

        // PPIs & Antacids
        if (text.contains("奥美拉唑") || text.contains("omeprazole")) {
            classes += PharmClass.PPI_CYP2C19
        } else if (text.contains("拉唑") || text.contains("prazole")) {
            classes += PharmClass.PPI_OTHER
        }
        if (text.contains("钙") && !text.contains("他汀") || text.contains("铝碳酸镁") || text.contains("calcium") || text.contains("hydrotalcite")) {
            classes += PharmClass.CALCIUM_OR_ANTACID
        }

        // Endocrine & Metabolic
        if (text.contains("二甲双胍") || text.contains("metformin")) classes += PharmClass.METFORMIN
        if (text.contains("列净") || text.contains("gliflozin")) classes += PharmClass.SGLT2I
        if (text.contains("列汀") || text.contains("gliptin")) classes += PharmClass.DPP4I
        if (text.contains("胰岛素") || text.contains("格列") || text.contains("insulin") || text.contains("glimepiride")) {
            classes += PharmClass.INSULIN_OR_SU
        }
        if (text.contains("非布司他") || text.contains("别嘌醇") || text.contains("febuxostat") || text.contains("allopurinol")) {
            classes += PharmClass.XOI_GOUT
        }
        if (text.contains("左甲状腺素") || text.contains("优甲乐") || text.contains("levothyroxine")) {
            classes += PharmClass.LEVOTHYROXINE
        }

        return classes
    }

    fun analyzeRegimen(medications: List<Medication>): List<DrugInteractionAlert> {
        if (medications.size < 2) return emptyList()

        val alerts = mutableListOf<DrugInteractionAlert>()
        val classMap = mutableMapOf<PharmClass, MutableList<Medication>>()
        medications.forEach { med ->
            classify(med).forEach { cls ->
                classMap.getOrPut(cls) { mutableListOf() }.add(med)
            }
        }

        // 1. ARNI + ACEI (Strict Contraindication - Angioedema)
        val arnis = classMap[PharmClass.ARNI].orEmpty()
        val aceis = classMap[PharmClass.ACEI].orEmpty()
        if (arnis.isNotEmpty() && aceis.isNotEmpty()) {
            alerts += DrugInteractionAlert(
                id = "arni_acei",
                severity = InteractionSeverity.HIGH,
                title = "严禁联用：ARNI 与普利类 (ACEI)",
                involvedDrugs = (arnis + aceis).map { it.name }.distinct(),
                mechanism = "沙库巴曲抑制脑啡肽酶、普利类抑制缓激肽降解，合用将导致缓激肽剧烈蓄积，显著诱发致命性血管性水肿与严重低血压。",
                recommendation = "立即联系心内科医师调整方案；从 ACEI 转换为沙库巴曲缬沙坦必须至少停药洗脱 36 小时。"
            )
        }

        // 2. ACEI + ARB Dual RAAS Blockade
        val pureArbs = classMap[PharmClass.ARB].orEmpty().filter { med -> med !in arnis }
        if (pureArbs.isNotEmpty() && aceis.isNotEmpty()) {
            alerts += DrugInteractionAlert(
                id = "acei_arb_dual",
                severity = InteractionSeverity.HIGH,
                title = "不推荐联用：普利类 (ACEI) + 沙坦类 (ARB)",
                involvedDrugs = (pureArbs + aceis).map { it.name }.distinct(),
                mechanism = "双重阻断肾素-血管紧张素系统 (ONTARGET 研究证实) 不能进一步降低心血管终点，反而显著增加高血钾、低血压及急性肾损伤风险。",
                recommendation = "请复核处方，通常仅需保留一种 RAAS 抑制剂。"
            )
        }

        // 3. Duplicate Therapy Detection (including fixed-dose combos like 缬沙坦氨氯地平 + 氨氯地平)
        val duplicateCheckClasses = listOf(
            PharmClass.STATIN to "重复使用他汀类降脂药，显著增加肝损伤与横纹肌溶解风险。",
            PharmClass.CCB to "重复使用地平类钙拮抗剂（含复方单片内成分），易致反射性心动过速、下肢水肿及低血压。",
            PharmClass.ARB to "重复使用沙坦类药物（含沙库巴曲缬沙坦或单片复方），增加高钾血症与肾灌注下降风险。",
            PharmClass.ACEI to "重复使用普利类降压药，增加低血压、干咳及肾损伤风险。",
            PharmClass.XOI_GOUT to "别嘌醇与非布司他同为黄嘌呤氧化酶抑制剂，严禁叠加使用。",
            PharmClass.SGLT2I to "重复使用列净类降糖药，叠加泌尿生殖道感染与酮症酸中毒风险。",
            PharmClass.METFORMIN to "重复摄入二甲双胍（注意西格列汀二甲双胍等复方制剂已含二甲双胍），易致胃肠道不耐受及乳酸酸中毒风险。",
            PharmClass.NSAID to "叠加使用两种非甾体抗炎药，胃肠道溃疡出血与急性肾损伤风险倍增。"
        )
        duplicateCheckClasses.forEach { (pharmClass, reason) ->
            val medsInClass = classMap[pharmClass].orEmpty().distinctBy { it.name }
            if (medsInClass.size >= 2) {
                alerts += DrugInteractionAlert(
                    id = "dup_${pharmClass.name.lowercase()}",
                    severity = InteractionSeverity.HIGH,
                    title = "疑似重复用药：${pharmClass.label}",
                    involvedDrugs = medsInClass.map { it.name },
                    mechanism = reason,
                    recommendation = "请核对是否误将单方与单片复方制剂重复加入计划，并向主治医师或药师确认。"
                )
            }
        }

        // 4. Clopidogrel + Omeprazole / Esomeprazole
        val clopidogrels = classMap[PharmClass.P2Y12].orEmpty().filter {
            it.name.contains("氯吡格雷") || it.genericName.contains("clopidogrel", ignoreCase = true)
        }
        val cyp2c19Ppis = classMap[PharmClass.PPI_CYP2C19].orEmpty()
        if (clopidogrels.isNotEmpty() && cyp2c19Ppis.isNotEmpty()) {
            alerts += DrugInteractionAlert(
                id = "clopidogrel_omeprazole",
                severity = InteractionSeverity.HIGH,
                title = "药效拮抗：氯吡格雷 + 奥美拉唑/艾司奥美拉唑",
                involvedDrugs = (clopidogrels + cyp2c19Ppis).map { it.name }.distinct(),
                mechanism = "奥美拉唑强效抑制肝药酶 CYP2C19，阻断氯吡格雷转化为活性代谢产物，削弱抗血小板保护效力，增加支架内血栓风险 (FDA 黑框警示)。",
                recommendation = "建议在医生指导下将护胃药更换为对 CYP2C19 影响极小的「泮托拉唑」或「雷贝拉唑」。"
            )
        }

        // 5. Anticoagulant + NSAIDs / Aspirin
        val anticoagulants = classMap[PharmClass.ANTICOAGULANT].orEmpty()
        val nsaids = classMap[PharmClass.NSAID].orEmpty()
        val aspirins = classMap[PharmClass.ASPIRIN].orEmpty()
        if (anticoagulants.isNotEmpty() && nsaids.isNotEmpty()) {
            alerts += DrugInteractionAlert(
                id = "doac_nsaid",
                severity = InteractionSeverity.HIGH,
                title = "高出血风险：口服抗凝药 + 非甾体止痛药",
                involvedDrugs = (anticoagulants + nsaids).map { it.name }.distinct(),
                mechanism = "直接口服抗凝药与布洛芬/塞来昔布等 NSAIDs 联用会显著损伤胃肠黏膜并协同抑制止血，大出血风险上升 2-3 倍。",
                recommendation = "尽量避免自行联用止痛消炎药；若必须镇痛，优先咨询医生改用对乙酰氨基酚并评估胃黏膜保护。"
            )
        }

        // 6. Aspirin + Ibuprofen
        val ibuprofens = nsaids.filter {
            it.name.contains("布洛芬") || it.genericName.contains("ibuprofen", ignoreCase = true)
        }
        if (aspirins.isNotEmpty() && ibuprofens.isNotEmpty()) {
            alerts += DrugInteractionAlert(
                id = "aspirin_ibuprofen",
                severity = InteractionSeverity.HIGH,
                title = "竞争拮抗：阿司匹林 + 布洛芬",
                involvedDrugs = (aspirins + ibuprofens).map { it.name }.distinct(),
                mechanism = "布洛芬可竞争性占据血小板 COX-1 结合位点，阻断低剂量阿司匹林的心脑血管抗血栓作用，并显著增加消化道出血风险。",
                recommendation = "如需退热镇痛优先选择对乙酰氨基酚；若必须合用布洛芬，应在服用阿司匹林至少 2 小时后再服用。"
            )
        }

        // 7. Moderate: Statin + Fibrate (Myopathy monitoring)
        val statins = classMap[PharmClass.STATIN].orEmpty()
        val fibrates = classMap[PharmClass.FIBRATE].orEmpty()
        if (statins.isNotEmpty() && fibrates.isNotEmpty()) {
            alerts += DrugInteractionAlert(
                id = "statin_fibrate",
                severity = InteractionSeverity.MODERATE,
                title = "监测肌病风险：他汀类 + 非诺贝特",
                involvedDrugs = (statins + fibrates).map { it.name }.distinct(),
                mechanism = "两类调脂药合用可增加骨骼肌损伤、肌酸激酶 (CK) 升高及横纹肌溶解的发生率。",
                recommendation = "建议错开时间服用（如晨服非诺贝特、晚服他汀），定期复查肝功能与 CK；若出现不明原因肌肉酸痛或茶色尿请立即就医。"
            )
        }

        // 8. Moderate: Levothyroxine + Calcium / Antacid / PPI
        val levothyroxines = classMap[PharmClass.LEVOTHYROXINE].orEmpty()
        val absorptionInterferers = (classMap[PharmClass.CALCIUM_OR_ANTACID].orEmpty() +
            classMap[PharmClass.PPI_CYP2C19].orEmpty() + classMap[PharmClass.PPI_OTHER].orEmpty()).distinctBy { it.name }
        if (levothyroxines.isNotEmpty() && absorptionInterferers.isNotEmpty()) {
            alerts += DrugInteractionAlert(
                id = "levo_absorption",
                severity = InteractionSeverity.MODERATE,
                title = "吸收间隔提醒：左甲状腺素钠 + 钙剂/胃药",
                involvedDrugs = (levothyroxines + absorptionInterferers).map { it.name }.distinct(),
                mechanism = "钙离子、铝镁抗酸剂及抑酸剂会与左甲状腺素结合或改变胃内 pH，显著降低甲状腺激素吸收率，导致 TSH 波动。",
                recommendation = "左甲状腺素钠务必在早餐前 30-60 分钟空腹单服，并与钙剂/铝碳酸镁严格间隔至少 4 小时。"
            )
        }

        // 9. Moderate: RAAS inhibitor + MRA (Hyperkalemia)
        val raasBlockers = (classMap[PharmClass.ARB].orEmpty() + classMap[PharmClass.ACEI].orEmpty() + arnis).distinctBy { it.name }
        val mras = classMap[PharmClass.MRA].orEmpty()
        if (raasBlockers.isNotEmpty() && mras.isNotEmpty()) {
            alerts += DrugInteractionAlert(
                id = "raas_mra_potassium",
                severity = InteractionSeverity.MODERATE,
                title = "血钾监测提醒：RAAS 抑制剂 + 盐皮质激素受体拮抗剂",
                involvedDrugs = (raasBlockers + mras).map { it.name }.distinct(),
                mechanism = "沙坦/普利类与非奈利酮或螺内酯联用虽对心肾有协同保护作用，但均减少肾脏排钾，可能引起高钾血症。",
                recommendation = "用药初期与调量后 2-4 周复查血钾及血肌酐，日常避免盲目使用高钾低钠盐或补钾保健品。"
            )
        }

        // 10. Moderate: Dual antiplatelet or Anticoagulant + Aspirin
        val p2y12s = classMap[PharmClass.P2Y12].orEmpty()
        if ((aspirins.isNotEmpty() && p2y12s.isNotEmpty()) || (anticoagulants.isNotEmpty() && aspirins.isNotEmpty())) {
            val combo = (aspirins + p2y12s + anticoagulants).map { it.name }.distinct()
            alerts += DrugInteractionAlert(
                id = "dual_antithrombotic",
                severity = InteractionSeverity.MODERATE,
                title = "联合抗栓出血监测提示",
                involvedDrugs = combo,
                mechanism = "双联抗血小板或抗凝联合抗血小板方案用于高危冠脉/房颤患者，期间需严密监测出血征象。",
                recommendation = "切勿自行加服丹参、三七、银杏叶等活血中药；如出现黑便、牙龈持续出血或皮下大片瘀斑请及时就诊。"
            )
        }

        // 11. Moderate: Insulin/SU + SGLT2i/Metformin hypoglycemia watch
        val insulinOrSu = classMap[PharmClass.INSULIN_OR_SU].orEmpty()
        val otherHypoglycemics = (classMap[PharmClass.SGLT2I].orEmpty() + classMap[PharmClass.METFORMIN].orEmpty()).distinctBy { it.name }
        if (insulinOrSu.isNotEmpty() && otherHypoglycemics.isNotEmpty()) {
            alerts += DrugInteractionAlert(
                id = "hypoglycemia_watch",
                severity = InteractionSeverity.MODERATE,
                title = "联合降糖低血糖防护提醒",
                involvedDrugs = (insulinOrSu + otherHypoglycemics).map { it.name }.distinct(),
                mechanism = "多机制联合降糖显著改善糖化血红蛋白，但在运动增量或进餐延迟时需防范低血糖反应。",
                recommendation = "规律监测空腹及餐后 2 小时指尖血糖，外出运动随身携带 15g 葡萄糖片或糖果应急。"
            )
        }

        return alerts.sortedBy { if (it.severity == InteractionSeverity.HIGH) 0 else 1 }
    }

    /**
     * Recommends matching DietPlanProfile IDs based on the user's active medications.
     */
    fun recommendDietProfileIds(medications: List<Medication>): Set<String> {
        val matched = linkedSetOf<String>()
        medications.forEach { med ->
            val classes = classify(med)
            if (PharmClass.CCB in classes || PharmClass.ARB in classes || PharmClass.ACEI in classes ||
                PharmClass.ARNI in classes || PharmClass.BETA_BLOCKER in classes
            ) {
                matched += "hypertension"
            }
            if (PharmClass.METFORMIN in classes || PharmClass.SGLT2I in classes ||
                PharmClass.DPP4I in classes || PharmClass.INSULIN_OR_SU in classes
            ) {
                matched += "diabetes"
            }
            if (PharmClass.STATIN in classes || PharmClass.FIBRATE in classes ||
                PharmClass.ASPIRIN in classes || PharmClass.P2Y12 in classes
            ) {
                matched += "hyperlipidemia"
            }
            if (PharmClass.XOI_GOUT in classes) {
                matched += "gout"
            }
        }
        if (matched.isEmpty()) {
            matched += "general"
        }
        return matched
    }

    /**
     * Produces concise, medication-driven diet & exercise safety tips for the active regimen.
     */
    fun buildPersonalizedDietAlerts(medications: List<Medication>): List<PersonalizedDietAlert> {
        if (medications.isEmpty()) return emptyList()
        val alerts = mutableListOf<PersonalizedDietAlert>()

        val grapefruitMeds = medications.filter { med ->
            val t = "${med.name} ${med.genericName}".lowercase()
            t.contains("阿托伐他汀") || t.contains("辛伐他汀") || t.contains("氨氯地平") ||
                t.contains("硝苯地平") || t.contains("非洛地平") || t.contains("atorvastatin") || t.contains("amlodipine")
        }
        if (grapefruitMeds.isNotEmpty()) {
            alerts += PersonalizedDietAlert(
                title = "严禁饮用西柚/葡萄柚汁 (CYP3A4 抑制)",
                detail = "您计划中的药物经肠道 CYP3A4 酶代谢，西柚汁会使血药浓度骤升，诱发低血压或肌肉损伤。",
                matchedDrugs = grapefruitMeds.map { it.name }
            )
        }

        val statinMeds = medications.filter { PharmClass.STATIN in classify(it) }
        if (statinMeds.isNotEmpty()) {
            alerts += PersonalizedDietAlert(
                title = "他汀用药期运动安全：监测肌痛",
                detail = "规律有氧运动可与他汀协同护心，但请避免突然进行超负荷马拉松或极量力量训练；若出现双侧近端肌肉压痛或深茶色尿请暂停运动并查肌酸激酶 (CK)。",
                matchedDrugs = statinMeds.map { it.name }
            )
        }

        val goutMeds = medications.filter { PharmClass.XOI_GOUT in classify(it) }
        if (goutMeds.isNotEmpty()) {
            alerts += PersonalizedDietAlert(
                title = "降尿酸期间：足量饮水 + 拒绝无氧冲刺",
                detail = "保持每日饮水 2000-3000ml 促进尿酸排泄；运动优先选择稳态低冲击有氧，避免高强度 HIIT 产生大量乳酸诱发急性痛风。",
                matchedDrugs = goutMeds.map { it.name }
            )
        }

        return alerts
    }
}
