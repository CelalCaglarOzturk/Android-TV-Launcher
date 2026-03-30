# Release Notes

## [X.Y.Z] - YYYY-MM-DD

### Added
- Feature 1: Brief description
- Feature 2: Brief description

### Changed
- Change 1: Brief description
- Change 2: Brief description

### Fixed
- Bugfix 1: Brief description
- Bugfix 2: Brief description

### Deprecated
- Deprecated feature 1: Brief description and migration path

### Removed
- Removed feature 1: Brief description

### Security
- Security fix 1: Brief description

### Breaking Changes
- Breaking change 1: Brief description and migration instructions

---

## Template Usage

### Version Numbering
- **MAJOR (X)**: Incompatible API changes
- **MINOR (Y)**: New features, backward compatible
- **PATCH (Z)**: Bug fixes, backward compatible

### Categories

#### Added
New features that were added in this release.

#### Changed
Changes to existing functionality that don't break compatibility.

#### Fixed
Bug fixes that don't change behavior significantly.

#### Deprecated
Features that will be removed in a future release. Include migrationpath.

#### Removed
Features that were removed in this release. Include migration instructions.

#### Security
Security-related changes. Be specific about what was fixed without revealing exploitation details.

#### Breaking Changes
Changes that require users to update their configuration or code. Always provide migration instructions.

---

## Example Release

## [2.1.0] - 2024-01-15

### Added
- **Suppress Original Launcher**: New accessibility service that redirects home button to TV Launcher
  when returning from other apps. Configure in Settings > Behavior > Suppress Original Launcher.
- **Restore Preview Dialog**: Backup restore now shows a preview of what will be restored before applying.
- **Portuguese Localization**: Added translations for Portuguese (Portugal) and Portuguese (Brazil).
- **Developer Mode**: Hidden setting in About screen (click version 7 times) to enable developer features.
- **Backup History**: Backups now create timestamped files with history management.

### Changed
- Improved accessibility service memory management - no longer uses dependency injection to prevent leaks.
- Refactored SettingsRepository to use centralized constants for all magic numbers.
- Enhanced crash recovery window from 60 seconds to 1 hour to reduce false positives.
- Improved null safety in accessibility service event handling.

### Fixed
- Fixed memory leak in accessibility service when app is更新的.
- Fixed hardcoded system launcher package names - now uses configurable list with custom ROM support.
- Fixed backup not preserving all settings on restore.
- Fixed restore not showing preview before applying changes.

### Security
- Developer mode test crash feature now requires explicit enabling before use.

### Breaking Changes
- Accessibility service configuration changed. Users need to re-enable the service after update.
  Migration: Go to Settings > Behavior > Suppress Original Launcher, disable and re-enable if previously configured.