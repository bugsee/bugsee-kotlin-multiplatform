package com.bugsee.gradle

import org.gradle.api.Project
import java.util.Properties

/** The value of [name] from -P or local.properties, or null when it is unset or blank. */
fun Project.localProperty(name: String): String? = (
    providers.gradleProperty(name).orNull
        ?: rootProject.file("local.properties").takeIf { it.isFile }?.let { file ->
            Properties().apply { file.inputStream().use { load(it) } }.getProperty(name)
        }
    )?.takeIf { it.isNotBlank() }
