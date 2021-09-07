package com.koxudaxi.htmpy

import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.LanguageSubstitutor
import com.koxudaxi.htmpy.HtmpyLanguage.Companion.INSTANCE

class HtmpyLanguageSubstitutor : LanguageSubstitutor() {
    override fun getLanguage(file: VirtualFile, project: Project): Language {
        return INSTANCE
    }
}