package org.vulture.ide.parser.gradle.script.visitors

import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.vulture.ide.parser.gradle.models.GradleBuild
import org.vulture.ide.parser.gradle.models.GradleBuild.Dependency
import org.vulture.ide.parser.gradle.models.GradleBuild.Plugin
import org.vulture.ide.parser.gradle.models.GradleBuild.Repository

class GradleBuildVisitor : CodeVisitorSupport() {

  private val plugins = mutableListOf<Plugin>()
  private val repositories = mutableListOf<Repository>()
  private val dependencies = mutableListOf<Dependency>()

  override fun visitMethodCallExpression(methodCallExpression: MethodCallExpression) {
    super.visitMethodCallExpression(methodCallExpression)

    val method = methodCallExpression.methodAsString
    val arguments = methodCallExpression.arguments

    when (method) {
      "plugins" -> {
        if (arguments is ArgumentListExpression) {
          val expressions = arguments.expressions
          if (expressions.size == 1) {
            val closureExpression = expressions.get(0)
            if (closureExpression is ClosureExpression) {
              val code = closureExpression.code
              if (code is BlockStatement) {
                val statements = code.statements
                statements.forEach { statement ->
                  if (statement is ExpressionStatement) {
                    val expression = statement.expression
                    if (expression is MethodCallExpression) {
                      val methodId = expression.methodAsString
                      val argumentsId = expression.arguments
                      when (methodId) {
                        "id" -> {
                          if (argumentsId is ArgumentListExpression) {
                            val expressionsId = argumentsId.expressions
                            if (expressionsId.size == 1) {
                              val id = expressionsId.get(0)
                              if (id is ConstantExpression) {
                                plugins.add(Plugin(id.text))
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
      "repositories" -> {
        if (arguments is ArgumentListExpression) {
          val expressions = arguments.expressions
          if (expressions.size == 1) {
            val closureExpression = expressions.get(0)
            if (closureExpression is ClosureExpression) {
              val code = closureExpression.code
              if (code is BlockStatement) {
                val statements = code.statements
                statements.forEach { statement ->
                  if (statement is ExpressionStatement) {
                    val expression = statement.expression
                    if (expression is MethodCallExpression) {
                      val repository = expression.methodAsString
                      repositories.addAll(getDefaultRepositories(repository))
                    }
                  }
                }
              }
            }
          }
        }
      }
      "dependencies" -> {
        if (arguments is ArgumentListExpression) {
          val expressions = arguments.expressions
          if (expressions.size == 1) {
            val closureExpression = expressions.get(0)
            if (closureExpression is ClosureExpression) {
              val code = closureExpression.code
              if (code is BlockStatement) {
                val statements = code.statements
                statements.forEach { statement ->
                  if (statement is ExpressionStatement) {
                    val expression = statement.expression
                    if (expression is MethodCallExpression) {
                      val methodId = expression.methodAsString
                      val argumentsId = expression.arguments
                      when (methodId) {
                        "implementation" -> {
                          if (argumentsId is ArgumentListExpression) {
                            val expressionsId = argumentsId.expressions
                            if (expressionsId.size == 1) {
                              val id = expressionsId.get(0)
                              if (id is ConstantExpression) {
                                val text = id.text.split(":").toList()
                                dependencies.add(
                                    Dependency(
                                        text.get(0),
                                        text.get(1),
                                        text.get(2),
                                        if (text.size == 4) text.get(3) else null,
                                        methodId))
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  fun getDefaultRepositories(repository: String): List<Repository> {
    val repositories = mutableListOf<Repository>()
    when (repository) {
      "google" -> {
        repositories.add(Repository(repository, "https://maven.google.com/"))
      }
      "mavenLocal" -> {
        repositories.add(Repository(repository, ".m2/repository"))
      }
      "mavenCentral" -> {
        repositories.add(Repository(repository, "https://repo.maven.apache.org/maven2/"))
      }
      "gradlePluginPortal" -> {
        repositories.add(Repository(repository, "https://plugins.gradle.org/m2/"))
      }
    }
    return repositories
  }

  fun getBuild(): GradleBuild {
    return GradleBuild(plugins, repositories, dependencies)
  }
}
