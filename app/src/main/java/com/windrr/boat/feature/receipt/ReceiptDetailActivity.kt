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
        private const val EXTRA_SHOW_CLOSE_ICON = "extra_show_close_icon"

        fun intent(context: Context, receiptId: String): Intent =
            Intent(context, ReceiptDetailActivity::class.java).apply {
                putExtra(EXTRA_RECEIPT_ID, receiptId)
            }

        /** 영수증 등록완료 화면의 "보러가기"처럼 뒤로가기 스택이 아닌 진입 시 — 닫기(X) 아이콘으로 노출. */
        fun intentWithCloseIcon(context: Context, receiptId: String): Intent =
            intent(context, receiptId).apply {
                putExtra(EXTRA_SHOW_CLOSE_ICON, true)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val receiptId = intent.getStringExtra(EXTRA_RECEIPT_ID).orEmpty()
        val showCloseIcon = intent.getBooleanExtra(EXTRA_SHOW_CLOSE_ICON, false)

        setContent {
            BoatTheme {
                ReceiptDetailScreen(
                    receiptId = receiptId,
                    showCloseIcon = showCloseIcon,
                    onBack = { finish() },
                )
            }
        }
    }
}
