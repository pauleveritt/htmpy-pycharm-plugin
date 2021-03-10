// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.koxudaxi.htmpy
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider


class HtmpyFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, HtmpyLanguage.INSTANCE) {
    override fun getFileType(): FileType {
        return HtmpyFileType.INSTANCE
    }

    override fun toString(): String {
        return "Htmpy File"
    }

}
