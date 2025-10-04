plugins {
    id("java")
    kotlin("jvm")
    application
}

group = "viliusvv"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}


application {
    // Replace with your package + main class if needed
    mainClass.set("viliusvv.MainKt")
}

dependencies {


    implementation(kotlin("stdlib-jdk8"))

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}