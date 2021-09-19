// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.koxudaxi.htmpy

import com.intellij.icons.AllIcons
import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.CharsetUtil
import com.intellij.openapi.fileTypes.EditorHighlighterProvider
import com.intellij.openapi.fileTypes.FileTypeEditorHighlighterProviders
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.TemplateLanguageFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings
import org.jetbrains.annotations.NonNls
import java.nio.charset.Charset
import javax.swing.Icon

class HtmpyFileType protected constructor(lang: Language) : LanguageFileType(lang), TemplateLanguageFileType {
    private constructor() : this(HtmpyLanguage.INSTANCE)

    init{
        FileTypeEditorHighlighterProviders.INSTANCE.addExplicitExtension(this,
                EditorHighlighterProvider { project, fileType, virtualFile, editorColorsScheme -> HtmpyTemplateHighlighter(project, virtualFile, editorColorsScheme)
                }
        )
    }
    override fun getName(): String {
        return "Htmpy"
    }

    override fun getDescription(): String {
        return "HTML template in pyhton"
    }

    override fun getDefaultExtension(): String {
        return DEFAULT_EXTENSION
    }

    override fun getIcon(): Icon {
        return AllIcons.FileTypes.Html
    }

    override fun extractCharsetFromFileContent(project: Project?,
                                               file: VirtualFile?,
                                               content: CharSequence): Charset? {
        val associatedFileType = getAssociatedFileType(file, project) ?: return null
        return CharsetUtil.extractCharsetFromFileContent(project, file, associatedFileType, content)
    }

    companion object {
        val INSTANCE: LanguageFileType = HtmpyFileType()

        @NonNls
        val DEFAULT_EXTENSION = "htmpy"
        private fun getAssociatedFileType(file: VirtualFile?, project: Project?): LanguageFileType? {
            if (project == null) {
                return null
            }
            val language = TemplateDataLanguageMappings.getInstance(project).getMapping(file)
            var associatedFileType: LanguageFileType? = null
            if (language != null) {
                associatedFileType = language.associatedFileType
            }
            if (language == null || associatedFileType == null) {
                associatedFileType = HtmpyLanguage.defaultTemplateLang
            }
            return associatedFileType
        }
    }
}