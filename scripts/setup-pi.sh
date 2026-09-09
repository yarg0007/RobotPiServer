#!/bin/bash
# setup-pi.sh — One-time Raspberry Pi setup for RobotPi Server.
#
# Run this script on the Pi after copying the JAR and config.json to /home/pi/.
# It is safe to run multiple times.
#
# Usage:
#   bash setup-pi.sh
#   bash setup-pi.sh --no-audio   # skip USB audio configuration
#   bash setup-pi.sh --no-service # skip systemd service installation

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# -----------------------------------------------------------------------
# Parse flags
# -----------------------------------------------------------------------
SKIP_AUDIO=0
SKIP_SERVICE=0
for arg in "$@"; do
    case "$arg" in
        --no-audio)   SKIP_AUDIO=1 ;;
        --no-service) SKIP_SERVICE=1 ;;
        -h|--help)
            echo "Usage: $0 [--no-audio] [--no-service]"
            exit 0 ;;
        *)
            echo "Unknown argument: $arg  (use --help for usage)"
            exit 1 ;;
    esac
done

# -----------------------------------------------------------------------
# Helpers
# -----------------------------------------------------------------------
print_header() { echo; echo "=== $1 ==="; }
ok()   { echo "  [OK] $1"; }
warn() { echo "  [WARN] $1"; }
fail() { echo "  [ERROR] $1"; exit 1; }

# -----------------------------------------------------------------------
# 1. Architecture detection → Java validation
# -----------------------------------------------------------------------
print_header "Architecture and Java"

ARCH="$(uname -m)"
echo "  Detected CPU architecture: $ARCH"

if [ "$ARCH" = "armv6l" ]; then
    # Pi 1 / Pi Zero (BCM2835) — ARMv6 hard-float
    # Modern JREs (Java 8+) hang or crash on ARMv6.
    # Only the pre-installed Java 7 at /usr/bin/java works.
    echo "  ARMv6 detected — only Java 7 is compatible with this CPU."
    echo "  DO NOT install a newer JRE; Java 8+ hangs on ARMv6."

    if [ -x /usr/bin/java ]; then
        JAVA_BIN=/usr/bin/java
        JAVA_VER="$(/usr/bin/java -version 2>&1 | head -1)"
        ok "Java found at /usr/bin/java: $JAVA_VER"
    else
        fail "/usr/bin/java not found. On ARMv6 this should be pre-installed by Raspbian. Re-flash and retry."
    fi
else
    # Pi 2 / 3 / 4 / 5 / Zero 2 — ARMv7 or aarch64
    echo "  ARMv7+ / aarch64 detected — installing default-jre (Java 11+)."
    sudo apt-get update -qq
    sudo apt-get install -y default-jre
    JAVA_BIN="$(command -v java)"
    JAVA_VER="$($JAVA_BIN -version 2>&1 | head -1)"
    ok "Java installed: $JAVA_VER"
fi

# Confirm the java binary works
"$JAVA_BIN" -version >/dev/null 2>&1 || fail "Java at $JAVA_BIN does not execute correctly."

# -----------------------------------------------------------------------
# 2. VLC (for RTSP video streaming)
# -----------------------------------------------------------------------
print_header "VLC"

if command -v cvlc >/dev/null 2>&1; then
    ok "VLC already installed: $(cvlc --version 2>&1 | head -1)"
else
    echo "  Installing VLC..."
    sudo apt-get update -qq
    sudo apt-get install -y vlc
    ok "VLC installed."
fi

# -----------------------------------------------------------------------
# 3. USB audio configuration
# -----------------------------------------------------------------------
if [ "$SKIP_AUDIO" -eq 0 ]; then
    print_header "USB Audio"

    # Find the card number of the C-Media USB audio device (or any USB audio).
    CARD_NUM=""
    while IFS= read -r line; do
        if echo "$line" | grep -qi "USB"; then
            CARD_NUM="$(echo "$line" | grep -o 'card [0-9]*' | grep -o '[0-9]*')"
            break
        fi
    done < <(aplay -l 2>/dev/null)

    if [ -z "$CARD_NUM" ]; then
        warn "No USB audio device found (aplay -l lists none with 'USB')."
        warn "Plug in your USB audio adapter and re-run this script, or use --no-audio."
    else
        MIXER_NAME="Set [plughw:${CARD_NUM},0]"
        echo "  Found USB audio on card $CARD_NUM (mixer name: \"$MIXER_NAME\")"

        # Disable hardware microphone monitoring (mic → speaker loopback).
        # C-Media adapters enable this by default, which causes the Pi mic to
        # play directly through the Pi speaker regardless of any software routing.
        if amixer -c "$CARD_NUM" sget Mic 2>/dev/null | grep -q "Playback"; then
            amixer -c "$CARD_NUM" set Mic playback 0% >/dev/null
            amixer -c "$CARD_NUM" set Mic playback off >/dev/null
            ok "Mic hardware monitoring (loopback) disabled."
        else
            ok "No Mic Playback control on this device — skipping loopback disable."
        fi

        # Disable Auto Gain Control — causes unpredictable volume jumps.
        if amixer -c "$CARD_NUM" sget 'Auto Gain Control' 2>/dev/null | grep -q "Playback"; then
            amixer -c "$CARD_NUM" set 'Auto Gain Control' off >/dev/null
            ok "Auto Gain Control disabled."
        else
            ok "No Auto Gain Control on this device — skipping."
        fi

        # Persist settings so they survive reboots.
        sudo alsactl store
        ok "ALSA state saved (will be restored on next boot)."

        echo
        echo "  config.json microphoneMixerName should be:"
        echo "    \"microphoneMixerName\": \"$MIXER_NAME\""
    fi
else
    echo "  Skipping audio configuration (--no-audio)."
fi

# -----------------------------------------------------------------------
# 4. Verify JAR and config.json are present
# -----------------------------------------------------------------------
print_header "Deployment Files"

JAR_PATH=~/RobotPiServer-0.0.1-SNAPSHOT-jar-with-dependencies.jar
CFG_PATH=~/config.json

if [ -f "$JAR_PATH" ]; then
    ok "Server JAR found: $JAR_PATH"
else
    warn "Server JAR not found at $JAR_PATH"
    warn "Copy it with:"
    warn "  scp target/RobotPiServer-0.0.1-SNAPSHOT-jar-with-dependencies.jar pi@<pi-ip>:~/"
fi

if [ -f "$CFG_PATH" ]; then
    ok "config.json found: $CFG_PATH"
else
    warn "config.json not found at $CFG_PATH"
    # Emit a ready-to-use default.  The user will need to update microphoneMixerName.
    cat > "$CFG_PATH" << 'EOF'
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
EOF
    warn "Created a default config.json. Update microphoneMixerName to match your USB audio card."
fi

# -----------------------------------------------------------------------
# 5. Systemd service
# -----------------------------------------------------------------------
if [ "$SKIP_SERVICE" -eq 0 ]; then
    print_header "Systemd Service"

    SERVICE_SRC="$SCRIPT_DIR/robotpiserver.service"

    if [ ! -f "$SERVICE_SRC" ]; then
        # Try finding the service file relative to a common install path
        SERVICE_SRC="$(dirname "$SCRIPT_DIR")/scripts/robotpiserver.service"
    fi

    if [ ! -f "$SERVICE_SRC" ]; then
        warn "robotpiserver.service not found next to this script. Skipping service install."
        warn "Copy scripts/robotpiserver.service to the Pi and run install-service.sh manually."
    else
        # Patch ExecStart to use the correct java binary for this architecture.
        TMP_SERVICE="$(mktemp)"
        sed "s|ExecStart=.*|ExecStart=$JAVA_BIN -jar $JAR_PATH|" "$SERVICE_SRC" > "$TMP_SERVICE"

        sudo cp "$TMP_SERVICE" /etc/systemd/system/robotpiserver.service
        rm -f "$TMP_SERVICE"

        sudo systemctl daemon-reload
        sudo systemctl enable robotpiserver
        sudo systemctl restart robotpiserver
        sleep 2

        if systemctl is-active --quiet robotpiserver; then
            ok "robotpiserver service is running."
        else
            warn "Service did not start cleanly. Check logs with:"
            warn "  sudo journalctl -u robotpiserver -n 50"
        fi
    fi
else
    echo "  Skipping service installation (--no-service)."
fi

# -----------------------------------------------------------------------
# Summary
# -----------------------------------------------------------------------
print_header "Setup Complete"
echo "  Useful commands:"
echo "    sudo systemctl status robotpiserver    — check service status"
echo "    sudo journalctl -u robotpiserver -f    — follow live logs"
echo "    sudo systemctl restart robotpiserver   — restart after JAR update"
echo "    aplay -l                               — list audio devices"
echo "    amixer -c 1 scontents                  — inspect ALSA mixer controls"
echo
