package org.vulture.ide.parser.gradle.models

data class GradleBuild(
    val plugins: List<Plugin>? = mutableListOf<Plugin>(),
    val repositories: List<Repository>? = mutableListOf<Repository>(),
    val dependencies: List<Dependency>? = mutableListOf<Dependency>()
) {

  data class Plugin(val plugin: String? = null)

  data class Repository(val repository: String? = null, val url: String? = null)

  data class Dependency(
      val group: String? = null,
      val name: String? = null,
      val version: String? = null,
      val classifier: String? = null,
      val scope: String? = null
  )
}
