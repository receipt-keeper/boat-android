package com.windrr.boat.ui.component

import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.AdChoicesView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.windrr.boat.R
import com.windrr.boat.core.log.BoatLog
import com.windrr.boat.feature.home.AccessoryBanner

/**
 * 기존 AccessoryBanner 디자인을 계승하는 구글 네이티브 광고 컴포넌트.
 *
 * @param adUnitId 광고 단위 ID (네이티브 광고용 테스트 ID)
 */
@Composable
fun BoatNativeAdBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-2156013754929909/7289005642" // 네이티브 고급 광고 ID
) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var adLoadFailed by remember { mutableStateOf(false) }

    // 광고 로드
    DisposableEffect(adUnitId) {
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad : NativeAd ->
                nativeAd = ad
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("ADMOB_DEBUG", "Native Ad 로드 실패: code=${error.code}, message=${error.message}")
                    adLoadFailed = true
                }
                override fun onAdLoaded() {
                    Log.d("ADMOB_DEBUG", "Native Ad 로드 성공!")
                }
            })
            .build()

        Log.d("ADMOB_DEBUG", "광고 로드 시작: unitId=$adUnitId")
        adLoader.loadAd(AdRequest.Builder().build())

        onDispose {
            nativeAd?.destroy()
        }
    }

    if (nativeAd != null) {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .height(110.dp),
            factory = { ctx ->
                val adView = LayoutInflater.from(ctx)
                    .inflate(R.layout.layout_native_ad_banner, null) as NativeAdView
                
                populateNativeAdView(nativeAd!!, adView)
                adView
            },
            update = { adView ->
                populateNativeAdView(nativeAd!!, adView)
            }
        )
    } else if (adLoadFailed) {
        // 💡 광고 로드 실패 시 기존 임시 배너(AccessoryBanner)를 폴백으로 노출
        AccessoryBanner(
            onClick = { /* 필요 시 쇼핑 페이지 연결 */ },
            modifier = modifier
        )
    } else {
        // 로딩 중: 공간만 차지하도록 빈 박스 처리 (또는 스켈레톤)
        Box(modifier = modifier.fillMaxWidth().height(110.dp))
    }
}

private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
    // 텍스트 매핑
    val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
    headlineView.text = nativeAd.headline
    adView.headlineView = headlineView

    val bodyView = adView.findViewById<TextView>(R.id.ad_body)
    bodyView.text = nativeAd.body
    adView.bodyView = bodyView

    // 메인 이미지/동영상 매핑 — 아이콘 전용 ImageView가 아니라 MediaView를 써야 한다
    // (AdMob Native Ad Validator: "MediaView not used for main image or video asset").
    val mediaView = adView.findViewById<MediaView>(R.id.ad_media)
    mediaView.mediaContent = nativeAd.mediaContent
    adView.mediaView = mediaView

    // AdChoices(광고 선택 옵션) 아이콘 — setNativeAd 호출 전에 반드시 지정해야 한다.
    adView.adChoicesView = adView.findViewById<AdChoicesView>(R.id.ad_choices_view)

    // 광고 객체 등록
    adView.setNativeAd(nativeAd)
}
