package com.windrr.boat.core.ocr

import androidx.annotation.DrawableRes
import com.windrr.boat.R

/**
 * 서버가 내려주는 category(대분류) / subCategory(소분류=대표 기기명) 문자열을
 * 표시용 이미지 리소스로 매핑한다.
 *
 * 우선순위:
 *  1) 소분류(대표 기기명) 전용 이미지가 있으면 그 이미지
 *  2) 없거나 미지정이면 대분류(카테고리) 기본 이미지
 *  3) 둘 다 매칭 실패하면 '기타' 기본 이미지
 *
 * 문자열은 공백 편차("주방 가전"/"주방가전")를 흡수하도록 정규화 후 비교한다.
 * 이미지 매칭은 소분류 우선이므로, 대분류 문자열 표기가 달라도 기기 이미지는 정확히 매칭된다.
 */
object DeviceImage {

    // 소분류(대표 기기명) → 전용 이미지. key는 normalize()된 형태.
    private val SUB_CATEGORY_IMAGE: Map<String, Int> = mapOf(
        // 주방 가전
        "냉장고" to R.drawable.img_refridgerator,
        "전자레인지" to R.drawable.img_microwave,
        "밥솥" to R.drawable.img_rice_cooker,
        "정수기" to R.drawable.img_water_purifier,
        "오븐" to R.drawable.img_oven,
        // 세탁/청소
        "세탁기" to R.drawable.img_washing_machine,
        "청소기" to R.drawable.img_vacuum_cleaner,
        "건조기" to R.drawable.img_dry_machine,
        "로봇청소기" to R.drawable.img_robot_vacuum,
        // 리빙/냉난방
        "에어컨" to R.drawable.img_air_conditioner,
        "선풍기" to R.drawable.img_fan,
        "공기청정기" to R.drawable.img_air_purifier,
        "가습기" to R.drawable.img_humidifier,
        // IT 제품
        "데스크탑/tv" to R.drawable.img_monitor,
        "데스크탑" to R.drawable.img_monitor,
        "tv" to R.drawable.img_monitor,
        "티비" to R.drawable.img_monitor,
        "모니터" to R.drawable.img_monitor,
        "스피커" to R.drawable.img_speaker,
        "카메라" to R.drawable.img_camera,
        "게임기" to R.drawable.img_game_console,
        "헤드셋" to R.drawable.img_headset,
        "스마트워치" to R.drawable.img_smartwatch,
        "핸드폰" to R.drawable.img_smartphone,
        "휴대폰" to R.drawable.img_smartphone,
        "스마트폰" to R.drawable.img_smartphone,
        "무선이어폰" to R.drawable.img_bluetooth_earphone,
        "이어폰" to R.drawable.img_bluetooth_earphone,
        "노트북" to R.drawable.img_laptop,
    )

    /** 대분류 기본 이미지 (enum 기준) */
    @DrawableRes
    fun categoryDefault(category: DeviceCategory): Int = when (category) {
        DeviceCategory.KITCHEN -> R.drawable.img_kitchen
        DeviceCategory.LAUNDRY -> R.drawable.img_laundry_room
        DeviceCategory.LIVING  -> R.drawable.img_living_room
        DeviceCategory.IT      -> R.drawable.img_digital_device
        DeviceCategory.OTHER   -> R.drawable.img_misc
    }

    /** 대분류 기본 이미지 (서버 문자열 기준, 표기 편차 흡수) */
    @DrawableRes
    fun categoryDefault(category: String?): Int = when (normalize(category)) {
        "주방가전"                       -> R.drawable.img_kitchen
        "세탁/청소", "세탁청소"           -> R.drawable.img_laundry_room
        "리빙/냉난방", "리빙냉난방"        -> R.drawable.img_living_room
        "it제품", "it기기",
        "영상/it제품", "영상it제품"        -> R.drawable.img_digital_device
        else                             -> R.drawable.img_misc   // 기타 제품/기기 및 미매칭
    }

    /**
     * category + subCategory로 최종 표시 이미지를 결정.
     * 소분류 전용 이미지가 있으면 우선, 없으면 대분류 기본 이미지로 폴백.
     */
    @DrawableRes
    fun resolve(category: String?, subCategory: String?): Int {
        SUB_CATEGORY_IMAGE[normalize(subCategory)]?.let { return it }
        return categoryDefault(category)
    }

    // 공백 제거 + 소문자화 (슬래시는 카테고리 구분자로 유지)
    private fun normalize(raw: String?): String =
        raw?.trim()?.replace(" ", "")?.lowercase() ?: ""
}
