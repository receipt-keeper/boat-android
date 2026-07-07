package com.windrr.boat.feature.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windrr.boat.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** 공통 헤더의 알림 벨 배지용 — 읽지 않은 알림이 하나라도 있는지만 확인한다. */
class NotificationBadgeViewModel : ViewModel() {

    private val _hasUnread = MutableStateFlow(false)
    val hasUnread: StateFlow<Boolean> = _hasUnread.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            runCatching { ApiClient.notificationApiService.getNotifications().data.notifications }
                .onSuccess { list -> _hasUnread.value = list.any { it.readAt == null } }
        }
    }
}
