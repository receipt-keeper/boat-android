package com.windrr.boat.feature.receipt

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.windrr.boat.data.remote.model.OcrData
import com.windrr.boat.ui.theme.BoatTheme

class ReceiptManualInputActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_PHOTOS   = "extra_photos"
        private const val EXTRA_OCR_DATA = "extra_ocr_data"

        fun intent(
            context: Context,
            photos: List<Uri> = emptyList(),
            ocrData: OcrData? = null,
        ): Intent = Intent(context, ReceiptManualInputActivity::class.java).apply {
            putParcelableArrayListExtra(EXTRA_PHOTOS, ArrayList(photos))
            ocrData?.let { putExtra(EXTRA_OCR_DATA, it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        @Suppress("DEPRECATION")
        val initialPhotos: List<Uri> =
            intent.getParcelableArrayListExtra<Uri>(EXTRA_PHOTOS) ?: emptyList()

        @Suppress("DEPRECATION")
        val ocrData = intent.getSerializableExtra(EXTRA_OCR_DATA) as? OcrData

        setContent {
            BoatTheme {
                ReceiptManualInputScreen(
                    onBack = { finish() },
                    initialPhotos = initialPhotos,
                    ocrData = ocrData,
                )
            }
        }
    }
}
