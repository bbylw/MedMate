package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CatalogDrug
import com.example.data.EvidenceDrugCatalog
import com.example.data.EvidencePyramidTier
import com.example.data.ToxicHerbAlert
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.IOSHealthCoral
import com.example.ui.theme.IOSMintGreen
import com.example.ui.theme.IOSPlumPurple
import com.example.ui.theme.IOSWarmOrange
import com.example.ui.theme.JetBrainsMonoFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvidenceLibraryScreen(
    addedDrugNames: Set<String> = emptySet(),
    addedDrugDosages: Map<String, String> = emptyMap(),
    onAddDrugToRegimen: (CatalogDrug, String) -> Unit,
    onRemoveDrugFromRegimen: (CatalogDrug) -> Unit = {}
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("全部") }
    var inspectingCatalogDrug by remember { mutableStateOf<CatalogDrug?>(null) }

    val totalDrugCount = EvidenceDrugCatalog.catalogDrugs.size
    val totalToxicCount = EvidenceDrugCatalog.toxicHerbAlerts.size

    val subTabs = listOf(
        "循证药典 ($totalDrugCount)",
        "毒性警示 ($totalToxicCount)",
        "证据分级"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // iOS Large Title Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = 10.dp)
        ) {
            Text(
                text = "WHO / FDA / COCHRANE 临床指南",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "循证药典",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 32.sp,
                    letterSpacing = (-0.8).sp
                ),
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Authentic UISegmentedControl
            IOSSegmentedControl(
                options = subTabs,
                selectedIndex = selectedSubTab,
                onSelect = { selectedSubTab = it },
                tagPrefix = "library_subtab"
            )
        }

        when (selectedSubTab) {
            0 -> CatalogDrugsSection(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                selectedCategory = selectedCategory,
                onCategoryChange = { selectedCategory = it },
                addedDrugNames = addedDrugNames,
                addedDrugDosages = addedDrugDosages,
                onAddDrug = onAddDrugToRegimen,
                onRemoveDrug = onRemoveDrugFromRegimen,
                onOpenDetail = { inspectingCatalogDrug = it }
            )
            1 -> ToxicHerbAlertsSection()
            2 -> EvidencePyramidSection()
        }
    }

    // Full Clinical Monograph Modal Sheet when user taps a drug card
    val activeDrug = inspectingCatalogDrug
    if (activeDrug != null) {
        val isActiveDrugAdded = activeDrug.name in addedDrugNames
        val currentSavedDosage = addedDrugDosages[activeDrug.name]
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { inspectingCatalogDrug = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            CatalogDrugMonographSheet(
                drug = activeDrug,
                isAdded = isActiveDrugAdded,
                initialDosage = currentSavedDosage ?: activeDrug.defaultDosage,
                onAddWithDosage = { customDosage ->
                    onAddDrugToRegimen(activeDrug, customDosage)
                    inspectingCatalogDrug = null
                },
                onUpdateLiveDosage = { customDosage ->
                    if (isActiveDrugAdded) {
                        onAddDrugToRegimen(activeDrug, customDosage)
                    }
                },
                onRemove = {
                    onRemoveDrugFromRegimen(activeDrug)
                    inspectingCatalogDrug = null
                },
                onDismiss = { inspectingCatalogDrug = null }
            )
        }
    }
}

@Composable
private fun CatalogDrugsSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    addedDrugNames: Set<String>,
    addedDrugDosages: Map<String, String>,
    onAddDrug: (CatalogDrug, String) -> Unit,
    onRemoveDrug: (CatalogDrug) -> Unit,
    onOpenDetail: (CatalogDrug) -> Unit
) {
    val allDrugs = EvidenceDrugCatalog.catalogDrugs
    val filteredDrugs = remember(searchQuery, selectedCategory) {
        allDrugs.filter { drug ->
            val matchesCategory = selectedCategory == "全部" || drug.category == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                drug.name.contains(searchQuery, ignoreCase = true) ||
                drug.genericName.contains(searchQuery, ignoreCase = true) ||
                drug.atcCode.contains(searchQuery, ignoreCase = true) ||
                drug.category.contains(searchQuery, ignoreCase = true) ||
                drug.mechanism.contains(searchQuery, ignoreCase = true) ||
                drug.safetyNote.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Authentic iOS Search Bar (#E5E5EA pill)
        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "搜索 ${allDrugs.size} 种药物、商品名（如倍博特、诺欣妥）...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchChange,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(IOSHealthCoral),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("catalog_search_input")
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Rounded.Cancel,
                            contentDescription = "清除",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { onSearchChange("") }
                        )
                    }
                }
            }
        }

        // Category Pills with Per-Category Counts
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EvidenceDrugCatalog.categories.forEach { cat ->
                    val selected = selectedCategory == cat
                    val count = if (cat == "全部") allDrugs.size else allDrugs.count { it.category == cat }
                    Surface(
                        onClick = { onCategoryChange(cat) },
                        color = if (selected) IOSHealthCoral else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "$cat ($count)",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        if (filteredDrugs.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "未找到匹配的药物",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "可尝试搜索通用名或商品名（如：倍博特、他汀、沙坦、二甲双胍）",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredDrugs, key = { it.id }) { drug ->
                val isAdded = drug.name in addedDrugNames
                val savedDosage = addedDrugDosages[drug.name]
                CatalogDrugIOSCard(
                    drug = drug,
                    isAdded = isAdded,
                    savedDosage = savedDosage,
                    onAddOrUpdateDosage = { customDosage -> onAddDrug(drug, customDosage) },
                    onRemove = { onRemoveDrug(drug) },
                    onOpenDetail = { onOpenDetail(drug) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Optimized iOS Health Drug Card — includes inline dosage adjustment and zero badge squishing!
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CatalogDrugIOSCard(
    drug: CatalogDrug,
    isAdded: Boolean,
    savedDosage: String?,
    onAddOrUpdateDosage: (String) -> Unit,
    onRemove: () -> Unit,
    onOpenDetail: () -> Unit
) {
    var showSafetyInline by remember { mutableStateOf(true) }
    var chosenDosage by remember(drug.id, savedDosage) {
        mutableStateOf(savedDosage ?: drug.defaultDosage)
    }
    val accentColor = Color(drug.colorHex)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onOpenDetail() }
            .testTag("catalog_drug_${drug.id}"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 1. Top Header: Squircle Icon + Full Title/Generic Name + Right Add/Remove Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(44.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(accentColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconForForm(drug.form),
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(23.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = drug.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 17.sp,
                            lineHeight = 22.sp
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = drug.genericName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // iOS App Store Style "+ 加入计划" or "已加入 · 移除" Pill Button
                Surface(
                    onClick = {
                        if (isAdded) {
                            onRemove()
                        } else {
                            onAddOrUpdateDosage(chosenDosage)
                        }
                    },
                    color = if (isAdded) CrimsonDanger.copy(alpha = 0.12f) else IOSHealthCoral.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("add_catalog_${drug.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isAdded) Icons.Rounded.DeleteOutline else Icons.Rounded.Add,
                            contentDescription = null,
                            tint = if (isAdded) CrimsonDanger else IOSHealthCoral,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isAdded) "已加入 · 移除" else "加入计划",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isAdded) CrimsonDanger else IOSHealthCoral,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Wrapping FlowRow for Category, Evidence Level & ATC Code (Never squishes!)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PillMetaBadge(
                    text = drug.category,
                    containerColor = accentColor.copy(alpha = 0.12f),
                    contentColor = accentColor
                )
                PillMetaBadge(
                    text = drug.evidenceLevel,
                    containerColor = IOSMintGreen.copy(alpha = 0.12f),
                    contentColor = IOSMintGreen
                )
                PillMetaBadge(
                    text = "ATC ${drug.atcCode}",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Interactive Clinical Dosage & Timing Strip (Supports direct [-] / [+] adjustment!)
            Surface(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.25f)) {
                            Text(
                                text = if (isAdded) "已设剂量 (可加减)" else "设定单次剂量 (可调)",
                                style = MaterialTheme.typography.labelSmall,
                                color = IOSHealthCoral,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    onClick = {
                                        val next = stepDosageString(chosenDosage, -0.5f, drug.form)
                                        chosenDosage = next
                                        if (isAdded) onAddOrUpdateDosage(next)
                                    },
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = CircleShape,
                                    shadowElevation = 0.5.dp,
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Remove,
                                            contentDescription = "减半剂",
                                            tint = IOSHealthCoral,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = chosenDosage,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.ExtraBold
                                )

                                Surface(
                                    onClick = {
                                        val next = stepDosageString(chosenDosage, +0.5f, drug.form)
                                        chosenDosage = next
                                        if (isAdded) onAddOrUpdateDosage(next)
                                    },
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = CircleShape,
                                    shadowElevation = 0.5.dp,
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = "加半剂",
                                            tint = IOSHealthCoral,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(0.85f)) {
                            Text(
                                text = "推荐时段",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "${drug.recommendedTime} (${drug.form})",
                                style = MaterialTheme.typography.labelLarge,
                                color = IOSHealthCoral,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(
                            modifier = Modifier.weight(0.9f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "服药方式",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                            Text(
                                text = drug.mealInstruction,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Pharmacology Mechanism Section
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Rounded.Science,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = drug.mechanism,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            }

            // 5. Safety & Anti-Herb Interaction Alert Box (Visible by default!)
            AnimatedVisibility(visible = showSafetyInline) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Surface(
                        color = CrimsonDanger.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WarningAmber,
                                contentDescription = null,
                                tint = CrimsonDanger,
                                modifier = Modifier
                                    .padding(top = 1.dp)
                                    .size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = drug.safetyNote,
                                style = MaterialTheme.typography.bodySmall,
                                color = CrimsonDanger,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Bottom Hint Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "点击卡片可自定义任意剂量并查看完整说明书",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showSafetyInline = !showSafetyInline }
                ) {
                    Text(
                        text = if (showSafetyInline) "折叠安全警示" else "展开安全警示",
                        style = MaterialTheme.typography.labelSmall,
                        color = IOSHealthCoral,
                        fontSize = 11.sp
                    )
                    Icon(
                        imageVector = if (showSafetyInline) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        tint = IOSHealthCoral,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Full iOS Modal Sheet for inspecting a CatalogDrug's complete clinical monograph + customizing dosage
 */
@Composable
private fun CatalogDrugMonographSheet(
    drug: CatalogDrug,
    isAdded: Boolean,
    initialDosage: String,
    onAddWithDosage: (String) -> Unit,
    onUpdateLiveDosage: (String) -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    val accentColor = Color(drug.colorHex)
    var selectedDosage by remember(drug.id, initialDosage) { mutableStateOf(initialDosage) }

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
        // iOS Sheet Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onDismiss) {
                Text("关闭", color = IOSHealthCoral, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
            Text(
                text = "循证药物说明书",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(
                onClick = {
                    if (isAdded) {
                        onRemove()
                    } else {
                        onAddWithDosage(selectedDosage)
                    }
                }
            ) {
                Text(
                    text = if (isAdded) "移出计划" else "加入计划",
                    color = if (isAdded) CrimsonDanger else IOSHealthCoral,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        // Identity Card
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
                        .background(accentColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconForForm(drug.form),
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = drug.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = drug.genericName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = drug.evidenceLevel,
                        style = MaterialTheme.typography.labelSmall,
                        color = IOSMintGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Interactive Dosage Selector right inside the Monograph Sheet!
        IOSDosageAdjusterCard(
            currentDosage = selectedDosage,
            form = drug.form,
            baseReferenceDosage = drug.defaultDosage,
            onDosageChange = { newDosage ->
                selectedDosage = newDosage
                onUpdateLiveDosage(newDosage)
            }
        )

        // Key-Value Clinical Parameters
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                IOSKeyValueRow(label = "临床科室分类", value = drug.category, showDivider = true)
                IOSKeyValueRow(label = "WHO ATC 编码", value = drug.atcCode, showDivider = true)
                IOSKeyValueRow(label = "指南标准参考剂量", value = "${drug.defaultDosage} (${drug.form})", showDivider = true)
                IOSKeyValueRow(label = "最佳服药时间", value = "${drug.recommendedTime} · ${drug.mealInstruction}", showDivider = false)
            }
        }

        // Pharmacology Mechanism
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "药理机制与临床获益证据 (RCT)",
                    style = MaterialTheme.typography.labelLarge,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = drug.mechanism,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 21.sp
                )
            }
        }

        // Safety & Herb Contraindication Card
        Surface(
            color = CrimsonDanger.copy(alpha = 0.08f),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.WarningAmber,
                        contentDescription = null,
                        tint = CrimsonDanger,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "用药安全警示与中草药联用禁忌",
                        style = MaterialTheme.typography.labelLarge,
                        color = CrimsonDanger,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = drug.safetyNote,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CrimsonDanger,
                    lineHeight = 20.sp
                )
            }
        }

        Button(
            onClick = {
                if (isAdded) {
                    onRemove()
                } else {
                    onAddWithDosage(selectedDosage)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isAdded) CrimsonDanger else IOSHealthCoral
            )
        ) {
            Icon(
                imageVector = if (isAdded) Icons.Rounded.DeleteOutline else Icons.Rounded.Add,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isAdded) {
                    "从今日用药计划中移除"
                } else {
                    "按「$selectedDosage」加入今日用药计划"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun ToxicHerbAlertsSection() {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CrimsonDanger.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = null,
                            tint = CrimsonDanger,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "破除“纯天然中草药无毒”误区",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonDanger
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "流行病学研究显示，传统中草药与膳食补充剂占中国急性药物性肝损伤 (DILI) 病因首位 (26.81%)。以下为 WHO 与国家药监局明确通报的 10 类高危肝肾与神经毒性成分：",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(EvidenceDrugCatalog.toxicHerbAlerts, key = { it.id }) { alert ->
            ToxicHerbIOSCard(alert)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ToxicHerbIOSCard(alert: ToxicHerbAlert) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                PillMetaBadge(
                    text = alert.severity,
                    containerColor = CrimsonDanger.copy(alpha = 0.12f),
                    contentColor = CrimsonDanger
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "致毒成分：${alert.toxicCompound}",
                style = MaterialTheme.typography.labelMedium,
                color = IOSWarmOrange,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "器官损害：${alert.hazardType}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = CrimsonDanger,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "涉毒来源：${alert.commonSources}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = alert.clinicalEvidence,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EvidencePyramidSection() {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(EvidenceDrugCatalog.evidencePyramid, key = { it.level }) { tier ->
            EvidenceTierIOSCard(tier)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EvidenceTierIOSCard(tier: EvidencePyramidTier) {
    val barColor = when {
        tier.reliabilityScore >= 90 -> IOSMintGreen
        tier.reliabilityScore >= 70 -> IOSPlumPurple
        else -> CrimsonDanger
    }

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
                PillMetaBadge(
                    text = tier.level,
                    containerColor = barColor.copy(alpha = 0.14f),
                    contentColor = barColor
                )
                Text(
                    text = "可信度 ${tier.reliabilityScore}%",
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = barColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tier.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = tier.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { tier.reliabilityScore / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(CircleShape),
                color = barColor,
                trackColor = barColor.copy(alpha = 0.14f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tier.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
