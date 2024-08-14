package com.koxudaxi.htmpy

import com.intellij.lang.Language
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.FileViewProvider
import com.intellij.psi.FileViewProviderFactory
import com.intellij.psi.PsiManager


class HtmpyFielViewProviderFactory : FileViewProviderFactory {
    override fun createFileViewProvider(virtualFile: VirtualFile,
                                        language: Language,
                                        psiManager: PsiManager,
                                        eventSystemEnabled: Boolean): FileViewProvider {
        assert(language.isKindOf(HtmpyLanguage.INSTANCE))
        return HtmpyFileViewProvider(psiManager, virtualFile, eventSystemEnabled, language)
    }
}