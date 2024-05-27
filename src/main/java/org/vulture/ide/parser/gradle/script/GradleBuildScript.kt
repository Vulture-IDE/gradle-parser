package org.vulture.ide.parser.gradle.script

import java.io.File
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.vulture.ide.parser.gradle.script.visitors.GradleBuildVisitor

class GradleBuildScript(val build_file: File) {

  val name = build_file.name

  val text: String? by lazy { if (isValid) build_file.readText() else null }

  val isValid: Boolean
    get() = build_file == null || build_file.exists()

  val isGradle = build_file.extension == "gradle"

  val isKts = build_file.extension == "kts"

  val extension = build_file.extension

  val gradleBuildVisitor = GradleBuildVisitor()

  fun parse(): MultipleCompilationErrorsException? {
    try {
      val compilePhase = CompilePhase.CANONICALIZATION
      val cu = CompilationUnit(CompilerConfiguration.DEFAULT)
      cu.addSource(build_file)
      cu.compile(compilePhase.phaseNumber)

      val astNodes = mutableListOf<ASTNode>()
      cu.ast.modules.forEach { node ->
        val statementBlock = node.statementBlock
        if (statementBlock != null) {
          astNodes.add(statementBlock)
        }
      }

      astNodes.forEach { astNode -> astNode.visit(gradleBuildVisitor) }
    } catch (e: MultipleCompilationErrorsException) {
      return e
    }
    return null
  }
}