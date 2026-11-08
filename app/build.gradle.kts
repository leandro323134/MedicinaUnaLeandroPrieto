plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.kapt") }
val parallelInstall = providers.gradleProperty("parallelInstall").orNull == "true"
android {
    namespace = "py.una.medstudy"
    compileSdk = 35
    defaultConfig { applicationId = "py.una.medstudy"; minSdk = 26; targetSdk = 35; versionCode = 2; versionName = "0.2.0"
        manifestPlaceholders["appLabel"] = if (parallelInstall) "Medicina UNA Estudio" else "Medicina UNA"
    }
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
        getByName("debug") {
            if (parallelInstall) applicationIdSuffix = ".estudio"
        }
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("releasePublica")
        }
    }
    testOptions { unitTests.isIncludeAndroidResources = true }
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
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.google.code.gson:gson:2.11.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}
