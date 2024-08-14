package com.koxudaxi.htmpy.completion

import ai.grazie.text.find
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.completion.CompletionUtilCore.DUMMY_IDENTIFIER
import com.intellij.codeInsight.lookup.LookupElementDecorator
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.impl.source.resolve.FileContextUtil
import com.intellij.util.ProcessingContext
import com.jetbrains.python.documentation.doctest.PyDocstringFile
import com.jetbrains.python.psi.*
import com.koxudaxi.htmpy.collectComponents
import com.koxudaxi.htmpy.isHtmpy

class HtmpyTagCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters, context: ProcessingContext, resultSet: CompletionResultSet
                ) {
                    val position = parameters.position
                    val containingFile = position.parent.containingFile
                    if (containingFile !is PyDocstringFile) return
                    val hostElement = position.parent.containingFile.getUserData(FileContextUtil.INJECTED_IN_ELEMENT)?.element ?: return
                    if (!isHtmpy(hostElement)) return
                    collectComponents(hostElement, { _, _, _, _ -> {} }, { _, _, _, _ -> },
                        { _, tag, _, _ ->
                            // For tags
                            val startOffset = hostElement.text.find(DUMMY_IDENTIFIER)?.start
                            if (startOffset != null && tag.range.contains(startOffset)) {
                                resultSet.runRemainingContributors(parameters) { completionResult ->
                                    val lookupElement = completionResult.lookupElement
                                    val result = when (lookupElement.psiElement) {
                                        is PyFunction -> completionResult.withLookupElement(
                                            LookupElementDecorator.withInsertHandler(
                                                lookupElement
                                            ) { _, _ -> })

                                        else -> completionResult
                                    }
                                    resultSet.passResult(result)

                                }
                            }
                        })
                }
            })

    }
}