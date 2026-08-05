package com.example.tasknight.ui.screens.history

import android.app.DatePickerDialog
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasknight.R
import com.example.tasknight.presentation.history.HistoryViewModel
import com.example.tasknight.ui.screens.dashboard.BottomNavigationBar
import com.example.tasknight.ui.theme.InterFontFamily
import com.example.tasknight.domain.models.Task
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

import androidx.compose.ui.draw.blur
import androidx.compose.ui.zIndex

@Composable
fun HistoryScreen(
    isDarkMode: Boolean = true,
    onNavigateToHome: () -> Unit = {},
    onNavigateToSet: () -> Unit = {},
    onNavigateToLog: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
    viewModel: HistoryViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val isGuest = state.isGuest
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isTablet = screenWidth >= 600.dp

    val horizontalPadding = if (isTablet) 32.dp else 20.dp
    val topPadding = if (screenHeight < 700.dp) 16.dp else 24.dp
    val bottomPadding = if (screenHeight < 700.dp) 16.dp else 24.dp
    val cardSpacing = if (isTablet) 20.dp else 16.dp
    val cardCornerRadius = if (isTablet) 20.dp else 16.dp

    val BgNavy = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val CardBg = if (isDarkMode) Color(0xFF1C212B) else Color(0xFFFFFFFF)
    val PureWhite = if (isDarkMode) Color(0xFFFFFFFF) else Color(0xFF1E293B)
    val MutedGrey = if (isDarkMode) Color(0xFF8B949E) else Color(0xFF64748B)
    val AccentGreen = Color(0xFF10B981)
    val AccentOrange = Color(0xFFF59E0B)
    val AccentRed = Color(0xFFEF4444)
    val AccentPurple = Color(0xFF8B5CF6)
    val FlameOrange = Color(0xFFF97316)

    val scrollState = rememberScrollState()
    val context = LocalContext.current

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
                            Color(0xFF1E3A8A).copy(alpha = 0.3f),
                            BgNavy
                        )
                    )
                )
        )

        // Main Column with sticky bottom navigation
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(if (isGuest) Modifier.blur(8.dp) else Modifier)
                    .verticalScroll(scrollState)
                    .padding(horizontal = horizontalPadding)
                    .padding(top = topPadding)
            ) {
                // Header
                HistoryHeader(
                    daysLogged = if (isGuest) 0 else state.daysLogged,
                    isTablet = isTablet,
                    titleColor = PureWhite,
                    subtitleColor = MutedGrey
                )

                Spacer(modifier = Modifier.height(cardSpacing))

                // Stats Cards Row
                StatsRow(
                    streakCount = if (isGuest) 0 else state.streakCount,
                    tasksDone = if (isGuest) 0 else state.tasksDone,
                    daysLogged = if (isGuest) 0 else state.daysLogged,
                    isTablet = isTablet,
                    cardBg = CardBg,
                    pureWhite = PureWhite,
                    mutedGrey = MutedGrey,
                    accentGreen = AccentGreen,
                    flameOrange = FlameOrange,
                    accentPurple = AccentPurple
                )

                Spacer(modifier = Modifier.height(cardSpacing))

                // Calendar Card
                CalendarCard(
                    currentYearMonth = state.currentYearMonth,
                    completedDates = if (isGuest) emptySet() else state.completedDates,
                    partialDates = if (isGuest) emptySet() else state.partialDates,
                    missedDates = if (isGuest) emptySet() else state.missedDates,
                    selectedDate = state.selectedDate,
                    isTablet = isTablet,
                    cardBg = CardBg,
                    pureWhite = PureWhite,
                    mutedGrey = MutedGrey,
                    accentGreen = AccentGreen,
                    accentOrange = AccentOrange,
                    accentRed = AccentRed,
                    cornerRadius = cardCornerRadius,
                    onDateSelected = { date -> if (!isGuest) viewModel.selectDate(date) },
                    onMonthChanged = { yearMonth -> if (!isGuest) viewModel.changeMonth(yearMonth) },
                    context = context
                )

                Spacer(modifier = Modifier.height(cardSpacing))

                // Detail View Card
                DetailViewCard(
                    selectedDate = state.selectedDate,
                    tasksForDate = if (isGuest) emptyList() else state.tasksForSelectedDate,
                    reflection = if (isGuest) null else state.reflectionForSelectedDate,
                    isTablet = isTablet,
                    cardBg = CardBg,
                    pureWhite = PureWhite,
                    mutedGrey = MutedGrey,
                    accentGreen = AccentGreen,
                    cornerRadius = cardCornerRadius
                )

                // Extra padding
                Spacer(modifier = Modifier.height(if (isTablet) 24.dp else 18.dp))
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
                    selectedIndex = 3, // History is selected
                    onItemSelected = { index ->
                        when (index) {
                            0 -> onNavigateToHome()
                            1 -> onNavigateToSet()
                            2 -> onNavigateToLog()
                            3 -> { /* Already on History */ }
                            4 -> onNavigateToSettings()
                        }
                    }
                )
            }
        }

        // Guest Overlay
        if (isGuest) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp)
                    .zIndex(1f),
                contentAlignment = Alignment.Center
            ) {
                // Background overlay for content behind the card
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgNavy.copy(alpha = 0.9f))
                )
                Card(
                    modifier = Modifier
                        .padding(horizontalPadding)
                        .fillMaxWidth(if (isTablet) 0.7f else 1f),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🔒 Premium Feature",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "History and analytics are only available for registered accounts. Create an account to track your progress over time.",
                            fontSize = 16.sp,
                            color = MutedGrey,
                            textAlign = TextAlign.Center,
                            fontFamily = InterFontFamily
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = AccentOrange.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentOrange)
                        ) {
                            Text(
                                text = "Create your account to access this feature",
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                                fontWeight = FontWeight.Bold,
                                color = AccentOrange,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryHeader(
    daysLogged: Int,
    isTablet: Boolean,
    titleColor: Color,
    subtitleColor: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "History",
            color = titleColor,
            fontSize = if (isTablet) 36.sp else 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = InterFontFamily
        )
        Text(
            text = "$daysLogged days logged",
            color = subtitleColor,
            fontSize = if (isTablet) 16.sp else 14.sp,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun StatsRow(
    streakCount: Int,
    tasksDone: Int,
    daysLogged: Int,
    isTablet: Boolean,
    cardBg: Color,
    pureWhite: Color,
    mutedGrey: Color,
    accentGreen: Color,
    flameOrange: Color,
    accentPurple: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (isTablet) 12.dp else 8.dp)
    ) {
        // Streak Card
        StatTile(
            icon = "🔥",
            label = "Streak",
            value = streakCount.toString(),
            modifier = Modifier.weight(1f),
            cardBg = cardBg,
            pureWhite = pureWhite,
            mutedGrey = mutedGrey,
            accentColor = flameOrange,
            isTablet = isTablet
        )

        // Done Card
        StatTile(
            icon = "📈",
            label = "Done",
            value = tasksDone.toString(),
            modifier = Modifier.weight(1f),
            cardBg = cardBg,
            pureWhite = pureWhite,
            mutedGrey = mutedGrey,
            accentColor = accentGreen,
            isTablet = isTablet
        )

        // Days Card
        StatTile(
            icon = "🎯",
            label = "Days",
            value = daysLogged.toString(),
            modifier = Modifier.weight(1f),
            cardBg = cardBg,
            pureWhite = pureWhite,
            mutedGrey = mutedGrey,
            accentColor = accentPurple,
            isTablet = isTablet
        )
    }
}

@Composable
fun StatTile(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    cardBg: Color,
    pureWhite: Color,
    mutedGrey: Color,
    accentColor: Color,
    isTablet: Boolean
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(if (isTablet) 16.dp else 14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isTablet) 14.dp else 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                fontSize = if (isTablet) 28.sp else 22.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = mutedGrey,
                fontSize = if (isTablet) 13.sp else 11.sp,
                fontFamily = InterFontFamily
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = accentColor,
                fontSize = if (isTablet) 28.sp else 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = InterFontFamily
            )
        }
    }
}

@Composable
fun CalendarCard(
    currentYearMonth: YearMonth,
    completedDates: Set<LocalDate>,
    partialDates: Set<LocalDate>,
    missedDates: Set<LocalDate>,
    selectedDate: LocalDate?,
    isTablet: Boolean,
    cardBg: Color,
    pureWhite: Color,
    mutedGrey: Color,
    accentGreen: Color,
    accentOrange: Color,
    accentRed: Color,
    cornerRadius: androidx.compose.ui.unit.Dp,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit,
    context: android.content.Context
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selected = LocalDate.of(year, month + 1, dayOfMonth)
            onMonthChanged(YearMonth.from(selected))
            onDateSelected(selected)
        },
        currentYearMonth.year,
        currentYearMonth.monthValue - 1,
        selectedDate?.dayOfMonth ?: 1
    )

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
            // Month Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentYearMonth.format(dateFormatter),
                        color = pureWhite,
                        fontSize = if (isTablet) 18.sp else 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = InterFontFamily
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { onMonthChanged(currentYearMonth.minusMonths(1)) },
                        modifier = Modifier.size(if (isTablet) 40.dp else 32.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chevron_left),
                            contentDescription = "Previous Month",
                            tint = mutedGrey,
                            modifier = Modifier.size(if (isTablet) 20.dp else 16.dp)
                        )
                    }

                    IconButton(
                        onClick = { datePickerDialog.show() },
                        modifier = Modifier.size(if (isTablet) 40.dp else 32.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_calendar),
                            contentDescription = "Select Date",
                            tint = pureWhite,
                            modifier = Modifier.size(if (isTablet) 22.dp else 18.dp)
                        )
                    }

                    IconButton(
                        onClick = { onMonthChanged(currentYearMonth.plusMonths(1)) },
                        modifier = Modifier.size(if (isTablet) 40.dp else 32.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chevron_right),
                            contentDescription = "Next Month",
                            tint = mutedGrey,
                            modifier = Modifier.size(if (isTablet) 20.dp else 16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (isTablet) 20.dp else 16.dp))

            // Day Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                dayLabels.forEach { day ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            color = mutedGrey,
                            fontSize = if (isTablet) 14.sp else 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = InterFontFamily
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (isTablet) 12.dp else 8.dp))

            // Calendar Grid
            val dates = generateCalendarDates(currentYearMonth)

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(if (isTablet) 280.dp else 240.dp),
                verticalArrangement = Arrangement.spacedBy(if (isTablet) 8.dp else 6.dp),
                horizontalArrangement = Arrangement.spacedBy(if (isTablet) 8.dp else 6.dp)
            ) {
                items(dates) { date ->
                    CalendarDay(
                        date = date,
                        isCompleted = date?.let { completedDates.contains(it) } == true,
                        isPartial = date?.let { partialDates.contains(it) } == true,
                        isMissed = date?.let { missedDates.contains(it) } == true,
                        isSelected = date?.let { selectedDate == it } == true,
                        isTablet = isTablet,
                        pureWhite = pureWhite,
                        mutedGrey = mutedGrey,
                        accentGreen = accentGreen,
                        accentOrange = accentOrange,
                        accentRed = accentRed,
                        onClick = { date?.let { onDateSelected(it) } }
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarDay(
    date: LocalDate?,
    isCompleted: Boolean,
    isPartial: Boolean,
    isMissed: Boolean,
    isSelected: Boolean,
    isTablet: Boolean,
    pureWhite: Color,
    mutedGrey: Color,
    accentGreen: Color,
    accentOrange: Color,
    accentRed: Color,
    onClick: () -> Unit
) {
    val today = LocalDate.now()
    val isToday = date == today

    val backgroundColor = when {
        date == null -> Color.Transparent
        isToday -> Color(0xFF64748B) // Neo-brutalism
        isCompleted -> accentGreen
        isPartial -> accentOrange
        isMissed -> accentRed
        else -> pureWhite.copy(alpha = 0.05f)
    }

    val textColor = when {
        date == null -> Color.Transparent
        isToday -> Color.Black
        isCompleted || isPartial || isMissed -> pureWhite
        else -> pureWhite.copy(alpha = 0.8f)
    }

    val borderModifier = when {
        isToday && date != null -> Modifier.border(2.dp, Color.Black, RoundedCornerShape(if (isTablet) 12.dp else 8.dp))
        isSelected && date != null -> Modifier.border(2.dp, pureWhite, RoundedCornerShape(if (isTablet) 12.dp else 8.dp))
        else -> Modifier
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .then(
                if (isToday && date != null) {
                    Modifier.graphicsLayer {
                        this.translationX = (-2).dp.toPx()
                        this.translationY = (-2).dp.toPx()
                        this.shadowElevation = 0f
                    }
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        RoundedCornerShape(if (isTablet) 12.dp else 8.dp)
                    )
                    .offset(x = 4.dp, y = 4.dp)
                } else Modifier
            )
            .clip(RoundedCornerShape(if (isTablet) 12.dp else 8.dp))
            .background(backgroundColor)
            .then(borderModifier)
            .clickable(enabled = date != null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    color = textColor,
                    fontSize = if (isTablet) 14.sp else 12.sp,
                    fontWeight = if (isCompleted || isPartial || isToday) FontWeight.Bold else FontWeight.Normal,
                    fontFamily = InterFontFamily
                )
            }
        }
    }
}

@Composable
fun DetailViewCard(
    selectedDate: LocalDate?,
    tasksForDate: List<Task>,
    reflection: com.example.tasknight.domain.models.DailyReflection?,
    isTablet: Boolean,
    cardBg: Color,
    pureWhite: Color,
    mutedGrey: Color,
    accentGreen: Color,
    cornerRadius: androidx.compose.ui.unit.Dp
) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")

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
            if (selectedDate != null) {
                Text(
                    text = selectedDate.format(dateFormatter),
                    color = pureWhite,
                    fontSize = if (isTablet) 16.sp else 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = InterFontFamily
                )

                if (reflection != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        reflection.mood?.let {
                            Text(text = it.emoji, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        repeat(5) { index ->
                            Icon(
                                painter = painterResource(id = if (index < reflection.rating) R.drawable.ic_star_filled else R.drawable.ic_star_outline),
                                contentDescription = null,
                                tint = if (index < reflection.rating) Color(0xFFFFD700) else mutedGrey.copy(alpha = 0.3f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    if (reflection.dailyNote.isNotEmpty()) {
                        Text(
                            text = reflection.dailyNote,
                            color = pureWhite.copy(alpha = 0.7f),
                            fontSize = if (isTablet) 14.sp else 12.sp,
                            fontFamily = InterFontFamily,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (isTablet) 16.dp else 12.dp))

                if (tasksForDate.isNotEmpty()) {
                    tasksForDate.forEach { task ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = if (task.isCompleted) R.drawable.ic_check_circle else R.drawable.ic_moon),
                                    contentDescription = null,
                                    tint = if (task.isCompleted) accentGreen else mutedGrey,
                                    modifier = Modifier.size(if (isTablet) 18.dp else 14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = task.title,
                                    color = pureWhite.copy(alpha = 0.9f),
                                    fontSize = if (isTablet) 14.sp else 12.sp,
                                    fontFamily = InterFontFamily,
                                    textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                )
                            }
                            if (task.reflection.isNotEmpty()) {
                                Text(
                                    text = "💡 ${task.reflection}",
                                    color = mutedGrey,
                                    fontSize = if (isTablet) 12.sp else 10.sp,
                                    fontFamily = InterFontFamily,
                                    modifier = Modifier.padding(start = if (isTablet) 26.dp else 22.dp, top = 2.dp)
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📭",
                            fontSize = if (isTablet) 24.sp else 20.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "No tasks logged for this day",
                            color = mutedGrey,
                            fontSize = if (isTablet) 14.sp else 12.sp,
                            fontFamily = InterFontFamily
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📅",
                        fontSize = if (isTablet) 24.sp else 20.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Select a date to view details",
                        color = mutedGrey,
                        fontSize = if (isTablet) 14.sp else 12.sp,
                        fontFamily = InterFontFamily,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// Helper function to generate calendar dates
private fun generateCalendarDates(yearMonth: YearMonth): List<LocalDate?> {
    val firstOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = firstOfMonth.dayOfWeek.value % 7

    val dates = mutableListOf<LocalDate?>()

    repeat(firstDayOfWeek) {
        dates.add(null)
    }

    for (day in 1..daysInMonth) {
        dates.add(yearMonth.atDay(day))
    }

    while (dates.size < 42) {
        dates.add(null)
    }

    return dates
}