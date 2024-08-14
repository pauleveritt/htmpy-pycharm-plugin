// This is a generated file. Not intended for manual editing.
package com.koxudaxi.htmpy.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.koxudaxi.htmpy.parsing.HtmpyElementType;
import com.koxudaxi.htmpy.psi.impl.*;

public interface HtmpyTypes {

  IElementType PYTHON_ELEMENT = new HtmpyElementType("PYTHON_ELEMENT");

  IElementType BLOCK_WRAPPER = new HtmpyTokenType("BLOCK_WRAPPER");
  IElementType CLOSE = new HtmpyTokenType("CLOSE");
  IElementType CLOSE_BLOCK_STACHE = new HtmpyTokenType("CLOSE_BLOCK_STACHE");
  IElementType CLOSE_RAW_BLOCK = new HtmpyTokenType("CLOSE_RAW_BLOCK");
  IElementType CLOSE_UNESCAPED = new HtmpyTokenType("CLOSE_UNESCAPED");
  IElementType COMMENT = new HtmpyTokenType("COMMENT");
  IElementType COMMENT_CLOSE = new HtmpyTokenType("COMMENT_CLOSE");
  IElementType COMMENT_CONTENT = new HtmpyTokenType("COMMENT_CONTENT");
  IElementType COMMENT_OPEN = new HtmpyTokenType("COMMENT_OPEN");
  IElementType CONTENT = new HtmpyTokenType("CONTENT");
  IElementType ELSE = new HtmpyTokenType("ELSE");
  IElementType ESCAPE_CHAR = new HtmpyTokenType("ESCAPE_CHAR");
  IElementType ID = new HtmpyTokenType("ID");
  IElementType INVALID = new HtmpyTokenType("INVALID");
  IElementType MUSTACHE = new HtmpyTokenType("MUSTACHE");
  IElementType OPEN = new HtmpyTokenType("OPEN");
  IElementType OPEN_BLOCK = new HtmpyTokenType("OPEN_BLOCK");
  IElementType OPEN_BLOCK_STACHE = new HtmpyTokenType("OPEN_BLOCK_STACHE");
  IElementType OPEN_ENDBLOCK = new HtmpyTokenType("OPEN_ENDBLOCK");
  IElementType OPEN_INVERSE = new HtmpyTokenType("OPEN_INVERSE");
  IElementType OPEN_INVERSE_BLOCK_STACHE = new HtmpyTokenType("OPEN_INVERSE_BLOCK_STACHE");
  IElementType OPEN_INVERSE_CHAIN = new HtmpyTokenType("OPEN_INVERSE_CHAIN");
  IElementType OPEN_PARTIAL = new HtmpyTokenType("OPEN_PARTIAL");
  IElementType OPEN_RAW_BLOCK = new HtmpyTokenType("OPEN_RAW_BLOCK");
  IElementType OPEN_UNESCAPED = new HtmpyTokenType("OPEN_UNESCAPED");
  IElementType OUTER_ELEMENT_TYPE = new HtmpyTokenType("OUTER_ELEMENT_TYPE");
  IElementType PARAM = new HtmpyTokenType("PARAM");
  IElementType PYTHON_CODE = new HtmpyTokenType("PYTHON_CODE");
  IElementType SEP = new HtmpyTokenType("SEP");
  IElementType SIMPLE_INVERSE = new HtmpyTokenType("SIMPLE_INVERSE");
  IElementType UNCLOSED_COMMENT = new HtmpyTokenType("UNCLOSED_COMMENT");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == PYTHON_ELEMENT) {
        return new HtmpyPythonElementImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
