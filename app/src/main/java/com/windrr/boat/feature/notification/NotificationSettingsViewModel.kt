package com.windrr.boat.feature.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windrr.boat.data.model.User
import com.windrr.boat.data.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 알림 설정 화면 ViewModel — 알림/마케팅 수신 설정을 UserDataStore에 영속화.
 */
class NotificationSettingsViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    val user: StateFlow<User> = userRepository.user
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), User())

    fun setNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch { userRepository.updateNotificationEnabled(enabled) }
    }

    fun setMarketingConsent(consent: Boolean) {
        viewModelScope.launch { userRepository.updateMarketingConsent(consent) }
    }
}
