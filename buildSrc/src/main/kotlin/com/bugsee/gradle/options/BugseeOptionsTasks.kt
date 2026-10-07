package com.bugsee.gradle.options

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import org.gradle.work.DisableCachingByDefault
import java.io.File
import java.net.URI

/**
 * Generates `BugseeOptions`, `BugseeLaunchOptions` and `BugseeOptionsSpec` into [outputDir]
 * from the committed option manifest snapshots and `kmp-options.json`.
 */
@CacheableTask
abstract class GenerateBugseeOptionsTask : DefaultTask() {

    @get:Internal
    abstract val androidManifest: RegularFileProperty

    @get:Internal
    abstract val iosManifest: RegularFileProperty

    @get:Internal
    abstract val config: RegularFileProperty

    // A missing snapshot must fail with a hint, not with Gradle's input validation error.
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val inputFiles: ConfigurableFileCollection = project.objects.fileCollection().from(androidManifest, iosManifest, config)

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val generator = try {
            BugseeOptionsGenerator(
                OptionsJson.readConfig(config.get().asFile),
                readSnapshot(androidManifest.get().asFile, "android"),
                readSnapshot(iosManifest.get().asFile, "ios"),
            )
        } catch (e: GradleException) {
            throw e
        } catch (e: RuntimeException) {
            throw GradleException(e.message ?: "Cannot generate Bugsee options", e)
        }
        generator.warnings.forEach { logger.warn("w: $it") }

        val packageDir = outputDir.get().asFile.resolve("com/bugsee/kmp")
        outputDir.get().asFile.deleteRecursively()
        packageDir.mkdirs()
        generator.generate().forEach { (name, content) -> packageDir.resolve(name).writeText(content) }
    }

    /** Reads `<platform>-<version>.json` and checks that it describes that platform and version. */
    private fun readSnapshot(file: File, platform: String): OptionsManifest {
        if (!file.isFile) {
            throw GradleException(
                "Missing option manifest snapshot $file. Run ./gradlew :library:updateBugseeOptionsManifests " +
                    "after changing the Bugsee SDK versions in gradle/libs.versions.toml."
            )
        }
        val manifest = OptionsJson.readManifest(file)
        val version = file.name.removePrefix("$platform-").removeSuffix(".json")
        if (manifest.sdk != platform || manifest.sdkVersion != version) {
            throw GradleException(
                "$file describes ${manifest.sdk} ${manifest.sdkVersion}, expected $platform $version. " +
                    "Run ./gradlew :library:updateBugseeOptionsManifests to refresh it."
            )
        }
        return manifest
    }
}

/**
 * Downloads the Android and iOS option manifests for the configured SDK versions into
 * [outputDir], replacing older snapshots. Run it by hand after bumping an SDK version.
 */
@UntrackedTask(because = "Downloads from the network on demand")
@DisableCachingByDefault(because = "Downloads from the network on demand")
abstract class UpdateBugseeOptionsManifestsTask : DefaultTask() {

    @get:Input
    abstract val androidVersion: Property<String>

    @get:Input
    abstract val iosVersion: Property<String>

    @get:Input
    abstract val baseUrl: Property<String>

    @get:Internal
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun update() {
        val dir = outputDir.get().asFile.apply { mkdirs() }
        download("android", androidVersion.get(), dir)
        download("ios", iosVersion.get(), dir)
    }

    private fun download(platform: String, version: String, dir: File) {
        val url = "${baseUrl.get().trimEnd('/')}/$platform/$version.json"
        val target = dir.resolve("$platform-$version.json")
        val temp = File.createTempFile("bugsee-options-$platform", ".json")
        try {
            val connection = URI(url).toURL().openConnection().apply {
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
            }
            connection.getInputStream().use { input -> temp.outputStream().use { input.copyTo(it) } }
            val manifest = OptionsJson.readManifest(temp)
            if (manifest.sdk != platform || manifest.sdkVersion != version) {
                throw GradleException("$url describes ${manifest.sdk} ${manifest.sdkVersion}, expected $platform $version")
            }
            temp.copyTo(target, overwrite = true)
        } catch (e: GradleException) {
            throw e
        } catch (e: Exception) {
            throw GradleException("Cannot download $url: ${e.message}", e)
        } finally {
            temp.delete()
        }

        dir.listFiles { file -> file.name.startsWith("$platform-") && file.name.endsWith(".json") && file != target }
            ?.forEach { stale ->
                stale.delete()
                logger.lifecycle("Removed stale snapshot ${stale.name}")
            }
        logger.lifecycle("Updated ${target.name} from $url")
    }

    private companion object {
        const val TIMEOUT_MS = 30_000
    }
}
