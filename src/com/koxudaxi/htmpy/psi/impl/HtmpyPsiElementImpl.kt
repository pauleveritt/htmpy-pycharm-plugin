package com.koxudaxi.htmpy.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.ItemPresentationProviders
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.koxudaxi.htmpy.psi.HtmpyPsiElement

open class HtmpyPsiElementImpl(astNode: ASTNode) : ASTWrapperPsiElement(astNode), HtmpyPsiElement {
    override fun getPresentation(): ItemPresentation? {
        return ItemPresentationProviders.getItemPresentation(this)
    }

    override fun getReferences(): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this)
    }
}