package com.windrr.boat.feature.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windrr.boat.core.log.BoatLog
import com.windrr.boat.data.model.User
import com.windrr.boat.data.remote.ApiErrorParser
import com.windrr.boat.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 알림 설정 화면 ViewModel — 진입 시 GET /notifications/settings 로 최신 설정을 조회하고,
 * 토글 변경 시 PATCH /notifications/settings 로 서버에 반영하며 로컬에도 캐시한다.
 */
class NotificationSettingsViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    val user: StateFlow<User> = userRepository.user
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), User())

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // 화면 진입 시 서버에서 최신 알림/마케팅 설정을 GET 하여 로컬 캐시 갱신
        viewModelScope.launch {
            userRepository.refreshNotificationSettings()
                .onFailure { BoatLog.e("알림 설정 조회 실패", it) }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun setNotificationEnabled(enabled: Boolean) {
        if (user.value.notificationEnabled == enabled) return // 변경 없음 → 불필요한 PATCH 방지
        viewModelScope.launch {
            userRepository.updateMe(notificationEnabled = enabled)
                .onFailure {
                    BoatLog.e("알림 수신 설정 변경 실패", it)
                    _error.value = ApiErrorParser.message(it)
                    userRepository.refreshUser() // 실패 시 서버 기준으로 복구
                }
        }
    }

    fun setMarketingConsent(consent: Boolean) {
        if (user.value.marketingConsent == consent) return
        viewModelScope.launch {
            userRepository.updateMe(marketingConsent = consent)
                .onFailure {
                    BoatLog.e("마케팅 수신 동의 변경 실패", it)
                    _error.value = ApiErrorParser.message(it)
                    userRepository.refreshUser()
                }
        }
    }
}
