package tn.esprit.dam.features.scan.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import tn.esprit.dam.data.local.AppScanner
import tn.esprit.dam.data.repository.ScanRepository

class ScanViewModelFactory(
    private val repository: ScanRepository,
    private val appScanner: AppScanner,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ScanViewModel::class.java) -> {
                ScanViewModel(repository, appScanner, context) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repository, context) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
