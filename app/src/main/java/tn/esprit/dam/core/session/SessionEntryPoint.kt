package tn.esprit.dam.core.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SessionEntryPoint {
    fun tokenRepository(): TokenRepository
}

@Composable
fun rememberTokenRepository(): TokenRepository {
    val appContext = LocalContext.current.applicationContext
    return remember(appContext) {
        EntryPointAccessors.fromApplication(appContext, SessionEntryPoint::class.java).tokenRepository()
    }
}
