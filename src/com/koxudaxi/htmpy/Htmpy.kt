package com.koxudaxi.htmpy

import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.QualifiedName
import com.jetbrains.python.codeInsight.dataflow.scope.ScopeUtil
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.resolve.PyResolveContext
import com.jetbrains.python.psi.resolve.PyResolveUtil
import com.jetbrains.python.psi.types.PyType
import com.jetbrains.python.psi.types.TypeEvalContext

const val HTM_HTM_Q_NAME = "htm.htm"

const val DATA_CLASS_Q_NAME = "dataclasses.dataclass"

val DATA_CLASS_QUALIFIED_NAME = QualifiedName.fromDottedString(DATA_CLASS_Q_NAME)

val DATA_CLASS_QUALIFIED_NAMES = listOf(
    DATA_CLASS_QUALIFIED_NAME
)

internal fun hasDecorator(pyDecoratable: PyDecoratable, refName: String): Boolean {
    pyDecoratable.decoratorList?.decorators?.mapNotNull { it.callee as? PyReferenceExpression }?.forEach {
        PyResolveUtil.resolveImportedElementQNameLocally(it).forEach { decoratorQualifiedName ->
            if (decoratorQualifiedName == QualifiedName.fromDottedString(refName)) return true
        }
    }
    return false
}


internal fun getContext(pyElement: PyElement): TypeEvalContext {
    return TypeEvalContext.codeAnalysis(pyElement.project, pyElement.containingFile)
}

internal fun getContextForCodeCompletion(pyElement: PyElement): TypeEvalContext {
    return TypeEvalContext.codeCompletion(pyElement.project, pyElement.containingFile)
}

internal fun getResolveElements(
    referenceExpression: PyReferenceExpression,
    context: TypeEvalContext
): Array<ResolveResult> {
    return PyResolveContext.defaultContext().withTypeEvalContext(context).let {
        referenceExpression.getReference(it).multiResolve(false)
    }
}

internal fun multiResolveCalledPyTargetExpression(
    expression: Any?,
    functionName: String?,
    index: Int
): List<PyTargetExpression> {
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

internal fun hasDecorator(pyDecoratable: PyDecoratable, refNames: List<QualifiedName>): Boolean {
    return pyDecoratable.decoratorList?.decorators?.mapNotNull { it.callee as? PyReferenceExpression }?.any {
        PyResolveUtil.resolveImportedElementQNameLocally(it).any { decoratorQualifiedName ->
            refNames.any { refName -> decoratorQualifiedName == refName }
        }
    } ?: false
}

internal fun isDataclass(pyClass: PyClass): Boolean {
    return hasDecorator(pyClass, DATA_CLASS_QUALIFIED_NAMES)
}

fun isHtm(context: PsiElement): Boolean {
    return multiResolveCalledDecoratedFunction(context, HTM_HTM_Q_NAME).any()
}

fun isViewDomHtm(context: PsiElement): Boolean {
    val functionQualifiedName = "viewdom.h.html"
    val functionName = functionQualifiedName.split(".").last()
    return multiResolveCalledPyTargetExpression(context, functionName, 0)
        .filter { it.qualifiedName == functionQualifiedName }.any()
}

fun isHtmpy(psiElement: PsiElement): Boolean = isViewDomHtm(psiElement) || isHtm(psiElement)


fun collectComponents(
    psiElement: PsiElement,
    actionForComponent: (resolvedComponent: PyTypedElement, tag: MatchResult, component: MatchResult, keys: Map<String, MatchResult>) -> Unit,
    actionForEmptyComponent: (resolvedComponent: PyTypedElement, tag: MatchResult, component: MatchResult, keys: Map<String, MatchResult>) -> Unit,
    actionForTag: ((tag: MatchResult, fisrt: Int, last: Int) -> Unit)? = null
) {
    val text = psiElement.text
    Regex("<[^>]*\\{([^}]*)\\}[^/>]*(/\\s*>|.*$)").findAll(text).forEach { element ->
        val tag = Regex("\\{([^}]*)\\}").findAll(element.value).firstOrNull()
        if (tag != null) {
            val owner = ScopeUtil.getScopeOwner(psiElement)
            if (tag.value.length > 2 && owner != null) {
                val component =
                    PyResolveUtil.resolveLocally(owner, tag.destructured.component1()).firstOrNull()
                        .let {
                            when (it) {
                                is PyImportElement -> {
                                    PyUtil.filterTopPriorityResults(it.multiResolve())
                                        .asSequence()
                                        .map { result -> result.element }
                                        .firstOrNull { element ->  when(element) {
                                            is PyClass -> isDataclass(element)
                                            is PyFunction -> true
                                            else -> false
                                            }
                                        }
                                 }
                                else -> (it as? PyClass)?.takeIf { pyClass -> isDataclass(pyClass) } ?: (it as? PyFunction)
                            }
                        }

                if (component is PyTypedElement) {
                    val keys =
                        (Regex("([^=}\\s]+)=([^\\s/]*)").findAll(element.value)
                                + Regex("([^=}\\s]+)=\"([^\"]*)").findAll(element.value)
                                + Regex("([^=}\\s]+)=(\\{[^\"]*\\})").findAll(element.value))
                            .map { key ->
                                Pair(key.destructured.component1(), key)
                            }.toMap()
                    actionForComponent(component, element, tag, keys)

                    val emptyKeys = Regex("([^=}\\s]+)=(\\s+)").findAll(element.value).map { key ->
                        Pair(key.destructured.component1(), key)
                    }.toMap()
                    actionForEmptyComponent(component, element, tag,  emptyKeys)
                }
            }
            if (actionForTag != null && tag.value.length > 2) {
                actionForTag(tag, element.range.first + tag.range.first + 1, element.range.first + tag.range.last)
            }
        }
    }

}

fun getPyTypeFromPyExpression(pyExpression: PyExpression, context: TypeEvalContext): PyType? {
    return when (pyExpression) {
        is PyType -> pyExpression
        is PyReferenceExpression -> {
//            val resolveResults = getResolveElements(pyExpression, context)
//            PyUtil.filterTopPriorityResults(resolveResults)
//                .filterIsInstance<PyTypedElement>()
//                .map { context.getType(it) }
//                .firstOrNull() ?:
                PyResolveUtil.resolveLocally(pyExpression).firstOrNull()
                .let {
                    when (it) {
                        is PyImportElement -> {
                            PyUtil.filterTopPriorityResults(it.multiResolve())
                                .firstOrNull()
                        }
                        else -> it
                    }
                }
                ?.let { (it as? PyTypedElement)?.let { typedElement-> context.getType(typedElement)} }
        }
        else -> context.getType(pyExpression)
    }
}