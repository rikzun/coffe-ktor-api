plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.30" // для поддержки котлина
    application // плагин добавляющий возможность создания консольных приложений
}

repositories {
    mavenCentral() // база библиотек, наподобие npm
    google()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom")) // указывает другим зависимостям под какую версию котлина всё устанавливать
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")   // стандартная библиотека kotlin
    implementation("com.google.guava:guava:29.0-jre")           // дополнительная библиотека для проекта
    
    implementation("io.ktor:ktor-server-core:1.6.3")
    implementation("io.ktor:ktor-server-netty:1.6.3")
    implementation("io.ktor:ktor-gson:1.6.3")
    implementation("ch.qos.logback:logback-classic:1.2.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")

    implementation("com.google.firebase:firebase-admin:8.0.1")
}

application {
    mainClass.set("kt.AppKt") // главный класс приложения
}