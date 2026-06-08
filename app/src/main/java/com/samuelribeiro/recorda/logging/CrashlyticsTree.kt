package com.samuelribeiro.recorda.logging

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/** [Timber.Tree] that forwards warnings and errors to Firebase Crashlytics. */
class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < Log.WARN) return
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.log(message)
        t?.let { crashlytics.recordException(it) }
    }
}
