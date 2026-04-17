plugins {
    id("dev.prism")
}

group = "warfactory.ultimateweight"
version = "1.0.0"

prism {
        curseMaven()
        modrinthMaven()
        maven("WarfactoryMaven", "https://repo.warfactory.co/releases")
        maven("CleanroomMaven", "https://maven.cleanroommc.com")
        maven("GTCEu Maven", "https://maven.gtceu.com")

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
            api("it.unimi.dsi:fastutil:8.5.12")
            jarJar("org.yaml:snakeyaml:2.6")
            jarJar("it.unimi.dsi:fastutil:8.5.12")
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
    }

    // Single-loader: just fabric
    version("26.1") {
        fabric {
            loaderVersion = "0.18.6"
            fabricApi("0.145.2+26.1.1")
        }
    }

    // Legacy Forge 1.12.2
    version("1.12.2") {
        legacyForge {
            mcVersion = "1.12.2"
            forgeVersion = "14.23.5.2847"
            mappingChannel = "stable"
            mappingVersion = "39"
            username = "Developer"
            dependencies {
                annotationProcessor ("org.ow2.asm:asm-debug-all:5.2")
                annotationProcessor ("com.google.guava:guava:32.1.2-jre")
                annotationProcessor ("com.google.code.gson:gson:2.8.9")
                annotationProcessor ("zone.rong:mixinbooter:10.7")
                modImplementation("curse.maven:mixin-booter-419286:7049694");
                modRuntimeOnly("curse.maven:had-enough-items-557549:7899997")
                modRuntimeOnly("curse.maven:groovyscript-687577:7925117")
//                modRuntimeOnly("curse.maven:gregtech-ce-unofficial-557242:5519022")
                modRuntimeOnly("curse.maven:ctm-267602:2915363")
//                modRuntimeOnly("curse.maven:hbm-nuclear-tech-mod-community-edition-1312314:7685718")
                modRuntimeOnly("curse.maven:storage-drawers-223852:5981297")
                modRuntimeOnly("curse.maven:chameleon-230497:2450900")
//                modRuntimeOnly("codechicken:codechickenlib:3.2.3.358")
            }
        }
    }
}

project(":common") {
    configurations.configureEach {
        exclude(group = "com.mojang", module = "logging")
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
}

project(":1.12.2") {
    tasks.withType<Jar>().configureEach {
        manifest {
            attributes(
                "FMLCorePlugin" to "com.warfactory.ultimateweight.mixin.UltimateWeightLoadingPlugin",
                "FMLCorePluginContainsFMLMod" to "true",
                "ForceLoadAsMod" to "true"
            )
        }
    }
}
