package com.windrr.boat.feature.terms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.windrr.boat.R
import com.windrr.boat.ui.theme.ColorBrandPrimary
import com.windrr.boat.ui.theme.ColorGray500
import com.windrr.boat.ui.theme.ColorGray600
import com.windrr.boat.ui.theme.ColorGray900
import com.windrr.boat.ui.theme.ColorWhite
import com.windrr.boat.ui.theme.Margin12
import com.windrr.boat.ui.theme.Margin16
import com.windrr.boat.ui.theme.Margin20
import com.windrr.boat.ui.theme.Margin24
import com.windrr.boat.ui.theme.Margin32
import com.windrr.boat.ui.theme.Margin8

/**
 * 서비스 이용약관 상세 내용 화면.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "서비스 이용약관",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ColorGray900
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.common_back),
                            tint = Color.Unspecified
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorWhite
                )
            )
        },
        containerColor = ColorWhite
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Margin20)
                .padding(bottom = Margin32)
        ) {
            Spacer(Modifier.height(Margin12))
            
            Text(
                text = "서비스 이용약관",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ColorGray900
            )

            Spacer(Modifier.height(Margin32))

            TermsSectionTitle("제 1장 총칙")
            
            TermsArticleTitle("제1조 (목적)")
            TermsBodyText(
                buildAnnotatedString {
                    append("본 약관은 ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("‘보트랩’ 서비스(이하 “서비스”)가 제공하는")
                    }
                    append(" 영수증 및 보증 관리 서비스의 이용과 관련하여 ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("운영자와")
                    }
                    append(" 이용자 간의 권리, 의무 및 책임사항, 기타 필요한 사항을 규정함을 목적으로 합니다.")
                }
            )

            TermsArticleTitle("제2조 (용어의 정의)")
            TermsBulletRow("1", "“운영자”: 보트랩 서비스를 운영하는 주체를 의미합니다.")
            TermsBulletRow("2", "“보트랩”: 운영자가 제공하는 웹사이트 및 모바일 애플리케이션을 의미합니다.")
            TermsBulletRow("3", "“회원”: 본 약관에 동의하고 Apple, Google 등 소셜 로그인 계정을 통해 서비스에 가입하여 서비스를 이용하는 자를 의미합니다.")
            TermsBulletRow("4", "“서비스”: 운영자가 제공하는 영수증 디지털화, 보증 기간 관리 및 관련 제반 서비스를 의미합니다.")
            TermsBulletRow("5", "“데이터”: 회원이 서비스를 통해 업로드한 영수증 및 보증서 이미지, 그리고 이를 AI 기술로 분석하여 추출된 텍스트, 보증 기간, 보증 내용 등 일체의 정보를 의미합니다.")
            TermsBulletRow("6", "“유료서비스”: 운영자가 유료로 제공하는 제반 서비스를 의미합니다.")
            TermsBulletRow("7", "“크레딧”: 서비스 내 기능(영수증 및 보증서 분석 등)을 이용하기 위한 횟수 차감 방식의 전용 재화를 의미합니다.")

            TermsArticleTitle("제3조 (약관의 게시 및 변경)")
            TermsBulletRow("1", "본 약관은 서비스 내 화면 또는 운영자가 제공하는 방법을 통해 게시되며, 이용자가 약관에 동의함으로써 효력이 발생합니다.")
            TermsBulletRow("2", "운영자는 관련 법령을 위배하지 않는 범위에서 본 약관을 변경할 수 있습니다.")
            TermsBulletRow("3", "운영자는 약관을 변경하는 경우, 적용일자 및 변경내용을 명시하여 현행 약관과 함께 서비스 내 게시하며, 이용자의 권리 또는 의무에 중대한 영향을 미치는 변경의 경우 서비스 내 게시와 더불어 이메일, 푸시 알림 등 이용자가 충분히 인지할 수 있는 방법으로 별도 고지합니다.")

            Spacer(Modifier.height(Margin32))
            TermsSectionTitle("제 2장 서비스 이용계약")

            TermsArticleTitle("제4조 (회원가입 및 이용계약의 체결)")
            TermsBulletRow("1", "서비스 이용은 Apple, Google 등 소셜 로그인 방식을 통해 회원가입을 완료한 후 이용 가능합니다.")
            TermsBulletRow("2", "이용자는 Google, Apple 등 운영자가 제공하는 인증 수단을 통해 회원가입할 수 있으며, 본 약관 및 개인정보처리방침에 동의함으로써 이용계약이 성립합니다.")
            TermsBulletRow("3", "운영자는 타인의 명의 도용, 허위 정보 기재, 만 14세 미만 가입, 이전에 약관 위반으로 자격을 상실한 경우 등 정당한 사유가 있을 시 가입을 제한할 수 있습니다.")

            TermsArticleTitle("제5조 (이용계약의 해지 및 이용제한)")
            TermsBulletRow("1", "회원은 언제든 서비스 내 메뉴를 통해 탈퇴할 수 있습니다.")
            TermsBulletRow("2", "운영자는 다음 각 호에 해당하는 부정한 행위가 확인될 경우 이용자의 서비스 이용을 제한하거나 계정을 정지할 수 있습니다.")
            Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp)) {
                TermsBulletText("부정한 방법으로 크레딧을 적립하거나 사용하는 경우")
                TermsBulletText("본인의 영수증이 아닌 타인의 영수증을 무단으로 업로드하거나, 타인의 개인정보가 포함된 데이터를 임의로 등록하는 행위")
                TermsBulletText("서비스 운영을 고의로 방해하거나 시스템 해킹 등을 시도하는 경우")
                TermsBulletText("기타 본 약관 및 관련 법령을 위반하는 행위")
            }

            Spacer(Modifier.height(Margin32))
            TermsSectionTitle("제 3장 서비스 제공 및 관리")

            TermsArticleTitle("제6조 (서비스의 내용)")
            TermsBodyText("보트랩은 이미지 스캔을 통한 영수증 디지털화, 보증기간 알림, 자산 기록 서비스를 제공합니다. 서비스는 연중무휴 24시간 제공을 원칙으로 하되, 시스템 보수나 정전 등 불가항력적 사유 시 중단될 수 있습니다.")

            TermsArticleTitle("제7조 (데이터 및 크레딧 관리)")
            TermsBulletRow("1", "데이터 기준: 서비스 운영 및 데이터 무결성 유지를 위해, 서버에 저장된 데이터를 최종 기준으로 합니다. 클라이언트(사용자 기기)와 서버 간 데이터 불일치가 발생할 경우 서버의 수치를 우선 적용합니다.")
            TermsBulletRow("2", "크레딧 관리: 크레딧은 양도, 상속, 담보 제공이 불가능하며, 부정 적립된 크레딧은 발견 즉시 회수합니다.")

            TermsArticleTitle("제8조 (게시물 및 콘텐츠)")
            TermsBodyText("회원이 서비스에 게시한 게시물의 저작권은 작성자에게 있습니다. 다만, 회사는 서비스 운영 및 홍보를 위해 필요한 범위 내에서 이를 이용할 수 있습니다.")

            Spacer(Modifier.height(Margin32))
            TermsSectionTitle("제 4장 유료서비스 및 환불")

            TermsArticleTitle("제9조 (유료서비스 결제 및 환불)")
            TermsBulletRow("1", "유료서비스 이용 시 이용대금을 납부해야 하며, 결제 정보 입력의 책임은 이용자 본인에게 있습니다.")
            TermsBulletRow("2", "유료서비스는 이용자의 선택에 따라 건별 결제(1회성) 또는 구독형 결제(정기) 방식 중 선택하여 이용할 수 있습니다.")
            TermsBulletRow("3", "환불 규정:")
            Column(modifier = Modifier.padding(start = 16.dp, top = 4.dp)) {
                TermsBulletText("이용자 귀책 시: 1회성 결제 서비스는 사용 여부에 따라 차감 후 환불하며, 구독형 서비스는 결제된 이용 기간 내 해지 시 관련 법령에 따라 잔여 기간을 일할 계산하여 환불합니다.")
                TermsBulletText("운영자 귀책 시: 서비스 장애나 미제공 시 전액 환불합니다.")
            }
            TermsBulletRow("4", "환불은 결제수단과 동일한 방법으로 진행하며, 환불 의무가 발생한 날로부터 3영업일 이내에 처리합니다.")

            Spacer(Modifier.height(Margin32))
            TermsSectionTitle("제 5장 개인정보 및 책임 제한")

            TermsArticleTitle("제10조 (개인정보 보호)")
            TermsBodyText("서비스 이용과 관련된 개인정보의 수집, 이용, 보호 등에 관한 사항은 별도로 고지하는 '개인정보처리방침'에 따릅니다.")

            TermsArticleTitle("제11조 (보증 정보의 신뢰도)")
            TermsBodyText("본 서비스는 AI OCR 분석 결과를 기반으로 영수증 및 보증 정보를 제공하며, 정확하고 유용한 정보를 제공하기 위해 최선의 노력을 다합니다. 다만, 분석 과정에서 실제 정보와 차이가 발생할 수 있으므로, 중요한 보증 관련 확인 시 원본 증빙 서류를 대조할 것을 권장합니다. 운영자의 고의 또는 중대한 과실이 없는 한, 시스템상 발생하는 분석 정보의 차이로 인한 손해에 대하여 운영자는 책임을 부담하지 않습니다.")

            TermsArticleTitle("제12조 (손해배상 및 면책)")
            TermsBulletRow("1", "운영자는 천재지변, 이동통신사 장애 등 운영자의 귀책사유가 없는 불가항력적 장애에 대해 책임을 지지 않습니다.")
            TermsBulletRow("2", "이용자의 부정한 행위(타인 데이터 무단 업로드 등)로 인해 발생하는 모든 민·형사상 책임은 해당 회원 본인에게 있으며, 운영자는 이에 대해 어떠한 책임도 지지 않습니다.")
            TermsBulletRow("3", "이용자 간 발생한 분쟁에 대해 운영자는 개입할 의무가 없습니다.")

            TermsArticleTitle("제13조 (분쟁 조정 및 관할)")
            TermsBodyText("본 약관 관련 분쟁은 대한민국 법령을 준거법으로 하며, 민사소송법상 관할법원을 관할로 합니다.")

            Spacer(Modifier.height(Margin32))
            
            Text(
                text = "[부칙]",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = ColorGray900
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "본 약관은 2026. 07. 12.부터 시행됩니다.",
                fontSize = 14.sp,
                color = ColorGray600,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(Margin16))

            Text(
                text = "[고객 문의]",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = ColorGray900
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = buildAnnotatedString {
                    append("서비스 이용 중 불편사항이나 문의사항은 ")
                    withStyle(SpanStyle(color = ColorBrandPrimary, fontWeight = FontWeight.SemiBold)) {
                        append("team.swyp8.app@gmail.com")
                    }
                    append("으로 연락해주시기 바랍니다.")
                },
                fontSize = 14.sp,
                color = ColorGray600,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun TermsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = ColorGray900,
        modifier = Modifier.padding(vertical = Margin16)
    )
}

@Composable
private fun TermsArticleTitle(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = ColorGray900,
        modifier = Modifier.padding(top = Margin12, bottom = Margin8)
    )
}

@Composable
private fun TermsBodyText(text: CharSequence) {
    if (text is String) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = ColorGray600,
            lineHeight = 22.sp
        )
    } else {
        Text(
            text = text as androidx.compose.ui.text.AnnotatedString,
            fontSize = 14.sp,
            color = ColorGray600,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun TermsBulletRow(number: String, text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "$number. ",
            fontSize = 14.sp,
            color = ColorGray600,
            lineHeight = 22.sp
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = ColorGray600,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun TermsBulletText(text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "• ",
            fontSize = 14.sp,
            color = ColorGray600,
            lineHeight = 22.sp
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = ColorGray600,
            lineHeight = 22.sp
        )
    }
}
