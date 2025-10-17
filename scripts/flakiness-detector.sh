#!/bin/bash

# Flakiness Detection Script
# Runs tests multiple times and detects inconsistent results

set -e

RUNS=${1:-3}
OUTPUT_DIR="flakiness-reports"

echo "🔬 Starting flakiness detection with $RUNS test runs"

# Create output directory
mkdir -p "$OUTPUT_DIR"

# Array to store test results
declare -A test_results

# Function to parse JUnit XML and extract test results
parse_junit_xml() {
  local run_number=$1
  local xml_dir="app/build/test-results"

  if [ ! -d "$xml_dir" ]; then
    echo "⚠️ No test results found in $xml_dir"
    return
  fi

  # Find all XML files and parse them
  find "$xml_dir" -name "*.xml" -type f | while read -r xml_file; do
    # Extract test cases using grep and awk (simplified parsing)
    # In production, would use proper XML parser
    grep -oP '(?<=<testcase name=")[^"]*' "$xml_file" 2>/dev/null | while read -r test_name; do
      # Check if test failed
      if grep -q "name=\"$test_name\".*<failure" "$xml_file" 2>/dev/null; then
        echo "$test_name:FAIL:$run_number" >> "$OUTPUT_DIR/all-results.txt"
      else
        echo "$test_name:PASS:$run_number" >> "$OUTPUT_DIR/all-results.txt"
      fi
    done
  done
}

# Run tests multiple times
for ((i=1; i<=RUNS; i++)); do
  echo ""
  echo "🔄 Test run $i/$RUNS"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

  # Clean previous results
  ./gradlew clean > /dev/null 2>&1

  # Run tests (allow failures)
  if ./gradlew check --rerun-tasks 2>&1 | tee "$OUTPUT_DIR/run-$i.log"; then
    echo "✅ Run $i: All tests passed"
    echo "RUN_$i:PASS" >> "$OUTPUT_DIR/run-summary.txt"
  else
    echo "❌ Run $i: Some tests failed"
    echo "RUN_$i:FAIL" >> "$OUTPUT_DIR/run-summary.txt"
  fi

  # Parse results
  parse_junit_xml "$i"

  # Small delay between runs
  sleep 2
done

echo ""
echo "📊 Analyzing results for flakiness..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Analyze results
if [ -f "$OUTPUT_DIR/all-results.txt" ]; then
  # Find tests with inconsistent results
  awk -F: '{results[$1] = results[$1] " " $2} END {
    for (test in results) {
      if (results[test] ~ /PASS/ && results[test] ~ /FAIL/) {
        print "🔴 FLAKY: " test
        print "   Results:" results[test]
        flaky_count++
      }
    }
    if (flaky_count > 0) {
      print ""
      print "⚠️ Found " flaky_count " flaky test(s)"
      exit 1
    } else {
      print "✅ No flaky tests detected"
      exit 0
    }
  }' "$OUTPUT_DIR/all-results.txt" | tee "$OUTPUT_DIR/flakiness-report.txt"
else
  echo "⚠️ No results to analyze"
fi

echo ""
echo "📁 Reports saved to: $OUTPUT_DIR/"
echo "  - all-results.txt: All test results across runs"
echo "  - flakiness-report.txt: Flakiness analysis"
echo "  - run-*.log: Individual run logs"
echo ""

# Exit with failure if flaky tests were found
if grep -q "FLAKY" "$OUTPUT_DIR/flakiness-report.txt" 2>/dev/null; then
  exit 1
fi

exit 0
