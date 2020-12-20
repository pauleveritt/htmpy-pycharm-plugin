package com.koxudaxi.htmpy

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.codeInsight.dataflow.scope.ScopeUtil
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyStringLiteralExpression
import com.jetbrains.python.psi.resolve.PyResolveUtil

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
            if (!HtmpyInjector.isViewDomHtm(node) && !HtmpyInjector.isHtm(node)) return
            val text = node.text
            Regex("<[^>]*\\{([^}]*)\\}[^>]*/[^>]*>").findAll(text).forEach {
                val attribute = Regex("\\{([^}\\s]*)\\}").findAll(it.value).firstOrNull()
                val owner = ScopeUtil.getScopeOwner(node)
                if (attribute != null && attribute.value.length > 2 && owner != null) {
                    val psiElement =
                        PyResolveUtil.resolveLocally(owner, attribute.destructured.component1()).firstOrNull()
                    if (psiElement is PyClass && isDataclass(psiElement)) {
                        val keys =
                            Regex("([^=}\\s]+)[=\\s/]([^\\s/]*)").findAll(it.value).toList()
                        val keyNames = keys.map { key -> key.destructured.component1() }
                                .toList()
                        psiElement.classAttributes.forEach { instanceAttribute ->
                            if (!instanceAttribute.hasAssignedValue() && !keyNames.contains(instanceAttribute.name)) {
                                registerProblem(
                                    node,
                                    "missing a required argument: '${instanceAttribute.name}'",
                                    ProblemHighlightType.WARNING,
                                    null,
                                    TextRange(
                                        it.range.first + attribute.range.first + 1,
                                        attribute.value.length + it.range.first
                                    )
                                )
                            }

                        }
                        keys.forEach { key ->
                            val name = key.destructured.component1()
                            if (psiElement.findClassAttribute(name, true, myTypeEvalContext) == null) {
                                val startPoint = it.range.first + attribute.range.first -1 + key.range.first
                                registerProblem(
                                node,
                                "invalid a argument: '${name}'",
                                ProblemHighlightType.WARNING,
                                null,
                                TextRange(startPoint, startPoint + name.length)
                            )
                            }
                        }
                    }
                }
            }

        }
    }
}