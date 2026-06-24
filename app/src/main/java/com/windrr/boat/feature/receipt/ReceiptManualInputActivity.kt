package com.windrr.boat.feature.receipt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.windrr.boat.ui.theme.BoatTheme

class ReceiptManualInputActivity : ComponentActivity() {

    companion object {
        fun intent(context: Context): Intent =
            Intent(context, ReceiptManualInputActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BoatTheme {
                ReceiptManualInputScreen(onBack = { finish() })
            }
        }
    }
}
