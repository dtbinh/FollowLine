package rbt;

import lejos.nxt.ColorLightSensor;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.addon.CompassSensor;
import lejos.nxt.LCD;
import lejos.nxt.Sound;
import lejos.nxt.Battery;
import lejos.nxt.comm.*;
import lejos.robotics.navigation.*;
import rbt.PIDController;

import java.io.*;

public class Robot {


    public static void main(String[] args) throws Exception {

        final int COMMAND_BATTERY_VOLTAGE = 0;
        final int COMMAND_TRAVEL = 1;
        final int COMMAND_TURN = 2;
        final int COMMAND_READ_COLOR = 3;
        final int COMMAND_DISCONNECT = 4;
        int[] command = new int[3];
        int[] reply = new int[8];
        boolean keepItRunning = true;
        String connected = "Connected";
        String waiting = "Waiting...";
        String closing = "Closing...";
        DataInputStream dis;
        DataOutputStream dos;
        BTConnection btc;

        PIDController pidController = new PIDController(150, 0, 0);
        int offset = 33;
        int Tp = 50;
        int lightValue , turn, powerA, powerC;
        Motor motorA = Motor.A;
        Motor motorC = Motor.C;
        ColorLightSensor colorLightSensor = new ColorLightSensor(SensorPort.S2, ColorLightSensor.TYPE_COLORRED);
        //UltrasonicSensor ultrasonicSensor = new UltrasonicSensor(SensorPort.S1);
        CompassSensor compassSensor = new CompassSensor(SensorPort.S3);
        CompassPilot compassPilot = new CompassPilot(compassSensor, 56.0f, 101.0f, motorA, motorC);

        LCD.drawString(waiting, 0, 0);
        LCD.refresh();

        Sound.twoBeeps();

        btc = Bluetooth.waitForConnection();

        LCD.clear();
        LCD.drawString(connected, 0, 0);
        LCD.refresh();

        dis = btc.openDataInputStream();
        dos = btc.openDataOutputStream();

        Sound.beepSequenceUp();
        compassPilot.calibrate();
        while (keepItRunning) {
            // Fetch the Master's command
            for (int k = 0; k < 3; k++) {
                command[k] = dis.readInt();
            }

            LCD.clear();
            LCD.drawInt(command[0], 0, 0);
            LCD.drawInt(command[1], 0, 2);
            LCD.drawInt(command[2], 0, 4);
            LCD.refresh();
            pidController.start();

            // Respond to the Master's command which is stored in command[0]

            switch (command[0]) {
                case COMMAND_BATTERY_VOLTAGE:
                    reply[0] = Battery.getVoltageMilliVolt();
                    for (int n = 1; n < 8; n++) {
                        reply[n] = 0;
                    }
                    break;
                case COMMAND_TRAVEL:
                    int length = command[1];
                    while (true) {
                        if (getMM((motorA.getTachoCount() + motorC.getTachoCount()) / 2) >= length * 10) {
                            motorA.stop();
                            motorC.stop();
                            motorA.resetTachoCount();
                            motorC.resetTachoCount();
                            colorLightSensor.setType(ColorLightSensor.TYPE_COLORFULL);
                            reply[0] = colorLightSensor.readValue();
                            for (int n = 1; n < 8; n++) {
                                reply[n] = 0;
                            }
                            break;
                        }
                        lightValue = colorLightSensor.readValue();
                        turn = (int) pidController.getOutput((lightValue - offset));
                        turn = turn / 100;
                        powerA = Tp - turn;
                        powerC = Tp + turn;
                        if (powerA > 0) {
                            motorA.setPower(powerA);
                            motorA.forward();
                        } else {
                            powerA = powerA * (-1);
                            motorA.setPower(powerA);
                            motorA.backward();
                        }
                        if (powerC > 0) {
                            motorC.setPower(powerC);
                            motorC.forward();
                        } else {
                            powerC = powerC * (-1);
                            motorC.setPower(powerC);
                            motorC.backward();
                        }
                        colorLightSensor.setType(ColorLightSensor.TYPE_COLORRED);
                    }
                    break;

                case COMMAND_READ_COLOR:
                    colorLightSensor.setType(ColorLightSensor.TYPE_COLORFULL);
                    reply[0] = colorLightSensor.readValue();
                    for (int n = 1; n < 8; n++) {
                        reply[n] = 0;
                    }
                    colorLightSensor.setType(ColorLightSensor.TYPE_COLORRED);
                    break;

                case COMMAND_TURN:
                    compassPilot.setMoveSpeed(200);
                    compassPilot.rotate(command[1]);
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        System.exit(0);
                    }
                    break;

                // Master warns of a bluetooth disconnect; set while loop so it stops
                case COMMAND_DISCONNECT:
                    keepItRunning = false;
                    for (int k = 0; k < 8; k++) {
                        reply[k] = 255;
                    }
                    break;

            }

            // Slave send back reply
            LCD.clear();
            for (int k = 0; k < 8; k++) {
                LCD.drawInt(reply[k], 0, k);
            }
            LCD.refresh();

            for (int k = 0; k < 8; k++) {
                dos.writeInt(reply[k]);
                dos.flush();
            }

        }
        dis.close();
        dos.close();
        Thread.sleep(100); // wait for data to drain
        LCD.clear();
        LCD.drawString(closing, 0, 0);
        LCD.refresh();
        // Close the bluetooth connection from the Slave's point-of-view
        btc.close();
        LCD.clear();
    }

    public static int getMM(int tacho) {
        int rounds = tacho / 360;
        double extra = tacho % 360;
        double cm = rounds * 56 * Math.PI;
        cm += 56.0 * Math.PI * (extra / 360.0);
        return (int) cm;

    }
}

