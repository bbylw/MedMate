package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EvidenceDrugCatalog
import com.example.data.Medication
import com.example.data.inferPeriodFromTime
import com.example.data.normalizeTimeFormat
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.IOSHealthCoral
import com.example.ui.theme.PillColorPalette

@Composable
fun AddEditMedicationSheet(
    initialMedication: Medication? = null,
    onDismiss: () -> Unit,
    onSave: (Medication) -> Unit,
    onDelete: ((Medication) -> Unit)? = null
) {
    var name by remember(initialMedication) { mutableStateOf(initialMedication?.name ?: "") }
    var genericName by remember(initialMedication) { mutableStateOf(initialMedication?.genericName ?: "") }
    var dosage by remember(initialMedication) { mutableStateOf(initialMedication?.dosage ?: "20 mg (1片)") }
    var baseReferenceDosage by remember(initialMedication) {
        val catalogDefault = initialMedication?.name?.let { medName ->
            EvidenceDrugCatalog.catalogDrugs.find { it.name == medName }?.defaultDosage
        }
        mutableStateOf(catalogDefault ?: initialMedication?.dosage ?: "20 mg (1片)")
    }
    var form by remember(initialMedication) { mutableStateOf(initialMedication?.form ?: "片剂") }
    var time by remember(initialMedication) { mutableStateOf(initialMedication?.time ?: "08:00") }
    var mealInstruction by remember(initialMedication) { mutableStateOf(initialMedication?.mealInstruction ?: "餐后服用") }
    var evidenceLevel by remember(initialMedication) { mutableStateOf(initialMedication?.evidenceLevel ?: "Grade A • RCT强证据") }
    var clinicalNote by remember(initialMedication) {
        mutableStateOf(
            initialMedication?.clinicalNote
                ?: "遵循现代临床指南定量服用。避免与西柚汁或不明成分中草药同服，降低肝肾代谢负担。"
        )
    }
    var stockCount by remember(initialMedication) { mutableIntStateOf(initialMedication?.stockCount ?: 28) }
    var selectedColor by remember(initialMedication) {
        mutableLongStateOf(
            initialMedication?.color?.toLong()?.and(0xFFFFFFFFL) ?: PillColorPalette.first()
        )
    }

    val formOptions = listOf("片剂", "胶囊", "缓释片", "滴剂", "喷雾", "注射剂")
    val quickTimes = listOf("07:30", "08:30", "12:30", "18:30", "21:30", "22:30")
    val mealOptions = listOf("空腹服用", "随餐服用", "餐后服用", "睡前服用", "不限饮食")

    val canSave = name.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 36.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Authentic iOS Modal Navigation Header (取消 | 添加药物 | 存储)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "取消",
                    color = IOSHealthCoral,
                    fontSize = 16.sp
                )
            }
            Text(
                text = if (initialMedication == null) "添加药物" else "编辑药物与剂量",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(
                onClick = {
                    if (canSave) {
                        val normalizedTime = normalizeTimeFormat(time.ifBlank { "08:00" })
                        onSave(
                            Medication(
                                id = initialMedication?.id ?: 0,
                                name = name.trim(),
                                genericName = genericName.trim(),
                                dosage = dosage.ifBlank { "1${unitForForm(form)}" }.trim(),
                                form = form,
                                time = normalizedTime,
                                period = inferPeriodFromTime(normalizedTime),
                                mealInstruction = mealInstruction,
                                frequency = "每天",
                                evidenceLevel = evidenceLevel,
                                clinicalNote = clinicalNote.trim(),
                                isTakenToday = initialMedication?.isTakenToday ?: false,
                                lastTakenDate = initialMedication?.lastTakenDate ?: "",
                                lastTakenTime = initialMedication?.lastTakenTime ?: "",
                                stockCount = stockCount.coerceAtLeast(0),
                                streakDays = initialMedication?.streakDays ?: 1,
                                color = selectedColor.toInt()
                            )
                        )
                    }
                },
                enabled = canSave,
                modifier = Modifier.testTag("save_medication_button")
            ) {
                Text(
                    text = "存储",
                    color = if (canSave) IOSHealthCoral else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        // Quick-Fill Templates from EvidenceDrugCatalog
        if (initialMedication == null) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "常见循证药物快速填入",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 12.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EvidenceDrugCatalog.catalogDrugs.forEach { preset ->
                        Surface(
                            onClick = {
                                name = preset.name
                                genericName = preset.genericName
                                dosage = preset.defaultDosage
                                baseReferenceDosage = preset.defaultDosage
                                form = preset.form
                                time = preset.recommendedTime
                                mealInstruction = preset.mealInstruction
                                evidenceLevel = preset.evidenceLevel
                                clinicalNote = "${preset.mechanism}\n【安全提示】${preset.safetyNote}"
                                selectedColor = preset.colorHex
                            },
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(preset.colorHex))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = preset.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // iOS Inset Grouped Section 1: 药物名称
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "药物基本信息",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp)
            )
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    IOSInlineInputRow(
                        label = "名称",
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "必填（如：阿托伐他汀钙片）",
                        testTag = "input_med_name",
                        showDivider = true
                    )
                    IOSInlineInputRow(
                        label = "英文/通用名",
                        value = genericName,
                        onValueChange = { genericName = it },
                        placeholder = "选填（如：Atorvastatin）",
                        showDivider = false
                    )
                }
            }
        }

        // Interactive Dosage Selector Card (Stepper + Smart Presets + Custom Input)
        IOSDosageAdjusterCard(
            currentDosage = dosage,
            form = form,
            baseReferenceDosage = baseReferenceDosage,
            onDosageChange = { dosage = it }
        )

        // iOS Inset Grouped Section 2: 剂型与颜色
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "外观与剂型",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp)
            )
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        formOptions.forEach { option ->
                            val selected = form == option
                            Surface(
                                onClick = { form = option },
                                color = if (selected) IOSHealthCoral else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = option,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("标签颜色", style = MaterialTheme.typography.bodyLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            PillColorPalette.forEach { hex ->
                                val isSelected = selectedColor == hex
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(Color(hex))
                                        .border(
                                            width = if (isSelected) 2.5.dp else 0.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColor = hex }
                                )
                            }
                        }
                    }
                }
            }
        }

        // iOS Inset Grouped Section 3: 时间、饮食与库存
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "提醒时间与库存",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp)
            )
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("时间 (${inferPeriodFromTime(time)})", style = MaterialTheme.typography.bodyLarge)
                        BasicTextField(
                            value = time,
                            onValueChange = { time = it },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = IOSHealthCoral,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End
                            ),
                            cursorBrush = SolidColor(IOSHealthCoral),
                            singleLine = true,
                            modifier = Modifier
                                .width(90.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("input_med_time")
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickTimes.forEach { presetTime ->
                            val selected = time == presetTime
                            Surface(
                                onClick = { time = presetTime },
                                color = if (selected) IOSHealthCoral.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = presetTime,
                                    color = if (selected) IOSHealthCoral else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        mealOptions.forEach { option ->
                            val selected = mealInstruction == option
                            Surface(
                                onClick = { mealInstruction = option },
                                color = if (selected) IOSHealthCoral else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = option,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("药箱库存", style = MaterialTheme.typography.bodyLarge)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                onClick = { stockCount = (stockCount - 7).coerceAtLeast(0) },
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "-7",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                            Surface(
                                onClick = { stockCount = (stockCount - 1).coerceAtLeast(0) },
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Remove,
                                    contentDescription = "减少1",
                                    modifier = Modifier.padding(6.dp).size(16.dp)
                                )
                            }
                            Text(
                                text = "$stockCount ${unitForForm(form)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            Surface(
                                onClick = { stockCount += 1 },
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Add,
                                    contentDescription = "增加1",
                                    modifier = Modifier.padding(6.dp).size(16.dp)
                                )
                            }
                            Surface(
                                onClick = { stockCount += 7 },
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "+7",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (initialMedication != null && onDelete != null) {
            Surface(
                onClick = { onDelete(initialMedication) },
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_sheet_delete_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = null,
                        tint = CrimsonDanger,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "从今日用药计划中移除此药物",
                        color = CrimsonDanger,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun IOSInlineInputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    testTag: String? = null,
    showDivider: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(96.dp)
            )
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(IOSHealthCoral),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .let { if (testTag != null) it.testTag(testTag) else it }
                )
            }
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
            )
        }
    }
}
