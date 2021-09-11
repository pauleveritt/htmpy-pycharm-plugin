package com.koxudaxi.htmpy.psi

import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.util.ProcessingContext
import com.jetbrains.python.psi.PyStringLiteralExpression
import com.koxudaxi.htmpy.isHtmpy

class HtmpyReferenceContributor : PsiReferenceContributor() {
    internal object Holder {
        @JvmField
        val TAG_STRING_PATTERN = PlatformPatterns.psiElement(PyStringLiteralExpression::class.java)
                .with(object : PatternCondition<PsiElement>("isFormatFunctionHtmp") {
                    override fun accepts(expression: PsiElement, context: ProcessingContext): Boolean {
                        return isHtmpy(expression)
                    }
                })
    }

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(Holder.TAG_STRING_PATTERN,
            HtmpyReferenceProvider())
    }
}