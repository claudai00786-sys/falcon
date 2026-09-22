package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkPanel
import com.example.ui.theme.DarkPanelRaised
import com.example.ui.theme.DarkSteelLine
import com.example.ui.theme.PosAmber
import com.example.ui.theme.PosOrange
import com.example.ui.theme.PosRed
import com.example.ui.theme.PosSkyBlue
import com.example.ui.components.AppBrandLogo

@Composable
fun LockScreen(
    enteredPin: String,
    pinError: Boolean,
    onPinDigit: (String) -> Unit,
    onPinBackspace: () -> Unit,
    onPinClear: () -> Unit,
    onResetPinWithAnswer: (String, String) -> Boolean,
    currentLanguage: String,
    modifier: Modifier = Modifier,
    onToggleLanguage: ((String) -> Unit)? = null
) {
    val isUr = currentLanguage == "ur"
    var showPassword by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }
    var securityAnswerInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var newPinConfirmInput by remember { mutableStateOf("") }
    var forgotStep by remember { mutableIntStateOf(1) } // 1: answer question, 2: set new pin
    var forgotError by remember { mutableStateOf<String?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pingScale"
    )
    val pingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pingAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Central Lock Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkPanel),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSteelLine),
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Optional Language Toggle Row on Lock Screen
                if (onToggleLanguage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkPanelRaised)
                                .padding(2.dp)
                        ) {
                            Text(
                                text = "EN",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentLanguage == "en") PosAmber else Color.Gray,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (currentLanguage == "en") DarkSteelLine else Color.Transparent)
                                    .clickable { onToggleLanguage("en") }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                            Text(
                                text = "اردو",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentLanguage == "ur") PosAmber else Color.Gray,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (currentLanguage == "ur") DarkSteelLine else Color.Transparent)
                                    .clickable { onToggleLanguage("ur") }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Animated Falcon Circular Badge
                Box(
                    modifier = Modifier
                        .size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp * pingScale)
                            .clip(CircleShape)
                            .background(PosAmber.copy(alpha = pingAlpha.coerceIn(0f, 1f)))
                    )
                    AppBrandLogo(
                        modifier = Modifier.size(56.dp),
                        showBorder = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Brand Title in Blue/Cyan Metallic styling as in HTML
                Text(
                    text = if (currentLanguage == "ur") "فالکن راڈ میکر" else "Falcon Rod Maker",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    color = PosSkyBlue,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (currentLanguage == "ur") "فین ایکسیسریز" else "Fan Accessories",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = PosSkyBlue.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // City badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = DarkPanelRaised,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSteelLine)
                ) {
                    Text(
                        text = if (currentLanguage == "ur") "گجرات" else "Gujrat",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = PosAmber,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Username Display Box
                OutlinedTextField(
                    value = "Amir",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (currentLanguage == "ur") "صارف" else "Username", fontSize = 10.sp) },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    modifier = Modifier.fillMaxWidth(0.75f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkSteelLine,
                        unfocusedBorderColor = DarkSteelLine,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // PIN Display with Visibility Eye
                OutlinedTextField(
                    value = if (showPassword) enteredPin else "•".repeat(enteredPin.length),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (currentLanguage == "ur") "پن درج کریں" else "Enter 4-Digit PIN", fontSize = 10.sp) },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle PIN Visibility",
                                tint = Color.Gray
                            )
                        }
                    },
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 6.sp,
                        color = if (pinError) PosRed else PosAmber
                    ),
                    modifier = Modifier.fillMaxWidth(0.75f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (pinError) PosRed else PosAmber,
                        unfocusedBorderColor = if (pinError) PosRed else DarkSteelLine
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 4 PIN Dots Indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(13.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        pinError -> PosRed
                                        isFilled -> PosAmber
                                        else -> Color.Transparent
                                    }
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = when {
                                        pinError -> PosRed
                                        isFilled -> PosAmber
                                        else -> DarkSteelLine
                                    },
                                    shape = CircleShape
                                )
                        )
                    }
                }

                if (pinError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (currentLanguage == "ur") "غلط پن۔ دوبارہ کوشش کریں۔" else "Incorrect PIN. Try again.",
                        color = PosRed,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Custom Keypad
                KeypadGrid(
                    onDigit = onPinDigit,
                    onClear = onPinClear,
                    onBackspace = onPinBackspace,
                    isUrdu = isUr
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Forgot PIN link
                Text(
                    text = if (currentLanguage == "ur") "پن بھول گئے؟" else "Forgot PIN?",
                    color = Color.LightGray,
                    fontSize = 11.5.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .clickable {
                            forgotStep = 1
                            securityAnswerInput = ""
                            newPinInput = ""
                            newPinConfirmInput = ""
                            forgotError = null
                            showForgotDialog = true
                        }
                        .padding(6.dp)
                        .testTag("forgot_pin_link")
                )
            }
        }
    }

    // Forgot PIN Dialog
    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            containerColor = DarkPanel,
            title = {
                Text(
                    text = if (currentLanguage == "ur") "پن ری سیٹ کریں" else "Reset PIN",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (forgotStep == 1) {
                        Text(
                            text = if (currentLanguage == "ur")
                                "سیکیورٹی سوال: دکان کے مالک کا نام کیا ہے؟"
                            else
                                "Security Question: What is the shop owner's name?",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = securityAnswerInput,
                            onValueChange = { securityAnswerInput = it; forgotError = null },
                            placeholder = { Text("e.g. amir") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = if (currentLanguage == "ur") "نیا 4 ہندسوں کا پن درج کریں:" else "Enter new 4-digit PIN:",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = { if (it.length <= 4) newPinInput = it },
                            placeholder = { Text("••••") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPinConfirmInput,
                            onValueChange = { if (it.length <= 4) newPinConfirmInput = it },
                            placeholder = { Text("Confirm PIN") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (forgotError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = forgotError!!,
                            color = PosRed,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (forgotStep == 1) {
                            if (securityAnswerInput.trim().lowercase() == "amir") {
                                forgotStep = 2
                                forgotError = null
                            } else {
                                forgotError = if (currentLanguage == "ur") "غلط جواب۔" else "Incorrect answer. Try again."
                            }
                        } else {
                            if (newPinInput.length == 4 && newPinInput == newPinConfirmInput) {
                                val success = onResetPinWithAnswer(securityAnswerInput, newPinInput)
                                if (success) {
                                    showForgotDialog = false
                                } else {
                                    forgotError = "Error updating PIN."
                                }
                            } else {
                                forgotError = "PINs must match and be exactly 4 digits."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PosAmber, contentColor = Color(0xFF1C1F22))
                ) {
                    Text(if (forgotStep == 1) (if (currentLanguage == "ur") "تصدیق کریں" else "Verify") else (if (currentLanguage == "ur") "محفوظ کریں" else "Save PIN"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text(if (currentLanguage == "ur") "منسوخ" else "Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun KeypadGrid(
    onDigit: (String) -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
    isUrdu: Boolean = false
) {
    val clearLabel = if (isUrdu) "صاف" else "CLEAR"
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(clearLabel, "0", "⌫")
    )

    Column(
        modifier = modifier.widthIn(max = 280.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DarkPanelRaised,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSteelLine),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clickable {
                                when (key) {
                                    clearLabel, "CLEAR" -> onClear()
                                    "⌫" -> onBackspace()
                                    else -> onDigit(key)
                                }
                            }
                            .testTag("keypad_$key")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = key,
                                fontSize = if (key.length > 1) 11.sp else 19.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (key.length > 1) Color.LightGray else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
