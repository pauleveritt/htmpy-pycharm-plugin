package com.koxudaxi.htmpy.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.jetbrains.python.PyTokenTypes
import com.koxudaxi.htmpy.psi.HtmpyPythonElement
import com.koxudaxi.htmpy.psi.HtmpyTypes
import com.koxudaxi.htmpy.psi.impl.HtmpyPythonElementImpl

class HtmpyKeywordCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(HtmpyTypes.PYTHON_CODE),
                object : CompletionProvider<CompletionParameters>() {
                    override fun addCompletions(parameters: CompletionParameters,
                                                context: ProcessingContext,
                                                result: CompletionResultSet) {
//                        result.addElement(LookupElementBuilder.create("abc"))
                    }
                }
        )

    }
}