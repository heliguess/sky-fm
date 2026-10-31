import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	id("net.fabricmc.fabric-loom")
	id("maven-publish")
	id("org.jetbrains.kotlin.jvm")
}

version = "${property("mod_version")}+${stonecutter.current.version}"

base {
	archivesName = rootProject.name
}

repositories {
	maven("https://maven.shedaniel.me/")
	maven("https://maven.terraformersmc.com/releases/")
}

loom {
	mods {
		register("plead-fm") {
			sourceSet(sourceSets["main"])
		}
	}

	runConfigs.configureEach {
		ideConfigGenerated(stonecutter.current.isActive)
		runDir("../../run")
	}
}

dependencies {
	minecraft("com.mojang:minecraft:${stonecutter.current.version}")
	implementation("net.fabricmc:fabric-loader:${property("loader_version")}")
	implementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
	implementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")
	implementation("me.shedaniel.cloth:cloth-config-fabric:${property("cloth_config_version")}")
	implementation("com.terraformersmc:modmenu:${property("modmenu_version")}")
}

tasks.processResources {
	val version = project.version.toString()
	val minecraft = stonecutter.current.version
	inputs.property("version", version)
	inputs.property("minecraft", minecraft)

	filesMatching("fabric.mod.json") {
		expand("version" to version, "minecraft" to minecraft)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release = 25
}

kotlin {
	compilerOptions {
		jvmTarget = JvmTarget.JVM_25
	}
}

java {
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_25
	targetCompatibility = JavaVersion.VERSION_25
}

tasks.jar {
	val projectName = rootProject.name
	inputs.property("projectName", projectName)

	from(rootProject.file("LICENSE")) {
		rename { "${it}_$projectName" }
	}
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}
}

val copyJar by tasks.registering(Copy::class) {
	description = "Copy Jars to /src/build/libs"
    group = "build"
	from(tasks.jar.flatMap { it.archiveFile })
	into(rootProject.layout.buildDirectory.dir("libs"))
}

tasks.build {
	finalizedBy(copyJar)
}
