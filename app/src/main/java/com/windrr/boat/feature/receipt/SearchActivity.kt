package com.windrr.boat.feature.receipt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.windrr.boat.ui.theme.BoatTheme

/**
 * 메인 탭 NavHost 밖(별도 Activity)에서 검색 화면에 진입할 때 사용하는 래퍼.
 * 메인 탭 안에서는 MainScreen의 "search" 컴포저블 라우트를 그대로 쓴다.
 */
class SearchActivity : ComponentActivity() {

    companion object {
        fun intent(context: Context): Intent = Intent(context, SearchActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BoatTheme {
                SearchScreen(onBack = { finish() })
            }
        }
    }
}
