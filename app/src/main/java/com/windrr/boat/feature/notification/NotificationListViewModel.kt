package com.windrr.boat.feature.notification

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windrr.boat.R
import com.windrr.boat.core.log.BoatLog
import com.windrr.boat.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 알림 탭 유형 */
enum class NotificationTab(@StringRes val titleRes: Int) {
    ALL(R.string.notif_tab_all),
    UNREAD(R.string.notif_tab_unread),
    READ(R.string.notif_tab_read),
}

/** 카테고리 필터 */
enum class NotificationFilter(@StringRes val labelRes: Int) {
    ALL(R.string.notif_filter_all),
    RECEIPT(R.string.notif_filter_receipt),
    SYSTEM(R.string.notif_filter_system),
}

/** 정렬 옵션 */
enum class NotificationSort(@StringRes val labelRes: Int) {
    RECENT(R.string.notif_sort_recent),
    OLDEST(R.string.notif_sort_oldest),
}

data class NotificationListState(
    val notifications: List<AppNotification> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedTab: NotificationTab = NotificationTab.ALL,
    val selectedFilter: NotificationFilter = NotificationFilter.ALL,
    val selectedSort: NotificationSort = NotificationSort.RECENT,
    val totalCount: Int = 0,
)

class NotificationListViewModel : ViewModel() {

    private val api = ApiClient.notificationApiService

    private val _state = MutableStateFlow(NotificationListState())
    val state: StateFlow<NotificationListState> = _state.asStateFlow()

    // 전체 알림 목록 캐싱 (필터링/정렬용)
    private var cachedAllNotifications: List<AppNotification> = emptyList()

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching { api.getNotifications().data.notifications }
                .onSuccess { dtos ->
                    cachedAllNotifications = dtos.map { it.toAppNotification() }
                    applyFiltersAndSort()
                    _state.update { it.copy(isLoading = false) }
                }
                .onFailure { e ->
                    BoatLog.e("알림 목록 조회 실패", e)
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun selectTab(tab: NotificationTab) {
        _state.update { it.copy(selectedTab = tab) }
        applyFiltersAndSort()
    }

    fun selectFilter(filter: NotificationFilter) {
        _state.update { it.copy(selectedFilter = filter) }
        applyFiltersAndSort()
    }

    fun selectSort(sort: NotificationSort) {
        _state.update { it.copy(selectedSort = sort) }
        applyFiltersAndSort()
    }

    private fun applyFiltersAndSort() {
        val source = cachedAllNotifications

        var filtered = when (_state.value.selectedTab) {
            NotificationTab.ALL -> source
            NotificationTab.UNREAD -> source.filter { it.date.isNotBlank() } // 읽지 않은 알림 (실제 로직은 readAt 체크)
            NotificationTab.READ -> emptyList() // 읽은 알림 (실제 로직은 readAt 체크)
        }

        filtered = when (_state.value.selectedFilter) {
            NotificationFilter.ALL -> filtered
            NotificationFilter.RECEIPT -> filtered.filter { it.resourceType == "receipt" }
            NotificationFilter.SYSTEM -> filtered.filter { it.resourceType != "receipt" }
        }

        filtered = when (_state.value.selectedSort) {
            NotificationSort.RECENT -> filtered.sortedByDescending { it.date }
            NotificationSort.OLDEST -> filtered.sortedBy { it.date }
        }

        _state.update {
            it.copy(
                notifications = filtered,
                totalCount = source.size
            )
        }
    }

    /**
     * 카드 탭 처리 — 읽음 API 호출 후 목록에서 즉시 제거(낙관적).
     * 화면 이동은 호출부(Screen)가 [AppNotification]의 resourceType/kind를 보고 결정한다.
     */
    fun onNotificationClicked(item: AppNotification) {
        // 낙관적 제거 — 실패해도 서버가 읽음 처리 안 됐을 뿐, 다음 조회 때 다시 나타난다
        _state.update { s -> 
            s.copy(
                notifications = s.notifications.filterNot { it.id == item.id },
                totalCount = s.totalCount - 1
            ) 
        }
        viewModelScope.launch {
            runCatching { api.markNotificationRead(item.id) }
                .onFailure { BoatLog.e("알림 읽음 처리 실패 id=${item.id}", it) }
        }
    }
}
