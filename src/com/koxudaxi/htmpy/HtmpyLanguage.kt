package com.koxudaxi.htmpy

import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.lang.InjectableLanguage
import com.intellij.lang.Language
import com.intellij.psi.templateLanguages.TemplateLanguage
import org.jetbrains.annotations.NonNls

class HtmpyLanguage : Language, TemplateLanguage, InjectableLanguage {
    constructor() : super("Htmpy", "text/x-htmpy-template", "text/x-htmpy")
    constructor(baseLanguage: Language?, @NonNls ID: String, @NonNls vararg mimeTypes: String) : super(baseLanguage, ID, *mimeTypes)

    companion object {
        val INSTANCE = HtmpyLanguage()
        val defaultTemplateLang: HtmlFileType = HtmlFileType.INSTANCE
    }
}