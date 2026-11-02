package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Medication
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.IOSHealthCoral
import com.example.ui.theme.IOSMintGreen
import com.example.ui.theme.IOSPlumPurple
import com.example.ui.theme.IOSWarmOrange
import com.example.ui.theme.JetBrainsMonoFontFamily
import kotlin.math.min

@Composable
fun StatsAndStockScreen(
    medications: List<Medication>,
    onRestock: (Medication, Int) -> Unit,
    onDeleteMedication: (Medication) -> Unit = {},
    onResetDaily: () -> Unit,
    onLoadPreset: () -> Unit
) {
    val totalCount = medications.size
    val takenCount = medications.count { it.isTakenToday }
    val todayRate = if (totalCount > 0) (takenCount * 100) / totalCount else 100
    val lowStockMeds = medications.filter { it.stockCount <= 7 }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // iOS Large Title Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "健康摘要与药箱",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
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

        // Apple Health Style 3-Column Highlights Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IOSHighlightCard(
                    modifier = Modifier.weight(1f),
                    category = "今日完成率",
                    value = "$todayRate%",
                    subtitle = "$takenCount/$totalCount 剂已记录",
                    accentColor = IOSMintGreen
                )
                IOSHighlightCard(
                    modifier = Modifier.weight(1f),
                    category = "连续达标",
                    value = "${medications.maxOfOrNull { it.streakDays } ?: 0}天",
                    subtitle = "稳态血药浓度",
                    accentColor = IOSHealthCoral
                )
                IOSHighlightCard(
                    modifier = Modifier.weight(1f),
                    category = "购药提醒",
                    value = "${lowStockMeds.size}项",
                    subtitle = if (lowStockMeds.isEmpty()) "库存充足" else "余量不足7剂",
                    accentColor = if (lowStockMeds.isEmpty()) IOSPlumPurple else IOSWarmOrange
                )
            }
        }

        // Apple Health Style 7-Day Adherence Chart Card
        item {
            WeeklyAdherenceIOSCard(todayRate = todayRate)
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
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface
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
    category: String,
    value: String,
    subtitle: String,
    accentColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = category,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
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
private fun WeeklyAdherenceIOSCard(todayRate: Int) {
    val days = listOf("一", "二", "三", "四", "五", "六", "今日")
    val rates = listOf(100, 100, 75, 100, 100, 100, todayRate)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = null,
                        tint = IOSHealthCoral,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "近 7 日用药依从趋势",
                        style = MaterialTheme.typography.labelLarge,
                        color = IOSHealthCoral,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "平均 ${(rates.sum() / rates.size)}%",
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                val barCount = rates.size
                val slotWidth = size.width / barCount
                val barWidth = min(slotWidth * 0.42f, 24.dp.toPx())
                val maxBarHeight = size.height - 6.dp.toPx()

                rates.forEachIndexed { index, rate ->
                    val x = index * slotWidth + (slotWidth - barWidth) / 2f
                    drawRoundRect(
                        color = Color(0xFFE5E5EA).copy(alpha = 0.6f),
                        topLeft = Offset(x, 0f),
                        size = Size(barWidth, maxBarHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                    val barHeight = (rate.coerceIn(8, 100) / 100f) * maxBarHeight
                    val barColor = when {
                        index == barCount - 1 -> IOSHealthCoral
                        rate >= 90 -> IOSMintGreen
                        else -> IOSWarmOrange
                    }
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, maxBarHeight - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                days.forEachIndexed { idx, label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (idx == days.lastIndex) IOSHealthCoral else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (idx == days.lastIndex) FontWeight.Bold else FontWeight.Normal
                    )
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
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = { onRestock(14) },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
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
                    color = IOSHealthCoral.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
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
                    color = CrimsonDanger.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
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
