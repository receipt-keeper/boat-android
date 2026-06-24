package com.windrr.boat.feature.receipt

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BoatTheme {
                // 무료 분석 잔여 횟수는 로컬 캐시된 사용자 정보에서 가져온다
                val user by ApiClient.userDataStore.user.collectAsState(initial = User())
                ReceiptRegisterScreen(
                    freeAnalysisTokens = user.freeAnalysisTokensRemaining,
                    onBack = { finish() },
                )
            }
        }
    }
}
