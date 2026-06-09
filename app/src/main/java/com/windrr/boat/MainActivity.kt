package com.windrr.boat

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.windrr.boat.data.remote.ApiClient
import com.windrr.boat.data.repository.AuthRepositoryImpl
import com.windrr.boat.feature.auth.AuthIntent
import com.windrr.boat.feature.auth.AuthViewModel
import com.windrr.boat.ui.theme.BoatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BoatTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun LoginTestScreen(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val state by authViewModel.state.collectAsState()
    val context = LocalContext.current

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

    val launcher = rememberLauncherForActivityResult(
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
                // Google 로그인 취소 또는 실패 — 별도 처리 불필요
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
                CircularProgressIndicator()
            }

            state.isLoggedIn -> {
                Text(text = "안녕하세요 👋", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = state.displayName ?: "이름 없음", fontSize = 18.sp)
                Text(text = state.email ?: "", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { authViewModel.handleIntent(AuthIntent.SignOut) }) {
                    Text("로그아웃")
                }
            }

            else -> {
                Button(onClick = { launcher.launch(googleSignInClient.signInIntent) }) {
                    Text("Google 로그인")
                }
                state.error?.let { error ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = error, color = Color.Red, fontSize = 13.sp)
                }
            }
        }
    }
}
