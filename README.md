DeeJ

This plugin detects sound levels and triggers Fiji commands on the currently active window based on a defined threshold.


Features:

Sound Detection Sensitivity: Adjustable sensitivity slider for detecting sound levels.
Threshold-Based Command Execution: When the sound level exceeds the defined threshold, it automatically triggers a Fiji command (Rotate, Contrast or LUT).
Active Window Detection: Executes the command on the currently active Fiji window.


Requirements:

Ubuntu 24.04.1 LTS
Fiji (ImageJ)


Installation:

Clone this repository to your desired location.
        git clone https://github.com/Hugo-LE-GUENNO/DeeJ.git
Add the plugin to your Fiji installation's plugins folder.
Restart Fiji.
Enjoy !


Usage:

Set the sensitivity slider to adjust the sound detection level.
Configure the sound threshold at which commands will be triggered.
When the threshold is exceeded, a Fiji command (Rotate, Enhance Contrast, Change LUT) will be executed on the active image window.



License:

This project is licensed under the GPLv3 License.