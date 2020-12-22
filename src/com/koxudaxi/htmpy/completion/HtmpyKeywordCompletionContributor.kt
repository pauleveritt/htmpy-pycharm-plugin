package com.koxudaxi.htmpy.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.impl.source.resolve.FileContextUtil
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.ProcessingContext
import com.jetbrains.python.psi.PyElement
import com.koxudaxi.htmpy.*

class HtmpyKeywordCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val position = parameters.position
                    val type = position.node.elementType
                    if (type !== XmlTokenType.XML_DATA_CHARACTERS) {
                        return
                    }
                    val hostElement =
                        position.parent.containingFile.getUserData(FileContextUtil.INJECTED_IN_ELEMENT)?.element as? PyElement
                            ?: return
                    if (!isHtmpy(hostElement)) return
                    val typeContext = getContext(hostElement)
                    collectComponents(hostElement) { resolvedComponent, tag, _, keys ->
                        if (tag.range.contains(position.textOffset)) {
                            resolvedComponent.classAttributes.filter { instanceAttribute ->
                                !instanceAttribute.hasAssignedValue() && !keys.contains(instanceAttribute.name)
                            }
                                .mapNotNull { validKey -> validKey.name }
                                .forEach { name ->
                                    val attribute = resolvedComponent.findClassAttribute(name, true, null)
                                    if (attribute != null) {
                                        val element = PrioritizedLookupElement.withGrouping(
                                            LookupElementBuilder
                                                .createWithSmartPointer("$name=", attribute)
                                                .withTypeText(typeContext.getType(attribute)?.name)
                                                .withIcon(AllIcons.Nodes.Field), 1
                                        )
                                        result.addElement(PrioritizedLookupElement.withPriority(element, 100.0))
                                    }
                                }
                        }
                    }
                }
            }
        )

    }
}