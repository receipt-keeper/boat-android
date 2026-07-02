package com.windrr.boat.feature.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windrr.boat.data.remote.model.ReceiptItem
import com.windrr.boat.data.repository.ReceiptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReceiptListState(
    val receipts: List<ReceiptItem> = emptyList(),
    val totalCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTab: ReceiptTab = ReceiptTab.ALL,
    val selectedFilter: ReceiptFilter = ReceiptFilter.ALL,
    val selectedSort: ReceiptSort = ReceiptSort.DEFAULT,
)

sealed class ReceiptListIntent {
    data class SelectTab(val tab: ReceiptTab) : ReceiptListIntent()
    data class SelectFilter(val filter: ReceiptFilter) : ReceiptListIntent()
    data class SelectSort(val sort: ReceiptSort) : ReceiptListIntent()
    /** 홈 화면 등 외부 진입 시 초기 탭/정렬 1회 적용 */
    data class ApplyInitial(val tab: ReceiptTab?, val sort: ReceiptSort?) : ReceiptListIntent()
    data object Refresh : ReceiptListIntent()
}

class ReceiptListViewModel : ViewModel() {

    private val repository = ReceiptRepository()

    private val _state = MutableStateFlow(ReceiptListState())
    val state: StateFlow<ReceiptListState> = _state.asStateFlow()

    fun handleIntent(intent: ReceiptListIntent) {
        when (intent) {
            is ReceiptListIntent.SelectTab -> {
                _state.update { it.copy(selectedTab = intent.tab) }
                loadReceipts()
            }
            is ReceiptListIntent.SelectFilter -> {
                _state.update { it.copy(selectedFilter = intent.filter) }
                loadReceipts()
            }
            is ReceiptListIntent.SelectSort -> {
                _state.update { it.copy(selectedSort = intent.sort) }
                loadReceipts()
            }
            is ReceiptListIntent.ApplyInitial -> {
                _state.update { s ->
                    var next = s
                    if (intent.tab != null)  next = next.copy(selectedTab  = intent.tab)
                    if (intent.sort != null) next = next.copy(selectedSort = intent.sort)
                    next
                }
                // 초기 진입 시엔 변경 여부와 무관하게 항상 최신화
                loadReceipts()
            }
            ReceiptListIntent.Refresh -> loadReceipts()
        }
    }

    private fun loadReceipts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val s = _state.value
            repository.getReceipts(
                status   = s.selectedTab.toApiStatus(),
                sort     = s.selectedSort.toApiSort(),
                limit    = 20,
                category = s.selectedFilter.toApiCategory(),
            ).fold(
                onSuccess = { data ->
                    _state.update { it.copy(
                        receipts   = data.receipts,
                        totalCount = data.pagination.totalCount,
                        isLoading  = false,
                    )}
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                },
            )
        }
    }
}

private fun ReceiptTab.toApiStatus() = when (this) {
    ReceiptTab.ALL      -> "all"
    ReceiptTab.EXPIRING -> "expiring"
    ReceiptTab.EXPIRED  -> "expired"
}

private fun ReceiptSort.toApiSort() = when (this) {
    ReceiptSort.DEFAULT, ReceiptSort.RECENT -> "recent"
    ReceiptSort.EXPIRING                    -> "expiresOn"
    ReceiptSort.PURCHASE                    -> "purchaseDate"
}

// 서버 category 문자열과 정확히 일치해야 필터가 걸린다 (등록/OCR과 동일한 가이드 기준 문자열)
private fun ReceiptFilter.toApiCategory(): String? = when (this) {
    ReceiptFilter.ALL     -> null
    ReceiptFilter.IT      -> "IT 제품"
    ReceiptFilter.LAUNDRY -> "세탁/청소"
    ReceiptFilter.KITCHEN -> "주방 가전"
    ReceiptFilter.LIVING  -> "리빙/냉난방"
    ReceiptFilter.OTHER   -> "기타 제품"
}
