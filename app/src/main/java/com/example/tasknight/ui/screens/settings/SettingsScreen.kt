package com.example.tasknight.ui.screens.settings

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LightMode
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasknight.R
import com.example.tasknight.presentation.settings.SettingsViewModel
import com.example.tasknight.ui.theme.InterFontFamily
import androidx.compose.ui.graphics.graphicsLayer
import com.example.tasknight.ui.screens.dashboard.BottomNavigationBar
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    onLogout: () -> Unit = {},
    onAccountDeleted: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToSet: () -> Unit = {},
    onNavigateToLog: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isTablet = screenWidth >= 600.dp

    val horizontalPadding = if (isTablet) 32.dp else 20.dp
    val topPadding = if (screenHeight < 700.dp) 16.dp else 24.dp
    val bottomPadding = if (screenHeight < 700.dp) 16.dp else 24.dp
    val cardSpacing = if (isTablet) 20.dp else 16.dp
    val cardCornerRadius = if (isTablet) 20.dp else 16.dp
    val avatarSize = if (isTablet) 64.dp else 52.dp

    val isDark = state.isDarkMode
    val BgNavy = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val CardBg = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    val PureWhite = if (isDark) Color(0xFFFFFFFF) else Color(0xFF1E293B)
    val AccentGreen = Color(0xFF10B981)
    val MutedGrey = Color(0xFF8B949E)
    val DangerRed = Color(0xFFEF4444)
    val DangerBg = Color(0xFFEF4444).copy(alpha = 0.1f)
    val MoonYellow = if (isDark) Color(0xFFFDE047) else Color(0xFFF59E0B)

    val scrollState = rememberScrollState()
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    var isLoggingOut by remember { mutableStateOf(false) }
    var isShowingLogoutLoading by remember { mutableStateOf(false) }

    LaunchedEffect(isLoggingOut) {
        if (isLoggingOut) {
            isShowingLogoutLoading = true
            viewModel.logout { }
            delay(1000)
            onLogout()
            isLoggingOut = false
            isShowingLogoutLoading = false
        }
    }

    var isClearingData by remember { mutableStateOf(false) }
    LaunchedEffect(isClearingData) {
        if (isClearingData) {
            isShowingLogoutLoading = true
            viewModel.clearAllData {
                onNavigateToHome()
            }
            delay(1000)
            isClearingData = false
            isShowingLogoutLoading = false
        }
    }

    var isDeletingAccount by remember { mutableStateOf(false) }
    LaunchedEffect(isDeletingAccount) {
        if (isDeletingAccount) {
            isShowingLogoutLoading = true
            viewModel.deleteAccount {
                onAccountDeleted()
            }
            delay(1000)
            isDeletingAccount = false
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
                    text = when {
                        isClearingData -> "Clearing data..."
                        isDeletingAccount -> "Deleting account..."
                        else -> "Signing out..."
                    },
                    color = PureWhite,
                    fontSize = if (isTablet) 18.sp else 16.sp,
                    fontWeight = FontWeight.Medium
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
        if (isDark) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF1E3A8A).copy(alpha = 0.3f), BgNavy)
                        )
                    )
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = horizontalPadding)
                    .padding(top = topPadding)
            ) {
                SettingsHeader(
                    isTablet = isTablet,
                    titleColor = PureWhite,
                    subtitleColor = MutedGrey
                )

                Spacer(modifier = Modifier.height(cardSpacing))

                ProfileCard(
                    userName = state.userName,
                    email = state.email,
                    isGuest = state.isGuest,
                    daysLogged = state.daysLogged,
                    tasksDone = state.tasksDone,
                    avatarSize = avatarSize,
                    cardBg = CardBg,
                    pureWhite = PureWhite,
                    accentGreen = AccentGreen,
                    mutedGrey = MutedGrey,
                    cornerRadius = cardCornerRadius,
                    isTablet = isTablet,
                    onUserNameChange = { viewModel.updateUserName(it) }
                )

                Spacer(modifier = Modifier.height(cardSpacing))

                DarkModeCard(
                    isDarkMode = state.isDarkMode,
                    onToggle = { viewModel.toggleDarkMode(it) },
                    cardBg = CardBg,
                    pureWhite = PureWhite,
                    accentGreen = AccentGreen,
                    cornerRadius = cardCornerRadius,
                    isTablet = isTablet
                )

                Spacer(modifier = Modifier.height(cardSpacing))

                LogoutCard(
                    cardBg = CardBg,
                    pureWhite = PureWhite,
                    dangerRed = DangerRed,
                    cornerRadius = cardCornerRadius,
                    isTablet = isTablet,
                    onClick = { showLogoutDialog = true }
                )

                if (!state.isGuest) {
                    Spacer(modifier = Modifier.height(cardSpacing))

                    DangerZoneCard(
                        cardBg = DangerBg,
                        pureWhite = PureWhite,
                        dangerRed = DangerRed,
                        cornerRadius = cardCornerRadius,
                        isTablet = isTablet,
                        onClearDataClick = { showClearDataDialog = true },
                        onDeleteAccountClick = { showDeleteAccountDialog = true }
                    )
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
                    isDarkMode = state.isDarkMode,
                    selectedIndex = 4,
                    onItemSelected = { index ->
                        when (index) {
                            0 -> onNavigateToHome()
                            1 -> onNavigateToSet()
                            2 -> onNavigateToLog()
                            3 -> onNavigateToHistory()
                        }
                    }
                )
            }
        }

        if (showClearDataDialog) {
            ClearDataDialog(
                onConfirm = {
                    showClearDataDialog = false
                    isClearingData = true
                },
                onDismiss = { showClearDataDialog = false },
                dangerRed = DangerRed,
                pureWhite = PureWhite,
                cardBg = CardBg
            )
        }

        if (showDeleteAccountDialog) {
            DeleteAccountDialog(
                onConfirm = {
                    showDeleteAccountDialog = false
                    isDeletingAccount = true
                },
                onDismiss = { showDeleteAccountDialog = false },
                dangerRed = DangerRed,
                pureWhite = PureWhite,
                cardBg = CardBg
            )
        }

        if (showLogoutDialog) {
            LogoutConfirmationDialog(
                isGuest = state.isGuest,
                onConfirm = {
                    showLogoutDialog = false
                    isLoggingOut = true
                },
                onDismiss = { showLogoutDialog = false },
                dangerRed = DangerRed,
                pureWhite = PureWhite,
                cardBg = CardBg,
                mutedGrey = MutedGrey
            )
        }
    }
}

@Composable
fun SettingsHeader(isTablet: Boolean, titleColor: Color, subtitleColor: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Settings",
            color = titleColor,
            fontSize = if (isTablet) 36.sp else 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = InterFontFamily
        )
        Text(
            text = "Customize your experience",
            color = subtitleColor,
            fontSize = if (isTablet) 16.sp else 14.sp,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun ProfileCard(
    userName: String,
    email: String?,
    isGuest: Boolean,
    daysLogged: Int,
    tasksDone: Int,
    avatarSize: androidx.compose.ui.unit.Dp,
    cardBg: Color,
    pureWhite: Color,
    accentGreen: Color,
    mutedGrey: Color,
    cornerRadius: androidx.compose.ui.unit.Dp,
    isTablet: Boolean,
    onUserNameChange: (String) -> Unit
) {
    var isEditingName by remember { mutableStateOf(false) }
    var nameText by remember { mutableStateOf(userName) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userName) { nameText = userName }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(cornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(if (isTablet) 20.dp else 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                        .background(accentGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.take(1).uppercase(),
                        color = Color.White,
                        fontSize = if (isTablet) 28.sp else 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = InterFontFamily
                    )
                }

                Spacer(modifier = Modifier.width(if (isTablet) 16.dp else 12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    if (isEditingName && !isGuest) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = nameText,
                                onValueChange = { nameText = it },
                                modifier = Modifier.weight(1.5f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = pureWhite,
                                    unfocusedTextColor = pureWhite,
                                    focusedBorderColor = accentGreen
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { if (nameText != userName) showConfirmDialog = true else isEditingName = false },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(painterResource(R.drawable.ic_check), "Save", tint = accentGreen, modifier = Modifier.size(20.dp))
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { nameText = userName; isEditingName = false },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Close, "Cancel", tint = Color.Red, modifier = Modifier.size(20.dp))
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = userName,
                                    color = pureWhite,
                                    fontSize = if (isTablet) 18.sp else 16.sp,
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    maxLines = 1,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = InterFontFamily
                                )
                                if (!isGuest && email != null) {
                                    Text(
                                        text = email,
                                        color = mutedGrey,
                                        fontSize = if (isTablet) 14.sp else 12.sp,
                                        fontFamily = InterFontFamily
                                    )
                                } else if (isGuest) {
                                    Text(
                                        text = "Guest Mode",
                                        color = accentGreen,
                                        fontSize = if (isTablet) 14.sp else 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = InterFontFamily
                                    )
                                }
                            }
                            if (!isGuest) {
                                IconButton(onClick = { isEditingName = true }) {
                                    Icon(painterResource(R.drawable.ic_edit), "Edit", tint = mutedGrey)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Days Logged", daysLogged.toString(), Modifier.weight(1f), pureWhite.copy(0.05f), pureWhite, accentGreen, mutedGrey, isTablet)
                StatCard("Tasks Done", tasksDone.toString(), Modifier.weight(1f), pureWhite.copy(0.05f), pureWhite, accentGreen, mutedGrey, isTablet)
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Name Change?", color = pureWhite) },
            text = { Text("Do you want to change your name to \"$nameText\"?", color = pureWhite.copy(0.8f)) },
            confirmButton = {
                TextButton(onClick = { onUserNameChange(nameText); isEditingName = false; showConfirmDialog = false }) {
                    Text("Yes", color = accentGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("No", color = pureWhite.copy(0.7f))
                }
            },
            containerColor = cardBg
        )
    }
}

@Composable
fun DarkModeCard(isDarkMode: Boolean, onToggle: (Boolean) -> Unit, cardBg: Color, pureWhite: Color, accentGreen: Color, cornerRadius: androidx.compose.ui.unit.Dp, isTablet: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isTablet) 20.dp else 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (isDarkMode) {
                    Icon(painterResource(R.drawable.ic_moon), null, tint = pureWhite)
                } else {
                    Icon(Icons.Default.LightMode, null, tint = Color(0xFFF59E0B))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Dark Mode", color = pureWhite, fontSize = if (isTablet) 16.sp else 14.sp)
            }
            Switch(
                checked = isDarkMode,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(checkedTrackColor = accentGreen)
            )
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier, cardBg: Color, pureWhite: Color, accentGreen: Color, mutedGrey: Color, isTablet: Boolean) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = cardBg)) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = mutedGrey, fontSize = 11.sp)
            Text(value, color = accentGreen, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LogoutCard(cardBg: Color, pureWhite: Color, dangerRed: Color, cornerRadius: androidx.compose.ui.unit.Dp, isTablet: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Row(
            modifier = Modifier.padding(if (isTablet) 20.dp else 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_logout), null, tint = dangerRed)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Sign Out", color = dangerRed, fontWeight = FontWeight.Medium)
            }
            Icon(painterResource(R.drawable.ic_chevron_right), null, tint = dangerRed.copy(0.5f))
        }
    }
}

@Composable
fun DangerZoneCard(
    cardBg: Color,
    pureWhite: Color,
    dangerRed: Color,
    cornerRadius: androidx.compose.ui.unit.Dp,
    isTablet: Boolean,
    onClearDataClick: () -> Unit,
    onDeleteAccountClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Danger Zone", color = pureWhite, fontWeight = FontWeight.Bold)
            Text("This action cannot be undone", color = dangerRed.copy(0.8f), fontSize = 11.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onClearDataClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = dangerRed),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(painterResource(R.drawable.ic_trash), null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear All Data", fontSize = 12.sp, maxLines = 1)
                }
                Button(
                    onClick = onDeleteAccountClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    border = androidx.compose.foundation.BorderStroke(1.dp, dangerRed),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(painterResource(R.drawable.ic_trash), null, tint = dangerRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete Account", color = dangerRed, fontSize = 12.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun ClearDataDialog(onConfirm: () -> Unit, onDismiss: () -> Unit, dangerRed: Color, pureWhite: Color, cardBg: Color) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clear All Data?", color = pureWhite) },
        text = { Text("This will permanently delete all your tasks, streaks, and reflections. This action is irreversible.", color = pureWhite.copy(0.8f)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Clear Everything", color = dangerRed) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = pureWhite.copy(0.7f)) } },
        containerColor = cardBg
    )
}

@Composable
fun DeleteAccountDialog(onConfirm: () -> Unit, onDismiss: () -> Unit, dangerRed: Color, pureWhite: Color, cardBg: Color) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Account?", color = pureWhite) },
        text = { Text("Are you sure you want to delete your account? All your data, including login credentials, will be permanently wiped out. This process is irreversible.", color = pureWhite.copy(0.8f)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Delete Account", color = dangerRed) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = pureWhite.copy(0.7f)) } },
        containerColor = cardBg
    )
}

@Composable
fun LogoutConfirmationDialog(isGuest: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit, dangerRed: Color, pureWhite: Color, cardBg: Color, mutedGrey: Color) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isGuest) "Exit Guest Mode?" else "Sign Out?", color = pureWhite) },
        text = { Text(if (isGuest) "Your data is saved locally." else "Are you sure you want to sign out?", color = mutedGrey) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(if (isGuest) "Exit" else "Sign Out", color = dangerRed) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = pureWhite.copy(0.7f)) } },
        containerColor = cardBg
    )
}
