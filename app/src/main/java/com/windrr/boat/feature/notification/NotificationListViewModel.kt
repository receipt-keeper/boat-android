package com.windrr.boat.feature.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windrr.boat.core.log.BoatLog
import com.windrr.boat.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationListState(
    val notifications: List<AppNotification> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

class NotificationListViewModel : ViewModel() {

    private val api = ApiClient.notificationApiService

    private val _state = MutableStateFlow(NotificationListState())
    val state: StateFlow<NotificationListState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching { api.getNotifications().data.notifications }
                .onSuccess { dtos ->
                    // 아직 읽지 않은 알림만 노출 (읽으면 리스트에서 제거 — PRD 기준)
                    val items = dtos.filter { it.readAt == null }.map { it.toAppNotification() }
                    _state.update { it.copy(notifications = items, isLoading = false) }
                }
                .onFailure { e ->
                    BoatLog.e("알림 목록 조회 실패", e)
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    /**
     * 카드 탭 처리 — 읽음 API 호출 후 목록에서 즉시 제거(낙관적).
     * 화면 이동은 호출부(Screen)가 [AppNotification]의 resourceType/kind를 보고 결정한다.
     */
    fun onNotificationClicked(item: AppNotification) {
        // 낙관적 제거 — 실패해도 서버가 읽음 처리 안 됐을 뿐, 다음 조회 때 다시 나타난다
        _state.update { s -> s.copy(notifications = s.notifications.filterNot { it.id == item.id }) }
        viewModelScope.launch {
            runCatching { api.markNotificationRead(item.id) }
                .onFailure { BoatLog.e("알림 읽음 처리 실패 id=${item.id}", it) }
        }
    }
}
