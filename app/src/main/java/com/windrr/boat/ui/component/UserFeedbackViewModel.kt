package com.windrr.boat.ui.component

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.windrr.boat.data.local.UserDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class UserFeedbackViewModel(
    private val userDataStore: UserDataStore,
    private val firebaseAnalytics: FirebaseAnalytics
) : ViewModel() {

    private val _showFeedbackSheet = MutableStateFlow(false)
    val showFeedbackSheet: StateFlow<Boolean> = _showFeedbackSheet.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private var hasTriggeredInSession = false

    init {
        viewModelScope.launch {
            FeedbackTrigger.events.collectLatest {
                tryShowFeedback()
            }
        }
    }

    /**
     * 피드백 시트 노출 시도.
     * 30일 재노출 제한을 확인한 후 조건을 만족하면 시트를 띄운다.
     */
    fun tryShowFeedback() {
        if (hasTriggeredInSession) return

        viewModelScope.launch {
            val nextDisplayAt = userDataStore.nextFeedbackDisplayAt.first()
            val now = System.currentTimeMillis()

            if (now >= nextDisplayAt) {
                _showFeedbackSheet.value = true
                hasTriggeredInSession = true
            }
        }
    }

    /**
     * 피드백 제출 처리.
     * Firebase Analytics에 이벤트를 전송하고 30일 노출 제한을 설정한다.
     */
    fun submitFeedback(rating: Int, comment: String, onResult: (Boolean) -> Unit) {
        if (_isSubmitting.value) return

        viewModelScope.launch {
            _isSubmitting.value = true
            
            runCatching {
                firebaseAnalytics.logEvent("service_feedback") {
                    param("rating", rating.toLong())
                    param("comment", comment)
                }
            }.onSuccess {
                deferFeedback(days = 30)
                _showFeedbackSheet.value = false
                _isSubmitting.value = false
                onResult(true)
            }.onFailure {
                _isSubmitting.value = false
                onResult(false)
            }
        }
    }

    /**
     * 피드백 시트 닫기 처리 (X 버튼 또는 외부 터치).
     * 30일 노출 제한을 설정하고 시트를 닫는다.
     */
    fun onFeedbackDismissed() {
        deferFeedback(days = 30)
        _showFeedbackSheet.value = false
    }

    /**
     * 피드백 시트 "다음에" 버튼 처리.
     * 15일 노출 제한을 설정하고 시트를 닫는다.
     */
    fun onFeedbackPostponed() {
        deferFeedback(days = 15)
        _showFeedbackSheet.value = false
    }

    private fun deferFeedback(days: Int) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, days)
            userDataStore.updateNextFeedbackDisplayAt(calendar.timeInMillis)
        }
    }
}
