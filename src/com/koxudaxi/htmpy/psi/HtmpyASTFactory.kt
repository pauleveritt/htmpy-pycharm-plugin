// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.koxudaxi.htmpy.psi

import com.intellij.lang.ASTFactory
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.templateLanguages.OuterLanguageElementImpl
import com.intellij.psi.tree.IElementType
import com.koxudaxi.htmpy.psi.HtmpyTypes.OUTER_ELEMENT_TYPE

class HtmpyASTFactory : ASTFactory() {
    override fun createLeaf(type: IElementType, text: CharSequence): LeafElement? {
        return if (type === OUTER_ELEMENT_TYPE) {
             OuterLanguageElementImpl(type, text)
        } else super.createLeaf(type, text)
    }
}