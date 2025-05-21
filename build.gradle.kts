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
    id("com.github.johnrengelman.shadow") version "8.1.1" // Gradle 8.8と互換性のある最新バージョン
    id("com.diffplug.spotless") version "6.21.0"
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
    
    // 基本的な依存関係
    implementation("org.yaml:snakeyaml:2.2")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.json:json:20231013")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.20")
    
    // Ktorの依存関係 - シャドウJARにパッケージングされる
    implementation("io.ktor:ktor-server-core:2.3.9")
    implementation("io.ktor:ktor-server-netty:2.3.9")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.9")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.9")
    
    // Netty依存関係 - 直接参照ではなくKtorを通じて使用
    // (Ktorが内部的に使用する)
    
    // ロギング
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.11") // Java 8互換バージョン
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

    // すべての依存関係をJARに含める
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // META-INF内の競合ファイルを除外
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/MANIFEST.MF")

    // Jar完了時のメッセージ
    doLast {
        println("JAR作成が完了しました。このバージョンではNetty/Ktorのリロケーションは行われていませんが、テスト用途には十分かもしれません。")
        println("本番環境では、適切なリロケーションを行うようにしてください。")
    }
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

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    relocate("io.netty", "org.com.syun0521.minecraft.narouskinpacks.shaded.io.netty")
    relocate("kotlin", "org.com.syun0521.minecraft.narouskinpacks.shaded.kotlin")
    relocate("io.ktor", "org.com.syun0521.minecraft.narouskinpacks.shaded.io.ktor")
    relocate("org.slf4j", "org.com.syun0521.minecraft.narouskinpacks.shaded.org.slf4j")
    relocate("ch.qos.logback", "org.com.syun0521.minecraft.narouskinpacks.shaded.ch.qos.logback")
    relocate("kotlinx", "org.com.syun0521.minecraft.narouskinpacks.shaded.kotlinx")
    
    // META-INF内の競合ファイルを除外
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/MANIFEST.MF")

    archiveBaseName.set("NarouSkinPacks")   // 既存の jar と合わせる
    archiveClassifier.set("")               // "-all" を付けたくない場合
    archiveVersion.set("")

    // 重複ファイルの処理
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}