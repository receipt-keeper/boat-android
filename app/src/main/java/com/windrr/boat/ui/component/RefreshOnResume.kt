package com.windrr.boat.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect

/**
 * 화면이 다시 보일 때(ON_RESUME)마다 [onResume]를 호출한다.
 *
 * 최초 진입 시의 첫 ON_RESUME은 건너뛴다 — 최초 데이터 로딩은 각 화면의 `LaunchedEffect`가
 * 담당하고, 이 콜백은 "다른 화면/액티비티에서 복귀"에만 반응하도록 하기 위함이다.
 *
 * 영수증 등록/수정/삭제는 별도 Activity에서 이뤄지고 복귀 시 기존 화면의 composition이
 * 그대로 유지되어 `LaunchedEffect(Unit)`이 재실행되지 않으므로, 이 헬퍼로 복귀마다 최신화한다.
 *
 * 주의: [remember](saveable 아님)를 사용해 화면이 composition을 떠났다 다시 들어오면(탭 전환 등)
 * 다시 "최초 진입"으로 취급한다 — 이때는 `LaunchedEffect`가 로딩하므로 중복 호출을 피할 수 있다.
 */
@Composable
fun RefreshOnResume(onResume: () -> Unit) {
    var isFirstResume by remember { mutableStateOf(true) }
    LifecycleResumeEffect(Unit) {
        if (isFirstResume) {
            isFirstResume = false
        } else {
            onResume()
        }
        onPauseOrDispose { }
    }
}
