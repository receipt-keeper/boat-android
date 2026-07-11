package com.windrr.boat.feature.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windrr.boat.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** 공통 헤더의 알림 벨 배지용 — 마지막 확인 이후 새로운 알림이 있는지 체크한다. */
class NotificationBadgeViewModel : ViewModel() {

    private val _hasUnread = MutableStateFlow(false)
    val hasUnread: StateFlow<Boolean> = _hasUnread.asStateFlow()

    private val userDataStore = ApiClient.userDataStore

    fun refresh() {
        viewModelScope.launch {
            val lastViewedAt = userDataStore.lastNotifViewedAt.first()
            
            runCatching { ApiClient.notificationApiService.getNotifications().data.notifications }
                .onSuccess { list ->
                    if (list.isEmpty()) {
                        _hasUnread.value = false
                        return@onSuccess
                    }

                    // 1) 아직 읽지 않은 알림이 있는지 기본 체크
                    val hasUnreadAtServer = list.any { it.readAt == null }
                    
                    if (!hasUnreadAtServer) {
                        _hasUnread.value = false
                        return@onSuccess
                    }

                    // 2) 마지막 확인 시각(lastViewedAt)보다 나중에 생성된 알림이 있는지 체크
                    if (lastViewedAt == null) {
                        // 기록이 없으면 읽지 않은 알림이 있는 것만으로 Red Dot 노출
                        _hasUnread.value = true
                    } else {
                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREA).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }
                        val lastViewedMs = sdf.parse(lastViewedAt)?.time ?: 0L
                        
                        // 서버 리스트 중 하나라도 lastViewedMs 이후에 생성된 알림이 있으면 Red Dot 노출
                        val hasNewNotif = list.any { 
                            val createdMs = sdf.parse(it.createdAt ?: "")?.time ?: 0L
                            createdMs > lastViewedMs
                        }
                        _hasUnread.value = hasNewNotif
                    }
                }
        }
    }
}
