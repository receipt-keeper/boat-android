package com.windrr.boat.feature.terms

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.windrr.boat.ui.theme.BoatTheme

/**
 * 서비스 이용약관 상세 화면 Activity.
 */
class TermsDetailActivity : ComponentActivity() {

    companion object {
        fun intent(context: Context): Intent =
            Intent(context, TermsDetailActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BoatTheme {
                TermsDetailScreen(
                    onBack = { finish() }
                )
            }
        }
    }
}
