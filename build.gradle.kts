plugins {
    `java-library`
}

group = "io.github.bytosphere"
version = "1.0.1"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.46")
    annotationProcessor("org.projectlombok:lombok:1.18.46")
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    archiveFileName.set("transcriptor-v${version}.jar")
    manifest {
        attributes(
            "Implementation-Title" to "Transcriptor",
            "Implementation-Version" to version,
            "Implementation-Vendor" to "Bytosphere",
            "License" to "MIT"
        )
    }
}