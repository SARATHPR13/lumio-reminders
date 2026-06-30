package com.lumio.app.crash

import android.content.Context
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Installs a process-wide uncaught exception handler so that if the app
 * crashes anywhere, the full stack trace is saved to disk synchronously
 * before the process dies. On the next launch, MainActivity checks for a
 * saved trace and shows it on screen, so it can be read or copied straight
 * from the phone without ADB or a computer.
 *
 * SharedPreferences (not DataStore) is used deliberately: DataStore writes
 * are async/coroutine-based and are not guaranteed to finish before the
 * process is killed. SharedPreferences.Editor.commit() blocks until the
 * write is on disk, which is what's needed in the last moments before a
 * crash.
 */
object CrashHandler {

    private const val PREFS_NAME = "lumio_crash_prefs"
    private const val KEY_TRACE  = "last_crash_trace"

    fun install(context: Context) {
        val appContext     = context.applicationContext
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                val trace = sw.toString()

                appContext
                    .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_TRACE, trace)
                    .commit()
            } catch (_: Throwable) {
                // Never let the crash handler itself throw during a crash.
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    fun getLastCrash(context: Context): String? =
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TRACE, null)

    fun clearLastCrash(context: Context) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_TRACE)
            .apply()
    }
}
