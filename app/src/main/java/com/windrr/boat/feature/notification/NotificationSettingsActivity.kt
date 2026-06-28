package com.windrr.boat.feature.notification

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.windrr.boat.data.remote.ApiClient
import com.windrr.boat.data.repository.UserRepositoryImpl
import com.windrr.boat.ui.theme.BoatTheme

class NotificationSettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BoatTheme {
                val viewModel: NotificationSettingsViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return NotificationSettingsViewModel(
                                UserRepositoryImpl(ApiClient.userDataStore, ApiClient.userApiService, ApiClient.notificationApiService)
                            ) as T
                        }
                    }
                )
                NotificationSettingsScreen(
                    viewModel = viewModel,
                    onBack = { finish() },
                )
            }
        }
    }
}
