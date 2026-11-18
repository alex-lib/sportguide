#!/usr/bin/env bash
set -Eeuo pipefail
cd "$(dirname "$0")/../.."
source deploy/static/checks.sh

test_dir=$(mktemp -d)
trap 'rm -rf "$test_dir"' EXIT

# Deterministic response sequence; no network, sleeps or production writes.
curl() {
  local count response
  count=$(cat "$test_dir/count")
  count=$((count + 1))
  printf '%s\n' "$count" > "$test_dir/count"
  response=$(sed -n "${count}p" "$test_dir/responses")
  if [ "$response" = network-error ]; then return 7; fi
  printf '%s' "$response"
}
sleep() { printf 'retry\n' >> "$test_dir/sleeps"; }

prepare() {
  printf '0\n' > "$test_dir/count"
  : > "$test_dir/sleeps"
  printf '%s\n' "$@" > "$test_dir/responses"
}

prepare 301 308
wait_for_http_status https://example.invalid/landing 308 > "$test_dir/output" 2>&1
test "$(cat "$test_dir/count")" = 2
test "$(wc -l < "$test_dir/sleeps" | tr -d ' ')" = 1
grep -q 'expected 308, received 301' "$test_dir/output"
echo 'PASS: old worker 301 followed by new worker 308 succeeds'

prepare network-error 308
wait_for_http_status https://example.invalid/landing 308 > "$test_dir/output" 2>&1
test "$(cat "$test_dir/count")" = 2
grep -q 'received 000' "$test_dir/output"
echo 'PASS: transient connection failure is retried'

prepare 301 301 301 301 301 301 301 301 301 301
if wait_for_http_status https://example.invalid/landing 308 > "$test_dir/output" 2>&1; then
  echo 'FAIL: persistent wrong status must fail deployment' >&2
  exit 1
fi
test "$(cat "$test_dir/count")" = 10
test "$(wc -l < "$test_dir/sleeps" | tr -d ' ')" = 9
grep -q 'failed after 10 attempts' "$test_dir/output"
echo 'PASS: persistent incorrect status fails after a bounded number of attempts'

prepare 404
wait_for_http_status https://example.invalid/landing/missing.js 404 > "$test_dir/output" 2>&1
test "$(cat "$test_dir/count")" = 1
test ! -s "$test_dir/sleeps"
echo 'PASS: expected 404 succeeds immediately'
