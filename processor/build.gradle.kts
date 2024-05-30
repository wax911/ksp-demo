plugins {
    kotlin("jvm") version ("1.9.23")
    id("com.google.devtools.ksp") version "1.9.23-1.0.20"
}

group = "io.wax911"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":annotation"))
    implementation("com.squareup:kotlinpoet:1.17.0")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.9.23-1.0.20")

    testImplementation(kotlin("test"))

    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.6.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}