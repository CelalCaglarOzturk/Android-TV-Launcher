package nl.ndat.tvlauncher.util

import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.getSystemService
import androidx.core.role.RoleManagerCompat

class DefaultLauncherHelper(
    private val context: Context,
) {
    // Only accessed on API 29+
    private val roleManager by lazy { context.getSystemService<RoleManager>() }

    @SuppressLint("NewApi")
    fun isDefaultLauncher(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            roleManager != null && roleManager!!.isRoleHeld(RoleManagerCompat.ROLE_HOME)
        } else {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            resolveInfo?.activityInfo?.packageName == context.packageName
        }
    }

    @SuppressLint("NewApi")
    fun canRequestDefaultLauncher(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            roleManager != null && roleManager!!.isRoleAvailable(RoleManagerCompat.ROLE_HOME)
        } else {
            true
        }
    }

    @SuppressLint("NewApi")
    fun requestDefaultLauncherIntent(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            roleManager?.createRequestRoleIntent(RoleManagerCompat.ROLE_HOME)
        } else {
            Intent(Settings.ACTION_HOME_SETTINGS)
        }
    }
}
