
DeeJ 

This plugin detects sound levels and triggers Fiji commands on the currently active window based on a defined threshold.


![Logo](https://github.com/Hugo-LE-GUENNO/DeeJ/blob/main/DeeJ.svg)


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

- Add the plugin to your Fiji installation's plugins folder.
- Restart Fiji.
- Enjoy !



graph TD;
    A[Sound Detection] -->|Exceeds Threshold| B{Trigger Fiji Command};
    B -->|Rotate| C[Active Image Window];
    B -->|Enhance Contrast| C;
    B -->|Change LUT| C;




License:

This project is licensed under the GPLv3 License.