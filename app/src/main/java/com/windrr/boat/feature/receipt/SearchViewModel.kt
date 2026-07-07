package com.windrr.boat.feature.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windrr.boat.data.remote.model.ReceiptItem
import com.windrr.boat.data.repository.ReceiptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val SEARCH_DEBOUNCE_MS = 300L
private const val SEARCH_RESULT_LIMIT = 50

data class SearchState(
    val query: String = "",
    /** 마지막으로 실제 검색을 실행한 쿼리. query와 다르면 디바운스 대기 중(이전 결과를 보여주지 않음). */
    val searchedQuery: String? = null,
    val results: List<ReceiptItem> = emptyList(),
    val totalCount: Int = 0,
    val isLoading: Boolean = false,
)

class SearchViewModel : ViewModel() {

    private val repository = ReceiptRepository()

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            queryFlow
                .debounce(SEARCH_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _state.update { it.copy(results = emptyList(), totalCount = 0, isLoading = false, searchedQuery = null) }
                        return@collectLatest
                    }
                    _state.update { it.copy(isLoading = true, searchedQuery = query) }
                    repository.getReceipts(q = query, limit = SEARCH_RESULT_LIMIT).fold(
                        onSuccess = { data ->
                            _state.update { it.copy(results = data.receipts, totalCount = data.totalCount, isLoading = false) }
                        },
                        onFailure = {
                            _state.update { it.copy(results = emptyList(), totalCount = 0, isLoading = false) }
                        },
                    )
                }
        }
    }

    fun onQueryChanged(query: String) {
        _state.update { it.copy(query = query) }
        queryFlow.value = query
    }

    fun onClear() {
        _state.update { it.copy(query = "", results = emptyList(), totalCount = 0, searchedQuery = null) }
        queryFlow.value = ""
    }
}
