package com.windrr.boat.feature.terms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.R
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorBrandSenary
import com.windrr.boat.ui.theme.ColorGray100
import com.windrr.boat.ui.theme.ColorGray200
import com.windrr.boat.ui.theme.ColorGray400
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorGray700
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin12
import com.windrr.boat.ui.theme.Margin16
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.Margin32
import com.windrr.boat.ui.theme.Margin56
import com.windrr.boat.ui.theme.Margin8
import com.windrr.boat.ui.theme.RoundedLg
import com.windrr.boat.ui.theme.RoundedXl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(
    onBack: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var ageConsent    by remember { mutableStateOf(false) }
    var serviceTerms  by remember { mutableStateOf(false) }
    var privacyPolicy by remember { mutableStateOf(false) }
    var marketing     by remember { mutableStateOf(false) }

    val allAgreed   = ageConsent && serviceTerms && privacyPolicy && marketing
    val allRequired = ageConsent && serviceTerms && privacyPolicy

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.terms_title_bar),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = ColorGray900,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = ColorGray900,
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ColorWhite,
                ),
            )
        },
        containerColor = ColorWhite,
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Margin20),
        ) {
            Spacer(Modifier.height(Margin20))

            Text(
                text = stringResource(R.string.terms_headline),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp,
                color = ColorGray900,
            )

            Spacer(Modifier.height(Margin32))

            // 전체 동의 행
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedLg)
                    .background(if (allAgreed) ColorBrandSenary else ColorGray100)
                    .clickable {
                        val next = !allAgreed
                        ageConsent    = next
                        serviceTerms  = next
                        privacyPolicy = next
                        marketing     = next
                    }
                    .padding(horizontal = Margin16, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = if (allAgreed) ColorBrandPrimary else ColorGray400,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(Margin12))
                Text(
                    text = stringResource(R.string.terms_agree_all),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = ColorGray900,
                )
            }

            Spacer(Modifier.height(Margin8))

            TermsItem(
                text = stringResource(R.string.terms_age),
                checked = ageConsent,
                onClick = { ageConsent = !ageConsent },
            )
            TermsItem(
                text = stringResource(R.string.terms_service),
                checked = serviceTerms,
                onClick = { serviceTerms = !serviceTerms },
                showViewLink = true,
            )
            TermsItem(
                text = stringResource(R.string.terms_privacy),
                checked = privacyPolicy,
                onClick = { privacyPolicy = !privacyPolicy },
                showViewLink = true,
            )
            TermsItem(
                text = stringResource(R.string.terms_marketing),
                checked = marketing,
                onClick = { marketing = !marketing },
                showViewLink = true,
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onComplete,
                enabled = allRequired,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Margin56),
                shape = RoundedXl,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorBrandPrimary,
                    contentColor = ColorWhite,
                    disabledContainerColor = ColorGray200,
                    disabledContentColor = ColorGray500,
                ),
            ) {
                Text(
                    text = stringResource(R.string.terms_complete),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
            }

            Spacer(Modifier.height(Margin32))
        }
    }
}

@Composable
private fun TermsItem(
    text: String,
    checked: Boolean,
    onClick: () -> Unit,
    showViewLink: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = if (checked) ColorBrandPrimary else ColorGray400,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(Margin12))
        Text(
            text = text,
            fontSize = 14.sp,
            color = ColorGray700,
            modifier = Modifier.weight(1f),
        )
        if (showViewLink) {
            Text(
                text = stringResource(R.string.terms_view),
                fontSize = 13.sp,
                color = ColorGray500,
                modifier = Modifier.padding(start = Margin8),
            )
        }
    }
}
