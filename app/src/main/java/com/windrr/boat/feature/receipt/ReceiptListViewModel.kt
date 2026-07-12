package com.windrr.boat.feature.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windrr.boat.data.remote.ApiErrorParser
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
    /** 다음 페이지 로딩 중 여부 (하단 푸터 인디케이터). iOS ReceiptListViewModel.isLoadingMore 대응. */
    val isLoadingMore: Boolean = false,
    val hasNext: Boolean = false,
    val nextCursor: String? = null,
    val error: String? = null,
    val selectedTab: ReceiptTab = ReceiptTab.ALL,
    val selectedFilter: ReceiptFilter = ReceiptFilter.ALL,
    val selectedSort: ReceiptSort = ReceiptSort.DEFAULT,
    /** 삭제 실패 시 1회성 에러 메시지 (토스트 표시용, 목록 전체 에러 화면과는 분리) */
    val deleteError: String? = null,
    /** 삭제 성공 1회성 신호 (성공 토스트 표시용) */
    val deleteSuccess: Boolean = false,
)

sealed class ReceiptListIntent {
    data class SelectTab(val tab: ReceiptTab) : ReceiptListIntent()
    data class SelectFilter(val filter: ReceiptFilter) : ReceiptListIntent()
    data class SelectSort(val sort: ReceiptSort) : ReceiptListIntent()
    /** 홈 화면 등 외부 진입 시 초기 탭/정렬 1회 적용 */
    data class ApplyInitial(val tab: ReceiptTab?, val sort: ReceiptSort?) : ReceiptListIntent()
    data object Refresh : ReceiptListIntent()
    /** 마지막 항목 근처까지 스크롤됐을 때 다음 페이지 추가 조회. iOS loadMoreIfNeeded 대응. */
    data object LoadMore : ReceiptListIntent()
    data class DeleteReceipt(val receiptId: String) : ReceiptListIntent()
    data object ConsumeDeleteError : ReceiptListIntent()
    data object ConsumeDeleteSuccess : ReceiptListIntent()
}

class ReceiptListViewModel : ViewModel() {

    private val repository = ReceiptRepository()

    private val _state = MutableStateFlow(ReceiptListState())
    val state: StateFlow<ReceiptListState> = _state.asStateFlow()

    /** 재조회 세대 토큰 — 탭/정렬/필터가 바뀌는 동안 진행 중이던 이전 요청 결과를 무시한다.
     *  iOS ReceiptListViewModel의 generation 패턴과 동일. */
    private var generation = 0

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
            ReceiptListIntent.LoadMore -> loadMore()
            is ReceiptListIntent.DeleteReceipt -> deleteReceipt(intent.receiptId)
            ReceiptListIntent.ConsumeDeleteError -> _state.update { it.copy(deleteError = null) }
            ReceiptListIntent.ConsumeDeleteSuccess -> _state.update { it.copy(deleteSuccess = false) }
        }
    }

    /** 서버 삭제 성공 시에만 목록에서 즉시 제거 — 실패하면 목록은 그대로 두고 에러만 알림 */
    private fun deleteReceipt(receiptId: String) {
        viewModelScope.launch {
            repository.deleteReceipt(receiptId).fold(
                onSuccess = {
                    _state.update { s ->
                        s.copy(
                            receipts = s.receipts.filterNot { it.receiptId == receiptId },
                            totalCount = (s.totalCount - 1).coerceAtLeast(0),
                            deleteSuccess = true,
                        )
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(deleteError = ApiErrorParser.message(e)) }
                },
            )
        }
    }

    /** 탭/정렬/필터 변경 또는 새로고침 — 첫 페이지부터 다시 조회 */
    private fun loadReceipts() {
        generation += 1
        val token = generation
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isLoadingMore = false, error = null) }
            val s = _state.value
            repository.getReceipts(
                status   = s.selectedTab.toApiStatus(),
                sort     = s.selectedSort.toApiSort(),
                limit    = PAGE_SIZE,
                category = s.selectedFilter.toApiCategory(),
            ).fold(
                onSuccess = { data ->
                    if (token != generation) return@fold  // 더 최신 요청이 들어왔으면 폐기
                    _state.update { it.copy(
                        receipts   = data.receipts,
                        totalCount = data.totalCount,
                        hasNext    = data.pagination.hasNext,
                        nextCursor = data.pagination.nextCursor,
                        isLoading  = false,
                    )}
                },
                onFailure = { e ->
                    if (token != generation) return@fold
                    _state.update { it.copy(isLoading = false, error = ApiErrorParser.message(e)) }
                },
            )
        }
    }

    /** 리스트 마지막 근처까지 스크롤됐을 때 다음 페이지를 이어붙인다. iOS loadMore() 대응. */
    private fun loadMore() {
        val s = _state.value
        val cursor = s.nextCursor
        if (!s.hasNext || s.isLoading || s.isLoadingMore || cursor == null) return

        val token = generation
        viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true) }
            repository.getReceipts(
                status   = s.selectedTab.toApiStatus(),
                sort     = s.selectedSort.toApiSort(),
                limit    = PAGE_SIZE,
                cursor   = cursor,
                category = s.selectedFilter.toApiCategory(),
            ).fold(
                onSuccess = { data ->
                    if (token != generation) return@fold
                    _state.update {
                        it.copy(
                            receipts   = it.receipts + data.receipts,
                            totalCount = data.totalCount,
                            hasNext    = data.pagination.hasNext,
                            nextCursor = data.pagination.nextCursor,
                            isLoadingMore = false,
                        )
                    }
                },
                onFailure = {
                    if (token != generation) return@fold
                    // 추가 로드 실패는 조용히 멈춘다(iOS와 동일) — 다음 스크롤에서 재시도되진 않으므로
                    // hasNext를 내려 더 이상 트리거되지 않게 한다.
                    _state.update { it.copy(isLoadingMore = false, hasNext = false) }
                },
            )
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}

private fun ReceiptTab.toApiStatus() = when (this) {
    ReceiptTab.ALL      -> "all"
    ReceiptTab.EXPIRING -> "expiring"
    ReceiptTab.EXPIRED  -> "expired"
}

private fun ReceiptSort.toApiSort() = when (this) {
    ReceiptSort.DEFAULT     -> "title"
    ReceiptSort.RECENT      -> "recent"
    ReceiptSort.EXPIRING    -> "expiresOn"
    ReceiptSort.PURCHASE    -> "purchaseDate"
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
