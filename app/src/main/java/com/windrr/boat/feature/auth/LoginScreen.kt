package com.windrr.boat.feature.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.BuildConfig
import com.windrr.boat.data.remote.ApiClient
import com.windrr.boat.data.remote.ApiErrorParser
import kotlinx.coroutines.launch
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthCredential
import com.google.firebase.auth.OAuthProvider
import com.windrr.boat.R
import com.windrr.boat.core.log.BoatLog
import com.windrr.boat.ui.component.BoatToastHost
import com.windrr.boat.ui.component.rememberBoatToastState
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin16
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.RoundedXl

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val activity = context as Activity
    val webClientId = stringResource(R.string.default_web_client_id)
    val toastState = rememberBoatToastState()
    val scope = rememberCoroutineScope()

    BoatToastHost(state = toastState)

    val msgCancelled  = stringResource(R.string.login_error_cancelled)
    val msgGoogleFail = stringResource(R.string.login_error_google)
    val msgAppleFail  = stringResource(R.string.login_error_apple)

    val googleSignInClient = remember {
        BoatLog.i("[GOOGLE-0] GoogleSignInClient 생성 — webClientId=${webClientId.takeLast(12)}")
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .requestProfile()   // 이름(displayName) 명시적 요청
                .build()
        )
    }

    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        BoatLog.i("[GOOGLE-2] 계정 선택창 결과 resultCode=${result.resultCode} (RESULT_OK=${Activity.RESULT_OK})")
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val account = GoogleSignIn
                    .getSignedInAccountFromIntent(result.data)
                    .getResult(ApiException::class.java)
                BoatLog.i("[GOOGLE-3] 계정 획득 성공 — email=${account.email}, idToken=${if (account.idToken != null) "있음(len=${account.idToken!!.length})" else "null!!"}")
                account.idToken?.let {
                    viewModel.handleIntent(
                        AuthIntent.SignInWithGoogle(
                            idToken = it,
                            email = account.email,
                            displayName = account.displayName,
                        )
                    )
                } ?: BoatLog.e("[GOOGLE-3-FAIL] idToken이 null — requestIdToken(webClientId) 설정/SHA-1 등록 확인 필요")
            } catch (e: ApiException) {
                when (e.statusCode) {
                    12501 -> {
                        BoatLog.i("[GOOGLE-2-CANCEL] 사용자 취소 (statusCode=12501)")
                        toastState.showError(msgCancelled)
                    }
                    else  -> {
                        // 10=DEVELOPER_ERROR(SHA-1/OAuth 클라이언트 설정 불일치), 7=NETWORK_ERROR 등
                        BoatLog.e("[GOOGLE-2-FAIL] statusCode=${e.statusCode} message=${e.message}", e)
                        toastState.showError(msgGoogleFail)
                    }
                }
            }
        } else {
            BoatLog.i("[GOOGLE-2-CANCEL] 계정 선택창이 OK가 아닌 resultCode로 종료")
            toastState.showError(msgCancelled)
        }
    }

    fun handleAppleSignIn() {
        BoatLog.i("[APPLE-1] Apple 로그인 버튼 클릭")
        val provider = OAuthProvider.newBuilder("apple.com")
            .setScopes(listOf("email", "name"))
            .build()
        val pending = FirebaseAuth.getInstance().pendingAuthResult
        BoatLog.i("[APPLE-2] pendingAuthResult=${if (pending != null) "있음(재사용)" else "없음(신규 웹뷰 시작)"}")
        val task = pending ?: FirebaseAuth.getInstance()
            .startActivityForSignInWithProvider(activity, provider)
        task
            .addOnSuccessListener { result ->
                val idToken = (result.credential as? OAuthCredential)?.idToken
                if (idToken == null) {
                    BoatLog.e("[APPLE-3-FAIL] credential에서 idToken을 못 얻음 — credential=${result.credential}")
                    return@addOnSuccessListener
                }
                BoatLog.i("[APPLE-3] idToken 획득 성공 (len=${idToken.length})")
                // Apple은 최초 로그인 때만 name 제공 (Map 구조), 이후엔 Firebase user에서 가져옴
                val nameMap = result.additionalUserInfo?.profile?.get("name") as? Map<*, *>
                val displayName = if (nameMap != null) {
                    val first = nameMap["firstName"] as? String ?: ""
                    val last  = nameMap["lastName"]  as? String ?: ""
                    "$first $last".trim().ifBlank { null }
                } else {
                    result.user?.displayName
                }
                val email = result.additionalUserInfo?.profile?.get("email") as? String
                    ?: result.user?.email
                BoatLog.i("[APPLE-4] displayName=$displayName, email=$email")
                viewModel.handleIntent(AuthIntent.SignInWithApple(idToken, displayName, email))
            }
            .addOnFailureListener { e ->
                if (e.message?.contains("canceled", ignoreCase = true) == true) {
                    BoatLog.i("[APPLE-2-CANCEL] 사용자 취소: ${e.message}")
                    toastState.showError(msgCancelled)
                } else {
                    // 자주 나오는 원인: Apple Developer 콘솔의 Return URL(Firebase 콜백)/Service ID 설정 불일치,
                    // 또는 Firebase 콘솔 Apple 제공업체 Services ID·Team ID·Key 설정 만료/변경
                    BoatLog.e("[APPLE-2-FAIL] ${e.javaClass.simpleName}: ${e.message}", e)
                    toastState.showError(msgAppleFail)
                }
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorWhite)
            .padding(horizontal = Margin20),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))

        // 로고 — "Boat"(검정) + "Lab"(파란색) 텍스트
        Row(horizontalArrangement = Arrangement.Center) {
            Text(
                text = "Boat ",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = ColorGray900,
            )
            Text(
                text = "Lab",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = ColorBrandPrimary,
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.login_subtitle),
            fontSize = 15.sp,
            color = ColorGray500,
        )

        Spacer(Modifier.weight(1.5f))

        // Google 로그인 버튼
        OutlinedButton(
            onClick = {
                BoatLog.i("[GOOGLE-1] Google 로그인 버튼 클릭 — 계정 선택창 실행")
                googleLauncher.launch(googleSignInClient.signInIntent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedXl,
            border = BorderStroke(1.dp, ColorBrandPrimary),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = ColorWhite),
            contentPadding = PaddingValues(horizontal = Margin16),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(R.drawable.ic_google),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.CenterStart),
                )
                Text(
                    text = stringResource(R.string.login_btn_google),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF3C3C3C),
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Apple 로그인 버튼
        Button(
            onClick = { handleAppleSignIn() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedXl,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            contentPadding = PaddingValues(horizontal = Margin16),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(R.drawable.ic_apple),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.CenterStart),
                )
                Text(
                    text = stringResource(R.string.login_btn_apple),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorWhite,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }

        // DEBUG 전용 — 서버 URL 전환 토글
        if (BuildConfig.DEBUG) {
            val prefs = remember {
                context.getSharedPreferences(ApiClient.DEBUG_PREFS, android.content.Context.MODE_PRIVATE)
            }
            var useLocalUrl by remember {
                mutableStateOf(prefs.getBoolean(ApiClient.KEY_USE_LOCAL_URL, false))
            }
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "로컬 서버 (DEBUG)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorGray500,
                    )
                    Text(
                        text = if (useLocalUrl) ApiClient.BASE_URL_LOCAL else ApiClient.BASE_URL_PROD,
                        fontSize = 11.sp,
                        color = if (useLocalUrl) ColorBrandPrimary else ColorGray500,
                    )
                }
                Switch(
                    checked = useLocalUrl,
                    onCheckedChange = { checked ->
                        useLocalUrl = checked
                        prefs.edit().putBoolean(ApiClient.KEY_USE_LOCAL_URL, checked).apply()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ColorWhite,
                        checkedTrackColor = ColorBrandPrimary,
                        checkedBorderColor = ColorBrandPrimary,
                        uncheckedThumbColor = ColorWhite,
                        uncheckedTrackColor = Color(0xFFBDBDBD),
                        uncheckedBorderColor = Color(0xFFBDBDBD),
                    ),
                )
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    scope.launch {
                        runCatching { ApiClient.exampleApiService.serverError() }
                            .onFailure { toastState.showError(ApiErrorParser.message(it)) }
                            .onSuccess { toastState.show("서버 응답 성공") }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedXl,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFCC0000),
                    contentColor = ColorWhite,
                ),
            ) {
                Text(text = "[TEST] 서버 에러 테스트", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(Modifier.height(64.dp))
    }
}
