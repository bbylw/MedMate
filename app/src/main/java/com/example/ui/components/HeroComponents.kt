package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EvidenceDrugCatalog
import com.example.data.Medication
import com.example.data.normalizeTimeFormat
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.IOSHealthCoral
import com.example.ui.theme.IOSHealthCoralGlow
import com.example.ui.theme.IOSMintGlow
import com.example.ui.theme.IOSMintGreen
import com.example.ui.theme.IOSPlumPurple
import com.example.ui.theme.IOSWarmOrange
import com.example.ui.theme.IOSWarmOrangeGlow
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ObsidianHeroBorder
import com.example.ui.theme.ObsidianHeroElevated
import com.example.ui.theme.ObsidianHeroSurface
import com.example.ui.theme.ObsidianHeroTextPrimary
import com.example.ui.theme.ObsidianHeroTextSecondary
import java.time.LocalTime
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Parses a "HH:mm" time string into minutes since 00:00 (0..1439)
 */
fun timeStringToMinutes(time: String): Int {
    val normalized = normalizeTimeFormat(time)
    val parts = normalized.split(":")
    val h = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 8
    val m = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
    return h * 60 + m
}

fun formatDurationMinutes(minutes: Int): String {
    val safe = abs(minutes)
    val h = safe / 60
    val m = safe % 60
    return when {
        h > 0 && m > 0 -> "${h}小时${m}分钟"
        h > 0 -> "${h}小时"
        m > 0 -> "${m}分钟"
        else -> "即将到点"
    }
}

/**
 * Editorial Tactile Pill Segmented Control with smooth animated transitions
 */
@Composable
fun IOSSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    tagPrefix: String = "ios_segment"
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.85f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.5.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            options.forEachIndexed { index, label ->
                val selected = selectedIndex == index
                val bgColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    animationSpec = tween(200),
                    label = "seg_bg_$index"
                )
                val textColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(200),
                    label = "seg_text_$index"
                )
                Surface(
                    onClick = { onSelect(index) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("${tagPrefix}_$index"),
                    shape = RoundedCornerShape(10.5.dp),
                    color = bgColor,
                    border = if (selected) {
                        BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
                    } else null,
                    shadowElevation = if (selected) 2.5.dp else 0.dp
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor,
                        modifier = Modifier.padding(vertical = 8.5.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Flagship Obsidian Precision Health Instrument Card
 * Deep sculpted obsidian centerpiece with studio aura lighting & 24-tick chronograph concentric rings
 */
@Composable
fun EvidenceHeroBanner(
    medications: List<Medication>,
    currentMinutesOfDay: Int = LocalTime.now().let { it.hour * 60 + it.minute },
    onShowEbmManifesto: () -> Unit,
    onMarkAllTaken: () -> Unit,
    onResetToday: () -> Unit = {}
) {
    val totalCount = medications.size
    val takenCount = medications.count { it.isTakenToday }
    val progress = if (totalCount > 0) takenCount.toFloat() / totalCount.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "ios_ring_progress"
    )
    val pendingList = remember(medications) { medications.filter { !it.isTakenToday } }
    val overduePending = remember(pendingList, currentMinutesOfDay) {
        pendingList.filter { timeStringToMinutes(it.time) < currentMinutesOfDay }
    }
    val nextPending = overduePending.firstOrNull() ?: pendingList.firstOrNull()
    val maxStreak = medications.maxOfOrNull { it.streakDays } ?: 0
    val streakProgress = (maxStreak.coerceAtMost(30) / 30f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("evidence_hero_card"),
        shape = RoundedCornerShape(26.dp),
        color = ObsidianHeroSurface,
        border = BorderStroke(0.85.dp, ObsidianHeroBorder),
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    // Studio Mesh Lighting: Carmine Rose Aura top-right + Botanical Jade Aura bottom-left
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(IOSHealthCoral.copy(alpha = 0.24f), Color.Transparent),
                            center = Offset(size.width * 0.88f, size.height * 0.20f),
                            radius = size.width * 0.62f
                        ),
                        center = Offset(size.width * 0.88f, size.height * 0.20f),
                        radius = size.width * 0.62f
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(IOSMintGreen.copy(alpha = 0.16f), Color.Transparent),
                            center = Offset(size.width * 0.08f, size.height * 0.90f),
                            radius = size.width * 0.52f
                        ),
                        center = Offset(size.width * 0.08f, size.height * 0.90f),
                        radius = size.width * 0.52f
                    )
                }
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            // Top Editorial Eyebrow & Frosted Manifesto Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (totalCount > 0 && takenCount == totalCount) IOSMintGlow else IOSHealthCoralGlow
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "循证用药依从度",
                        style = MaterialTheme.typography.labelLarge,
                        color = ObsidianHeroTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    onClick = onShowEbmManifesto,
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.09f),
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.14f)),
                    modifier = Modifier.testTag("ebm_manifesto_badge")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 10.dp, end = 6.dp, top = 4.5.dp, bottom = 4.5.dp)
                    ) {
                        Text(
                            text = "拥抱循证，拒绝中医",
                            style = MaterialTheme.typography.labelSmall,
                            color = ObsidianHeroTextPrimary.copy(alpha = 0.88f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.5.sp
                        )
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = ObsidianHeroTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Centerpiece: Left Tabular Counter + Live Status Pill | Right 104dp Chronograph Dual Rings
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$takenCount",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 44.sp,
                                lineHeight = 46.sp
                            ),
                            color = ObsidianHeroTextPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = " / $totalCount 剂",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily
                            ),
                            color = ObsidianHeroTextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (nextPending != null) {
                        val deltaMinutes = timeStringToMinutes(nextPending.time) - currentMinutesOfDay
                        val isOverdue = deltaMinutes < 0
                        val pillAccent = if (isOverdue) Color(0xFFFF6B6B) else IOSWarmOrangeGlow
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = pillAccent.copy(alpha = 0.14f),
                            border = BorderStroke(0.5.dp, pillAccent.copy(alpha = 0.30f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(pillAccent)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isOverdue) {
                                        "已超时 ${formatDurationMinutes(deltaMinutes)} · ${nextPending.name}"
                                    } else if (deltaMinutes <= 15) {
                                        "即将到点 ${nextPending.time} · ${nextPending.name}"
                                    } else {
                                        "${formatDurationMinutes(deltaMinutes)}后 ${nextPending.time} · ${nextPending.name}"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isOverdue) Color(0xFFFFB4B4) else Color(0xFFFFDFA6),
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else if (totalCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = IOSMintGreen.copy(alpha = 0.18f),
                            border = BorderStroke(0.5.dp, IOSMintGlow.copy(alpha = 0.32f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Verified,
                                    contentDescription = null,
                                    tint = IOSMintGlow,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "今日计划已全部准时完成",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFFA7F3D0),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "点击右上角 + 添加您的科学用药计划",
                            style = MaterialTheme.typography.bodySmall,
                            color = ObsidianHeroTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(7.dp))
                    Text(
                        text = if (overduePending.size > 1) {
                            "当前有 ${overduePending.size} 项已过计划时间待补服"
                        } else {
                            "连续达标 ${maxStreak} 天 · 零不明草药负担"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (overduePending.size > 1) Color(0xFFFF8A8A) else ObsidianHeroTextSecondary,
                        fontSize = 11.5.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // 106dp Precision Chronograph Dual Concentric Ring Instrument
                Box(
                    modifier = Modifier.size(106.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val tickOuterRadius = size.minDimension / 2f
                        val tickInnerRadius = tickOuterRadius - 3.dp.toPx()

                        // 24 Swiss Chronograph Precision Dial Micro-Ticks
                        for (i in 0 until 24) {
                            val angleRad = (i * 15.0 - 90.0) * (PI / 180.0)
                            val isMajor = i % 6 == 0
                            val inner = if (isMajor) tickInnerRadius - 1.5.dp.toPx() else tickInnerRadius
                            drawLine(
                                color = Color.White.copy(alpha = if (isMajor) 0.34f else 0.14f),
                                start = Offset(
                                    cx + (inner * cos(angleRad)).toFloat(),
                                    cy + (inner * sin(angleRad)).toFloat()
                                ),
                                end = Offset(
                                    cx + (tickOuterRadius * cos(angleRad)).toFloat(),
                                    cy + (tickOuterRadius * sin(angleRad)).toFloat()
                                ),
                                strokeWidth = if (isMajor) 1.4.dp.toPx() else 0.9.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }

                        val outerInset = 7.dp.toPx()
                        val outerStroke = 9.dp.toPx()
                        val innerStroke = 7.5.dp.toPx()
                        val gap = 3.5.dp.toPx()

                        // Outer Ring: Daily Adherence (Carmine Rose Luminous Sweep)
                        val outerDia = size.minDimension - (outerInset + outerStroke / 2f) * 2f
                        val outerTopLeft = Offset(outerInset + outerStroke / 2f, outerInset + outerStroke / 2f)
                        drawArc(
                            color = Color.White.copy(alpha = 0.08f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = outerTopLeft,
                            size = Size(outerDia, outerDia),
                            style = Stroke(width = outerStroke, cap = StrokeCap.Round)
                        )
                        // Subtle outer glow halo behind active sweep
                        drawArc(
                            color = IOSHealthCoral.copy(alpha = 0.28f),
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress.coerceAtLeast(0.04f),
                            useCenter = false,
                            topLeft = outerTopLeft,
                            size = Size(outerDia, outerDia),
                            style = Stroke(width = outerStroke + 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(IOSHealthCoral, IOSHealthCoralGlow, IOSHealthCoral)
                            ),
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress.coerceAtLeast(0.04f),
                            useCenter = false,
                            topLeft = outerTopLeft,
                            size = Size(outerDia, outerDia),
                            style = Stroke(width = outerStroke, cap = StrokeCap.Round)
                        )

                        // Inner Ring: Streak Consistency (Botanical Jade Luminous Sweep)
                        val innerInset = outerInset + outerStroke + gap + innerStroke / 2f
                        val innerDia = size.minDimension - innerInset * 2f
                        val innerTopLeft = Offset(innerInset, innerInset)
                        drawArc(
                            color = Color.White.copy(alpha = 0.07f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = innerTopLeft,
                            size = Size(innerDia, innerDia),
                            style = Stroke(width = innerStroke, cap = StrokeCap.Round)
                        )
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(IOSMintGreen, IOSMintGlow, IOSMintGreen)
                            ),
                            startAngle = -90f,
                            sweepAngle = 360f * streakProgress.coerceAtLeast(0.08f),
                            useCenter = false,
                            topLeft = innerTopLeft,
                            size = Size(innerDia, innerDia),
                            style = Stroke(width = innerStroke, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(animatedProgress * 100).roundToInt()}%",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            ),
                            color = ObsidianHeroTextPrimary
                        )
                        Text(
                            text = "达标率",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.5.sp,
                            color = ObsidianHeroTextSecondary
                        )
                    }
                }
            }

            if (totalCount > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                // Translucent Obsidian Bottom Action Strip
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ObsidianHeroElevated.copy(alpha = 0.82f),
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Autorenew,
                                contentDescription = null,
                                tint = IOSMintGlow,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (takenCount < totalCount) {
                                    "00:00自动刷新 · 待服 ${totalCount - takenCount} 剂"
                                } else {
                                    "00:00自动刷新 · 明日归零"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = ObsidianHeroTextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (takenCount > 0) {
                                Surface(
                                    onClick = onResetToday,
                                    shape = RoundedCornerShape(50),
                                    color = Color.White.copy(alpha = 0.08f),
                                    modifier = Modifier.testTag("hero_reset_today_button")
                                ) {
                                    Text(
                                        text = "重置今日",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ObsidianHeroTextPrimary.copy(alpha = 0.85f),
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                            if (takenCount < totalCount) {
                                Surface(
                                    onClick = onMarkAllTaken,
                                    shape = RoundedCornerShape(50),
                                    color = Color.Transparent,
                                    modifier = Modifier.testTag("quick_take_all_button")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                Brush.linearGradient(listOf(IOSHealthCoral, IOSHealthCoralGlow)),
                                                shape = RoundedCornerShape(50)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 5.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "一键全服",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Authentic iOS Inset Grouped Row inside a time-period section
 */
@Composable
fun IOSMedicationRow(
    medication: Medication,
    showBottomDivider: Boolean,
    onToggleTaken: () -> Unit,
    onRowClick: () -> Unit,
    onDelete: () -> Unit,
    isManageMode: Boolean = false,
    currentMinutesOfDay: Int = LocalTime.now().let { it.hour * 60 + it.minute }
) {
    val accentColor = Color(medication.color)
    val isLowStock = medication.stockCount <= 7
    val isOverdue = !medication.isTakenToday && timeStringToMinutes(medication.time) < currentMinutesOfDay
    val checkScale by animateFloatAsState(
        targetValue = if (medication.isTakenToday) 1.06f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "check_scale"
    )

    val rowBg by animateColorAsState(
        targetValue = if (medication.isTakenToday) IOSMintGreen.copy(alpha = 0.055f)
        else MaterialTheme.colorScheme.surface,
        animationSpec = tween(240),
        label = "row_bg"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .clickable { onRowClick() }
            .testTag("med_card_${medication.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isManageMode) {
                // iOS Native TableView Edit Mode Minus Circle Button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(CrimsonDanger)
                        .clickable { onDelete() }
                        .testTag("manage_remove_${medication.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Remove,
                        contentDescription = "移除${medication.name}",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
            }

            // Editorial Soft-Gradient Squircle Pill Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        Brush.linearGradient(
                            colors = if (medication.isTakenToday) {
                                listOf(IOSMintGreen.copy(alpha = 0.18f), IOSMintGlow.copy(alpha = 0.08f))
                            } else {
                                listOf(accentColor.copy(alpha = 0.16f), accentColor.copy(alpha = 0.06f))
                            }
                        )
                    )
                    .border(
                        width = 0.75.dp,
                        color = if (medication.isTakenToday) IOSMintGreen.copy(alpha = 0.28f) else accentColor.copy(alpha = 0.24f),
                        shape = RoundedCornerShape(13.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconForForm(medication.form),
                    contentDescription = medication.form,
                    tint = if (medication.isTakenToday) IOSMintGreen else accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Center Editorial Cell Labels
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = medication.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (medication.isTakenToday) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        textDecoration = if (medication.isTakenToday) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val timePillColor = when {
                        medication.isTakenToday -> IOSMintGreen
                        isOverdue -> CrimsonDanger
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when {
                            medication.isTakenToday -> IOSMintGreen.copy(alpha = 0.11f)
                            isOverdue -> CrimsonDanger.copy(alpha = 0.11f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                        }
                    ) {
                        Text(
                            text = if (medication.isTakenToday) {
                                val timeSuffix = medication.lastTakenTime.takeIf { it.isNotBlank() }?.let { " $it" } ?: ""
                                "今日${timeSuffix}已服"
                            } else if (isOverdue) {
                                "${medication.time} 超时"
                            } else {
                                medication.time
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = timePillColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(5.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Hairline-Bordered Tappable Dosage Badge
                    Surface(
                        onClick = onRowClick,
                        color = IOSHealthCoral.copy(alpha = 0.08f),
                        border = BorderStroke(0.5.dp, IOSHealthCoral.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("dosage_badge_${medication.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = medication.dosage,
                                style = MaterialTheme.typography.labelSmall,
                                color = IOSHealthCoral,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "修改剂量",
                                tint = IOSHealthCoral,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }

                    Text(
                        text = "${medication.form} · ${medication.mealInstruction}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = medication.evidenceLevel,
                        style = MaterialTheme.typography.labelSmall,
                        color = IOSMintGreen,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isLowStock) {
                        Text(
                            text = "仅剩 ${medication.stockCount}${unitForForm(medication.form)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = IOSWarmOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isManageMode) {
                Surface(
                    onClick = onDelete,
                    color = CrimsonDanger.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("delete_pill_${medication.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "移除",
                            tint = CrimsonDanger,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "移除",
                            style = MaterialTheme.typography.labelMedium,
                            color = CrimsonDanger,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // Direct Quick-Remove Icon Button on Row
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("delete_med_${medication.id}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "从计划中移除${medication.name}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        modifier = Modifier.size(19.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Spring-animated Circular Check Button with 48dp accessible hit area
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable { onToggleTaken() }
                        .testTag("toggle_med_${medication.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .graphicsLayer {
                                scaleX = checkScale
                                scaleY = checkScale
                            }
                            .clip(CircleShape)
                            .background(
                                if (medication.isTakenToday) {
                                    Brush.linearGradient(listOf(IOSMintGreen, IOSMintGlow))
                                } else {
                                    Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                }
                            )
                            .border(
                                width = if (medication.isTakenToday) 0.dp else 2.dp,
                                color = if (medication.isTakenToday) Color.Transparent else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(targetState = medication.isTakenToday, label = "ios_check") { taken ->
                            if (taken) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "已服用",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Spacer(modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        if (showBottomDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 66.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
            )
        }
    }
}

@Composable
fun PillMetaBadge(
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            fontSize = 11.sp
        )
    }
}

@Composable
fun MedicationDetailSheetContent(
    medication: Medication,
    onToggleTaken: () -> Unit,
    onUpdateDosage: (String) -> Unit = {},
    onRestock: (Int) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val accentColor = Color(medication.color)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // iOS Sheet Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onDismiss) {
                Text("完成", color = IOSHealthCoral, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
            Text(
                text = "药物详情与剂量调节",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("sheet_top_delete_button")
                ) {
                    Text("移除", color = CrimsonDanger, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                TextButton(onClick = onEdit) {
                    Text("编辑", color = IOSHealthCoral, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }
        }

        // Top Drug Identity Card (iOS Inset Grouped Card)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconForForm(medication.form),
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medication.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (medication.genericName.isNotBlank()) {
                        Text(
                            text = medication.genericName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = medication.evidenceLevel,
                        style = MaterialTheme.typography.labelSmall,
                        color = IOSMintGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Interactive Dosage Adjuster Card right inside Detail Sheet!
        val baseReferenceDosage = remember(medication.id, medication.name) {
            EvidenceDrugCatalog.catalogDrugs.find { it.name == medication.name }?.defaultDosage
                ?: medication.dosage
        }
        IOSDosageAdjusterCard(
            currentDosage = medication.dosage,
            form = medication.form,
            baseReferenceDosage = baseReferenceDosage,
            onDosageChange = onUpdateDosage
        )

        // Primary iOS Action Buttons Row (Mark Taken + Remove from Plan)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    onToggleTaken()
                    onDismiss()
                },
                modifier = Modifier
                    .weight(1.3f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (medication.isTakenToday) IOSPlumPurple else IOSMintGreen
                )
            ) {
                Icon(
                    imageVector = if (medication.isTakenToday) Icons.AutoMirrored.Rounded.Undo else Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (medication.isTakenToday) "改为未服用" else "标记已服用",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Surface(
                onClick = onDelete,
                color = CrimsonDanger.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("sheet_quick_remove_button")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = null,
                        tint = CrimsonDanger,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "移出计划",
                        color = CrimsonDanger,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // Editorial Grouped Key-Value Rows
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.85f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                IOSKeyValueRow(label = "提醒时间", value = "${medication.period} ${medication.time}", showDivider = true)
                IOSKeyValueRow(
                    label = "当前单次剂量",
                    value = "${medication.dosage} (${medication.form})",
                    valueColor = IOSHealthCoral,
                    showDivider = true
                )
                IOSKeyValueRow(label = "饮食要求", value = medication.mealInstruction, showDivider = true)
                IOSKeyValueRow(
                    label = "药箱余量",
                    value = "${medication.stockCount} ${unitForForm(medication.form)}",
                    valueColor = if (medication.stockCount <= 7) IOSWarmOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                    showDivider = false
                )
            }
        }

        // Clinical Note Grouped Card
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.85f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "循证药理与禁忌说明",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = medication.clinicalNote,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { onRestock(14) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("补充 +14${unitForForm(medication.form)}", fontSize = 13.sp)
                    }
                    FilledTonalButton(
                        onClick = { onRestock(28) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("补充 +28${unitForForm(medication.form)}", fontSize = 13.sp)
                    }
                }
            }
        }

        Surface(
            onClick = onDelete,
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sheet_bottom_delete_button")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteForever,
                    contentDescription = null,
                    tint = CrimsonDanger,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "从今日用药计划中彻底移除",
                    color = CrimsonDanger,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Reusable Interactive iOS Health Dosage Stepper + Smart Presets + Custom Input Card
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IOSDosageAdjusterCard(
    currentDosage: String,
    form: String,
    baseReferenceDosage: String = currentDosage,
    onDosageChange: (String) -> Unit
) {
    var customInput by remember(currentDosage) { mutableStateOf(currentDosage) }
    val presets = remember(baseReferenceDosage, form) {
        buildSmartDosagePresets(baseReferenceDosage, form)
    }
    val formUnit = unitForForm(form)

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = null,
                        tint = IOSHealthCoral,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "单次服药剂量调节",
                        style = MaterialTheme.typography.labelLarge,
                        color = IOSHealthCoral,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "可加减半${formUnit}/整${formUnit}或自定义输入",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            // 1. Stepper Row: [-] Current Dosage [+]
            Surface(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = {
                            val next = stepDosageString(currentDosage, -0.5f, form)
                            customInput = next
                            onDosageChange(next)
                        },
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(9.dp),
                        shadowElevation = 0.5.dp,
                        modifier = Modifier.testTag("dosage_minus_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Remove,
                                contentDescription = "减少半剂",
                                tint = IOSHealthCoral,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "减半$formUnit",
                                style = MaterialTheme.typography.labelMedium,
                                color = IOSHealthCoral,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentDosage,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 17.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "当前设定剂量",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }

                    Surface(
                        onClick = {
                            val next = stepDosageString(currentDosage, +0.5f, form)
                            customInput = next
                            onDosageChange(next)
                        },
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(9.dp),
                        shadowElevation = 0.5.dp,
                        modifier = Modifier.testTag("dosage_plus_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "增加半剂",
                                tint = IOSHealthCoral,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "加半$formUnit",
                                style = MaterialTheme.typography.labelMedium,
                                color = IOSHealthCoral,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 2. Smart Quick-Select Dosage Chips (Wrapping FlowRow)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presets.forEach { preset ->
                    val isSelected = currentDosage == preset
                    Surface(
                        onClick = {
                            customInput = preset
                            onDosageChange(preset)
                        },
                        color = if (isSelected) IOSHealthCoral else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = preset,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // 3. Free-Form Custom Dosage Input Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    if (customInput.isEmpty()) {
                        Text(
                            text = "自定义剂量（如：10mg、半片、2粒、15单位）",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    BasicTextField(
                        value = customInput,
                        onValueChange = {
                            customInput = it
                            if (it.isNotBlank()) {
                                onDosageChange(it)
                            }
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        ),
                        cursorBrush = SolidColor(IOSHealthCoral),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_dosage_input")
                    )
                }

                if (customInput.isNotBlank() && customInput != currentDosage) {
                    Surface(
                        onClick = { onDosageChange(customInput.trim()) },
                        color = IOSHealthCoral,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "应用",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Generates smart clinical dosage options scaled around the drug's reference dosage
 */
fun buildSmartDosagePresets(baseDosage: String, form: String): List<String> {
    val unit = unitForForm(form)
    val result = mutableListOf<String>()
    val trimmed = baseDosage.trim()

    // Pattern 1: Single strength with count e.g. "20mg (1片)", "5 mg • 1片", "0.5mg (1片)", "1000IU (1粒)"
    val singleRegex = Regex(
        """^(\d+(?:\.\d+)?)\s*(mg|g|μg|mcg|IU|单位)\s*(?:\(\s*|[•·]\s*|\s+)(半|\d+(?:\.\d+)?)\s*([片粒袋支滴喷次])\s*\)?$""",
        RegexOption.IGNORE_CASE
    )
    val singleMatch = singleRegex.find(trimmed)
    if (singleMatch != null) {
        val baseStrength = singleMatch.groupValues[1].toFloatOrNull() ?: 1f
        val strengthUnit = singleMatch.groupValues[2]
        val countToken = singleMatch.groupValues[3]
        val baseCount = (if (countToken == "半") 0.5f else countToken.toFloatOrNull() ?: 1f).coerceAtLeast(0.5f)
        val pillUnit = singleMatch.groupValues[4]
        val strengthPerOne = baseStrength / baseCount

        val multipliers = listOf(0.5f, 1f, 1.5f, 2f, 3f)
        multipliers.forEach { count ->
            val scaledStrength = formatNumberClean(strengthPerOne * count)
            val countLabel = if (count == 0.5f) "半$pillUnit" else "${formatNumberClean(count)}$pillUnit"
            result.add("$scaledStrength $strengthUnit ($countLabel)")
        }
        return result.distinct()
    }

    // Pattern 2: Plain strength without pill count e.g. "20mg" or "10 mg"
    val plainStrengthRegex = Regex("""^(\d+(?:\.\d+)?)\s*(mg|g|μg|mcg|IU|单位)$""", RegexOption.IGNORE_CASE)
    val plainMatch = plainStrengthRegex.find(trimmed)
    if (plainMatch != null) {
        val strengthPerOne = plainMatch.groupValues[1].toFloatOrNull() ?: 1f
        val strengthUnit = plainMatch.groupValues[2]
        listOf(0.5f, 1f, 1.5f, 2f, 3f).forEach { count ->
            val scaledStrength = formatNumberClean(strengthPerOne * count)
            val countLabel = if (count == 0.5f) "半$unit" else "${formatNumberClean(count)}$unit"
            result.add("$scaledStrength $strengthUnit ($countLabel)")
        }
        return result.distinct()
    }

    // Pattern 3: Fixed-dose combination e.g. "80mg/5mg (1片)" or "80mg/5mg • 1片"
    val comboRegex = Regex("""^(.+?)\s*(?:\(\s*|[•·]\s*)(半|\d+(?:\.\d+)?)\s*([片粒袋支滴喷次])\s*\)?$""")
    val comboMatch = comboRegex.find(trimmed)
    if (comboMatch != null) {
        val prefix = comboMatch.groupValues[1].trim()
        val pillUnit = comboMatch.groupValues[3]
        result.add("$prefix (半$pillUnit)")
        result.add("$prefix (1$pillUnit)")
        result.add("$prefix (1.5$pillUnit)")
        result.add("$prefix (2$pillUnit)")
        return result.distinct()
    }

    // Fallback generic presets
    if (trimmed.isNotBlank()) {
        result.add(trimmed)
    }
    result.addAll(
        listOf(
            "半$unit (0.5$unit)",
            "1$unit",
            "1.5$unit",
            "2$unit",
            "5 mg (1$unit)",
            "10 mg (1$unit)",
            "20 mg (1$unit)",
            "50 mg (1$unit)",
            "100 mg (1$unit)",
            "500 mg (1$unit)"
        )
    )
    return result.distinct().take(6)
}

/**
 * Steps a dosage string up or down by deltaCount (e.g. -0.5f or +0.5f tablets)
 */
fun stepDosageString(currentDosage: String, deltaCount: Float, form: String): String {
    val defaultUnit = unitForForm(form)
    val trimmed = currentDosage.trim()

    // 1. Check "20mg (1片)", "10mg (半片)", "5 mg • 1片", etc.
    val singleRegex = Regex(
        """^(\d+(?:\.\d+)?)\s*(mg|g|μg|mcg|IU|单位)\s*(?:\(\s*|[•·]\s*|\s+)(半|\d+(?:\.\d+)?)\s*([片粒袋支滴喷次])\s*\)?$""",
        RegexOption.IGNORE_CASE
    )
    val singleMatch = singleRegex.find(trimmed)
    if (singleMatch != null) {
        val curStrength = singleMatch.groupValues[1].toFloatOrNull() ?: 1f
        val strengthUnit = singleMatch.groupValues[2]
        val countToken = singleMatch.groupValues[3]
        val curCount = if (countToken == "半") 0.5f else (countToken.toFloatOrNull() ?: 1f)
        val pillUnit = singleMatch.groupValues[4]

        val strengthPerOne = if (curCount > 0f) curStrength / curCount else curStrength
        val nextCount = (curCount + deltaCount).coerceIn(0.5f, 10f)
        val nextStrength = formatNumberClean(strengthPerOne * nextCount)
        val countLabel = if (nextCount == 0.5f) "半$pillUnit" else "${formatNumberClean(nextCount)}$pillUnit"
        return "$nextStrength $strengthUnit ($countLabel)"
    }

    // 2. Check plain strength "20mg" or "10 mg"
    val plainStrengthRegex = Regex("""^(\d+(?:\.\d+)?)\s*(mg|g|μg|mcg|IU|单位)$""", RegexOption.IGNORE_CASE)
    val plainMatch = plainStrengthRegex.find(trimmed)
    if (plainMatch != null) {
        val curStrength = plainMatch.groupValues[1].toFloatOrNull() ?: 1f
        val strengthUnit = plainMatch.groupValues[2]
        val nextCount = (1f + deltaCount).coerceIn(0.5f, 10f)
        val nextStrength = formatNumberClean(curStrength * nextCount)
        val countLabel = if (nextCount == 0.5f) "半$defaultUnit" else "${formatNumberClean(nextCount)}$defaultUnit"
        return "$nextStrength $strengthUnit ($countLabel)"
    }

    // 3. Check compound "80mg/5mg (1片)" or "80mg/5mg • 1片"
    val comboRegex = Regex("""^(.+?)\s*(?:\(\s*|[•·]\s*)(半|\d+(?:\.\d+)?)\s*([片粒袋支滴喷次])\s*\)?$""")
    val comboMatch = comboRegex.find(trimmed)
    if (comboMatch != null) {
        val prefix = comboMatch.groupValues[1].trim()
        val countToken = comboMatch.groupValues[2]
        val curCount = if (countToken == "半") 0.5f else (countToken.toFloatOrNull() ?: 1f)
        val pillUnit = comboMatch.groupValues[3]
        val nextCount = (curCount + deltaCount).coerceIn(0.5f, 10f)
        val countLabel = if (nextCount == 0.5f) "半$pillUnit" else "${formatNumberClean(nextCount)}$pillUnit"
        return "$prefix ($countLabel)"
    }

    // 4. Check simple count "1片", "半片", "1.5粒" or "半片 (0.5片)"
    val simpleRegex = Regex("""^(半|\d+(?:\.\d+)?)\s*([片粒袋支滴喷次]).*$""")
    val simpleMatch = simpleRegex.find(trimmed)
    if (simpleMatch != null) {
        val countToken = simpleMatch.groupValues[1]
        val curCount = if (countToken == "半") 0.5f else (countToken.toFloatOrNull() ?: 1f)
        val pillUnit = simpleMatch.groupValues[2]
        val nextCount = (curCount + deltaCount).coerceIn(0.5f, 10f)
        return if (nextCount == 0.5f) "半$pillUnit" else "${formatNumberClean(nextCount)}$pillUnit"
    }

    // 5. Fallback
    return if (deltaCount < 0) "半$defaultUnit" else "1.5$defaultUnit"
}

private fun formatNumberClean(value: Float): String {
    val rounded = (value * 100f).roundToInt() / 100f
    return if (rounded == rounded.toInt().toFloat()) {
        rounded.toInt().toString()
    } else {
        rounded.toString().trimEnd('0').trimEnd('.')
    }
}

@Composable
fun IOSKeyValueRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    showDivider: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = valueColor,
                fontWeight = FontWeight.Medium
            )
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

fun iconForForm(form: String): ImageVector = when (form) {
    "胶囊" -> Icons.Rounded.Medication
    "缓释片" -> Icons.Rounded.Timelapse
    "滴剂" -> Icons.Rounded.WaterDrop
    "喷雾" -> Icons.Rounded.Air
    "注射剂" -> Icons.Rounded.Vaccines
    else -> Icons.Rounded.Healing
}

fun unitForForm(form: String): String = when (form) {
    "胶囊" -> "粒"
    "滴剂", "喷雾", "注射剂" -> "次"
    else -> "片"
}
