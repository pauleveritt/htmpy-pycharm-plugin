// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.koxudaxi.htmpy

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.util.Pair
import com.intellij.psi.tree.IElementType
import com.koxudaxi.htmpy.parsing.HtmpyLexerAdapter
import com.koxudaxi.htmpy.psi.HtmpyTypes.CLOSE
import com.koxudaxi.htmpy.psi.HtmpyTypes.OPEN
import java.util.*


class HtmpyHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer {
        return HtmpyLexerAdapter()
    }

    companion object {
        private val keys1: MutableMap<IElementType, TextAttributesKey> = HashMap()
        private val keys2: Map<IElementType, TextAttributesKey> = HashMap()
        private val MUSTACHES = TextAttributesKey.createTextAttributesKey(
                "HTMPY.MUSTACHES",
                DefaultLanguageHighlighterColors.BRACES
        )
        private val COMMENTS = TextAttributesKey.createTextAttributesKey(
                "HTMPY.COMMENTS",
                DefaultLanguageHighlighterColors.BLOCK_COMMENT
        )


        val DISPLAY_NAMES: MutableMap<TextAttributesKey, Pair<String, HighlightSeverity?>> = LinkedHashMap()

        init {
            keys1[OPEN] = MUSTACHES
            keys1[CLOSE] = MUSTACHES
        }

        init {
//            DISPLAY_NAMES[MUSTACHES] = Pair("abc", null)
          }
    }

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return pack(keys1[tokenType], keys2[tokenType])
    }
}