package com.koxudaxi.htmpy

import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.ex.util.LayerDescriptor
import com.intellij.openapi.editor.ex.util.LayeredLexerEditorHighlighter
import com.intellij.openapi.fileTypes.FileTypes.PLAIN_TEXT
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings
import com.jetbrains.python.PythonFileType
import com.koxudaxi.htmpy.psi.HtmpyTypes.*


class HtmpyTemplateHighlighter(project: Project?, virtualFile: VirtualFile?, colors: EditorColorsScheme) : LayeredLexerEditorHighlighter(HtmpyHighlighter(), colors) {
    init {
        val languageFileType = when {
            project == null || virtualFile == null -> PLAIN_TEXT
            else -> TemplateDataLanguageMappings.getInstance(project).getMapping(virtualFile)?.associatedFileType
                    ?: HtmpyLanguage.defaultTemplateLang
        }
        SyntaxHighlighterFactory.getSyntaxHighlighter(languageFileType, project, virtualFile)?.let {
            registerLayer(CONTENT, LayerDescriptor(it, ""))
        }

        SyntaxHighlighterFactory.getSyntaxHighlighter(PythonFileType.INSTANCE, project, virtualFile)?.let {
            registerLayer(PYTHON_ELEMENT, LayerDescriptor(it, ""))
        }
    }
}