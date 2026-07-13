package com.windrr.boat.ui.component

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 영수증 등록 등 특정 액션 성공 시 홈 화면에서 피드백 시트를 띄우기 위한 전역 트리거.
 */
object FeedbackTrigger {
    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val events: SharedFlow<Unit> = _events.asSharedFlow()

    fun trigger() {
        _events.tryEmit(Unit)
    }
}
