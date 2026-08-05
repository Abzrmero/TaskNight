package com.example.tasknight.ui.screens.set

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasknight.R
import com.example.tasknight.ui.components.TaskNightToast
import com.example.tasknight.presentation.set.Priority
import com.example.tasknight.presentation.set.SetTasksViewModel
import com.example.tasknight.presentation.set.TaskItem as ViewModelTaskItem
import com.example.tasknight.ui.screens.dashboard.BottomNavigationBar
import com.example.tasknight.ui.theme.InterFontFamily
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import androidx.compose.ui.graphics.StrokeCap

@Composable
fun SetTasksScreen(
    isDarkMode: Boolean = true,
    onNavigateToHome: () -> Unit = {},
    onNavigateToLog: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onSaveComplete: () -> Unit = {},
    viewModel: SetTasksViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isTablet = screenWidth >= 600.dp

    val horizontalPadding = if (isTablet) 32.dp else 20.dp
    val topPadding = if (screenHeight < 700.dp) 16.dp else 24.dp
    val bottomPadding = if (screenHeight < 700.dp) 16.dp else 24.dp
    val cardSpacing = if (isTablet) 16.dp else 12.dp
    val cardCornerRadius = if (isTablet) 20.dp else 16.dp

    val BgNavy = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val CardBg = if (isDarkMode) Color(0xFF1A2332) else Color(0xFFFFFFFF)
    val PureWhite = if (isDarkMode) Color(0xFFFFFFFF) else Color(0xFF1E293B)
    val MutedGrey = if (isDarkMode) Color(0xFF8B949E) else Color(0xFF64748B)
    val AccentGreen = Color(0xFF10B981)
    val DangerRed = Color(0xFFEF4444)
    val PriorityHigh = Color(0xFFEF4444)
    val PriorityMedium = Color(0xFFF59E0B)
    val PriorityLow = Color(0xFF3B82F6)

    val tomorrowDate = LocalDate.now().plusDays(1)
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")

    val listState = rememberLazyListState()

    // Handle save success
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            onSaveComplete()
            viewModel.resetSaveSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgNavy)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        // Background Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E3A8A).copy(alpha = 0.2f),
                            BgNavy
                        )
                    )
                )
        )

        // Main Column
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Scrollable content
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = horizontalPadding)
                    .padding(top = topPadding),
                verticalArrangement = Arrangement.spacedBy(cardSpacing),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Header
                item {
                    SetTasksHeader(
                        tasksSet = state.savedCount,
                        maxTasks = state.maxTasks,
                        tomorrowDate = tomorrowDate.format(dateFormatter),
                        isTablet = isTablet,
                        titleColor = PureWhite,
                        subtitleColor = MutedGrey,
                        accentGreen = AccentGreen
                    )

                    Spacer(modifier = Modifier.height(cardSpacing))
                }

                // Max Tasks Selector
                item {
                    MaxTasksSelector(
                        currentMax = state.maxTasks,
                        onMaxChange = { viewModel.updateMaxTasks(it) },
                        pureWhite = PureWhite,
                        mutedGrey = MutedGrey,
                        cardBg = CardBg,
                        accentGreen = AccentGreen,
                        isTablet = isTablet
                    )
                    
                    Spacer(modifier = Modifier.height(cardSpacing))
                }

                // Task Cards
                itemsIndexed(
                    items = state.tasks,
                    key = { _, task -> task.id }
                ) { index, task ->
                    TaskCard(
                        task = task,
                        cardBg = CardBg,
                        pureWhite = PureWhite,
                        mutedGrey = MutedGrey,
                        accentGreen = AccentGreen,
                        dangerRed = DangerRed,
                        priorityHigh = PriorityHigh,
                        priorityMedium = PriorityMedium,
                        priorityLow = PriorityLow,
                        cornerRadius = cardCornerRadius,
                        isTablet = isTablet,
                        onTitleChange = { newTitle ->
                            viewModel.updateTaskTitle(task.id, newTitle)
                        },
                        onWhyChange = { newWhy ->
                            viewModel.updateTaskWhy(task.id, newWhy)
                        },
                        onPriorityChange = { priority ->
                            viewModel.updateTaskPriority(task.id, priority)
                        },
                        onDelete = {
                            viewModel.deleteTask(task.id)
                        },
                        onConfirm = {
                            viewModel.confirmTask(task.id)
                        },
                        onCancel = {
                            viewModel.cancelEdit(task.id)
                        },
                        onMoveUp = {
                            if (index > 0) {
                                viewModel.moveTask(index, index - 1)
                            }
                        },
                        onMoveDown = {
                            if (index < state.tasks.size - 1) {
                                viewModel.moveTask(index, index + 1)
                            }
                        },
                        isFirst = index == 0,
                        isLast = index == state.tasks.size - 1
                    )
                }

                // Add Task Button
                item {
                    val canAddTask = state.tasks.size < state.maxTasks &&
                                    state.tasks.none { it.title.isBlank() }
                    AddTaskButton(
                        currentTasks = state.savedCount,
                        maxTasks = state.maxTasks,
                        isTablet = isTablet,
                        accentGreen = AccentGreen,
                        enabled = canAddTask,
                        onClick = { viewModel.addTask() }
                    )
                }
            }

            // Bottom Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, BgNavy),
                            startY = 0f,
                            endY = 50f
                        )
                    )
                    .padding(horizontal = horizontalPadding)
                    .padding(bottom = bottomPadding),
                verticalArrangement = Arrangement.spacedBy(if (isTablet) 14.dp else 10.dp)
            ) {
                // Save Button
                Button(
                    onClick = {
                        viewModel.saveTasks()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isTablet) 58.dp else 52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGreen,
                        contentColor = BgNavy,
                        disabledContainerColor = AccentGreen.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(if (isTablet) 16.dp else 14.dp),
                    enabled = !state.isSaving && state.tasks.any { it.title.isNotBlank() },
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = BgNavy,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_save),
                            contentDescription = null,
                            modifier = Modifier.size(if (isTablet) 22.dp else 18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Save for Tomorrow",
                            fontSize = if (isTablet) 18.sp else 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = InterFontFamily
                        )
                    }
                }

                BottomNavigationBar(
                    isTablet = isTablet,
                    isDarkMode = isDarkMode,
                    selectedIndex = 1,
                    onItemSelected = { index ->
                        when (index) {
                            0 -> onNavigateToHome()
                            1 -> { }
                            2 -> onNavigateToLog()
                            3 -> onNavigateToHistory()
                            4 -> onNavigateToSettings()
                        }
                    }
                )
            }
        }

    }
}

@Composable
fun SetTasksHeader(
    tasksSet: Int,
    maxTasks: Int,
    tomorrowDate: String,
    isTablet: Boolean,
    titleColor: Color,
    subtitleColor: Color,
    accentGreen: Color
) {
    val progress = tasksSet.toFloat() / maxTasks.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Bedtime Journal",
                color = titleColor,
                fontSize = if (isTablet) 36.sp else 28.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = InterFontFamily,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tomorrowDate,
                color = accentGreen,
                fontSize = if (isTablet) 16.sp else 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = InterFontFamily
            )
            Text(
                text = "What are you going to conquer?",
                color = subtitleColor,
                fontSize = if (isTablet) 14.sp else 12.sp,
                fontFamily = InterFontFamily
            )
        }

        // Animated Progress Ring
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(start = 16.dp)
        ) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(56.dp),
                color = titleColor.copy(alpha = 0.05f),
                strokeWidth = 5.dp,
                strokeCap = StrokeCap.Round
            )
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.size(56.dp),
                color = accentGreen,
                strokeWidth = 5.dp,
                strokeCap = StrokeCap.Round
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$tasksSet",
                    color = titleColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = InterFontFamily
                )
            }
        }
    }
}

@Composable
fun TaskCard(
    task: ViewModelTaskItem,
    cardBg: Color,
    pureWhite: Color,
    mutedGrey: Color,
    accentGreen: Color,
    dangerRed: Color,
    priorityHigh: Color,
    priorityMedium: Color,
    priorityLow: Color,
    cornerRadius: androidx.compose.ui.unit.Dp,
    isTablet: Boolean,
    onTitleChange: (String) -> Unit,
    onWhyChange: (String) -> Unit,
    onPriorityChange: (Priority) -> Unit,
    onDelete: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    isFirst: Boolean,
    isLast: Boolean
) {
    var showPriorityMenu by remember { mutableStateOf(false) }

    val priorityColor = when (task.priority) {
        Priority.HIGH -> priorityHigh
        Priority.MEDIUM -> priorityMedium
        Priority.LOW -> priorityLow
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(cornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Priority Indicator Bar
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(priorityColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isTablet) 18.dp else 14.dp)
            ) {
                // Header Row: Drag/Reorder Controls, Priority, Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reorder Controls
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onMoveUp,
                            enabled = !isFirst,
                            modifier = Modifier.size(if (isTablet) 32.dp else 28.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_chevron_up),
                                contentDescription = "Move up",
                                tint = if (!isFirst) mutedGrey else mutedGrey.copy(alpha = 0.2f),
                                modifier = Modifier.size(if (isTablet) 20.dp else 16.dp)
                            )
                        }
                        IconButton(
                            onClick = onMoveDown,
                            enabled = !isLast,
                            modifier = Modifier.size(if (isTablet) 32.dp else 28.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_chevron_down),
                                contentDescription = "Move down",
                                tint = if (!isLast) mutedGrey else mutedGrey.copy(alpha = 0.2f),
                                modifier = Modifier.size(if (isTablet) 20.dp else 16.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Priority Chip Container
                        Box {
                            Surface(
                                onClick = { showPriorityMenu = true },
                                color = priorityColor.copy(alpha = 0.15f),
                                shape = CircleShape,
                            ) {
                                Row(
                                    modifier = Modifier
                                        .height(28.dp)
                                        .padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(priorityColor)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = task.priority.name.lowercase().replaceFirstChar { it.uppercase() },
                                        color = priorityColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = InterFontFamily
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showPriorityMenu,
                                onDismissRequest = { showPriorityMenu = false },
                                containerColor = cardBg,
                                modifier = Modifier
                                    .border(1.dp, mutedGrey.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            ) {
                                listOf(
                                    Triple(Priority.HIGH, priorityHigh, "High Priority"),
                                    Triple(Priority.MEDIUM, priorityMedium, "Medium Priority"),
                                    Triple(Priority.LOW, priorityLow, "Low Priority")
                                ).forEach { (p, color, label) ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(label, color = pureWhite, fontSize = 14.sp)
                                            }
                                        },
                                        onClick = {
                                            onPriorityChange(p)
                                            showPriorityMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Tick Button (Only if modified)
                        if (task.isModified) {
                            var showSaveDialog by remember { mutableStateOf(false) }

                            IconButton(
                                onClick = { showSaveDialog = true },
                                modifier = Modifier.size(if (isTablet) 32.dp else 28.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_check),
                                    contentDescription = "Confirm changes",
                                    tint = accentGreen,
                                    modifier = Modifier.size(if (isTablet) 20.dp else 18.dp)
                                )
                            }

                            if (showSaveDialog) {
                                AlertDialog(
                                    onDismissRequest = { showSaveDialog = false },
                                    title = { Text("Save Changes", color = pureWhite) },
                                    text = { Text("Would you like to save the note?", color = pureWhite) },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            onConfirm()
                                            showSaveDialog = false
                                        }) {
                                            Text("Yes", color = accentGreen)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = {
                                            onCancel()
                                            showSaveDialog = false
                                        }) {
                                            Text("No", color = dangerRed)
                                        }
                                    },
                                    containerColor = cardBg,
                                    titleContentColor = pureWhite,
                                    textContentColor = pureWhite
                                )
                            }
                        }

                        // Delete Button
                        var showDeleteDialog by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.size(if (isTablet) 32.dp else 28.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_trash_small),
                                contentDescription = "Delete task",
                                tint = dangerRed.copy(alpha = 0.8f),
                                modifier = Modifier.size(if (isTablet) 18.dp else 14.dp)
                            )
                        }

                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                title = { Text("Delete Task", color = pureWhite) },
                                text = { Text("Are you sure you want to delete this task?", color = pureWhite) },
                                confirmButton = {
                                    TextButton(onClick = {
                                        onDelete()
                                        showDeleteDialog = false
                                    }) {
                                        Text("Yes", color = dangerRed)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = {
                                        showDeleteDialog = false
                                    }) {
                                        Text("No", color = pureWhite)
                                    }
                                },
                                containerColor = cardBg,
                                titleContentColor = pureWhite,
                                textContentColor = pureWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Task Title Input
                TextField(
                    value = task.title,
                    onValueChange = onTitleChange,
                    placeholder = {
                        Text(
                            "What is your task?",
                            color = mutedGrey.copy(alpha = 0.5f),
                            fontSize = if (isTablet) 18.sp else 16.sp,
                            fontFamily = InterFontFamily
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        cursorColor = accentGreen,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = pureWhite,
                        fontSize = if (isTablet) 18.sp else 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = InterFontFamily
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Task "Why" Input
                TextField(
                    value = task.why,
                    onValueChange = onWhyChange,
                    placeholder = {
                        Text(
                            "Why is this important?",
                            color = mutedGrey.copy(alpha = 0.4f),
                            fontSize = if (isTablet) 14.sp else 13.sp,
                            fontFamily = InterFontFamily
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        cursorColor = accentGreen,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = mutedGrey,
                        fontSize = if (isTablet) 14.sp else 13.sp,
                        fontFamily = InterFontFamily
                    ),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
fun MaxTasksSelector(
    currentMax: Int,
    onMaxChange: (Int) -> Unit,
    pureWhite: Color,
    mutedGrey: Color,
    cardBg: Color,
    accentGreen: Color,
    isTablet: Boolean
) {
    val options = listOf(3, 5, 10)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Max Tasks for Tomorrow",
            color = mutedGrey,
            fontSize = if (isTablet) 14.sp else 12.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = InterFontFamily,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = currentMax == option
                Surface(
                    onClick = { onMaxChange(option) },
                    modifier = Modifier.weight(1f),
                    color = if (isSelected) accentGreen.copy(alpha = 0.2f) else cardBg,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) accentGreen else mutedGrey.copy(alpha = 0.2f)
                    )
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$option Tasks",
                            color = if (isSelected) accentGreen else pureWhite.copy(alpha = 0.7f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = if (isTablet) 16.sp else 14.sp,
                            fontFamily = InterFontFamily
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddTaskButton(
    currentTasks: Int,
    maxTasks: Int,
    isTablet: Boolean,
    accentGreen: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val MutedGrey = Color(0xFF8B949E)
    val contentColor = if (enabled) accentGreen else MutedGrey.copy(alpha = 0.4f)

    Surface(
        onClick = if (enabled) onClick else ({ }),
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = RoundedCornerShape(if (isTablet) 16.dp else 14.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = contentColor.copy(alpha = 0.3f)
        ),
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isTablet) 20.dp else 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(if (isTablet) 20.dp else 18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Add Another Task",
                color = contentColor,
                fontSize = if (isTablet) 16.sp else 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = InterFontFamily
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "($currentTasks/$maxTasks)",
                color = contentColor.copy(alpha = 0.5f),
                fontSize = if (isTablet) 14.sp else 12.sp,
                fontFamily = InterFontFamily
            )
        }
    }
}
