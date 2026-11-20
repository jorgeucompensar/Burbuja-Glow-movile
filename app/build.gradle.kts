import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val firebaseConfigured = rootProject.file("app/google-services.json").exists()
if (firebaseConfigured) {
    apply(plugin = "com.google.gms.google-services")
}

val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
fun prop(key: String, default: String) = (localProperties.getProperty(key) ?: default)
    .replace("\\", "\\\\").replace("\"", "\\\"")
fun rawProp(key: String, default: String) = localProperties.getProperty(key) ?: default

val releaseStoreFile = rootProject.file(rawProp("RELEASE_STORE_FILE", ""))

android {
    namespace = "com.example.burbujaglow"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.burbujaglow"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        buildConfigField("boolean", "FIREBASE_CONFIGURED", firebaseConfigured.toString())
        buildConfigField("String", "ADMIN_INVITE_CODE", "\"${prop("ADMIN_INVITE_CODE", "BURBUJA-ADMIN")}\"")
        buildConfigField("String", "DEMO_ADMIN_EMAIL", "\"${prop("DEMO_ADMIN_EMAIL", "admin@burbujaglow.test")}\"")
        buildConfigField("String", "DEMO_VENDOR_EMAIL", "\"${prop("DEMO_VENDOR_EMAIL", "vendedor@burbujaglow.test")}\"")
        buildConfigField("String", "DEMO_ADMIN_PASSWORD", "\"${prop("DEMO_ADMIN_PASSWORD", "demo-admin")}\"")
        buildConfigField("String", "DEMO_VENDOR_PASSWORD", "\"${prop("DEMO_VENDOR_PASSWORD", "demo-vendor")}\"")
        buildConfigField("String", "EPAYCO_PUBLIC_KEY", "\"${prop("EPAYCO_PUBLIC_KEY", "")}\"")
        buildConfigField("String", "EPAYCO_CUST_ID", "\"${prop("EPAYCO_CUST_ID", "")}\"")
        buildConfigField("String", "EPAYCO_SESSION_URL", "\"${prop("EPAYCO_SESSION_URL", "")}\"")
        buildConfigField("boolean", "EPAYCO_TEST_MODE", (prop("EPAYCO_TEST_MODE", "true") == "true").toString())

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (releaseStoreFile.exists()) {
                storeFile = releaseStoreFile
                storePassword = prop("RELEASE_STORE_PASSWORD", "")
                keyAlias = rawProp("RELEASE_KEY_ALIAS", "burbujaglow")
                keyPassword = prop("RELEASE_KEY_PASSWORD", "")
            }
        }
    }

    buildTypes {
        release {
            if (releaseStoreFile.exists()) signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.coil.compose)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.play.services.location)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}