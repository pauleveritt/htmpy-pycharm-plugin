package com.koxudaxi.htmpy.psi

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.resolve.PyResolveUtil
import com.jetbrains.python.psi.types.TypeEvalContext
import com.koxudaxi.htmpy.collectComponents
import com.koxudaxi.htmpy.getContextForCodeCompletion
import com.koxudaxi.htmpy.getTaggedActualValue
import com.koxudaxi.htmpy.isHtmpy

class HtmpyReferenceProvider : PsiReferenceProvider() {
    class HtmpyElementPsiReference(
        element: PyStringLiteralExpression,
        textRange: TextRange,
        private val target: PsiElement?,
        private val reference: PyReferenceExpression? = null,
    ) : PsiReferenceBase<PyStringLiteralExpression>(element, textRange, true), PsiReferenceEx {

        constructor(element: PyStringLiteralExpression, textRange: TextRange, reference: PyReferenceExpression) : this(
            element,
            textRange,
            null,
            reference)

        override fun resolve(): PsiElement? {
            if (reference is PyReferenceExpression) {
                return reference.reference.resolve() ?: PyResolveUtil.resolveLocally(reference).firstOrNull()
            }
            return target
        }

        override fun getUnresolvedHighlightSeverity(context: TypeEvalContext?): HighlightSeverity? {
            return HighlightSeverity.WARNING
        }

        override fun getUnresolvedDescription(): String? {
            return null
        }

    }

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        if (element !is PyStringLiteralExpression) return emptyList<PsiReference>().toTypedArray()
        if (!isHtmpy(element)) return listOf<PsiReference>().toTypedArray()
        val typeContext = getContextForCodeCompletion(element)
        val results = mutableListOf<PsiReference>()
        collectComponents(element, { resolvedComponent, tag, components, keys ->
            if (resolvedComponent is PyClass || resolvedComponent is PyFunction) {
                keys.forEach { (name, key) ->
                    val elementStart = tag.range.first + key.range.first  //element.textRange.startOffset + 1
                        val valueResult = key.groups.first()
                        if (valueResult != null) {
                            val parameter = when (resolvedComponent) {
                                is PyClass -> resolvedComponent.findClassAttribute(name, true, typeContext)
                                else -> (resolvedComponent as PyFunction).parameterList.findParameterByName(name)
                            }
                            results.add(HtmpyElementPsiReference(
                                element,
                                TextRange(elementStart, elementStart + name.length),
                                parameter,
                            ))
                            val value = key.destructured.component2()
                            if (value.isNotEmpty()) {
                                val actualValue = getTaggedActualValue(value).let { if (it.endsWith(".")) { it.substring(0, it.length - 1)} else {it} }
                                val valueExpression =
                                    PyUtil.createExpressionFromFragment(actualValue, element)
                                if (valueExpression is PyReferenceExpression) {
                                    val valueStart =
                                        elementStart + key.groups[2]!!.range.first - key.groups[0]!!.range.first + 1
                                    results.add(HtmpyElementPsiReference(
                                        element,
                                        TextRange(valueStart, valueStart + valueExpression.text.length),
                                        valueExpression))
                                }
                            }
                        }
                }
            }
        }, { _, _, _, _ -> },
            { resolvedComponent, tag, first, _ ->
                val actualComponent =
                    PyUtil.createExpressionFromFragment(tag.value.substring(IntRange(1, tag.value.length - 2)),
                        element)
                if (actualComponent != null) {
                    results.add(HtmpyElementPsiReference(
                        element,
                        TextRange(first, first + actualComponent.text.length),
                        resolvedComponent,
                    ))
                }
            })
        return results.toTypedArray()
    }
}