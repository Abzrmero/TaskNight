package com.example.tasknight.ui.screens.auth
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasknight.R
import com.example.tasknight.presentation.auth.AuthEvent
import com.example.tasknight.presentation.auth.AuthViewModel
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    isUpgrade: Boolean = false,
    viewModel: AuthViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(isUpgrade) {
        viewModel.onEvent(AuthEvent.SetUpgradeMode(isUpgrade))
    }
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val isTablet = screenWidth >= 600.dp
    val horizontalPadding = if (isTablet) 48.dp else 24.dp
    val logoSize = if (isTablet) 100.dp else 70.dp
    val titleSize = if (isTablet) 32.sp else 26.sp
    val buttonHeight = if (isTablet) 52.dp else 46.dp
    val spacing = if (isTablet) 20.dp else 14.dp
    val topPadding = if (screenHeight < 700.dp) 12.dp else 24.dp
    val bottomPadding = if (screenHeight < 700.dp) 16.dp else 32.dp
    val BgNavy = Color(0xFF0F172A)
    val PureWhite = Color(0xFFFFFFFF)
    val MoonYellow = Color(0xFFFDE047)
    val scrollState = rememberScrollState()
    LaunchedEffect(state.user) {
        if (state.user != null) {
            onAuthSuccess()
        }
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
                            Color(0xFF1E3A8A).copy(alpha = 0.6f),
                            Color(0xFF4A3299).copy(alpha = 0.4f),
                            BgNavy
                        )
                    )
                )
        )
        AuthBackgroundElements(isTablet = isTablet)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = horizontalPadding)
                .padding(top = topPadding, bottom = bottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(logoSize)
                    .clip(CircleShape)
                    .background(PureWhite.copy(alpha = 0.1f))
                    .padding(if (isTablet) 20.dp else 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "TaskNight Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(spacing))
            Text(
                text = "TaskNight",
                color = PureWhite,
                fontSize = titleSize,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Default,
                letterSpacing = 1.sp
            )
            Text(
                text = when {
                    state.isGuestUpgrade -> "Upgrade Account"
                    state.isSignInMode -> "Welcome Back!"
                    else -> "Create Account"
                },
                color = PureWhite.copy(alpha = 0.8f),
                fontSize = if (isTablet) 16.sp else 13.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(spacing))
            if (!state.isGuestUpgrade) {
                ModeToggleTabs(
                    isSignInMode = state.isSignInMode,
                    onToggle = { viewModel.onEvent(AuthEvent.ToggleMode) },
                    isTablet = isTablet
                )
                Spacer(modifier = Modifier.height(spacing))
            }
            AnimatedContent(
                targetState = if (state.isGuestUpgrade) false else state.isSignInMode,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "formTransition"
            ) { isSignIn ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    if (!isSignIn) {
                        AuthTextField(
                            value = state.name,
                            onValueChange = { viewModel.onEvent(AuthEvent.NameChanged(it)) },
                            placeholder = "Full Name",
                            leadingIcon = "👤",
                            isError = state.nameError != null,
                            errorText = state.nameError,
                            isTablet = isTablet
                        )
                    }
                    AuthTextField(
                        value = state.email,
                        onValueChange = { viewModel.onEvent(AuthEvent.EmailChanged(it)) },
                        placeholder = "Email",
                        leadingIcon = "✉️",
                        keyboardType = KeyboardType.Email,
                        isError = state.emailError != null,
                        errorText = state.emailError,
                        isTablet = isTablet
                    )
                    AuthTextField(
                        value = state.password,
                        onValueChange = { viewModel.onEvent(AuthEvent.PasswordChanged(it)) },
                        placeholder = "Password",
                        leadingIcon = "🔒",
                        isPassword = true,
                        isError = state.passwordError != null,
                        errorText = state.passwordError,
                        isTablet = isTablet
                    )
                }
            }
            AnimatedVisibility(
                visible = state.error != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacing),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEF4444).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = state.error ?: "",
                        color = Color(0xFFEF4444),
                        fontSize = if (isTablet) 14.sp else 12.sp,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(spacing))
            Button(
                onClick = {
                    when {
                        state.isGuestUpgrade -> viewModel.onEvent(AuthEvent.SignUpWithEmail)
                        state.isSignInMode -> viewModel.onEvent(AuthEvent.SignInWithEmail)
                        else -> viewModel.onEvent(AuthEvent.SignUpWithEmail)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(buttonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MoonYellow,
                    contentColor = BgNavy
                ),
                shape = RoundedCornerShape(buttonHeight / 2),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = BgNavy,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = when {
                            state.isGuestUpgrade -> "Upgrade Now"
                            state.isSignInMode -> "Sign In"
                            else -> "Sign Up"
                        },
                        fontSize = if (isTablet) 16.sp else 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (!state.isGuestUpgrade) {
                Spacer(modifier = Modifier.height(spacing))
                TextButton(
                    onClick = { viewModel.onEvent(AuthEvent.SignInAsGuest) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                ) {
                    Text(
                        text = "Continue as Guest",
                        color = PureWhite.copy(alpha = 0.7f),
                        fontSize = if (isTablet) 15.sp else 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
@Composable
fun ModeToggleTabs(
    isSignInMode: Boolean,
    onToggle: () -> Unit,
    isTablet: Boolean
) {
    val PureWhite = Color(0xFFFFFFFF)
    val MoonYellow = Color(0xFFFDE047)
    val tabHeight = if (isTablet) 44.dp else 38.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tabHeight / 2))
            .background(PureWhite.copy(alpha = 0.1f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TabButton(
            text = "Sign In",
            isSelected = isSignInMode,
            onClick = { if (!isSignInMode) onToggle() },
            modifier = Modifier.weight(1f),
            height = tabHeight,
            selectedColor = MoonYellow,
            unselectedColor = Color.Transparent,
            textColor = PureWhite,
            isTablet = isTablet
        )
        TabButton(
            text = "Sign Up",
            isSelected = !isSignInMode,
            onClick = { if (isSignInMode) onToggle() },
            modifier = Modifier.weight(1f),
            height = tabHeight,
            selectedColor = MoonYellow,
            unselectedColor = Color.Transparent,
            textColor = PureWhite,
            isTablet = isTablet
        )
    }
}
@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp,
    selectedColor: Color,
    unselectedColor: Color,
    textColor: Color,
    isTablet: Boolean
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(if (isSelected) selectedColor else unselectedColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color(0xFF0F172A) else textColor,
            fontSize = if (isTablet) 15.sp else 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    isError: Boolean = false,
    errorText: String? = null,
    isTablet: Boolean
) {
    val PureWhite = Color(0xFFFFFFFF)
    var passwordVisible by remember { mutableStateOf(false) }
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    color = PureWhite.copy(alpha = 0.5f),
                    fontSize = if (isTablet) 14.sp else 13.sp
                )
            },
            leadingIcon = {
                Text(
                    text = leadingIcon,
                    fontSize = if (isTablet) 18.sp else 16.sp
                )
            },
            trailingIcon = {
                if (isPassword) {
                    TextButton(
                        onClick = { passwordVisible = !passwordVisible },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (passwordVisible) "🙈" else "👁️",
                            fontSize = if (isTablet) 16.sp else 14.sp
                        )
                    }
                }
            },
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PureWhite,
                unfocusedTextColor = PureWhite,
                focusedContainerColor = PureWhite.copy(alpha = 0.05f),
                unfocusedContainerColor = PureWhite.copy(alpha = 0.05f),
                focusedBorderColor = if (isError) Color(0xFFEF4444) else Color(0xFF4A3299),
                unfocusedBorderColor = if (isError) Color(0xFFEF4444) else PureWhite.copy(alpha = 0.2f),
                errorBorderColor = Color(0xFFEF4444)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            isError = isError,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = if (isTablet) 15.sp else 14.sp
            )
        )
        if (errorText != null && isError) {
            Text(
                text = errorText,
                color = Color(0xFFEF4444),
                fontSize = if (isTablet) 11.sp else 10.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
@Composable
fun AuthBackgroundElements(isTablet: Boolean) {
    val PureWhite = Color(0xFFFFFFFF)
    val MoonYellow = Color(0xFFFDE047)
    val infiniteTransition = rememberInfiniteTransition(label = "bgAnim")
    val float1 by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float1"
    )
    val float2 by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float2"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        drawCircle(
            color = PureWhite.copy(alpha = 0.03f),
            radius = size.minDimension * 0.4f + float1,
            center = Offset(centerX, centerY - 100)
        )
        drawCircle(
            color = MoonYellow.copy(alpha = 0.02f),
            radius = size.minDimension * 0.3f + float2,
            center = Offset(centerX + 50, centerY + 100)
        )
        drawCircle(
            color = Color(0xFF4A3299).copy(alpha = 0.04f),
            radius = size.minDimension * 0.2f,
            center = Offset(size.width * 0.2f, size.height * 0.3f)
        )
    }
}