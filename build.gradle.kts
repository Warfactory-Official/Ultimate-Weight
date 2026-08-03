plugins {
    id("dev.prism")
}

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.gtnewhorizons.retrofuturagradle.mcp.GenSrgMappingsTask
import com.gtnewhorizons.retrofuturagradle.mcp.ReobfuscatedJar
import com.gtnewhorizons.retrofuturagradle.minecraft.RunMinecraftTask
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
        // Parchment parameter-name + javadoc mappings layered onto Mojmaps (all loaders, via prism).
        parchmentMinecraftVersion = "1.20.1"
        parchmentMappingsVersion = "2023.09.03"
        fabric {
            loaderVersion = "0.18.6"
            fabricApi("0.92.7+1.20.1")
            // Second client (runClient2) is wired via Loom directly in the project(":1.20.1:fabric")
            // block below - prism 0.5.15's `runs {}` bridge crashes on the Fabric path.
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
                modCompileOnly("curse.maven:timeless-and-classics-zero-1028108:8141310")
                // AE2: read storage-cell contents so a cell full of weighted items weighs its contents
                // (Ae2StorageCellWeightPatch1201). Compile-only; the pack provides AE2 at runtime and the
                // @CompatPlugin(requiredMods="ae2") gate keeps the plugin dormant when AE2 is absent.
                modCompileOnly("curse.maven:applied-energistics-2-223794:7148487")
            }
            // Second client (runClient2) is wired via ModDevGradle directly in the
            // project(":1.20.1:forge") block below - prism 0.5.15's `runs {}` bridge is broken.
        }
    }

    // Multi-loader: neoforge + lexforge (MinecraftForge via ForgeGradle 7)
    version("1.21.1") {
        // Parchment parameter-name + javadoc mappings layered onto Mojmaps (all loaders, via prism).
        parchmentMinecraftVersion = "1.21.1"
        parchmentMappingsVersion = "2024.11.17"
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

            // Second client (runClient2) is wired via ModDevGradle directly in the
            // project(":1.21.1:neoforge") block below - prism 0.5.15's `runs {}` bridge is broken.
        }
        lexForge {
            loaderVersion = "52.1.9"
            // Second client (runClient2) is wired via ForgeGradle directly in the
            // project(":1.21.1:lexforge") block below - prism 0.5.15's `runs {}` bridge is broken.
        }
        fabric {
           loaderVersion = "0.16.14"
            fabricApi("0.116.11+1.21.1")
            // Second client (runClient2) is wired via Loom directly in the project(":1.21.1:fabric")
            // block below - prism 0.5.15's `runs {}` bridge crashes on the Fabric path.
        }
    }

    // Minecraft 26.1 - NeoForge + Fabric (ported from the 1.21.1 DataComponents implementation).
    // Optional mod integrations (Sophisticated/Traveler's Backpacks, Storage Drawers, KubeJS) are not
    // wired yet because those mods have no 26.1 builds; the generic item-handler / container support
    // and the core weight + stamina systems are.
    // MC 26.1 port (versions/26.1) is scaffolded from the 1.21.1 DataComponents implementation but not
    // yet wired in - see the note in settings.gradle.kts. Intended config once unblocked:
//    version("26.1") {
//        neoforge {
//            loaderVersion = "26.1.1.0-beta"   // blocked: prism 0.5.15 vs MDG additionalRuntimeClasspath
//        }
//        fabric {
//            loaderVersion = "0.18.6"
//            fabricApi("0.145.2+26.1.1")       // needs 26.1 API migration (ClickType, GuiGraphics)
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

// ---------------------------------------------------------------------------------------------------
// Second dev client ("runClient2") for two-client / desync testing.
//
// prism 0.5.15's `runs { client("client2") }` DSL is broken for the loaders we ship: on Fabric its
// reflective bridge picks Loom's Closure overload instead of the Action one ("argument type mismatch"),
// and on ModDevGradle it assigns a File to the Directory-typed `gameDirectory`. So each loader's second
// client is wired against the loader's own run DSL instead. Each registers a `runClient2` task and an
// IDE run config with its own run dir + username, so the two clients never share a save lock /
// options.txt / log. Typical workflow: build once, then launch `runClient` and `runClient2` from two
// terminals (or the IDE) - one opens its world to LAN, the other joins.
//   * Fabric (Loom)              -> here
//   * Forge 1.20.1 (ModDevGradle)-> here
//   * NeoForge 1.21.1 (MDG)      -> here
//   * 1.12.2 (RetroFuturaGradle) -> via `-Pclient2` on runClient, in the project(":1.12.2") block below
//   * LexForge 1.21.1            -> not wired (niche secondary loader); test 1.21.1 desync on NeoForge
// ---------------------------------------------------------------------------------------------------
listOf(":1.20.1:fabric", ":1.21.1:fabric").forEach { fabricPath ->
    project(fabricPath) {
        plugins.withId("fabric-loom") {
            extensions.configure<net.fabricmc.loom.api.LoomGradleExtensionAPI> {
                runs.create("client2") {
                    client()
                    configName = "Minecraft Client 2"
                    ideConfigGenerated(true)
                    runDir("run/client2")
                    programArgs("--username", "Dev2")
                }
            }
        }
    }
}

project(":1.20.1:forge") {
    plugins.withId("net.neoforged.moddev.legacyforge") {
        extensions.configure<net.neoforged.moddevgradle.legacyforge.dsl.LegacyForgeExtension> {
            runs.register("client2") {
                client()
                gameDirectory.set(layout.projectDirectory.dir("run/client2"))
                programArgument("--username")
                programArgument("Dev2")
            }
        }
    }
}

project(":1.21.1:neoforge") {
    plugins.withId("net.neoforged.moddev") {
        extensions.configure<net.neoforged.moddevgradle.dsl.NeoForgeExtension> {
            runs.register("client2") {
                client()
                gameDirectory.set(layout.projectDirectory.dir("run/client2"))
                programArgument("--username")
                programArgument("Dev2")
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

// MDG (via prism) emits `--mixin.config wfweight.mixins.json` twice in the generated dev run args,
// which crashes the Mixin bootstrap on a duplicate config. Dedupe to a single pair: dropping *every*
// occurrence (the previous workaround) left the dev client with no mixin config at all, so none of the
// client mixins - including the exhaustion sprint block - applied in dev.
fun dedupeMixinConfigArgs(lines: List<String>): List<String> {
    val out = mutableListOf<String>()
    var kept = false
    var i = 0
    while (i < lines.size) {
        val line = lines[i]
        if (line == "--mixin.config" && i + 1 < lines.size && lines[i + 1] == "wfweight.mixins.json") {
            if (!kept) {
                out += line
                out += lines[i + 1]
                kept = true
            }
            i += 2
        } else {
            out += line
            i += 1
        }
    }
    return out
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

            file.writeText(
                dedupeMixinConfigArgs(file.readLines()).joinToString(System.lineSeparator()) + System.lineSeparator()
            )
        }
    }

    // The secondary `runClient2` (added for two-client testing) gets its own program-args file from
    // ModDevGradle, which carries the same duplicate `--mixin.config wfweight.mixins.json` that crashes
    // the primary client. Dedupe every generated run-args file to a single pair so runClient2 launches
    // cleanly *and* still loads the mixin config.
    val sanitizeClient2RunArgs = tasks.register("sanitizeClient2RunArgs") {
        dependsOn(tasks.matching { it.name.startsWith("prepare") && it.name.endsWith("Run") })

        doLast {
            val dir = layout.buildDirectory.dir("moddev").get().asFile
            if (!dir.isDirectory) {
                return@doLast
            }

            dir.listFiles { file -> file.name.endsWith("RunProgramArgs.txt") }?.forEach { file ->
                file.writeText(
                    dedupeMixinConfigArgs(file.readLines()).joinToString(System.lineSeparator()) + System.lineSeparator()
                )
            }
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
    tasks.matching { it.name == "runClient2" }.configureEach {
        dependsOn(sanitizeClient2RunArgs)
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

        val refmapFile = layout.buildDirectory.file("mixin/wfweight.refmap.json")
        val mixinSrgFile = layout.buildDirectory.file("mixin/wfweight.mixins.srg")
        val genSrg = tasks.named<GenSrgMappingsTask>("generateForgeSrgMappings")
        val mcpToSrg = genSrg.flatMap { it.mcpToSrg }

        tasks.named<ReobfuscatedJar>("reobfJar") {
            inputJar.set(tasks.named<Jar>("shadowJar").flatMap { it.archiveFile })
            dependsOn("compileJava")
            extraSrgFiles.from(mixinSrgFile)
        }

        // Only the main compile hosts the mixin sources; never touch RFG's Minecraft compiles.
        tasks.withType<JavaCompile>().matching { it.name == "compileJava" }.configureEach {
            dependsOn(genSrg)
            inputs.file(mcpToSrg).withPropertyName("mixinReobfSrg")
            outputs.file(refmapFile).withPropertyName("mixinRefmap")
            outputs.file(mixinSrgFile).withPropertyName("mixinObfSrg")
            doFirst {
                val refmap = refmapFile.get().asFile
                refmap.parentFile.mkdirs()
                options.compilerArgs.addAll(
                    listOf(
                        "-AreobfSrgFile=${mcpToSrg.get().asFile.absolutePath}",
                        "-AoutRefMapFile=${refmap.absolutePath}",
                        "-AoutSrgFile=${mixinSrgFile.get().asFile.absolutePath}",
                        "-AdefaultObfuscationEnv=searge"
                    )
                )
            }
        }

        // The shadow plugin is applied after RFG, so defer these lookups via configureEach.
        tasks.withType<Jar>().matching { it.name == "jar" }.configureEach {
            dependsOn("compileJava")
            from(refmapFile)
        }
        tasks.withType<ShadowJar>().configureEach {
            dependsOn("compileJava")
            from(refmapFile)
        }

        // Two-client testing for 1.12.2. Prism's `runs { client("client2") }` DSL (used by every other
        // loader to get a `runClient2` task) is not applied to RetroFuturaGradle, so instead of fragile
        // duplication of RFG's internal run wiring we launch a SECOND client by reusing the proven
        // `runClient` with an alternate run dir + username. In a second terminal run:
        //     ./gradlew :1.12.2:runClient -Pclient2            (username "Dev2")
        //     ./gradlew :1.12.2:runClient -Pclient2=SomeName   (custom username)
        // When the property is absent, `runClient` behaves exactly as before, so the primary client and
        // the second client never share a save lock / options.txt / log.
        if (project.hasProperty("client2")) {
            val altName = (project.property("client2") as? String)?.takeIf { it.isNotBlank() } ?: "Dev2"
            val altRunDir = layout.projectDirectory.dir("run2")
            tasks.named<RunMinecraftTask>("runClient").configure {
                username.set(altName)
                workingDir = altRunDir.asFile
                doFirst { altRunDir.asFile.mkdirs() }
            }
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
