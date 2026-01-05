package nl.ndat.tvlauncher.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class FocusController {
    private val _focusReset = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val focusReset = _focusReset.asSharedFlow()

    fun requestFocusReset() {
        _focusReset.tryEmit(Unit)
    }
}
