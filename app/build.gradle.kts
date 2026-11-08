plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.kapt") }
android {
    namespace = "py.una.medstudy"
    compileSdk = 35
    defaultConfig { applicationId = "py.una.medstudy"; minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "0.1.0" }
    // La firma se toma de variables de entorno para no compartir la clave privada.
    signingConfigs {
        create("releasePublica") {
            val ruta = System.getenv("APP_SIGNING_KEYSTORE")
            if (!ruta.isNullOrBlank()) {
                storeFile = file(ruta)
                storePassword = System.getenv("APP_SIGNING_STORE_PASSWORD")
                keyAlias = System.getenv("APP_SIGNING_KEY_ALIAS")
                keyPassword = System.getenv("APP_SIGNING_KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("releasePublica")
        }
    }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.15" }
    kotlinOptions { jvmTarget = "17" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}
dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}
