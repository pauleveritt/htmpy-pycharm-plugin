// This is a generated file. Not intended for manual editing.
package com.koxudaxi.htmpy.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.koxudaxi.htmpy.psi.HtmpyTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.koxudaxi.htmpy.psi.*;
import com.jetbrains.python.psi.PyElement;

public class HtmpyPythonElementImpl extends ASTWrapperPsiElement implements HtmpyPythonElement {

  public HtmpyPythonElementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull HtmpyVisitor visitor) {
    visitor.visitPythonElement(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HtmpyVisitor) accept((HtmpyVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  public PyElement getPyElement() {
    return HtmpyPsiImplUtil.getPyElement(this);
  }

}
