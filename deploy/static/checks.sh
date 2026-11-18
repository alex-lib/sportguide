#!/usr/bin/env bash

# A successful reload only signals the Nginx master. Old workers can still serve
# the next request, so wait for the expected response before continuing deployment.
wait_for_http_status() {
  local url=$1 expected=$2
  shift 2
  local attempt status
  for attempt in {1..10}; do
    if ! status=$(curl "$@" -o /dev/null -w '%{http_code}' "$url"); then
      status=000
    fi
    if [ "$status" = "$expected" ]; then
      printf 'HTTP check passed: %s -> %s\n' "$url" "$status"
      return 0
    fi
    printf 'HTTP check %s/10: %s expected %s, received %s\n' \
      "$attempt" "$url" "$expected" "$status" >&2
    if [ "$attempt" -lt 10 ]; then sleep 1; fi
  done
  printf 'HTTP check failed after 10 attempts: %s\n' "$url" >&2
  return 1
}
