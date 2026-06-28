import os

def write(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w') as f:
        f.write(content)
    print(f"✅ Created: {path}")

# ── settings.gradle.kts ──────────────────────────────
write("settings.gradle.kts", '''pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\\\.android.*")
                includeGroupByRegex("com\\\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "Lumio"
include(":app")
''')

# ── build.gradle.kts (root) ──────────────────────────
write("build.gradle.kts", '''plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android)      apply false
    alias(libs.plugins.kotlin.compose)      apply false
    alias(libs.plugins.hilt.android)        apply false
    alias(libs.plugins.ksp)                 apply false
}
''')

# ── gradle.properties ────────────────────────────────
write("gradle.properties", '''org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true
android.useAndroidX=true
android.enableJetifier=false
android.nonTransitiveRClass=true
kotlin.code.style=official
kotlin.incremental=true
''')

# ── gradle/libs.versions.toml ────────────────────────
write("gradle/libs.versions.toml", '''[versions]
agp                 = "8.5.2"
kotlin              = "2.0.21"
ksp                 = "2.0.21-1.0.28"
coreKtx             = "1.13.1"
activityCompose     = "1.9.3"
lifecycle           = "2.8.7"
splashscreen        = "1.0.1"
biometric           = "1.1.0"
composeBom          = "2024.09.03"
navigationCompose   = "2.8.3"
room                = "2.6.1"
datastore           = "1.1.1"
hilt                = "2.51.1"
hiltExt             = "1.2.0"
workManager         = "2.9.1"
gson                = "2.11.0"
coil                = "2.7.0"
accompanist         = "0.36.0"
coroutines          = "1.9.0"
junit               = "4.13.2"
junitExt            = "1.2.1"
espresso            = "3.6.1"

[libraries]
androidx-core-ktx                    = { group = "androidx.core",            name = "core-ktx",                      version.ref = "coreKtx"        }
androidx-activity-compose            = { group = "androidx.activity",         name = "activity-compose",              version.ref = "activityCompose" }
androidx-core-splashscreen           = { group = "androidx.core",             name = "core-splashscreen",             version.ref = "splashscreen"    }
androidx-biometric                   = { group = "androidx.biometric",        name = "biometric",                     version.ref = "biometric"       }
androidx-lifecycle-runtime-ktx       = { group = "androidx.lifecycle",        name = "lifecycle-runtime-ktx",         version.ref = "lifecycle"       }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle",        name = "lifecycle-viewmodel-compose",   version.ref = "lifecycle"       }
androidx-lifecycle-runtime-compose   = { group = "androidx.lifecycle",        name = "lifecycle-runtime-compose",     version.ref = "lifecycle"       }
androidx-compose-bom                 = { group = "androidx.compose",          name = "compose-bom",                   version.ref = "composeBom"      }
androidx-ui                          = { group = "androidx.compose.ui",       name = "ui"                                                             }
androidx-ui-graphics                 = { group = "androidx.compose.ui",       name = "ui-graphics"                                                    }
androidx-ui-tooling                  = { group = "androidx.compose.ui",       name = "ui-tooling"                                                     }
androidx-ui-tooling-preview          = { group = "androidx.compose.ui",       name = "ui-tooling-preview"                                             }
androidx-ui-test-manifest            = { group = "androidx.compose.ui",       name = "ui-test-manifest"                                               }
androidx-ui-test-junit4              = { group = "androidx.compose.ui",       name = "ui-test-junit4"                                                 }
androidx-material3                   = { group = "androidx.compose.material3",name = "material3"                                                      }
androidx-material-icons-extended     = { group = "androidx.compose.material", name = "material-icons-extended"                                        }
androidx-navigation-compose          = { group = "androidx.navigation",       name = "navigation-compose",            version.ref = "navigationCompose"}
androidx-room-runtime                = { group = "androidx.room",             name = "room-runtime",                  version.ref = "room"            }
androidx-room-ktx                    = { group = "androidx.room",             name = "room-ktx",                      version.ref = "room"            }
androidx-room-compiler               = { group = "androidx.room",             name = "room-compiler",                 version.ref = "room"            }
hilt-android                         = { group = "com.google.dagger",         name = "hilt-android",                  version.ref = "hilt"            }
hilt-compiler                        = { group = "com.google.dagger",         name = "hilt-android-compiler",         version.ref = "hilt"            }
hilt-navigation-compose              = { group = "androidx.hilt",             name = "hilt-navigation-compose",       version.ref = "hiltExt"         }
hilt-work                            = { group = "androidx.hilt",             name = "hilt-work",                     version.ref = "hiltExt"         }
hilt-ext-compiler                    = { group = "androidx.hilt",             name = "hilt-compiler",                 version.ref = "hiltExt"         }
androidx-work-runtime-ktx            = { group = "androidx.work",             name = "work-runtime-ktx",              version.ref = "workManager"     }
androidx-datastore-preferences       = { group = "androidx.datastore",        name = "datastore-preferences",         version.ref = "datastore"       }
kotlinx-coroutines-android           = { group = "org.jetbrains.kotlinx",     name = "kotlinx-coroutines-android",    version.ref = "coroutines"      }
coil-compose                         = { group = "io.coil-kt",                name = "coil-compose",                  version.ref = "coil"            }
accompanist-permissions              = { group = "com.google.accompanist",    name = "accompanist-permissions",       version.ref = "accompanist"     }
gson                                 = { group = "com.google.code.gson",      name = "gson",                          version.ref = "gson"            }
junit                                = { group = "junit",                     name = "junit",                         version.ref = "junit"           }
androidx-junit                       = { group = "androidx.test.ext",         name = "junit",                         version.ref = "junitExt"        }
androidx-espresso-core               = { group = "androidx.test.espresso",    name = "espresso-core",                 version.ref = "espresso"        }

[plugins]
android-application = { id = "com.android.application",             version.ref = "agp"    }
kotlin-android      = { id = "org.jetbrains.kotlin.android",        version.ref = "kotlin" }
kotlin-compose      = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
hilt-android        = { id = "com.google.dagger.hilt.android",      version.ref = "hilt"   }
ksp                 = { id = "com.google.devtools.ksp",             version.ref = "ksp"    }
''')

# ── gradle/wrapper/gradle-wrapper.properties ─────────
write("gradle/wrapper/gradle-wrapper.properties", '''distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\\://services.gradle.org/distributions/gradle-8.7-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
''')

# ── app/build.gradle.kts ─────────────────────────────
write("app/build.gradle.kts", '''plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace   = "com.lumio.app"
    compileSdk  = 35

    defaultConfig {
        applicationId   = "com.lumio.app"
        minSdk          = 26
        targetSdk       = 35
        versionCode     = 1
        versionName     = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        debug {
            isDebuggable        = true
            applicationIdSuffix = ".debug"
            isMinifyEnabled     = false
        }
        release {
            isMinifyEnabled     = true
            isShrinkResources   = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }

    buildFeatures {
        compose     = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += setOf("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

ksp {
    arg("room.schemaLocation",  "$projectDir/schemas")
    arg("room.incremental",     "true")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.ext.compiler)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.coil.compose)
    implementation(libs.accompanist.permissions)
    implementation(libs.gson)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
}
''')

# ── app/proguard-rules.pro ───────────────────────────
write("app/proguard-rules.pro", '''-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep class com.lumio.app.data.** { *; }
-keep class com.lumio.app.domain.model.** { *; }
-keep class com.lumio.app.receiver.** { *; }
-keep class com.lumio.app.widget.** { *; }
-dontwarn dagger.hilt.**
-dontwarn androidx.room.**
-dontwarn kotlinx.coroutines.**
''')

# ── AndroidManifest.xml ──────────────────────────────
write("app/src/main/AndroidManifest.xml", '''<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.USE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.USE_BIOMETRIC" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name=".LumioApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.Lumio"
        android:hardwareAccelerated="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.Lumio.Splash"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <receiver android:name=".receiver.BootReceiver"
            android:exported="true">
            <intent-filter android:priority="999">
                <action android:name="android.intent.action.BOOT_COMPLETED" />
            </intent-filter>
        </receiver>

        <receiver android:name=".receiver.AlarmReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="com.lumio.app.REMINDER_ALARM" />
            </intent-filter>
        </receiver>

        <receiver android:name=".receiver.NotificationActionReceiver"
            android:exported="false">
            <intent-filter>
                <action android:name="com.lumio.app.ACTION_SNOOZE_5" />
                <action android:name="com.lumio.app.ACTION_SNOOZE_15" />
                <action android:name="com.lumio.app.ACTION_SNOOZE_30" />
                <action android:name="com.lumio.app.ACTION_MARK_DONE" />
            </intent-filter>
        </receiver>

        <receiver android:name=".widget.SmallWidgetProvider"
            android:exported="true"
            android:label="@string/widget_small_name">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/small_widget_info" />
        </receiver>

        <receiver android:name=".widget.LargeWidgetProvider"
            android:exported="true"
            android:label="@string/widget_large_name">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/large_widget_info" />
        </receiver>

    </application>
</manifest>
''')

# ── LumioApp.kt ──────────────────────────────────────
write("app/src/main/java/com/lumio/app/LumioApp.kt", '''package com.lumio.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LumioApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS, "Reminders", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Your scheduled reminders"
                enableVibration(true)
                enableLights(true)
            }

            val alarmChannel = NotificationChannel(
                CHANNEL_ALARMS, "Priority Alarms", NotificationManager.IMPORTANCE_MAX
            ).apply {
                description = "High-priority reminder alarms"
                enableVibration(true)
                setBypassDnd(true)
            }

            val silentChannel = NotificationChannel(
                CHANNEL_SILENT, "Silent Reminders", NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Silent notifications"
                enableVibration(false)
                setSound(null, null)
            }

            manager.createNotificationChannels(
                listOf(reminderChannel, alarmChannel, silentChannel)
            )
        }
    }

    companion object {
        const val CHANNEL_REMINDERS     = "lumio_reminders_channel"
        const val CHANNEL_ALARMS        = "lumio_alarms_channel"
        const val CHANNEL_SILENT        = "lumio_silent_channel"
        const val ACTION_SNOOZE_5       = "com.lumio.app.ACTION_SNOOZE_5"
        const val ACTION_SNOOZE_15      = "com.lumio.app.ACTION_SNOOZE_15"
        const val ACTION_SNOOZE_30      = "com.lumio.app.ACTION_SNOOZE_30"
        const val ACTION_MARK_DONE      = "com.lumio.app.ACTION_MARK_DONE"
        const val EXTRA_REMINDER_ID     = "reminder_id"
        const val ACTION_REMINDER_ALARM = "com.lumio.app.REMINDER_ALARM"
        const val DATABASE_NAME         = "lumio_database"
    }
}
''')

# ── MainActivity.kt ──────────────────────────────────
write("app/src/main/java/com/lumio/app/MainActivity.kt", '''package com.lumio.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.lumio.app.presentation.navigation.LumioNavGraph
import com.lumio.app.presentation.theme.LumioTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LumioTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LumioNavGraph()
                }
            }
        }
    }
}
''')

# ── Color.kt ─────────────────────────────────────────
write("app/src/main/java/com/lumio/app/presentation/theme/Color.kt", '''package com.lumio.app.presentation.theme

import androidx.compose.ui.graphics.Color

val LumioBlue         = Color(0xFF1A73E8)
val LumioBlueDim      = Color(0xFF4A9EFF)
val LumioPurple       = Color(0xFF7B2FBE)
val LumioPurpleDim    = Color(0xFFAB63F5)
val LumioTeal         = Color(0xFF00897B)
val LumioGreen        = Color(0xFF2E7D32)
val LumioOrange       = Color(0xFFFF6B35)
val LumioRed          = Color(0xFFD32F2F)
val LumioYellow       = Color(0xFFF9A825)
val LumioPink         = Color(0xFFE91E63)

val PriorityUrgent    = Color(0xFFD32F2F)
val PriorityHigh      = Color(0xFFFF6B35)
val PriorityMedium    = Color(0xFFF9A825)
val PriorityLow       = Color(0xFF4CAF50)
val PriorityNone      = Color(0xFF9E9E9E)

val BackgroundLight   = Color(0xFFF8F9FA)
val SurfaceLight      = Color(0xFFFFFFFF)
val SurfaceVarLight   = Color(0xFFE8F0FE)

val BackgroundDark    = Color(0xFF0F0F0F)
val SurfaceDark       = Color(0xFF1E1E1E)
val SurfaceVarDark    = Color(0xFF2A2A2A)

val BackgroundAMOLED  = Color(0xFF000000)
val SurfaceAMOLED     = Color(0xFF0D0D0D)
val SurfaceVarAMOLED  = Color(0xFF111111)

val TextPrimaryLight      = Color(0xFF1A1A1A)
val TextSecondaryLight    = Color(0xFF5F5F5F)
val TextPrimaryDark       = Color(0xFFF5F5F5)
val TextSecondaryDark     = Color(0xFFAAAAAA)

val ColorSuccess      = Color(0xFF4CAF50)
val ColorError        = Color(0xFFD32F2F)
val ColorWarning      = Color(0xFFFFC107)
val ColorInfo         = Color(0xFF2196F3)
val ColorTransparent  = Color(0x00000000)
''')

# ── Theme.kt ─────────────────────────────────────────
write("app/src/main/java/com/lumio/app/presentation/theme/Theme.kt", '''package com.lumio.app.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode { LIGHT, DARK, AMOLED, SYSTEM }

val LocalAmoledMode = compositionLocalOf { false }

private val LightColors = lightColorScheme(
    primary              = LumioBlue,
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFFD3E4FF),
    onPrimaryContainer   = Color(0xFF001B3D),
    secondary            = LumioPurple,
    onSecondary          = Color.White,
    background           = BackgroundLight,
    onBackground         = TextPrimaryLight,
    surface              = SurfaceLight,
    onSurface            = TextPrimaryLight,
    surfaceVariant       = SurfaceVarLight,
    onSurfaceVariant     = TextSecondaryLight,
    error                = ColorError,
    onError              = Color.White,
)

private val DarkColors = darkColorScheme(
    primary              = LumioBlueDim,
    onPrimary            = Color(0xFF003064),
    primaryContainer     = Color(0xFF004594),
    onPrimaryContainer   = Color(0xFFD3E4FF),
    secondary            = LumioPurpleDim,
    onSecondary          = Color(0xFF4B007F),
    background           = BackgroundDark,
    onBackground         = TextPrimaryDark,
    surface              = SurfaceDark,
    onSurface            = TextPrimaryDark,
    surfaceVariant       = SurfaceVarDark,
    onSurfaceVariant     = TextSecondaryDark,
    error                = Color(0xFFFFB4AB),
    onError              = Color(0xFF690005),
)

private val AmoledColors = DarkColors.copy(
    background     = BackgroundAMOLED,
    surface        = SurfaceAMOLED,
    surfaceVariant = SurfaceVarAMOLED,
)

@Composable
fun LumioTheme(
    themeMode: ThemeMode  = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.LIGHT          -> false
        ThemeMode.DARK,
        ThemeMode.AMOLED         -> true
        ThemeMode.SYSTEM         -> isSystemInDarkTheme()
    }
    val isAmoled = themeMode == ThemeMode.AMOLED

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            when {
                isAmoled -> dynamicDarkColorScheme(ctx).copy(
                    background     = BackgroundAMOLED,
                    surface        = SurfaceAMOLED,
                    surfaceVariant = SurfaceVarAMOLED,
                )
                isDark   -> dynamicDarkColorScheme(ctx)
                else     -> dynamicLightColorScheme(ctx)
            }
        }
        isAmoled -> AmoledColors
        isDark   -> DarkColors
        else     -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor     = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars     = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(LocalAmoledMode provides isAmoled) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = LumioTypography,
            content     = content
        )
    }
}
''')

# ── Type.kt ──────────────────────────────────────────
write("app/src/main/java/com/lumio/app/presentation/theme/Type.kt", '''package com.lumio.app.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val LumioTypography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 57.sp, lineHeight = 64.sp),
    displayMedium = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall  = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium= TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium   = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    titleSmall    = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodySmall     = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelLarge    = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium   = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall    = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
)
''')

# ── Screen.kt ────────────────────────────────────────
write("app/src/main/java/com/lumio/app/presentation/navigation/Screen.kt", '''package com.lumio.app.presentation.navigation

sealed class Screen(val route: String) {
    object Home           : Screen("home")
    object Calendar       : Screen("calendar")
    object Categories     : Screen("categories")
    object Settings       : Screen("settings")
    object AddReminder    : Screen("add_reminder")
    object Search         : Screen("search")

    object EditReminder : Screen("edit_reminder/{reminderId}") {
        fun createRoute(id: Long) = "edit_reminder/$id"
    }
    object ReminderDetail : Screen("reminder_detail/{reminderId}") {
        fun createRoute(id: Long) = "reminder_detail/$id"
    }
    object CategoryDetail : Screen("category_detail/{categoryId}") {
        fun createRoute(id: Long) = "category_detail/$id"
    }
}
''')

# ── LumioNavGraph.kt ─────────────────────────────────
write("app/src/main/java/com/lumio/app/presentation/navigation/LumioNavGraph.kt", '''package com.lumio.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lumio.app.presentation.screens.home.HomeScreen

@Composable
fun LumioNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.AddReminder.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Calendar.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Categories.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Search.route) {
            HomeScreen(navController = navController)
        }
        composable(
            route = Screen.EditReminder.route,
            arguments = listOf(navArgument("reminderId") { type = NavType.LongType; defaultValue = -1L })
        ) { HomeScreen(navController = navController) }

        composable(
            route = Screen.ReminderDetail.route,
            arguments = listOf(navArgument("reminderId") { type = NavType.LongType; defaultValue = -1L })
        ) { HomeScreen(navController = navController) }

        composable(
            route = Screen.CategoryDetail.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.LongType; defaultValue = -1L })
        ) { HomeScreen(navController = navController) }
    }
}
''')

# ── HomeScreen.kt ────────────────────────────────────
write("app/src/main/java/com/lumio/app/presentation/screens/home/HomeScreen.kt", '''package com.lumio.app.presentation.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(navController: NavController) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(800), label = "alpha"
    )
    val scale by animateFloatAsState(
        targetValue   = if (visible) 1f else 0.8f,
        animationSpec = tween(800), label = "scale"
    )
    LaunchedEffect(Unit) { delay(200); visible = true }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.alpha(alpha).scale(scale),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsActive,
                        contentDescription = "Lumio",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(24.dp))
                Text("LUMIO", style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Text("Smart Reminders", style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(40.dp))
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.shapes.medium)
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text("Phase 1 Complete!", style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
    }
}
''')

# ── AppModule.kt ─────────────────────────────────────
write("app/src/main/java/com/lumio/app/di/AppModule.kt", '''package com.lumio.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context = context
}
''')

# ── Receivers ────────────────────────────────────────
write("app/src/main/java/com/lumio/app/receiver/BootReceiver.kt", '''package com.lumio.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Phase 4: Re-schedule all active reminders
        }
    }
}
''')

write("app/src/main/java/com/lumio/app/receiver/AlarmReceiver.kt", '''package com.lumio.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("reminder_id", -1L)
        // Phase 5: Show notification for reminderId
    }
}
''')

write("app/src/main/java/com/lumio/app/receiver/NotificationActionReceiver.kt", '''package com.lumio.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("reminder_id", -1L)
        when (intent.action) {
            "com.lumio.app.ACTION_SNOOZE_5"  -> { /* Phase 5 */ }
            "com.lumio.app.ACTION_SNOOZE_15" -> { /* Phase 5 */ }
            "com.lumio.app.ACTION_SNOOZE_30" -> { /* Phase 5 */ }
            "com.lumio.app.ACTION_MARK_DONE" -> { /* Phase 5 */ }
        }
    }
}
''')

# ── Widgets ──────────────────────────────────────────
write("app/src/main/java/com/lumio/app/widget/SmallWidgetProvider.kt", '''package com.lumio.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class SmallWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Phase 6: Update small widget
    }
}
''')

write("app/src/main/java/com/lumio/app/widget/LargeWidgetProvider.kt", '''package com.lumio.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class LargeWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Phase 6: Update large widget
    }
}
''')

# ── XML Resources ────────────────────────────────────
write("app/src/main/res/values/strings.xml", '''<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Lumio</string>
    <string name="app_tagline">Smart Reminders</string>
    <string name="widget_small_name">Lumio — Small</string>
    <string name="widget_large_name">Lumio — Large</string>
    <string name="nav_home">Home</string>
    <string name="nav_calendar">Calendar</string>
    <string name="nav_categories">Categories</string>
    <string name="nav_settings">Settings</string>
    <string name="action_add">Add Reminder</string>
    <string name="action_save">Save</string>
    <string name="action_cancel">Cancel</string>
    <string name="action_delete">Delete</string>
    <string name="action_done">Mark Done</string>
    <string name="action_snooze">Snooze</string>
    <string name="snooze_5_min">Snooze 5 min</string>
    <string name="snooze_15_min">Snooze 15 min</string>
    <string name="snooze_30_min">Snooze 30 min</string>
    <string name="priority_urgent">Urgent</string>
    <string name="priority_high">High</string>
    <string name="priority_medium">Medium</string>
    <string name="priority_low">Low</string>
    <string name="empty_no_reminders">No reminders yet</string>
    <string name="empty_add_hint">Tap + to add your first reminder</string>
</resources>
''')

write("app/src/main/res/values/colors.xml", '''<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="lumio_blue">#FF1A73E8</color>
    <color name="lumio_purple">#FF7B2FBE</color>
    <color name="splash_background">#FF1A73E8</color>
    <color name="splash_background_dark">#FF0F0F0F</color>
    <color name="background_light">#FFF8F9FA</color>
    <color name="background_dark">#FF0F0F0F</color>
    <color name="background_amoled">#FF000000</color>
    <color name="transparent">#00000000</color>
</resources>
''')

write("app/src/main/res/values/themes.xml", '''<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.Lumio" parent="Theme.Material3.DayNight.NoActionBar">
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
        <item name="android:windowDrawsSystemBarBackgrounds">true</item>
    </style>
    <style name="Theme.Lumio.Splash" parent="Theme.SplashScreen">
        <item name="windowSplashScreenBackground">@color/splash_background</item>
        <item name="windowSplashScreenAnimatedIcon">@drawable/ic_splash_logo</item>
        <item name="windowSplashScreenAnimationDuration">400</item>
        <item name="postSplashScreenTheme">@style/Theme.Lumio</item>
    </style>
</resources>
''')

write("app/src/main/res/values-night/themes.xml", '''<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.Lumio" parent="Theme.Material3.DayNight.NoActionBar">
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
        <item name="android:windowDrawsSystemBarBackgrounds">true</item>
    </style>
    <style name="Theme.Lumio.Splash" parent="Theme.SplashScreen">
        <item name="windowSplashScreenBackground">@color/splash_background_dark</item>
        <item name="windowSplashScreenAnimatedIcon">@drawable/ic_splash_logo</item>
        <item name="windowSplashScreenAnimationDuration">400</item>
        <item name="postSplashScreenTheme">@style/Theme.Lumio</item>
    </style>
</resources>
''')

write("app/src/main/res/xml/backup_rules.xml", '''<?xml version="1.0" encoding="utf-8"?>
<full-backup-content>
    <include domain="sharedpref" path="." />
    <include domain="database" path="." />
    <include domain="file" path="." />
</full-backup-content>
''')

write("app/src/main/res/xml/data_extraction_rules.xml", '''<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-backup>
        <include domain="sharedpref" path="." />
        <include domain="database" path="." />
    </cloud-backup>
    <device-transfer>
        <include domain="sharedpref" path="." />
        <include domain="database" path="." />
    </device-transfer>
</data-extraction-rules>
''')

write("app/src/main/res/xml/small_widget_info.xml", '''<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="110dp"
    android:minHeight="40dp"
    android:targetCellWidth="2"
    android:targetCellHeight="1"
    android:updatePeriodMillis="1800000"
    android:initialLayout="@layout/widget_small"
    android:resizeMode="horizontal|vertical">
</appwidget-provider>
''')

write("app/src/main/res/xml/large_widget_info.xml", '''<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="250dp"
    android:minHeight="180dp"
    android:targetCellWidth="4"
    android:targetCellHeight="3"
    android:updatePeriodMillis="1800000"
    android:initialLayout="@layout/widget_large"
    android:resizeMode="horizontal|vertical">
</appwidget-provider>
''')

write("app/src/main/res/drawable/ic_splash_logo.xml", '''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path android:fillColor="#FFFFFF"
        android:pathData="M54,14C32.46,14 15,31.46 15,53C15,74.54 32.46,92 54,92C75.54,92 93,74.54 93,53C93,31.46 75.54,14 54,14ZM54,22C71.12,22 85,35.88 85,53C85,70.12 71.12,84 54,84C36.88,84 23,70.12 23,53C23,35.88 36.88,22 54,22Z"/>
    <path android:fillColor="#FFFFFF"
        android:pathData="M54,53L54,33L50,33L50,55L54,55Z"/>
    <path android:fillColor="#FFFFFF"
        android:pathData="M54,53L72,53L72,49L54,49L54,53Z"/>
    <path android:fillColor="#FFFFFF"
        android:pathData="M54,49C51.79,49 50,50.79 50,53C50,55.21 51.79,57 54,57C56.21,57 58,55.21 58,53C58,50.79 56.21,49 54,49Z"/>
</vector>
''')

write("app/src/main/res/layout/widget_small.xml", '''<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="horizontal"
    android:gravity="center_vertical"
    android:padding="12dp">
    <TextView
        android:id="@+id/tv_widget_title"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="No upcoming reminders"
        android:textColor="#FFFFFFFF"
        android:textSize="13sp"
        android:maxLines="1"
        android:ellipsize="end" />
</LinearLayout>
''')

write("app/src/main/res/layout/widget_large.xml", '''<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">
    <TextView
        android:id="@+id/tv_widget_header"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="LUMIO"
        android:textColor="#FFFFFFFF"
        android:textSize="16sp"
        android:textStyle="bold" />
    <ListView
        android:id="@+id/list_widget_reminders"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:divider="@null" />
</LinearLayout>
''')

write("app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml", '''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/lumio_blue" />
    <foreground android:drawable="@drawable/ic_splash_logo" />
</adaptive-icon>
''')

write("app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml", '''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/lumio_blue" />
    <foreground android:drawable="@drawable/ic_splash_logo" />
</adaptive-icon>
''')

# ── GitHub Actions ───────────────────────────────────
write(".github/workflows/build.yml", '''name: Build Lumio APK

on:
  push:
    branches: [ main ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: \'17\'
          distribution: \'temurin\'
          cache: gradle

      - name: Make gradlew executable
        run: chmod +x gradlew

      - name: Build Debug APK
        run: ./gradlew assembleDebug --stacktrace

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: Lumio-Debug-APK
          path: app/build/outputs/apk/debug/app-debug.apk
          retention-days: 7
''')

print("")
print("=" * 50)
print("ALL FILES CREATED SUCCESSFULLY!")
print("=" * 50)
print(f"Total files created.")
print("Now run: git add . && git commit -m 'Phase 1' && git push")

