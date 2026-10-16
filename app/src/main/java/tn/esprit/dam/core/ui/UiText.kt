package tn.esprit.dam.core.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/** Text a ViewModel can emit without holding a Context; resolved in the UI. */
sealed interface UiText {
    data class Raw(val value: String) : UiText

    class Res(@StringRes val id: Int, vararg val args: Any) : UiText
}

/** A server/exception message when there is one, otherwise the given fallback resource. */
fun String?.toUiTextOr(@StringRes fallback: Int): UiText =
    if (isNullOrBlank()) UiText.Res(fallback) else UiText.Raw(this)

@Composable
fun UiText.asString(): String = when (this) {
    is UiText.Raw -> value
    is UiText.Res -> stringResource(id, *args)
}

/** For non-composable code such as a LaunchedEffect showing a snackbar. */
fun UiText.asString(context: Context): String = when (this) {
    is UiText.Raw -> value
    is UiText.Res -> context.getString(id, *args)
}
