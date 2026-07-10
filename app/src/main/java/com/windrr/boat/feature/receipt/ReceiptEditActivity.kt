package com.windrr.boat.feature.receipt

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.windrr.boat.ui.theme.BoatTheme

/**
 * 영수증 수정 화면. 상세 화면 케밥 메뉴의 "수정하기"에서 receiptId를 받아 진입한다.
 */
class ReceiptEditActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_RECEIPT_ID = "extra_receipt_id"

        fun intent(context: Context, receiptId: String): Intent =
            Intent(context, ReceiptEditActivity::class.java).apply {
                putExtra(EXTRA_RECEIPT_ID, receiptId)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val receiptId = intent.getStringExtra(EXTRA_RECEIPT_ID).orEmpty()

        setContent {
            BoatTheme {
                ReceiptEditScreen(
                    receiptId = receiptId,
                    onBack = { finish() },
                    onSubmitted = {
                        // 상세 화면이 재조회하도록 성공 결과를 전달하고 종료
                        setResult(Activity.RESULT_OK)
                        finish()
                    },
                )
            }
        }
    }
}
