#!/usr/bin/env bash
# Builds the project and runs the dependency-free test suite.
set -euo pipefail

cd "$(dirname "$0")"
./build.sh
echo
echo "Running test suite..."
java -cp "out/main:out/test" com.devin.bank.TestRunner
