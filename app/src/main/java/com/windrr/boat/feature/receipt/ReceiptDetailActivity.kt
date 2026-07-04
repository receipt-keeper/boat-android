package com.windrr.boat.feature.receipt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.windrr.boat.ui.theme.BoatTheme

/**
 * 영수증 상세 화면. 목록에서 카드 클릭 시 receiptId를 받아 GET /api/v1/receipts/{id}로 조회한다.
 */
class ReceiptDetailActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_RECEIPT_ID = "extra_receipt_id"

        fun intent(context: Context, receiptId: String): Intent =
            Intent(context, ReceiptDetailActivity::class.java).apply {
                putExtra(EXTRA_RECEIPT_ID, receiptId)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val receiptId = intent.getStringExtra(EXTRA_RECEIPT_ID).orEmpty()

        setContent {
            BoatTheme {
                ReceiptDetailScreen(
                    receiptId = receiptId,
                    onBack = { finish() },
                )
            }
        }
    }
}
