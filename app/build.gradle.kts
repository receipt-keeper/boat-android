import com.google.firebase.appdistribution.gradle.firebaseAppDistribution
import java.util.Properties

val localProps = Properties().apply {
    // CI 등 local.properties가 없는 환경에서도 빌드 설정이 깨지지 않도록 가드
    val localPropsFile = rootProject.file("local.properties")
    if (localPropsFile.exists()) {
        load(localPropsFile.inputStream())
    }
}

// 공유 release 서명 설정 — keystore.properties가 있을 때만 로드(git 미포함).
// 키가 없는 팀원은 release 서명만 생략되고 debug 빌드는 정상 동작.
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) {
        load(keystorePropsFile.inputStream())
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics.plugin)
    alias(libs.plugins.firebase.appdistribution.plugin)
}

android {
    namespace = "com.windrr.boat"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.windrr.boat"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "VISION_API_KEY", "\"${localProps["VISION_API_KEY"]}\"")
    }

    signingConfigs {
        // keystore.properties가 존재할 때만 release 서명 구성
        if (keystorePropsFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProps["storeFile"] as String)
                storePassword = keystoreProps["storePassword"] as String
                keyAlias = keystoreProps["keyAlias"] as String
                keyPassword = keystoreProps["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // 공유 키가 있으면 release 서명 적용 (App Distribution 배포용)
            if (keystorePropsFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }

            // Firebase App Distribution 배포 설정
            // 업로드: ./gradlew assembleRelease appDistributionUploadRelease
            // 릴리스 노트는 CLI로 덮어쓰기 가능: --releaseNotes "내용"
            firebaseAppDistribution {
                appId = "1:925770804748:android:2b734719054e3afa37c829"
                artifactType = "APK"
                groups = "boat-team" // Firebase Console에서 만든 테스터 그룹 별칭
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

dependencies {

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.messaging)

    // CameraX
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)   // Coil 3 원격(http) 이미지 로딩에 필수

    // Haze — 글래스모피즘 backdrop blur (하단 플로팅 바)
    implementation(libs.haze)
    implementation(libs.haze.materials)

    // Navigation
    implementation(libs.navigation.compose)

    // Accompanist
    implementation(libs.accompanist.permissions)

    // Lottie
    implementation(libs.lottie.compose)

    // Google Sign-In
    implementation(libs.play.services.auth)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // DataStore
    implementation(libs.datastore.preferences)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}