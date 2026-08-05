package com.example.tasknight.ui.screens.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.Modifier.Companion.then
import androidx.compose.ui.draw.blur
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasknight.presentation.dashboard.DashboardViewModel
import kotlinx.coroutines.delay
import androidx.compose.ui.draw.blur
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import com.example.tasknight.R
import com.example.tasknight.ui.components.TaskNightToast

@Composable
fun HomeDashboard(
    isDarkMode: Boolean = true,
    onLogout: () -> Unit,
    onSignOutAndStartFresh: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSet: () -> Unit = {},
    onNavigateToLog: () -> Unit = {},
    onNavigateToUpgrade: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel(),
    pendingToastMessage: String? = null,
    onToastShown: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val isTablet = screenWidth >= 600.dp

    val horizontalPadding = if (isTablet) 32.dp else 20.dp
    val topPadding = if (screenHeight < 700.dp) 16.dp else 24.dp
    val bottomPadding = if (screenHeight < 700.dp) 16.dp else 24.dp

    val BgNavy = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val PureWhite = if (isDarkMode) Color(0xFFFFFFFF) else Color(0xFF1E293B)
    val MoonYellow = if (isDarkMode) Color(0xFFFDE047) else Color(0xFFF59E0B)
    val AccentPurple = if (isDarkMode) Color(0xFF4A3299) else Color(0xFF6366F1)
    val CardBg = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    val MutedText = if (isDarkMode) PureWhite.copy(alpha = 0.7f) else Color(0xFF64748B)

    val scrollState = rememberScrollState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showTipsDialog by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }

    // Track if logout is in progress
    var isLoggingOut by remember { mutableStateOf(false) }

    // LOCAL loading state for immediate UI feedback
    var isShowingLogoutLoading by remember { mutableStateOf(false) }

    LaunchedEffect(isLoggingOut) {
        if (isLoggingOut) {
            isShowingLogoutLoading = true

            viewModel.logout {
            }
            delay(2000)

            onLogout()
            isLoggingOut = false
            isShowingLogoutLoading = false
        }
    }

    var isFullLoggingOut by remember { mutableStateOf(false) }

    LaunchedEffect(isFullLoggingOut) {
        if (isFullLoggingOut) {
            isShowingLogoutLoading = true

            viewModel.fullLogout {
            }

            delay(2000)

            onSignOutAndStartFresh()
            isFullLoggingOut = false
            isShowingLogoutLoading = false
        }
    }

    if (isShowingLogoutLoading || state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgNavy),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    color = MoonYellow,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(if (isTablet) 48.dp else 40.dp)
                )
                Text(
                    text = "Signing out...",
                    color = PureWhite,
                    fontSize = if (isTablet) 18.sp else 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Please wait",
                    color = PureWhite.copy(alpha = 0.6f),
                    fontSize = if (isTablet) 14.sp else 12.sp
                )
            }
        }
        return
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

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = horizontalPadding)
                    .padding(top = topPadding)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hello, ${state.userName}!",
                            color = PureWhite,
                            fontSize = if (isTablet) 26.sp else 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (state.isGuest) {
                            Text(
                                text = "Guest Mode • Data saved locally",
                                color = MoonYellow.copy(alpha = 0.8f),
                                fontSize = if (isTablet) 12.sp else 10.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(if (isTablet) 12.dp else 8.dp)
                    ) {
                        // Timer Icon Button
                        Box(
                            modifier = Modifier
                                .size(if (isTablet) 46.dp else 38.dp)
                                .clip(CircleShape)
                                .background(PureWhite.copy(alpha = 0.08f))
                                .clickable { showTimerDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⏳", fontSize = if (isTablet) 20.sp else 16.sp)
                        }

                        // Tips Icon Button
                        Box(
                            modifier = Modifier
                                .size(if (isTablet) 46.dp else 38.dp)
                                .clip(CircleShape)
                                .background(PureWhite.copy(alpha = 0.08f))
                                .clickable { showTipsDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💡", fontSize = if (isTablet) 20.sp else 16.sp)
                        }

                        // Profile Avatar with dropdown menu
                        ProfileMenu(
                            isGuest = state.isGuest,
                            userName = state.userName,
                            isTablet = isTablet,
                            onLogout = { showLogoutDialog = true },
                            accentPurple = AccentPurple,
                            pureWhite = PureWhite,
                            cardBg = CardBg
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (isTablet) 28.dp else 20.dp))

                // Streak Card
                StreakCard(
                    streakCount = state.streakCount,
                    isTablet = isTablet,
                    pureWhite = PureWhite,
                    moonYellow = MoonYellow,
                    cardBg = CardBg
                )

                Spacer(modifier = Modifier.height(if (isTablet) 24.dp else 18.dp))

                // Empty State or Progress Cards
                if (!state.hasAnyTasks && !state.isLoading) {
                    WelcomeCard(
                        userName = state.userName,
                        isTablet = isTablet,
                        onNavigateToSet = onNavigateToSet,
                        moonYellow = MoonYellow,
                        pureWhite = PureWhite,
                        bgNavy = BgNavy,
                        cardBg = CardBg
                    )
                } else {
                    // Today's Progress & Tomorrow's Prep
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ProgressCard(
                                progress = state.todayProgress,
                                done = state.todayTasksDone,
                                total = state.todayTasksTotal,
                                isTablet = isTablet,
                                pureWhite = PureWhite,
                                moonYellow = MoonYellow,
                                cardBg = CardBg
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            TomorrowCard(
                                count = state.tomorrowTasksTotal,
                                isTablet = isTablet,
                                pureWhite = PureWhite,
                                moonYellow = MoonYellow,
                                cardBg = CardBg
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(if (isTablet) 16.dp else 12.dp))

                // Analytics Cards: Weekly Consistency & Lifetime Wins
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ConsistencyCard(
                            rate = if (state.isGuest) 0f else state.weeklyCompletionRate,
                            isTablet = isTablet,
                            pureWhite = PureWhite,
                            cardBg = CardBg,
                            isGuest = state.isGuest
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        LifetimeCard(
                            count = if (state.isGuest) 0 else state.lifetimeCompletedTasks,
                            isTablet = isTablet,
                            pureWhite = PureWhite,
                            cardBg = CardBg,
                            isGuest = state.isGuest
                        )
                    }
                }

                // Guest Info Banner
                if (state.isGuest) {
                    Spacer(modifier = Modifier.height(if (isTablet) 20.dp else 14.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToUpgrade() },
                        colors = CardDefaults.cardColors(
                            containerColor = MoonYellow.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(if (isTablet) 16.dp else 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "💾 Guest Data Saved Locally",
                                    color = MoonYellow,
                                    fontSize = if (isTablet) 14.sp else 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Tap to upgrade & sync to cloud",
                                    color = MutedText,
                                    fontSize = if (isTablet) 12.sp else 10.sp
                                )
                            }
                            Text(
                                text = "Upgrade ➔",
                                color = MoonYellow,
                                fontSize = if (isTablet) 14.sp else 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(if (isTablet) 28.dp else 20.dp))

                // Action Buttons
                Column(
                    verticalArrangement = Arrangement.spacedBy(if (isTablet) 14.dp else 10.dp)
                ) {
                    Button(
                        onClick = { onNavigateToSet() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isTablet) 58.dp else 50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MoonYellow,
                            contentColor = if (isDarkMode) BgNavy else Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Set Tasks for Tomorrow",
                            fontSize = if (isTablet) 18.sp else 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { onNavigateToLog() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isTablet) 50.dp else 44.dp),
                        enabled = state.todayTasksTotal > 0,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PureWhite,
                            disabledContentColor = PureWhite.copy(alpha = 0.3f)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 2.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (state.todayTasksTotal > 0) PureWhite.copy(alpha = 0.3f)
                                else PureWhite.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (state.todayTasksTotal > 0) "Log Today's Tasks" else "No Tasks to Log Today",
                            fontSize = if (isTablet) 16.sp else 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (isTablet) 24.dp else 18.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
                    .padding(bottom = bottomPadding)
            ) {
                BottomNavigationBar(
                    isTablet = isTablet,
                    isDarkMode = isDarkMode,
                    selectedIndex = 0,
                    onItemSelected = { index ->
                        when (index) {
                            0 -> { }
                            1 -> onNavigateToSet()
                            2 -> onNavigateToLog()
                            3 -> onNavigateToHistory()
                            4 -> onNavigateToSettings()
                        }
                    }
                )
            }
        }

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            LogoutDialog(
                isGuest = state.isGuest,
                onDismiss = { showLogoutDialog = false },
                onSaveAndExit = {
                    showLogoutDialog = false
                    isLoggingOut = true
                },
                onClearAndExit = {
                    showLogoutDialog = false
                    isFullLoggingOut = true
                },
                onSimpleLogout = {
                    showLogoutDialog = false
                    isLoggingOut = true
                },
                pureWhite = PureWhite,
                cardBg = CardBg
            )
        }

        // Tips Dialog
        if (showTipsDialog) {
            TipsDialog(
                onDismiss = { showTipsDialog = false },
                pureWhite = PureWhite,
                cardBg = CardBg,
                moonYellow = MoonYellow
            )
        }

        // Productivity Timer Dialog
        if (showTimerDialog) {
            TimerDialog(
                onDismiss = { showTimerDialog = false },
                pureWhite = PureWhite,
                cardBg = CardBg,
                moonYellow = MoonYellow
            )
        }

        // Show pending toast if any
        TaskNightToast(
            message = pendingToastMessage ?: "",
            isVisible = pendingToastMessage != null,
            onDismiss = onToastShown
        )
    }
}

@Composable
fun WelcomeCard(
    userName: String,
    isTablet: Boolean,
    onNavigateToSet: () -> Unit,
    moonYellow: Color,
    pureWhite: Color,
    bgNavy: Color,
    cardBg: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        shape = RoundedCornerShape(if (isTablet) 22.dp else 18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isTablet) 28.dp else 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🚀",
                fontSize = if (isTablet) 48.sp else 40.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Welcome, $userName!",
                color = pureWhite,
                fontSize = if (isTablet) 22.sp else 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your journey to better productivity starts here. Set your first tasks for tomorrow!",
                color = pureWhite.copy(alpha = 0.7f),
                fontSize = if (isTablet) 16.sp else 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onNavigateToSet,
                colors = ButtonDefaults.buttonColors(
                    containerColor = moonYellow,
                    contentColor = bgNavy
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Set Tomorrow's Tasks",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TipsDialog(
    onDismiss: () -> Unit,
    pureWhite: Color,
    cardBg: Color,
    moonYellow: Color
) {
    val allTips = listOf(
        "Use the 2-minute rule: If a task takes less than 2 minutes, do it now.",
        "Eat the frog: Tackle your most difficult task first thing in the morning.",
        "Try the Pomodoro technique: 25 minutes of work followed by a 5-minute break.",
        "Batch similar tasks together to maintain flow and reduce context switching.",
        "Clear your workspace every evening to start fresh the next day.",
        "Write down your 'Why' for every task to stay motivated.",
        "Avoid multitasking; focus on one task at a time for better quality.",
        "Review your wins at the end of every day, no matter how small.",
        "Set firm boundaries for your work hours to avoid burnout.",
        "Take short walks to clear your mind when you're feeling stuck.",
        "Hydrate! Your brain needs water to function at its best.",
        "Use 'No' as a tool to protect your time and energy.",
        "Prioritize tasks using the Eisenhower Matrix: Urgent vs Important.",
        "Limit notifications to only the essentials during focus time.",
        "Get 7-8 hours of sleep; productivity starts with rest."
    )

    val randomTips = remember { allTips.shuffled().take(5) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💡", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Productivity Tips", color = pureWhite)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                randomTips.forEach { tip ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("•", color = moonYellow, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(tip, color = pureWhite.copy(alpha = 0.8f), fontSize = 14.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it!", color = moonYellow)
            }
        },
        containerColor = cardBg,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun TimerDialog(
    onDismiss: () -> Unit,
    pureWhite: Color,
    cardBg: Color,
    moonYellow: Color
) {
    var timeLeft by remember { mutableStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
            }
            isRunning = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⏳", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Focus Timer", color = pureWhite)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val minutes = timeLeft / 60
                val seconds = timeLeft % 60
                Text(
                    text = String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds),
                    color = pureWhite,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { isRunning = !isRunning },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) Color.Red.copy(alpha = 0.7f) else moonYellow,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(if (isRunning) "Pause" else "Start")
                    }
                    OutlinedButton(
                        onClick = {
                            isRunning = false
                            timeLeft = 25 * 60
                        },
                        border = androidx.compose.foundation.BorderStroke(1.dp, pureWhite.copy(alpha = 0.3f))
                    ) {
                        Text("Reset", color = pureWhite)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = pureWhite.copy(alpha = 0.7f))
            }
        },
        containerColor = cardBg,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun LogoutDialog(
    isGuest: Boolean,
    onDismiss: () -> Unit,
    onSaveAndExit: () -> Unit,
    onClearAndExit: () -> Unit,
    onSimpleLogout: () -> Unit,
    pureWhite: Color,
    cardBg: Color
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isGuest) "Exit Guest Mode?" else "Sign Out?",
                color = pureWhite
            )
        },
        text = {
            if (isGuest) {
                Column {
                    Text(
                        text = "Choose how you want to exit:",
                        color = pureWhite.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Option 1: Save and exit
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSaveAndExit() },
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF22C55E).copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💾", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Save & Exit",
                                    color = Color(0xFF4ADE80),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Your data will be restored next time",
                                    color = pureWhite.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Option 2: Clear and exit
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClearAndExit() },
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFEF4444).copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🗑️", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Clear & Exit",
                                    color = Color(0xFFF87171),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Start fresh next time",
                                    color = pureWhite.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Are you sure you want to sign out?",
                    color = pureWhite.copy(alpha = 0.8f)
                )
            }
        },
        confirmButton = {
            if (!isGuest) {
                TextButton(onClick = onSimpleLogout) {
                    Text("Sign Out", color = Color(0xFFEF4444))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = pureWhite.copy(alpha = 0.7f))
            }
        },
        containerColor = cardBg,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun ProfileMenu(
    isGuest: Boolean,
    userName: String,
    isTablet: Boolean,
    onLogout: () -> Unit,
    accentPurple: Color,
    pureWhite: Color,
    cardBg: Color
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Box(
            modifier = Modifier
                .size(if (isTablet) 50.dp else 42.dp)
                .clip(CircleShape)
                .background(accentPurple)
                .clickable { expanded = true },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userName.take(1).uppercase(),
                color = pureWhite,
                fontSize = if (isTablet) 22.sp else 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(cardBg),
            containerColor = cardBg
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        if (isGuest) "Exit Guest Mode" else "Sign Out",
                        color = Color(0xFFEF4444)
                    )
                },
                onClick = {
                    expanded = false
                    onLogout()
                },
                leadingIcon = {
                    Text("🚪", fontSize = 16.sp)
                }
            )
        }
    }
}

@Composable
fun StreakCard(
    streakCount: Int,
    isTablet: Boolean,
    pureWhite: Color,
    moonYellow: Color,
    cardBg: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "streakPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(pulseScale),
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        shape = RoundedCornerShape(if (isTablet) 22.dp else 18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isTablet) 22.dp else 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Current Streak",
                    color = pureWhite.copy(alpha = 0.7f),
                    fontSize = if (isTablet) 15.sp else 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "$streakCount",
                        color = moonYellow,
                        fontSize = if (isTablet) 42.sp else 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " days",
                        color = pureWhite.copy(alpha = 0.7f),
                        fontSize = if (isTablet) 16.sp else 13.sp,
                        modifier = Modifier.padding(start = 6.dp, bottom = 5.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(if (isTablet) 56.dp else 42.dp)
                    .clip(CircleShape)
                    .background(moonYellow.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔥",
                    fontSize = if (isTablet) 32.sp else 24.sp
                )
            }
        }
    }
}

@Composable
fun ProgressCard(
    progress: Float,
    done: Int,
    total: Int,
    isTablet: Boolean,
    pureWhite: Color,
    moonYellow: Color,
    cardBg: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        shape = RoundedCornerShape(if (isTablet) 22.dp else 18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isTablet) 18.dp else 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Today",
                color = pureWhite.copy(alpha = 0.7f),
                fontSize = if (isTablet) 15.sp else 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(if (isTablet) 14.dp else 10.dp))

            Box(
                modifier = Modifier
                    .size(if (isTablet) 80.dp else 60.dp),
                contentAlignment = Alignment.Center
            ) {
                if (total > 0) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = if (isTablet) 7.dp.toPx() else 5.dp.toPx()
                        drawCircle(
                            color = pureWhite.copy(alpha = 0.1f),
                            style = Stroke(width = strokeWidth)
                        )
                        drawArc(
                            color = moonYellow,
                            startAngle = -90f,
                            sweepAngle = progress * 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        color = pureWhite,
                        fontSize = if (isTablet) 18.sp else 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(pureWhite.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "☕",
                            fontSize = if (isTablet) 32.sp else 24.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (isTablet) 12.dp else 8.dp))

            Text(
                text = if (total > 0) "$done/$total Done" else "No Tasks",
                color = pureWhite,
                fontSize = if (isTablet) 14.sp else 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TomorrowCard(
    count: Int,
    isTablet: Boolean,
    pureWhite: Color,
    moonYellow: Color,
    cardBg: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        shape = RoundedCornerShape(if (isTablet) 22.dp else 18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isTablet) 18.dp else 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tomorrow",
                color = pureWhite.copy(alpha = 0.7f),
                fontSize = if (isTablet) 15.sp else 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(if (isTablet) 14.dp else 10.dp))

            Box(
                modifier = Modifier
                    .size(if (isTablet) 80.dp else 60.dp)
                    .clip(CircleShape)
                    .background(moonYellow.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (count > 0) "🌙" else "➕",
                    fontSize = if (isTablet) 32.sp else 24.sp
                )
            }

            Spacer(modifier = Modifier.height(if (isTablet) 12.dp else 8.dp))

            Text(
                text = if (count > 0) "$count Set" else "Not Set",
                color = if (count > 0) pureWhite else pureWhite.copy(alpha = 0.5f),
                fontSize = if (isTablet) 14.sp else 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ConsistencyCard(
    rate: Float,
    isTablet: Boolean,
    pureWhite: Color,
    cardBg: Color,
    isGuest: Boolean = false
) {
    val SuccessGreen = Color(0xFF10B981)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        shape = RoundedCornerShape(if (isTablet) 22.dp else 18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isGuest) Modifier.blur(4.dp) else Modifier)
                    .padding(if (isTablet) 18.dp else 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Consistency",
                    color = pureWhite.copy(alpha = 0.7f),
                    fontSize = if (isTablet) 15.sp else 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(if (isTablet) 14.dp else 10.dp))

                Box(
                    modifier = Modifier
                        .size(if (isTablet) 80.dp else 60.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📈",
                        fontSize = if (isTablet) 32.sp else 24.sp
                    )
                }

                Spacer(modifier = Modifier.height(if (isTablet) 12.dp else 8.dp))

                Text(
                    text = "${(rate * 100).toInt()}% Week",
                    color = pureWhite,
                    fontSize = if (isTablet) 14.sp else 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isGuest) {
                Text(
                    text = "🔒",
                    modifier = Modifier.size(20.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun LifetimeCard(
    count: Int,
    isTablet: Boolean,
    pureWhite: Color,
    cardBg: Color,
    isGuest: Boolean = false
) {
    val Gold = Color(0xFFFBBF24)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        shape = RoundedCornerShape(if (isTablet) 22.dp else 18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isGuest) Modifier.blur(4.dp) else Modifier)
                    .padding(if (isTablet) 18.dp else 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Lifetime",
                    color = pureWhite.copy(alpha = 0.7f),
                    fontSize = if (isTablet) 15.sp else 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(if (isTablet) 14.dp else 10.dp))

                Box(
                    modifier = Modifier
                        .size(if (isTablet) 80.dp else 60.dp)
                        .clip(CircleShape)
                        .background(Gold.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🏆",
                        fontSize = if (isTablet) 32.sp else 24.sp
                    )
                }

                Spacer(modifier = Modifier.height(if (isTablet) 12.dp else 8.dp))

                Text(
                    text = "$count Wins",
                    color = pureWhite,
                    fontSize = if (isTablet) 14.sp else 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isGuest) {
                Text(
                    text = "🔒",
                    modifier = Modifier.size(20.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    isTablet: Boolean,
    isDarkMode: Boolean = true,
    selectedIndex: Int = 0,
    onItemSelected: (Int) -> Unit = {}
) {
    val InactiveColor = if (isDarkMode) Color(0xFF99A1AF) else Color(0xFF64748B)
    val ActiveColor = Color(0xFF10B981)
    val BackgroundColor = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF)

    val navBarHeight = if (isTablet) 72.dp else 56.dp
    val cornerRadius = if (isTablet) 24.dp else 16.dp
    val iconSize = if (isTablet) 22.dp else 18.dp
    val labelSize = if (isTablet) 11.sp else 9.sp
    val iconTextGap = if (isTablet) 3.dp else 1.8.dp
    val verticalPadding = if (isTablet) 10.dp else 8.1.dp
    val horizontalPadding = if (isTablet) 24.dp else 16.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(navBarHeight)
            .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius, bottomStart = cornerRadius, bottomEnd = cornerRadius))
            .background(BackgroundColor)
            .padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Home
        NavItemSimple(
            icon = { HomeIcon(size = iconSize, isSelected = selectedIndex == 0) },
            label = "Home",
            isSelected = selectedIndex == 0,
            iconSize = iconSize,
            labelSize = labelSize,
            gap = iconTextGap,
            verticalPadding = verticalPadding,
            activeColor = ActiveColor,
            inactiveColor = InactiveColor,
            onClick = { onItemSelected(0) }
        )

        // Set
        NavItemSimple(
            icon = { SetIcon(size = iconSize, isSelected = selectedIndex == 1) },
            label = "Set",
            isSelected = selectedIndex == 1,
            iconSize = iconSize,
            labelSize = labelSize,
            gap = iconTextGap,
            verticalPadding = verticalPadding,
            activeColor = ActiveColor,
            inactiveColor = InactiveColor,
            onClick = { onItemSelected(1) }
        )

        // Log
        NavItemSimple(
            icon = { LogIcon(size = iconSize, isSelected = selectedIndex == 2) },
            label = "Log",
            isSelected = selectedIndex == 2,
            iconSize = iconSize,
            labelSize = labelSize,
            gap = iconTextGap,
            verticalPadding = verticalPadding,
            activeColor = ActiveColor,
            inactiveColor = InactiveColor,
            onClick = { onItemSelected(2) }
        )

        // History
        NavItemSimple(
            icon = { HistoryIcon(size = iconSize, isSelected = selectedIndex == 3) },
            label = "History",
            isSelected = selectedIndex == 3,
            iconSize = iconSize,
            labelSize = labelSize,
            gap = iconTextGap,
            verticalPadding = verticalPadding,
            activeColor = ActiveColor,
            inactiveColor = InactiveColor,
            onClick = { onItemSelected(3) }
        )

        // Settings
        NavItemSimple(
            icon = { SettingsIcon(size = iconSize, isSelected = selectedIndex == 4) },
            label = "Settings",
            isSelected = selectedIndex == 4,
            iconSize = iconSize,
            labelSize = labelSize,
            gap = iconTextGap,
            verticalPadding = verticalPadding,
            activeColor = ActiveColor,
            inactiveColor = InactiveColor,
            onClick = { onItemSelected(4) }
        )
    }
}

@Composable
fun NavItemSimple(
    icon: @Composable () -> Unit,
    label: String,
    isSelected: Boolean,
    iconSize: androidx.compose.ui.unit.Dp,
    labelSize: androidx.compose.ui.unit.TextUnit,
    gap: androidx.compose.ui.unit.Dp,
    verticalPadding: androidx.compose.ui.unit.Dp,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(vertical = verticalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(iconSize),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(modifier = Modifier.height(gap))
        Text(
            text = label,
            color = if (isSelected) activeColor else inactiveColor,
            fontSize = labelSize,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            letterSpacing = 0.105469.sp,
            lineHeight = (labelSize.value * 1.5).sp
        )
    }
}

// Home Icon
@Composable
fun HomeIcon(size: androidx.compose.ui.unit.Dp, isSelected: Boolean) {
    val color = if (isSelected) Color(0xFF10B981) else Color(0xFF99A1AF)
    Icon(
        painter = painterResource(id = R.drawable.ic_home),
        contentDescription = "Home",
        modifier = Modifier.size(size),
        tint = color
    )
}

// Set Icon (Moon)
@Composable
fun SetIcon(size: androidx.compose.ui.unit.Dp, isSelected: Boolean) {
    val color = if (isSelected) Color(0xFF10B981) else Color(0xFF99A1AF)
    Icon(
        painter = painterResource(id = R.drawable.ic_moon),
        contentDescription = "Set",
        modifier = Modifier.size(size),
        tint = color
    )
}

// Log Icon (Checkmark)
@Composable
fun LogIcon(size: androidx.compose.ui.unit.Dp, isSelected: Boolean) {
    val color = if (isSelected) Color(0xFF10B981) else Color(0xFF99A1AF)
    Icon(
        painter = painterResource(id = R.drawable.ic_check),
        contentDescription = "Log",
        modifier = Modifier.size(size),
        tint = color
    )
}

// History Icon (Calendar)
@Composable
fun HistoryIcon(size: androidx.compose.ui.unit.Dp, isSelected: Boolean) {
    val color = if (isSelected) Color(0xFF10B981) else Color(0xFF99A1AF)
    Icon(
        painter = painterResource(id = R.drawable.ic_calendar),
        contentDescription = "History",
        modifier = Modifier.size(size),
        tint = color
    )
}

// Settings Icon
@Composable
fun SettingsIcon(size: androidx.compose.ui.unit.Dp, isSelected: Boolean) {
    val color = if (isSelected) Color(0xFF10B981) else Color(0xFF99A1AF)
    Icon(
        painter = painterResource(id = R.drawable.ic_settings),
        contentDescription = "Settings",
        modifier = Modifier.size(size),
        tint = color
    )
}