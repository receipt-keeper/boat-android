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

    /**
     * 서버 사용량(OCR 분석 가능 여부 + 잔여 횟수)을 조회해 [canAnalyzeState]와
     * 로컬 무료 분석 토큰 캐시를 함께 갱신한다. 크레딧 충전 등으로 잔여 횟수가
     * 바뀐 직후 다시 호출해 화면을 최신 상태로 맞추는 용도로도 재사용된다.
     */
    private fun refreshUsage() {
        lifecycleScope.launch {
            try {
                val ocrUsage = ApiClient.usageApiService.getUsage().data.ocr
                canAnalyzeState.value = ocrUsage.canAnalyze
                ApiClient.userDataStore.updateFreeAnalysisTokens(ocrUsage.remainingCount)
            } catch (e: Exception) {
                canAnalyzeState.value = false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        refreshUsage()

        val autoLaunch = intent.getStringExtra(EXTRA_AUTO_LAUNCH)
        setContent {
            BoatTheme {
                val user by ApiClient.userDataStore.user.collectAsState(initial = User())
                val remoteCanAnalyze by canAnalyzeState.collectAsState()
                ReceiptRegisterScreen(
                    freeAnalysisTokens = user.freeAnalysisTokensRemaining,
                    remoteCanAnalyze = remoteCanAnalyze,
                    onUsageChanged = { refreshUsage() },
                    autoLaunch = autoLaunch,
                    onBack = { finish() },
                )
            }
        }
    }
}
