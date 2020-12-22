package com.koxudaxi.htmpy

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyStringLiteralExpression

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
                resolvedComponent.classAttributes.forEach { instanceAttribute ->
                    if (!instanceAttribute.hasAssignedValue() && !keys.contains(instanceAttribute.name)) {
                        registerProblem(
                            node,
                            "missing a required argument: '${instanceAttribute.name}'",
                            ProblemHighlightType.WARNING,
                            null,
                            TextRange(
                                tag.range.first + component.range.first + 1,
                                component.value.length + tag.range.first
                            )
                        )
                    }

                }
                keys.forEach { (name, key) ->
                    if (resolvedComponent.findClassAttribute(name, true, myTypeEvalContext) != null) {
                        if (key.destructured.component2().isEmpty()) {
                            val startPoint = tag.range.first + component.range.first + key.range.first
                            registerProblem(
                                node,
                                "Expression expected",
                                ProblemHighlightType.GENERIC_ERROR,
                                null,
                                TextRange(startPoint + name.length, startPoint + name.length + 1)
                            )
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