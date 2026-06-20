package com.windrr.boat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.windrr.boat.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.windrr.boat.data.remote.ApiClient
import com.windrr.boat.data.repository.AuthRepositoryImpl
import com.windrr.boat.feature.auth.AuthIntent
import com.windrr.boat.feature.auth.AuthViewModel
import com.windrr.boat.feature.auth.LoginScreen
import com.windrr.boat.feature.terms.TermsScreen
import com.windrr.boat.ui.component.BoatToastHost
import com.windrr.boat.ui.component.rememberBoatToastState
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
                    val state by authViewModel.state.collectAsState()
                    var termsAgreed by remember { mutableStateOf(false) }
                    val toastState = rememberBoatToastState()
                    val msgTermsCancelled = stringResource(R.string.login_error_cancelled)

                    BoatToastHost(state = toastState)

                    when {
                        !state.isLoggedIn -> LoginScreen(
                            viewModel = authViewModel,
                            modifier = Modifier.padding(innerPadding),
                        )
                        !termsAgreed -> TermsScreen(
                            onBack = {
                                toastState.showError(msgTermsCancelled)
                                authViewModel.handleIntent(AuthIntent.SignOut)
                            },
                            onComplete = { termsAgreed = true },
                            modifier = Modifier.padding(innerPadding),
                        )
                        else -> HomeStub(
                            displayName = state.displayName,
                            onSignOut = { authViewModel.handleIntent(AuthIntent.SignOut) },
                            modifier = Modifier.padding(innerPadding),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeStub(
    displayName: String?,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(stringResource(R.string.home_stub_login_success))
        displayName?.let {
            Spacer(Modifier.height(8.dp))
            Text(it)
        }
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = onSignOut) { Text(stringResource(R.string.common_logout)) }
    }
}
