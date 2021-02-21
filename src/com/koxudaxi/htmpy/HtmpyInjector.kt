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
            registrar.startInjecting(HtmpyLanguage.INSTANCE)
            registrar.addPlace("", "", host, TextRange(0,  host.text.length))
            try {
                registrar.doneInjecting()
            } catch (e: Exception) {
                return PyInjectionUtil.InjectionResult.EMPTY
            }

            collectComponents(host,
                actionForComponent = { resolvedComponent, tag, component, keys ->
                    val componentRange = component.destructured.match.range
                    val parameters = keys.mapNotNull { (name, key) ->
                        val keyValue = key.destructured.toList()
                        val value = keyValue[1]
                        if (key.destructured.toList().size > 1) {
                            Pair(name, value)
                        } else {
                            null
                        }
                    }.toMap()
                    parameters.forEach { (name, value) ->
                        val key = keys[name]!!
                        val keyRangeFirst =
                            tag.range.first + componentRange.first + key.destructured.match.range.first + name.length
                        Regex("\\{([^}]*)\\}").findAll(value).forEach {
                            if (it.value.length > 2) {
                                registrar.startInjecting(PyDocstringLanguageDialect.getInstance())
                                registrar.addPlace(
                                    "",
                                    "",
                                    host,
                                    TextRange(keyRangeFirst + it.range.first + 1, keyRangeFirst + it.range.last + 1)
                                )
                                registrar.doneInjecting()
                            }
                        }
                    }
                },
                actionForTag = { _, first, last ->
                    registrar.startInjecting(PyDocstringLanguageDialect.getInstance())
                    registrar.addPlace("", "", host, TextRange(first, last))
                    registrar.doneInjecting()
                }
            )
            return PyInjectionUtil.InjectionResult(true, true)
        }
    }
}
