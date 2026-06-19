#!/usr/bin/env bash
# Compiles the banking system using only the JDK (no external dependencies).
set -euo pipefail

cd "$(dirname "$0")"
OUT="out"
rm -rf "$OUT"
mkdir -p "$OUT/main" "$OUT/test"

echo "Compiling main sources..."
find src/main/java -name '*.java' > sources.txt
javac -d "$OUT/main" @sources.txt

echo "Compiling test sources..."
find src/test/java -name '*.java' > test-sources.txt
javac -cp "$OUT/main" -d "$OUT/test" @test-sources.txt

rm -f sources.txt test-sources.txt
echo "Build complete. Classes are in $OUT/."
