plugins {
    kotlin("jvm") version "2.2.0"
    id("org.openjfx.javafxplugin") version "0.1.0"
    application
}

group = "fi.priku"
version = "2.1.1"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}

// JavaFX configuration
javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.swing", "javafx.web")
}

// Detect current OS for JavaFX dependencies
val currentOS = org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem()
val javafxPlatform = when {
    currentOS.isWindows -> "win"
    currentOS.isMacOsX -> "mac"
    else -> "linux"
}

repositories {
    mavenCentral()
}

dependencies {
    // JavaFX dependencies
    implementation("org.openjfx:javafx-controls:21:$javafxPlatform")
    implementation("org.openjfx:javafx-fxml:21:$javafxPlatform")
    implementation("org.openjfx:javafx-swing:21:$javafxPlatform")
    implementation("org.openjfx:javafx-base:21:$javafxPlatform")
    implementation("org.openjfx:javafx-graphics:21:$javafxPlatform")
    implementation("org.openjfx:javafx-web:21:$javafxPlatform")
    
    // Kotlin
    implementation(kotlin("stdlib"))
    
    // Database drivers
    implementation("org.xerial:sqlite-jdbc:3.49.1.0")
    implementation("com.mysql:mysql-connector-j:9.3.0")
    implementation("org.postgresql:postgresql:42.7.7")
    
    // PDF generation
    implementation("org.apache.pdfbox:pdfbox:3.0.6")
    
    // CSV handling
    implementation("com.opencsv:opencsv:5.10")
    
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.slf4j:slf4j-simple:2.0.16")
    
    // Modern Look and Feel (for Swing interop)
    implementation("com.formdev:flatlaf:3.6")
    implementation("com.formdev:flatlaf-extras:3.6")
    implementation("com.formdev:flatlaf-intellij-themes:3.6")
    
    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
    testImplementation("org.testfx:testfx-core:4.0.18")
    testImplementation("org.testfx:testfx-junit5:4.0.18")
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
    test {
        java {
            srcDirs("src/test/java")
        }
        kotlin {
            srcDirs("src/test/kotlin")
        }
        resources {
            srcDirs("src/test/resources")
        }
    }
}

// Main application configuration - JavaFX version
application {
    mainClass.set("kirjanpito.ui.javafx.JavaFXApp")
    applicationDefaultJvmArgs = listOf(
        "-Xmx2048m",
        "-Xms512m"
    )
}

// Handle duplicate resources from Java source directory
tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    from("src/main/java") {
        include("**/*.png", "**/*.ico", "**/*.gif")
    }
    from(projectDir) {
        include("LISENSSIT.html")
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
            "Main-Class" to "kirjanpito.ui.javafx.JavaFXApp",
            "Implementation-Title" to "Tilitin",
            "Implementation-Version" to project.version
        )
    }
}

// Configure test task
tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

// Configure run task with JavaFX module path
tasks.named<JavaExec>("run") {
    doFirst {
        val javafxJars = classpath.files.filter { it.name.contains("javafx") }
        val modulePath = javafxJars.joinToString(File.pathSeparator) { it.absolutePath }
        jvmArgs = listOf(
            "-Xmx1024m",
            "--module-path", modulePath,
            "--add-modules", "javafx.controls,javafx.fxml,javafx.swing"
        )
    }
}

// Task to run JavaFX test application
tasks.register<JavaExec>("runTest") {
    group = "application"
    description = "Run JavaFX test application"
    mainClass.set("kirjanpito.ui.javafx.JavaFXTest")
    classpath = sourceSets["main"].runtimeClasspath
    
    doFirst {
        val javafxJars = classpath.files.filter { it.name.contains("javafx") }
        val modulePath = javafxJars.joinToString(File.pathSeparator) { it.absolutePath }
        jvmArgs = listOf(
            "-Xmx512m",
            "--module-path", modulePath,
            "--add-modules", "javafx.controls,javafx.fxml,javafx.swing"
        )
    }
}

// Task to run old Swing application (legacy)
tasks.register<JavaExec>("runSwing") {
    group = "application"
    description = "Run Tilitin Swing application (legacy)"
    mainClass.set("kirjanpito.ui.Kirjanpito")
    classpath = sourceSets["main"].runtimeClasspath
    jvmArgs = listOf("-Xmx1024m", "-Xms256m")
}

// Fat JAR task for distribution
tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Creates a fat JAR with all dependencies"
    archiveClassifier.set("all")
    
    manifest {
        attributes(
            "Main-Class" to "kirjanpito.ui.javafx.JavaFXApp",
            "Implementation-Title" to "Tilitin",
            "Implementation-Version" to project.version
        )
    }
    
    from(sourceSets.main.get().output)
    
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    }) {
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}
