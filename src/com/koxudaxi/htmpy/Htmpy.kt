package com.koxudaxi.htmpy

import com.intellij.psi.ResolveResult
import com.intellij.psi.util.QualifiedName
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.resolve.PyResolveContext
import com.jetbrains.python.psi.resolve.PyResolveUtil
import com.jetbrains.python.psi.types.TypeEvalContext

const val HTM_HTM_Q_NAME = "htm.htm"

internal fun hasDecorator(pyDecoratable: PyDecoratable, refName: String): Boolean {
    pyDecoratable.decoratorList?.decorators?.mapNotNull { it.callee as? PyReferenceExpression }?.forEach {
        PyResolveUtil.resolveImportedElementQNameLocally(it).forEach { decoratorQualifiedName ->
            if (decoratorQualifiedName == QualifiedName.fromDottedString(refName)) return true
        }
    }
    return false
}


internal fun getContext(pyExpression: PyExpression): TypeEvalContext {
    return TypeEvalContext.codeAnalysis(pyExpression.project, pyExpression.containingFile)
}

internal fun getResolveElements(referenceExpression: PyReferenceExpression, context: TypeEvalContext): Array<ResolveResult> {
    return PyResolveContext.defaultContext().withTypeEvalContext(context).let {
        referenceExpression.getReference(it).multiResolve(false)
    }
}

internal fun multiResolveCalledPyTargetExpression(expression: Any?, functionName: String?, index: Int): List<PyTargetExpression> {
    if (!isCallArgument(expression, functionName, index)) return emptyList()

    val call = (expression as? PyExpression)?.parent?.parent as? PyCallExpression ?: return emptyList()
    val referenceExpression = call.callee as? PyReferenceExpression ?: return emptyList()
    val resolveResults = getResolveElements(referenceExpression, getContext(call))
    return PyUtil.filterTopPriorityResults(resolveResults)
            .asSequence()
            .filterIsInstance(PyTargetExpression::class.java)
            .toList()
}

internal fun isCallArgument(expression: Any?, functionName: String?, index: Int): Boolean {
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

internal fun multiResolveCalledDecoratedFunction(expression: Any?, decoratorQName: String): List<PyFunction> {
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