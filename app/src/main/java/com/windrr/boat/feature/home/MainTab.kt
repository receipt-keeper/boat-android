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
) {
    LIST("list", R.string.tab_list, R.drawable.tab_list),
    HOME("home", R.string.tab_home, R.drawable.tab_home),
    MY("my", R.string.tab_my, R.drawable.tab_my);

    companion object {
        /** 진입 시 기본 선택 탭 */
        val START = HOME
    }
}
