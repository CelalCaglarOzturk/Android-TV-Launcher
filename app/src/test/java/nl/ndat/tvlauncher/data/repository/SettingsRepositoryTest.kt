package nl.ndat.tvlauncher.data.repository

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.anySet

class SettingsRepositoryTest {

    private lateinit var mockContext: Context
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setup() {
        mockContext = mock(Context::class.java)
        mockPrefs = mock(SharedPreferences::class.java)
        mockEditor = mock(SharedPreferences.Editor::class.java)

        `when`(mockContext.getSharedPreferences("launcher_settings", Context.MODE_PRIVATE))
            .thenReturn(mockPrefs)
        `when`(mockPrefs.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putInt(anyString(), anyInt())).thenReturn(mockEditor)
        `when`(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor)
        `when`(mockEditor.putStringSet(anyString(), anySet())).thenReturn(mockEditor)
        `when`(mockEditor.clear()).thenReturn(mockEditor)
        `when`(mockEditor.remove(anyString())).thenReturn(mockEditor)

        // Default values
        `when`(mockPrefs.getInt("app_card_size", 90)).thenReturn(90)
        `when`(mockPrefs.getInt("channel_card_size", 90)).thenReturn(90)
        `when`(mockPrefs.getInt("channel_cards_per_row", 7)).thenReturn(7)
        `when`(mockPrefs.getStringSet("hidden_inputs", null)).thenReturn(null)
        `when`(mockPrefs.getBoolean("show_mobile_apps", false)).thenReturn(false)
        `when`(mockPrefs.getBoolean("enable_animations", true)).thenReturn(true)
        `when`(mockPrefs.getBoolean("suppress_original_launcher", false)).thenReturn(false)
        `when`(mockPrefs.getBoolean("suppress_launcher_only_external", false)).thenReturn(false)
        `when`(mockPrefs.getStringSet("toolbar_items_order", null)).thenReturn(null)
        `when`(mockPrefs.getStringSet("toolbar_items_enabled", null)).thenReturn(null)

        settingsRepository = SettingsRepository(mockContext)
    }

    @Test
    fun `appCardSize default value is 90`() {
        assertEquals(90, settingsRepository.appCardSize.value)
    }

    @Test
    fun `setAppCardSize updates value`() {
        settingsRepository.setAppCardSize(120)
        assertEquals(120, settingsRepository.appCardSize.value)
    }

    @Test
    fun `channelCardsPerRow clamps to valid range`() {
        settingsRepository.setChannelCardsPerRow(25)
        assertTrue(settingsRepository.channelCardsPerRow.value <= 20)

        settingsRepository.setChannelCardsPerRow(2)
        assertTrue(settingsRepository.channelCardsPerRow.value >= 3)
    }

    @Test
    fun `suppressOriginalLauncher default value is false`() {
        assertFalse(settingsRepository.suppressOriginalLauncher.value)
    }

    @Test
    fun `setSuppressOriginalLauncher updates value`() {
        settingsRepository.setSuppressOriginalLauncher(true)
        assertTrue(settingsRepository.suppressOriginalLauncher.value)
    }

    @Test
    fun `suppressLauncherOnlyExternal default value is false`() {
        assertFalse(settingsRepository.suppressLauncherOnlyExternal.value)
    }

    @Test
    fun `toggleToolbarItem cannot disable LAUNCHER_SETTINGS`() {
        settingsRepository.setToolbarItemEnabled("LAUNCHER_SETTINGS", false)
        assertTrue(settingsRepository.toolbarItemsEnabled.value.contains("LAUNCHER_SETTINGS"))
    }
}