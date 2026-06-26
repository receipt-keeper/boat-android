package com.windrr.boat.feature.receipt

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.windrr.boat.ui.theme.BoatTheme

class ReceiptManualInputActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_PHOTOS = "extra_photos"

        /**
         * @param photos 직전 등록 화면에서 업로드한 영수증 사진 URI 목록 (그대로 이어받음)
         */
        fun intent(context: Context, photos: List<Uri> = emptyList()): Intent =
            Intent(context, ReceiptManualInputActivity::class.java).apply {
                putParcelableArrayListExtra(EXTRA_PHOTOS, ArrayList(photos))
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        @Suppress("DEPRECATION")
        val initialPhotos: List<Uri> =
            intent.getParcelableArrayListExtra<Uri>(EXTRA_PHOTOS) ?: emptyList()

        setContent {
            BoatTheme {
                ReceiptManualInputScreen(
                    onBack = { finish() },
                    initialPhotos = initialPhotos,
                )
            }
        }
    }
}
