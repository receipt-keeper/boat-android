package com.windrr.boat

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.windrr.boat.core.crash.CrashReporter
import com.windrr.boat.core.log.BoatLog
import com.windrr.boat.data.remote.ApiClient
import com.windrr.boat.data.repository.AuthRepositoryImpl
import com.windrr.boat.feature.auth.AuthIntent
import com.windrr.boat.feature.auth.AuthViewModel
import com.windrr.boat.feature.gallery.GalleryScreen
import com.windrr.boat.feature.notification.AlarmPermissionTestScreen
import com.windrr.boat.feature.ocr.OcrTestScreen
import com.windrr.boat.ui.component.BoatToastHost
import com.windrr.boat.ui.component.BoatToastType
import com.windrr.boat.ui.component.rememberBoatToastState
import com.windrr.boat.ui.theme.BoatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BoatTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var showGallery by rememberSaveable { mutableStateOf(false) }
                    var showAlarmTest by rememberSaveable { mutableStateOf(false) }
                    var showOcrTest by rememberSaveable { mutableStateOf(false) }

                    when {
                        showGallery -> {
                            GalleryScreen(onBack = { showGallery = false })
                        }
                        showAlarmTest -> {
                            AlarmPermissionTestScreen(onBack = { showAlarmTest = false })
                        }
                        showOcrTest -> {
                            OcrTestScreen(onBack = { showOcrTest = false })
                        }
                        else -> {
                            val authViewModel: AuthViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        @Suppress("UNCHECKED_CAST")
                                        return AuthViewModel(
                                            AuthRepositoryImpl(ApiClient.tokenDataStore)
                                        ) as T
                                    }
                                }
                            )
                            LoginTestScreen(
                                authViewModel = authViewModel,
                                onOpenGallery = { showGallery = true },
                                onOpenAlarmTest = { showAlarmTest = true },
                                onOpenOcrTest = { showOcrTest = true },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginTestScreen(
    authViewModel: AuthViewModel,
    onOpenGallery: () -> Unit,
    onOpenAlarmTest: () -> Unit,
    onOpenOcrTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val toastState = rememberBoatToastState()
    BoatToastHost(state = toastState)

    val state by authViewModel.state.collectAsState()
    val context = LocalContext.current
    val activity = context as Activity
    val webClientId = stringResource(R.string.default_web_client_id)
    val googleSignInClient = remember {
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()
        )
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val account = GoogleSignIn
                    .getSignedInAccountFromIntent(result.data)
                    .getResult(ApiException::class.java)
                account.idToken?.let { idToken ->
                    authViewModel.handleIntent(AuthIntent.SignInWithGoogle(idToken))
                }
            } catch (e: ApiException) {
                if (e.statusCode != 12501) { //사용자가 직접 취소한 경우 제외
                    BoatLog.e("Google 계정 선택 실패 (code=${e.statusCode})", e)
                    Toast.makeText(
                        context,
                        "Google 로그인에 실패했습니다 (${e.statusCode})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun handleAppleSignIn() {
        val provider = OAuthProvider.newBuilder("apple.com")
            .setScopes(listOf("email", "name"))
            .build()

        val pending = FirebaseAuth.getInstance().pendingAuthResult
        val task = pending ?: FirebaseAuth.getInstance()
            .startActivityForSignInWithProvider(activity, provider)

        task
            .addOnSuccessListener { result ->
                val idToken = result.credential?.let {
                    (it as? com.google.firebase.auth.OAuthCredential)?.idToken
                } ?: return@addOnSuccessListener
                val displayName = result.additionalUserInfo
                    ?.profile?.get("name") as? String
                authViewModel.handleIntent(
                    AuthIntent.SignInWithApple(
                        idToken = idToken,
                        displayName = displayName
                    )
                )
            }
            .addOnFailureListener { e ->
                val message = when {
                    e.message?.contains("canceled", ignoreCase = true) == true -> null
                    else -> "Apple 로그인에 실패했습니다"
                }
                message?.let {
                    BoatLog.e("Apple OAuth 인증 실패", e)
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(color = Color.Black)
            }

            state.isLoggedIn -> {
                LoggedInSection(
                    displayName = state.displayName,
                    email = state.email,
                    onSignOut = { authViewModel.handleIntent(AuthIntent.SignOut) }
                )
            }

            else -> {
                LoginSection(
                    onGoogleSignIn = { googleLauncher.launch(googleSignInClient.signInIntent) },
                    onAppleSignIn = { handleAppleSignIn() },
                    error = state.error
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 갤러리 테스트 버튼 — 로그인 여부와 무관하게 항상 표시
        OutlinedButton(
            onClick = onOpenGallery,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("갤러리 열기 (테스트)")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 알람 권한 테스트 진입 버튼
        OutlinedButton(
            onClick = onOpenAlarmTest,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("알람 권한 테스트")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // OCR 테스트 진입 버튼
        OutlinedButton(
            onClick = onOpenOcrTest,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("OCR 테스트 (Vision API)")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Crashlytics 검증용 강제 크래시 버튼 (테스트 전용)
        OutlinedButton(
            onClick = { CrashReporter.forceTestCrash() },
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFFE57373))
        ) {
            Text("테스트 크래시 발생 (Crashlytics)", color = Color(0xFFC62828))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("── Toast 테스트 ──", fontSize = 12.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { toastState.show("무료 횟수 3회가 충전되었습니다.", BoatToastType.INFO) },
            shape = RoundedCornerShape(8.dp)
        ) { Text("INFO 토스트") }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { toastState.showSuccess("저장되었습니다.") },
            shape = RoundedCornerShape(8.dp)
        ) { Text("SUCCESS 토스트") }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { toastState.showError("오류가 발생했습니다.") },
            shape = RoundedCornerShape(8.dp)
        ) { Text("ERROR 토스트") }
    }
}

/** 로그인 성공 후 유저 정보 + 로그아웃 버튼 */
@Composable
private fun LoggedInSection(
    displayName: String?,
    email: String?,
    onSignOut: () -> Unit
) {
    Text(text = "안녕하세요 👋", fontSize = 22.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))
    Text(text = displayName ?: "이름 없음", fontSize = 18.sp)
    Text(text = email ?: "", fontSize = 14.sp, color = Color.Gray)
    Spacer(modifier = Modifier.height(32.dp))
    OutlinedButton(
        onClick = onSignOut,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text("로그아웃")
    }
}

/** Google / Apple 로그인 버튼 영역 */
@Composable
private fun LoginSection(
    onGoogleSignIn: () -> Unit,
    onAppleSignIn: () -> Unit,
    error: String?
) {
    Text(
        text = "BOAT",
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "영수증을 스마트하게 관리하세요",
        fontSize = 14.sp,
        color = Color.Gray
    )
    Spacer(modifier = Modifier.height(48.dp))

    // Google 로그인 버튼 — 흰 배경 + 테두리
    OutlinedButton(
        onClick = onGoogleSignIn,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .height(52.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFDDDDDD)),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "G",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4285F4)   // Google Blue
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Google로 계속하기",
                fontSize = 15.sp,
                color = Color(0xFF3C3C3C)
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Apple 로그인 버튼 — 검정 배경 + 흰 텍스트
    Button(
        onClick = onAppleSignIn,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .height(52.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "",
                fontSize = 20.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Apple로 계속하기",
                fontSize = 15.sp,
                color = Color.White
            )
        }
    }

    error?.let {
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = it, color = Color.Red, fontSize = 13.sp)
    }
}
