package com.example.ui.components

import android.content.Context
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.DrugInteractionChecker
import com.example.data.Medication
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.IOSHealthCoral
import com.example.ui.theme.IOSHealthCoralGlow
import com.example.ui.theme.IOSMintGlow
import com.example.ui.theme.IOSMintGreen
import com.example.ui.theme.IOSPlumPurple
import com.example.ui.theme.IOSWarmOrange
import com.example.ui.theme.IOSWarmOrangeGlow
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ObsidianHeroSurface
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.math.min

@Composable
fun StatsAndStockScreen(
    medications: List<Medication>,
    currentDateIso: String = LocalDate.now().toString(),
    onRestock: (Medication, Int) -> Unit,
    onDeleteMedication: (Medication) -> Unit = {},
    onResetDaily: () -> Unit,
    onLoadPreset: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences("medmate_prefs", Context.MODE_PRIVATE)
    }
    val totalCount = medications.size
    val takenCount = medications.count { it.isTakenToday }
    val todayRate = if (totalCount > 0) (takenCount * 100) / totalCount else 0
    val lowStockMeds = medications.filter { it.stockCount <= 7 }
    val interactionAlerts = remember(medications) {
        DrugInteractionChecker.analyzeRegimen(medications)
    }

    LaunchedEffect(currentDateIso, todayRate, totalCount) {
        if (totalCount > 0) {
            prefs.edit().putInt("adherence_rate_$currentDateIso", todayRate).apply()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(IOSMintGreen.copy(alpha = 0.055f), Color.Transparent),
                        center = Offset(size.width * 0.85f, 40.dp.toPx()),
                        radius = size.width * 0.72f
                    ),
                    center = Offset(size.width * 0.85f, 40.dp.toPx()),
                    radius = size.width * 0.72f
                )
            },
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // iOS Large Title Header with Version Badge
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "健康摘要与药箱 · $currentDateIso",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Surface(
                            color = IOSPlumPurple.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "v${BuildConfig.VERSION_NAME}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp
                                ),
                                color = IOSPlumPurple,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "药箱统计",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 32.sp,
                            letterSpacing = (-0.8).sp
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (totalCount > 0) {
                    Surface(
                        onClick = onResetDaily,
                        color = IOSHealthCoral.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("reset_daily_button")
                    ) {
                        Text(
                            text = "重置今日",
                            style = MaterialTheme.typography.labelLarge,
                            color = IOSHealthCoral,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Modern 3-Column Highlights Row with Micro-Icon Badges
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IOSHighlightCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.CheckCircle,
                    category = "今日完成率",
                    value = "$todayRate%",
                    subtitle = "$takenCount/$totalCount 剂已记录",
                    accentColor = IOSMintGreen
                )
                IOSHighlightCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.LocalFireDepartment,
                    category = "连续达标",
                    value = "${medications.maxOfOrNull { it.streakDays } ?: 0}天",
                    subtitle = "稳态血药浓度",
                    accentColor = IOSHealthCoral
                )
                IOSHighlightCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Inventory2,
                    category = "购药提醒",
                    value = "${lowStockMeds.size}项",
                    subtitle = if (lowStockMeds.isEmpty()) "库存充足" else "余量不足7剂",
                    accentColor = if (lowStockMeds.isEmpty()) IOSPlumPurple else IOSWarmOrange
                )
            }
        }

        // Apple Health Style 7-Day Adherence Chart Card
        item {
            WeeklyAdherenceIOSCard(
                todayRate = todayRate,
                hasMedicationsToday = totalCount > 0,
                currentDateIso = currentDateIso,
                prefs = prefs
            )
        }

        // Clinical Drug-Drug Interaction & Duplicate Therapy Screening Card
        item {
            ClinicalSafetySummaryCard(
                medicationCount = totalCount,
                alerts = interactionAlerts
            )
        }

        // iOS Inset Grouped Inventory Section
        item {
            Text(
                text = "智能药箱余量（打卡自动扣减）",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }

        if (medications.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "药箱目前为空",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onLoadPreset,
                            colors = ButtonDefaults.buttonColors(containerColor = IOSHealthCoral),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("导入示例循证药箱")
                        }
                    }
                }
            }
        } else {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)),
                    shadowElevation = 1.5.dp
                ) {
                    Column {
                        medications.forEachIndexed { index, med ->
                            IOSStockRow(
                                medication = med,
                                showDivider = index < medications.lastIndex,
                                onRestock = { amount -> onRestock(med, amount) },
                                onDelete = { onDeleteMedication(med) }
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun IOSHighlightCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    category: String,
    value: String,
    subtitle: String,
    accentColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.75.dp, accentColor.copy(alpha = 0.22f)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(accentColor.copy(alpha = 0.09f), Color.Transparent),
                            center = Offset(size.width * 0.92f, size.height * 0.14f),
                            radius = size.width * 0.75f
                        ),
                        center = Offset(size.width * 0.92f, size.height * 0.14f),
                        radius = size.width * 0.75f
                    )
                }
                .padding(13.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = category,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun WeeklyAdherenceIOSCard(
    todayRate: Int,
    hasMedicationsToday: Boolean,
    currentDateIso: String,
    prefs: android.content.SharedPreferences
) {
    val parsedDate = runCatching { LocalDate.parse(currentDateIso) }.getOrElse { LocalDate.now() }
    val days = (6 downTo 0).map { offset ->
        if (offset == 0) {
            "今日"
        } else {
            when (parsedDate.minusDays(offset.toLong()).dayOfWeek) {
                DayOfWeek.MONDAY -> "一"
                DayOfWeek.TUESDAY -> "二"
                DayOfWeek.WEDNESDAY -> "三"
                DayOfWeek.THURSDAY -> "四"
                DayOfWeek.FRIDAY -> "五"
                DayOfWeek.SATURDAY -> "六"
                DayOfWeek.SUNDAY -> "日"
            }
        }
    }
    val nullableRates: List<Int?> = (6 downTo 0).map { offset ->
        if (offset == 0) {
            if (hasMedicationsToday) todayRate else null
        } else {
            val dayIso = parsedDate.minusDays(offset.toLong()).toString()
            val key = "adherence_rate_$dayIso"
            if (prefs.contains(key)) prefs.getInt(key, 0) else null
        }
    }
    val recordedRates = nullableRates.filterNotNull()
    val avgLabel = if (recordedRates.isNotEmpty()) {
        "已记录 ${recordedRates.size} 天 · 平均 ${recordedRates.sum() / recordedRates.size}%"
    } else {
        "今日开始记录"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(Brush.linearGradient(listOf(IOSHealthCoral, IOSHealthCoralGlow))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.BarChart,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "近 7 日用药依从趋势",
                        style = MaterialTheme.typography.labelLarge,
                        color = IOSHealthCoral,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = avgLabel,
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val animatedHeights = nullableRates.mapIndexed { idx, rate ->
                val targetFraction = when {
                    rate == null -> 0f
                    rate == 0 -> 0.06f
                    else -> rate.coerceIn(10, 100) / 100f
                }
                animateFloatAsState(
                    targetValue = targetFraction,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "chart_bar_$idx"
                ).value
            }

            val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
            val guideLineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.42f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Editorial Micro Y-Axis Labels
                Column(
                    modifier = Modifier
                        .height(118.dp)
                        .padding(end = 6.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "100",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily, fontSize = 8.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "50",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily, fontSize = 8.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily, fontSize = 8.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(118.dp)
                ) {
                    val barCount = nullableRates.size
                    val slotWidth = size.width / barCount
                    val barWidth = min(slotWidth * 0.46f, 24.dp.toPx())
                    val maxBarHeight = size.height - 6.dp.toPx()

                    // Crisp 100% & 50% dashed reference guidelines
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 7f), 0f)
                    drawLine(
                        color = guideLineColor.copy(alpha = 0.25f),
                        start = Offset(0f, 1.dp.toPx()),
                        end = Offset(size.width, 1.dp.toPx()),
                        strokeWidth = 0.8.dp.toPx(),
                        pathEffect = dashEffect
                    )
                    drawLine(
                        color = guideLineColor,
                        start = Offset(0f, maxBarHeight * 0.5f),
                        end = Offset(size.width, maxBarHeight * 0.5f),
                        strokeWidth = 0.9.dp.toPx(),
                        pathEffect = dashEffect
                    )

                    nullableRates.forEachIndexed { index, rate ->
                        val x = index * slotWidth + (slotWidth - barWidth) / 2f
                        drawRoundRect(
                            color = trackColor,
                            topLeft = Offset(x, 0f),
                            size = Size(barWidth, maxBarHeight),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                        if (rate != null) {
                            val barHeight = (animatedHeights[index] * maxBarHeight).coerceAtLeast(6.dp.toPx())
                            val barTopY = maxBarHeight - barHeight
                            val barBrush = when {
                                index == barCount - 1 -> Brush.verticalGradient(listOf(IOSHealthCoralGlow, IOSHealthCoral))
                                rate >= 90 -> Brush.verticalGradient(listOf(IOSMintGlow, IOSMintGreen))
                                rate > 0 -> Brush.verticalGradient(listOf(IOSWarmOrangeGlow, IOSWarmOrange))
                                else -> Brush.verticalGradient(listOf(Color(0xFFC7C7CC), Color(0xFFA8ABB6)))
                            }
                            drawRoundRect(
                                brush = barBrush,
                                topLeft = Offset(x, barTopY),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )
                            // Luminous specular pearl indicator on today's active bar
                            if (index == barCount - 1 && barHeight > 18.dp.toPx()) {
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.72f),
                                    radius = 2.2.dp.toPx(),
                                    center = Offset(x + barWidth / 2f, barTopY + 7.dp.toPx())
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                days.forEachIndexed { idx, label ->
                    val rate = nullableRates[idx]
                    val isToday = idx == days.lastIndex
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isToday) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ObsidianHeroSurface
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                                )
                            }
                        } else {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = rate?.let { "$it%" } ?: "--",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.sp
                            ),
                            color = if (isToday) IOSHealthCoral else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IOSStockRow(
    medication: Medication,
    showDivider: Boolean,
    onRestock: (Int) -> Unit,
    onDelete: () -> Unit
) {
    val stockColor = when {
        medication.stockCount <= 3 -> CrimsonDanger
        medication.stockCount <= 7 -> IOSWarmOrange
        else -> IOSMintGreen
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(medication.color).copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconForForm(medication.form),
                    contentDescription = null,
                    tint = Color(medication.color),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "余量 ${medication.stockCount} ${unitForForm(medication.form)} · 连续 ${medication.streakDays} 天",
                    style = MaterialTheme.typography.bodySmall,
                    color = stockColor,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(5.dp))
                val stockFraction = (medication.stockCount.coerceIn(0, 28) / 28f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(stockFraction.coerceAtLeast(0.05f))
                            .clip(RoundedCornerShape(50))
                            .background(stockColor)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = { onRestock(14) },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(9.dp)
                ) {
                    Text(
                        text = "+14",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                    )
                }
                Surface(
                    onClick = { onRestock(28) },
                    color = IOSHealthCoral.copy(alpha = 0.10f),
                    border = BorderStroke(0.5.dp, IOSHealthCoral.copy(alpha = 0.28f)),
                    shape = RoundedCornerShape(9.dp),
                    modifier = Modifier.testTag("restock_28_${medication.id}")
                ) {
                    Text(
                        text = "+28盒",
                        style = MaterialTheme.typography.labelMedium,
                        color = IOSHealthCoral,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                    )
                }
                Surface(
                    onClick = onDelete,
                    color = CrimsonDanger.copy(alpha = 0.10f),
                    border = BorderStroke(0.5.dp, CrimsonDanger.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(9.dp),
                    modifier = Modifier.testTag("stock_delete_${medication.id}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "移除${medication.name}",
                        tint = CrimsonDanger,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(16.dp)
                    )
                }
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 66.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
            )
        }
    }
}
