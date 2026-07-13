package com.windrr.boat.feature.notification

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.windrr.boat.ui.component.FeedbackTrigger
import com.windrr.boat.ui.theme.BoatTheme

class NotificationListActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BoatTheme {
                NotificationListScreen(onBack = {
                    FeedbackTrigger.trigger()
                    finish()
                })
            }
        }
    }
}
