plugins { id("java-library"); id("application") }
java { sourceCompatibility = JavaVersion.VERSION_21; targetCompatibility = JavaVersion.VERSION_21 }
application { mainClass.set("org.lsposed.lspatch.jar.Main") }
tasks.jar {
    manifest { attributes["Main-Class"] = "org.lsposed.lspatch.jar.Main" }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
dependencies {
    implementation("com.github.iBotPeaches:apktool-lib:2.9.3")
}
