package com.koxudaxi.htmpy.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.FileContextUtil
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.ProcessingContext
import com.intellij.util.containers.ContainerUtil
import com.jetbrains.python.PyNames
import com.jetbrains.python.psi.*
import com.koxudaxi.htmpy.*

class HtmpyKeywordCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet,
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
                    val typeContext = getContextForCodeCompletion(hostElement)
                    collectComponents(hostElement, { resolvedComponent, tag, _, keys ->
                        if (tag.range.contains(position.textOffset)) {
                            // For parameters
                            when (resolvedComponent) {
                                is PyClass -> {
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
                                else -> {
                                    (resolvedComponent as PyFunction).parameterList.parameters.filter { parameter ->
                                        !parameter.hasDefaultValue() && !keys.contains(parameter.name)
                                    }
                                        .mapNotNull { validKey -> validKey.name }
                                        .forEach { name ->
                                            val parameter = resolvedComponent.parameterList.findParameterByName(name)
                                            if (parameter != null) {
                                                val element = PrioritizedLookupElement.withGrouping(
                                                    LookupElementBuilder
                                                        .createWithSmartPointer("$name=", parameter)
                                                        .withTypeText(typeContext.getType(parameter)?.name)
                                                        .withIcon(AllIcons.Nodes.Field), 1
                                                )
                                                result.addElement(PrioritizedLookupElement.withPriority(element, 100.0))
                                            }
                                        }
                                }
                            }
                        }
                    }, { _, _, _, _ -> })
                    val targetBrace =
                        Regex("\\{([^}]*)}").findAll(hostElement.text)
                            .firstOrNull { it.range.contains(position.textOffset - 1) }
                            ?: return
                    val completionPrefix: String =
                        hostElement.text.substring(targetBrace.range.first + 1, position.textOffset - 1)
                    val autoPopupAfterOpeningBrace = completionPrefix.isEmpty() && parameters.isAutoPopup
                    if (autoPopupAfterOpeningBrace) {
                        return
                    }
                    val prefixCannotStartReference =
                        completionPrefix.isNotEmpty() && (!PyNames.isIdentifier(completionPrefix) && !completionPrefix.endsWith("."))
                    if (prefixCannotStartReference) {
                        return
                    }

                    val reference: PsiReference = hostElement.findReferenceAt(parameters.offset - 1) ?: return
                    val tagVariants = ContainerUtil.mapNotNull(reference.variants
                    ) { v: Any? ->
                        PyUtil.`as`(v,
                            LookupElement::class.java)
                    }
                    if (tagVariants.isEmpty()) {
                        return
                    }
                    result.withPrefixMatcher(completionPrefix)
                }
            }
        )

    }
}