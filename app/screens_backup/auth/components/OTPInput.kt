package tn.esprit.dam.screens.auth.components

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

/**
 * Composant OTP Input (Version corrigée sans crash)
 */
@Composable
fun OTPInput(
    otp: String,
    onOtpChange: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Utiliser TextFieldValue pour contrôler le curseur
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = otp, selection = TextRange(otp.length)))
    }

    // Synchroniser avec le state parent
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
        // Affichage visuel des 6 cases
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // Cliquer sur les cases pour ouvrir le clavier
                    focusRequester.requestFocus()
                    keyboardController?.show()
                },
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(6) { index ->
                OTPBox(
                    digit = otp.getOrNull(index)?.toString() ?: "",
                    isFocused = enabled && otp.length == index,
                    enabled = enabled
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TextField invisible pour la saisie
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                // Accepter seulement les chiffres et max 6 caractères
                val newText = newValue.text.filter { it.isDigit() }.take(6)

                if (newText != otp) {
                    onOtpChange(newText)
                }

                // Mettre à jour le TextFieldValue avec le curseur à la fin
                textFieldValue = TextFieldValue(
                    text = newText,
                    selection = TextRange(newText.length)
                )
            },
            modifier = Modifier
                .size(1.dp) // Très petit mais pas 0
                .focusRequester(focusRequester),
            enabled = enabled,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword
            ),
            cursorBrush = SolidColor(Color.Transparent),
            decorationBox = { innerTextField ->
                // Pas de décoration visible
                Box(modifier = Modifier.size(1.dp)) {
                    innerTextField()
                }
            }
        )
    }

    // Auto-focus au démarrage
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300) // Petit délai pour éviter le crash
        focusRequester.requestFocus()
        keyboardController?.show()
    }
}

/**
 * Une case pour un chiffre du OTP
 */
@Composable
fun OTPBox(
    digit: String,
    isFocused: Boolean,
    enabled: Boolean
) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .background(
                color = Color(0xFF2D3250),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 2.dp,
                color = when {
                    !enabled -> Color(0xFF374151)
                    isFocused -> Color(0xFF7C3AED)
                    digit.isNotEmpty() -> Color(0xFF10B981)
                    else -> Color(0xFF374151)
                },
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit.ifEmpty { "•" },
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            ),
            color = if (digit.isNotEmpty()) Color.White else Color(0xFF6B7280),
            textAlign = TextAlign.Center
        )
    }
}
