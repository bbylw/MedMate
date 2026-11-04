package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DietPlanProfile
import com.example.data.EvidenceDietCatalog
import com.example.data.ExercisePrescription
import com.example.data.ExerciseWorkoutItem
import com.example.data.FoodTrafficItem
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.IOSHealthCoral
import com.example.ui.theme.IOSMintGreen
import com.example.ui.theme.IOSPlumPurple
import com.example.ui.theme.IOSWarmOrange
import com.example.ui.theme.JetBrainsMonoFontFamily
import java.time.LocalDate

@Composable
fun EvidenceDietScreen(
    currentDateIso: String = LocalDate.now().toString()
) {
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences("medmate_prefs", Context.MODE_PRIVATE)
    }
    val profiles = EvidenceDietCatalog.dietProfiles
    var selectedProfileIndex by remember { mutableIntStateOf(0) }
    var selectedFoodTrafficTab by remember { mutableIntStateOf(0) } // 0: 绿灯推荐, 1: 黄灯限量, 2: 红灯禁忌

    val habitPrefsKey = "diet_habits_$currentDateIso"
    val checkedHabits = remember(currentDateIso) {
        val savedSet = prefs.getStringSet(habitPrefsKey, emptySet()) ?: emptySet()
        mutableStateMapOf<String, Boolean>().apply {
            savedSet.forEach { put(it, true) }
        }
    }

    val saveCheckedHabits: () -> Unit = {
        val activeKeys = checkedHabits.filterValues { it }.keys.toSet()
        prefs.edit().putStringSet(habitPrefsKey, activeKeys).apply()
    }

    val currentProfile = profiles[selectedProfileIndex]
    val accentColor = Color(currentProfile.accentColorHex)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // iOS Large Title Header
        item {
            Column {
                Text(
                    text = "CLINICAL NUTRITION & EXERCISE · $currentDateIso",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "饮食与运动处方",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 32.sp,
                        letterSpacing = (-0.8).sp
                    ),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Population Selector Pills (高血压 / 糖尿病 / 普通人群 / 高血脂 / 痛风)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                profiles.forEachIndexed { index, profile ->
                    val selected = selectedProfileIndex == index
                    val pillColor = Color(profile.accentColorHex)
                    Surface(
                        onClick = { selectedProfileIndex = index },
                        color = if (selected) pillColor else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("diet_profile_tab_${profile.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) Color.White else pillColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = profile.shortTabName,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Apple Health Style Diet & Exercise Overview Card
        item {
            DietHeroSummaryCard(profile = currentProfile, accentColor = accentColor)
        }

        // 3 Core Quantified Daily Targets (Diet + Exercise)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                currentProfile.coreMetrics.forEach { metric ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = metric.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = metric.value,
                                style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = metric.unitNote,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Red-Yellow-Green Food Traffic Light Segmented Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "食材红绿灯量化速查",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 12.dp)
                )

                IOSSegmentedControl(
                    options = listOf(
                        "绿灯优选 (${currentProfile.greenFoods.size})",
                        "黄灯限量 (${currentProfile.yellowFoods.size})",
                        "红灯禁忌 (${currentProfile.redFoods.size})"
                    ),
                    selectedIndex = selectedFoodTrafficTab,
                    onSelect = { selectedFoodTrafficTab = it },
                    tagPrefix = "diet_traffic_tab"
                )

                val activeList = when (selectedFoodTrafficTab) {
                    0 -> currentProfile.greenFoods
                    1 -> currentProfile.yellowFoods
                    else -> currentProfile.redFoods
                }
                val trafficColor = when (selectedFoodTrafficTab) {
                    0 -> IOSMintGreen
                    1 -> IOSWarmOrange
                    else -> CrimsonDanger
                }

                // iOS Inset Grouped Food List Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        activeList.forEachIndexed { idx, food ->
                            FoodTrafficIOSRow(
                                item = food,
                                badgeColor = trafficColor,
                                showDivider = idx < activeList.lastIndex
                            )
                        }
                    }
                }
            }
        }

        // NEW: Evidence-Based Exercise Prescription Section (循证运动处方与禁忌)
        item {
            ExercisePrescriptionSection(
                exercise = currentProfile.exercisePrescription,
                accentColor = accentColor
            )
        }

        // Drug-Food Interaction Warning Card
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(IOSWarmOrange.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WarningAmber,
                                contentDescription = null,
                                tint = IOSWarmOrange,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "服药与饮食相互作用警示",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentProfile.drugFoodWarning,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Myth Buster Card (破除食疗与运动伪科学)
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CrimsonDanger.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.GppBad,
                                contentDescription = null,
                                tint = CrimsonDanger,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = currentProfile.mythDebunkTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonDanger
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentProfile.mythDebunkContent,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 19.sp
                    )
                }
            }
        }

        // Interactive Daily Diet & Exercise Habit Checklist (iOS Reminders Style, Daily Auto-Reset)
        item {
            val doneCount = currentProfile.dailyChecklist.indices.count { idx ->
                checkedHabits["${currentProfile.id}_$idx"] == true
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "今日循证饮食与运动习惯自查 ($doneCount/${currentProfile.dailyChecklist.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (doneCount > 0) {
                        Text(
                            text = "重置今日自查",
                            style = MaterialTheme.typography.labelSmall,
                            color = IOSHealthCoral,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                currentProfile.dailyChecklist.indices.forEach { idx ->
                                    checkedHabits.remove("${currentProfile.id}_$idx")
                                }
                                saveCheckedHabits()
                            }
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        currentProfile.dailyChecklist.forEachIndexed { idx, habit ->
                            val key = "${currentProfile.id}_$idx"
                            val isChecked = checkedHabits[key] == true
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        checkedHabits[key] = !isChecked
                                        saveCheckedHabits()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 13.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (isChecked) IOSMintGreen else Color.Transparent)
                                            .border(
                                                width = if (isChecked) 0.dp else 1.6.dp,
                                                color = if (isChecked) Color.Transparent else MaterialTheme.colorScheme.outline,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isChecked) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = habit,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                        textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                if (idx < currentProfile.dailyChecklist.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 52.dp),
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                                    )
                                }
                            }
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

/**
 * Apple Health / Fitness Style Exercise Prescription Card (FITT-VP + Golden Timing + Contraindications)
 */
@Composable
private fun ExercisePrescriptionSection(
    exercise: ExercisePrescription,
    accentColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "循证运动医学处方 (FITT-VP 方案)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 12.dp)
        )

        // 1. Exercise Overview & Golden Timing Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(IOSMintGreen.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DirectionsRun,
                            contentDescription = null,
                            tint = IOSMintGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = exercise.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = exercise.guidelineSource,
                            style = MaterialTheme.typography.labelSmall,
                            color = IOSMintGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "运动生理获益：${exercise.clinicalBenefit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Rounded.Favorite,
                                contentDescription = null,
                                tint = IOSHealthCoral,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = exercise.targetHeartRateOrIntensity,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = exercise.bestTimingRule,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. Recommended Workouts Inset Grouped List
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                exercise.recommendedWorkouts.forEachIndexed { idx, workout ->
                    WorkoutIOSRow(
                        workout = workout,
                        accentColor = accentColor,
                        showDivider = idx < exercise.recommendedWorkouts.lastIndex
                    )
                }
            }
        }

        // 3. Exercise Safety Contraindications Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = CrimsonDanger.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Rounded.HealthAndSafety,
                    contentDescription = null,
                    tint = CrimsonDanger,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "运动红线与安全禁忌（防意外事件）",
                        style = MaterialTheme.typography.labelLarge,
                        color = CrimsonDanger,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = exercise.contraindications,
                        style = MaterialTheme.typography.bodySmall,
                        color = CrimsonDanger,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WorkoutIOSRow(
    workout: ExerciseWorkoutItem,
    accentColor: Color,
    showDivider: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp)
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.FitnessCenter,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = workout.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                PillMetaBadge(
                    text = workout.fittSpec,
                    containerColor = IOSMintGreen.copy(alpha = 0.12f),
                    contentColor = IOSMintGreen
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = workout.mechanismAndTip,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
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

@Composable
private fun DietHeroSummaryCard(
    profile: DietPlanProfile,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Restaurant,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = profile.guidelineSource,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = profile.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "临床获益证据：${profile.clinicalBenefit}",
                style = MaterialTheme.typography.bodyMedium,
                color = IOSMintGreen,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = profile.mealOrderTip,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 19.sp
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FoodTrafficIOSRow(
    item: FoodTrafficItem,
    badgeColor: Color,
    showDivider: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    PillMetaBadge(
                        text = item.portionOrMetric,
                        containerColor = badgeColor.copy(alpha = 0.12f),
                        contentColor = badgeColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 38.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
            )
        }
    }
}
