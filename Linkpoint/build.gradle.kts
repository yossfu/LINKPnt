import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application") version "8.1.4"
    id("org.jetbrains.kotlin.android") version "1.9.22"
}

// Load keystore properties if available
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "com.linkpoint"
    compileSdk = 34
    buildToolsVersion = "34.0.0"
    
    defaultConfig {
        applicationId = "com.linkpoint"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
        
        vectorDrawables.useSupportLibrary = true
        
        // Add build info to BuildConfig
        buildConfigField("String", "BUILD_TIME", "\"${System.currentTimeMillis()}\"")
        buildConfigField("String", "GIT_COMMIT", "\"${getGitHash()}\"")
    }
    
    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = file(keystoreProperties.getProperty("storeFile") ?: "release.keystore")
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = false  // Keep false until stable
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // Sign release builds if keystore is available
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += listOf("-opt-in=kotlin.RequiresOptIn")
    }
    
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    
    sourceSets {
        getByName("main") {
            manifest.srcFile("src/main/AndroidManifest.xml")
            java.srcDirs("src/main/java", "src/main/kotlin")
            kotlin.srcDirs("src/main/java", "src/main/kotlin")
            res.srcDirs("src/main/res")
            assets.srcDirs("src/main/assets")

            // Hito de login (2026-11-18): solo se compila el cierre de login.
            // De los 1.221 archivos .kt del árbol, ~897 son Java descompilado
            // que no compila como Kotlin; reescribirlos uno por uno no es
            // viable y el run #8 falló con miles de errores en
            // :compileDebugKotlin. Con `include` (lista no vacía = solo
            // coinciden estos archivos) el conjunto de compilación queda
            // limitado a los 5 archivos que necesita el arranque hasta la
            // pantalla de login; el resto del árbol sigue en el repo pero
            // fuera de la compilación. Se pone en java Y en kotlin porque
            // el plugin de Kotlin para Android toma fuentes de ambos
            // conjuntos (los .kt de src/main/java también compilarían).
            // Se usa llamada directa a include() sobre cada conjunto:
            // `java { ... }` como bloque no es sintaxis válida del DSL
            // Kotlin de AGP.
            java.include(
                "**/LinkpointApp.kt",
                "**/GridConnectionService.kt",
                "**/ui/login/CleanLoginActivity.kt",
                "**/debug/AutoLogUploader.kt",
                "**/fixes/ResourceConflictResolver.kt"
            )
            kotlin.include(
                "**/LinkpointApp.kt",
                "**/GridConnectionService.kt",
                "**/ui/login/CleanLoginActivity.kt",
                "**/debug/AutoLogUploader.kt",
                "**/fixes/ResourceConflictResolver.kt"
            )
            // Para ampliar el cierre en fases posteriores, añadir aquí el
            // archivo (ya reescrito en Kotlin real o stubbeado) que pida el
            // compilador. El manifest sigue declarando el resto de
            // actividades/servicios; el merger no exige que sus clases
            // existan, y la pantalla de login no los lanza.
        }
    }
    
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
    
    packaging {
        resources {
            excludes += listOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/INDEX.LIST",
                "META-INF/*.kotlin_module",
                "META-INF/io.netty.versions.properties"
            )
            pickFirsts += listOf(
                "**/libjnidispatch.so",
                "**/libopenjpeg.so",
                "**/liblumiya-native.so"
            )
        }
    }
}

dependencies {
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    
    // Google Services
    implementation("com.google.android.gms:play-services-base:18.3.0")
    implementation("com.google.android.gms:play-services-drive:17.0.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    
    // Guava
    implementation("com.google.guava:guava:32.1.3-android")
    
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.22")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // JSON
    implementation("com.google.code.gson:gson:2.10.1")
    
    // WebRTC for voice
    implementation("io.getstream:stream-webrtc-android:1.0.7")
    implementation("io.getstream:stream-webrtc-android-ui:1.0.7")
    
    // Media
    implementation("androidx.media:media:1.7.0")
    
    // UI Components
    implementation("com.astuetz:pagerslidingtabstrip:1.0.1")
    
    // Filament Rendering Engine - Modern PBR renderer
    implementation("com.google.android.filament:filament-android:1.66.0")
    implementation("com.google.android.filament:filament-utils-android:1.66.0")
    implementation("com.google.android.filament:gltfio-android:1.66.0")
    implementation("com.google.android.filament:filamat-android:1.66.0")  // Runtime material compilation
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.10.3")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

// Helper function to get git commit hash
fun getGitHash(): String {
    return try {
        val process = Runtime.getRuntime().exec("git rev-parse --short HEAD")
        process.inputStream.bufferedReader().readText().trim()
    } catch (e: Exception) {
        "unknown"
    }
}