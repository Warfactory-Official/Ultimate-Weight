pluginManagement {
    repositories {
        maven { url = uri("https://maven.leclowndu93150.dev/releases") }
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://maven.fabricmc.net/") }
        maven { url = uri("https://maven.neoforged.net/releases") }
        maven { url = uri("https://repo.spongepowered.org/repository/maven-public/") }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.prism.settings") version "+"
}

rootProject.name = "warfactory-ultimate-weight"

// Standalone compile-time annotation processor that indexes @CompatPlugin classes. Plain Java
// subproject (not a Minecraft module), so it is included directly rather than through prism.
include("compat-processor")

prism {
    sharedCommon("shared")
    version("1.20.1") {
        common()
        fabric()
        forge()
    }
    version("1.21.1") {
        common()
        neoforge()
        lexForge()
        fabric()
    }
    // MC 26.1 (NeoForge + Fabric) port is scaffolded under versions/26.1 but not yet wired into the
    // build: NeoForge is blocked by prism 0.5.15 (injects shared shadow libs via MDG's removed
    // `additionalRuntimeClasspath`), and Fabric still needs the 26.1 API migration (ClickType removal,
    // GuiGraphics -> render-state extraction). Re-enable once those are resolved.
//    version("26.1") {
//        common()
//        neoforge()
//        fabric()
//    }
    version("1.12.2") {
        legacyForge()
    }
}
