package nl.ndat.tvlauncher.ui.tab.apps

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import nl.ndat.tvlauncher.BuildConfig
import nl.ndat.tvlauncher.data.repository.AppRepository
import nl.ndat.tvlauncher.data.repository.SettingsRepository
import nl.ndat.tvlauncher.data.sqldelight.App
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppsTabViewModelTest {

    private val appRepository: AppRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: AppsTabViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Mock settings repository flows
        every { settingsRepository.showMobileApps } returns MutableStateFlow(false)
        every { settingsRepository.appCardSize } returns MutableStateFlow(SettingsRepository.DEFAULT_APP_CARD_SIZE)

        // Mock app repository flows
        coEvery { appRepository.getApps() } returns MutableStateFlow(emptyList())
        coEvery { appRepository.getHiddenApps() } returns MutableStateFlow(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `apps flow filters out current application`(): runTest = runTest {
        val currentApp = createApp(BuildConfig.APPLICATION_ID, leanback = true)
        val otherApp = createApp("com.example.app", leanback = true)

        coEvery { appRepository.getApps() } returns MutableStateFlow(listOf(currentApp, otherApp))

        viewModel = AppsTabViewModel(appRepository, settingsRepository)

        viewModel.apps.test {
            // Skip initial empty state if necessary, or just check that eventually we get the right state
            val first = awaitItem()
            val apps = if (first.isEmpty()) awaitItem() else first

            assertEquals(1, apps.size)
            assertEquals("com.example.app", apps[0].packageName)
        }
    }

    @Test
    fun `apps flow always shows settings app`(): runTest = runTest {
        val settingsApp = createApp("nl.ndat.tvlauncher.settings", leanback = false) // Not leanback
        // Should be shown even if showMobileApps is false (default)

        coEvery { appRepository.getApps() } returns MutableStateFlow(listOf(settingsApp))

        viewModel = AppsTabViewModel(appRepository, settingsRepository)

        viewModel.apps.test {
            val first = awaitItem()
            val apps = if (first.isEmpty()) awaitItem() else first

            assertEquals(1, apps.size)
            assertEquals("nl.ndat.tvlauncher.settings", apps[0].packageName)
        }
    }

    @Test
    fun `apps flow shows mobile apps when setting enabled`(): runTest = runTest {
        val mobileApp = createApp("com.mobile.app", leanback = false)
        val tvApp = createApp("com.tv.app", leanback = true)

        val showMobileAppsFlow = MutableStateFlow(true)
        every { settingsRepository.showMobileApps } returns showMobileAppsFlow
        coEvery { appRepository.getApps() } returns MutableStateFlow(listOf(mobileApp, tvApp))

        viewModel = AppsTabViewModel(appRepository, settingsRepository)

        viewModel.apps.test {
            val first = awaitItem()
            val apps = if (first.isEmpty()) awaitItem() else first

            assertEquals(2, apps.size)
        }
    }

    @Test
    fun `apps flow hides mobile apps when setting disabled`(): runTest = runTest {
        val mobileApp = createApp("com.mobile.app", leanback = false)
        val tvApp = createApp("com.tv.app", leanback = true)

        val showMobileAppsFlow = MutableStateFlow(false)
        every { settingsRepository.showMobileApps } returns showMobileAppsFlow
        coEvery { appRepository.getApps() } returns MutableStateFlow(listOf(mobileApp, tvApp))

        viewModel = AppsTabViewModel(appRepository, settingsRepository)

        viewModel.apps.test {
            val first = awaitItem()
            val apps = if (first.isEmpty()) awaitItem() else first

            assertEquals(1, apps.size)
            assertEquals("com.tv.app", apps[0].packageName)
        }
    }

    private fun createApp(packageName: String, leanback: Boolean): App {
        return App(
            id = "app:$packageName",
            displayName = "App $packageName",
            packageName = packageName,
            launchIntentUriDefault = "intent:$packageName",
            launchIntentUriLeanback = if (leanback) "intent:leanback:$packageName" else null,
            favoriteOrder = null,
            hidden = 0,
            allAppsOrder = null
        )
    }
}
