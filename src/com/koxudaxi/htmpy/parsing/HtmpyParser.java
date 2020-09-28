// This is a generated file. Not intended for manual editing.
package com.koxudaxi.htmpy.parsing;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.koxudaxi.htmpy.psi.HtmpyTypes.*;
import static com.koxudaxi.htmpy.parsing.HtmpyParserUtil.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class HtmpyParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return htmpyFile(b, l + 1);
  }

  /* ********************************************************** */
  // item_*
  static boolean htmpyFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "htmpyFile")) return false;
    while (true) {
      int c = current_position_(b);
      if (!item_(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "htmpyFile", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // OPEN|CLOSE|INVALID|COMMENT|COMMENT_CONTENT|CONTENT|OUTER_ELEMENT_TYPE|ESCAPE_CHAR|CLOSE_UNESCAPED|COMMENT_OPEN|OPEN_INVERSE|ID|SEP|OPEN_UNESCAPED|UNCLOSED_COMMENT|COMMENT_CLOSE|OPEN_BLOCK|OPEN_ENDBLOCK|OPEN_PARTIAL|ELSE|BLOCK_WRAPPER|OPEN_RAW_BLOCK|CLOSE_RAW_BLOCK|OPEN_BLOCK_STACHE|OPEN_INVERSE_CHAIN|OPEN_INVERSE_BLOCK_STACHE|MUSTACHE|CLOSE_BLOCK_STACHE|SIMPLE_INVERSE|PARAM|PYTHON_CODE
  static boolean item_(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "item_")) return false;
    boolean r;
    r = consumeToken(b, OPEN);
    if (!r) r = consumeToken(b, CLOSE);
    if (!r) r = consumeToken(b, INVALID);
    if (!r) r = consumeToken(b, COMMENT);
    if (!r) r = consumeToken(b, COMMENT_CONTENT);
    if (!r) r = consumeToken(b, CONTENT);
    if (!r) r = consumeToken(b, OUTER_ELEMENT_TYPE);
    if (!r) r = consumeToken(b, ESCAPE_CHAR);
    if (!r) r = consumeToken(b, CLOSE_UNESCAPED);
    if (!r) r = consumeToken(b, COMMENT_OPEN);
    if (!r) r = consumeToken(b, OPEN_INVERSE);
    if (!r) r = consumeToken(b, ID);
    if (!r) r = consumeToken(b, SEP);
    if (!r) r = consumeToken(b, OPEN_UNESCAPED);
    if (!r) r = consumeToken(b, UNCLOSED_COMMENT);
    if (!r) r = consumeToken(b, COMMENT_CLOSE);
    if (!r) r = consumeToken(b, OPEN_BLOCK);
    if (!r) r = consumeToken(b, OPEN_ENDBLOCK);
    if (!r) r = consumeToken(b, OPEN_PARTIAL);
    if (!r) r = consumeToken(b, ELSE);
    if (!r) r = consumeToken(b, BLOCK_WRAPPER);
    if (!r) r = consumeToken(b, OPEN_RAW_BLOCK);
    if (!r) r = consumeToken(b, CLOSE_RAW_BLOCK);
    if (!r) r = consumeToken(b, OPEN_BLOCK_STACHE);
    if (!r) r = consumeToken(b, OPEN_INVERSE_CHAIN);
    if (!r) r = consumeToken(b, OPEN_INVERSE_BLOCK_STACHE);
    if (!r) r = consumeToken(b, MUSTACHE);
    if (!r) r = consumeToken(b, CLOSE_BLOCK_STACHE);
    if (!r) r = consumeToken(b, SIMPLE_INVERSE);
    if (!r) r = consumeToken(b, PARAM);
    if (!r) r = consumeToken(b, PYTHON_CODE);
    return r;
  }

}
