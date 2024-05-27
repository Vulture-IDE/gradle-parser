package org.vulture.ide.parser.gradle.script

import java.io.File
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.vulture.ide.parser.gradle.script.visitors.GradleSettingsVisitor

class GradleSettingsScript(val settings_file: File) {

  val name = settings_file.name

  val text: String? by lazy { if (isValid) settings_file.readText() else null }

  val isValid: Boolean
    get() = settings_file == null || settings_file.exists()

  val isGradle = settings_file.extension == "gradle"

  val isKts = settings_file.extension == "kts"

  val extension = settings_file.extension

  val gradleSettingsVisitor = GradleSettingsVisitor()

  fun parse(): MultipleCompilationErrorsException? {
    try {
      val compilePhase = CompilePhase.CANONICALIZATION
      val cu = CompilationUnit(CompilerConfiguration.DEFAULT)
      cu.addSource(settings_file)
      cu.compile(compilePhase.phaseNumber)

      val astNodes = mutableListOf<ASTNode>()
      cu.ast.modules.forEach { node ->
        val statementBlock = node.statementBlock
        if (statementBlock != null) {
          astNodes.add(statementBlock)
        }
      }

      astNodes.forEach { astNode -> astNode.visit(gradleSettingsVisitor) }
    } catch (e: MultipleCompilationErrorsException) {
      return e
    }
    return null
  }
}