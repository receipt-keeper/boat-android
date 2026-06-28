package com.windrr.boat.feature.receipt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.windrr.boat.data.model.User
import com.windrr.boat.data.remote.ApiClient
import com.windrr.boat.ui.theme.BoatTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

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

    // null=로딩 중, true/false=서버 응답 완료
    private val canAnalyzeState = MutableStateFlow<Boolean?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            try {
                val response = ApiClient.usageApiService.getUsage()
                canAnalyzeState.value = response.data.ocr.canAnalyze
            } catch (e: Exception) {
                canAnalyzeState.value = false
            }
        }

        val autoLaunch = intent.getStringExtra(EXTRA_AUTO_LAUNCH)
        setContent {
            BoatTheme {
                val user by ApiClient.userDataStore.user.collectAsState(initial = User())
                val remoteCanAnalyze by canAnalyzeState.collectAsState()
                ReceiptRegisterScreen(
                    freeAnalysisTokens = user.freeAnalysisTokensRemaining,
                    remoteCanAnalyze = remoteCanAnalyze,
                    autoLaunch = autoLaunch,
                    onBack = { finish() },
                )
            }
        }
    }
}
