#!/usr/bin/env bash
# Builds (if needed) and launches the interactive banking CLI.
# Pass an optional path as the first argument to choose where data is stored.
set -euo pipefail

cd "$(dirname "$0")"
if [ ! -d out/main ]; then
    ./build.sh
fi
java -cp "out/main" com.devin.bank.cli.BankApp "$@"
