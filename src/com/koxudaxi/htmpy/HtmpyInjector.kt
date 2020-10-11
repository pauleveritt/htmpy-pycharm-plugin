package com.koxudaxi.htmpy

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.PsiElement
import com.koxudaxi.htmpy.psi.impl.HtmpyPythonElementImpl


class HtmpyInjector : MultiHostInjector {
    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        // this method is not called.
        throw RuntimeException()
    }

    override fun elementsToInjectIn(): MutableList<out Class<out PsiElement>> {
        return mutableListOf(HtmpyPythonElementImpl::class.java)
    }
}
