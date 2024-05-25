package org.vulture.ide.parser.gradle.models

data class GradleSettings(
    val rootProjectName: String? = null,
    val includes: List<Include>? = mutableListOf<Include>()
) {
  data class Include(val include: String? = null)
}
