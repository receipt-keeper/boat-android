package com.windrr.boat.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.R
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorGray200
import com.windrr.boat.ui.theme.ColorGray400
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorGray600
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Rounded2xl
import com.windrr.boat.ui.theme.RoundedLg
import com.windrr.boat.ui.theme.RoundedXl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFeedbackBottomSheet(
    onDismiss: () -> Unit,
    onNext: () -> Unit,
    onSubmit: (Int, String) -> Unit,
    isSubmitting: Boolean = false,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ColorWhite,
        dragHandle = null,
        shape = Rounded2xl,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Close Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 12.dp, top = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.common_close),
                        tint = ColorGray900
                    )
                }
            }

            // Title
            Text(
                text = stringResource(R.string.feedback_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ColorGray900,
            )

            Spacer(Modifier.height(12.dp))

            // Subtitle
            Text(
                text = stringResource(R.string.feedback_subtitle),
                fontSize = 15.sp,
                color = ColorGray600,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(32.dp))

            // Star Rating
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) { index ->
                    val isChecked = index < rating
                    Image(
                        painter = painterResource(
                            if (isChecked) R.drawable.star_check else R.drawable.star_uncheck
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .size(50.dp)
                            .clickable { rating = index + 1 }
                    )
                }
            }

            // Comment Input (Dynamic Visibility)
            AnimatedVisibility(
                visible = rating > 0,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(Modifier.height(32.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        OutlinedTextField(
                            value = comment,
                            onValueChange = { if (it.length <= 100) comment = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.feedback_placeholder),
                                    color = ColorGray400,
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp
                                )
                            },
                            shape = RoundedLg,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ColorGray200,
                                unfocusedBorderColor = ColorGray200,
                                cursorColor = ColorBrandPrimary,
                                focusedContainerColor = ColorWhite,
                                unfocusedContainerColor = ColorWhite,
                            ),
                            textStyle = TextStyle(
                                fontSize = 15.sp,
                                color = ColorGray900,
                                lineHeight = 22.sp
                            ),
                            singleLine = false
                        )

                        // Character Counter
                        Text(
                            text = stringResource(R.string.feedback_counter, comment.length),
                            fontSize = 12.sp,
                            color = ColorGray400,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 16.dp, end = 16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            // Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Next Button
                OutlinedButton(
                    onClick = onNext,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedXl,
                    border = BorderStroke(1.dp, ColorBrandPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = ColorWhite,
                        contentColor = ColorBrandPrimary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.feedback_next),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Submit Button
                Button(
                    onClick = { onSubmit(rating, comment) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedXl,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorBrandPrimary,
                        contentColor = ColorWhite,
                        disabledContainerColor = ColorGray200,
                        disabledContentColor = ColorGray500
                    ),
                    enabled = rating > 0 && !isSubmitting
                ) {
                    Text(
                        text = stringResource(R.string.feedback_submit),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserFeedbackBottomSheetPreview() {
    UserFeedbackBottomSheet(
        onDismiss = {},
        onNext = {},
        onSubmit = { _, _ -> }
    )
}
