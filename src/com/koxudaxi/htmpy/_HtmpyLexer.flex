package com.koxudaxi.htmpy;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.*;import static com.koxudaxi.htmpy.psi.HtmpyTypes.*;
import com.intellij.util.containers.Stack;

%%

%{
  public _HtmpyLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class _HtmpyLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode
%state mu
%state emu
%state raw
%state comment
%state comment_end
%state comment_block
%state python_code
%state python_code_end
%{
// This parser was copied from https://github.com/dmarcotte/idea-handlebars
    private Stack<Integer> stack = new Stack<>();

    public void yypushState(int newState) {
      stack.push(yystate());
      yybegin(newState);
    }

    public void yypopState() {
      yybegin(stack.pop());
    }
%}

LineTerminator = \r|\n|\r\n
WhiteSpace = {LineTerminator} | [ \t\f]


%%
<YYINITIAL> {
~"{" {
          // backtrack over any stache characters at the end of this string
          while (yylength() > 0 && yytext().subSequence(yylength() - 1, yylength()).toString().equals("{")) {
            yypushback(1);
          }

          // inspect the characters leading up to this mustache for escaped characters
          if (yylength() > 1 && yytext().subSequence(yylength() - 2, yylength()).toString().equals("\\\\")) {
            return CONTENT; // double-slash is just more content
          } else if (yylength() > 0 && yytext().toString().substring(yylength() - 1, yylength()).equals("\\")) {
            yypushback(1); // put the escape char back
            yypushState(emu);
          } else {
            yypushState(mu);
          }

          // we stray from the handlebars.js lexer here since we need our WHITE_SPACE more clearly delineated
          //    and we need to avoid creating extra tokens for empty strings (makes the parser and formatter happier)
          if (!yytext().toString().equals("")) {
              if (yytext().toString().trim().length() == 0) {
                  return WHITE_SPACE;
              } else {
                  return CONTENT;
              }
          }
        }

  // Check for anything that is not a string containing "{{"; that's CONTENT
  !([^]*"{"[^]*)                         { return CONTENT; }
}

<emu> {
    "\\" { return ESCAPE_CHAR; }
    "{"~"{" { // grab everything up to the next open stache
          // backtrack over any stache characters or escape characters at the end of this string
          while (yylength() > 0
                  && (yytext().subSequence(yylength() - 1, yylength()).toString().equals("{")
                      || yytext().subSequence(yylength() - 1, yylength()).toString().equals("\\"))) {
            yypushback(1);
          }

          yypopState();
          return CONTENT;
    }
    "{"!([^]*"{"[^]*) { // otherwise, if the remaining text just contains the one escaped mustache, then it's all CONTENT
        return CONTENT;
    }
}

<raw> {
   ~"{/" {
             // backtrack over the END_RAW_BLOCK we picked up at the end of this string
             yypushback(5);

             yypopState();

             // we stray from the handlebars.js lexer here since we need our WHITE_SPACE more clearly delineated
             //    and we need to avoid creating extra tokens for empty strings (makes the parser and formatter happier)
             if (!yytext().toString().equals("")) {
                 if (yytext().toString().trim().length() == 0) {
                     return WHITE_SPACE;
                 } else {
                     return CONTENT;
                 }
             }
           }

   // Check for anything that is not a string containing "{{{{/"; that's CONTENT
   !([^]*"{/"[^]*)                         { return CONTENT; }
}

<mu> {
  // NOTE: the standard Handlebars lexer would checks for "{{^}}" and "{{else}}" here and lexes the simple inverse directly.
  // We lex it in pieces and identify simple inverses in the parser
  // (this also allows us to  highlight "{{" and the "else" independently.  See where we make an HbTokens.ELSE below)
//  "{"\~?"^" { return OPEN_INVERSE; }
//  "{"\~?"{" { return OPEN_UNESCAPED; }
//  "{"\~?"&" { return OPEN; }
//  "{!" { yypopState(); yypushState(comment); return COMMENT_OPEN; }
//  "{!--" { yypopState(); yypushState(comment_block); return COMMENT_OPEN; }

  "{"\~? {  yypopState(); yypushState(python_code); return OPEN; }
//  "."/[\~\}\t| \n\x0B\f\r] { return PYTHON_CODE; }
//  ".." { return PYTHON_CODE; }
//  [\/.] { return SEP; }
//  [\t \n\x0B\f\r]* { return WHITE_SPACE; }
  "}"\~?"}" { yypopState(); return CLOSE_UNESCAPED; }
  \~?"}" { yypopState(); return CLOSE; }
  /*
    ID is the inverse of control characters.
    Control characters ranges:
      [\\t \n\x0B\f\r]          Whitespace
      [!"#%-,\./]   !, ", #, %, &, ', (, ), *, +, ,, ., /,  Exceptions in range: $, -
      [;->@]        ;, <, =, >, @,                          Exceptions in range: :, ?
      [\[-\^`]      [, \, ], ^, `,                          Exceptions in range: _
      [\{-~]        {, |, }, ~
    */
//  [^\t \n\x0B\f\r!\"#%-,\.\/;->@\[-\^`\{-~]+/[\~=}\)|\t \n\x0B\f\r\/.]   { return PYTHON_CODE; }
  // TODO handlesbars.l extracts the id from within the square brackets.  Fix it to match handlebars.l?
//  "["[^\]]*"]" { return PYTHON_CODE; }
}

<python_code> {
  "}" { yypopState(); return CLOSE; }
  ~"}" {
      // backtrack over any extra stache characters at the end of this string
      while (yylength() > 2 && yytext().subSequence(yylength() - 3, yylength()).toString().equals("}}}")) {
        yypushback(1);
      }

      yypushback(1);
      yybegin(python_code_end);
      return PYTHON_CODE;
  }
  // lex unclosed comments so that we can give better errors
  !([^]*"}"[^]*) {  return UNCLOSED_COMMENT; }
}


<python_code_end> {
  "}" { yypopState(); return CLOSE; }
}

<comment> {
  "}" { yypopState(); return COMMENT_CLOSE; }
  ~"}" {
      // backtrack over any extra stache characters at the end of this string
      while (yylength() > 2 && yytext().subSequence(yylength() - 3, yylength()).toString().equals("}}}")) {
        yypushback(1);
      }

      yypushback(2);
      yybegin(comment_end);
      return COMMENT_CONTENT;
  }
  // lex unclosed comments so that we can give better errors
  !([^]*"}"[^]*) {  return UNCLOSED_COMMENT; }
}

<comment_block> {
  ~"--}" { yypushback(4); yybegin(comment_end); return COMMENT_CONTENT; }

   // lex unclosed comments so that we can give better errors
  !([^]*"--}"[^]*) { yypopState(); return UNCLOSED_COMMENT; }
}
<comment_end> {
  "}" |
  "--}" { yypopState(); return COMMENT_CLOSE; }
}

{WhiteSpace}+ { return WHITE_SPACE; }
. { return INVALID; }
