package tn.esprit.dam.features.darkweb.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.asString
import tn.esprit.dam.core.ui.components.AppErrorAlert
import tn.esprit.dam.core.ui.components.AppErrorState
import tn.esprit.dam.core.ui.components.AppPrimaryButton
import tn.esprit.dam.core.ui.components.AppSkeletonList
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography
import tn.esprit.dam.features.darkweb.data.model.Breach
import tn.esprit.dam.features.darkweb.data.model.ManualBreachResult

@Composable
fun DarkWebMonitoringScreen(
    onBreachClick: (String) -> Unit,
    viewModel: DarkWebViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = AppSpacing.md, end = AppSpacing.sm, top = AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.dark_web_monitoring),
                style = AppTypography.titleLarge,
                color = AppColors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            if (uiState.isLoading && selectedTab == 0) {
                CircularProgressIndicator(color = AppColors.primary, strokeWidth = 2.dp, modifier = Modifier.padding(AppSpacing.md).size(20.dp))
            } else {
                IconButton(onClick = { viewModel.checkNow() }) {
                    Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.scan_now), tint = AppColors.primary)
                }
            }
        }

        SegmentedTabs(
            labels = listOf(stringResource(R.string.darkweb_tab_breaches), stringResource(R.string.darkweb_tab_manual)),
            selected = selectedTab,
            onSelect = { selectedTab = it }
        )

        if (selectedTab == 0) {
            BreachesTab(
                uiState = uiState,
                onRetry = { viewModel.loadBreaches() },
                onCheckNow = { viewModel.checkNow() },
                onBreachClick = onBreachClick
            )
        } else {
            LaunchedEffect(Unit) { viewModel.resetManualState() }
            ManualCheckContent(viewModel, uiState)
        }
    }
}

@Composable
private fun SegmentedTabs(labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppSpacing.md)
            .clip(RoundedCornerShape(AppCorners.large))
            .background(AppColors.surface)
            .padding(AppSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        labels.forEachIndexed { index, label ->
            val isSelected = index == selected
            Text(
                text = label,
                style = AppTypography.labelLarge,
                color = if (isSelected) AppColors.primary else AppColors.textTertiary,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(AppCorners.medium))
                    .background(if (isSelected) AppColors.primaryContainer else AppColors.surface)
                    .clickable { onSelect(index) }
                    .padding(vertical = AppSpacing.sm + AppSpacing.xs),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun BreachesTab(
    uiState: DarkWebUiState,
    onRetry: () -> Unit,
    onCheckNow: () -> Unit,
    onBreachClick: (String) -> Unit
) {
    val error = uiState.error
    when {
        uiState.isLoading && uiState.breaches.isEmpty() -> AppSkeletonList(itemCount = 3, modifier = Modifier.padding(horizontal = AppSpacing.md))
        // An error must never look like "no breaches": show it, with a retry.
        error != null && uiState.breaches.isEmpty() -> AppErrorState(
            message = error.asString(),
            onRetry = onRetry,
            retryText = stringResource(R.string.retry)
        )
        else -> {
            val open = uiState.breaches.filterNot { it.isResolved }
            val resolved = uiState.breaches.filter { it.isResolved }
            LazyColumn(
                contentPadding = PaddingValues(start = AppSpacing.md, end = AppSpacing.md, bottom = AppSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                if (error != null) {
                    item { AppErrorAlert(message = error.asString(), modifier = Modifier.fillMaxWidth()) }
                }
                item {
                    BreachStatusHero(
                        openCount = open.size,
                        resolvedCount = resolved.size,
                        checking = uiState.isLoading,
                        onCheckNow = onCheckNow
                    )
                }
                breachSection(R.string.darkweb_section_open, open, onBreachClick)
                breachSection(R.string.darkweb_section_resolved, resolved, onBreachClick)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.breachSection(
    title: Int,
    breaches: List<Breach>,
    onBreachClick: (String) -> Unit
) {
    if (breaches.isEmpty()) return
    item(key = "section-$title") {
        Text(
            text = stringResource(title),
            style = AppTypography.titleMedium,
            color = AppColors.textPrimary,
            modifier = Modifier.padding(top = AppSpacing.md, bottom = AppSpacing.xs)
        )
    }
    items(breaches, key = { it._id }) { breach ->
        BreachCard(breach = breach, onClick = { onBreachClick(breach._id) })
    }
}

/** Big status: how many breaches still need action, and the one primary action of the tab. */
@Composable
private fun BreachStatusHero(openCount: Int, resolvedCount: Int, checking: Boolean, onCheckNow: () -> Unit) {
    val clear = openCount == 0
    val color = if (clear) AppColors.riskLow else AppColors.riskHigh
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCorners.xlarge))
            .background(AppColors.surface)
            .background(Brush.radialGradient(listOf(color.copy(alpha = 0.16f), Color.Transparent), radius = 600f))
            .padding(AppSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(50))
                .background(color.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (clear) Icons.Default.VerifiedUser else Icons.Default.GppMaybe,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
        }
        Text(
            text = if (clear) stringResource(R.string.darkweb_all_clear) else pluralStringResource(R.plurals.darkweb_open_n, openCount, openCount),
            style = AppTypography.titleLarge,
            color = AppColors.textPrimary
        )
        Text(
            text = stringResource(if (clear) R.string.darkweb_all_clear_desc else R.string.darkweb_open_desc),
            style = AppTypography.bodyMedium,
            color = AppColors.textSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (resolvedCount > 0) {
            Text(
                text = pluralStringResource(R.plurals.darkweb_resolved_n, resolvedCount, resolvedCount),
                style = AppTypography.labelMedium,
                color = AppColors.textTertiary
            )
        }
        Spacer(modifier = Modifier.size(AppSpacing.xs))
        AppPrimaryButton(text = stringResource(R.string.darkweb_check_now), onClick = onCheckNow, loading = checking, enabled = !checking)
    }
}

@Composable
fun ManualCheckContent(viewModel: DarkWebViewModel, uiState: DarkWebUiState) {
    var email by rememberSaveable { mutableStateOf("") }
    // remember, not rememberSaveable: the password must never go into the saved instance state.
    var password by remember { mutableStateOf("") }
    var checkedEmail by rememberSaveable { mutableStateOf("") }
    var running by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) running = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = AppSpacing.md, end = AppSpacing.md, bottom = AppSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        CheckCard(
            icon = Icons.Default.Email,
            title = stringResource(R.string.check_email_address),
            description = stringResource(R.string.check_if_your_email_has_been_exposed_in)
        ) {
            CheckField(
                value = email,
                onValueChange = { email = it },
                placeholder = stringResource(R.string.example_email_com),
                keyboardType = KeyboardType.Email
            )
            AppPrimaryButton(
                text = stringResource(R.string.check_email),
                onClick = {
                    checkedEmail = email.trim()
                    running = "email"
                    viewModel.manualCheckEmail(email.trim())
                },
                enabled = email.isNotBlank() && !uiState.isLoading,
                loading = running == "email"
            )
            uiState.manualEmailResult?.let { results -> EmailResult(results, checkedEmail) }
        }

        uiState.error?.let { AppErrorAlert(message = it.asString(), modifier = Modifier.fillMaxWidth()) }

        CheckCard(
            icon = Icons.Default.Key,
            title = stringResource(R.string.check_password_safety),
            description = stringResource(R.string.we_check_securely_using_k_anonymity_only)
        ) {
            CheckField(
                value = password,
                onValueChange = { password = it },
                placeholder = stringResource(R.string.enter_password_to_check),
                keyboardType = KeyboardType.Password,
                isPassword = true
            )
            AppPrimaryButton(
                text = stringResource(R.string.check_password),
                onClick = {
                    running = "password"
                    viewModel.manualCheckPassword(password)
                },
                enabled = password.isNotBlank() && !uiState.isLoading,
                loading = running == "password"
            )
            if (uiState.passwordCheckPerformed) {
                val count = uiState.manualPasswordCount ?: 0
                ResultBanner(
                    safe = count == 0,
                    title = if (count == 0) stringResource(R.string.darkweb_password_safe) else stringResource(R.string.darkweb_password_found, count)
                )
            }
        }
    }
}

@Composable
private fun CheckCard(
    icon: ImageVector,
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCorners.large))
            .background(AppColors.surface)
            .padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(AppCorners.medium))
                    .background(AppColors.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = AppColors.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(AppSpacing.md))
            Text(text = title, style = AppTypography.titleMedium, color = AppColors.textPrimary)
        }
        Text(text = description, style = AppTypography.bodyMedium, color = AppColors.textSecondary)
        content()
    }
}

@Composable
private fun CheckField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text(placeholder, color = AppColors.textMuted) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(AppCorners.medium),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AppColors.textPrimary,
            unfocusedTextColor = AppColors.textPrimary,
            focusedBorderColor = AppColors.primary,
            unfocusedBorderColor = AppColors.surfaceVariant,
            focusedContainerColor = AppColors.background,
            unfocusedContainerColor = AppColors.background,
            cursorColor = AppColors.primary
        )
    )
}

@Composable
private fun EmailResult(results: List<ManualBreachResult>, checkedEmail: String) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        ResultBanner(
            safe = results.isEmpty(),
            title = if (results.isEmpty()) stringResource(R.string.darkweb_email_safe)
            else pluralStringResource(R.plurals.darkweb_email_found, results.size, results.size),
            subtitle = checkedEmail.takeIf { it.isNotBlank() }
        )
        results.forEach { breach ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppCorners.medium))
                    .background(AppColors.background)
                    .padding(AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = breach.name.ifBlank { breach.domain },
                        style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = AppColors.textPrimary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    formatBreachDate(breach.breachDate, "d MMM yyyy")?.let {
                        Text(text = it, style = AppTypography.labelMedium, color = AppColors.textTertiary)
                    }
                }
                if (breach.dataClasses.isNotEmpty()) {
                    DataClassChips(breach.dataClasses.sortedByDescending { it.isSensitiveData() }, max = 4)
                }
            }
        }
    }
}

@Composable
private fun ResultBanner(safe: Boolean, title: String, subtitle: String? = null) {
    val color = if (safe) AppColors.riskLow else AppColors.riskHigh
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCorners.medium))
            .background(color.copy(alpha = 0.10f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(AppCorners.medium))
            .padding(AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(if (safe) Icons.Default.VerifiedUser else Icons.Default.Warning, contentDescription = null, tint = color)
        Spacer(modifier = Modifier.width(AppSpacing.md))
        Column {
            Text(text = title, style = AppTypography.labelLarge, color = color)
            subtitle?.let { Text(text = it, style = AppTypography.labelMedium, color = AppColors.textSecondary) }
        }
    }
}
