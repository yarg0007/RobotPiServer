# RobotPiServer
===============
Server application that runs on the Raspberry Pi and receives control commands from client.  

Example robot: BruceBot1000 https://www.youtube.com/watch?v=05eA5SQ0DeI

##General Instructions / Initial Setup
---
This has been tested with Raspbian running on the Raspberry PI. There shouldn't be any problem executing on another distro as long as the JVM is supported. Be sure to install with support for WiFi and the Raspberry PI camera when choosing installation settings.

Be sure to configure installation for commandline only. You can boot into GUI mode from the commandline by entering 'startx' to initialize an X session.

First things first, connect via an ethernet connection and do the following:
```
sudo apt-get update
sudo apt-get upgrade
sudo reboot
sudo apt-get install vim
sudo apt-get install oracle-java7-jdk
```

##WiFi Setup Raspberry Pi
---

1. Connect to your WiFi network.  
Assumes use of Wi-Pi adapter (taken from Wi_Pi.User_Manual.pdf)  
```  
root@raspberrypi:~# sudo vi /etc/network/interfaces
```  
In the case of WPA/WPA2, add the following lines to the end of the interfaces document:  
```
auto wlan0  
iface wlan0 inet dhcp  
wpa-ssid <name of your WiFi network>  
wpa-psk <password of your WiFi network>  
```
alternate WPA/WPA2 (auto selecting wasn't the most stable configuration)
```
auto lo  
iface lo inet loopback  
iface eth0 inet dhcp
auto wlan0  
allow-hotplug wlan0
iface wlan0 inet dhcp
wpa-ssid <name of your WiFi network>
wpa-psk <password of your WiFi network>
```
In the case of WEP, add the following instead  
```
auto wlan0  
iface wlan0 inet dhcp  
wireless-essid <name of your WiFi network>  
wireless-key <password of your WiFi network>  
```  
Make sure these files are typed exactly as they are presented here, and also note that you do not type the quotation marks.

restart newtork services:  
```
root@raspberrypi:~# sudo /etc/init.d/networking restart
```
or reboot the raspberry pi
```
root@raspberrypi:~# sudo reboot
```

2. Create a .local domain, for the raspberry pi, to be picked up by bonjour.  
For robot, we used robotpi.local  
```
sudo apt-get install avahi-daemon
sudo vi /etc/hosts
# change 'raspberrypi     ' to 'robotpi     '
sudo vi /etc/hostname
# change 'rapberrypi      ' to 'robotpi     '
sudo /etc/init.d/hostname.sh
sudo reboot
```
[Raspberry Pi .local Domain Name](http://www.howtogeek.com/167190/how-and-why-to-assign-the-.local-domain-to-your-raspberry-pi/)

##Video Streaming
---
Test streaming video from raspberry pi over network.

1. On your Mac:  
Install XQuartz: https://xquartz.macosforge.org  
install homebrew: http://brew.sh/
Install gstreamer 
```
brew install gstreamer gst-libav gst-plugins-ugly gst-plugins-base gst-plugins-bad gst-plugins-good
```

2. On raspberry pi (NOTE THAT EDITING sources.list IS NO LONGER REQUIRED. GSTREAMER IS PART OF RASPBIAN):  
```
sudo vi /etc/apt/sources.list  
# and add to the end: deb http://vontaene.de/raspbian-updates/ . main
sudo apt-get update
sudo apt-get install gstreamer1.0
```

3. Start streaming from raspberry pi:  
```
raspivid -t 999999 -h 720 -w 1080 -fps 25 -hf -b 2000000 -o - | gst-launch-1.0 -v fdsrc ! h264parse !  rtph264pay config-interval=1 pt=96 ! gdppay ! tcpserversink host=YOUR-PI-IP-ADDRESS port=5000
```

4. Receive stream on Mac:  
```
gst-launch-1.0 -v tcpclientsrc host=YOUR-PI-IP-ADDRESS port=5000  ! gdpdepay !  rtph264depay ! avdec_h264 ! videoconvert ! autovideosink sync=false
```

Source: http://blog.tkjelectronics.dk/2013/06/how-to-stream-video-and-audio-from-a-raspberry-pi-with-no-latency/  
Source: http://pi.gbaman.info/?p=150

##Audio Streaming Setup Raspberry Pi
---
Setup audio streaming  
Assumes USB soundcard is plugged in.

1. Execute:
```
lsusb
```
Should se a listing for your Audio Adapter

2. Install alsa drivers  
```
sudo apt-get install alsa-utils
sudo apt-get install alsa-firmware
sudo apt-get install alsa-plugins
sudo reboot
```

3. Check soundcards - We are usinng the C-Media USB Headphone Set  
```
cat /proc/asound/cards
Prints out mlist of sound cards like:
0 [ALSA           ]: BRCM bcm2835 ALSbcm2835 ALSA - bcm2835 ALSA
bcm2835 ALSA
1 [Set            ]: USB-Audio - C-Media USB Headphone Set
C-Media USB Headphone Set at usb-bcm2708_usb-1.2, full speed
```

4. Edit alsa config  
[Raspberry Pi Microphone Setup With USB  Card](http://www.linuxcircle.com/2013/05/08/raspberry-pi-microphone-setup-with-usb-sound-card/)
```
# load the sound driver
sudo modprobe snd_bcm2835
vi /etc/modprobe.d/alsa-base.conf
# Change:
# options snd-usb-audio index=-2
# To:
# options snd-usb-audio index=1 (used to use 0, but you should use 1)
sudo vi /etc/asound.conf
# add the following:
# pcm.!default {
#     type plug
#     slave {
#         pcm "hw:1,0"
#     }
# }
# ctl.!default {
#     type hw
#     card 1
# }
sudo reboot
arecord -D plughw:1,0 -f cd test.wav
aplay test.wav
# adjust volume levels
alsamixev
# save the settings
sudo alsactl store
sudo reboot
```

5. Test audio streaming  
```
speaker-test -c 2
gst-launch-1.0 audiotestsrc ! alsasink
# attach microphone
gst-launch-1.0 alsasrc device=plughw:Set ! alsasink
# (You should hear the microphone through the speakers)
# test microphone recording
arecord -D plughw:0 -r 48000 test.wav
# play recording
aplay -D plughw:0 test.wav
```

PWM-Servo Controller
---
[Raspberry Pi PWM support](http://www.lediouris.net/RaspberryPI/servo/readme.html)  
1. Setup PWM modules
```
vi /etc/modules
# add the following lines:
# i2c-bcm2708
# i2c-dev
```
2. Install utilities
```
sudo apt-get install python-smbus
sudo apt-get install i2c-tools
```
3. Remove i2c support from blacklist
```
vi /etc/modprobe.d/raspi-blacklist.conf
# (comment out following lines to look as such, include hash before each line!)
# #blacklist spi-bcm2708
# #blacklist i2c-bcm2708
sudo reboot
```
4. Confirum i2c Support Enabled
```
# Make sure your PWM board is connected to the Raspberry Pi
sudo i2cdetect -y 1
# look for 40 & 70 as being attached
```

