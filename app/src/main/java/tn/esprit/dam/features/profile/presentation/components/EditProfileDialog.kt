package tn.esprit.dam.features.profile.presentation.components
import tn.esprit.dam.R
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tn.esprit.dam.core.model.User
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners

@Composable
fun EditProfileDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (String?) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.surface,
        title = {
            Text(
                stringResource(R.string.edit_profile),
                color = AppColors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name), color = AppColors.textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Person,
                            null,
                            tint = AppColors.textTertiary
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(AppCorners.medium),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AppColors.textPrimary,
                        unfocusedTextColor = AppColors.textPrimary,
                        focusedBorderColor = AppColors.primary,
                        unfocusedBorderColor = AppColors.surfaceVariant,
                        focusedContainerColor = AppColors.surfaceVariant,
                        unfocusedContainerColor = AppColors.surfaceVariant,
                        cursorColor = AppColors.primary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val currentName = user.name
                    onSave(if (name != currentName) name else null)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.primary
                ),
                shape = RoundedCornerShape(AppCorners.medium)
            ) {
                Text(stringResource(R.string.save), color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = AppColors.textSecondary
                )
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
        shape = RoundedCornerShape(AppCorners.xlarge)
    )
}
