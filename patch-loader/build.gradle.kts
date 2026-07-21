plugins { id("com.android.library") }
android {
    namespace = "org.lsposed.lspatch.loader"
    compileSdk = 35
    defaultConfig { minSdk = 26; targetSdk = 35 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_21; targetCompatibility = JavaVersion.VERSION_21 }
}
dependencies { implementation("androidx.annotation:annotation:1.8.2") }
