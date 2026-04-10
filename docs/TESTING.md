# Testing Guide

## Running Unit Tests

Unit tests run on the JVM and don't require an Android device or emulator.

```bash
# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests "dev.mudrock.TiViyomiTVLauncher.data.repository.SettingsRepositoryTest"

# Run with verbose output
./gradlew test --info

# Generate test report
./gradlew test
# Report location: app/build/reports/tests/testDebugUnitTest/index.html
```

## Running Instrumentation Tests

Instrumentation tests require an Android device or emulator.

```bash
# Run all instrumentation tests
./gradlew connectedAndroidTest

# Run specific test class
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=dev.mudrock.TiViyomiTVLauncher.LauncherSettingsTest

# Run on specific device
adb devices  # List connected devices
./gradlew connectedAndroidTest -Pandroid.test.connectedDevices=device-id

# Generate test report
# Report location: app/build/reports/androidTests/connected/index.html
```

## Checking for Missing Translations

Android Lint automatically checks for missing translations.

```bash
# Run lint to check for missing translations
./gradlew lint

# View lint report
# Report location: app/build/reports/lint-results-debug.html

# Check only missing translations
./gradlew lint | grep -i "missingtranslation"

# Run lint on release build (stricter)
./gradlew lintRelease
```

To see which strings are missing:

1. Open `app/build/reports/lint-results-debug.html`
2. Look for "MissingTranslation" warnings
3. Each warning shows the string name and languages missing it

## Testing on Multiple API Levels

### Option 1: Android Studio AVD Manager

Create AVDs for different API levels (8, 14, 16, etc.):

1. Open Android Studio > Tools > Device Manager
2. Create Virtual Device
3. Select device type (Android TV)
4. Select system image for each API level

### Option 2: Command Line

```bash
# List available AVDs
emulator -list-avds

# Start specific AVD
emulator -avd <avd_name> &

# Wait for emulator to boot
adb wait-for-device

# Run tests
./gradlew connectedAndroidTest
```
## Common Test Issues

### API Level Compatibility

For Android 8 (API 26) and below:
- Some androidx APIs may not be available
- Notification channels require API 26+
- Storage permissions differ (WRITE_EXTERNAL_STORAGE vs MANAGE_EXTERNAL_STORAGE)

### TV-Specific Testing

```bash
# Enable TV mode on emulator
adb shell settings put system dtv_app_launched 1

# Test leanback features
adb shell am start -a android.intent.action.MAIN -c android.intent.category.LEANBACK_LAUNCHER
```

## Test Architecture

```
app/src/
├── test/                           # Unit tests
│   └── java/dev/mudrock/TiViyomiTVLauncher/
│       └── data/repository/
│           └── SettingsRepositoryTest.kt
│
└── androidTest/                    # Instrumentation tests
    └── java/dev/mudrock/TiViyomiTVLauncher/
        ├── LauncherSettingsTest.kt
        └── ExampleInstrumentedTest.kt
```

## Debugging Test Failures

```bash
# Run with stack trace
./gradlew test --stacktrace

# Run with full debug output
./gradlew test --debug

# Run single test method
./gradlew test --tests "*.SettingsRepositoryTest.testMethodName"
```
