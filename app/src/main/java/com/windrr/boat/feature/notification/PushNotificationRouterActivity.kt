package com.windrr.boat.feature.notification

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.windrr.boat.MainActivity
import com.windrr.boat.core.log.BoatLog
import com.windrr.boat.data.remote.ApiClient
import com.windrr.boat.feature.receipt.ReceiptDetailActivity
import com.windrr.boat.feature.receipt.ReceiptRegisterActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 실제 푸시 알림 탭의 유일한 진입점 — 화면 없이(트램폴린) 다음을 수행하고 즉시 끝난다.
 * 1) notificationId가 있으면 읽음 처리(PATCH) 를 fire-and-forget으로 호출한다.
 * 2) resourceType/resourceId/kind를 [resolveNotificationRoute]로 해석해 목적 화면으로 이동한다.
 *    (목록 카드 탭과 동일한 라우팅 규칙을 공유한다)
 *
 * 항상 MainActivity를 태스크 루트로 먼저 세우고 그 위에 목적 화면을 올린다 —
 * 백그라운드/종료 상태에서 탭했을 때 뒤로가기가 바로 앱 종료로 빠지지 않도록 하기 위함.
 */
class PushNotificationRouterActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        private const val EXTRA_RESOURCE_TYPE = "extra_resource_type"
        private const val EXTRA_RESOURCE_ID = "extra_resource_id"
        private const val EXTRA_KIND = "extra_kind"

        // Activity 종료 이후에도 읽음 처리 요청이 취소되지 않도록 액티비티 생명주기와 분리된 스코프를 쓴다.
        private val routerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun intent(
            context: Context,
            notificationId: String?,
            resourceType: String?,
            resourceId: String?,
            kind: String?,
        ): Intent = Intent(context, PushNotificationRouterActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(EXTRA_RESOURCE_TYPE, resourceType)
            putExtra(EXTRA_RESOURCE_ID, resourceId)
            putExtra(EXTRA_KIND, kind)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val notificationId = intent.getStringExtra(EXTRA_NOTIFICATION_ID)
        val resourceType = intent.getStringExtra(EXTRA_RESOURCE_TYPE)
        val resourceId = intent.getStringExtra(EXTRA_RESOURCE_ID)
        val kind = intent.getStringExtra(EXTRA_KIND)

        if (!notificationId.isNullOrBlank()) {
            routerScope.launch {
                runCatching { ApiClient.notificationApiService.markNotificationRead(notificationId) }
                    .onFailure { BoatLog.e("[PUSH] 알림 읽음 처리 실패 id=$notificationId", it) }
            }
        }

        // 태스크 루트를 MainActivity로 리셋
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )

        when (val route = resolveNotificationRoute(resourceType, resourceId, kind)) {
            is NotificationRoute.ReceiptDetail ->
                startActivity(ReceiptDetailActivity.intent(this, route.receiptId))
            NotificationRoute.ReceiptRegister ->
                startActivity(ReceiptRegisterActivity.intent(this))
            NotificationRoute.None -> Unit
        }

        finish()
    }
}
