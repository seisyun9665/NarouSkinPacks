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
    id("com.diffplug.spotless") version "6.0.0"
    // IDEプロジェクトファイル生成用プラグイン
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.json:json:20210307")
    
    // BukkitKotlinライブラリの代わりに、Kotlin拡張機能を直接使用
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.20")
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
        ktlint("0.41.0").userData(mapOf("indent_size" to "4", "continuation_indent_size" to "4"))
    }
}

tasks.named("build") {
    dependsOn("spotlessApply")
}