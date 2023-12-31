import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.util.Properties

plugins {
    id("com.android.application")
}



android {

    namespace = "com.example.asmt4"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.asmt4"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val propertiesFile = rootProject.file("local.properties")
        val properties = gradleLocalProperties(propertiesFile)
        buildConfigField("String", "API_KEY_1", "\"${properties.getProperty("API_KEY_1")}\"")
        buildConfigField("String", "API_KEY_2", "\"${properties.getProperty("API_KEY_2")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation ("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    implementation ("com.google.api-client:google-api-client-android:1.23.0")
    implementation ("com.google.http-client:google-http-client-gson:1.23.0")
    implementation ("com.google.apis:google-api-services-vision:v1-rev369-1.23.0")

    implementation("com.android.volley:volley:1.2.1")


    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}