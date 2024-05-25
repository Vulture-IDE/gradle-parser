package org.vulture.ide.parser.gradle.script.visitors

import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.vulture.ide.parser.gradle.models.GradleSettings
import org.vulture.ide.parser.gradle.models.GradleSettings.Include

class GradleSettingsVisitor : CodeVisitorSupport() {

  private lateinit var rootProjectName: String
  private val includes = mutableListOf<Include>()

  override fun visitBinaryExpression(binaryExpression: BinaryExpression) {
    super.visitBinaryExpression(binaryExpression)

    val leftExpression = binaryExpression.leftExpression
    val rightExpression = binaryExpression.rightExpression

    if (leftExpression is PropertyExpression && rightExpression is ConstantExpression) {
      val propertyOwner = leftExpression.objectExpression
      val propertyName = leftExpression.property

      if (propertyOwner is VariableExpression && propertyName is ConstantExpression) {
        val propertyFullName = "${propertyOwner.name}.${propertyName.text}"
        val propertyValue = rightExpression.text

        when (propertyFullName) {
          "rootProject.name" -> rootProjectName = propertyValue
        }
      }
    }
  }

  override fun visitMethodCallExpression(methodCallExpression: MethodCallExpression) {
    super.visitMethodCallExpression(methodCallExpression)

    val method = methodCallExpression.methodAsString
    val arguments = methodCallExpression.arguments

    when (method) {
      "include" -> {
        if (arguments is TupleExpression) {
          arguments.expressions.forEach {
            if (it is ConstantExpression) {
              val argText = it.text
              if (argText.contains(",")) {
                includes.addAll(argText.split(",").map { Include(it.trim()) })
              } else {
                includes.add(Include(argText))
              }
            }
          }
        }
      }
    }
  }

  fun getSettings(): GradleSettings {
    return GradleSettings(rootProjectName, includes)
  }
}
