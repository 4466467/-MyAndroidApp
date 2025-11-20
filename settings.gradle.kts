import org.gradle.kotlin.dsl.maven

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}


dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // 高德地图仓库 - 正确URL
        //maven {
            //url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        //}
        maven { url = uri("https://oss.sonatype.org/content/repositories/releases/") }
        // 阿里云Maven镜像 - 正确URL
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
        }
        // 或者使用阿里云新的仓库地址
        maven {
            url = uri("https://maven.aliyun.com/repository/google")
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/central")
        }
    }
}

rootProject.name = "OurBookApplication"
include(":app")
 