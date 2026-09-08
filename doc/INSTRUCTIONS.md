# Raspberry Pi Setup Instructions

Step-by-step instructions for configuring a Raspberry Pi to run Robot Pi Server.

---

## 1. OS Installation

Install Raspbian (Raspberry Pi OS) — Bullseye or later recommended. Choose the 32-bit Lite image (no desktop needed). During setup with Raspberry Pi Imager:
- Enable SSH
- Set hostname, username (`pi`), and password
- Configure WiFi

After first boot, update the system:

```bash
sudo apt-get update
sudo apt-get upgrade
sudo reboot
```

---

## 2. Java Runtime

Install the default JRE (Java 11+):

```bash
sudo apt-get install -y default-jre
java -version
```

Expected output:
```
openjdk version "11.0.x" ...
```

---

## 3. Camera Module

Enable the camera in `raspi-config`:

```bash
sudo raspi-config
# Navigate to: Interface Options → Camera → Enable
sudo reboot
```

Verify `raspivid` is available:

```bash
/usr/bin/raspivid --version
```

Test the camera captures frames (runs for 3 seconds):

```bash
raspivid -t 3000 -o /dev/null && echo "Camera OK"
```

---

## 4. VLC (for RTSP Video Streaming)

VLC is used to serve the H.264 camera feed as an RTSP stream that the Android controller receives via `MediaPlayer`.

```bash
sudo apt-get install -y vlc
cvlc --version
```

Verify the full pipeline works (runs for 5 seconds, then exits):

```bash
timeout 5 /usr/bin/raspivid -n -t 0 -h 480 -w 640 -fps 15 -hf -b 2000000 -o - 2>/dev/null \
  | /usr/bin/cvlc -vvv stream:///dev/stdin \
      --sout '#rtp{sdp=rtsp://:8554/}' :demux=h264 2>&1 | head -10
echo "Pipeline test done"
```

You should see VLC startup messages with no fatal errors.

---

## 5. Audio (Optional)

Audio streaming requires a USB audio device with both microphone and speaker/headphone outputs (e.g., a C-Media USB Headphone Set).

**Verify the device is detected:**

```bash
lsusb      # should list your USB audio adapter
aplay -l   # lists playback devices — note the card number
```

Example `aplay -l` output showing card 2:
```
card 2: Set [C-Media USB Headphone Set], device 0: USB Audio [USB Audio]
  Subdevices: 1/1
  Subdevice #0: subdevice #0
```

**Update `config.json`** with the correct mixer name (format: `"Set [plughw:X,0]"` where X is the card number):
```json
"microphoneMixerName": "Set [plughw:2,0]"
```

**Test speaker output:**
```bash
speaker-test -c2 -t sine -f 500
```

**Test microphone recording:**
```bash
arecord -D plughw:2,0 -f cd -d 3 /tmp/test.wav && aplay /tmp/test.wav
```

Audio issues will not prevent connect/disconnect from working — audio streams degrade gracefully if the mixer isn't found.

---

## 6. PWM Servo Controller (Optional)

If using a PCA9685 PWM board for servos/motors:

```bash
sudo apt-get install -y python3-smbus i2c-tools
```

Enable I2C via `raspi-config`:
```
Interface Options → I2C → Enable
```

Verify the PWM board is detected (address 0x40 is typical for PCA9685):
```bash
sudo i2cdetect -y 1
```

---

## 7. WiFi / Networking

Use the IP address (not mDNS hostname) when connecting from Android — mDNS (`raspi.local`) may not resolve reliably from all Android devices.

Find your Pi's IP address:
```bash
hostname -I
```

Use this IP address in the RobotPiController app's SSH Host config field.

---

## 8. Auto-start on Boot (Optional)

To have the server start automatically when the Pi boots (no SSH required after reboot):

**Copy the service files to the Pi:**
```bash
# From your development machine, in the RobotPiServer project root:
scp scripts/robotpiserver.service pi@<pi-ip>:~/
scp scripts/install-service.sh pi@<pi-ip>:~/
```

**Install and start the service on the Pi:**
```bash
ssh pi@<pi-ip> "bash ~/install-service.sh"
```

**Verify the service is running:**
```bash
ssh pi@<pi-ip> "sudo systemctl status robotpiserver"
```

You should see `active (running)`. After this, the server will start automatically on every boot.

**Useful service management commands (run on the Pi):**

| Command | Purpose |
|---|---|
| `sudo systemctl status robotpiserver` | Check status |
| `sudo journalctl -u robotpiserver -f` | Follow live logs |
| `sudo systemctl restart robotpiserver` | Restart after JAR update |
| `sudo systemctl stop robotpiserver` | Stop the service |
| `sudo systemctl disable robotpiserver` | Remove from boot |

> **After updating the JAR:** Copy the new JAR to `/home/pi/`, then `sudo systemctl restart robotpiserver`.
> If the JAR filename changes (e.g., new version), also update `ExecStart` in `/etc/systemd/system/robotpiserver.service` and run `sudo systemctl daemon-reload`.

> **Log location:** Service logs go to `/var/log/robotpi.log`. Logs from a manual `nohup` start go to `/tmp/robotpi.log`.

---

## Next Steps

See the main [README](../README.md) for:
- Building and deploying the server JAR
- `config.json` reference
- HTTP API documentation
- Troubleshooting
