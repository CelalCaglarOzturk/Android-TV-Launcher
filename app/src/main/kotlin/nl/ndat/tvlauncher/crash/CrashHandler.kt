package nl.ndat.tvlauncher.crash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Process
import nl.ndat.tvlauncher.util.LauncherConstants
import timber.log.Timber
import java.util.concurrent.TimeUnit

class CrashHandler(
    private val appContext: Context
) : Thread.UncaughtExceptionHandler {

    companion object {
        private val PREFS_NAME = LauncherConstants.CrashRecovery.CRASH_COUNT_PREFS
        private const val KEY_CRASH_TIMESTAMPS = "crash_timestamps"
        private val KEY_CRASH_COUNT = LauncherConstants.CrashRecovery.KEY_CRASH_COUNT
        private val MAX_CRASH_COUNT = LauncherConstants.CrashRecovery.MAX_CRASH_COUNT
        private val CRASH_WINDOW_MS = TimeUnit.HOURS.toMillis(LauncherConstants.CrashRecovery.CRASH_COUNT_RESET_HOURS.toLong())

        private const val KEY_LAST_CRASH_MESSAGE = "last_crash_message"
        private const val KEY_LAST_CRASH_STACK = "last_crash_stack"

        @Volatile
        private var instance: CrashHandler? = null

        fun init(context: Context): CrashHandler {
            return instance ?: synchronized(this) {
                instance ?: run {
                    val handler = CrashHandler(context.applicationContext)
                    val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
                    handler.defaultHandler = previousHandler
                    Thread.setDefaultUncaughtExceptionHandler(handler)
                    Timber.d("CrashHandler: Initialized, previous handler = $previousHandler")
                    instance = handler
                    handler
                }
            }
        }

        fun getInstance(): CrashHandler = instance ?: throw IllegalStateException("CrashHandler not initialized")

        @SuppressLint("ApplySharedPref")
        fun clearCrashHistory(context: Context) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit()
            Timber.d("CrashHandler: Crash history cleared")
        }

        fun getCrashCount(context: Context): Int {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_CRASH_COUNT, 0)
        }

        fun getLastCrashMessage(context: Context): String? {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_LAST_CRASH_MESSAGE, null)
        }

        fun isInCrashLoop(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val count = prefs.getInt(KEY_CRASH_COUNT, 0)
            return count >= MAX_CRASH_COUNT
        }
    }

    private val prefs: SharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    var defaultHandler: Thread.UncaughtExceptionHandler? = null
        internal set

    @SuppressLint("ApplySharedPref")
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        Timber.e(throwable, "CrashHandler: Uncaught exception")

        try {
            val currentTime = System.currentTimeMillis()
            val crashTimestamps = getCrashTimestamps()

            crashTimestamps.add(0, currentTime)

            while (crashTimestamps.size > MAX_CRASH_COUNT + 1) {
                crashTimestamps.removeAt(crashTimestamps.size - 1)
            }

            val recentCrashes = crashTimestamps.filter { currentTime - it < CRASH_WINDOW_MS }

            val crashCount = recentCrashes.size

            Timber.d("CrashHandler: Crash count = $crashCount, timestamps = ${recentCrashes.joinToString()}")

            prefs.edit()
                .putString(KEY_CRASH_TIMESTAMPS, recentCrashes.joinToString(","))
                .putInt(KEY_CRASH_COUNT, crashCount)
                .putString(KEY_LAST_CRASH_MESSAGE, throwable.message ?: throwable.javaClass.simpleName)
                .putString(KEY_LAST_CRASH_STACK, getStackTraceString(throwable))
                .commit()

            val isCrashLoop = crashCount >= MAX_CRASH_COUNT

            Timber.d("CrashHandler: isCrashLoop = $isCrashLoop (count=$crashCount, threshold=$MAX_CRASH_COUNT)")

            if (isCrashLoop) {
                Timber.e("CrashHandler: Crash loop detected ($crashCount crashes in ${CRASH_WINDOW_MS}ms)")
                launchRecoveryActivity()
            } else {
                Timber.d("CrashHandler: Not a crash loop yet, delegating to default handler")
                defaultHandler?.uncaughtException(thread, throwable)
            }
        } catch (e: Exception) {
            Timber.e(e, "CrashHandler: Error handling crash")
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun getCrashTimestamps(): MutableList<Long> {
        val timestampsStr = prefs.getString(KEY_CRASH_TIMESTAMPS, "") ?: ""
        return if (timestampsStr.isEmpty()) {
            mutableListOf()
        } else {
            try {
                timestampsStr.split(",").filter { it.isNotEmpty() }.map { it.toLong() }.toMutableList()
            } catch (e: Exception) {
                Timber.e(e, "CrashHandler: Error parsing timestamps")
                mutableListOf()
            }
        }
    }

    private fun getStackTraceString(throwable: Throwable): String {
        val sw = java.io.StringWriter()
        val pw = java.io.PrintWriter(sw)
        throwable.printStackTrace(pw)
        return sw.toString()
    }

    private fun launchRecoveryActivity() {
        try {
            Timber.d("CrashHandler: Launching CrashRecoveryActivity")
            val intent = Intent(appContext, CrashRecoveryActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            }
            appContext.startActivity(intent)
            Timber.d("CrashHandler: CrashRecoveryActivity started, finishing process")
            Process.killProcess(Process.myPid())
        } catch (e: Exception) {
            Timber.e(e, "CrashHandler: Failed to launch recovery activity")
            Process.killProcess(Process.myPid())
        }
    }
}
