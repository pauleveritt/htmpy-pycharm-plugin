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
                        if (key.destructured.toList().size > 1) {
                            Pair(name, keyValue[1])
                        } else {
                            null
                        }
                    }.toMap()
                    parameters.forEach { (name, value) ->
                        val key = keys[name]!!
                        val keyRangeFirst =
                            tag.range.first + componentRange.first + key.destructured.match.range.first + name.length
                        val otherParameters =
                            parameters
                                .filter { (k, v) -> k != name && v.isNotEmpty() &&  host.text[0] != v[0]}
                                .map { (k, v) -> "${k}=${v}," }.joinToString(" ")
                        if (host.textLength > keyRangeFirst + value.length && host.text[0] != host.text[keyRangeFirst]) {
                            registrar.startInjecting(PyDocstringLanguageDialect.getInstance())
                            registrar.addPlace(
                                "${resolvedComponent.name}(${otherParameters}${name}=",
                                ")",
                                host,
                                TextRange(keyRangeFirst, keyRangeFirst + value.length)
                            )
                            registrar.doneInjecting()
                        }
                    }
                },
                actionForTag = { tag, first, last ->
                    registrar.startInjecting(PyDocstringLanguageDialect.getInstance())
                    registrar.addPlace("", "", host, TextRange(first, last))
                    registrar.doneInjecting()
                }
            )
            return PyInjectionUtil.InjectionResult(true, true)
        }
    }
}
