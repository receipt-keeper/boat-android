package com.windrr.boat.feature.gallery

import android.net.Uri

/**
 * [MVI - Model]
 * 사진 선택 화면의 UI 상태
 *
 * @property photos 현재 선택된 사진 URI 목록 (최대 [MAX_PHOTOS]장)
 * @property error 에러 메시지 (null이면 에러 없음)
 */
data class GalleryState(
    val photos: List<Uri> = emptyList(),
    val error: String? = null
) {
    /** 사진을 더 추가할 수 있는지 여부 */
    val canAddMore: Boolean
        get() = photos.size < MAX_PHOTOS

    /** 추가로 선택 가능한 남은 장수 */
    val remainingSlots: Int
        get() = (MAX_PHOTOS - photos.size).coerceAtLeast(0)

    companion object {
        /** 선택 가능한 최대 사진 장수 */
        const val MAX_PHOTOS = 5
    }
}
