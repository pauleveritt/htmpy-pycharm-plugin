package com.koxudaxi.htmpy.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.impl.source.resolve.FileContextUtil
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.ProcessingContext
import com.jetbrains.python.codeInsight.dataflow.scope.ScopeUtil
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.resolve.PyResolveUtil
import com.koxudaxi.htmpy.HtmpyInjector
import com.koxudaxi.htmpy.isDataclass

class HtmpyKeywordCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC,
            PlatformPatterns.psiElement(),
                object : CompletionProvider<CompletionParameters>() {
                    override fun addCompletions(parameters: CompletionParameters,
                                                context: ProcessingContext,
                                                result: CompletionResultSet) {
                        val position = parameters.position
                        val type = position.node.elementType
                        if (type !== XmlTokenType.XML_DATA_CHARACTERS) {
                            return
                        }
                        val hostElement = position.parent.containingFile.getUserData(FileContextUtil.INJECTED_IN_ELEMENT)?.element ?: return
                        if (!HtmpyInjector.isViewDomHtm(hostElement) && !HtmpyInjector.isHtm(hostElement)) return
                        val text = hostElement.text
                        Regex("<[^>]*\\{([^}]*)\\}[^>]*/[^>]*>").findAll(text).forEach {
                            val attribute = Regex("\\{([^}\\s]*)\\}").findAll(it.value).firstOrNull()
                            val owner = ScopeUtil.getScopeOwner(hostElement)
                            if (attribute != null && attribute.value.length > 2 && owner != null) {
                                val psiElement =
                                    PyResolveUtil.resolveLocally(owner, attribute.destructured.component1()).firstOrNull()
                                if (psiElement is PyClass && isDataclass(psiElement)) {
                                    val keys =
                                        Regex("([^=}\\s]+)[=\\s/]([^\\s/]*)").findAll(it.value).toList()
                                    val keyNames = keys.map { key -> key.destructured.component1() }
                                        .toList()
                                    psiElement.classAttributes.filter { instanceAttribute ->
                                     !instanceAttribute.hasAssignedValue() && !keyNames.contains(instanceAttribute.name) }
                                        .mapNotNull { validKey -> validKey.name }.forEach { name -> result.addElement(LookupElementBuilder.create(name)) }
                                }
                            }
                        }
                    }
                }
        )

    }
}