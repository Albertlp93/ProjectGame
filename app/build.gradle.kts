plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.projectgame.projectgame"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.projectgame.projectgame"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    
    //RXJava
    implementation ("io.reactivex.rxjava3:rxjava:3.1.9")
    implementation ("io.reactivex.rxjava3:rxandroid:3.0.2")
    //libreria de animacion
    implementation ("com.airbnb.android:lottie:6.6.0")

    dependencies {
        implementation ("androidx.appcompat:appcompat:1.6.1") // Asegúrate de que la versión sea la correcta
        implementation ("androidx.core:core-ktx:1.12.0") // Esto es opcional, pero puede ser útil

        // Otras dependencias...
    }



    implementation (libs.android.lottie)
    implementation(libs.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}