package com.windrr.boat.feature.gallery

import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * [MVI - ViewModel]
 * 선택된 사진 목록(최대 [GalleryState.MAX_PHOTOS]장)을 관리하는 ViewModel
 *
 * 사진은 Photo Picker 또는 카메라로부터 받은 URI만 보관하므로
 * Context나 ContentResolver가 필요 없음 → 일반 ViewModel 사용.
 *
 * @Stable — Compose 컴파일러에 안정적 타입임을 명시(리컴포지션 최적화).
 */
@Stable
class GalleryViewModel : ViewModel() {

    private val _state = MutableStateFlow(GalleryState())
    val state: StateFlow<GalleryState> = _state.asStateFlow()

    /**
     * View에서 발생한 Intent를 처리하는 단일 진입점
     *
     * @param intent 처리할 [GalleryIntent]
     */
    fun handleIntent(intent: GalleryIntent) {
        when (intent) {
            is GalleryIntent.AddPhotos -> addPhotos(intent.uris)
            is GalleryIntent.RemovePhoto -> removePhoto(intent.uri)
            is GalleryIntent.ClearError -> _state.update { it.copy(error = null) }
        }
    }

    /**
     * 사진 추가 — 최대 장수를 초과하면 남은 만큼만 담고 에러 메시지 표시.
     * 이미 선택된 동일 URI는 중복으로 추가하지 않음.
     */
    private fun addPhotos(uris: List<Uri>) {
        _state.update { current ->
            val newUris = uris.filterNot { it in current.photos }
            val accepted = newUris.take(current.remainingSlots)
            val isOverflow = newUris.size > current.remainingSlots

            current.copy(
                photos = current.photos + accepted,
                error = if (isOverflow) {
                    "사진은 최대 ${GalleryState.MAX_PHOTOS}장까지 선택할 수 있습니다"
                } else {
                    null
                }
            )
        }
    }

    /** 선택된 사진 1장 제거 */
    private fun removePhoto(uri: Uri) {
        _state.update { it.copy(photos = it.photos - uri) }
    }
}
