package com.koxudaxi.htmpy.psi

import com.intellij.psi.tree.IElementType
import com.koxudaxi.htmpy.HtmpyLanguage
import org.jetbrains.annotations.NonNls


class HtmpyTokenType(@NonNls debugName: String) : IElementType(debugName, HtmpyLanguage.INSTANCE) {
    override fun toString(): String {
        return "HtmpyTokenType." + super.toString()
    }
}
