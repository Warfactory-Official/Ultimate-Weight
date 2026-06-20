plugins {
    `java-library`
}

// The processor must load under javac 8 (RetroFuturaGradle 1.12.2 build) as well as the modern
// toolchains, so compile it to Java 8 bytecode. `--release 8` works on every JDK that runs Gradle.
tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
    options.encoding = "UTF-8"
    // This module is the processor; it has none of its own.
    options.compilerArgs.add("-proc:none")
}
