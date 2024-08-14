package com.koxudaxi.htmpy.parsing

import com.intellij.psi.tree.IElementType
import com.koxudaxi.htmpy.HtmpyLanguage
import org.jetbrains.annotations.NonNls

internal class HtmpyElementType(@NonNls debugName: String) : IElementType(debugName, HtmpyLanguage.INSTANCE) {
    override fun toString(): String {
        return "[HtmPy] " + super.toString()
    }
}