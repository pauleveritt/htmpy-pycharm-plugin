package com.koxudaxi.htmpy.psi.resolve

import com.jetbrains.python.codeInsight.dataflow.scope.ScopeUtil
import com.jetbrains.python.documentation.doctest.PyDocstringLanguageDialect
import com.jetbrains.python.psi.PyQualifiedExpression
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.resolve.*
import com.jetbrains.python.psi.types.TypeEvalContext


class HtmpyReferenceResolveProvider : PyReferenceResolveProvider {
    override fun resolveName(element: PyQualifiedExpression, context: TypeEvalContext): List<RatedResolveResult> {
        if (element.containingFile.language != PyDocstringLanguageDialect.getInstance()) return emptyList()
        if (element !is PyReferenceExpression) return emptyList()
        val referencedName = element.referencedName ?: return emptyList()
        val originalOwner = ScopeUtil.getScopeOwner(element)

        return if (originalOwner != null) {
            PyResolveUtil.resolveLocally(originalOwner, referencedName)
                    .map { RatedResolveResult(RatedResolveResult.RATE_NORMAL, it) }
        } else emptyList()
    }
}