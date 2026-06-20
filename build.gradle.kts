plugins {
    id("dev.prism")
}

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.gtnewhorizons.retrofuturagradle.mcp.ReobfuscatedJar
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService

group = "warfactory.ultimateweight"
version = providers.gradleProperty("modVersion").orElse("1.0.0").get()

prism {
        curseMaven()
        modrinthMaven()
        maven("WarfactoryMaven", "https://repo.warfactory.co/releases")
        maven("CleanroomMaven", "https://maven.cleanroommc.com")
        maven("GTCEu Maven", "https://maven.gtceu.com")
        maven("GTNH Nexus", "https://nexus.gtnewhorizons.com/repository/public/")
    maven("BlameJared", "https://maven.blamejared.com/")


    metadata {
        modId = "wfweight"
        name = "Warfactory: Ultimate Weight"
        description = "An inventory weight mod that doesn't suck"
        license = "GPLv3"
        author("MrNorwood")
    }

    sharedCommon {
        dependencies {
            api("org.yaml:snakeyaml:2.6")
            api("it.unimi.dsi:fastutil:8.5.9")
            api("com.github.ben-manes.caffeine:caffeine:2.9.3")
            shadowRelocation(true)
            shadow("org.yaml:snakeyaml:2.6")
            shadow("it.unimi.dsi:fastutil:8.5.9")
            shadow("com.github.ben-manes.caffeine:caffeine:2.9.3")
        }
    }

    // Multi-loader: shared common + fabric + forge
    version("1.20.1") {
        fabric {
            loaderVersion = "0.18.6"
            fabricApi("0.92.7+1.20.1")
        }
        forge {
            loaderVersion = "47.4.18"
            dependencies {
                modRuntimeOnly("curse.maven:jei-238222:7920915")
                modCompileOnly("curse.maven:kubejs-238086:5853326")
                modRuntimeOnly("curse.maven:kubejs-238086:5853326")
                modRuntimeOnly("curse.maven:storage-drawers-223852:6994481")
                modCompileOnly("curse.maven:rhino-416294:6186971")
                modRuntimeOnly("curse.maven:rhino-416294:6186971")
                modCompileOnly("curse.maven:architectury-api-419699:5137938")
                modRuntimeOnly("curse.maven:architectury-api-419699:5137938")
                modRuntimeOnly("curse.maven:sophisticated-backpacks-422301:7916619")
                modRuntimeOnly("curse.maven:sophisticated-core-618298:7916595")
                modCompileOnly("curse.maven:travelers-backpack-321117:7816782")
                modRuntimeOnly("curse.maven:travelers-backpack-321117:7816782")
            }
        }
    }

    // Multi-loader: neoforge + lexforge (MinecraftForge via ForgeGradle 7)
    version("1.21.1") {
        neoforge {
            loaderVersion = "21.1.222"
            dependencies {

                modCompileOnly("curse.maven:travelers-backpack-321117:8120689")
                modRuntimeOnly("curse.maven:travelers-backpack-321117:8120689")

                modCompileOnly("curse.maven:sophisticated-backpacks-422301:8272377")
                modRuntimeOnly("curse.maven:sophisticated-backpacks-422301:8272377")

                modCompileOnly("curse.maven:storage-drawers-223852:6995432")
                modRuntimeOnly("curse.maven:storage-drawers-223852:6995432")


                modCompileOnly("curse.maven:kubejs-238086:8083208")
                modRuntimeOnly("curse.maven:kubejs-238086:8083208")

            }

        }
        lexForge {
            loaderVersion = "52.1.9"
        }
        fabric {
           loaderVersion = "0.16.14"
            fabricApi("0.116.11+1.21.1")
        }
    }

    // Single-loader: just fabric
//    version("26.1") {
//        fabric {
//            loaderVersion = "0.18.6"
//            fabricApi("0.145.2+26.1.1")
//        }
//    }

    // Legacy Forge 1.12.2
    version("1.12.2") {
        legacyForge {
            coreMod("com.warfactory.ultimateweight.mixin.UltimateWeightLoadingPlugin")
            mcVersion = "1.12.2"
            forgeVersion = "14.23.5.2847"
            mappingChannel = "stable"
            mappingVersion = "39"
            username = "Developer"
            dependencies {
                modCompileOnly("curse.maven:baubles-227083:2518667")
                modCompileOnly("curse.maven:travelers-backpack-321117:3150850")
                modCompileOnly("curse.maven:retro-sophisticated-backpacks-1197465:7589941")
                modRuntimeOnly("curse.maven:baubles-227083:2518667")
                annotationProcessor ("org.ow2.asm:asm-debug-all:5.2")
                annotationProcessor ("com.google.guava:guava:32.1.2-jre")
                annotationProcessor ("com.google.code.gson:gson:2.8.9")
                annotationProcessor ("zone.rong:mixinbooter:10.7")
                modImplementation("curse.maven:mixin-booter-419286:7049694");
//                modRuntimeOnly("curse.maven:had-enough-items-557549:7899997")
                modCompileOnly("curse.maven:groovyscript-687577:7925117")
                modRuntimeOnly("curse.maven:groovyscript-687577:7925117")
                modCompileOnly("curse.maven:retro-sophisticated-backpacks-1197465:7589941")
                modRuntimeOnly("curse.maven:retro-sophisticated-backpacks-1197465:7589941")
                modRuntimeOnly("curse.maven:travelers-backpack-321117:3150850")
                modCompileOnly("curse.maven:travelers-backpack-321117:3150850")
                modRuntimeOnly("com.cleanroommc:modularui:3.0.6")
                modRuntimeOnly("io.github.chaosunity.forgelin:Forgelin-Continuous:2.3.20.0")


//                modRuntimeOnly("curse.maven:ctm-267602:2915363")
//                modRuntimeOnly("curse.maven:hbm-nuclear-tech-mod-community-edition-1312314:7685718")
                modRuntimeOnly("curse.maven:storage-drawers-223852:5981297")
                modRuntimeOnly("curse.maven:chameleon-230497:2450900")
//                modRuntimeOnly("codechicken:codechickenlib:3.2.3.358")
//                modRuntimeOnly("gregtech:gregtech:2.8.10-beta")
            }
        }
    }
}

//Dont know why it kept pulling those
project(":common") {
    plugins.withType<JavaPlugin> {
        extensions.configure<JavaPluginExtension> {
            toolchain.languageVersion.set(JavaLanguageVersion.of(25))
        }

        val toolchains = extensions.getByType<JavaToolchainService>()
        tasks.withType<JavaCompile>().configureEach {
            javaCompiler.set(toolchains.compilerFor {
                languageVersion.set(JavaLanguageVersion.of(25))
            })
        }
    }

    configurations.configureEach {
        exclude(group = "com.mojang", module = "logging")
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
}

project(":1.20.1:forge") {
    val forgeRefmap = layout.buildDirectory.file("mixin/wfweight.refmap.json")
    val clientRunProgramArgs = layout.buildDirectory.file("moddev/clientRunProgramArgs.txt")

    val sanitizeClientRunArgs = tasks.register("sanitizeClientRunArgs") {
        dependsOn("prepareClientRun")

        inputs.file(clientRunProgramArgs)
        outputs.file(clientRunProgramArgs)

        doLast {
            val file = clientRunProgramArgs.get().asFile
            if (!file.exists()) {
                return@doLast
            }

            val sanitized = file.readLines().filterNot { line ->
                line == "--mixin.config" || line == "wfweight.mixins.json"
            }
            file.writeText(sanitized.joinToString(System.lineSeparator()) + System.lineSeparator())
        }
    }

    tasks.withType<ProcessResources>().configureEach {
        dependsOn("compileJava")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(forgeRefmap)
    }

    tasks.withType<Jar>().matching { it.name == "jar" }.configureEach {
        dependsOn("compileJava")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(forgeRefmap)
    }

    tasks.withType<ShadowJar>().configureEach {
        dependsOn("compileJava")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(forgeRefmap)
        exclude("META-INF/jandex.idx")
    }

    tasks.matching { it.name == "runClient" }.configureEach {
        dependsOn(sanitizeClientRunArgs)
    }
}

// Wire the @CompatPlugin annotation processor into every module that hosts plugin classes, so each
// jar carries its own META-INF/wfweight/compat-plugins.txt index that WeightCompatBootstrap reads.
listOf(":1.12.2", ":1.20.1:common", ":1.20.1:forge", ":1.20.1:fabric", ":1.21.1:common", ":1.21.1:neoforge", ":1.21.1:lexforge", ":1.21.1:fabric").forEach { path ->
    project(path) {
        plugins.withType<JavaPlugin> {
            dependencies {
                add("annotationProcessor", project(":compat-processor"))
            }
        }
    }
}

// Prism wires shadow relocation into the reobf step only for moddev-based loaders. The 1.12.2 build
// uses RetroFuturaGradle, whose `reobfJar` reobfuscates the thin `jar` task and never sees the
// `shadowJar` - so the relocated snakeyaml/fastutil/caffeine never reached the published jar (it was
// just the mod classes, which would NoClassDefFoundError at runtime). Reobfuscate the already
// relocated `shadowJar` output instead, so the runtime jar bundles the shaded libraries.
project(":1.12.2") {
    plugins.withId("com.gtnewhorizons.retrofuturagradle") {
        tasks.named<ReobfuscatedJar>("reobfJar") {
            inputJar.set(tasks.named<Jar>("shadowJar").flatMap { it.archiveFile })
        }
    }
}

// Prism auto-adds the SpongePowered Mixin annotation processor to the LexForge module, where it runs
// in reobfuscation mode and fails ("Unable to locate obfuscation mapping for @Inject target ...")
// because Forge 1.21.1 - like NeoForge - runs on official Mojang names, so no refmap is needed. The
// shared 1.21.x mixins compile and resolve by official names exactly as they do on NeoForge (which
// carries no refmap either), so drop the AP here to let the common mixins compile.
project(":1.21.1:lexforge") {
    plugins.withType<JavaPlugin> {
        configurations.matching { it.name == "annotationProcessor" }.configureEach {
            exclude(group = "org.spongepowered", module = "mixin")
        }
    }
}
