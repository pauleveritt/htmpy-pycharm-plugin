package com.koxudaxi.htmpy.parsing

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyElementGenerator
import com.koxudaxi.htmpy.HtmpyFile
import com.koxudaxi.htmpy.HtmpyLanguage
import com.koxudaxi.htmpy.psi.HtmpyTypes
import com.koxudaxi.htmpy.psi.HtmpyTypes.PYTHON_ELEMENT
import com.koxudaxi.htmpy.psi.impl.HtmpyPythonElementImpl
import org.jetbrains.annotations.NotNull

class HtmpyParseDefinition : ParserDefinition {

    val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
    val COMMENT = TokenSet.create(HtmpyTypes.COMMENT, HtmpyTypes.COMMENT_CONTENT)
    val FILE = IFileElementType(HtmpyLanguage.INSTANCE)

    override fun createLexer(project: Project?): Lexer {
        return HtmpyLexerAdapter()
    }

    override fun getWhitespaceTokens(): TokenSet {
        return WHITE_SPACES
    }

    override fun getCommentTokens(): TokenSet {
        return COMMENT
    }

    override fun getStringLiteralElements(): TokenSet {
        return TokenSet.EMPTY
    }

    @NotNull
    override fun createParser(project: Project?): PsiParser? {
        return HtmpyParser()
    }

    @NotNull
    override fun getFileNodeType(): IFileElementType? {
        return FILE
    }

    @NotNull
    override fun createFile(viewProvider: FileViewProvider?): PsiFile? {
        return viewProvider?.let { HtmpyFile(it) }
    }

    override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?): ParserDefinition.SpaceRequirements? {
        return ParserDefinition.SpaceRequirements.MAY
    }

    override fun createElement(node: ASTNode): PsiElement {
        if (node.elementType == PYTHON_ELEMENT) {
            return HtmpyPythonElementImpl(node).pyElement
        }
        throw RuntimeException()
    }
}