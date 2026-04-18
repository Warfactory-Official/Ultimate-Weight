plugins {
    id("dev.prism")
}

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.bundling.Jar
import org.gradle.language.jvm.tasks.ProcessResources

group = "warfactory.ultimateweight"
version = "1.0.0"

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
            loaderVersionRange = "[47,)"
            dependencies {
                modRuntimeOnly("curse.maven:jei-238222:7920915")
                modRuntimeOnly("curse.maven:kubejs-238086:5853326")
                modRuntimeOnly("curse.maven:storage-drawers-223852:6994481")
                modRuntimeOnly("curse.maven:rhino-416294:6186971")
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
            loaderVersionRange = "[4,)"
        }
        lexForge {
            loaderVersion = "52.1.9"
            loaderVersionRange = "[52,)"
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
                modRuntimeOnly("curse.maven:baubles-227083:2518667")
                annotationProcessor ("org.ow2.asm:asm-debug-all:5.2")
                annotationProcessor ("com.google.guava:guava:32.1.2-jre")
                annotationProcessor ("com.google.code.gson:gson:2.8.9")
                annotationProcessor ("zone.rong:mixinbooter:10.7")
                modImplementation("curse.maven:mixin-booter-419286:7049694");
//                modRuntimeOnly("curse.maven:had-enough-items-557549:7899997")
                modRuntimeOnly("curse.maven:groovyscript-687577:7925117")
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
        from(forgeRefmap)
    }

    tasks.withType<Jar>().configureEach {
        from(forgeRefmap)
    }

    tasks.withType<ShadowJar>().configureEach {
        from(forgeRefmap)
        exclude("META-INF/jandex.idx")
    }

    tasks.matching { it.name == "runClient" }.configureEach {
        dependsOn(sanitizeClientRunArgs)
    }
}
