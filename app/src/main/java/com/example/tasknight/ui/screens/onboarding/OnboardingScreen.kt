package com.example.tasknight.ui.screens.onboarding
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.animation.core.*
import com.example.tasknight.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.Font
import kotlinx.coroutines.flow.first
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.example.tasknight.ui.screens.splash.FastOutSlowInEasing
val InterFontFamily = FontFamily(
    Font(R.font.inter_28pt_regular, FontWeight.Normal),
    Font(R.font.inter_28pt_bold, FontWeight.Bold)
)
data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: Int,
    val backgroundColor: Color
)
val onboardingPages = listOf(
    OnboardingPage(
        title = "Welcome to TaskNight",
        description = "Your personal night-time task planner to conquer tomorrow with confidence",
        icon = R.drawable.rocket,
        backgroundColor = Color(0xFF1E3A8A)
    ),
    OnboardingPage(
        title = "Plan Your Tasks",
        description = "Every night, set your goals for tomorrow. Break big tasks into manageable steps",
        icon = R.drawable.target_goals_icon,
        backgroundColor = Color(0xFF4A3299)
    ),
    OnboardingPage(
        title = "Track Progress",
        description = "Log completed tasks and watch your productivity soar day after day",
        icon = R.drawable.progress,
        backgroundColor = Color(0xFF6B21A8)
    ),
    OnboardingPage(
        title = "Build Your Streak",
        description = "Stay consistent and build an impressive streak. Small wins lead to big results",
        icon = R.drawable.hot_icon,
        backgroundColor = Color(0xFF0F172A)
    )
)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isTablet = screenWidth >= 600.dp
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val coroutineScope = rememberCoroutineScope()
    var resetTime by remember { mutableStateOf(0L) }
    var timerProgress by remember { mutableStateOf(0f) }
    var isTimerActive by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            snapshotFlow { pagerState.currentPageOffsetFraction }
                .first { it == 0f }
            isTimerActive = true
            timerProgress = 0f
            if (pagerState.currentPageOffsetFraction != 0f) {
                isTimerActive = false
                timerProgress = 0f
                break
            }
            isTimerActive = false
            val startTime = System.currentTimeMillis()
            while (true) {
                delay(16) 
                val elapsed = System.currentTimeMillis() - startTime
                timerProgress = (elapsed / 5000f).coerceIn(0f, 1f)
                if (pagerState.currentPageOffsetFraction != 0f) {
                    timerProgress = 0f
                    break
                }
                if (elapsed >= 5000) {
                    val nextPage = if (pagerState.currentPage == onboardingPages.size - 1) {
                        0
                    } else {
                        pagerState.currentPage + 1
                    }
                    pagerState.animateScrollToPage(
                        nextPage,
                        animationSpec = tween(
                            durationMillis = 400,
                            easing = FastOutSlowInEasing
                        )
                    )
                    timerProgress = 0f
                    break
                }
            }
        }
    }
    val horizontalPadding = if (isTablet) 48.dp else 32.dp
    val iconSize = if (isTablet) 180.dp else 120.dp
    val titleSize = if (isTablet) 42.sp else 32.sp
    val descriptionSize = if (isTablet) 20.sp else 16.sp
    val buttonHeight = if (isTablet) 56.dp else 48.dp
    val buttonTextSize = if (isTablet) 18.sp else 16.sp
    val PureWhite = Color(0xFFFFFFFF)
    val BgNavy = Color(0xFF0F172A)
    val easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgNavy)
        )
        val gradientColors = remember(pagerState.currentPage) {
            when (pagerState.currentPage) {
                0 -> listOf(Color(0xFF1E3A8A).copy(alpha = 0.85f), Color(0xFF2D1B69).copy(alpha = 0.85f))
                1 -> listOf(Color(0xFF4A3299).copy(alpha = 0.85f), Color(0xFF3B1F7A).copy(alpha = 0.85f))
                2 -> listOf(Color(0xFF6B21A8).copy(alpha = 0.85f), Color(0xFF4A1D8A).copy(alpha = 0.85f))
                else -> listOf(Color(0xFF0F172A).copy(alpha = 0.85f), Color(0xFF1E1B4B).copy(alpha = 0.85f))
            }
        }
        val backgroundGradient = Brush.verticalGradient(gradientColors)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
        ) {
            ModernBackgroundElements(
                currentPage = pagerState.currentPage,
                isTablet = isTablet,
                screenWidthValue = configuration.screenWidthDp.toFloat(),
                easing = easing
            )
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding, vertical = 16.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    TextButton(
                        onClick = onOnboardingComplete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = PureWhite.copy(alpha = 0.7f)
                        )
                    ) {
                        Text(
                            text = "Skip",
                            fontSize = if (isTablet) 18.sp else 16.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    OnboardingPageContentModern(
                        page = onboardingPages[page],
                        iconSize = iconSize,
                        titleSize = titleSize,
                        descriptionSize = descriptionSize,
                        horizontalPadding = horizontalPadding,
                        isTablet = isTablet,
                        easing = easing,
                        timerProgress = timerProgress,
                        isTimerActive = isTimerActive
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding)
                        .padding(bottom = if (isTablet) 40.dp else 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ModernDotsIndicator(
                        totalDots = onboardingPages.size,
                        currentPage = pagerState.currentPage,
                        isTablet = isTablet,
                        modifier = Modifier.padding(bottom = if (isTablet) 32.dp else 24.dp)
                    )
                    Button(
                        onClick = {
                            if (pagerState.currentPage < onboardingPages.size - 1) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                onOnboardingComplete()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(buttonHeight),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PureWhite,
                            contentColor = onboardingPages[pagerState.currentPage].backgroundColor
                        ),
                        shape = RoundedCornerShape(buttonHeight / 2),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 4.dp
                        )
                    ) {
                        Text(
                            text = if (pagerState.currentPage < onboardingPages.size - 1) "Next" else "Get Started",
                            fontSize = buttonTextSize,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun ModernBackgroundElements(
    currentPage: Int,
    isTablet: Boolean,
    screenWidthValue: Float,
    easing: Easing
) {
    val PureWhite = Color(0xFFFFFFFF)
    val bubble1Offset by animateFloatAsState(
        targetValue = when (currentPage) {
            0 -> 0f
            1 -> 50f
            2 -> -30f
            else -> 20f
        },
        animationSpec = tween(800, easing = easing),
        label = "bubble1"
    )
    val bubble2Offset by animateFloatAsState(
        targetValue = when (currentPage) {
            0 -> 0f
            1 -> -40f
            2 -> 60f
            else -> -20f
        },
        animationSpec = tween(800, easing = easing),
        label = "bubble2"
    )
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(if (isTablet) 400.dp else 280.dp)
                .offset(
                    x = (-80 + bubble1Offset).dp,
                    y = (-40 + bubble2Offset).dp
                )
                .clip(CircleShape)
                .background(PureWhite.copy(alpha = 0.03f))
        )
        Box(
            modifier = Modifier
                .size(if (isTablet) 300.dp else 200.dp)
                .offset(
                    x = ((screenWidthValue - 100) + bubble2Offset).dp,
                    y = (200 + bubble1Offset).dp
                )
                .clip(CircleShape)
                .background(PureWhite.copy(alpha = 0.04f))
        )
        Box(
            modifier = Modifier
                .size(if (isTablet) 200.dp else 140.dp)
                .offset(
                    x = ((screenWidthValue / 2 + 50) + (bubble1Offset * 0.5f)).dp,
                    y = (400 + bubble2Offset * 0.5f).dp
                )
                .clip(CircleShape)
                .background(PureWhite.copy(alpha = 0.03f))
        )
        Box(
            modifier = Modifier
                .size(if (isTablet) 100.dp else 70.dp)
                .offset(
                    x = ((screenWidthValue * 0.7f) + (bubble2Offset * 0.3f)).dp,
                    y = (100 + bubble1Offset * 0.3f).dp
                )
                .clip(CircleShape)
                .background(PureWhite.copy(alpha = 0.05f))
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = if (isTablet) 2.dp.toPx() else 1.5.dp.toPx()
            drawPath(
                path = Path().apply {
                    moveTo(size.width * 0.1f, size.height * 0.15f)
                    quadraticBezierTo(
                        size.width * 0.3f + bubble1Offset,
                        size.height * 0.1f,
                        size.width * 0.5f,
                        size.height * 0.2f
                    )
                },
                color = PureWhite.copy(alpha = 0.08f),
                style = Stroke(
                    width = strokeWidth,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            )
            drawPath(
                path = Path().apply {
                    moveTo(size.width * 0.8f, size.height * 0.7f)
                    quadraticBezierTo(
                        size.width * 0.6f + bubble2Offset * 0.3f,
                        size.height * 0.75f,
                        size.width * 0.4f,
                        size.height * 0.65f
                    )
                },
                color = PureWhite.copy(alpha = 0.06f),
                style = Stroke(
                    width = strokeWidth,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f))
                )
            )
            drawPath(
                path = Path().apply {
                    moveTo(size.width * 0.15f, size.height * 0.85f)
                    quadraticBezierTo(
                        size.width * 0.4f + bubble1Offset * 0.2f,
                        size.height * 0.8f,
                        size.width * 0.7f,
                        size.height * 0.9f
                    )
                },
                color = PureWhite.copy(alpha = 0.05f),
                style = Stroke(
                    width = strokeWidth,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 12f))
                )
            )
        }
    }
}
@Composable
fun OnboardingPageContentModern(
    page: OnboardingPage,
    iconSize: Dp,
    titleSize: TextUnit,
    descriptionSize: TextUnit,
    horizontalPadding: Dp,
    isTablet: Boolean,
    easing: Easing,
    timerProgress: Float,
    isTimerActive: Boolean
) {
    val PureWhite = Color(0xFFFFFFFF)
    val MoonYellow = Color(0xFFFDE047)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        var isVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(100)
            isVisible = true
        }
        val infiniteTransition = rememberInfiniteTransition(label = "float")
        val floatOffset by infiniteTransition.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = easing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "floatOffset"
        )
        val iconScale by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0.3f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessVeryLow
            ),
            label = "iconScale"
        )
        val iconAlpha by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0f,
            animationSpec = tween(800, easing = easing),
            label = "iconAlpha"
        )
        Box(
            modifier = Modifier
                .offset(y = floatOffset.dp)
                .size(iconSize * 1.8f)
                .scale(iconScale)
                .alpha(iconAlpha),
            contentAlignment = Alignment.Center
        ) {
            CircularTimerProgress(
                progress = timerProgress,
                size = iconSize * 1.2f,
                strokeWidth = if (isTablet) 4.dp else 3.dp,
                isActive = isTimerActive
            )
            Box(
                modifier = Modifier
                    .size(iconSize * 1.2f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MoonYellow.copy(alpha = 0.15f),
                                PureWhite.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .clip(CircleShape)
                    .background(PureWhite.copy(alpha = 0.08f))
                    .padding(if (isTablet) 28.dp else 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = page.icon),
                    contentDescription = page.title,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(modifier = Modifier.height(if (isTablet) 56.dp else 44.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = page.title,
                color = PureWhite,
                fontSize = titleSize,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = titleSize * 1.2f,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(if (isTablet) 12.dp else 8.dp))
            val underlineWidth by animateFloatAsState(
                targetValue = if (isVisible) 1f else 0f,
                animationSpec = tween(600, delayMillis = 200),
                label = "underlineWidth"
            )
            Box(
                modifier = Modifier
                    .width((if (isTablet) 60.dp else 40.dp) * underlineWidth)
                    .height(if (isTablet) 4.dp else 3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MoonYellow.copy(alpha = 0.8f),
                                MoonYellow.copy(alpha = 0.3f)
                            )
                        )
                    )
            )
        }
        Spacer(modifier = Modifier.height(if (isTablet) 28.dp else 20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(if (isTablet) 24.dp else 20.dp))
                .background(PureWhite.copy(alpha = 0.05f))
                .padding(horizontal = if (isTablet) 24.dp else 20.dp, vertical = if (isTablet) 20.dp else 16.dp)
        ) {
            Text(
                text = page.description,
                color = PureWhite.copy(alpha = 0.9f),
                fontSize = descriptionSize,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                lineHeight = descriptionSize * 1.5f
            )
        }
    }
}
@Composable
fun CircularTimerProgress(
    progress: Float,
    size: Dp,
    strokeWidth: Dp,
    isActive: Boolean
) {
    val MoonYellow = Color(0xFFFDE047)
    if (isActive) {
        Canvas(modifier = Modifier.size(size)) {
            val sweepAngle = progress * 360f
            drawArc(
                color = MoonYellow.copy(alpha = 0.9f),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
    }
    Canvas(modifier = Modifier.size(size)) {
        val sweepAngle = progress * 360f
        drawArc(
            color = MoonYellow.copy(alpha = 0.9f),
            startAngle = -90f, 
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}
@Composable
fun ModernDotsIndicator(
    totalDots: Int,
    currentPage: Int,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    val PureWhite = Color(0xFFFFFFFF)
    val MoonYellow = Color(0xFFFDE047)
    val dotSize = if (isTablet) 10.dp else 8.dp
    val selectedDotWidth = if (isTablet) 36.dp else 28.dp
    val spacing = if (isTablet) 12.dp else 10.dp
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalDots) { index ->
            val isSelected = index == currentPage
            val width by animateDpAsState(
                targetValue = if (isSelected) selectedDotWidth else dotSize,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessHigh
                ),
                label = "dotWidth"
            )
            val alpha by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.4f,
                animationSpec = tween(300),
                label = "dotAlpha"
            )
            val dotColor = if (isSelected) MoonYellow else PureWhite
            Box(
                modifier = Modifier
                    .width(width)
                    .height(dotSize)
                    .clip(if (isSelected) RoundedCornerShape(dotSize / 2) else CircleShape)
                    .background(dotColor.copy(alpha = alpha))
            )
        }
    }
}