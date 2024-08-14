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

    override fun registerInjection(
        registrar: MultiHostRegistrar,
        context: PsiElement
    ): PyInjectionUtil.InjectionResult {
        val result = super.registerInjection(registrar, context)
        return if (result === PyInjectionUtil.InjectionResult.EMPTY &&
            context is PsiLanguageInjectionHost &&
            context.getContainingFile() is PyFile &&
            (isViewDomHtm(context) || isHtm(context))
        ) {
            return registerPyElementInjection(registrar, context)
        } else result
    }

    override fun getInjectedLanguage(context: PsiElement): Language? {
        return null
    }


    companion object {
        private fun registerPyElementInjection(
            registrar: MultiHostRegistrar,
            host: PsiLanguageInjectionHost
        ): PyInjectionUtil.InjectionResult {
            val text = host.text
            val stringValue =  (host as? PyStringLiteralExpression)?.stringValue ?: return PyInjectionUtil.InjectionResult.EMPTY
            if (text.length <= 2 ) {
                    return PyInjectionUtil.InjectionResult.EMPTY
            }
            val offset = when (stringValue) {
                text.substring(1, text.lastIndex) -> 0
                text.substring(3, text.lastIndex - 2) -> 2
                else -> return PyInjectionUtil.InjectionResult.EMPTY
            }
            registrar.startInjecting(HtmpyLanguage.INSTANCE)
            registrar.addPlace("", "", host, TextRange(offset,  text.length- offset))
            try {
                registrar.doneInjecting()
            } catch (e: Exception) {
                return PyInjectionUtil.InjectionResult.EMPTY
            }

            Regex("\\{([^}]*)\\}").findAll(text).drop(0).forEach {
                registrar.startInjecting(PyDocstringLanguageDialect.getInstance())
                registrar.addPlace("", "", host,  TextRange(it.range.first + 1, it.range.last))
                try {
                    registrar.doneInjecting()
                } catch (e: Exception) {
                    return PyInjectionUtil.InjectionResult.EMPTY
                }
            }
            return PyInjectionUtil.InjectionResult(true, true)
        }
    }
}
