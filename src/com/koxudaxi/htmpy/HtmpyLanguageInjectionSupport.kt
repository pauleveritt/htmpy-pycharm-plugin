package com.koxudaxi.htmpy

import com.intellij.psi.PsiLanguageInjectionHost
import com.jetbrains.python.psi.PyElement
import org.intellij.plugins.intelliLang.inject.AbstractLanguageInjectionSupport

class HtmpyLanguageInjectionSupport : AbstractLanguageInjectionSupport() {
    override fun getId(): String = SUPPORT_ID

    override fun getPatternClasses(): Array<Class<*>> = arrayOf(HtmpyPatterns::class.java)

    override fun isApplicableTo(host: PsiLanguageInjectionHost): Boolean = host is PyElement

    override fun getHelpId(): String? = "reference.settings.language.injection.generic.htmpy"

    companion object {
        private const val SUPPORT_ID: String = "htmpy"
    }
}