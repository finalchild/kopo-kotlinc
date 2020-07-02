plugins {
    kotlin("jvm") version "1.3.72"
}

group = "me.finalchild"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly(group="org.jetbrains.kotlin", name="kotlin-compiler")
}
