package com.windrr.boat.feature.receipt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.windrr.boat.data.model.User
import com.windrr.boat.data.remote.ApiClient
import com.windrr.boat.ui.theme.BoatTheme

class ReceiptRegisterActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_AUTO_LAUNCH = "extra_auto_launch"
        const val LAUNCH_CAMERA = "camera"
        const val LAUNCH_GALLERY = "gallery"

        /**
         * @param autoLaunch 진입 즉시 열 소스(LAUNCH_CAMERA/LAUNCH_GALLERY). null이면 자동 실행 안 함.
         */
        fun intent(context: Context, autoLaunch: String? = null): Intent =
            Intent(context, ReceiptRegisterActivity::class.java).apply {
                autoLaunch?.let { putExtra(EXTRA_AUTO_LAUNCH, it) }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val autoLaunch = intent.getStringExtra(EXTRA_AUTO_LAUNCH)
        setContent {
            BoatTheme {
                // 무료 분석 잔여 횟수는 로컬 캐시된 사용자 정보에서 가져온다
                val user by ApiClient.userDataStore.user.collectAsState(initial = User())
                ReceiptRegisterScreen(
                    freeAnalysisTokens = user.freeAnalysisTokensRemaining,
                    autoLaunch = autoLaunch,
                    onBack = { finish() },
                )
            }
        }
    }
}
