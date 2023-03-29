import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.10"
    application
}

group = "me.marvuun"
version = "0.0.1"

application {
    mainClass.set("me.marvuun.MainKt")
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("me.jakejmattson:DiscordKt:0.23.4")
    implementation("dev.kord:kord-core:0.8.0")

    implementation("org.jetbrains.exposed:exposed-core:0.40.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.40.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.40.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    implementation("org.mariadb.jdbc:mariadb-java-client:3.1.2")

    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.8.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
