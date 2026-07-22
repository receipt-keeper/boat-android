package com.windrr.boat.feature.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.windrr.boat.R

/**
 * 하단 Bottom Navigation 탭 정의.
 * route / 라벨 / 아이콘을 한곳에 모아 BoatBottomBar와 NavHost가 공유한다.
 */
enum class MainTab(
    val route: String,
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
    /** 선택 상태일 때 표시하는 fill 아이콘 — 색상(Secondary)이 에셋에 이미 베이크되어 있다. */
    @DrawableRes val iconResSelected: Int,
) {
    LIST("list", R.string.tab_list, R.drawable.tab_list, R.drawable.folder_minus_fill),
    HOME("home", R.string.tab_home, R.drawable.tab_home, R.drawable.icon_home_fill),
    MY("my", R.string.tab_my, R.drawable.tab_my, R.drawable.icon_profile_fill);

    companion object {
        /** 진입 시 기본 선택 탭 */
        val START = HOME
    }
}
