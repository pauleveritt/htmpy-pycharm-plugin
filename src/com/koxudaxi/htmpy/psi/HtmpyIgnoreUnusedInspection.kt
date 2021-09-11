package com.koxudaxi.htmpy.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspectionExtension
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.resolve.PyResolveUtil
import com.jetbrains.python.psi.types.TypeEvalContext
import com.koxudaxi.htmpy.*

class HtmpyIgnoreUnusedInspection : PyInspectionExtension() {
    override fun ignoreUnused(element: PsiElement, evalContext: TypeEvalContext): Boolean {
        if (element.containingFile !is PyFile) {
            return false
        }
        if (element !is PyTargetExpression) {
            return false
        }
        var result = false
        PsiTreeUtil.findChildrenOfType(element.parent.parent, PyStringLiteralExpression::class.java)?.forEach {
            if (isHtmpy(it)) {
                collectComponents(it, { _, tag, component, keys ->
                    val actualComponent =
                        PyUtil.createExpressionFromFragment(component.value.substring(IntRange(1,
                            component.value.length - 2)),
                            element)
                    val resolvedElement = (actualComponent as? PyReferenceExpression)?.let { reference ->
                        PyResolveUtil.resolveLocally(reference).firstOrNull()
                    }
                    if (resolvedElement == element) {
                        result = true
                        return@collectComponents
                    }
                    keys.forEach { (_, key) ->
                        val valueResult = key.groups.first()
                        if (valueResult != null) {
                            val value = key.destructured.component2()
                            if (value.isNotEmpty()) {
                                val actualValue = getTaggedActualValue(value)
                                val valueExpression =
                                    PyUtil.createExpressionFromFragment(actualValue, element)
                                if (valueExpression is PyReferenceExpression) {
                                    val resolvedValue = PyResolveUtil.resolveLocally(valueExpression).firstOrNull()
                                    if (resolvedValue == element) {
                                        result = true
                                        return@collectComponents
                                    }
                                }
                            }
                        }
                    }
                },
                    { _, _, _, _ -> })
            }
        }
        return result
    }
}
