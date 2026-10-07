plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    // groovy.json.JsonSlurper for reading the SDK option manifests
    implementation(localGroovy())

    testImplementation(kotlin("test-junit5"))
}

tasks.test {
    useJUnitPlatform()
}
