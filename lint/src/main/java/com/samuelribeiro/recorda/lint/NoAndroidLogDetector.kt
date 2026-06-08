package com.samuelribeiro.recorda.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement

/** Enforces use of Timber instead of android.util.Log. */
class NoAndroidLogDetector : Detector(), Detector.UastScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UCallExpression::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler = object : UElementHandler() {
        override fun visitCallExpression(node: UCallExpression) {
            val name = node.methodName ?: return
            if (name !in LOG_METHODS) return

            val qualifier = node.receiver?.asRenderString() ?: return
            if (qualifier != "Log") return

            val resolved = node.resolve() ?: return
            if (resolved.containingClass?.qualifiedName != "android.util.Log") return

            context.report(
                issue = ISSUE,
                scope = node,
                location = context.getLocation(node),
                message = "Use `Timber` instead of `android.util.Log`."
            )
        }
    }

    companion object {
        private val LOG_METHODS = setOf("v", "d", "i", "w", "e", "wtf")

        val ISSUE = Issue.create(
            id = "NoAndroidLog",
            briefDescription = "Use Timber instead of android.util.Log",
            explanation = """
                Direct calls to `android.util.Log` bypass Timber's tag management,
                debug-only filtering, and Crashlytics integration. Always use `Timber`.
            """.trimIndent(),
            category = Category.CORRECTNESS,
            priority = 8,
            severity = Severity.ERROR,
            implementation = Implementation(
                NoAndroidLogDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}
