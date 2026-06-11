package com.windrr.boat.feature.gallery

import android.net.Uri

/**
 * [MVI - Intent]
 * 사진 선택 화면에서 발생할 수 있는 이벤트 목록
 */
sealed class GalleryIntent {

    /**
     * 사진 추가 (갤러리 다중선택 또는 카메라 촬영 결과)
     *
     * @param uris 추가할 사진 URI 목록 (카메라는 1장)
     */
    data class AddPhotos(val uris: List<Uri>) : GalleryIntent()

    /**
     * 선택된 사진 1장 삭제
     *
     * @param uri 삭제할 사진 URI
     */
    data class RemovePhoto(val uri: Uri) : GalleryIntent()

    /** 에러 메시지 소비 후 초기화 */
    data object ClearError : GalleryIntent()
}
