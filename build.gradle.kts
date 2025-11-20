// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.0")
    }
}

// 所有子项目的通用配置
allprojects {
    repositories {
        //google()
      //  mavenCentral()
        // 阿里云镜像仓库
       //maven { url = uri("https://maven.aliyun.com/repository/public/") }
        // 高德地图官方仓库
       //maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
       //maven { url = uri("https://oss.sonatype.org/content/repositories/releases/") }
    }
}

// 任务清理配置
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}