package com.samuelribeiro.recorda.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.PsiMember
import com.intellij.psi.PsiNamedElement
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UNamedExpression
import org.jetbrains.uast.tryResolve

/** Detects hardcoded Color.* values on text composables that may violate WCAG AA contrast. */
class A11yHardcodedColorDetector : Detector(), Detector.UastScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UCallExpression::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler = object : UElementHandler() {
        override fun visitCallExpression(node: UCallExpression) {
            val name = node.methodName ?: return
            if (name !in TEXT_COMPOSABLES) return

            val colorArg = node.valueArguments
                .filterIsInstance<UNamedExpression>()
                .firstOrNull { it.name == "color" }
                ?.expression ?: return

            if (!isHardcodedColorConstant(colorArg)) return

            context.report(
                issue = ISSUE,
                scope = node,
                location = context.getLocation(colorArg),
                message = "Hardcoded `Color.*` may fail WCAG AA contrast (4.5:1). " +
                    "Use `MaterialTheme.colors.*` to keep contrast correct in light and dark themes."
            )
        }
    }

    private fun isHardcodedColorConstant(expr: UExpression): Boolean {
        val resolved = expr.tryResolve() as? PsiMember ?: return false
        val containingClass = resolved.containingClass?.qualifiedName ?: return false
        if (!containingClass.endsWith(".Color") && !containingClass.endsWith(".Color.Companion")) return false
        val memberName = (resolved as? PsiNamedElement)?.name ?: return false
        return memberName !in SAFE_COLOR_NAMES
    }

    companion object {
        private val TEXT_COMPOSABLES = setOf("Text", "BasicText")
        private val SAFE_COLOR_NAMES = setOf("Unspecified", "Transparent")

        val ISSUE = Issue.create(
            id = "A11yHardcodedColor",
            briefDescription = "Hardcoded color on text composable may fail WCAG AA contrast",
            explanation = """
                Using a hardcoded `Color.*` constant on a text composable bypasses the theme
                and may produce contrast ratios below the WCAG AA minimum (4.5:1 for normal text).
                Use semantic colors from `MaterialTheme.colors` so that both light and dark themes
                maintain sufficient contrast — e.g. `MaterialTheme.colors.onSurface.copy(alpha = 0.7f)`.
            """.trimIndent(),
            category = Category.A11Y,
            priority = 7,
            severity = Severity.WARNING,
            implementation = Implementation(
                A11yHardcodedColorDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}
