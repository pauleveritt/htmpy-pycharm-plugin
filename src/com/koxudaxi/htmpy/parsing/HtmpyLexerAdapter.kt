package com.koxudaxi.htmpy.parsing
import com.intellij.lexer.FlexAdapter
import com.koxudaxi.htmpy._HtmpyLexer

class HtmpyLexerAdapter : FlexAdapter(_HtmpyLexer(null))
