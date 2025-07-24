#!/bin/bash

DATA_DIR="./data"
TIME_INTERVAL_IN_SECONDS=300

firebase emulators:export $DATA_DIR --force

while true; do
    sleep $TIME_INTERVAL_IN_SECONDS
    firebase emulators:export $DATA_DIR --force
done
