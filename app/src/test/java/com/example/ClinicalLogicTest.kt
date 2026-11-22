package com.example

import com.example.data.DrugInteractionChecker
import com.example.data.InteractionSeverity
import com.example.data.Medication
import com.example.data.inferPeriodFromTime
import com.example.data.normalizeTimeFormat
import com.example.ui.components.buildSmartDosagePresets
import com.example.ui.components.formatDurationMinutes
import com.example.ui.components.stepDosageString
import com.example.ui.components.timeStringToMinutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClinicalLogicTest {

    private fun med(name: String, generic: String = "", dosage: String = "10 mg (1片)", time: String = "08:00") =
        Medication(name = name, genericName = generic, dosage = dosage, time = time)

    @Test
    fun safeStandardRegimenProducesNoConflicts() {
        val regimen = listOf(
            med("苯磺酸氨氯地平片", "Amlodipine Besylate", "5 mg (1片)", "07:30"),
            med("盐酸二甲双胍缓释片", "Metformin ER", "500 mg (1片)", "18:30"),
            med("阿托伐他汀钙片", "Atorvastatin Calcium", "20 mg (1片)", "21:30"),
            med("维生素 D3 软胶囊", "Cholecalciferol", "1000 IU (1粒)", "12:30")
        )
        val alerts = DrugInteractionChecker.analyzeRegimen(regimen)
        assertTrue("Standard safe regimen should have no conflicts", alerts.isEmpty())
    }

    @Test
    fun detectsArniPlusAceiContraindication() {
        val regimen = listOf(
            med("沙库巴曲缬沙坦钠片", "Sacubitril/Valsartan"),
            med("培哚普利叔丁胺片", "Perindopril")
        )
        val alerts = DrugInteractionChecker.analyzeRegimen(regimen)
        assertTrue(alerts.any { it.id == "arni_acei" && it.severity == InteractionSeverity.HIGH })
    }

    @Test
    fun detectsHiddenDuplicateInFixedDoseCombination() {
        val regimen = listOf(
            med("缬沙坦氨氯地平片 (I)", "Valsartan / Amlodipine"),
            med("苯磺酸氨氯地平片", "Amlodipine Besylate")
        )
        val alerts = DrugInteractionChecker.analyzeRegimen(regimen)
        assertTrue(alerts.any { it.id == "dup_ccb" && it.severity == InteractionSeverity.HIGH })
    }

    @Test
    fun detectsClopidogrelPlusOmeprazoleCyp2c19Conflict() {
        val regimen = listOf(
            med("硫酸氢氯吡格雷片", "Clopidogrel"),
            med("奥美拉唑肠溶胶囊", "Omeprazole")
        )
        val alerts = DrugInteractionChecker.analyzeRegimen(regimen)
        assertTrue(alerts.any { it.id == "clopidogrel_omeprazole" && it.severity == InteractionSeverity.HIGH })
    }

    @Test
    fun detectsAspirinPlusIbuprofenConflict() {
        val regimen = listOf(
            med("阿司匹林肠溶片", "Aspirin"),
            med("布洛芬缓释胶囊", "Ibuprofen")
        )
        val alerts = DrugInteractionChecker.analyzeRegimen(regimen)
        assertTrue(alerts.any { it.id == "aspirin_ibuprofen" && it.severity == InteractionSeverity.HIGH })
    }

    @Test
    fun detectsStatinPlusFibrateModerateAlert() {
        val regimen = listOf(
            med("阿托伐他汀钙片", "Atorvastatin"),
            med("非诺贝特胶囊 (微粒化)", "Fenofibrate")
        )
        val alerts = DrugInteractionChecker.analyzeRegimen(regimen)
        assertTrue(alerts.any { it.id == "statin_fibrate" && it.severity == InteractionSeverity.MODERATE })
    }

    @Test
    fun matchesDietProfilesFromActiveMedications() {
        val regimen = listOf(
            med("非布司他片", "Febuxostat"),
            med("达格列净片", "Dapagliflozin")
        )
        val matched = DrugInteractionChecker.recommendDietProfileIds(regimen)
        assertTrue("gout" in matched)
        assertTrue("diabetes" in matched)
    }

    @Test
    fun normalizesTimeFormatsAndInfersPeriods() {
        assertEquals("08:30", normalizeTimeFormat("8:30"))
        assertEquals("07:05", normalizeTimeFormat("705"))
        assertEquals("21:30", normalizeTimeFormat("21：30"))
        assertEquals("晨起", inferPeriodFromTime("07:30"))
        assertEquals("午餐", inferPeriodFromTime("12:30"))
        assertEquals("晚餐", inferPeriodFromTime("18:30"))
        assertEquals("睡前", inferPeriodFromTime("21:30"))
        assertEquals(450, timeStringToMinutes("07:30"))
        assertEquals("1小时15分钟", formatDurationMinutes(75))
    }

    @Test
    fun dosageSteppingAndPresetsPreserveDecimalAccuracy() {
        assertEquals("10 mg (半片)", stepDosageString("20 mg (1片)", -0.5f, "片剂"))
        assertEquals("30 mg (1.5片)", stepDosageString("20 mg (1片)", 0.5f, "片剂"))
        assertEquals("23.75 mg (半片)", stepDosageString("47.5 mg (1片)", -0.5f, "缓释片"))
        assertEquals("0.15 mg (1.5片)", stepDosageString("0.1 mg (1片)", 0.5f, "片剂"))
        val presets = buildSmartDosagePresets("80mg/5mg (1片)", "片剂")
        assertTrue("80mg/5mg (半片)" in presets)
        assertTrue("80mg/5mg (1片)" in presets)
    }
}
