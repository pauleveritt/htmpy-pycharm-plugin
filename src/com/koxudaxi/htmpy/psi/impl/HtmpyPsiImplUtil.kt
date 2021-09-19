package com.koxudaxi.htmpy.psi.impl

import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.codeInsight.dataflow.scope.ScopeUtil
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.impl.PyExpressionCodeFragmentImpl
import com.jetbrains.python.psi.impl.PyFileImpl
import com.jetbrains.python.psi.impl.PyStatementListImpl
import com.jetbrains.python.psi.resolve.PyResolveUtil
import com.koxudaxi.htmpy.psi.HtmpyPythonElement

object HtmpyPsiImplUtil {
    @JvmStatic
    fun getPyElement(element: HtmpyPythonElement): PyElement {
        return tryParsePythonElement(element) ?: PyElementGenerator.getInstance(element.project).createPassStatement()
    }

    private fun tryParsePythonElement(element: HtmpyPythonElement): PyElement? {
//        val pyElementNode = element.node.findChildByType(HtmpyTypes.PYTHON_CODE) ?: return null
        val pyElement = PsiTreeUtil.getContextOfType(element, PyCallExpression::class.java) ?: return null
        val pyExpression: PyExpression

        try {
            pyExpression = PyUtil.createExpressionFromFragment(element.node.text, element) ?: return null
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