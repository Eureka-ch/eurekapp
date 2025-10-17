# CI/CD Pipeline Documentation

## Overview

EurekApp uses GitHub Actions for continuous integration and deployment. This document describes the CI/CD pipeline, workflows, and optimization strategies.

## Workflows

### 1. Main CI Workflow (`ci.yml`)

**Triggers:**
- Push to `main` branch
- Pull request events (opened, synchronize, reopened)

**Steps:**
1. **Checkout**: Clones repository with full history for better analysis
2. **KVM Setup**: Enables hardware acceleration for Android emulator
3. **JDK Setup**: Installs Java 17 (Temurin distribution)
4. **Gradle Caching**: Caches Gradle dependencies for faster builds
5. **AVD Caching**: Caches Android Virtual Device for emulator tests
6. **Secret Decoding**: Decodes Firebase and Android secrets
7. **Code Formatting**: Runs KTFmt format check
8. **Build & Lint**: Assembles app and runs lint checks
9. **Unit Tests**: Runs local unit tests
10. **Firebase Emulators**: Starts Firebase emulators for integration tests
11. **Instrumented Tests**: Runs connected tests on Android emulator
12. **Coverage Report**: Generates JaCoCo coverage report
13. **SonarCloud**: Uploads reports to SonarCloud for analysis

**Optimizations:**
- Parallel execution with `--parallel` flag
- Build cache with `--build-cache` flag
- AVD snapshot caching to avoid emulator recreation
- Gradle dependency caching

### 2. APK Build Workflow (`build-apk.yml`)

**Triggers:**
- Manual trigger (workflow_dispatch)
- Push to `release/**` branches

**Steps:**
1. Checkout code
2. Set up JDK 17
3. Decode secrets (keystore, properties)
4. Build release APK
5. Upload APK as artifact ✅ (Already implemented)

## Acceptance Criteria Implementation

### ✅ 1. Upload APK Artifacts for Builds
**Status**: Already implemented in `build-apk.yml`

The workflow already uploads APK artifacts:
```yaml
- name: Upload APK Artifact
  uses: actions/upload-artifact@v4
  with:
    name: release-apk
    path: app/build/outputs/apk/release/*.apk
```

### 📋 2. Add Test Result Reporting in PR Comments

**Implementation**: Add test result parsing and PR comment generation.

See `workflows/ci-improved.yml` for the enhanced workflow with:
- Test result artifact upload
- JUnit XML parsing
- Automated PR comment with test summary

### 📋 3. Implement Test Impact Analysis

**Strategy**: Use Gradle Test Filtering + Changed Files Detection

Benefits:
- Skip tests for unchanged modules
- Reduce CI time for large codebases
- Focus testing on impacted areas

See `workflows/ci-improved.yml` and `scripts/test-impact-analysis.sh` for implementation.

### 📋 4. Build Caching Optimizations

**Current Caching:**
- ✅ Gradle dependencies (via `gradle/actions/setup-gradle@v3`)
- ✅ AVD snapshots
- ✅ Build cache enabled (`--build-cache`)

**Additional Optimizations:**
- Use `gradle/actions/setup-gradle@v3` with cache-read-only on main branch
- Cache Node.js dependencies for Firebase CLI
- Cache SonarCloud analysis cache

See `workflows/ci-improved.yml` for enhanced caching configuration.

### 📋 5. Add Flakiness Detection

**Strategy**: Test retry with result tracking

Implementation approach:
- Run flaky tests multiple times
- Track failure patterns
- Generate flakiness report
- Annotate PR with flaky test warnings

See `workflows/ci-improved.yml` for flakiness detection job.

### 📋 6. Document CI/CD Process

**Status**: ✅ This document

## Test Reports

### Unit Test Results
- Location: `app/build/test-results/`
- Format: JUnit XML
- Processed by: Test reporter action

### Instrumented Test Results
- Location: `app/build/outputs/androidTest-results/`
- Format: JUnit XML
- Coverage: `app/build/reports/coverage/`

### Code Coverage
- Tool: JaCoCo
- Report: `app/build/reports/jacoco/jacocoTestReport/`
- Uploaded to: SonarCloud

## Performance Benchmarks

### Current CI Performance
- **Average CI Duration**: ~15-20 minutes
- **Breakdown**:
  - Checkout & Setup: ~2 minutes
  - Gradle Build: ~3-5 minutes
  - Unit Tests: ~2-3 minutes
  - AVD Setup: ~3-5 minutes (cached: ~30s)
  - Instrumented Tests: ~5-8 minutes
  - Coverage & Analysis: ~2-3 minutes

### Optimization Targets
- **With Impact Analysis**: 30-50% reduction for incremental changes
- **With Enhanced Caching**: ~2-3 minute reduction
- **With Parallel Execution**: Already optimized

## Secrets Management

Required secrets for CI/CD:
- `GOOGLE_SERVICES`: Firebase configuration (base64)
- `FIRESTORE_RULES`: Firestore security rules (base64)
- `STORAGE_RULES`: Storage security rules (base64)
- `LOCAL_PROPERTIES`: Local Android properties (base64)
- `KEYSTORE_FILE`: Release keystore (base64)
- `KEYSTORE_PROPERTIES`: Keystore properties (base64)
- `SONAR_TOKEN`: SonarCloud authentication token

## Troubleshooting

### Common Issues

1. **AVD Creation Timeout**
   - Solution: AVD is cached; first run takes longer

2. **Firebase Emulator Connection Failed**
   - Solution: Check `firebase.json` configuration
   - Ensure emulators section is properly configured

3. **Gradle Build Failure**
   - Solution: Clear Gradle cache
   - Re-run with `--info` flag for details

4. **Test Flakiness**
   - Solution: Enable flakiness detection
   - Run tests multiple times locally
   - Check for timing-dependent assertions

## Best Practices

1. **Commits**
   - Keep commits focused and atomic
   - CI runs on every PR commit

2. **Pull Requests**
   - Ensure CI passes before requesting review
   - Check SonarCloud quality gate
   - Review test coverage changes

3. **Branch Strategy**
   - `main`: Protected, requires CI pass
   - `release/**`: Triggers APK build
   - Feature branches: Full CI on PR

4. **Testing**
   - Write unit tests for business logic
   - Write instrumented tests for UI flows
   - Maintain >80% code coverage

## Future Enhancements

- [ ] Integrate E2E testing framework
- [ ] Add performance testing benchmarks
- [ ] Implement canary deployments
- [ ] Add automated screenshot testing
- [ ] Integrate accessibility testing

## Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Gradle Build Cache](https://docs.gradle.org/current/userguide/build_cache.html)
- [Android CI Best Practices](https://developer.android.com/studio/test/command-line)
- [SonarCloud Integration](https://docs.sonarcloud.io/advanced-setup/ci-based-analysis/github-actions/)
