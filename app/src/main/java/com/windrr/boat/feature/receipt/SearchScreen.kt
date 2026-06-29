package com.windrr.boat.feature.receipt

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.R
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorGray400
import com.windrr.boat.ui.theme.ColorGray50
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin16
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.RoundedFull
import com.windrr.boat.ui.theme.RoundedXl

/**
 * 검색 화면 — 초기 진입 시 키보드 자동 표시, 쿼리에 따른 결과 표시(TODO).
 */
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorGray50)
            .navigationBarsPadding(),
    ) {
        // 상단 검색 바 (뒤로 + 입력 필드)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = Margin20),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val noRipple = remember { MutableInteractionSource() }
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = stringResource(R.string.common_back),
                tint = Color.Unspecified,
                modifier = Modifier.clickable(
                    interactionSource = noRipple,
                    indication = null,
                    onClick = onBack,
                ),
            )
            Spacer(Modifier.width(12.dp))
            SearchField(
                query = query,
                onQueryChange = { query = it },
                onSearch = { /* TODO: 검색 실행 */ },
                onClear = { query = "" },
                focusRequester = focusRequester,
                modifier = Modifier.weight(1f),
            )
        }

        // 결과 영역
        if (query.isNotEmpty()) {
            SearchEmptyState(
                onRegisterClick = {
                    context.startActivity(Intent(context, ReceiptRegisterActivity::class.java))
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Margin20),
            )
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.focusRequester(focusRequester),
        singleLine = true,
        textStyle = TextStyle(fontSize = 15.sp, color = ColorGray900),
        cursorBrush = SolidColor(ColorGray900),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedFull)
                    .background(ColorWhite)
                    .padding(horizontal = Margin16, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_placeholder),
                            fontSize = 15.sp,
                            color = ColorGray400,
                        )
                    }
                    innerTextField()
                }
                if (query.isNotEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    val clearRipple = remember { MutableInteractionSource() }
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = ColorGray400,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable(
                                interactionSource = clearRipple,
                                indication = null,
                                onClick = onClear,
                            ),
                    )
                }
            }
        },
    )
}

@Composable
private fun SearchEmptyState(
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.search_empty_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ColorGray900,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.search_empty_subtitle),
                fontSize = 14.sp,
                color = ColorGray500,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = onRegisterClick,
                shape = RoundedXl,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorBrandPrimary,
                    contentColor = ColorWhite,
                ),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_plus),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.search_empty_register),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
