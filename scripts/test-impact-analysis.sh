#!/bin/bash

# Test Impact Analysis Script
# Analyzes changed files and determines which tests need to be run

set -e

BASE_SHA=${1:-"origin/main"}
HEAD_SHA=${2:-"HEAD"}

echo "🔍 Analyzing test impact between $BASE_SHA and $HEAD_SHA"

# Get list of changed files
CHANGED_FILES=$(git diff --name-only "$BASE_SHA" "$HEAD_SHA")

echo "📝 Changed files:"
echo "$CHANGED_FILES"
echo ""

# Initialize test filter
TEST_FILTER=""
RUN_ALL_TESTS=false

# Analyze changed files
while IFS= read -r file; do
  case "$file" in
    # Changes to build files require full test suite
    *.gradle.kts|*.gradle|gradle.properties|settings.gradle*)
      echo "⚠️ Build configuration changed: $file"
      RUN_ALL_TESTS=true
      break
      ;;

    # Changes to source code
    app/src/main/java/*)
      # Extract package path
      PACKAGE_PATH=$(echo "$file" | sed 's|app/src/main/java/||' | sed 's|\.kt$||' | sed 's|/|.|g')
      echo "📦 Source file changed: $PACKAGE_PATH"

      # Find corresponding test file
      TEST_FILE=$(echo "$file" | sed 's|/main/|/test/|' | sed 's|\.kt$|Test.kt|')
      if [ -f "$TEST_FILE" ]; then
        echo "  ✓ Found test file: $TEST_FILE"
        TEST_FILTER="$TEST_FILTER $TEST_FILE"
      else
        echo "  ⚠️ No corresponding test file found"
      fi

      # Also check for instrumented tests
      INSTRUMENTED_TEST=$(echo "$file" | sed 's|/main/|/androidTest/|' | sed 's|\.kt$|Test.kt|')
      if [ -f "$INSTRUMENTED_TEST" ]; then
        echo "  ✓ Found instrumented test: $INSTRUMENTED_TEST"
      fi
      ;;

    # Changes to test files
    app/src/test/*)
      echo "🧪 Test file changed: $file"
      TEST_FILTER="$TEST_FILTER $file"
      ;;

    app/src/androidTest/*)
      echo "🧪 Instrumented test changed: $file"
      # Would need to run connectedCheck for these
      ;;

    # Changes to resources might affect tests
    app/src/main/res/*|app/src/main/assets/*)
      echo "📱 Resource changed: $file"
      # Could be more selective here
      ;;

    # Other files
    *)
      echo "📄 Other file changed: $file"
      ;;
  esac
done <<< "$CHANGED_FILES"

# Determine test strategy
if [ "$RUN_ALL_TESTS" = true ]; then
  echo ""
  echo "🚀 Running FULL test suite (build config or critical files changed)"
  echo "test_filter=full" >> "$GITHUB_OUTPUT"
  exit 0
fi

if [ -z "$TEST_FILTER" ]; then
  echo ""
  echo "✨ No tests need to be run (no source code changes detected)"
  echo "test_filter=none" >> "$GITHUB_OUTPUT"
else
  echo ""
  echo "🎯 Running SELECTIVE tests:"
  echo "$TEST_FILTER"
  echo "test_filter=selective" >> "$GITHUB_OUTPUT"

  # Could generate a test filter file for Gradle
  # echo "$TEST_FILTER" > test-filter.txt
fi

echo ""
echo "✅ Test impact analysis complete"
