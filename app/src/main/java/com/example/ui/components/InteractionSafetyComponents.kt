package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.GppMaybe
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DrugInteractionAlert
import com.example.data.InteractionSeverity
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.IOSMintGreen
import com.example.ui.theme.IOSWarmOrange

/**
 * Compact expandable Apple Health style Clinical Drug-Drug Interaction banner.
 * Appears on TodayRegimenScreen only when conflicts or duplicate therapies are detected.
 */
@Composable
fun RegimenInteractionAlertBanner(
    alerts: List<DrugInteractionAlert>,
    modifier: Modifier = Modifier
) {
    if (alerts.isEmpty()) return

    val hasHighRisk = alerts.any { it.severity == InteractionSeverity.HIGH }
    val accentColor = if (hasHighRisk) CrimsonDanger else IOSWarmOrange
    var expanded by remember(alerts) { mutableStateOf(hasHighRisk) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("interaction_alert_banner"),
        shape = RoundedCornerShape(18.dp),
        color = accentColor.copy(alpha = 0.08f),
        border = BorderStroke(0.75.dp, accentColor.copy(alpha = 0.24f)),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        accentColor.copy(alpha = 0.22f),
                                        accentColor.copy(alpha = 0.10f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (hasHighRisk) Icons.Rounded.GppMaybe else Icons.Rounded.WarningAmber,
                            contentDescription = "循证用药安全警示",
                            tint = accentColor,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (hasHighRisk) {
                                "循证安全警示 · 发现 ${alerts.size} 项用药冲突"
                            } else {
                                "联用注意事项 · ${alerts.size} 项临床监测提示"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = accentColor,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = alerts.first().title,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "收起详情" else "展开详情",
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    alerts.forEach { alert ->
                        InteractionAlertItemCard(alert = alert)
                    }
                }
            }
        }
    }
}

/**
 * Full Clinical Safety Screening Summary Card displayed in StatsAndStockScreen.
 */
@Composable
fun ClinicalSafetySummaryCard(
    medicationCount: Int,
    alerts: List<DrugInteractionAlert>,
    modifier: Modifier = Modifier
) {
    val isSafe = alerts.isEmpty()
    val hasHighRisk = alerts.any { it.severity == InteractionSeverity.HIGH }
    val statusColor = when {
        isSafe -> IOSMintGreen
        hasHighRisk -> CrimsonDanger
        else -> IOSWarmOrange
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("clinical_safety_summary_card"),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            0.75.dp,
            if (isSafe) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
            else statusColor.copy(alpha = 0.28f)
        ),
        shadowElevation = 1.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        statusColor.copy(alpha = 0.22f),
                                        statusColor.copy(alpha = 0.09f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSafe) Icons.Rounded.Shield else Icons.Rounded.GppMaybe,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "循证用药安全筛查",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when {
                                medicationCount < 2 -> "已扫描 $medicationCount 项药物 · 暂无多药联用风险"
                                isSafe -> "已扫描 $medicationCount 项药物 · 未发现相互作用与重复成分"
                                else -> "已扫描 $medicationCount 项药物 · 检出 ${alerts.size} 项需关注风险"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                if (isSafe) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "方案安全",
                        tint = IOSMintGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            if (alerts.isNotEmpty()) {
                alerts.forEach { alert ->
                    InteractionAlertItemCard(alert = alert)
                }
            }
        }
    }
}

@Composable
private fun InteractionAlertItemCard(alert: DrugInteractionAlert) {
    val badgeColor = if (alert.severity == InteractionSeverity.HIGH) CrimsonDanger else IOSWarmOrange
    val badgeText = if (alert.severity == InteractionSeverity.HIGH) "高危冲突" else "联用提示"

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, badgeColor.copy(alpha = 0.22f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PillMetaBadge(
                    text = badgeText,
                    containerColor = badgeColor.copy(alpha = 0.14f),
                    contentColor = badgeColor
                )
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "涉及药物：${alert.involvedDrugs.joinToString(" + ")}",
                style = MaterialTheme.typography.labelMedium,
                color = badgeColor,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "药理机制：${alert.mechanism}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
            Text(
                text = "临床建议：${alert.recommendation}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp
            )
        }
    }
}
