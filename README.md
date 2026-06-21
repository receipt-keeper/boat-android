# BOAT — Android

> 영수증 사진 한 장으로 관리하는 무상 A/S 케어 앱
> 영수증을 OCR로 인식해 제품·구매일·보증기간을 자동 추출하고, A/S 만료 전 알림을 보내줍니다.

스위프 앱스쿨 5기 8조 사이드 프로젝트 · GitHub 조직 [`receipt-keeper`](https://github.com/receipt-keeper)

---

## 기술 스택

| 영역 | 사용 기술 |
|---|---|
| **언어 / 빌드** | Kotlin 2.3, Gradle (KTS) + Version Catalog(`libs.versions.toml`), KSP |
| **UI** | Jetpack Compose (BOM), Material 3, Compose Navigation |
| **아키텍처** | MVI (State / Intent / ViewModel), `StateFlow`, Repository 패턴 |
| **네트워크** | Retrofit 2 + Gson, OkHttp (Interceptor / Authenticator), `BaseResponse` envelope |
| **로컬 저장소** | DataStore(Preferences) — 토큰, Room — 영수증 데이터(예정) |
| **인증** | Firebase Auth (Google / Apple), 자체 JWT(Access/Refresh) + 토큰 회전 |
| **카메라 / OCR** | CameraX, Google Cloud Vision API → (Gemini 백엔드 연동 예정) |
| **이미지** | Coil 3 |
| **권한** | Accompanist Permissions |
| **알림** | AlarmManager(정확 알람) + `BOOT_COMPLETED` 재등록, Notification Channel |
| **운영** | Firebase Crashlytics, Analytics, App Distribution(테스터 배포) |

- `minSdk 24` / `targetSdk 36` / `compileSdk 36`
- 16KB 페이지 정렬 대응(`useLegacyPackaging = false`) 위해 다수 라이브러리 최신화

---

## 아키텍처 · 패턴

### 1. MVI 단방향 데이터 흐름
화면 단위로 `State` / `Intent` / `ViewModel` 3종을 둡니다.

```
View ──(Intent)──▶ ViewModel ──▶ Repository ──▶ DataSource
  ▲                    │
  └──── State(StateFlow) ┘
```

- `ViewModel`은 `MutableStateFlow<XxxState>`를 단일 소스로 노출(`asStateFlow()`)하고, `handleIntent(intent)`로 모든 사용자 이벤트를 받습니다.
- View는 `collectAsState()`로 상태를 구독하고 화면을 그릴 뿐, 비즈니스 로직을 갖지 않습니다.
- 예: [`feature/auth`](app/src/main/java/com/windrr/boat/feature/auth) — `AuthState` / `AuthIntent` / `AuthViewModel` / `LoginScreen`

### 2. 패키지 구조 (레이어드 + 기능 단위)
```
com.windrr.boat
├─ core/            # 도메인 비종속 공통 모듈
│  ├─ ocr/          #   영수증 텍스트 파서 (알고리즘)
│  ├─ notification/ #   AlarmManager 기반 A/S 알림 스케줄러
│  ├─ permission/   #   카메라·알람 권한 추상화
│  ├─ crash/  log/  #   Crashlytics / 통합 로깅(BoatLog)
│  └─ ApiResult.kt  #   API 결과 sealed 타입
├─ data/
│  ├─ remote/       # Retrofit ApiService, ApiClient, interceptor, model
│  ├─ local/        # DataStore (토큰)
│  └─ repository/   # BaseRepository + 도메인별 Repository
├─ feature/         # 화면 단위(auth, home, terms, ocr, gallery, notification)
└─ ui/              # 디자인 시스템(theme, component)
```

### 3. 네트워크 계층

**공통 결과 타입 — `ApiResult<T>`**
모든 응답을 `Idle / Loading / Success / Error(code, message)` 4-상태 sealed class로 감싸 View에서 `when`으로 분기합니다. ([ApiResult.kt](app/src/main/java/com/windrr/boat/core/ApiResult.kt))

**`BaseRepository.apiCall {}`**
Retrofit 호출을 한 번 감싸 네트워크 오류 / 서버 오류 / 빈 응답을 일괄 처리하고, 서버 envelope(`BaseResponse<T>`)에서 `data`만 추출해 `ApiResult<T>`로 반환합니다. ([BaseRepository.kt](app/src/main/java/com/windrr/boat/data/repository/BaseRepository.kt))

**토큰 자동 주입 / 갱신 — OkHttp 파이프라인**
- `TokenInterceptor` — 모든 요청에 `Authorization: Bearer <accessToken>` 자동 첨부
- `TokenAuthenticator` — **401 응답 시** OkHttp가 자동 호출 → RefreshToken으로 재발급 → 새 토큰을 단 요청을 반환하면 OkHttp가 원래 요청을 **투명하게 재시도**

```
API 호출(만료 토큰) → 401 → authenticate() → /auth/refresh → 토큰 저장
        → 새 토큰 단 Request 반환 → OkHttp 자동 재시도 → 성공
```

토큰 갱신의 안전장치:
- **재귀 방지** — refresh 호출은 Interceptor/Authenticator가 없는 **별도 클라이언트**로 수행
- **동시성 제어** — `@Synchronized` + "실패한 요청의 토큰 vs 현재 토큰" 비교로 동시 401 시 **중복 갱신 차단**
- **무한루프 방지** — `priorResponse` 카운트로 재시도 1회 제한
- refresh마저 401(회전된 토큰 재사용 등)이면 토큰 삭제 → 자동 로그아웃

([interceptor/](app/src/main/java/com/windrr/boat/data/remote/interceptor) · [ApiClient.kt](app/src/main/java/com/windrr/boat/data/remote/ApiClient.kt))

### 4. 인증 플로우
소셜 로그인 → 백엔드 로그인 API는 **약관 동의 시점에 단 1회** 호출하여, 신규/기존 유저 판단을 **백엔드(서버)를 소스 오브 트루스**로 위임합니다. 발급된 Access/Refresh 토큰은 DataStore에 저장되어 앱 재시작 시 로그인 상태가 유지됩니다.

### 5. 영수증 OCR 파싱 알고리즘 — `ReceiptTextParser`
Vision API가 반환한 영수증 전문(fullText)에서 **규칙 기반(정규식 + 키워드 매칭)**으로 필드를 추출합니다. ([ReceiptTextParser.kt](app/src/main/java/com/windrr/boat/core/ocr/ReceiptTextParser.kt))

| 필드 | 추출 전략 |
|---|---|
| 제품명 | ① 키워드(`제품명:`, `모델명:` …) 뒤 값 → ② 모델번호 패턴(`SM-S928B`) |
| 가격 | 공백 정규화 후 금액 키워드 매칭, 실패 시 `원` 명시 금액 중 최댓값(fallback) |
| 구매일 | 날짜 키워드 우선, `(?<!\d)` lookbehind로 사업자번호 등 오탐 방지 → ISO 변환 |
| 보증기간 | `n년`/`n개월`/`warranty` 패턴 → 개월 수로 정규화 |
| 시리얼 | `S/N`, `일련번호` 키워드 뒤 영숫자 6+ |
| 상품명 목록 | `상품명 단가 수량 금액` 테이블 헤더 이후 텍스트 행 수집(카드 영수증 대응) |
| 카테고리 | 제품명 → PRD 14종 대분류 키워드 매핑 |

설계 포인트:
- **키워드 우선 → 패턴 fallback** 2단계로 다양한 영수증 포맷에 대응
- 열감지 프린터 출력의 공백 깨짐("결제 금 액"), 레이블·값 줄 분리, 한글 음절 내부 오탐("상품**품명**") 등 **실제 영수증 노이즈를 케이스별로 방어**
- 현재 온디바이스 규칙 기반 → 추후 Gemini 백엔드 파싱으로 대체 예정

### 6. A/S 만료 알림
`AlarmManager`의 정확 알람으로 보증 만료 전 알림을 예약하고, `BOOT_COMPLETED` 수신 시 알람을 재등록합니다. ([core/notification/](app/src/main/java/com/windrr/boat/core/notification))

### 7. 디자인 시스템 (`ui/`)
색상·치수·모양·타이포를 토큰화하고(`Color`, `Dimens`, `Shape`, `Type`), 공통 컴포넌트(`BoatToast`, `BoatDialog`, DatePicker 테마)를 제공합니다. 토스트는 `BoatToast`, DatePicker는 `boatDatePickerColors()` 사용을 규칙으로 합니다.

---

## 빌드 / 실행

1. `local.properties`에 Vision API 키 추가
   ```properties
   VISION_API_KEY=your_key_here
   ```
2. Firebase `google-services.json`을 `app/`에 배치
3. (선택) release 서명·App Distribution 배포 시 루트에 `keystore.properties` 배치
   — 없으면 release 서명만 생략되고 debug 빌드는 정상 동작
4. 빌드
   ```bash
   ./gradlew assembleDebug          # 디버그 APK
   ./gradlew assembleRelease appDistributionUploadRelease   # 테스터 배포
   ```

> `local.properties` · `google-services.json` · `keystore.properties` 는 git에 포함되지 않습니다.

---

## 개발 컨벤션

- **커밋**: `type(scope): 설명` — type `feat|fix|design|refactor|chore`, scope `android|ios|common`
- **머지**: Squash Merge
- **버전/마일스톤**: 앱 독립 버전 라인 `v0.1.0`(인증/홈) → `v1.0.0`(MVP 배포). 진행 관리는 마일스톤으로 묶어 추적
- **문자열**: 하드코딩 금지 → `strings.xml` 리소스 등록
