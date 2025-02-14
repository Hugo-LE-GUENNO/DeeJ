
# DeeJ 

This plugin detects sound levels and triggers Fiji commands on the currently active window based on a defined threshold.


![Logo](https://github.com/Hugo-LE-GUENNO/DeeJ/blob/main/DeeJ.svg)


## **Requirements**

- Ubuntu 24.04.1 LTS
- Fiji (ImageJ)


## **Installation**

1. Clone this repository to your desired location.
```sh
git clone https://github.com/Hugo-LE-GUENNO/DeeJ.git
```

2. Add the plugin to your Fiji installation's plugins folder.
3. Restart Fiji.
4. Enjoy !


## **Usage**
```mermaid
graph TD;
    A[Sound Detection] -->|Exceeds Threshold| B{Trigger Fiji Command};
    B -->|Rotate| C[Active Image Window];
    B -->|Enhance Contrast| C;
    B -->|Change LUT| C;




## **License**

This project is licensed under the GPLv3 License.