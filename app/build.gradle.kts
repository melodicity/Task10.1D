import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.task101d"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.task101d"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Load properties from local.properties file
        val localProperties = Properties()
        localProperties.load(project.rootProject.file("local.properties").inputStream())

        // Retrieve STRIPE_KEY properties from local.properties
        val publishableKey: String? = localProperties.getProperty("STRIPE_PUBLISHABLE_KEY")
        val secretKey: String? = localProperties.getProperty("STRIPE_SECRET_KEY")

        // Define buildConfig fields with the retrieved keys
        buildConfigField("String", "STRIPE_PUBLISHABLE_KEY", "\"$publishableKey\"")
        buildConfigField("String", "STRIPE_SECRET_KEY", "\"$secretKey\"")
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
    buildFeatures {
        viewBinding = true

        // Enable the buildConfig feature
        buildConfig = true
    }
}

val retrofitVersion = "2.11.0"
dependencies {

    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation("com.stripe:stripe-android:20.11.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}