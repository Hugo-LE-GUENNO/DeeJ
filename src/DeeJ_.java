/*
 * DeeJ - An ImageJ plugin detects sound levels of mic or computer and triggers Fiji commands on the currently active window based on a defined threshold.
 * 
 * License: This software is provided under the GNU General Public License (GPL) version 3.
 * Copyright (c) 2025 Hugo LE GUENNO
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */


import ij.IJ;
import ij.ImageJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.NewImage;
import ij.plugin.frame.PlugInFrame;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.util.Random;

import javax.sound.sampled.*;
import javax.swing.*;
import java.net.URL;

public class DeeJ_ extends PlugInFrame implements ActionListener, WindowListener, ImageListener {
	
    private static final long serialVersionUID = 1L;
    
    private ImageIcon playIcon;
    private ImageIcon stopIcon;
    private JButton startStopButton;
    private JProgressBar progressBar;
    private JSlider volumeSlider;
    private JToggleButton rotateToggle;
    private JToggleButton contrastToggle;
    private JToggleButton lutToggle;

    private boolean capturingAudio = false;
    private int progressBarHeight;
    private ImagePlus imp;
    private static final double INTENSITY_THRESHOLD = 0.01;
    // Array of LUT names
    private String[] lutNames = {
            "Red", "Green", "Blue", "Cyan", "Magenta", "Yellow Hot", "Spectrum", "Ice", "Fire",
            "Rainbow RGB", "Cyan Hot", "Orange Hot", "5_Ramps", "Gem"
    };

    public DeeJ_() {
    	super("DeeJ");
    	        // Chargement de l'icône
      try {
          URL iconURL = getClass().getResource("/Resources/DeeJ.ico");
          if (iconURL != null) {
              ImageIcon icon = new ImageIcon(iconURL);
              Image image = icon.getImage();
              // Définit l'icône pour la fenêtre
              setIconImage(image);
          } else {
              IJ.log("Impossible de charger l'icône: fichier non trouvé");
          }
      } catch (Exception e) {
          IJ.log("Erreur lors du chargement de l'icône: " + e.getMessage());
      }

    	
    	// Add window listener
        addWindowListener(this);
        ImagePlus.addImageListener((ImageListener) this);
    	
        // Main panel with GridLayout for 3 columns and 1 row
        JPanel mainPanel = new JPanel(new GridLayout(1, 3));

        // First column panel with GridLayout for 1 column and 3 rows
        JPanel firstColumnPanel = new JPanel(new GridLayout(4, 1));

        // Second column panel with GridLayout for 1 column and 1 row
        JPanel secondColumnPanel = new JPanel(new GridLayout(1, 1));

        // Third column panel with GridLayout for 1 column and 1 row
        JPanel thirdColumnPanel = new JPanel(new GridLayout(1, 1));

        // Load the play and stop icons
        playIcon = new ImageIcon(getClass().getResource("/Resources/play_icon.png"));
        stopIcon = new ImageIcon(getClass().getResource("/Resources/stop_icon.png"));
        
        // First column components
        startStopButton = new JButton(playIcon);
        startStopButton.addActionListener(this);
        startStopButton.setSelected(false);

        rotateToggle = new JToggleButton("Rotate");
        rotateToggle.setSelected(false);
        rotateToggle.setFont(new Font("Arial", Font.PLAIN, 12));
        rotateToggle.setBackground(Color.LIGHT_GRAY);
        rotateToggle.addActionListener(this);

        contrastToggle = new JToggleButton("Contrast");
        contrastToggle.setSelected(false);
        contrastToggle.setFont(new Font("Arial", Font.PLAIN, 12));
        contrastToggle.setBackground(Color.LIGHT_GRAY);
        contrastToggle.addActionListener(this);

        lutToggle = new JToggleButton("LUT");
        lutToggle.setSelected(false);
        lutToggle.setFont(new Font("Arial", Font.PLAIN, 12));
        lutToggle.setBackground(Color.LIGHT_GRAY);
        lutToggle.addActionListener(this);

        firstColumnPanel.add(startStopButton);
        firstColumnPanel.add(rotateToggle);
        firstColumnPanel.add(contrastToggle);
        firstColumnPanel.add(lutToggle);

        // Second column components
        volumeSlider = new JSlider(JSlider.VERTICAL, 0, 10000, 50);
        volumeSlider.setMajorTickSpacing(100);
        volumeSlider.setMinorTickSpacing(20);
        volumeSlider.setPaintTicks(false);
        volumeSlider.setPaintLabels(false);
        
     // Add the MouseWheelListener to the volumeSlider
        volumeSlider.addMouseWheelListener(e -> {
            int notches = e.getWheelRotation();
            int currentValue = volumeSlider.getValue();
            int newValue = currentValue + (notches > 0 ? -100 : 100); // Change by 100 for each notch

            // Ensure the newValue is within the slider's range
            newValue = Math.min(volumeSlider.getMaximum(), Math.max(volumeSlider.getMinimum(), newValue));

            volumeSlider.setValue(newValue);
        });

        secondColumnPanel.add(volumeSlider);

        progressBar = new JProgressBar(JProgressBar.VERTICAL, 0, 1000);
        progressBar.setString(""); // Hide the percentage value

        // Set the background color of the progress bar
        progressBar.setBackground(Color.WHITE);

        progressBarHeight = progressBar.getPreferredSize().height;

        // Create GridBagConstraints
        GridBagConstraints progressBarConstraints = new GridBagConstraints();
        progressBarConstraints.fill = GridBagConstraints.BOTH;
        progressBarConstraints.weightx = 1.0; // Allow horizontal stretching
        progressBarConstraints.weighty = 1.0; // Allow vertical stretching
        progressBarConstraints.gridx = 0;
        progressBarConstraints.gridy = 0;



        thirdColumnPanel.add(progressBar);

        // Add the column panels to the main panel
        mainPanel.add(firstColumnPanel);
        mainPanel.add(secondColumnPanel);
        mainPanel.add(thirdColumnPanel);

        add(mainPanel);
        pack();
        setVisible(true);

        
        if (WindowManager.getImageCount() == 0) {
            try {
                imp = NewImage.createImage("DeeJ", 100, 100, 1, 8, NewImage.FILL_RAMP);
                imp.show();
                IJ.run(imp, "Fire", "");
                IJ.run(imp, "Enhance Contrast", "saturated=0.35");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            imp = IJ.getImage();
        }
    }
    
    private void updateCheckboxColor(JToggleButton toggleButton, boolean isSelected) {
        // Update the color of the toggle button based on its selection
        if (isSelected) {
            // Use light blue color (similar to sky blue)
            Color lightBlue = new Color(173, 216, 230);
            toggleButton.setBackground(lightBlue);
        } else {
            toggleButton.setBackground(Color.WHITE);
        }
    }
    private void updateProgressBarColor(int scaledIntensity) {
        if (scaledIntensity >= 10 && scaledIntensity <= 700) {
            progressBar.setForeground(Color.GREEN);
        } else if (scaledIntensity > 700 && scaledIntensity <= 900) {
            progressBar.setForeground(Color.ORANGE);
        } else if (scaledIntensity > 900 && scaledIntensity <= 1000) {
            progressBar.setForeground(Color.RED);
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startStopButton) {
            if (!capturingAudio) {
                capturingAudio = true;
                startStopButton.setIcon(stopIcon); // Change the icon to "stop"
                startAudioCapture();
            } else {
                capturingAudio = false;
                stopAudioCapture(); // Remove the extra stopAudioCapture() call here
            }
  
        } else if (e.getSource() == rotateToggle) {
            updateCheckboxColor(rotateToggle, rotateToggle.isSelected());
            if (!capturingAudio) {
                double absolutePercentage = progressBar.getValue() * 100.0 / progressBarHeight;
                if (rotateToggle.isSelected()) {
                    adjustRotation(imp, absolutePercentage);
                }
            }
        } else if (e.getSource() == contrastToggle) {
            updateCheckboxColor(contrastToggle, contrastToggle.isSelected());
            if (!capturingAudio) {
                double absolutePercentage = progressBar.getValue() * 100.0 / progressBarHeight;
                if (contrastToggle.isSelected()) {
                    adjustContrast(imp, absolutePercentage);
                }
            }
        } else if (e.getSource() == lutToggle) {
        	updateCheckboxColor(lutToggle, lutToggle.isSelected());
        	 // When LUT checkbox is clicked, change LUT if scaledIntensity is above 700
            if (e.getSource() == lutToggle) {
                if (lutToggle.isSelected()) {
                    int scaledIntensity = progressBar.getValue();
                    if (scaledIntensity > 700) {
                        changeRandomLUT();
                    }
                } 
            } 
        } 
    }
    
        private void changeRandomLUT() {
        	if (WindowManager.getImageCount() != 0)  {
            int randomIndex = new Random().nextInt(lutNames.length);
            String selectedLut = lutNames[randomIndex];
            IJ.run(imp, selectedLut, "");}
            else {updateToggleState();}
        }

        private void startAudioCapture() {
            // Check if an image is available
            ImagePlus currentImage = WindowManager.getCurrentImage();
            if (currentImage == null) {
            }
            else {
	            // Check if the image is in 8-bit format, if not, convert it
	            int imageType = currentImage.getType();
	            if (imageType != ImagePlus.GRAY8) {
	                if (imageType == ImagePlus.GRAY16 || imageType == ImagePlus.COLOR_RGB) {
	                    IJ.run(currentImage, "8-bit", "");
	                }
	            }
            }
            // Now the image is in 8-bit format, proceed with audio capture
            capturingAudio = true;
            updateToggleState();

            SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        AudioFormat audioFormat = new AudioFormat(44100, 16, 1, true, false);
                        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
                        TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
                        targetDataLine.open(audioFormat);
                        targetDataLine.start();

                        AudioInputStream audioInputStream = new AudioInputStream(targetDataLine);

                        byte[] buffer = new byte[2048];
                        int bytesRead;

                        boolean lutChanged = false; // Flag to track if LUT was changed

                        while (capturingAudio) {
                            bytesRead = audioInputStream.read(buffer, 0, buffer.length);

                            double intensity = 0;
                            for (int i = 0; i < bytesRead; i += 2) {
                                short sample = (short) ((buffer[i + 1] << 8) | buffer[i]);
                                intensity = Math.max(intensity, Math.abs(sample) / 32768.0);
                            }

                            int sensitivity = volumeSlider.getValue();
                            int scaledIntensity = (int) (intensity * progressBarHeight * sensitivity / 100.0);

                            if (intensity > INTENSITY_THRESHOLD) {
                                publish(scaledIntensity);
                            } else {
                                publish(0); // If intensity is below threshold, set progress bar to 0
                            }

                            // Check if scaled intensity is above 700 and LUT checkbox is selected
                            if (scaledIntensity > 700 && lutToggle.isSelected()) {
                                if (!lutChanged) {
                                    changeRandomLUT(); // Change LUT once if not already changed
                                    lutChanged = true; // Set the flag to true to avoid repeated changes
                                }
                            } else {
                                lutChanged = false; // Reset the flag if scaled intensity goes below 700 or LUT checkbox is unchecked
                            }

                            Thread.sleep(2);
                        }

                        audioInputStream.close();
                        targetDataLine.close();
                        progressBar.setValue(0);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return null;
                
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                int scaledIntensity = chunks.get(chunks.size() - 1);
                progressBar.setValue(scaledIntensity);
                updateProgressBarColor(scaledIntensity);

                if (scaledIntensity > 700 && lutToggle.isSelected()) {
                    changeRandomLUT();
                }

                if (imp != null) {
                    double absolutePercentage = scaledIntensity * 100.0 / progressBarHeight;
                    if (rotateToggle.isSelected()) {
                        adjustRotation(imp, absolutePercentage);
                    }
                    if (contrastToggle.isSelected()) {
                        adjustContrast(imp, absolutePercentage);
                    }
                }
            }

            @Override
            protected void done() {
                capturingAudio = false;
                startStopButton.setSelected(false);
                stopAudioCapture();
            }
        };

        worker.execute();
    }

    private void stopAudioCapture() {
        // Check if the button is still in "stop" state before starting capture
        if (startStopButton.isSelected()) {
            capturingAudio = true;
            return; // Do not startAudioCapture() if button still in "stop" state
        }

        capturingAudio = false;
        progressBar.setValue(0); // Reset the progress bar to 0
        startStopButton.setIcon(playIcon);
    }
    
   

    
    private void adjustRotation(ImagePlus imp, double absolutePercentage) {
    	if (WindowManager.getImageCount() != 0)  {
            int rotationAngle = 0;
            if (absolutePercentage >= 100 && absolutePercentage < 200) {
                rotationAngle = 1;
            } else if (absolutePercentage >= 200 && absolutePercentage < 300) {
                rotationAngle = 2;
            } else if (absolutePercentage >= 300 && absolutePercentage < 400) {
                rotationAngle = 3;
            } else if (absolutePercentage >= 400 && absolutePercentage < 500) {
                rotationAngle = 4;
            } else if (absolutePercentage >= 600 && absolutePercentage < 700) {
                rotationAngle = 5;
            } else if (absolutePercentage >= 700 && absolutePercentage < 800) {
                rotationAngle = 6;
            } else if (absolutePercentage >= 800 && absolutePercentage < 900) {
                rotationAngle = 7;
            } else if (absolutePercentage >= 900 && absolutePercentage < 1000) {
                rotationAngle = 8;
            } else if (absolutePercentage >= 1100 && absolutePercentage < 1200) {
                rotationAngle = 9;
            } else if (absolutePercentage >= 1200 && absolutePercentage < 1300) {
                rotationAngle = 13;
            } else if (absolutePercentage >= 1300 && absolutePercentage < 1400) {
                rotationAngle = 19;
            } else if (absolutePercentage >= 1400 && absolutePercentage < 1500) {
                rotationAngle = 28;
            } else if (absolutePercentage >= 1500 && absolutePercentage < 1600) {
                rotationAngle = 41;
            }
            IJ.run("Rotate... ", "angle=" + rotationAngle);
        }
        else {updateToggleState();}
    }

    private void adjustContrast(ImagePlus imp, double absolutePercentage) {
    	 if (WindowManager.getImageCount() != 0)  {
            double contrast = 1.0;
            if (absolutePercentage >= 0 && absolutePercentage < 100) {
                contrast = 1.0;
            } else if (absolutePercentage >= 100 && absolutePercentage < 150) {
                contrast = 0.95;
            } else if (absolutePercentage >= 150 && absolutePercentage < 200) {
                contrast = 0.90;
            } else if (absolutePercentage >= 250 && absolutePercentage < 350) {
                contrast = 0.85;
            } else if (absolutePercentage >= 350 && absolutePercentage < 500) {
                contrast = 0.80;
            } else if (absolutePercentage >= 500 && absolutePercentage < 900) {
                contrast = 0.65;
            } else if (absolutePercentage >= 900 && absolutePercentage < 1500) {
                contrast = 0.50;
            } else if (absolutePercentage >= 1500 && absolutePercentage < 2500) {
                contrast = 0.30;
            }
            imp.getProcessor().setMinAndMax(0, 255 * contrast);
            imp.updateAndDraw();
        
        }
        
        else {updateToggleState();}
    
        }
    
    
    public void imageOpened(ImagePlus img) {
        // Réactiver les boutons lorsque qu'une nouvelle image est ouverte
        //startStopButton.setEnabled(true);
    	updateToggleState();
        if (img.getType() != ImagePlus.GRAY8) {
            // Convert the image to 8-bit
            IJ.run(img, "8-bit", "");
        }
        imp = img;
    }

    public void imageUpdated(ImagePlus img) {
        imp = WindowManager.getCurrentImage(); // Update imp to the new current image
        updateToggleState();
    }
    
    @Override
    public void imageClosed(ImagePlus img) {
        if (imp != null && imp == img) {
            imp = WindowManager.getCurrentImage(); // Set imp to the remaining open image
        }
        updateToggleState();
    }
    
    private void updateToggleState() {
        ImagePlus imp = WindowManager.getCurrentImage();
        
        // Enable/disable the toggle buttons based on whether an image is open or not
        if (imp == null) {
            rotateToggle.setSelected(false);
            rotateToggle.setEnabled(false);

            contrastToggle.setSelected(false);
            contrastToggle.setEnabled(false);

            lutToggle.setSelected(false);
            lutToggle.setEnabled(false);
        } else {
            rotateToggle.setEnabled(true);
            contrastToggle.setEnabled(true);
            lutToggle.setEnabled(true);
        }
        // Update button colors only if they are enabled
        if (rotateToggle.isEnabled()) {
            updateCheckboxColor(rotateToggle, rotateToggle.isSelected());
        }
        if (contrastToggle.isEnabled()) {
            updateCheckboxColor(contrastToggle, contrastToggle.isSelected());
        }
        if (lutToggle.isEnabled()) {
            updateCheckboxColor(lutToggle, lutToggle.isSelected());
        }
    }

    
    public static void main(String[] args) {
        new ImageJ();
        DeeJ_ plugin = new DeeJ_();
        plugin.run("");
    }
}
