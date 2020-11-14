package com.koxudaxi.htmpy.psi.resolve

import com.intellij.psi.impl.source.resolve.FileContextUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import com.jetbrains.python.documentation.doctest.PyDocstringLanguageDialect
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.resolve.*
import com.jetbrains.python.psi.types.TypeEvalContext


class HtmpyReferenceResolveProvider : PyReferenceResolveProvider {
    override fun resolveName(element: PyQualifiedExpression, context: TypeEvalContext): List<RatedResolveResult> {
        if (element.containingFile.language != PyDocstringLanguageDialect.getInstance()) return emptyList()
        if (element !is PyReferenceExpression) return emptyList()

        // For comprehension
        val psiRange = element.containingFile.getUserData(FileContextUtil.INJECTED_IN_ELEMENT)?.psiRange
                ?: return emptyList()
        return PyResolveUtil.resolveLocally(element).filter {
            PsiTreeUtil.getTopmostParentOfType(it, PyComprehensionElement::class.java)?.let { listCompExpression ->
                listCompExpression.startOffset < psiRange.startOffset && listCompExpression.endOffset > psiRange.endOffset
            } ?: false
        }.map {
            RatedResolveResult(RatedResolveResult.RATE_NORMAL, it)
        }
    }
}