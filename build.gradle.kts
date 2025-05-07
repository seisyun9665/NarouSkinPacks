import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val spigot_version = "1.12.2-R0.1-SNAPSHOT"

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20")
    }
}

plugins {
    java
    kotlin("jvm") version "1.9.20"
    id("com.diffplug.spotless") version "6.21.0" // 最新バージョンに更新
    idea
}

// IDEAプロジェクト設定
idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

group = "org.com.syun0521.minecraft"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        name = "spigotmc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://jitpack.io") {
        name = "jitpack"
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:$spigot_version")
    implementation("junit:junit:4.13.1") // JUnitを最新バージョンに更新
    implementation("org.yaml:snakeyaml:2.2")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.json:json:20231013")
    implementation("com.google.guava:guava:32.1.2-jre")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.20")
    implementation("io.ktor:ktor-server-core:2.3.7")
    implementation("io.ktor:ktor-server-cio:2.3.7")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    implementation("io.netty:netty-codec-http:4.1.118.Final") // 安全なバージョンに更新
    implementation("io.netty:netty-handler:4.1.118.Final") // 脆弱性修正のためアップグレード
    // ロギング用
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.5.13")
}

val targetJavaVersion = 8
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.jar {
    archiveBaseName.set("NarouSkinPacks")
    archiveVersion.set("")
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

spotless {
    kotlin {
        target("src/**/*.kt")
        ktlint("0.50.0").userData(mapOf("indent_size" to "4", "continuation_indent_size" to "4")) // 最新バージョンに更新
    }
}

tasks.named("build") {
    dependsOn("spotlessApply")
}