import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}

dependencies {
    implementation("com.formdev:flatlaf:3.6.1")


    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

val fatJarTask = tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Assembles a fat jar archive containing the main classes and their dependencies."

    archiveClassifier.set("all")

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    val sourcesMain = sourceSets.main.get()
    val contents = configurations.runtimeClasspath.get()
        .map {
            if (it.isDirectory) it else zipTree(it)
        } + sourcesMain.output

    from(contents)

    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
}

tasks.assemble {
    finalizedBy(fatJarTask)
}

tasks.test {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}
