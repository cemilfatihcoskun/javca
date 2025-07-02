#!/bin/bash

EXPORT_DIR="./firebase_data"
INTERVAL=300

while true; do
  timestamp=$(date +%Y-%m-%d_%H-%M-%S)
  firebase emulators:export "./emulator_export_latest"
  echo "Exported at $timestamp"
  sleep $INTERVAL
done

