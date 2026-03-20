package nl.ndat.tvlauncher.crash

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Process
import nl.ndat.tvlauncher.LauncherActivity
import timber.log.Timber
import java.util.concurrent.TimeUnit

class CrashHandler(
    private val context: Context
) : Thread.UncaughtExceptionHandler {
    
    companion object {
        private const val PREFS_NAME = "crash_recovery"
        private const val KEY_CRASH_TIMESTAMPS = "crash_timestamps"
        private const val KEY_CRASH_COUNT = "crash_count"
        private const val MAX_CRASH_COUNT = 3
        private val CRASH_WINDOW_MS = TimeUnit.SECONDS.toMillis(60)
        
        private const val KEY_LAST_CRASH_MESSAGE = "last_crash_message"
        private const val KEY_LAST_CRASH_STACK = "last_crash_stack"
        
        @Volatile
        private var instance: CrashHandler? = null
        
        fun init(context: Context): CrashHandler {
            return instance ?: synchronized(this) {
                instance ?: CrashHandler(context.applicationContext).also {
                    instance = it
                    Thread.setDefaultUncaughtExceptionHandler(it)
                }
            }
        }
        
        fun getInstance(): CrashHandler = instance ?: throw IllegalStateException("CrashHandler not initialized")
        
        fun clearCrashHistory(context: Context) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
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
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val defaultHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()
    
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        Timber.e(throwable, "CrashHandler: Uncaught exception")
        
        val currentTime = System.currentTimeMillis()
        val crashTimestamps = getCrashTimestamps()
        
        crashTimestamps.add(0, currentTime)
        
        while (crashTimestamps.size > MAX_CRASH_COUNT) {
            crashTimestamps.removeAt(crashTimestamps.size - 1)
        }
        
        val recentCrashes = crashTimestamps.filter { currentTime - it < CRASH_WINDOW_MS }
        
        prefs.edit()
            .putString(KEY_CRASH_TIMESTAMPS, recentCrashes.joinToString(","))
            .putInt(KEY_CRASH_COUNT, recentCrashes.size)
            .putString(KEY_LAST_CRASH_MESSAGE, throwable.message ?: throwable.javaClass.simpleName)
            .putString(KEY_LAST_CRASH_STACK, getStackTraceString(throwable))
            .apply()
        
        val isCrashLoop = recentCrashes.size >= MAX_CRASH_COUNT
        
        if (isCrashLoop) {
            Timber.e("CrashHandler: Crash loop detected (${recentCrashes.size} crashes in ${CRASH_WINDOW_MS}ms)")
            launchRecoveryActivity()
        } else {
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
    
    private fun getCrashTimestamps(): MutableList<Long> {
        val timestampsStr = prefs.getString(KEY_CRASH_TIMESTAMPS, "") ?: ""
        return if (timestampsStr.isEmpty()) {
            mutableListOf()
        } else {
            try {
                timestampsStr.split(",").map { it.toLong() }.toMutableList()
            } catch (e: Exception) {
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
            val intent = Intent(context, CrashRecoveryActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            }
            context.startActivity(intent)
            Process.killProcess(Process.myPid())
        } catch (e: Exception) {
            Timber.e(e, "CrashHandler: Failed to launch recovery activity")
            Process.killProcess(Process.myPid())
        }
    }
}