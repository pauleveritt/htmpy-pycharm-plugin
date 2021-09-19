package com.koxudaxi.htmpy.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.impl.PsiFileEx
import com.koxudaxi.htmpy.HtmpyFileType
import com.koxudaxi.htmpy.HtmpyLanguage

class HtmpyPsiFile @JvmOverloads constructor(viewProvider: FileViewProvider, lang: Language? = HtmpyLanguage.INSTANCE) : PsiFileBase(viewProvider, lang!!), PsiFileEx {
    override fun getFileType(): FileType {
        return HtmpyFileType.INSTANCE
    }

    override fun toString(): String {
        return "HtmpyFile:$name"
    }
}