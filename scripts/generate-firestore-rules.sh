#!/bin/bash

# Script to generate Firestore rules from annotated Kotlin data classes
# This compiles and runs the generator without needing complex Gradle configuration

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/.."
APP_DIR="$PROJECT_ROOT/app"

echo "🔥 Generating Firestore Rules..."

# Build the project first
cd "$PROJECT_ROOT"
./gradlew assembleDebug

# Run the generator using the compiled classes
./gradlew :app:run \
    -PmainClass=ch.eureka.eurekapp.codegen.RulesGeneratorMainKt \
    -Doutput.path="$PROJECT_ROOT/firestore.rules"

echo "✅ Done! Rules generated at: $PROJECT_ROOT/firestore.rules"
