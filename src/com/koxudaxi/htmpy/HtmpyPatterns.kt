package com.koxudaxi.htmpy

import com.intellij.patterns.InitialPatternCondition
import com.intellij.psi.ResolveResult
import com.intellij.util.ProcessingContext
import com.jetbrains.python.patterns.PyElementPattern
import com.jetbrains.python.patterns.PythonPatterns
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.resolve.PyResolveContext
import com.jetbrains.python.psi.types.TypeEvalContext

object HtmpyPatterns : PythonPatterns() {
    @JvmStatic
    fun pyHtmDecoratedFunctionArgument(): PyElementPattern.Capture<PyExpression?> {
        return PyElementPattern.Capture(object : InitialPatternCondition<PyExpression>(PyExpression::class.java) {
            override fun accepts(o: Any?, context: ProcessingContext): Boolean {
                return multiResolveCalledDecoratedFunction(o, HTM_HTM_Q_NAME).any()
            }
        })
    }

    @JvmStatic
    fun pyReferencedModuleFunctionArgument(functionQualifiedName: String, index: Int): PyElementPattern.Capture<PyExpression?> {
        val functionName = functionQualifiedName.split(".").last()
        return PyElementPattern.Capture(object : InitialPatternCondition<PyExpression>(PyExpression::class.java) {
            override fun accepts(o: Any?, context: ProcessingContext): Boolean {
                return multiResolveCalledPyTargetExpression(o, functionName, index)
                        .filter { it.qualifiedName == functionQualifiedName }.any()
            }
        })
    }

    private fun getContext(pyExpression: PyExpression): TypeEvalContext {
        return TypeEvalContext.codeAnalysis(pyExpression.project, pyExpression.containingFile)
    }

    private fun getResolveElements(referenceExpression: PyReferenceExpression, context: TypeEvalContext): Array<ResolveResult> {
        return PyResolveContext.defaultContext().withTypeEvalContext(context).let {
            referenceExpression.getReference(it).multiResolve(false)
        }
    }

    private fun multiResolveCalledPyTargetExpression(expression: Any?, functionName: String?, index: Int): List<PyTargetExpression> {
        if (!isCallArgument(expression, functionName, index)) return emptyList()

        val call = (expression as? PyExpression)?.parent?.parent as? PyCallExpression ?: return emptyList()
        val referenceExpression = call.callee as? PyReferenceExpression ?: return emptyList()
        val resolveResults = getResolveElements(referenceExpression, getContext(call))
        return PyUtil.filterTopPriorityResults(resolveResults)
                .asSequence()
                .filterIsInstance(PyTargetExpression::class.java)
                .toList()
    }

    private fun isCallArgument(expression: Any?, functionName: String?, index: Int): Boolean {
        if (expression !is PyExpression) return false

        val argumentList = expression.parent as? PyArgumentList ?: return false
        val call = argumentList.parent as? PyCallExpression ?: return false
        val referenceToCallee = call.callee as? PyReferenceExpression ?: return false
        val referencedName = referenceToCallee.referencedName ?: return false
        if (referencedName != functionName) return false
        argumentList.children.let { arguments ->
            return index < arguments.size && expression === arguments[index]
        }
    }

    private fun multiResolveCalledDecoratedFunction(expression: Any?, decoratorQName: String): List<PyFunction> {
        if (expression !is PyExpression) return emptyList()

        val argumentList = expression.parent as? PyArgumentList ?: return emptyList()
        val call = argumentList.parent as? PyCallExpression ?: return emptyList()
        val referenceToCallee = call.callee as? PyReferenceExpression ?: return emptyList()
        val resolveResults = getResolveElements(referenceToCallee, getContext(expression))
        return PyUtil.filterTopPriorityResults(resolveResults)
                .asSequence()
                .filterIsInstance(PyFunction::class.java)
                .filter { hasDecorator(it, decoratorQName) }
                .toList()

    }
}

