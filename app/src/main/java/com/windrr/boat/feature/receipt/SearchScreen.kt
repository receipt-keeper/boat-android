package com.windrr.boat.feature.receipt

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.windrr.boat.R
import com.windrr.boat.data.remote.model.ReceiptItem
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorGray100
import com.windrr.boat.ui.theme.ColorGray200
import com.windrr.boat.ui.theme.ColorGray300
import com.windrr.boat.ui.theme.ColorGray400
import com.windrr.boat.ui.theme.ColorGray50
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorGray600
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin12
import com.windrr.boat.ui.theme.Margin16
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.RoundedFull
import com.windrr.boat.ui.theme.RoundedLg
import com.windrr.boat.ui.theme.RoundedXl

/**
 * 검색 화면 — 초기 진입 시 키보드 자동 표시, 쿼리에 따른 결과 표시(TODO).
 */
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = viewModel(),
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorGray50)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
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
                modifier = Modifier
                    .clickable(
                        interactionSource = noRipple,
                        indication = null,
                        onClick = onBack,
                    )
                    .size(30.dp),
            )
            Spacer(Modifier.width(12.dp))
            SearchField(
                query = state.query,
                onQueryChange = {
                    if (it.length <= 100) viewModel.onQueryChanged(it)
                },
                onSearch = { /* 디바운스 검색으로 자동 실행되므로 별도 처리 불필요 */ },
                onClear = { viewModel.onClear() },
                focusRequester = focusRequester,
                modifier = Modifier.weight(1f),
            )
        }

        // 결과 영역
        when {
            state.query.isBlank() -> Unit
            // 디바운스 대기 중 — 직전 쿼리의 결과를 그대로 보여주지 않도록 비워 둔다.
            state.searchedQuery != state.query -> Unit
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = ColorBrandPrimary) }

            state.results.isEmpty() -> SearchEmptyState(
                onRegisterClick = {
                    context.startActivity(Intent(context, ReceiptRegisterActivity::class.java))
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Margin20),
            )

            else -> SearchResultList(
                results = state.results,
                totalCount = state.totalCount,
                onItemClick = { receiptId ->
                    context.startActivity(ReceiptDetailActivity.intent(context, receiptId))
                },
            )
        }
    }
}

@Composable
private fun SearchResultList(
    results: List<ReceiptItem>,
    totalCount: Int,
    onItemClick: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Margin20, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.receipt_filter_all),
                fontSize = 14.sp,
                color = ColorGray600
            )
            Text(text = "  |  ", fontSize = 14.sp, color = ColorGray200)
            Text(
                text = "$totalCount",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ColorBrandPrimary
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = Margin20, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(Margin12),
        ) {
            items(results, key = { it.receiptId }) { item ->
                SearchResultCard(item = item, onClick = { onItemClick(item.receiptId) })
            }
        }
    }
}

@Composable
private fun SearchResultCard(item: ReceiptItem, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedXl,
        color = ColorWhite,
        border = BorderStroke(1.dp, ColorGray100),
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ReceiptItemThumbnail(
                category = item.category,
                subCategory = item.subCategory,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.itemName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorGray900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    WarrantyDayBadge(item.warrantyDDay)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "AS 만료일",
                        fontSize = 13.sp,
                        color = ColorGray400,
                        maxLines = 1,
                        softWrap = false,
                    )
                    Text(text = "  |  ", fontSize = 13.sp, color = ColorGray200)
                    Text(
                        text = item.expiresOn?.formatDate() ?: "-",
                        fontSize = 13.sp,
                        color = ColorGray500,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
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
                    .height(40.dp)
                    .clip(RoundedLg)
                    .background(ColorWhite)
                    .border(1.dp, ColorGray300, RoundedLg)
                    .padding(horizontal = Margin16),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_placeholder),
                            fontSize = 15.sp,
                            color = ColorGray400,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                    innerTextField()
                }
                if (query.isEmpty()) {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(16.dp),
                    )
                } else {
                    val clearRipple = remember { MutableInteractionSource() }
                    Icon(
                        painter = painterResource(R.drawable.icon_close_search),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(20.dp)
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
