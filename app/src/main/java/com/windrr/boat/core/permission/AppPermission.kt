package com.windrr.boat.core.permission

import android.Manifest

/**
 * 앱에서 사용하는 모든 권한을 중앙에서 정의
 *
 * 새 권한 추가 시 이 파일에만 추가하면 됨.
 * 실제 권한 요청은 Composable에서 Accompanist의
 * rememberMultiplePermissionsState로 처리.
 *
 * 참고: 갤러리 선택은 Photo Picker를 사용하므로 미디어 읽기 권한이 불필요.
 *
 * @property permissions 요청할 실제 권한 목록
 * @property rationale 권한이 필요한 이유 (거부 후 재요청 시 사용자에게 표시)
 */
sealed class AppPermission(
    val permissions: List<String>,
    val rationale: String
) {
    /** 카메라 — 영수증 촬영 */
    data object Camera : AppPermission(
        permissions = listOf(Manifest.permission.CAMERA),
        rationale = "영수증을 촬영하려면 카메라 권한이 필요합니다"
    )
}
