package nl.ndat.tvlauncher.ui.toolbar

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import nl.ndat.tvlauncher.R

@Composable
fun ToolbarNotificationsButton() = Box {
    val context = LocalContext.current

    IconButton(
        onClick = { context.startActivity(Intent("com.android.tv.NOTIFICATIONS_PANEL")) }
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_notifications),
            contentDescription = stringResource(id = R.string.notifications),
        )
    }
}