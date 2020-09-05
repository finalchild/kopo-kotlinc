plugins {
    kotlin("jvm") version "1.4.0"
    `java-library`
    `maven-publish`
    signing
    id("com.jfrog.bintray") version "1.8.5"
}

group = "me.finalchild"
version = "0.1.0-SNAPSHOT"
description = "kotlinc plugin for kopo"

repositories {
    mavenCentral()
}

dependencies {
    api(group="org.jetbrains.kotlin", name="kotlin-compiler-embeddable")
}

java {
    sourceCompatibility = JavaVersion.VERSION_14
    targetCompatibility = JavaVersion.VERSION_14
    modularity.inferModulePath.set(true)
    withSourcesJar()
}

tasks.compileJava {
    dependsOn(tasks.compileKotlin)
    inputs.property("moduleName", "me.finalchild.kopo.kotlinc")
    options.encoding = "UTF-8"
    destinationDir = tasks.compileKotlin.get().destinationDir
    doFirst {
        options.compilerArgs = listOf("--module-path", classpath.asPath)
        classpath = files()
    }
}

tasks.compileKotlin {
    kotlinOptions {
        freeCompilerArgs = listOf("-progressive")
        jvmTarget = "14"
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
            artifact(tasks["sourcesJar"])

            pom {
                name.set(project.name)
                description.set(project.description)
                url.set("https://github.com/finalchild/kopo-kotlinc")
                inceptionYear.set("2020")
                organization {
                    name.set("Our Minecraft Space")
                    url.set("https://ourmc.space/")
                }
                licenses {
                    license {
                        name.set("ISC License")
                        url.set("https://opensource.org/licenses/isc-license.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("finalchild")
                        name.set("Final Child")
                        email.set("finalchild2@gmail.com")
                        url.set("https://github.com/finalchild")
                        organization.set("Our Minecraft Space")
                        organizationUrl.set("https://github.com/Our-Minecraft-Space")
                        roles.set(setOf("developer"))
                        timezone.set("Asia/Seoul")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/finalchild/kopo-kotlinc.git")
                    developerConnection.set("scm:git:https://github.com/finalchild/kopo-kotlinc.git")
                    tag.set(if (!(project.version as String).endsWith("-SNAPSHOT")) {
                        "v${project.version}"
                    } else {
                        "HEAD"
                    })
                    url.set("https://github.com/finalchild/kopo-kotlinc")
                }
                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/finalchild/kopo-kotlinc/issues")
                }
                ciManagement {
                    system.set("GitHub Actions")
                    url.set("https://github.com/finalchild/kopo-kotlinc/actions")
                }
                distributionManagement {
                    downloadUrl.set("https://github.com/fnialchild/kopo-kotlinc/releases")
                }
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(System.getenv("SIGNING_KEY"), System.getenv("SIGNING_PASSWORD"))
    sign(publishing.publications["maven"])
}

bintray {
    user = System.getenv("BINTRAY_USER")
    key = System.getenv("BINTRAY_KEY")
    setPublications("maven")
    publish = true
    pkg.run {
        repo = "Final_Repo"
        name = project.name
        version.run {
            name = project.version as String
            vcsTag = if (!(project.version as String).endsWith("-SNAPSHOT")) {
                "v${project.version}"
            } else {
                null
            }
        }
    }
}
