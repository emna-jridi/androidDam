package tn.esprit.dam.features.auth.presentation.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object AuthColors {
    val BackgroundGradient = listOf(Color(0xFF0A0E27), Color(0xFF1A1F3A))
    val Primary = Color(0xFF7C3AED)
    val PrimaryDisabled = Color(0xFF5B21B6)
    val Card = Color(0xFF1E2139)
    val Field = Color(0xFF2D3250)
    val FieldBorder = Color(0xFF374151)
    val TextSecondary = Color(0xFFB4B4C6)
    val Placeholder = Color(0xFF6B7280)
    val Icon = Color(0xFF9CA3AF)
    val Success = Color(0xFF10B981)
    val Error = Color(0xFFEF4444)
}

@Composable
fun AuthBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = AuthColors.BackgroundGradient))
            .then(modifier),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun AuthIconBadge(icon: ImageVector) {
    Surface(
        modifier = Modifier.size(80.dp),
        shape = RoundedCornerShape(20.dp),
        color = AuthColors.Primary
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
fun AuthSuccessBadge() {
    Surface(
        modifier = Modifier.size(120.dp),
        shape = RoundedCornerShape(60.dp),
        color = AuthColors.Success.copy(alpha = 0.2f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                modifier = Modifier.size(70.dp),
                tint = AuthColors.Success
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
        color = Color.White,
        textAlign = textAlign
    )
}

@Composable
fun AuthSubtitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = AuthColors.TextSecondary,
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
        colors = CardDefaults.cardColors(containerColor = AuthColors.Card)
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
        color = Color.White
    )
}

@Composable
fun AuthFieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White,
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
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = AuthColors.Placeholder) },
        leadingIcon = {
            Icon(leadingIcon, contentDescription = null, tint = AuthColors.Icon)
        },
        trailingIcon = trailingIcon,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        visualTransformation = visualTransformation,
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(it, color = AuthColors.Error) } },
        shape = RoundedCornerShape(12.dp),
        colors = if (styleDisabledState) {
            OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                disabledTextColor = AuthColors.Icon,
                focusedBorderColor = AuthColors.Primary,
                unfocusedBorderColor = AuthColors.FieldBorder,
                focusedContainerColor = AuthColors.Field,
                unfocusedContainerColor = AuthColors.Field,
                disabledContainerColor = AuthColors.Field,
                cursorColor = AuthColors.Primary
            )
        } else {
            OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AuthColors.Primary,
                unfocusedBorderColor = AuthColors.FieldBorder,
                focusedContainerColor = AuthColors.Field,
                unfocusedContainerColor = AuthColors.Field,
                cursorColor = AuthColors.Primary
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
                    tint = AuthColors.Icon
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation()
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
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AuthColors.Primary,
            disabledContainerColor = AuthColors.PrimaryDisabled
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White
            )
        } else {
            Text(
                text,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
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
    val color = if (isError) AuthColors.Error else AuthColors.Success
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
                    tint = AuthColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Renvoyer le code",
                    color = AuthColors.Primary,
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
                tint = AuthColors.Icon,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Renvoyer dans ${countdownSeconds}s",
                color = AuthColors.Icon,
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
        colors = CardDefaults.cardColors(containerColor = AuthColors.Card.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = AuthColors.Primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                color = AuthColors.TextSecondary,
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
                contentDescription = "Back",
                tint = AuthColors.Primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, color = AuthColors.Primary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun AuthAutoRedirectNotice() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = AuthColors.Primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Redirection automatique dans 3 secondes...",
            color = AuthColors.Icon,
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
                color = AuthColors.Primary,
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
