package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.Medication
import com.example.data.MedicationRepository
import com.example.ui.MedicationViewModel
import com.example.ui.MedicationViewModelFactory
import com.example.ui.components.AddEditMedicationSheet
import com.example.ui.components.EvidenceDietScreen
import com.example.ui.components.EvidenceHeroBanner
import com.example.ui.components.EvidenceLibraryScreen
import com.example.ui.components.IOSMedicationRow
import com.example.ui.components.IOSSegmentedControl
import com.example.ui.components.MedicationDetailSheetContent
import com.example.ui.components.StatsAndStockScreen
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.IOSHealthCoral
import com.example.ui.theme.IOSMintGreen
import com.example.ui.theme.MedMateTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val database = remember { AppDatabase.getDatabase(context) }
            val repository = remember { MedicationRepository(database.medicationDao()) }
            val factory = remember { MedicationViewModelFactory(repository) }
            val viewModel: MedicationViewModel = viewModel(factory = factory)

            // Seed initial 4-medication evidence-based regimen with iOS Health colors on launch
            LaunchedEffect(Unit) {
                val prefs = context.getSharedPreferences("medmate_prefs", Context.MODE_PRIVATE)
                val seeded = prefs.getBoolean("initial_seed_v3_done", false)
                viewModel.seedDefaultIfNeeded(
                    alreadySeeded = seeded,
                    onMarkSeeded = {
                        prefs.edit().putBoolean("initial_seed_v3_done", true).apply()
                    }
                )
            }

            MedMateTheme {
                MedMateApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedMateApp(viewModel: MedicationViewModel) {
    val medications by viewModel.medications.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()

    var currentTab by remember { mutableIntStateOf(0) }
    var showAddEditSheet by remember { mutableStateOf(false) }
    var editingMedication by remember { mutableStateOf<Medication?>(null) }
    var inspectingMedication by remember { mutableStateOf<Medication?>(null) }
    var showEbmManifestoDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val addedDrugNames = remember(medications) {
        medications.map { it.name }.toSet()
    }
    val addedDrugDosages = remember(medications) {
        medications.associate { it.name to it.dosage }
    }

    val handleRemoveMedication: (Medication) -> Unit = { med ->
        viewModel.delete(med)
        if (inspectingMedication?.id == med.id) {
            inspectingMedication = null
        }
        coroutineScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = "已从今日用药移除「${med.name}」",
                actionLabel = "撤销",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.insert(med.copy(id = 0))
            }
        }
    }

    if (currentTab != 0) {
        BackHandler { currentTab = 0 }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            IOSTabBar(
                currentTab = currentTab,
                onSelectTab = { currentTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(targetState = currentTab, label = "ios_tab_switch") { tab ->
                when (tab) {
                    0 -> TodayRegimenScreen(
                        medications = medications,
                        selectedPeriod = selectedPeriod,
                        onSelectPeriod = { viewModel.selectPeriod(it) },
                        onAddClick = {
                            editingMedication = null
                            showAddEditSheet = true
                        },
                        onToggleTaken = { med ->
                            viewModel.toggleTaken(med)
                        },
                        onInspectMed = { med ->
                            inspectingMedication = med
                        },
                        onDeleteMed = handleRemoveMedication,
                        onClearAll = {
                            viewModel.clearAllMedications()
                            coroutineScope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar("已清空全部今日用药计划")
                            }
                        },
                        onMarkAllTaken = {
                            viewModel.markPeriodAllTaken(selectedPeriod)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("已全部标记为已服用")
                            }
                        },
                        onShowEbmManifesto = { showEbmManifestoDialog = true },
                        onLoadPreset = {
                            viewModel.loadPresetRegimen()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("已导入4项标准循证用药方案")
                            }
                        }
                    )
                    1 -> EvidenceLibraryScreen(
                        addedDrugNames = addedDrugNames,
                        addedDrugDosages = addedDrugDosages,
                        onAddDrugToRegimen = { drug, customDosage ->
                            val wasAlreadyAdded = drug.name in addedDrugNames
                            viewModel.addFromCatalog(drug, customDosage = customDosage)
                            coroutineScope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                if (wasAlreadyAdded) {
                                    snackbarHostState.showSnackbar("已更新「${drug.name}」剂量为 $customDosage")
                                } else {
                                    snackbarHostState.showSnackbar("已按「$customDosage」将「${drug.name}」加入今日计划")
                                }
                            }
                        },
                        onRemoveDrugFromRegimen = { drug ->
                            viewModel.removeFromCatalog(drug)
                            coroutineScope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar("已将「${drug.name}」从今日用药计划移除")
                            }
                        }
                    )
                    2 -> EvidenceDietScreen()
                    3 -> StatsAndStockScreen(
                        medications = medications,
                        onRestock = { med, amount ->
                            viewModel.restock(med, amount)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("「${med.name}」库存已增加 $amount")
                            }
                        },
                        onDeleteMedication = handleRemoveMedication,
                        onResetDaily = {
                            viewModel.resetDailyStatus()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("今日服药记录已重置")
                            }
                        },
                        onLoadPreset = {
                            viewModel.loadPresetRegimen()
                        }
                    )
                }
            }
        }

        // Medication Detail BottomSheet (iOS Sheet)
        val currentInspect = inspectingMedication?.let { target ->
            medications.find { it.id == target.id }
        }
        if (currentInspect != null) {
            val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { inspectingMedication = null },
                sheetState = detailSheetState,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                MedicationDetailSheetContent(
                    medication = currentInspect,
                    onToggleTaken = { viewModel.toggleTaken(currentInspect) },
                    onUpdateDosage = { newDosage ->
                        viewModel.updateDosage(currentInspect, newDosage)
                    },
                    onRestock = { amount ->
                        viewModel.restock(currentInspect, amount)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("已补充 $amount 剂库存")
                        }
                    },
                    onEdit = {
                        val medToEdit = currentInspect
                        inspectingMedication = null
                        editingMedication = medToEdit
                        showAddEditSheet = true
                    },
                    onDelete = {
                        handleRemoveMedication(currentInspect)
                    },
                    onDismiss = { inspectingMedication = null }
                )
            }
        }

        // Add / Edit Medication BottomSheet (iOS Sheet)
        if (showAddEditSheet) {
            val addEditSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = {
                    showAddEditSheet = false
                    editingMedication = null
                },
                sheetState = addEditSheetState,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                AddEditMedicationSheet(
                    initialMedication = editingMedication,
                    onDismiss = {
                        showAddEditSheet = false
                        editingMedication = null
                    },
                    onSave = { savedMed ->
                        if (editingMedication == null) {
                            viewModel.insert(savedMed)
                        } else {
                            viewModel.update(savedMed)
                        }
                        showAddEditSheet = false
                        editingMedication = null
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("已存储「${savedMed.name}」")
                        }
                    },
                    onDelete = { medToDelete ->
                        showAddEditSheet = false
                        editingMedication = null
                        handleRemoveMedication(medToDelete)
                    }
                )
            }
        }

        // iOS Style Alert Dialog for EBM Manifesto
        if (showEbmManifestoDialog) {
            EbmManifestoIOSDialog(
                onDismiss = { showEbmManifestoDialog = false },
                onExploreToxicAlerts = {
                    showEbmManifestoDialog = false
                    currentTab = 1
                }
            )
        }
    }
}

/**
 * Authentic iOS UITabBar with 0.5.dp top hairline border and Apple Health Coral tint
 */
@Composable
fun IOSTabBar(
    currentTab: Int,
    onSelectTab: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IOSTabBarItem(
                selected = currentTab == 0,
                label = "今日用药",
                activeIcon = Icons.Rounded.Favorite,
                inactiveIcon = Icons.Rounded.FavoriteBorder,
                onClick = { onSelectTab(0) },
                testTag = "nav_tab_today"
            )
            IOSTabBarItem(
                selected = currentTab == 1,
                label = "循证药典",
                activeIcon = Icons.Rounded.MenuBook,
                inactiveIcon = Icons.Outlined.MenuBook,
                onClick = { onSelectTab(1) },
                testTag = "nav_tab_library"
            )
            IOSTabBarItem(
                selected = currentTab == 2,
                label = "健康饮食",
                activeIcon = Icons.Rounded.Restaurant,
                inactiveIcon = Icons.Rounded.RestaurantMenu,
                onClick = { onSelectTab(2) },
                testTag = "nav_tab_diet"
            )
            IOSTabBarItem(
                selected = currentTab == 3,
                label = "药箱统计",
                activeIcon = Icons.Rounded.Analytics,
                inactiveIcon = Icons.Outlined.Analytics,
                onClick = { onSelectTab(3) },
                testTag = "nav_tab_stats"
            )
        }
    }
}

@Composable
private fun IOSTabBarItem(
    selected: Boolean,
    label: String,
    activeIcon: ImageVector,
    inactiveIcon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    val tint = if (selected) IOSHealthCoral else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .defaultMinSize(minWidth = 72.dp, minHeight = 48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag(testTag)
    ) {
        Icon(
            imageVector = if (selected) activeIcon else inactiveIcon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 10.sp
        )
    }
}

@Composable
fun TodayRegimenScreen(
    medications: List<Medication>,
    selectedPeriod: String,
    onSelectPeriod: (String) -> Unit,
    onAddClick: () -> Unit,
    onToggleTaken: (Medication) -> Unit,
    onInspectMed: (Medication) -> Unit,
    onDeleteMed: (Medication) -> Unit,
    onClearAll: () -> Unit,
    onMarkAllTaken: () -> Unit,
    onShowEbmManifesto: () -> Unit,
    onLoadPreset: () -> Unit
) {
    val dateFormatted = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINESE))
    }
    var isManageMode by remember { mutableStateOf(false) }
    var showClearAllConfirm by remember { mutableStateOf(false) }

    // Automatically exit manage mode when all medications are removed
    LaunchedEffect(medications.isEmpty()) {
        if (medications.isEmpty()) {
            isManageMode = false
        }
    }

    val periods = listOf("全部", "晨起", "午餐", "晚餐", "睡前")
    val selectedPeriodIdx = periods.indexOf(selectedPeriod).coerceAtLeast(0)

    val filteredMedications = remember(medications, selectedPeriod) {
        if (selectedPeriod == "全部") medications
        else medications.filter { it.period == selectedPeriod }
    }
    val groupedByPeriod = remember(filteredMedications) {
        val order = listOf("晨起", "午餐", "晚餐", "睡前")
        filteredMedications
            .groupBy { it.period }
            .entries
            .sortedBy { entry -> order.indexOf(entry.key).let { if (it == -1) 99 else it } }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Authentic iOS Large Title Navigation Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = dateFormatted.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "今日用药",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 34.sp,
                            letterSpacing = (-0.8).sp
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // iOS Top-Right Action Buttons: "管理/完成" Pill + Circular "+" Button
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (medications.isNotEmpty()) {
                        Surface(
                            onClick = { isManageMode = !isManageMode },
                            color = if (isManageMode) IOSHealthCoral else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("header_manage_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isManageMode) Icons.Rounded.Check else Icons.Rounded.EditNote,
                                    contentDescription = null,
                                    tint = if (isManageMode) Color.White else IOSHealthCoral,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isManageMode) "完成" else "管理",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isManageMode) Color.White else IOSHealthCoral,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Surface(
                        onClick = onAddClick,
                        color = IOSHealthCoral.copy(alpha = 0.14f),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("header_add_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "添加药物",
                                tint = IOSHealthCoral,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Apple Health Style Summary Ring Card
        item {
            EvidenceHeroBanner(
                medications = medications,
                onShowEbmManifesto = onShowEbmManifesto,
                onMarkAllTaken = onMarkAllTaken
            )
        }

        // Authentic iOS UISegmentedControl for Time Periods
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                IOSSegmentedControl(
                    options = periods,
                    selectedIndex = selectedPeriodIdx,
                    onSelect = { idx -> onSelectPeriod(periods[idx]) },
                    tagPrefix = "period_segment"
                )
            }
        }

        // Empty State or iOS Inset Grouped Sections
        if (filteredMedications.isEmpty()) {
            item {
                IOSEmptyStateCard(
                    isFiltered = selectedPeriod != "全部" && medications.isNotEmpty(),
                    onAddClick = onAddClick,
                    onLoadPreset = onLoadPreset,
                    onResetFilter = { onSelectPeriod("全部") }
                )
            }
        } else {
            groupedByPeriod.forEach { (periodName, periodMeds) ->
                item(key = "section_$periodName") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        // iOS Inset Grouped Section Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${periodName}时段 (${periodTimeHint(periodName)})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            val doneCount = periodMeds.count { it.isTakenToday }
                            Text(
                                text = if (isManageMode) {
                                    "点击红色按钮可移除药物"
                                } else if (doneCount == periodMeds.size) {
                                    "已全部完成"
                                } else {
                                    "$doneCount/${periodMeds.size} 已服用"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    isManageMode -> CrimsonDanger
                                    doneCount == periodMeds.size -> IOSMintGreen
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Single Rounded White Card Containing All Rows in this Section
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column {
                                periodMeds.forEachIndexed { index, med ->
                                    IOSMedicationRow(
                                        medication = med,
                                        showBottomDivider = index < periodMeds.lastIndex,
                                        onToggleTaken = { onToggleTaken(med) },
                                        onRowClick = { onInspectMed(med) },
                                        onDelete = { onDeleteMed(med) },
                                        isManageMode = isManageMode
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (isManageMode && medications.isNotEmpty()) {
                item(key = "clear_all_footer") {
                    Surface(
                        onClick = { showClearAllConfirm = true },
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .testTag("clear_all_medications_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteSweep,
                                contentDescription = null,
                                tint = CrimsonDanger,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "清空全部今日用药计划 (${medications.size}项)",
                                style = MaterialTheme.typography.titleMedium,
                                color = CrimsonDanger,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(18.dp),
            title = {
                Text(
                    text = "清空全部用药计划？",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "将从今日用药与智能药箱中移除当前全部 ${medications.size} 项药物。您随时可以从循证药典重新添加。",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearAllConfirm = false
                        isManageMode = false
                        onClearAll()
                    }
                ) {
                    Text("确认清空", color = CrimsonDanger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) {
                    Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun IOSEmptyStateCard(
    isFiltered: Boolean,
    onAddClick: () -> Unit,
    onLoadPreset: () -> Unit,
    onResetFilter: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(IOSHealthCoral.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = null,
                    tint = IOSHealthCoral,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isFiltered) "该时段无服药记录" else "暂无用药提醒",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isFiltered) {
                    "切换到「全部」查看今天其它时段的用药安排"
                } else {
                    "科学管理每日服药时间与剂量，拥抱现代循证医学"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (isFiltered) {
                Button(
                    onClick = onResetFilter,
                    colors = ButtonDefaults.buttonColors(containerColor = IOSHealthCoral),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("显示全部时段", fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        onClick = onLoadPreset,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("load_sample_regimen_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "导入示例方案",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Surface(
                        onClick = onAddClick,
                        color = IOSHealthCoral,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "添加药物",
                                style = MaterialTheme.typography.labelLarge,
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

@Composable
private fun EbmManifestoIOSDialog(
    onDismiss: () -> Unit,
    onExploreToxicAlerts: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        title = {
            Text(
                text = "拥抱循证，拒绝中医",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "• 明确化学结构与药代动力学：现代药物具备清晰的半衰期、血药浓度与代谢路径。",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• 双盲随机对照试验 (RCT)：拒绝个案轶事与安慰剂假象，以全球临床指南为唯一准绳。",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• 远离肝肾毒性：拒绝含马兜铃酸、吡咯里西定生物碱、何首乌等伤肝伤肾的不明草药成分。",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onExploreToxicAlerts) {
                Text("查看毒性警示录", color = IOSHealthCoral, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("好", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

private fun periodTimeHint(period: String) = when (period) {
    "晨起" -> "06:00 - 10:00"
    "午餐" -> "11:00 - 15:00"
    "晚餐" -> "16:00 - 20:00"
    "睡前" -> "20:30 - 23:30"
    else -> "全天"
}
