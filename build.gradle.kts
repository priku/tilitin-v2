import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.1.0"
    id("org.jetbrains.compose") version "1.7.3"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
}

group = "fi.priku"
version = "2.2.5"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    // Compose Desktop
    implementation(compose.desktop.currentOs)
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material)
    implementation(compose.ui)
    
    // Kotlin
    implementation(kotlin("stdlib"))
    
    // Database drivers
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    implementation("com.mysql:mysql-connector-j:9.2.0")
    implementation("org.postgresql:postgresql:42.7.4")
    
    // PDF generation
    implementation("com.itextpdf:itextpdf:5.5.13.4")
    implementation("org.apache.pdfbox:pdfbox:3.0.3")
    
    // CSV handling
    implementation("com.opencsv:opencsv:5.9")
    
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.slf4j:slf4j-simple:2.0.16")
    
    // Modern Look and Feel (for Swing interop)
    implementation("com.formdev:flatlaf:3.5.4")
    implementation("com.formdev:flatlaf-extras:3.5.4")
    implementation("com.formdev:flatlaf-intellij-themes:3.5.4")
}

sourceSets {
    main {
        java {
            srcDirs("src/main/java")
        }
        kotlin {
            srcDirs("src/main/kotlin", "src/main/java")
        }
        resources {
            srcDirs("src/main/resources")
        }
    }
}

// Handle duplicate resources from Java source directory
tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    from("src/main/java") {
        include("**/*.png", "**/*.ico", "**/*.gif")
    }
}

compose.desktop {
    application {
        mainClass = "kirjanpito.ui.Kirjanpito"
        
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Tilitin"
            packageVersion = "2.2.5"
            description = "Ilmainen suomalainen kirjanpito-ohjelma"
            vendor = "Tilitin"
            
            windows {
                menuGroup = "Tilitin"
                upgradeUuid = "18159785-d967-4CD2-8885-77BFA97CFA9F"
                iconFile.set(project.file("src/main/java/kirjanpito/ui/resources/tilitin-icon.ico"))
            }
            
            linux {
                iconFile.set(project.file("src/main/java/kirjanpito/ui/resources/tilitin-icon.png"))
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

// Configure JAR task with Main-Class manifest
tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "kirjanpito.ui.Kirjanpito",
            "Implementation-Title" to "Tilitin",
            "Implementation-Version" to project.version
        )
    }
    
    // Include all dependencies in JAR (fat JAR)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

// Copy LISENSSIT.html to build
tasks.processResources {
    from(projectDir) {
        include("LISENSSIT.html")
    }
}
