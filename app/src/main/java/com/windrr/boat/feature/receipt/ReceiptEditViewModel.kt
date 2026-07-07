package com.windrr.boat.feature.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windrr.boat.data.remote.model.ReceiptItem
import com.windrr.boat.data.remote.model.UpdateReceiptRequest
import com.windrr.boat.data.repository.ReceiptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

data class ReceiptEditState(
    val receipt: ReceiptItem? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSubmitting: Boolean = false,
    val submitted: Boolean = false,
    val submitError: String? = null,
)

class ReceiptEditViewModel : ViewModel() {

    private val repository = ReceiptRepository()

    private val _state = MutableStateFlow(ReceiptEditState())
    val state: StateFlow<ReceiptEditState> = _state.asStateFlow()

    fun load(receiptId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getReceiptDetail(receiptId).fold(
                onSuccess = { item -> _state.update { it.copy(receipt = item, isLoading = false) } },
                onFailure = { e -> _state.update { it.copy(isLoading = false, error = e.message) } },
            )
        }
    }

    /**
     * 새로 추가된 로컬 사진([newPhotoParts])만 업로드해 fileId를 받고,
     * 기존 유지 파일([remainingFileIds])과 합쳐 최종 receiptFileIds로 PATCH한다.
     */
    fun submit(
        receiptId: String,
        request: (receiptFileIds: List<String>) -> UpdateReceiptRequest,
        remainingFileIds: List<String>,
        newPhotoParts: List<MultipartBody.Part>,
    ) {
        if (_state.value.isSubmitting) return
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, submitError = null) }
            repository.uploadFiles(newPhotoParts).fold(
                onSuccess = { newFileIds ->
                    val finalFileIds = remainingFileIds + newFileIds
                    repository.updateReceipt(receiptId, request(finalFileIds)).fold(
                        onSuccess = { item ->
                            _state.update { it.copy(isSubmitting = false, submitted = true, receipt = item) }
                        },
                        onFailure = { e ->
                            _state.update { it.copy(isSubmitting = false, submitError = e.message) }
                        },
                    )
                },
                onFailure = { e ->
                    _state.update { it.copy(isSubmitting = false, submitError = e.message) }
                },
            )
        }
    }

    fun consumeSubmitError() {
        _state.update { it.copy(submitError = null) }
    }
}
