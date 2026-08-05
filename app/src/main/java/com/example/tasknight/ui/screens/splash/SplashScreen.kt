package com.example.tasknight.ui.screens.splash
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
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
import com.example.tasknight.R
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.math.cos
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.unit.Dp
enum class AnimationState {
    FADE_IN, VISIBLE, FADE_OUT
}
val FastOutSlowInEasing: Easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isTablet = screenWidth >= 600.dp
    val logoSize = if (isTablet) 280.dp else 200.dp
    val titleSize = if (isTablet) 56.sp else 42.sp
    val subtitleSize = if (isTablet) 22.sp else 16.sp
    var animationState by remember { mutableStateOf(AnimationState.FADE_IN) }
    val alpha by animateFloatAsState(
        targetValue = when (animationState) {
            AnimationState.FADE_IN -> 1f
            AnimationState.VISIBLE -> 1f
            AnimationState.FADE_OUT -> 0f
        },
        animationSpec = tween(
            durationMillis = when (animationState) {
                AnimationState.FADE_IN -> 1000
                AnimationState.FADE_OUT -> 600
                AnimationState.VISIBLE -> 0
            },
            easing = FastOutSlowInEasing
        ),
        label = "alpha"
    )
    val logoScale by animateFloatAsState(
        targetValue = when (animationState) {
            AnimationState.FADE_IN -> 1f
            AnimationState.FADE_OUT -> 0.8f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffset"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    LaunchedEffect(Unit) {
        delay(1000) 
        animationState = AnimationState.VISIBLE
        delay(2800) 
        animationState = AnimationState.FADE_OUT
        delay(600) 
        onSplashComplete()
    }
    val BgNavy = Color(0xFF0F172A)
    val PureWhite = Color(0xFFFFFFFF)
    val MoonYellow = Color(0xFFFDE047)
    val mainGradient = Brush.verticalGradient(
        0.0f to Color(0xFF1E3A8A),
        0.4f to Color(0xFF4A3299),
        1.0f to Color(0xFF6B21A8)
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgNavy)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(mainGradient)
        )
        SplashBackgroundElements(
            isTablet = isTablet,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            alpha = alpha
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .offset(y = floatOffset.dp)
                        .size(logoSize)
                        .scale(logoScale),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(logoSize * 1.3f)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MoonYellow.copy(alpha = 0.12f * alpha),
                                        Color(0xFF4A3299).copy(alpha = 0.06f * alpha),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(logoSize * 1.1f)
                            .clip(CircleShape)
                            .background(PureWhite.copy(alpha = 0.04f * alpha))
                    )
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "TaskNight Logo",
                        modifier = Modifier
                            .fillMaxSize(0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(if (isTablet) 48.dp else 36.dp))
                Text(
                    text = "TaskNight",
                    color = PureWhite,
                    fontSize = titleSize,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(
                        Font(R.font.inter_28pt_bold)
                    ),
                    letterSpacing = 1.2.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(if (isTablet) 16.dp else 12.dp))
                Text(
                    text = "Plan Tonight, Win Tomorrow",
                    color = PureWhite.copy(alpha = 0.85f),
                    fontSize = subtitleSize,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily(
                        Font(R.font.inter_28pt_italic)
                    ),
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(if (isTablet) 56.dp else 44.dp))
                LoadingIndicator(
                    isTablet = isTablet,
                    alpha = alpha
                )
            }
        }
    }
}
@Composable
fun SplashBackgroundElements(
    isTablet: Boolean,
    screenWidth: Dp,
    screenHeight: Dp,
    alpha: Float
) {
    val PureWhite = Color(0xFFFFFFFF)
    val MoonYellow = Color(0xFFFDE047)
    val infiniteTransition = rememberInfiniteTransition(label = "bgAnim")
    val rotate1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate1"
    )
    val rotate2 by infiniteTransition.animateFloat(
        initialValue = 180f,
        targetValue = -180f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate2"
    )
    val float1 by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float1"
    )
    val float2 by infiniteTransition.animateFloat(
        initialValue = 15f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float2"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val maxRadius = size.minDimension / 2
        drawCircle(
            color = PureWhite.copy(alpha = 0.02f * alpha),
            radius = maxRadius * 0.8f + float1,
            center = Offset(centerX, centerY),
            style = Stroke(width = if (isTablet) 3.dp.toPx() else 2.dp.toPx())
        )
        drawCircle(
            color = MoonYellow.copy(alpha = 0.015f * alpha),
            radius = maxRadius * 0.6f + float2,
            center = Offset(centerX + float1 * 0.5f, centerY - float2 * 0.3f),
            style = Stroke(
                width = if (isTablet) 2.dp.toPx() else 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 30f))
            )
        )
        val orbitRadius = maxRadius * 0.9f
        val dot1Angle = Math.toRadians(rotate1.toDouble()).toFloat()
        val dot1X = centerX + orbitRadius * cos(dot1Angle.toDouble()).toFloat()
        val dot1Y = centerY + orbitRadius * sin(dot1Angle.toDouble()).toFloat()
        drawCircle(
            color = PureWhite.copy(alpha = 0.06f * alpha),
            radius = if (isTablet) 6.dp.toPx() else 4.dp.toPx(),
            center = Offset(dot1X, dot1Y)
        )
        val dot2Angle = Math.toRadians((rotate1 + 120).toDouble()).toFloat()
        val dot2X = centerX + orbitRadius * 0.7f * cos(dot2Angle.toDouble()).toFloat()
        val dot2Y = centerY + orbitRadius * 0.7f * sin(dot2Angle.toDouble()).toFloat()
        drawCircle(
            color = MoonYellow.copy(alpha = 0.04f * alpha),
            radius = if (isTablet) 8.dp.toPx() else 5.dp.toPx(),
            center = Offset(dot2X, dot2Y)
        )
        val dot3Angle = Math.toRadians(rotate2.toDouble()).toFloat()
        val dot3X = centerX + orbitRadius * 0.5f * cos(dot3Angle.toDouble()).toFloat()
        val dot3Y = centerY + orbitRadius * 0.5f * sin(dot3Angle.toDouble()).toFloat()
        drawCircle(
            color = PureWhite.copy(alpha = 0.05f * alpha),
            radius = if (isTablet) 5.dp.toPx() else 3.dp.toPx(),
            center = Offset(dot3X, dot3Y)
        )
        val path1 = Path().apply {
            moveTo(size.width * 0.1f, size.height * 0.2f)
            quadraticBezierTo(
                size.width * 0.3f + float1 * 2,
                size.height * 0.1f,
                size.width * 0.5f,
                size.height * 0.25f
            )
        }
        drawPath(
            path = path1,
            color = PureWhite.copy(alpha = 0.04f * alpha),
            style = Stroke(
                width = if (isTablet) 2.5.dp.toPx() else 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 18f))
            )
        )
        val path2 = Path().apply {
            moveTo(size.width * 0.85f, size.height * 0.75f)
            quadraticBezierTo(
                size.width * 0.6f + float2 * 1.5f,
                size.height * 0.85f,
                size.width * 0.4f,
                size.height * 0.7f
            )
        }
        drawPath(
            path = path2,
            color = MoonYellow.copy(alpha = 0.03f * alpha),
            style = Stroke(
                width = if (isTablet) 2.dp.toPx() else 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 20f))
            )
        )
        repeat(8) { i ->
            val angle = i * 45f + rotate1 * 0.1f
            val rad = Math.toRadians(angle.toDouble()).toFloat()
            val distance = maxRadius * (0.3f + (i % 3) * 0.15f)
            val x = centerX + distance * cos(rad) + float2 * 0.2f * (i % 2)
            val y = centerY + distance * sin(rad) + float1 * 0.15f * (i % 3)
            drawCircle(
                color = PureWhite.copy(alpha = 0.03f * alpha),
                radius = if (isTablet) 3.dp.toPx() else 2.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(if (isTablet) 300.dp else 200.dp)
                .offset(x = (-80).dp, y = (-60).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.02f * alpha))
        )
        Box(
            modifier = Modifier
                .size(if (isTablet) 200.dp else 140.dp)
                .offset(x = (screenWidth - 100.dp), y = (screenHeight - 200.dp))
                .clip(CircleShape)
                .background(Color(0xFF4A3299).copy(alpha = 0.04f * alpha))
        )
        Box(
            modifier = Modifier
                .size(if (isTablet) 150.dp else 100.dp)
                .offset(x = (screenWidth * 0.7f), y = 100.dp)
                .clip(CircleShape)
                .background(Color(0xFFFDE047).copy(alpha = 0.02f * alpha))
        )
    }
}
@Composable
fun LoadingIndicator(
    isTablet: Boolean,
    alpha: Float
) {
    val PureWhite = Color(0xFFFFFFFF)
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(if (isTablet) 12.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (isTablet) 8.dp else 6.dp)
                .clip(CircleShape)
                .background(PureWhite.copy(alpha = dot1Alpha * alpha))
        )
        Box(
            modifier = Modifier
                .size(if (isTablet) 8.dp else 6.dp)
                .clip(CircleShape)
                .background(PureWhite.copy(alpha = dot2Alpha * alpha))
        )
        Box(
            modifier = Modifier
                .size(if (isTablet) 8.dp else 6.dp)
                .clip(CircleShape)
                .background(PureWhite.copy(alpha = dot1Alpha * alpha))
        )
    }
}