package tn.esprit.dam.features.auth.presentation.components
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography
import tn.esprit.dam.R
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AuthBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(AppColors.background, AppColors.surface)))
            .then(modifier),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/** Scrollable auth page on a hex-shield pattern with a cyan glow at the top; stays usable when the keyboard opens. */
@Composable
fun AuthScreenLayout(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
            .drawBehind {
                drawHexShield(unit = 1.dp.toPx())
                // Glow covers the whole screen and ends fully transparent, so there is no visible edge.
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(AppColors.primary.copy(alpha = 0.16f), Color.Transparent),
                        center = Offset(size.width / 2f, 0f),
                        radius = size.width
                    )
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xl),
            content = content
        )
    }
}

/** Faint honeycomb of cyan hexagons that fades smoothly to nothing near the bottom. */
private fun DrawScope.drawHexShield(unit: Float) {
    // One 28 x 48.5 tile: a pointy-top hexagon plus the stem that links it to the row below.
    val tileW = 28f * unit
    val tileH = 48.5f * unit
    val tile = Path().apply {
        moveTo(14f * unit, 0f)
        lineTo(28f * unit, 8.08f * unit)
        lineTo(28f * unit, 24.25f * unit)
        lineTo(14f * unit, 32.33f * unit)
        lineTo(0f, 24.25f * unit)
        lineTo(0f, 8.08f * unit)
        close()
        moveTo(14f * unit, 32.33f * unit)
        lineTo(14f * unit, tileH)
    }
    val fadeEnd = size.height * 0.85f
    val stroke = Stroke(width = unit)
    var y = 0f
    while (y < fadeEnd) {
        val alpha = 0.09f * (1f - y / fadeEnd)
        var x = 0f
        while (x < size.width) {
            translate(left = x, top = y) {
                drawPath(tile, AppColors.primary.copy(alpha = alpha), style = stroke)
            }
            x += tileW
        }
        y += tileH
    }
}

/** Shield emblem in concentric rings, brand label, title and subtitle. */
@Composable
fun AuthHeroHeader(icon: ImageVector, title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .border(1.dp, AppColors.primary.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .border(1.dp, AppColors.primary.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(AppColors.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = AppColors.primary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(AppSpacing.lg))
        Text(
            text = stringResource(R.string.shadowguard_title).uppercase(),
            style = AppTypography.labelMedium.copy(letterSpacing = 3.sp, fontWeight = FontWeight.Bold),
            color = AppColors.primary
        )
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        Text(
            text = title,
            style = AppTypography.displayLarge,
            color = AppColors.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        Text(
            text = subtitle,
            style = AppTypography.bodyMedium,
            color = AppColors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

/** Small "secure" pill shown at the bottom of auth pages. */
@Composable
fun AuthTrustBadge(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(AppColors.surface)
            .border(1.dp, AppColors.surfaceVariant, RoundedCornerShape(50))
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.VerifiedUser,
            contentDescription = null,
            tint = AppColors.success,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(AppSpacing.sm))
        Text(text = text, style = AppTypography.labelMedium, color = AppColors.textSecondary)
    }
}

/** Password score 0..4 from length and character variety. */
fun passwordStrengthScore(password: String): Int {
    if (password.isEmpty()) return 0
    var score = 0
    if (password.length >= 8) score++
    if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++
    if (password.length < 8) score = minOf(score, 1)
    return score.coerceAtLeast(1)
}

/** 4-segment strength bar with a text label, so strength is never shown by color alone. */
@Composable
fun PasswordStrengthMeter(password: String) {
    if (password.isEmpty()) {
        Text(
            text = stringResource(R.string.password_strength_hint),
            style = AppTypography.labelMedium,
            color = AppColors.textTertiary
        )
        return
    }
    val score = passwordStrengthScore(password)
    val (color, labelRes) = when (score) {
        1 -> AppColors.riskCritical to R.string.password_strength_weak
        2 -> AppColors.riskMedium to R.string.password_strength_medium
        3 -> AppColors.info to R.string.password_strength_good
        else -> AppColors.riskLow to R.string.password_strength_strong
    }
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (index < score) color else AppColors.surfaceVariant)
                )
            }
        }
        Text(
            text = stringResource(R.string.password_strength_label, stringResource(labelRes)),
            style = AppTypography.labelMedium,
            color = color
        )
    }
}

@Composable
fun AuthIconBadge(icon: ImageVector) {
    Surface(
        modifier = Modifier.size(80.dp),
        shape = RoundedCornerShape(20.dp),
        color = AppColors.primary
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = AppColors.onPrimary
            )
        }
    }
}

@Composable
fun AuthSuccessBadge() {
    Surface(
        modifier = Modifier.size(120.dp),
        shape = RoundedCornerShape(60.dp),
        color = AppColors.success.copy(alpha = 0.2f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = stringResource(R.string.success),
                modifier = Modifier.size(70.dp),
                tint = AppColors.success
            )
        }
    }
}

@Composable
fun AuthTitle(
    text: String,
    fontSize: TextUnit = 32.sp,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = fontSize
        ),
        color = AppColors.textPrimary,
        textAlign = textAlign
    )
}

@Composable
fun AuthSubtitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = AppColors.textSecondary,
        textAlign = TextAlign.Center
    )
}

@Composable
fun AuthCard(
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    contentPadding: Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface)
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            horizontalAlignment = horizontalAlignment,
            content = content
        )
    }
}

@Composable
fun AuthCardTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        color = AppColors.textPrimary
    )
}

@Composable
fun AuthFieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = AppColors.textPrimary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    enabled: Boolean,
    error: String?,
    styleDisabledState: Boolean = false,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = AppColors.textMuted) },
        leadingIcon = {
            Icon(
                leadingIcon,
                contentDescription = null,
                tint = if (value.isNotEmpty()) AppColors.primary else AppColors.textTertiary
            )
        },
        trailingIcon = trailingIcon,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(it, color = AppColors.error) } },
        shape = RoundedCornerShape(AppCorners.medium),
        colors = if (styleDisabledState) {
            OutlinedTextFieldDefaults.colors(
                focusedTextColor = AppColors.textPrimary,
                unfocusedTextColor = AppColors.textPrimary,
                disabledTextColor = AppColors.textTertiary,
                focusedBorderColor = AppColors.primary,
                unfocusedBorderColor = AppColors.border,
                focusedContainerColor = AppColors.surfaceVariant,
                unfocusedContainerColor = AppColors.surfaceVariant,
                disabledContainerColor = AppColors.surfaceVariant,
                cursorColor = AppColors.primary
            )
        } else {
            OutlinedTextFieldDefaults.colors(
                focusedTextColor = AppColors.textPrimary,
                unfocusedTextColor = AppColors.textPrimary,
                focusedBorderColor = AppColors.primary,
                unfocusedBorderColor = AppColors.surfaceVariant,
                focusedContainerColor = AppColors.surface,
                unfocusedContainerColor = AppColors.surface,
                cursorColor = AppColors.primary
            )
        }
    )
}

@Composable
fun AuthPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisibility: () -> Unit,
    enabled: Boolean,
    error: String?
) {
    AuthTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = "●●●●●●●●",
        leadingIcon = Icons.Filled.Lock,
        enabled = enabled,
        error = error,
        trailingIcon = {
            IconButton(onClick = onToggleVisibility, enabled = enabled) {
                Icon(
                    imageVector = if (visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = null,
                    tint = AppColors.textTertiary
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
    )
}

@Composable
fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(AppCorners.medium),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.primary,
            disabledContainerColor = AppColors.primaryDark
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = AppColors.onPrimary
            )
        } else {
            Text(
                text,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = AppColors.onPrimary
            )
        }
    }
}

@Composable
fun AuthStatusMessage(
    text: String,
    isError: Boolean,
    compact: Boolean = false
) {
    val color = if (isError) AppColors.error else AppColors.success
    Spacer(modifier = Modifier.height(16.dp))
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (compact) 12.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(if (compact) 20.dp else 24.dp)
            )
            Spacer(modifier = Modifier.width(if (compact) 8.dp else 12.dp))
            Text(
                text = text,
                color = color,
                style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun AuthResendCode(
    canResend: Boolean,
    countdownSeconds: Int,
    enabled: Boolean,
    onResend: () -> Unit
) {
    if (canResend) {
        TextButton(onClick = onResend, enabled = enabled) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = AppColors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.renvoyer_le_code),
                    color = AppColors.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = AppColors.textTertiary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Renvoyer dans ${countdownSeconds}s",
                color = AppColors.textTertiary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun AuthInfoCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = AppColors.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                color = AppColors.textSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun AuthBackButton(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = AppColors.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, color = AppColors.primary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun AuthAutoRedirectNotice() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = AppColors.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.redirection_automatique_dans_3_secondes),
            color = AppColors.textTertiary,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun OtpVerificationLayout(
    icon: ImageVector,
    title: String,
    subtitle: String,
    email: String,
    otp: String,
    onOtpChange: (String) -> Unit,
    isLoading: Boolean,
    verifyLabel: String,
    onVerify: () -> Unit,
    canResend: Boolean,
    resendCountdown: Int,
    onResend: () -> Unit,
    successMessage: String?,
    errorMessage: String?,
    infoText: String,
    backLabel: String,
    onBack: () -> Unit,
    columnModifier: Modifier
) {
    AuthBackground {
        Column(
            modifier = columnModifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthIconBadge(icon)
            Spacer(modifier = Modifier.height(24.dp))
            AuthTitle(title)
            Spacer(modifier = Modifier.height(16.dp))
            AuthSubtitle(subtitle)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = email,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = AppColors.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))

            AuthCard(horizontalAlignment = Alignment.CenterHorizontally) {
                AuthCardTitle("Entrez le code")
                Spacer(modifier = Modifier.height(32.dp))
                OTPInput(otp = otp, onOtpChange = onOtpChange, enabled = !isLoading)
                Spacer(modifier = Modifier.height(32.dp))
                AuthPrimaryButton(
                    text = verifyLabel,
                    onClick = onVerify,
                    enabled = !isLoading && otp.length == 6,
                    isLoading = isLoading
                )
                Spacer(modifier = Modifier.height(24.dp))
                AuthResendCode(
                    canResend = canResend,
                    countdownSeconds = resendCountdown,
                    enabled = !isLoading,
                    onResend = onResend
                )
                successMessage?.let { AuthStatusMessage(it, isError = false, compact = true) }
                errorMessage?.let { AuthStatusMessage(it, isError = true) }
            }

            Spacer(modifier = Modifier.height(24.dp))
            AuthInfoCard(infoText)
            Spacer(modifier = Modifier.height(24.dp))
            AuthBackButton(backLabel, onBack)
        }
    }
}
