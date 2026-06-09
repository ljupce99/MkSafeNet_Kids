# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- 

### Changed
- 

### Fixed
- 

### Removed
- 

### Deprecated
- 

---

## [1.0.0] - 2026-06-08

### Added
- Initial release of MkSafeNet_Kids
- User authentication with JWT tokens
- Role-based access control (Admin, Teacher, Student)
- Interactive chat-based scenarios for online safety education
- Teacher session management with QR code access
- Student progress tracking and scoring
- Certificate PDF generation for course completion
- School and teacher account management
- Scenario CRUD operations for admins
- Real-time chat responses with consequences
- Student session participation tracking
- Global statistics dashboard for admins

### Features
- **Authentication**: Login with username/password, JWT token validation
- **Teacher Features**: 
  - Create learning sessions with unique tokens
  - Generate QR codes for student access
  - View student results and progress
  - Toggle session active/inactive status
- **Student Features**:
  - Join sessions via token
  - Interactive scenario questions with multiple choice answers
  - Immediate feedback on answers
  - Consequence education for incorrect answers
  - Final score and certificate generation
- **Admin Features**:
  - Manage schools
  - Create and manage teacher accounts
  - Create, edit, delete safety scenarios
  - View global statistics

### Technology Stack
- Backend: Spring Boot 3.x, Spring Security, Spring Data JPA, SQLite
- Frontend: Vue 3, Vite, Pinia, Vue Router, Axios
- Database: SQLite with Hibernate ORM
- Authentication: JWT (JSON Web Tokens)

---

## Release Template

When creating a new release, use this template:

```markdown
## [X.Y.Z] - YYYY-MM-DD

### Added
- New feature description
- Another new feature

### Changed
- Changed feature or behavior
- Modified how something works

### Fixed
- Bug fix description
- Another bug fix

### Removed
- Removed deprecated feature
- Removed unsupported functionality

### Deprecated
- Deprecated feature that will be removed in future versions

### Security
- Security vulnerability fix
- Security enhancement
```

---

## Version Numbering Guide

Use Semantic Versioning: `MAJOR.MINOR.PATCH`

- **MAJOR**: Incompatible API changes (e.g., 1.0.0 → 2.0.0)
- **MINOR**: New features, backward-compatible (e.g., 1.0.0 → 1.1.0)
- **PATCH**: Bug fixes, backward-compatible (e.g., 1.0.0 → 1.0.1)

Examples:
- `1.0.0` - First release
- `1.1.0` - Added new features
- `1.1.1` - Fixed bug
- `2.0.0` - Major breaking changes

---

## How to Create a Release

### 1. Update Changelog

Edit `CHANGELOG.md`:

```markdown
## [1.1.0] - 2026-06-15

### Added
- New chat message delay feature
- Scenario difficulty levels
- Student performance analytics

### Fixed
- Fixed JWT token expiration edge case
- Fixed CORS issue with preflight requests
```

### 2. Update Version in pom.xml

**File**: `backend/pom.xml`

```xml
<groupId>com.mksafenet</groupId>
<artifactId>mksafenet</artifactId>
<version>1.1.0</version>
```

### 3. Update Version in package.json

**File**: `frontend/package.json`

```json
{
  "name": "mksafenet-frontend",
  "version": "1.1.0"
}
```

### 4. Commit Changes

```bash
git add CHANGELOG.md backend/pom.xml frontend/package.json
git commit -m "Bump version to 1.1.0"
```

### 5. Create Git Tag

```bash
git tag -a v1.1.0 -m "Release version 1.1.0"
git push origin v1.1.0
```

### 6. Create Release Notes

On GitHub/GitLab, create a release with:
- Tag: `v1.1.0`
- Title: `Version 1.1.0`
- Description: Copy from `CHANGELOG.md`
- Attach build artifacts (JAR, Docker image, etc.)

---

## Tracking Issues

Link issues to releases:

```markdown
## [1.2.0] - 2026-07-01

### Fixed
- Fixed scenario ordering issue (#42)
- Fixed missing teacher display name (#45)
- Fixed certificate PDF encoding (#48)
```

---

## Release Checklist

- [ ] All tests passing (`mvn test`, `npm test`)
- [ ] Code review completed
- [ ] CHANGELOG.md updated
- [ ] Version numbers updated (pom.xml, package.json)
- [ ] Build tested (JAR builds successfully)
- [ ] Frontend builds successfully (`npm run build`)
- [ ] Docker build successful (if applicable)
- [ ] Git tag created and pushed
- [ ] Release notes published
- [ ] Deployment tested in staging environment
- [ ] Production deployment completed
- [ ] Announcement sent to stakeholders

---

## Example Releases

### Version 1.0.1 (Patch Release)

```markdown
## [1.0.1] - 2026-06-15

### Fixed
- Fixed JWT token validation edge case
- Fixed null pointer exception in chat response
- Fixed database migration issue on first run

### Security
- Updated dependencies to patch known CVEs
```

### Version 1.1.0 (Minor Release)

```markdown
## [1.1.0] - 2026-07-01

### Added
- New scenario difficulty levels (Basic, Intermediate, Advanced)
- Student performance analytics dashboard
- Scenario result export as CSV
- Email notification for teachers on new student completions

### Changed
- Improved chat message animations
- Enhanced session token generation for better security
- Updated UI styling for better accessibility

### Fixed
- Fixed CORS preflight request handling
- Fixed scenario options JSON parsing
```

### Version 2.0.0 (Major Release)

```markdown
## [2.0.0] - 2026-09-01

### Added
- Mobile app version (React Native)
- Real-time collaboration features
- Scenario branching (different paths based on answers)
- Gamification system (badges, leaderboards)
- Multi-language support

### Changed
- **BREAKING**: API endpoints restructured (see migration guide)
- **BREAKING**: Database schema updated (requires migration)
- JWT token format changed (requires re-login)
- Certificate design improved

### Removed
- Deprecated XML scenario format (JSON only)
- Legacy authentication method

### Migration Guide
See [Migration v1.x to v2.0](docs/migration-v2.0.md)
```

---

## Guidelines

### What to Include

- **Features**: New functionality added
- **Changes**: Modifications to existing functionality
- **Fixes**: Bug fixes and patches
- **Removals**: Removed features or APIs
- **Deprecations**: Features marked for future removal
- **Security**: Security fixes and enhancements

### What NOT to Include

- Internal refactoring with no user impact
- Code style changes
- Comment updates
- Build system changes (unless they affect users)

### Writing Guidelines

- Use clear, concise language
- Write from end-user perspective
- Link to GitHub issues when applicable
- Group related changes together
- Note breaking changes prominently

---

## Maintenance Schedule

- **Patch releases** (1.0.x): As needed for critical bugs
- **Minor releases** (1.x.0): Monthly or when features complete
- **Major releases** (x.0.0): Quarterly or based on roadmap

---

## Support Timeline

| Version | Release Date | End of Life |
|---------|-------------|------------|
| 1.0.x | 2026-06-08 | 2027-06-08 |
| 1.1.x | 2026-07-01 | 2027-07-01 |
| 2.0.x | 2026-09-01 | 2027-09-01 |

Current LTS (Long-Term Support): 1.0.x (12 months)

---

## Previous Releases

### Links

- [GitHub Releases](https://github.com/yourorg/mksafenet/releases)
- [Changelog](https://github.com/yourorg/mksafenet/blob/main/CHANGELOG.md)

---

**Last Updated**: June 8, 2026

For contributing to the changelog, see [CONTRIBUTING.md](CONTRIBUTING.md)

