package com.windrr.boat.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windrr.boat.data.repository.ReceiptRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 홈 미리보기 목적이므로 각 리스트 최대 5개까지만 가져온다. */
private const val HOME_EXPIRING_LIMIT = 5
private const val HOME_RECENT_LIMIT = 5

data class HomeState(
    val expiring: List<ExpiringWarranty> = emptyList(),
    val recent: List<RecentReceipt> = emptyList(),
    /** 사용자에게 등록된 영수증이 하나라도 있는지(status=all 기준 totalCount). 초기 홈/일반 홈 분기에 사용. */
    val hasAnyReceipts: Boolean = true,
    val isLoading: Boolean = true,
)

private data class RecentQueryResult(val items: List<RecentReceipt>, val totalCount: Int)

class HomeViewModel : ViewModel() {

    private val repository = ReceiptRepository()

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    /** AS 만료 예정(가로형)은 만료일이 가까운 순, 최근 등록(세로형)은 등록일 내림차순으로 병렬 조회한다. */
    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            coroutineScope {
                val expiringDeferred = async {
                    repository.getReceipts(status = "expiring", sort = "expiresOn", limit = HOME_EXPIRING_LIMIT)
                        .getOrNull()?.receipts?.map { it.toExpiringWarranty() }.orEmpty()
                }
                // status=all의 totalCount로 "등록된 영수증이 하나라도 있는지"를 판단 — 초기 홈/일반 홈 분기 기준.
                val recentDeferred = async {
                    repository.getReceipts(status = "all", sort = "recent", limit = HOME_RECENT_LIMIT).fold(
                        onSuccess = { data -> RecentQueryResult(data.receipts.map { it.toRecentReceipt() }, data.totalCount) },
                        onFailure = { RecentQueryResult(emptyList(), 0) },
                    )
                }
                val recentResult = recentDeferred.await()
                _state.update {
                    it.copy(
                        expiring = expiringDeferred.await(),
                        recent = recentResult.items,
                        hasAnyReceipts = recentResult.totalCount > 0,
                        isLoading = false,
                    )
                }
            }
        }
    }
}
