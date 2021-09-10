package com.koxudaxi.htmpy.psi

import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.FileContextUtil
import com.jetbrains.python.psi.*
import com.koxudaxi.htmpy.collectComponents
import com.koxudaxi.htmpy.getContextForCodeCompletion
import com.koxudaxi.htmpy.isHtmpy

class HtmpyReferenceProvider : PsiReferenceProvider() {
    class HtmpyElementPsiReference(val htmpyElement: PsiElement, val range: TextRange, val target: PyElement?) :
        PsiReference {
        override fun getElement(): PsiElement {
            return htmpyElement
        }

        override fun getRangeInElement(): TextRange {
            return range
        }

        override fun resolve(): PsiElement? {
            return target
        }

        override fun getCanonicalText(): String {
            return (target as? PyQualifiedNameOwner)?.qualifiedName
                ?: target?.text
                ?: range.substring(htmpyElement.text)
        }

        override fun handleElementRename(newElementName: String): PsiElement {
            TODO("Not yet implemented")
        }

        override fun bindToElement(element: PsiElement): PsiElement {
            TODO("Not yet implemented")
        }

        override fun isReferenceTo(element: PsiElement): Boolean {
            TODO("Not yet implemented")
        }

        override fun isSoft(): Boolean {
            return true
        }

    }

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val hostElement =
            element.parent.containingFile.getUserData(FileContextUtil.INJECTED_IN_ELEMENT)?.element as? PyElement
                ?: return listOf<PsiReference>().toTypedArray()
        if (!isHtmpy(hostElement)) return listOf<PsiReference>().toTypedArray()
        val typeContext = getContextForCodeCompletion(hostElement)
        val results = mutableListOf<PsiReference>()
        collectComponents(hostElement, { resolvedComponent, tag, _, keys ->
            if (tag.range.contains(element.textOffset)) {
                when (resolvedComponent) {
                    is PyClass -> {
                        keys.forEach { (key, result) ->
                            val valueResult = result.groups.first()
                            if (valueResult != null) {
                                results.add(HtmpyElementPsiReference(element, TextRange(0, key.length), resolvedComponent.findClassAttribute(key, true, typeContext)))
                            }

                        }
//                        resolvedComponent.classAttributes.filter { instanceAttribute ->
//                            !instanceAttribute.hasAssignedValue() && !keys.contains(instanceAttribute.name)
//                        }
//                            .mapNotNull { validKey -> validKey.name }
//                            .forEach { name ->
//                                val attribute = resolvedComponent.findClassAttribute(name, true, null)
//                                if (attribute != null) {
//
//                                    val element = PrioritizedLookupElement.withGrouping(
//                                        LookupElementBuilder
//                                            .createWithSmartPointer("$name=", attribute)
//                                            .withTypeText(typeContext.getType(attribute)?.name)
//                                            .withIcon(AllIcons.Nodes.Field), 1
//                                    )
////                                    result.addElement(PrioritizedLookupElement.withPriority(element, 100.0))
//                                }
//                            }
                    }
                    is PyFunction -> {
                        resolvedComponent.parameterList.parameters.filter { parameter ->
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
//                                    result.addElement(PrioritizedLookupElement.withPriority(element, 100.0))
                                }
                            }
                    }
                }
            }
        }, { _, _, _, _ -> {} })
        return results.toTypedArray()
    }
}