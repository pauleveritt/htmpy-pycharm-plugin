package com.koxudaxi.htmpy.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.FileContextUtil
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.resolve.PyResolveUtil
import com.koxudaxi.htmpy.collectComponents
import com.koxudaxi.htmpy.getContextForCodeCompletion
import com.koxudaxi.htmpy.getTaggedActualValue
import com.koxudaxi.htmpy.isHtmpy

class HtmpyReferenceProvider : PsiReferenceProvider() {
    class HtmpyElementPsiReference(
        val htmpyElement: PsiElement,
        val range: TextRange,
        var target: PsiElement?,
        val reference: PyReferenceExpression? = null,
    ) :
        PsiReference {

        constructor(htmpyElement: PsiElement, range: TextRange, reference: PyReferenceExpression) : this(htmpyElement,
            range,
            null,
            reference)

        override fun getElement(): PsiElement {
            return htmpyElement
        }

        override fun getRangeInElement(): TextRange {
            return range
        }

        override fun resolve(): PsiElement? {
            if (reference is PyReferenceExpression) {
                return reference.reference.resolve() ?: PyResolveUtil.resolveLocally(reference).firstOrNull()
            }
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
            target = element
            return element
        }

        override fun isReferenceTo(element: PsiElement): Boolean {
            return this.resolve() == element
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
        collectComponents(hostElement, { resolvedComponent, tag, components, keys ->
            if (tag.range.contains(element.textOffset)) {
                if (resolvedComponent is PyClass || resolvedComponent is PyFunction) {
                    keys.forEach { (name, key) ->
                        if (tag.range.first + key.range.first == (element.textRange.startOffset + 1)) {
                            val valueResult = key.groups.first()
                            if (valueResult != null) {
                                val parameter = when (resolvedComponent) {
                                    is PyClass -> resolvedComponent.findClassAttribute(name, true, typeContext)
                                    else -> (resolvedComponent as PyFunction).parameterList.findParameterByName(name)
                                }
                                results.add(HtmpyElementPsiReference(
                                    element,
                                    TextRange(0, name.length),
                                    parameter,
                                ))
                                val value = key.destructured.component2()
                                if (value.isNotEmpty()) {
                                    val actualValue = getTaggedActualValue(value)
                                    val valueExpression =
                                        PyUtil.createExpressionFromFragment(actualValue, hostElement)
                                    if (valueExpression is PyReferenceExpression) {
                                        val valueStart = key.groups[2]!!.range.first - key.groups[0]!!.range.first + 1
                                        results.add(HtmpyElementPsiReference(element,
                                            TextRange(valueStart, valueStart + valueExpression.text.length),
                                            valueExpression))
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }, { _, _, _, _ -> },
            { resolvedComponent, tag, _, _ ->
                if (tag.range.contains(element.textOffset)) {
                    val tagGroup = tag.groups[0]!!
                    results.add(HtmpyElementPsiReference(
                        element,
                        TextRange(tag.range.first + tagGroup.range.first, tagGroup.value.length),
                        resolvedComponent,
                    ))
                }
            })
        return results.toTypedArray()
    }
}