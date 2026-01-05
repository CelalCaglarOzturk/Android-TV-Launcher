# TV Launcher - How-To Guide

## Table of Contents
1. [Features & Capabilities](#features--capabilities)
2. [Testing Guide](#testing-guide)

---

## Features & Capabilities

### 1. Home Tab
- **Favorite Apps Row**: Displays pinned apps at the top
- **Preview Channels**: Shows content recommendations from apps (Netflix, YouTube, etc.)
- **Watch Next Channel**: Displays continue watching content from all apps
- **Channel Management**: Enable/disable and reorder channels
- **Disabled Channels Section**: Shows disabled channels with option to re-enable

### 2. Apps Tab
- **All Apps Grid**: Displays all installed apps in a responsive grid
- **Mobile Apps Toggle**: Option to show/hide non-TV apps
- **App Reordering**: Move apps using D-pad in move mode
- **Favorite Management**: Add/remove apps from Home tab
- **Hide Apps**: Hide unwanted apps from the launcher
- **Hidden Apps Section**: View and unhide hidden apps
- **App Info**: Quick access to system app settings

### 3. Toolbar
- **Tab Navigation**: Switch between Home and Apps tabs
- **Input Switching**: Quick access to TV inputs (HDMI, etc.)
- **System Settings**: Direct access to Android TV settings
- **Clock**: Live time display

### 4. Launcher Settings (via Launcher Settings app)
- **App Card Size**: Adjust the size of app cards (50-200dp)
- **Watch Next Settings**: Per-app blacklist for Watch Next content
- **Input Settings**: Show/hide specific inputs
- **Backup**: Export configuration to file
- **Restore**: Import configuration from backup file

### 5. System Integration
- **Default Launcher**: Prompts to set as default home app
- **Package Monitoring**: Automatically detects new/updated/removed apps
- **Boot Receiver**: Refreshes app list on device boot
- **TV Provider Integration**: Full support for Android TV channels API

### 6. Navigation & Focus
- **D-pad Navigation**: Full remote control support
- **Menu Key**: Mapped to long-press for context menus
- **Focus Reset**: Pressing Home returns focus to first item
- **Focus Restoration**: Remembers focus position when switching tabs

---

## Testing Guide

### Prerequisites
- Android TV device or emulator (API 21+)
- ADB access for installation
- Remote control or D-pad input device

### Step-by-Step Testing

#### Phase 1: Installation & Initial Setup

1. **Install the APK**
   ```bash
   adb install -r tv-launcher.apk
   ```

2. **Launch the App**
   - Navigate to "TV Launcher" in your app drawer
   - Press SELECT to launch

3. **Set as Default Launcher**
   - When prompted, select "TV Launcher" as default
   - Verify: Press HOME button - should show TV Launcher

4. **Grant Permissions**
   - Accept the TV listings permission when prompted
   - Verify: Channels should start appearing

#### Phase 2: Home Tab Testing

5. **View Favorite Apps**
   - Verify: Apps appear in horizontal row at top
   - Verify: New apps are automatically added to favorites

6. **Reorder Favorite Apps**
   - Navigate to an app in the favorites row
   - Long-press (hold SELECT for 2 seconds)
   - Select "Move" from popup
   - Use LEFT/RIGHT to reposition
   - Press SELECT to confirm position
   - Verify: App order persists after restart

7. **Remove App from Home**
   - Long-press on a favorite app
   - Select "Remove from Home"
   - Verify: App disappears from favorites row
   - Verify: App still visible in Apps tab

8. **Test Preview Channels**
   - Verify: Channels from apps like Netflix, YouTube appear
   - Navigate to a program card
   - Press SELECT to launch content
   - Verify: Correct app opens with content

9. **Test Watch Next Channel**
   - Start watching content in any app
   - Return to launcher
   - Verify: Content appears in "Watch Next" row
   - Long-press on Watch Next item to remove it

10. **Disable a Channel**
    - Navigate to a channel title
    - Long-press on the channel title
    - Select "Disable Channel"
    - Verify: Channel moves to "Disabled Channels" section

11. **Reorder Channels**
    - Long-press on a channel title
    - Select "Move Channel"
    - Use UP/DOWN to reposition
    - Press SELECT to confirm
    - Verify: Channel order persists

12. **Re-enable a Channel**
    - Scroll to "Disabled Channels" section
    - Select a disabled channel card
    - Press SELECT to enable
    - Verify: Channel reappears in main list

#### Phase 3: Apps Tab Testing

13. **Navigate to Apps Tab**
    - Press UP to reach toolbar
    - Navigate to "All apps" tab
    - Verify: Full app grid displayed

14. **Toggle Mobile Apps**
    - If "Show mobile apps" toggle appears
    - Press SELECT to toggle
    - Verify: Additional apps appear/disappear

15. **Reorder Apps in Grid**
    - Long-press on any app
    - Select "Move"
    - Use D-pad to reposition
    - Press SELECT to confirm
    - Verify: New position persists

16. **Hide an App**
    - Long-press on any app
    - Select "Add to Home" if not already favorited
    - Long-press again
    - Open the app's long-press menu
    - Scroll down to "Hidden Apps" section
    - Navigate to the app and long-press
    - Select "Hide App"
    - Verify: App moves to "Hidden Apps" section

17. **Unhide an App**
    - Navigate to "Hidden Apps" section
    - Long-press on hidden app
    - Select "Unhide App"
    - Verify: App returns to main grid

18. **Access App Info**
    - Long-press on any app
    - Select "Info"
    - Verify: System app settings page opens

#### Phase 4: Toolbar Testing

19. **Test Input Switching**
    - Press UP to toolbar
    - Navigate to input button (antenna icon)
    - Press SELECT
    - Verify: Input list appears
    - Select an input
    - Verify: TV switches to that input

20. **Test System Settings**
    - Navigate to settings button (gear icon)
    - Press SELECT
    - Verify: Android TV settings open

21. **Test Clock**
    - Verify: Clock displays in toolbar
    - Wait 1+ minute
    - Verify: Time updates correctly

#### Phase 5: Launcher Settings Testing

22. **Open Launcher Settings**
    - Navigate to Apps tab
    - Find "Launcher Settings" app
    - Press SELECT
    - Verify: Settings dialog opens

23. **Adjust App Card Size**
    - In settings, locate "App Card Size"
    - Press + or - buttons
    - Verify: Size value changes (50-200 range)
    - Close settings
    - Verify: App cards reflect new size

24. **Configure Watch Next Blacklist**
    - Select "Watch Next" in settings
    - Toggle apps on/off
    - Close settings
    - Verify: Blacklisted apps don't show in Watch Next

25. **Configure Input Visibility**
    - Select "Inputs" in settings
    - Uncheck an input
    - Close settings
    - Verify: Input no longer appears in toolbar

26. **Test Backup**
    - Select "Backup" in settings
    - Verify: "Backup completed successfully" message
    - Verify: File exists at `Android/media/nl.ndat.tvlauncher/Backup/tv_launcher_backup.json`

27. **Test Restore**
    - Make some configuration changes
    - Select "Restore" in settings
    - Verify: "Restore completed successfully" message
    - Verify: Configuration reverts to backup state

#### Phase 6: System Integration Testing

28. **Test App Installation Detection**
    - Install a new app via ADB
    ```bash
    adb install some-app.apk
    ```
    - Verify: New app appears in Apps tab automatically
    - Verify: New app added to Home favorites

29. **Test App Uninstallation Detection**
    - Uninstall an app via ADB
    ```bash
    adb uninstall com.example.app
    ```
    - Verify: App removed from both tabs

30. **Test Boot Persistence**
    - Reboot the device
    ```bash
    adb reboot
    ```
    - Verify: TV Launcher loads as home
    - Verify: All favorites preserved
    - Verify: All channel settings preserved
    - Verify: Hidden apps remain hidden

#### Phase 7: Navigation Edge Cases

31. **Test Home Button**
    - Navigate deep into any tab
    - Press HOME button on remote
    - Verify: Focus resets to first item
    - Verify: Scrolls back to top

32. **Test Back Button**
    - Open any popup/dialog
    - Press BACK button
    - Verify: Popup closes
    - Verify: Focus returns to previous item

33. **Test Menu Button**
    - Focus on any app card
    - Press MENU button on remote
    - Verify: Context popup appears (same as long-press)

### Test Checklist Summary

| Test Category | Tests | Status |
|--------------|-------|--------|
| Installation | 1-4 | ☐ |
| Home Tab | 5-12 | ☐ |
| Apps Tab | 13-18 | ☐ |
| Toolbar | 19-21 | ☐ |
| Settings | 22-27 | ☐ |
| System Integration | 28-30 | ☐ |
| Navigation | 31-33 | ☐ |

---

## Troubleshooting

### Apps Not Appearing
- Ensure QUERY_ALL_PACKAGES permission is granted
- Check logcat for AppResolver errors:
  ```bash
  adb logcat -s AppResolver
  ```

### Channels Not Loading
- Grant TV listings permission
- Verify channels exist in source apps
- Check logcat for ChannelResolver errors:
  ```bash
  adb logcat -s ChannelResolver
  ```

### Focus Issues
- Press HOME to reset focus
- Check for FocusRequester crashes in logs

### Backup/Restore Failures
- Ensure external storage is available
- Check file permissions
- Verify backup file path: `Android/media/nl.ndat.tvlauncher/Backup/`
