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

| Requirement | Notes |
|---|---|
| Java | **ARMv6 (Pi 1/Zero):** use pre-installed `/usr/bin/java` (Java 7 only — Java 8+ hangs on ARMv6). **ARMv7+ (Pi 2 and newer):** `sudo apt-get install -y default-jre` |
| Camera | `sudo raspi-config` → Interface Options → Camera |
| VLC | `sudo apt-get install -y vlc` |
| USB audio *(optional)* | Plug in device; disable hardware mic monitoring (see below) |

> **Recommended:** use `scripts/setup-pi.sh` to handle all of the above automatically.

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
  "serverBackLogging": 10,
  "inputControlServerPort": 49801,
  "audioStreamServer": {
    "receivePort": 49809,
    "sendPort": 49808,
    "microphoneMixerName": "Set [plughw:1,0]"
  },
  "videoStreamPort": 5000
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
| `videoStreamPort` | RTSP port for the H.264 video stream (default 5000) |

### Deploy to Pi

**First-time setup** — run this once to install the systemd service:

```bash
# Build
mvn package -Dmaven.test.skip=true

# Copy everything to Pi (replace <pi-ip> with your Pi's IP address)
scp target/RobotPiServer-0.0.1-SNAPSHOT-jar-with-dependencies.jar pi@<pi-ip>:~/
scp config.json pi@<pi-ip>:~/
scp scripts/setup-pi.sh pi@<pi-ip>:~/
scp scripts/robotpiserver.service pi@<pi-ip>:~/

# Run the setup script on the Pi
ssh pi@<pi-ip> "bash ~/setup-pi.sh"
```

**Subsequent deploys** — after the service is installed, just copy the new JAR and restart:

```bash
mvn package -Dmaven.test.skip=true
scp target/RobotPiServer-0.0.1-SNAPSHOT-jar-with-dependencies.jar pi@<pi-ip>:~/
ssh pi@<pi-ip> "sudo systemctl restart robotpiserver"
```

### Auto-start on Boot

To have the server start automatically whenever the Pi boots, install it as a systemd service:

```bash
# From the Pi (after copying the JAR and config.json to /home/pi/)
scp scripts/robotpiserver.service pi@<pi-ip>:~/
scp scripts/install-service.sh pi@<pi-ip>:~/
ssh pi@<pi-ip> "bash ~/install-service.sh"
```

After this, the server starts on every boot without any SSH action. Useful commands on the Pi:

| Command | Purpose |
|---|---|
| `sudo systemctl status robotpiserver` | Check if the service is running |
| `sudo journalctl -u robotpiserver -f` | Follow live log output |
| `sudo systemctl restart robotpiserver` | Restart after updating the JAR |
| `sudo systemctl stop robotpiserver` | Stop the service |
| `sudo systemctl disable robotpiserver` | Remove from boot |

> **Note:** If the JAR filename changes (e.g., after a version bump), update `ExecStart` in `/etc/systemd/system/robotpiserver.service` and run `sudo systemctl daemon-reload`.

> **Log location:** When running as a service, logs go to `/var/log/robotpi.log`. When started manually via `nohup`, logs go to `/tmp/robotpi.log`.

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
      --sout '#rtp{sdp=rtsp://:5000/}' :demux=h264
```

This creates an RTSP server on the Pi. The Android controller connects to:

```
rtsp://<pi-ip>:5000/
```

The port is configured by `videoStreamPort` in `config.json`.

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
- Test the pipeline manually: `timeout 5 raspivid -n -t 0 -o - | cvlc stream:///dev/stdin --sout '#rtp{sdp=rtsp://:5000/}' :demux=h264`
- Check that `videoStreamPort` in `config.json` matches the port set in the Android app's Config screen (default: 5000)
- Check that the port is not blocked by a firewall

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
