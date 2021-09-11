package com.koxudaxi.htmpy.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.codeInsight.dataflow.scope.ScopeUtil
import com.jetbrains.python.inspections.PyInspectionExtension
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.impl.PyPsiUtils
import com.jetbrains.python.psi.resolve.PyResolveUtil
import com.jetbrains.python.psi.types.TypeEvalContext
import com.koxudaxi.htmpy.VIEWDOM_H_HTML_QUALIFIED_NAME
import com.koxudaxi.htmpy.collectComponents
import com.koxudaxi.htmpy.isHtm
import com.koxudaxi.htmpy.isHtmpy
import org.intellij.plugins.intelliLang.util.PsiUtilEx

class HtmpyIgnoreUnusedInspection: PyInspectionExtension() {
        override fun ignoreUnused(element: PsiElement, evalContext: TypeEvalContext): Boolean {
            if (element.containingFile !is PyFile) {
                return false
            }
            if (element !is PyTargetExpression ) {
                return false
            }
            val scope = ScopeUtil.getScopeOwner(element) ?: return false

            val html = PyResolveUtil.resolveQualifiedNameInScope(VIEWDOM_H_HTML_QUALIFIED_NAME, scope, evalContext).firstOrNull()
            var result = false
            PsiTreeUtil.getChildrenOfType(element.parent, PyStringLiteralExpression::class.java)?.forEach {
                if (isHtmpy(it)) {
                    collectComponents(it, { _, tag, component, keys ->
                        val actualComponent =
                            PyUtil.createExpressionFromFragment(tag.value.substring(IntRange(1, tag.value.length - 2)),
                                element)
                        if (actualComponent == element) {
                            result = true
                            return@collectComponents
                        }
                    },
                        {_, _, _, _ -> } )
                }
            }
            return result
        }
    }
