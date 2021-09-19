// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.koxudaxi.htmpy

import com.intellij.lang.Language
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.lang.ParserDefinition
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.LanguageSubstitutors
import com.intellij.psi.MultiplePsiFilesPerDocumentFileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.PsiFileImpl
import com.intellij.psi.templateLanguages.ConfigurableTemplateLanguageFileViewProvider
import com.intellij.psi.templateLanguages.TemplateDataElementType
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings
import com.intellij.util.containers.ContainerUtil
import com.koxudaxi.htmpy.psi.HtmpyTypes.*
import gnu.trove.THashSet

class HtmpyFileViewProvider @JvmOverloads constructor(manager: PsiManager, file: VirtualFile, physical: Boolean, private val myBaseLanguage: Language, private val myTemplateLanguage: Language = getTemplateDataLanguage(manager, file)) : MultiplePsiFilesPerDocumentFileViewProvider(manager, file, physical), ConfigurableTemplateLanguageFileViewProvider {
    override fun supportsIncrementalReparse(rootLanguage: Language): Boolean {
        return false
    }

    override fun getBaseLanguage(): Language {
        return myBaseLanguage
    }

    override fun getTemplateDataLanguage(): Language {
        return myTemplateLanguage
    }

    override fun getLanguages(): Set<Language> {
        return THashSet(listOf(myBaseLanguage, templateDataLanguage))
    }

    override fun cloneInner(virtualFile: VirtualFile): MultiplePsiFilesPerDocumentFileViewProvider {
        return HtmpyFileViewProvider(manager, virtualFile, false, myBaseLanguage, myTemplateLanguage)
    }

    override fun createFile(lang: Language): PsiFile? {
        val parserDefinition = getDefinition(lang)
        return when {
            lang.`is`(templateDataLanguage) -> {
                val file = parserDefinition.createFile(this) as PsiFileImpl
                file.contentElementType = getTemplateDataElementType(baseLanguage)
                file
            }
            lang.isKindOf(baseLanguage) -> parserDefinition.createFile(this)
            else -> null
        }
    }

    private fun getDefinition(lang: Language): ParserDefinition {
        return when {
            lang.isKindOf(baseLanguage) -> LanguageParserDefinitions.INSTANCE.forLanguage(if (lang.`is`(baseLanguage)) lang else baseLanguage)
            else -> LanguageParserDefinitions.INSTANCE.forLanguage(lang)
        }
    }

    companion object {
        private val TEMPLATE_DATA_TO_LANG = ContainerUtil.createConcurrentSoftMap<String, TemplateDataElementType>()
        private fun getTemplateDataElementType(lang: Language): TemplateDataElementType {
            val result = TEMPLATE_DATA_TO_LANG[lang.id]
            if (result != null) return result
            val created = TemplateDataElementType("HTMPY_TEMPLATE_DATA", lang, CONTENT, OUTER_ELEMENT_TYPE)
            return TEMPLATE_DATA_TO_LANG.putIfAbsent(lang.id, created) ?: created

        }

        private fun getTemplateDataLanguage(manager: PsiManager, file: VirtualFile): Language {
            val dataLang = TemplateDataLanguageMappings.getInstance(manager.project).getMapping(file)
                    ?: HtmpyLanguage.defaultTemplateLang.language

            val substituteLang = LanguageSubstitutors.getInstance().substituteLanguage(dataLang, file, manager.project)

            return when {
                TemplateDataLanguageMappings.getTemplateableLanguages().contains(substituteLang) -> substituteLang
                else -> dataLang
            }
        }
    }
}