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

// fat jar build task
tasks.register<Jar>("fatJar") {
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

    tasks.assemble {
        dependsOn(this)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

}

kotlin {
    jvmToolchain(17)
}
