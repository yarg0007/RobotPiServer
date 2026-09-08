# Robot Pi Server

HTTP server that runs on a Raspberry Pi and exposes an API for the [RobotPiController](https://github.com/yarg0007/RobotPiController) Android app to connect, stream video/audio, and send robot control commands.

Example robot running this code: BruceBot1000 — https://www.youtube.com/watch?v=05eA5SQ0DeI

---

## API Documentation

- [Server API (OpenAPI)](https://yarg0007.github.io/RobotPiServer/swaggerdist/index.html?spec=../src/main/resources/api/openapi.yaml) — `/connect` and `/disconnect` endpoints
- [Configuration Model](https://yarg0007.github.io/RobotPiServer/swaggerdist/index.html?spec=../src/main/resources/api/configuration.yaml) — `config.json` schema

---

## Quick Start

### Prerequisites

On your Raspberry Pi, install:

| Requirement | Install Command | Verify |
|---|---|---|
| Java 11+ | `sudo apt-get install -y default-jre` | `java -version` |
| Camera enabled | `sudo raspi-config` → Interface Options → Camera | `/usr/bin/raspivid --version` |
| VLC (for RTSP) | `sudo apt-get install -y vlc` | `cvlc --version` |
| USB audio *(optional)* | plug in device, check `aplay -l` | `speaker-test -c2` |

### Build

```bash
mvn package -DskipTests
```

This produces two JARs in `target/`:
- `RobotPiServer-0.0.1-SNAPSHOT.jar` — thin JAR, cannot run standalone
- `RobotPiServer-0.0.1-SNAPSHOT-jar-with-dependencies.jar` — **deploy this one**

### Configure

Create `config.json` in the same directory as the JAR:

```json
{
  "serverPort": 8001,
  "serverBackLogging": 1,
  "inputControlServerPort": 49801,
  "audioStreamServer": {
    "receivePort": 49809,
    "sendPort": 49808,
    "microphoneMixerName": "Set [plughw:2,0]"
  },
  "videoStreamPort": 8554
}
```

| Field | Description |
|---|---|
| `serverPort` | HTTP API port (default 8001) |
| `serverBackLogging` | HTTP server backlog queue size |
| `inputControlServerPort` | UDP port for robot control input from the controller app |
| `audioStreamServer.receivePort` | UDP port the server listens on for audio from the controller |
| `audioStreamServer.sendPort` | UDP port the server sends microphone audio to the controller |
| `audioStreamServer.microphoneMixerName` | ALSA mixer name for the USB audio device. Run `aplay -l` on the Pi to find the card number, then format as `"Set [plughw:X,0]"`. Audio is optional — the server degrades gracefully if the mixer is not found. |
| `videoStreamPort` | RTSP port for the H.264 video stream (default 8554) |

### Deploy to Pi

```bash
# Build
mvn package -DskipTests

# Copy to Pi (replace <pi-ip> with your Pi's IP address)
scp target/RobotPiServer-0.0.1-SNAPSHOT-jar-with-dependencies.jar pi@<pi-ip>:~/
scp config.json pi@<pi-ip>:~/

# Start server (runs in background, survives SSH logout)
ssh pi@<pi-ip> "nohup sudo java -jar ~/RobotPiServer-0.0.1-SNAPSHOT-jar-with-dependencies.jar > /tmp/robotpi.log 2>&1 &"

# Check it started
ssh pi@<pi-ip> "cat /tmp/robotpi.log"

# Stop server
ssh pi@<pi-ip> "sudo pkill -f 'java -jar'"
```

---

## HTTP API

### `GET /connect`

Registers a controller client and starts all streams (video, audio, control input).

- **Returns:** `200 OK` with `{"message": "Connection established."}`
- **Idempotent:** Calling `/connect` when already connected tears down the existing session and starts a new one.

```bash
curl -X GET http://<pi-ip>:8001/connect
```

### `POST /disconnect`

Stops all streams. Optionally shuts down the Pi.

- **Body:** `{"shutdown": false}` — disconnect only, server stays running
- **Body:** `{"shutdown": true}` — disconnect and shut down the Pi
- **Returns:** `200 OK` — always succeeds (idempotent, safe to call when not connected)

```bash
curl -X POST http://<pi-ip>:8001/disconnect -H "Content-Type: application/json" -d '{"shutdown":false}'
```

---

## Video Streaming

When `/connect` is called, the server launches this pipeline:

```bash
/usr/bin/raspivid -n -t 0 -h 480 -w 640 -fps 15 -hf -b 2000000 -o - \
  | /usr/bin/cvlc -vvv stream:///dev/stdin \
      --sout '#rtp{sdp=rtsp://:8554/}' :demux=h264
```

This creates an RTSP server on the Pi. The Android controller connects to:

```
rtsp://<pi-ip>:8554/
```

The stream is started on `/connect` and stopped on `/disconnect`.

---

## Troubleshooting

**Server exits immediately after starting:**
- Check `/tmp/robotpi.log` for the error
- Ensure `config.json` is in the same directory as the JAR (`~/config.json`)
- Verify Java is installed: `java -version`

**Video not appearing in the app:**
- Verify the camera is enabled: `vcgencmd get_camera` should show `supported=1 detected=1`
- Verify VLC is installed: `cvlc --version`
- Test the pipeline manually: `timeout 5 raspivid -n -t 0 -o - | cvlc stream:///dev/stdin --sout '#rtp{sdp=rtsp://:8554/}' :demux=h264`
- Check that port 8554 is not blocked by a firewall

**Audio not working:**
- Run `aplay -l` to find the USB audio card number
- Update `microphoneMixerName` in `config.json` to match (e.g. `"Set [plughw:2,0]"`)
- Audio failures are non-fatal — the connect/disconnect flow works without audio

**"Connection refused" when tapping CONNECT in the app:**
- The server may not have started yet — the app retries 5 times with a 3-second delay
- SSH to the Pi and run `cat /tmp/robotpi.log` to check server status

**App shows SSH error with `raspi.local`:**
- Use the Pi's IP address (`hostname -I` on the Pi) instead of the `.local` hostname in the app config — mDNS may not resolve from Android

---

## Full Setup Instructions

See [doc/INSTRUCTIONS.md](doc/INSTRUCTIONS.md) for step-by-step Raspberry Pi OS setup including Java, camera, VLC, audio, and networking configuration.

---

## Other Documentation

- [Contributing](doc/CONTRIBUTING.md)
- [TODO](doc/TODO.md)
