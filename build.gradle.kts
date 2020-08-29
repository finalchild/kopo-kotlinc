plugins {
    `java-library`
    kotlin("jvm") version "1.4.0"
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
            from(components["kotlin"])
            pom {
                description.set(project.description)
                inceptionYear.set("2020")
                url.set("https://github.com/finalchild/kopo")


            }
        }
    }
    repositories {
        maven {
            name = "heartpattern-repo"
            url = if (project.version.toString().endsWith("-SNAPSHOT")) {
                uri("https://maven.heartpattern.io/repository/finalchild-snapshots/")
            } else {
                uri("https://maven.heartpattern.io/repository/finalchild-releases/")
            }
            credentials {
                username = project.findProperty("repo-username") as? String ?: System.getenv("REPO_USERNAME")
                password = project.findProperty("repo-password") as? String? ?: System.getenv("REPO_PASSWORD")
            }
        }
    }
}
