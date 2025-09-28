# Telemetry Lab 

An Android app built with **Jetpack Compose**, **ForegroundService**, **Macrobenchmark**, and **Baseline Profiles**.

## Features
- Foreground service simulating edge inference pipeline.
- Real-time telemetry dashboard:
    - Frame latency (ms)
    - Moving average latency
    - Jank % (last 30s)
    - Power save mode indicator
- Start/Stop toggle & Compute Load slider (1–5).
- Animated counter for visible UI work.
- Macrobenchmark tests for cold startup & jank.

## Modules
- `app/` → Main app.
- `benchmark/` → Macrobenchmark tests.
