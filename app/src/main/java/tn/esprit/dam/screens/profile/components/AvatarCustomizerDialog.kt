package tn.esprit.dam.screens.profile.components
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import tn.esprit.dam.data.model.AvatarOptions
import tn.esprit.dam.data.model.UpdateAvatarDto
import tn.esprit.dam.screens.profile.AvatarCustomizerViewModel
import tn.esprit.dam.screens.profile.AvatarCustomizerUiState

@Composable
fun AvatarCustomizerDialog(
    userHash: String,
    onDismiss: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: AvatarCustomizerViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val currentConfig by viewModel.currentConfig.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAvatar(context, userHash)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E2139),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Personnaliser l'avatar",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                // Bouton génération aléatoire
                IconButton(
                    onClick = {
                        viewModel.generateRandomAvatar(context, userHash)
                    }
                ) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = "Aléatoire",
                        tint = Color(0xFF7C3AED)
                    )
                }
            }
        },
        text = {
            when (val state = uiState) {
                is AvatarCustomizerUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF7C3AED))
                    }
                }

                is AvatarCustomizerUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                state.message,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                is AvatarCustomizerUiState.Success -> {
                    AvatarCustomizerContent(
                        avatar = state.avatar,
                        currentConfig = currentConfig,
                        onHairColorChange = { viewModel.updateHairColor(it) },
                        onTopTypeChange = { viewModel.updateTopType(it) },
                        onClotheTypeChange = { viewModel.updateClotheType(it) },
                        onClotheColorChange = { viewModel.updateClotheColor(it) },
                        onSkinColorChange = { viewModel.updateSkinColor(it) },
                        onEyeTypeChange = { viewModel.updateEyeType(it) },
                        onMouthTypeChange = { viewModel.updateMouthType(it) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updateDto = UpdateAvatarDto(
                        topType = currentConfig.topType,
                        hairColor = currentConfig.hairColor,
                        clotheType = currentConfig.clotheType,
                        clotheColor = currentConfig.clotheColor,
                        skinColor = currentConfig.skinColor,
                        eyeType = currentConfig.eyeType,
                        mouthType = currentConfig.mouthType
                    )

                    // ✅ CORRECTION : Le callback reçoit maintenant fileName
                    viewModel.updateAvatarConfig(
                        context = context,
                        userHash = userHash,
                        updateDto = updateDto,
                        onSuccess = { fileName ->
                            // fileName est disponible ici si besoin
                            onSaveSuccess() // Appeler le callback parent
                        }
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7C3AED)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = uiState is AvatarCustomizerUiState.Success
            ) {
                Text("Enregistrer", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFFB4B4C6)
                )
            ) {
                Text("Annuler")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
@Composable
private fun AvatarCustomizerContent(
    avatar: tn.esprit.dam.data.model.Avatar,
    currentConfig: tn.esprit.dam.data.model.AvatarConfig,
    onHairColorChange: (String) -> Unit,
    onTopTypeChange: (String) -> Unit,
    onClotheTypeChange: (String) -> Unit,
    onClotheColorChange: (String) -> Unit,
    onSkinColorChange: (String) -> Unit,
    onEyeTypeChange: (String) -> Unit,
    onMouthTypeChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Preview de l'avatar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = avatar.url,
                contentDescription = "Avatar preview",
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2D3250)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Coiffure
        AvatarCustomizationSection(
            title = "Coiffure",
            options = AvatarOptions.topTypes,
            selectedOption = currentConfig.topType,
            onOptionSelected = onTopTypeChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Couleur de cheveux
        AvatarCustomizationSection(
            title = "Couleur de cheveux",
            options = AvatarOptions.hairColors,
            selectedOption = currentConfig.hairColor,
            onOptionSelected = onHairColorChange,
            isColor = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Vêtements
        AvatarCustomizationSection(
            title = "Vêtements",
            options = AvatarOptions.clotheTypes,
            selectedOption = currentConfig.clotheType,
            onOptionSelected = onClotheTypeChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Couleur des vêtements
        AvatarCustomizationSection(
            title = "Couleur des vêtements",
            options = AvatarOptions.clotheColors,
            selectedOption = currentConfig.clotheColor,
            onOptionSelected = onClotheColorChange,
            isColor = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Teint de peau
        AvatarCustomizationSection(
            title = "Teint de peau",
            options = AvatarOptions.skinColors,
            selectedOption = currentConfig.skinColor,
            onOptionSelected = onSkinColorChange,
            isColor = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Yeux
        AvatarCustomizationSection(
            title = "Yeux",
            options = AvatarOptions.eyeTypes,
            selectedOption = currentConfig.eyeType,
            onOptionSelected = onEyeTypeChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bouche
        AvatarCustomizationSection(
            title = "Bouche",
            options = AvatarOptions.mouthTypes,
            selectedOption = currentConfig.mouthType,
            onOptionSelected = onMouthTypeChange
        )
    }
}
@Composable
private fun AvatarCustomizationSection(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    isColor: Boolean = false
) {
    Column {
        Text(
            text = title,
            color = Color(0xFFB4B4C6),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(options) { option ->
                OptionChip(
                    label = option,
                    isSelected = option == selectedOption,
                    onClick = { onOptionSelected(option) },
                    isColor = isColor
                )
            }
        }
    }
}

@Composable
private fun OptionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isColor: Boolean = false
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFF7C3AED) else Color(0xFF2D3250),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF7C3AED))
        } else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isColor) {
                // Box pour la couleur avec border si blanc
                val colorBox = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(getColorForName(label))

                Box(
                    modifier = if (label.lowercase() == "white") {
                        colorBox.border(
                            width = 1.dp,
                            color = Color.Gray,
                            shape = CircleShape
                        )
                    } else {
                        colorBox
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = label,
                color = if (isSelected) Color.White else Color(0xFFB4B4C6),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// Helper function pour mapper les noms de couleurs
private fun getColorForName(name: String): Color {
    return when (name.lowercase()) {
        "black" -> Color(0xFF000000)
        "brown", "browndark" -> Color(0xFF8B4513)
        "blonde", "blondegolden" -> Color(0xFFFFD700)
        "auburn" -> Color(0xFFA52A2A)
        "red" -> Color(0xFFDC143C)
        "platinum", "silvergray" -> Color(0xFFC0C0C0)
        "pastelpink" -> Color(0xFFFFB6C1)
        "blue01", "blue02", "blue03" -> Color(0xFF0000FF)
        "gray01", "gray02" -> Color(0xFF808080)
        "white" -> Color(0xFFFFFFFF)
        "pink" -> Color(0xFFFFC0CB)
        "pastelblue" -> Color(0xFFADD8E6)
        "pastelgreen" -> Color(0xFF90EE90)
        "pastelorange" -> Color(0xFFFFDAB9)
        "pastelred" -> Color(0xFFFFB6C1)
        "pastelyellow" -> Color(0xFFFFFFE0)
        "heather" -> Color(0xFFB2A4A4)
        "tanned" -> Color(0xFFD2B48C)
        "yellow" -> Color(0xFFFFFF00)
        "pale" -> Color(0xFFFFF5EE)
        "light" -> Color(0xFFFAF0E6)
        "darkbrown" -> Color(0xFF654321)
        else -> Color(0xFF888888)
    }
}