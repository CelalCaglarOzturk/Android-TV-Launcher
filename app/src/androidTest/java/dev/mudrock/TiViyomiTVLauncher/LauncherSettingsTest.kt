package dev.mudrock.TiViyomiTVLauncher

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.mudrock.TiViyomiTVLauncher.data.repository.SettingsRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for Settings Repository
 */
@RunWith(AndroidJUnit4::class)
class LauncherSettingsTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        settingsRepository = SettingsRepository(context)
    }

    @Test
    fun testSuppressOriginalLauncherToggle() {
        // Reset to default
        settingsRepository.setSuppressOriginalLauncher(false)
        assertFalse(settingsRepository.suppressOriginalLauncher.value)
        
        // Toggle on
        settingsRepository.setSuppressOriginalLauncher(true)
        assertTrue(settingsRepository.suppressOriginalLauncher.value)
        
        // Toggle off
        settingsRepository.setSuppressOriginalLauncher(false)
        assertFalse(settingsRepository.suppressOriginalLauncher.value)
    }

    @Test
    fun testSuppressLauncherOnlyExternalToggle() {
        // Reset to default
        settingsRepository.setSuppressLauncherOnlyExternal(false)
        assertFalse(settingsRepository.suppressLauncherOnlyExternal.value)
        
        // Toggle on
        settingsRepository.setSuppressLauncherOnlyExternal(true)
        assertTrue(settingsRepository.suppressLauncherOnlyExternal.value)
        
        // Toggle off
        settingsRepository.setSuppressLauncherOnlyExternal(false)
        assertFalse(settingsRepository.suppressLauncherOnlyExternal.value)
    }

    @Test
    fun testAppCardSizeRange() {
        // Test minimum
        settingsRepository.setAppCardSize(50)
        assertEquals(50, settingsRepository.appCardSize.value)
        
        // Test maximum
        settingsRepository.setAppCardSize(200)
        assertEquals(200, settingsRepository.appCardSize.value)
        
        // Test middle
        settingsRepository.setAppCardSize(120)
        assertEquals(120, settingsRepository.appCardSize.value)
    }

    @Test
    fun testChannelCardsPerRowRange() {
        // Test minimum
        settingsRepository.setChannelCardsPerRow(3)
        assertEquals(3, settingsRepository.channelCardsPerRow.value)
        
        // Test maximum
        settingsRepository.setChannelCardsPerRow(20)
        assertEquals(20, settingsRepository.channelCardsPerRow.value)
        
        // Test below minimum (should clamp)
        settingsRepository.setChannelCardsPerRow(1)
        assertEquals(3, settingsRepository.channelCardsPerRow.value)
        
        // Test above maximum (should clamp)
        settingsRepository.setChannelCardsPerRow(25)
        assertEquals(20, settingsRepository.channelCardsPerRow.value)
    }

    @Test
    fun testShowMobileAppsToggle() {
        // Toggle on
        settingsRepository.setShowMobileApps(true)
        assertTrue(settingsRepository.showMobileApps.value)
        
        // Toggle off
        settingsRepository.setShowMobileApps(false)
        assertFalse(settingsRepository.showMobileApps.value)
    }

    @Test
    fun testAnimationsToggle() {
        // Reset to default
        settingsRepository.setEnableAnimations(true)
        assertTrue(settingsRepository.enableAnimations.value)
        
        // Toggle off
        settingsRepository.setEnableAnimations(false)
        assertFalse(settingsRepository.enableAnimations.value)
        
        // Individual animation toggles
        settingsRepository.setAnimAppIcon(false)
        assertFalse(settingsRepository.animAppIcon.value)
        
        settingsRepository.setAnimChannelRow(false)
        assertFalse(settingsRepository.animChannelRow.value)
        
        settingsRepository.setAnimChannelMove(false)
        assertFalse(settingsRepository.animChannelMove.value)
        
        settingsRepository.setAnimAppMove(false)
        assertFalse(settingsRepository.animAppMove.value)
    }

    @Test
    fun testHiddenInputsToggle() {
        val testInputId = "test_input_1"
        
        // Clear any existing
        settingsRepository.clearHiddenInputs()
        
        // Add hidden input
        settingsRepository.toggleInputHidden(testInputId)
        assertTrue(settingsRepository.hiddenInputs.value.contains(testInputId))
        
        // Remove hidden input
        settingsRepository.toggleInputHidden(testInputId)
        assertFalse(settingsRepository.hiddenInputs.value.contains(testInputId))
    }

    @Test
    fun testToolbarItemToggle() {
        val clockItem = "CLOCK"
        
        // Reset to enabled
        settingsRepository.setToolbarItemEnabled(clockItem, true)
        assertTrue(settingsRepository.toolbarItemsEnabled.value.contains(clockItem))
        
        // Disable clock
        settingsRepository.setToolbarItemEnabled(clockItem, false)
        assertFalse(settingsRepository.toolbarItemsEnabled.value.contains(clockItem))
        
        // Re-enable clock
        settingsRepository.setToolbarItemEnabled(clockItem, true)
        assertTrue(settingsRepository.toolbarItemsEnabled.value.contains(clockItem))
    }

    @Test
    fun testLauncherSettingsCannotBeDisabled() {
        val launcherSettingsItem = "LAUNCHER_SETTINGS"
        
        // Ensure it's enabled first
        settingsRepository.setToolbarItemEnabled(launcherSettingsItem, true)
        
        // Try to disable launcher settings
        settingsRepository.setToolbarItemEnabled(launcherSettingsItem, false)
        
        // Should still be enabled
        assertTrue(settingsRepository.toolbarItemsEnabled.value.contains(launcherSettingsItem))
    }

    @Test
    fun testResetSettings() {
        // Change some settings
        settingsRepository.setAppCardSize(150)
        settingsRepository.setShowMobileApps(true)
        settingsRepository.setSuppressOriginalLauncher(true)
        
        // Reset
        settingsRepository.resetSettings()
        
        // Verify defaults
        assertEquals(SettingsRepository.DEFAULT_APP_CARD_SIZE, settingsRepository.appCardSize.value)
        assertFalse(settingsRepository.showMobileApps.value)
        assertFalse(settingsRepository.suppressOriginalLauncher.value)
    }

    @Test
    fun testClearHiddenInputs() {
        // Add some hidden inputs
        settingsRepository.toggleInputHidden("input1")
        settingsRepository.toggleInputHidden("input2")
        
        // Clear all
        settingsRepository.clearHiddenInputs()
        
        // Verify empty
        assertTrue(settingsRepository.hiddenInputs.value.isEmpty())
    }

    @Test
    fun testToolbarItemsOrder() {
        val newOrder = listOf("CLOCK", "INPUTS", "NOTIFICATIONS")
        
        settingsRepository.setToolbarItemsOrder(newOrder)
        assertEquals(newOrder, settingsRepository.toolbarItemsOrder.value)
    }

    @Test
    fun testChannelCardSize() {
        settingsRepository.setChannelCardSize(100)
        assertEquals(100, settingsRepository.channelCardSize.value)
    }

    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("dev.mudrock.TiViyomiTVLauncher", appContext.packageName)
    }
}