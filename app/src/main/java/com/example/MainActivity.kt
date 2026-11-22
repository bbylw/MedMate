package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.DrugInteractionChecker
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
import com.example.ui.components.RegimenInteractionAlertBanner
import com.example.ui.components.StatsAndStockScreen
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.IOSHealthCoral
import com.example.ui.theme.IOSHealthCoralGlow
import com.example.ui.theme.IOSMintGreen
import com.example.ui.theme.IOSPlumPurple
import com.example.ui.theme.IOSWarmOrange
import com.example.ui.theme.MedMateTheme
import com.example.ui.theme.ObsidianHeroElevated
import com.example.ui.theme.ObsidianHeroSurface
import com.example.ui.theme.ObsidianHeroTextPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
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
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val medications by viewModel.medications.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val currentDateIso by viewModel.currentDateIso.collectAsStateWithLifecycle()

    var currentTab by remember { mutableIntStateOf(0) }
    var showAddEditSheet by remember { mutableStateOf(false) }
    var editingMedication by remember { mutableStateOf<Medication?>(null) }
    var inspectingMedication by remember { mutableStateOf<Medication?>(null) }
    var showEbmManifestoDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val triggerDailySync = remember(context, viewModel) {
        {
            val prefs = context.getSharedPreferences("medmate_prefs", Context.MODE_PRIVATE)
            val todayIso = LocalDate.now().toString()
            val lastSynced = prefs.getString("last_daily_refresh_date", null)
            viewModel.syncDailyRefresh(
                todayIso = todayIso,
                lastSyncedDate = lastSynced,
                onRefreshed = { syncedDate, refreshedCount ->
                    prefs.edit().putString("last_daily_refresh_date", syncedDate).apply()
                    if (refreshedCount > 0) {
                        coroutineScope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            snackbarHostState.showSnackbar("已自动刷新今日用药状态（${refreshedCount}项昨日记录已归零）")
                        }
                    }
                }
            )
        }
    }

    var currentMinutesOfDay by remember {
        mutableIntStateOf(LocalTime.now().let { it.hour * 60 + it.minute })
    }

    // 1. Trigger daily sync on launch and every 15 seconds to catch 00:00 midnight rollover & live countdown
    LaunchedEffect(Unit) {
        triggerDailySync()
        while (true) {
            delay(15_000L)
            currentMinutesOfDay = LocalTime.now().let { it.hour * 60 + it.minute }
            triggerDailySync()
        }
    }

    // 2. Trigger daily sync whenever the app returns to foreground (ON_RESUME)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                currentMinutesOfDay = LocalTime.now().let { it.hour * 60 + it.minute }
                triggerDailySync()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 3. Trigger daily sync on system date / time / timezone broadcasts
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                triggerDailySync()
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

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
                viewModel.restoreDeleted(med)
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
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(140))
                },
                label = "ios_tab_switch"
            ) { tab ->
                when (tab) {
                    0 -> TodayRegimenScreen(
                        medications = medications,
                        currentDateIso = currentDateIso,
                        currentMinutesOfDay = currentMinutesOfDay,
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
                                snackbarHostState.showSnackbar("已全部标记为今日已服用")
                            }
                        },
                        onResetToday = {
                            viewModel.resetDailyStatus()
                            coroutineScope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar("今日打卡状态已重置（药箱库存已还原）")
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
                    2 -> EvidenceDietScreen(
                        currentDateIso = currentDateIso,
                        medications = medications
                    )
                    3 -> StatsAndStockScreen(
                        medications = medications,
                        currentDateIso = currentDateIso,
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
                                snackbarHostState.showSnackbar("今日服药记录已重置（药箱库存已还原）")
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
                        if (selectedPeriod != "全部" && savedMed.period != selectedPeriod) {
                            viewModel.selectPeriod("全部")
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
 * Architectural Floating Porcelain Capsule Dock with High-Contrast Obsidian Active Pill
 */
@Composable
fun IOSTabBar(
    currentTab: Int,
    onSelectTab: (Int) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            color = if (isDark) ObsidianHeroSurface.copy(alpha = 0.96f) else Color.White.copy(alpha = 0.96f),
            border = BorderStroke(
                width = 0.85.dp,
                color = if (isDark) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.outlineVariant
            ),
            shadowElevation = 14.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IOSTabBarItem(
                    selected = currentTab == 0,
                    label = "今日用药",
                    activeIcon = Icons.Rounded.Favorite,
                    inactiveIcon = Icons.Rounded.FavoriteBorder,
                    onClick = { onSelectTab(0) },
                    testTag = "nav_tab_today",
                    modifier = Modifier.weight(1f)
                )
                IOSTabBarItem(
                    selected = currentTab == 1,
                    label = "循证药典",
                    activeIcon = Icons.AutoMirrored.Rounded.MenuBook,
                    inactiveIcon = Icons.AutoMirrored.Outlined.MenuBook,
                    onClick = { onSelectTab(1) },
                    testTag = "nav_tab_library",
                    modifier = Modifier.weight(1f)
                )
                IOSTabBarItem(
                    selected = currentTab == 2,
                    label = "饮食运动",
                    activeIcon = Icons.Rounded.Restaurant,
                    inactiveIcon = Icons.Rounded.RestaurantMenu,
                    onClick = { onSelectTab(2) },
                    testTag = "nav_tab_diet",
                    modifier = Modifier.weight(1f)
                )
                IOSTabBarItem(
                    selected = currentTab == 3,
                    label = "药箱统计",
                    activeIcon = Icons.Rounded.Analytics,
                    inactiveIcon = Icons.Outlined.Analytics,
                    onClick = { onSelectTab(3) },
                    testTag = "nav_tab_stats",
                    modifier = Modifier.weight(1f)
                )
            }
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
    testTag: String,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val iconTint by animateColorAsState(
        targetValue = if (selected) IOSHealthCoralGlow else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(220),
        label = "tab_icon_tint"
    )
    val labelTint by animateColorAsState(
        targetValue = if (selected) ObsidianHeroTextPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(220),
        label = "tab_label_tint"
    )
    val pillBg by animateColorAsState(
        targetValue = when {
            selected && isDark -> ObsidianHeroElevated
            selected -> ObsidianHeroSurface
            else -> Color.Transparent
        },
        animationSpec = tween(220),
        label = "tab_pill_bg"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "tab_icon_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(pillBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 7.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = if (selected) activeIcon else inactiveIcon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier
                .size(21.dp)
                .graphicsLayer {
                    scaleX = iconScale
                    scaleY = iconScale
                }
        )
        Spacer(modifier = Modifier.height(2.5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = labelTint,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 10.sp
        )
    }
}

@Composable
fun TodayRegimenScreen(
    medications: List<Medication>,
    currentDateIso: String,
    currentMinutesOfDay: Int = LocalTime.now().let { it.hour * 60 + it.minute },
    selectedPeriod: String,
    onSelectPeriod: (String) -> Unit,
    onAddClick: () -> Unit,
    onToggleTaken: (Medication) -> Unit,
    onInspectMed: (Medication) -> Unit,
    onDeleteMed: (Medication) -> Unit,
    onClearAll: () -> Unit,
    onMarkAllTaken: () -> Unit,
    onResetToday: () -> Unit,
    onShowEbmManifesto: () -> Unit,
    onLoadPreset: () -> Unit
) {
    val dateFormatted = remember(currentDateIso) {
        val parsed = runCatching { LocalDate.parse(currentDateIso) }.getOrElse { LocalDate.now() }
        parsed.format(DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINESE))
    }
    val interactionAlerts = remember(medications) {
        DrugInteractionChecker.analyzeRegimen(medications)
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
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Soft Warm Studio Ambient Wash at top of canvas
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(IOSHealthCoral.copy(alpha = 0.06f), Color.Transparent),
                        center = Offset(size.width * 0.85f, 40.dp.toPx()),
                        radius = size.width * 0.72f
                    ),
                    center = Offset(size.width * 0.85f, 40.dp.toPx()),
                    radius = size.width * 0.72f
                )
            },
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Editorial Large Title Navigation Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 14.dp, bottom = 8.dp),
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
                            letterSpacing = (-0.9).sp
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Editorial Top-Right Action Buttons: "管理/完成" Hairline Pill + Luminous Rose "+" Button
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (medications.isNotEmpty()) {
                        Surface(
                            onClick = { isManageMode = !isManageMode },
                            color = if (isManageMode) ObsidianHeroSurface else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                0.75.dp,
                                if (isManageMode) ObsidianHeroSurface else MaterialTheme.colorScheme.outlineVariant
                            ),
                            shadowElevation = 1.5.dp,
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
                                    tint = if (isManageMode) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isManageMode) "完成" else "管理",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isManageMode) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Surface(
                        onClick = onAddClick,
                        color = Color.Transparent,
                        shape = CircleShape,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("header_add_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(listOf(IOSHealthCoral, IOSHealthCoralGlow))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "添加药物",
                                tint = Color.White,
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
                currentMinutesOfDay = currentMinutesOfDay,
                onShowEbmManifesto = onShowEbmManifesto,
                onMarkAllTaken = onMarkAllTaken,
                onResetToday = onResetToday
            )
        }

        // Clinical Drug-Drug Interaction & Duplicate Therapy Alert Banner (only shown when conflicts exist)
        if (interactionAlerts.isNotEmpty()) {
            item(key = "interaction_alerts_banner") {
                RegimenInteractionAlertBanner(alerts = interactionAlerts)
            }
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
                        // Modern Atmospheric Period Section Header
                        val periodAccent = when (periodName) {
                            "晨起" -> IOSWarmOrange
                            "午餐" -> IOSMintGreen
                            "晚餐" -> IOSHealthCoral
                            else -> IOSPlumPurple
                        }
                        val periodIcon = when (periodName) {
                            "晨起" -> Icons.Rounded.WbTwilight
                            "午餐" -> Icons.Rounded.WbSunny
                            "晚餐" -> Icons.Rounded.NightsStay
                            else -> Icons.Rounded.Bedtime
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = periodIcon,
                                    contentDescription = null,
                                    tint = periodAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${periodName}时段",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = periodTimeHint(periodName),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            val doneCount = periodMeds.count { it.isTakenToday }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = when {
                                    isManageMode -> CrimsonDanger.copy(alpha = 0.12f)
                                    doneCount == periodMeds.size -> IOSMintGreen.copy(alpha = 0.14f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                }
                            ) {
                                Text(
                                    text = if (isManageMode) {
                                        "点击移除"
                                    } else if (doneCount == periodMeds.size) {
                                        "已全部完成"
                                    } else {
                                        "$doneCount/${periodMeds.size} 已服"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when {
                                        isManageMode -> CrimsonDanger
                                        doneCount == periodMeds.size -> IOSMintGreen
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Single Modern Elevated Card Containing All Rows in this Section
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)),
                            shadowElevation = 1.5.dp
                        ) {
                            Column {
                                periodMeds.forEachIndexed { index, med ->
                                    IOSMedicationRow(
                                        medication = med,
                                        showBottomDivider = index < periodMeds.lastIndex,
                                        onToggleTaken = { onToggleTaken(med) },
                                        onRowClick = { onInspectMed(med) },
                                        onDelete = { onDeleteMed(med) },
                                        isManageMode = isManageMode,
                                        currentMinutesOfDay = currentMinutesOfDay
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
