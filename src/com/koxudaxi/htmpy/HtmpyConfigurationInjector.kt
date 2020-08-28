package com.koxudaxi.htmpy

import com.intellij.lang.Language
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.PsiElement
import com.jetbrains.python.codeInsight.PyInjectorBase
import com.jetbrains.python.psi.PyElement
import org.intellij.plugins.intelliLang.Configuration
import org.intellij.plugins.intelliLang.inject.InjectedLanguage
import org.intellij.plugins.intelliLang.inject.InjectorUtils

class HtmpyConfigurationInjector : PyInjectorBase() {
    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        if (context !is PyElement) return
        val result = registerInjection(registrar, context)
        if (!result.isStrict) {
            getInjectedLanguage(context)?.let {
                InjectorUtils.putInjectedFileUserData(context, it, InjectedLanguageManager.FRANKENSTEIN_INJECTION, true)
            }
        }
    }

    override fun getInjectedLanguage(context: PsiElement): Language? {
        for (support in InjectorUtils.getActiveInjectionSupports()) {
            if (support is HtmpyLanguageInjectionSupport) {
                val configuration = Configuration.getInstance()
                for (injection in configuration.getInjections(support.getId())) {
                    if (injection.acceptsPsiElement(context)) {
                        return InjectedLanguage.findLanguageById(injection.injectedLanguageId)
                    }
                }
            }
        }
        return null
    }
}