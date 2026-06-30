package com.windrr.boat.feature.receipt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.windrr.boat.data.remote.model.OcrData
import com.windrr.boat.ui.theme.BoatTheme

/**
 * OCR 분석 결과를 그대로 보여주는 임시 확인 화면.
 * 추후 수동 입력/수정 화면으로 결과를 프리필하는 흐름으로 대체 예정.
 */
class OcrResultActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_RESULT = "extra_ocr_result"

        fun intent(context: Context, result: OcrData): Intent =
            Intent(context, OcrResultActivity::class.java).apply {
                putExtra(EXTRA_RESULT, result)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        @Suppress("DEPRECATION")
        val result = (intent.getSerializableExtra(EXTRA_RESULT) as? OcrData) ?: OcrData()

        setContent {
            BoatTheme {
                OcrResultScreen(
                    result = result,
                    onBack = { finish() },
                )
            }
        }
    }
}
