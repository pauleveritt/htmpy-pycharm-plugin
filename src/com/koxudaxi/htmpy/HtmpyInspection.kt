package com.koxudaxi.htmpy

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.documentation.PythonDocumentationProvider
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyStringLiteralExpression
import com.jetbrains.python.psi.PyUtil
import com.jetbrains.python.psi.impl.stubs.PyDataclassFieldStubImpl
import com.jetbrains.python.psi.stubs.PyDataclassFieldStub
import com.jetbrains.python.psi.types.PyNoneType
import com.jetbrains.python.psi.types.PyTypeChecker
import com.jetbrains.python.psi.types.PyUnionType

class HtmpyInspection : PyInspection() {

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor = Visitor(holder, session)

    inner class Visitor(holder: ProblemsHolder, session: LocalInspectionToolSession) :
        PyInspectionVisitor(holder, session) {
        override fun visitPyStringLiteralExpression(node: PyStringLiteralExpression) {
            super.visitPyStringLiteralExpression(node)
            if (!isHtmpy(node)) return
            collectComponents(node, { resolvedComponent, tag, component, keys ->
                resolvedComponent.classAttributes.forEach { classAttribute ->
                    if (!classAttribute.hasAssignedValue() && !keys.contains(classAttribute.name)) {
                        val field = PyDataclassFieldStubImpl.create(classAttribute)
                        if (!(field is PyDataclassFieldStub && !field.initValue()) && (myTypeEvalContext.getType(classAttribute) as? PyUnionType)?.members?.any { it is PyNoneType } != true) {
                            registerProblem(
                                node,
                                "missing a required argument: '${classAttribute.name}'",
                                ProblemHighlightType.WARNING,
                                null,
                                TextRange(
                                    tag.range.first + component.range.first + 1,
                                    component.value.length + tag.range.first
                                )
                            )
                        }
                    }

                }
                keys.forEach { (name, key) ->
                    val attribute = resolvedComponent.findClassAttribute(name, true, myTypeEvalContext)
                    if (attribute != null) {
                        val value = key.destructured.component2()
                        if (value.isEmpty()) {
                            val startPoint = tag.range.first + component.range.first + key.range.first
                            registerProblem(
                                node,
                                "Expression expected",
                                ProblemHighlightType.GENERIC_ERROR,
                                null,
                                TextRange(startPoint + name.length, startPoint + name.length + 1)
                            )
                        } else {
                            val expectedType =  myTypeEvalContext.getType(attribute)
                            val actualValue = when {
                                value.startsWith("\"{") && value.endsWith("}\"") -> value.substring(2, value.length - 2)
                                value.startsWith('"') && value.endsWith('"') -> value
                                value.startsWith('{') && value.endsWith('}') -> value.substring(1, value.length - 1)
                                else -> "\"$value\""
                            }
                            val actualType = PyUtil.createExpressionFromFragment(actualValue, node)
                                ?.let { getPyTypeFromPyExpression(it, myTypeEvalContext) }

                            if (! PyTypeChecker.match(expectedType, actualType, myTypeEvalContext)
                            ) {
                                val startPoint = tag.range.first + component.range.first
                                registerProblem(node, String.format("Expected type '%s', got '%s' instead",
                                    PythonDocumentationProvider.getTypeName(expectedType, myTypeEvalContext),
                                    PythonDocumentationProvider.getTypeName(actualType, myTypeEvalContext)),
                                    ProblemHighlightType.WARNING,
                                    null,
                                    TextRange(startPoint + key.range.first - 1, startPoint +  key.range.last)
                                )
                            }
                        }
                    } else {
                        val startPoint = tag.range.first + component.range.first - 1 + key.range.first
                        registerProblem(
                            node,
                            "invalid a argument: '${name}'",
                            ProblemHighlightType.WARNING,
                            null,
                            TextRange(startPoint, startPoint + name.length)
                        )
                    }
                }

            })
        }
    }
}