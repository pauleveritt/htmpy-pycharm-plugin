package com.koxudaxi.htmpy

import com.intellij.lang.Language
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.jetbrains.python.codeInsight.PyInjectionUtil
import com.jetbrains.python.codeInsight.PyInjectorBase
import com.jetbrains.python.documentation.doctest.PyDocstringLanguageDialect
import com.jetbrains.python.psi.*


class HtmpyInjector : PyInjectorBase() {

    override fun registerInjection(registrar: MultiHostRegistrar, context: PsiElement): PyInjectionUtil.InjectionResult {
        val result = super.registerInjection(registrar, context)
        return if (result === PyInjectionUtil.InjectionResult.EMPTY &&
                context is PsiLanguageInjectionHost &&
                context.getContainingFile() is PyFile &&
                (isViewDomHtm(context) || isHtm(context))) {
            return registerPyElementInjection(registrar, context)
        } else result
    }

    override fun getInjectedLanguage(context: PsiElement): Language? {
        return null
    }


    companion object {
        private fun isHtm(context: PsiElement): Boolean {
            return multiResolveCalledDecoratedFunction(context, HTM_HTM_Q_NAME).any()
        }

        private fun isViewDomHtm(context: PsiElement): Boolean {
            val functionQualifiedName = "viewdom.h.html"
            val functionName = functionQualifiedName.split(".").last()
            return multiResolveCalledPyTargetExpression(context, functionName, 0)
                    .filter { it.qualifiedName == functionQualifiedName }.any()
        }

        private fun registerPyElementInjection(registrar: MultiHostRegistrar,
                                               host: PsiLanguageInjectionHost): PyInjectionUtil.InjectionResult {
            val text = host.text
            registrar.startInjecting(HtmpyLanguage.INSTANCE)
            registrar.addPlace("", "", host, TextRange(0, text.length))
            registrar.doneInjecting()

            Regex("\\{([^}]*)\\}").findAll(text).drop(0).forEach {
                registrar.startInjecting(PyDocstringLanguageDialect.getInstance())
                registrar.addPlace("", "", host, TextRange(it.range.first + 1, it.range.last))
                registrar.doneInjecting()
            }

            return PyInjectionUtil.InjectionResult(true, true)
        }
    }
}
