package nl.ndat.tvlauncher.util

import android.content.Context
import android.content.Intent
import android.media.tv.TvInputInfo
import androidx.tvprovider.media.tv.TvContractCompat
import nl.ndat.tvlauncher.data.model.InputType

fun TvInputInfo.loadPreferredLabel(context: Context): String {
    val customLabel = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        loadCustomLabel(context)
    } else {
        null
    }

    val preferredLabel = customLabel ?: loadLabel(context)
    return preferredLabel.toString()
}

fun TvInputInfo.getInputType() = when (this.type) {
    TvInputInfo.TYPE_TUNER -> InputType.TUNER
    TvInputInfo.TYPE_OTHER -> InputType.OTHER
    TvInputInfo.TYPE_COMPOSITE -> InputType.COMPOSITE
    TvInputInfo.TYPE_SVIDEO -> InputType.SVIDEO
    TvInputInfo.TYPE_SCART -> InputType.SCART
    TvInputInfo.TYPE_COMPONENT -> InputType.COMPONENT
    TvInputInfo.TYPE_VGA -> InputType.VGA
    TvInputInfo.TYPE_DVI -> InputType.DVI
    TvInputInfo.TYPE_HDMI -> InputType.HDMI
    TvInputInfo.TYPE_DISPLAY_PORT -> InputType.DISPLAY_PORT
    else -> InputType.OTHER
}

fun TvInputInfo.createSwitchIntent(): Intent = Intent(
    Intent.ACTION_VIEW,
    TvContractCompat.buildChannelUriForPassthroughInput(id)
).apply {
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
