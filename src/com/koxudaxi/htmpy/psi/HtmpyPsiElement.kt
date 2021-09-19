package com.koxudaxi.htmpy.psi

import com.intellij.psi.PsiElement

interface HtmpyPsiElement : PsiElement {
    fun getName(): String?
}