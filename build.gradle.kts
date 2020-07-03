plugins {
    `java-library`
    kotlin("jvm") version "1.3.72"
    `maven-publish`
}

group = "me.finalchild"
version = "0.0.1-SNAPSHOT"

description = "Kotlin object is plugin object."

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(group="org.jetbrains.kotlin", name="kotlin-compiler-embeddable")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

java {
    sourceCompatibility = JavaVersion.VERSION_14
    targetCompatibility = JavaVersion.VERSION_14
    modularity.inferModulePath.set(true)
    withSourcesJar()
}
tasks.compileJava {
    inputs.property("moduleName", "me.finalchild.kopo")
    doFirst {
        options.compilerArgs = listOf("--module-path", classpath.asPath)
        classpath = files()
    }
    destinationDir = tasks.compileKotlin.get().destinationDir
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = listOf("-progressive")
        jvmTarget = "13"
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "me.finalchild"
            artifactId = "kopo"
            version = "0.0.1-SNAPSHOT"

            from(components["kotlin"])
        }
    }
    repositories {
        mavenLocal()
    }
}
