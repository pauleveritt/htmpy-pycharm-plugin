package com.koxudaxi.htmpy.psi

import com.intellij.patterns.PatternCondition
import com.intellij.psi.PsiReferenceContributor
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.impl.source.resolve.FileContextUtil
import com.intellij.psi.xml.XmlTokenType
import com.jetbrains.python.psi.*
import com.koxudaxi.htmpy.isHtmpy

class HtmpyReferenceContributor : PsiReferenceContributor() {
    internal object Holder {
        @JvmField
        val TAG_STRING_PATTERN = PlatformPatterns.psiElement()
                .with(object : PatternCondition<PsiElement>("isFormatFunctionHtmp") {
                    override fun accepts(expression: PsiElement, context: ProcessingContext): Boolean {
                        val type = expression.node.elementType
                        if (type !== XmlTokenType.XML_DATA_CHARACTERS) {
                            return false
                        }
                        val hostElement =
                            expression.parent.containingFile.getUserData(FileContextUtil.INJECTED_IN_ELEMENT)?.element as? PyElement
                                ?: return false
                        return isHtmpy(hostElement)
                    }
                })
    }

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement()
            .andOr(Holder.TAG_STRING_PATTERN),
            HtmpyReferenceProvider())
    }
}