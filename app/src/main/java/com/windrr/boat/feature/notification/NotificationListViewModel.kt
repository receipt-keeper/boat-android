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
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
    private val userDataStore = ApiClient.userDataStore

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
                    
                    // 💡 서버에서 받아온 원본 알림 중 가장 최신 생성 시각을 기록 (Red Dot 제거용)
                    // (기기 시각에 의존하지 않고 서버가 준 데이터 중 가장 최신 기준점을 잡음)
                    dtos.maxByOrNull { it.createdAt ?: "" }?.createdAt?.let { latest ->
                        markAsViewed(latest)
                    }
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

        // 💡 모든 알림이 목록에 표시되도록 탭 필터링 제거 (필요 시 탭 메뉴 자체를 수정할 수도 있으나, 현재는 ALL로 간주)
        var filtered = source

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

    /** 서버에서 받아온 알림 중 가장 최신 것의 시각을 기록하여 Red Dot 제거 */
    fun markAsViewed(latestCreatedAt: String?) {
        if (latestCreatedAt.isNullOrBlank()) return
        viewModelScope.launch {
            userDataStore.updateLastNotifViewedAt(latestCreatedAt)
        }
    }

    /**
     * 카드 탭 처리 — 읽음 API 호출 후 목록에서 즉시 제거(낙관적).
     * 화면 이동은 호출부(Screen)가 [AppNotification]의 resourceType/kind를 보고 결정한다.
     */
    fun onNotificationClicked(item: AppNotification) {
        // 💡 이미 읽은 알림이더라도 상세 화면 이동 등을 위해 클릭은 허용하되, 읽음 처리 API 호출만 스킵
        if (item.isRead) return

        // 낙관적 읽음 상태 변경
        _state.update { s -> 
            s.copy(
                notifications = s.notifications.map { 
                    if (it.id == item.id) it.copy(isRead = true) else it 
                }
            ) 
        }
        viewModelScope.launch {
            runCatching { api.markNotificationRead(item.id) }
                .onFailure { BoatLog.e("알림 읽음 처리 실패 id=${item.id}", it) }
        }
    }

    /**
     * 케밥 → "삭제하기" — 삭제 API 호출 성공 후에만 목록을 다시 불러와 반영한다.
     * 실패 시 호출부(Screen)가 토스트로 안내할 수 있도록 결과를 반환한다.
     * Response<Unit>은 2xx가 아니어도 예외를 던지지 않으므로 isSuccessful을 직접 확인해
     * 실패를 Result.failure로 변환한다.
     */
    suspend fun delete(item: AppNotification): Result<Unit> = runCatching {
        val response = api.deleteNotification(item.id)
        check(response.isSuccessful) { "알림 삭제 실패 code=${response.code()}" }
    }.onSuccess { load() }
        .onFailure { BoatLog.e("알림 삭제 실패 id=${item.id}", it) }
}
