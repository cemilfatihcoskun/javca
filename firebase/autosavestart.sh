#!/bin/bash

# 🔧 Firebase export klasörü
EXPORT_DIR="./firebase_data"

# 🔁 Export aralığı (saniye cinsinden)
INTERVAL=300

# 🚀 Firebase emülatörleri başlat
firebase emulators:start --import="$EXPORT_DIR" --export-on-exit &

# 🔢 Emülatörün PID'sini al
EMULATOR_PID=$!

echo "Firebase Emulators started with PID $EMULATOR_PID"
echo "Exporting to $EXPORT_DIR every $INTERVAL seconds..."

# ❗ Çıkış sinyali geldiğinde emülatörü düzgün kapat
trap "echo 'Stopping...'; kill $EMULATOR_PID; wait $EMULATOR_PID; firebase emulators:export $EXPORT_DIR; echo 'Final export done.'; exit 0" SIGINT SIGTERM

# 🔄 Sürekli export et
while kill -0 $EMULATOR_PID 2>/dev/null; do
    sleep $INTERVAL
    echo "Auto-exporting Firebase emulator data..."
    firebase emulators:export "$EXPORT_DIR" > /dev/null 2>&1
done

