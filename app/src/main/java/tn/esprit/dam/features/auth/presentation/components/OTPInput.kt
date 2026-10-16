package tn.esprit.dam.features.auth.presentation.components

import tn.esprit.dam.core.ui.theme.AppColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OTPInput(
    otp: String,
    onOtpChange: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = otp, selection = TextRange(otp.length)))
    }

    LaunchedEffect(otp) {
        if (textFieldValue.text != otp) {
            textFieldValue = TextFieldValue(
                text = otp,
                selection = TextRange(otp.length)
            )
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clickable {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                },
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(6) { index ->
                OTPBox(
                    digit = otp.getOrNull(index)?.toString() ?: "",
                    isFocused = enabled && otp.length == index,
                    enabled = enabled,
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                val newText = newValue.text.filter { it.isDigit() }.take(6)

                if (newText != otp) {
                    onOtpChange(newText)
                }

                textFieldValue = TextFieldValue(
                    text = newText,
                    selection = TextRange(newText.length)
                )
            },
            modifier = Modifier
                .size(1.dp)
                .focusRequester(focusRequester),
            enabled = enabled,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword
            ),
            cursorBrush = SolidColor(Color.Transparent),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.size(1.dp)) {
                    innerTextField()
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        focusRequester.requestFocus()
        keyboardController?.show()
    }
}

@Composable
fun OTPBox(
    digit: String,
    isFocused: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(
                color = AppColors.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 2.dp,
                color = when {
                    !enabled -> AppColors.border
                    isFocused -> AppColors.primary
                    digit.isNotEmpty() -> AppColors.success
                    else -> AppColors.border
                },
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit.ifEmpty { "●" },
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            ),
            color = if (digit.isNotEmpty()) Color.White else AppColors.textMuted,
            textAlign = TextAlign.Center
        )
    }
}
