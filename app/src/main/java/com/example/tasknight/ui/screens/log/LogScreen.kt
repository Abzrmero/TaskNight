package com.example.tasknight.ui.screens.log

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Star
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasknight.R
import com.example.tasknight.ui.components.TaskNightToast
import com.example.tasknight.presentation.log.LogViewModel
import com.example.tasknight.presentation.log.TaskLogItem
import com.example.tasknight.domain.models.Mood
import com.example.tasknight.ui.screens.dashboard.BottomNavigationBar
import androidx.compose.foundation.border
import com.example.tasknight.ui.theme.InterFontFamily
import kotlinx.coroutines.delay
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@Composable
fun LogScreen(
    isDarkMode: Boolean = true,
    onNavigateToHome: () -> Unit = {},
    onNavigateToSet: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onSaveComplete: () -> Unit = {},
    viewModel: LogViewModel = viewModel()
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
    val CardBg = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    val CompletedCardBg = Color(0xFF10B981).copy(alpha = 0.12f)
    val PureWhite = if (isDarkMode) Color(0xFFFFFFFF) else Color(0xFF1E293B)
    val MutedGrey = if (isDarkMode) Color(0xFF8B949E) else Color(0xFF64748B)
    val AccentGreen = Color(0xFF10B981)
    val GoldStar = Color(0xFFFBBF24)
    val AccentOrange = Color(0xFFF59E0B)

    val haptic = LocalHapticFeedback.current
    var showConfetti by remember { mutableStateOf(false) }

    // Handle save success and navigation
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            onSaveComplete()
            viewModel.resetSaveSuccess()
        }
    }

    val party = remember {
        Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            position = Position.Relative(0.5, 0.3),
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100)
        )
    }

    // Check if all tasks are completed and trigger confetti
    LaunchedEffect(state.completedCount) {
        if (state.completedCount == state.totalTasks && state.totalTasks > 0) {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            showConfetti = true
            delay(3000)
            showConfetti = false
        }
    }

    val listState = rememberLazyListState()
    var taskToConfirmSave by remember { mutableStateOf<String?>(null) }

    if (taskToConfirmSave != null) {
        AlertDialog(
            onDismissRequest = { taskToConfirmSave = null },
            title = { Text(text = "Save Changes?") },
            text = { Text(text = "Do you want to save this quick thought?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        taskToConfirmSave?.let { viewModel.confirmTaskReflection(it) }
                        taskToConfirmSave = null
                    }
                ) {
                    Text("Yes", color = AccentGreen)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        taskToConfirmSave?.let { viewModel.cancelTaskReflection(it) }
                        taskToConfirmSave = null
                    }
                ) {
                    Text("No", color = Color.Red.copy(alpha = 0.7f))
                }
            },
            containerColor = CardBg,
            titleContentColor = PureWhite,
            textContentColor = PureWhite.copy(alpha = 0.8f)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgNavy)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E3A8A).copy(alpha = 0.3f),
                            BgNavy
                        )
                    )
                )
        )

        // Confetti Effect Overlay
        if (showConfetti) {
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = listOf(party)
            )
        }

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
                verticalArrangement = Arrangement.spacedBy(cardSpacing)
            ) {
                // Header with Progress Ring
                item {
                    LogHeader(
                        completedCount = state.completedCount,
                        totalTasks = state.totalTasks,
                        maxTasks = state.maxTasks,
                        isTablet = isTablet,
                        titleColor = PureWhite,
                        subtitleColor = MutedGrey,
                        accentGreen = AccentGreen
                    )

                    Spacer(modifier = Modifier.height(cardSpacing))
                }

                // Empty State or Task List Title
                if (state.tasks.isNotEmpty()) {
                    item {
                        Text(
                            text = "Today's Focus",
                            color = PureWhite.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = InterFontFamily,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                // Task List
                items(
                    items = state.tasks,
                    key = { task -> task.id }
                ) { task ->
                    TaskLogCard(
                        task = task,
                        cardBg = CardBg,
                        completedCardBg = CompletedCardBg,
                        pureWhite = PureWhite,
                        mutedGrey = MutedGrey,
                        accentGreen = AccentGreen,
                        cornerRadius = cardCornerRadius,
                        isTablet = isTablet,
                        onToggleComplete = {
                            if (!task.isCompleted) {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            }
                            viewModel.toggleTaskCompletion(task.id)
                        },
                        onReflectionChange = { reflection ->
                            viewModel.updateTaskReflection(task.id, reflection)
                        },
                        onConfirmReflection = {
                            taskToConfirmSave = task.id
                        },
                        onCancelReflection = {
                            viewModel.cancelTaskReflection(task.id)
                        }
                    )
                }

                // Daily Reflection Footer
                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        DailyReflectionCard(
                            mood = state.selectedMood,
                            rating = state.rating,
                            dailyNote = state.dailyNote,
                            isSaving = state.isSaving,
                            cardBg = CardBg,
                            pureWhite = PureWhite,
                            mutedGrey = MutedGrey,
                            accentGreen = AccentGreen,
                            goldStar = GoldStar,
                            cornerRadius = cardCornerRadius,
                            isTablet = isTablet,
                            onMoodSelected = { mood ->
                                viewModel.selectMood(mood)
                            },
                            onRatingSelected = { rating ->
                                viewModel.selectRating(rating)
                            },
                            onNoteChange = { note ->
                                viewModel.updateDailyNote(note)
                            },
                            onSaveReflection = {
                                viewModel.saveDailyReflection()
                            }
                        )

                        if (state.isGuest) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(RoundedCornerShape(cardCornerRadius))
                                    .background(CardBg.copy(alpha = 0.94f))
                                    .clickable(
                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                        indication = null
                                    ) { /* Block clicks */ }
                            )

                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = AccentOrange.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        AccentOrange.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Text(
                                        text = "Create your account to access this feature",
                                        modifier = Modifier.padding(
                                            vertical = 12.dp,
                                            horizontal = 16.dp
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        color = AccentOrange,
                                        textAlign = TextAlign.Center,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Bottom Navigation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
                    .padding(bottom = bottomPadding)
            ) {
                BottomNavigationBar(
                    isTablet = isTablet,
                    isDarkMode = isDarkMode,
                    selectedIndex = 2,
                    onItemSelected = { index ->
                        when (index) {
                            0 -> onNavigateToHome()
                            1 -> onNavigateToSet()
                            2 -> { }
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
fun LogHeader(
    completedCount: Int,
    totalTasks: Int,
    maxTasks: Int,
    isTablet: Boolean,
    titleColor: Color,
    subtitleColor: Color,
    accentGreen: Color
) {
    val progress = if (totalTasks > 0) completedCount.toFloat() / totalTasks else 0f

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Log Your Progress",
                color = titleColor,
                fontSize = if (isTablet) 32.sp else 26.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = InterFontFamily
            )
            Text(
                text = if (totalTasks > 0) "$completedCount/$totalTasks tasks finished (Limit: $maxTasks)" else "No tasks set for today",
                color = subtitleColor,
                fontSize = if (isTablet) 16.sp else 14.sp,
                fontFamily = InterFontFamily
            )
        }

        // Progress Ring
        Box(
            modifier = Modifier.size(if (isTablet) 60.dp else 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = if (isTablet) 6.dp.toPx() else 5.dp.toPx()
                val sweepAngle = progress * 360f

                // Background circle
                drawCircle(
                    color = subtitleColor.copy(alpha = 0.2f),
                    radius = size.minDimension / 2,
                    style = Stroke(width = strokeWidth)
                )

                // Progress arc
                if (progress > 0) {
                    drawArc(
                        color = accentGreen,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            Text(
                text = "${(progress * 100).toInt()}%",
                color = titleColor,
                fontSize = if (isTablet) 14.sp else 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TaskLogCard(
    task: TaskLogItem,
    cardBg: Color,
    completedCardBg: Color,
    pureWhite: Color,
    mutedGrey: Color,
    accentGreen: Color,
    cornerRadius: androidx.compose.ui.unit.Dp,
    isTablet: Boolean,
    onToggleComplete: () -> Unit,
    onReflectionChange: (String) -> Unit,
    onConfirmReflection: () -> Unit = {},
    onCancelReflection: () -> Unit = {}
) {
    val backgroundColor = if (task.isCompleted) completedCardBg else cardBg

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(cornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleComplete() }
                    .padding(if (isTablet) 18.dp else 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox Circle
                Box(
                    modifier = Modifier
                        .size(if (isTablet) 28.dp else 24.dp)
                        .clip(CircleShape)
                        .background(
                            if (task.isCompleted) accentGreen else Color.Transparent
                        )
                        .then(
                            if (!task.isCompleted) {
                                Modifier.border(2.dp, mutedGrey.copy(alpha = 0.5f), CircleShape)
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isCompleted) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = "Completed",
                            tint = pureWhite,
                            modifier = Modifier.size(if (isTablet) 16.dp else 14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(if (isTablet) 16.dp else 12.dp))

                // Task Content
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Task Title
                    Text(
                        text = task.title,
                        color = if (task.isCompleted) accentGreen else pureWhite,
                        fontSize = if (isTablet) 16.sp else 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = InterFontFamily,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                    )

                    if (!task.isCompleted && task.description.isNotEmpty()) {
                        Text(
                            text = task.description,
                            color = mutedGrey,
                            fontSize = if (isTablet) 13.sp else 11.sp,
                            fontFamily = InterFontFamily,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }

                    if (task.isCompleted && task.completedAt != null) {
                        Text(
                            text = "Completed at ${task.completedAt}",
                            color = mutedGrey.copy(alpha = 0.7f),
                            fontSize = if (isTablet) 11.sp else 9.sp,
                            fontFamily = InterFontFamily
                        )
                    }
                }
            }

            if (task.isCompleted) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = if (isTablet) 58.dp else 48.dp, end = 16.dp, bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_pencil),
                            contentDescription = null,
                            tint = mutedGrey,
                            modifier = Modifier.size(if (isTablet) 16.dp else 14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = task.reflection,
                            onValueChange = onReflectionChange,
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                Text(
                                    text = "Quick thought on this task?",
                                    color = mutedGrey.copy(alpha = 0.6f),
                                    fontSize = if (isTablet) 13.sp else 11.sp
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = pureWhite,
                                unfocusedTextColor = pureWhite,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = if (isTablet) 13.sp else 11.sp,
                                fontFamily = InterFontFamily
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        // Save/Cancel icons for reflection
                        val originalReflection = task.originalTask?.reflection ?: ""
                        if (task.reflection != originalReflection) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_check),
                                    contentDescription = "Save Reflection",
                                    tint = accentGreen,
                                    modifier = Modifier
                                        .size(if (isTablet) 24.dp else 20.dp)
                                        .clickable { onConfirmReflection() }
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel Reflection",
                                    tint = Color.Red.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .size(if (isTablet) 24.dp else 20.dp)
                                        .clickable { onCancelReflection() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyReflectionCard(
    mood: Mood?,
    rating: Int,
    dailyNote: String,
    isSaving: Boolean,
    cardBg: Color,
    pureWhite: Color,
    mutedGrey: Color,
    accentGreen: Color,
    goldStar: Color,
    cornerRadius: androidx.compose.ui.unit.Dp,
    isTablet: Boolean,
    onMoodSelected: (Mood) -> Unit,
    onRatingSelected: (Int) -> Unit,
    onNoteChange: (String) -> Unit,
    onSaveReflection: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(cornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isTablet) 20.dp else 16.dp)
        ) {
            Text(
                text = "Daily Reflection",
                color = pureWhite,
                fontSize = if (isTablet) 18.sp else 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = InterFontFamily
            )

            Spacer(modifier = Modifier.height(if (isTablet) 16.dp else 12.dp))

            // Mood Selector
            Text(
                text = "How was your mood today?",
                color = mutedGrey,
                fontSize = if (isTablet) 14.sp else 12.sp,
                fontFamily = InterFontFamily,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Mood.values().forEach { moodItem ->
                    MoodButton(
                        emoji = moodItem.emoji,
                        label = moodItem.name.lowercase().replaceFirstChar { it.uppercase() },
                        isSelected = mood == moodItem,
                        accentGreen = accentGreen,
                        mutedGrey = mutedGrey,
                        pureWhite = pureWhite,
                        isTablet = isTablet,
                        onClick = { onMoodSelected(moodItem) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isTablet) 20.dp else 16.dp))

            // Star Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Overall day rating",
                    color = mutedGrey,
                    fontSize = if (isTablet) 14.sp else 12.sp,
                    fontFamily = InterFontFamily
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 1..5) {
                        Icon(
                            painter = painterResource(id = if (i <= rating) R.drawable.ic_star_filled else R.drawable.ic_star_outline),
                            contentDescription = "$i star",
                            tint = if (i <= rating) Color(0xFFFFD700) else mutedGrey.copy(alpha = 0.3f),
                            modifier = Modifier
                                .size(if (isTablet) 28.dp else 24.dp)
                                .clickable { onRatingSelected(i) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (isTablet) 20.dp else 16.dp))

            // Daily Note / Reflection
            Text(
                text = "Today's Highlights",
                color = pureWhite,
                fontSize = if (isTablet) 15.sp else 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = InterFontFamily
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = dailyNote,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "What made today special? Any lessons learned?",
                        color = mutedGrey.copy(alpha = 0.5f),
                        fontSize = if (isTablet) 14.sp else 12.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = pureWhite,
                    unfocusedTextColor = pureWhite,
                    focusedBorderColor = accentGreen.copy(alpha = 0.5f),
                    unfocusedBorderColor = mutedGrey.copy(alpha = 0.2f),
                    focusedContainerColor = pureWhite.copy(alpha = 0.03f),
                    unfocusedContainerColor = pureWhite.copy(alpha = 0.03f)
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = if (isTablet) 14.sp else 12.sp,
                    fontFamily = InterFontFamily
                ),
                shape = RoundedCornerShape(12.dp),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(if (isTablet) 24.dp else 20.dp))

            Button(
                onClick = onSaveReflection,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isTablet) 50.dp else 44.dp),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        text = "Complete Today's Journal",
                        fontSize = if (isTablet) 16.sp else 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = InterFontFamily
                    )
                }
            }
        }
    }
}

@Composable
fun MoodButton(
    emoji: String,
    label: String,
    isSelected: Boolean,
    accentGreen: Color,
    mutedGrey: Color,
    pureWhite: Color,
    isTablet: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) accentGreen.copy(alpha = 0.15f) else Color.Transparent
    val borderColor = if (isSelected) accentGreen else mutedGrey.copy(alpha = 0.3f)
    val textColor = if (isSelected) accentGreen else mutedGrey

    Card(
        modifier = Modifier
            .width(if (isTablet) 100.dp else 80.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(if (isTablet) 14.dp else 12.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (isTablet) 12.dp else 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                fontSize = if (isTablet) 32.sp else 28.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = if (isTablet) 13.sp else 11.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                fontFamily = InterFontFamily
            )
        }
    }
}
