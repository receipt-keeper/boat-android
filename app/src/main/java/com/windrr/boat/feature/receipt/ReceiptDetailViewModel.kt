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

data class ReceiptDetailState(
    val receipt: ReceiptItem? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isDeleting: Boolean = false,
    val deleted: Boolean = false,
    val deleteError: String? = null,
)

class ReceiptDetailViewModel : ViewModel() {

    private val repository = ReceiptRepository()

    private val _state = MutableStateFlow(ReceiptDetailState())
    val state: StateFlow<ReceiptDetailState> = _state.asStateFlow()

    fun load(receiptId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getReceiptDetail(receiptId).fold(
                onSuccess = { item -> _state.update { it.copy(receipt = item, isLoading = false) } },
                onFailure = { e -> _state.update { it.copy(isLoading = false, error = e.message) } },
            )
        }
    }

    fun delete(receiptId: String) {
        if (_state.value.isDeleting) return
        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true, deleteError = null) }
            repository.deleteReceipt(receiptId).fold(
                onSuccess = { _state.update { it.copy(isDeleting = false, deleted = true) } },
                onFailure = { e -> _state.update { it.copy(isDeleting = false, deleteError = e.message) } },
            )
        }
    }

    fun consumeDeleteError() {
        _state.update { it.copy(deleteError = null) }
    }
}
