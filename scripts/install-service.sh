#!/bin/bash
# Installs the RobotPi Server as a systemd service that starts automatically on boot.
# Run this once from the Pi after copying the JAR and config.json to /home/pi/.
set -e

SCRIPTS_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "Installing RobotPi Server systemd service..."
sudo cp "$SCRIPTS_DIR/robotpiserver.service" /etc/systemd/system/robotpiserver.service
sudo systemctl daemon-reload
sudo systemctl enable robotpiserver
sudo systemctl start robotpiserver

echo ""
echo "Done. Useful commands:"
echo "  Status:  sudo systemctl status robotpiserver"
echo "  Logs:    sudo journalctl -u robotpiserver -f"
echo "  Stop:    sudo systemctl stop robotpiserver"
echo "  Restart: sudo systemctl restart robotpiserver"
