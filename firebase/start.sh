#!/bin/bash

DATA_DIR="./data"

firebase emulators:start --import=$DATA_DIR --export-on-exit
