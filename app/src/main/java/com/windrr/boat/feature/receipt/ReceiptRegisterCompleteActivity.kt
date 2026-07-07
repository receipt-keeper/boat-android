package com.windrr.boat.feature.receipt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.windrr.boat.feature.home.HomeActivity
import com.windrr.boat.ui.theme.BoatTheme

/**
 * 영수증 등록 완료 화면. 등록 성공 직후 진입하며,
 * "홈으로 가기" / "보러가기" 모두 등록 플로우(촬영~입력) 스택을 정리하고 홈을 기준으로 이동한다.
 */
class ReceiptRegisterCompleteActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_RECEIPT_ID = "extra_receipt_id"

        fun intent(context: Context, receiptId: String): Intent =
            Intent(context, ReceiptRegisterCompleteActivity::class.java).apply {
                putExtra(EXTRA_RECEIPT_ID, receiptId)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val receiptId = intent.getStringExtra(EXTRA_RECEIPT_ID).orEmpty()

        fun goHome() {
            startActivity(
                Intent(this, HomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
            )
        }

        setContent {
            BoatTheme {
                ReceiptRegisterCompleteScreen(
                    onGoHome = { goHome() },
                    onViewReceipt = {
                        goHome()
                        startActivity(ReceiptDetailActivity.intent(this, receiptId))
                    },
                )
            }
        }
    }
}
