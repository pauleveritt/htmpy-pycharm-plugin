package com.koxudaxi.htmpy.psi.impl

import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.codeInsight.dataflow.scope.ScopeUtil
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.impl.PythonLanguageLevelPusher
import com.jetbrains.python.psi.resolve.PyResolveUtil
import com.koxudaxi.htmpy.psi.HtmpyPythonElement
import com.koxudaxi.htmpy.psi.HtmpyTypes

object HtmpyPsiImplUtil {
    @JvmStatic
    fun getPyElement(element: HtmpyPythonElement): PyElement {
        return tryParsePythonElement(element) ?: PyElementGenerator.getInstance(element.project).createPassStatement()
    }

    private fun tryParsePythonElement(element: HtmpyPythonElement): PyElement? {
        val pyElementNode = element.node.findChildByType(HtmpyTypes.PYTHON_CODE) ?: return null
        val pyElement = PsiTreeUtil.getContextOfType(element, PyElement::class.java) ?: return null
        val pyExpression: PyExpression
        try {
            pyExpression = PyElementGenerator.getInstance(element.project).createExpressionFromText(PythonLanguageLevelPusher.getLanguageLevelForVirtualFile(element.project, element.containingFile.virtualFile), pyElementNode.text)
        } catch (e: Exception) {
            return null
        }
        if (pyExpression is PyReferenceExpression) {
            return resolvePyReferenceExpression(pyExpression, pyElement)
        }
        resolveAllPyReferenceExpression(pyExpression, pyElement)
        return pyExpression
    }

    private fun resolveAllPyReferenceExpression(pyExpression: PyExpression, pyElement: PyElement) {
        pyExpression
                .children
                .filterIsInstance<PyReferenceExpression>()
                .filter { it.context == null }
                .forEach {
                    resolvePyReferenceExpression(it, pyElement)?.let { resolved -> it.replace(resolved) }
                    resolveAllPyReferenceExpression(it, pyElement)
                }
    }

    private fun resolvePyReferenceExpression(pyReferenceExpression: PyReferenceExpression, pyElement: PyElement): PyElement? {
        val name = pyReferenceExpression.name ?: return null
        val owner = ScopeUtil.getScopeOwner(pyElement) ?: return null
        return PyResolveUtil.resolveLocally(owner, name).filterIsInstance<PyElement>().firstOrNull()
    }
}