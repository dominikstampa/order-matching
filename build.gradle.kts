plugins {
    kotlin("jvm") version "1.9.0"
    `java-library`
    `maven-publish`
}

group = "de.ordermatching"
version = "0.3.1"

repositories {
    maven {
        url = uri("https://nexus.terrestris.de/repository/public/")
    }
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.8")
    implementation("org.geotools:gt-main:29.1")
    implementation("org.locationtech.jts:jts-core:1.19.0")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("ch.qos.logback:logback-classic:1.4.11")
}

tasks.test {
    useJUnitPlatform()
    minHeapSize = "1024m"
    maxHeapSize = "2048m"
//    jvmArgs = listOf("-XX:MaxPermSize=2048m")
}


kotlin {
    jvmToolchain(17)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "de.ordermatching"
            artifactId = "order-matching"
            version = "0.3.1"

            from(components["java"])
        }
    }
}