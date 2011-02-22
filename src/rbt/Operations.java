package rbt;

import lejos.nxt.*;
import lejos.robotics.navigation.TachoPilot;

public class Operations {

    private Motor motorA = Motor.A;
    private Motor motorC = Motor.C;
    private TachoPilot tachoPilot;
    private ColorLightSensor colorLightSensor;
    int offset = 33;
    int tp = 50;
    int kp = 160;


    public Operations() {
        tachoPilot = new TachoPilot(56.0f, 110.0f, motorA, motorC);
        colorLightSensor = new ColorLightSensor(SensorPort.S2, ColorLightSensor.TYPE_COLORFULL);
        colorLightSensor.setFloodlight(true);
    }

    public int[] travel(int length) {
        int[] reply = new int[8];
        int lightValue;
        int turn;
        int powerA;
        int powerC;
        motorA.resetTachoCount();
        motorC.resetTachoCount();
        tachoPilot.reset();
        while (true) {
            if (getMM(motorA.getTachoCount()) >= length * 0.8) {
                motorA.stop();
                motorC.stop();
                colorLightSensor.setFloodlight(false);
                colorLightSensor.setType(ColorLightSensor.TYPE_COLORFULL);
                tachoPilot.travel((float) (length * 0.3), true);
                while (tachoPilot.isMoving()) {
                    if (colorLightSensor.readValue() != 1 && colorLightSensor.readValue() != 0) {
                        motorA.stop();
                        motorC.stop();
                        reply[0] = colorLightSensor.readValue();
                        motorA.resetTachoCount();
                        motorC.resetTachoCount();
                        colorLightSensor.setFloodlight(true);
                        LCD.clear();
                        LCD.drawInt(length, 0, 0);
                        LCD.drawInt((int) getMM((motorA.getTachoCount() + motorC.getTachoCount()) / 2), 0, 1);
                        LCD.refresh();
                        for (int n = 1; n < 8; n++) {
                            reply[n] = 0;
                        }
                        return reply;
                    }
                }
                LCD.clear();
                LCD.drawInt(length, 0, 0);
                LCD.drawInt((int) getMM((motorA.getTachoCount() + motorC.getTachoCount()) / 2), 0, 1);
                LCD.refresh();
                for (int n = 1; n < 8; n++) {
                    reply[n] = 0;
                }
                break;
            }
            lightValue = colorLightSensor.readValue();
            LCD.clear();
            LCD.drawInt(lightValue, 0, 0);
            turn = (lightValue - offset) * kp;
            turn = turn / 100;
            powerA = tp - turn;
            powerC = tp + turn;
            LCD.drawInt(powerA, 0, 1);
            LCD.drawInt(powerC, 0, 2);
            LCD.refresh();
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

        }
        return reply;

    }

    public void turn(int degrees) {
        tachoPilot.setMoveSpeed(200);
        tachoPilot.reset();
        tachoPilot.rotate(degrees, true);
        while (tachoPilot.isMoving()) {
            if (Math.abs(tachoPilot.getAngle()) > Math.abs(degrees * 0.8) && colorLightSensor.readValue() < 25) {
                tachoPilot.stop();
                LCD.clear();
                LCD.drawInt(colorLightSensor.readValue(), 0, 0);
                LCD.drawInt((int) tachoPilot.getAngle(), 0, 1);
                LCD.refresh();
            }
        }

        return;
    }

    public int[] readValue() {
        int[] reply = new int[8];
        reply[0] = colorLightSensor.readValue();
        for (int n = 1; n < 8; n++) {
            reply[n] = 0;
        }
        return reply;
    }

    public int[] read_Color() {
        int[] reply = new int[8];
        colorLightSensor.setFloodlight(false);
        colorLightSensor.setType(ColorLightSensor.TYPE_COLORFULL);
        reply[0] = colorLightSensor.readValue();
        for (int n = 1; n < 8; n++) {
            reply[n] = 0;
        }
        colorLightSensor.setFloodlight(true);
        return reply;
    }

    public int[] battery_Voltage() {
        int[] reply = new int[8];
        reply[0] = Battery.getVoltageMilliVolt();
        for (int n = 1; n < 8; n++) {
            reply[n] = 0;
        }
        return reply;
    }


    private double getMM(int tacho) {
        int rounds = tacho / 360;
        double extra = tacho % 360;
        double cm = rounds * 56 * Math.PI;
        cm += 56.0 * Math.PI * (extra / 360.0);
        return cm;

    }

    public int[] sweep() {
        int[] reply = new int[8];
        tachoPilot.reset();
        tachoPilot.rotate(15);
        int i = 0;
        do {
            tachoPilot.rotate(-360, true);
            while (tachoPilot.isMoving()) {
                if (colorLightSensor.readValue() < 25) {
                    tachoPilot.stop();
                    LCD.clear();
                    LCD.drawInt(colorLightSensor.readValue(), 0, 0);
                    LCD.drawInt((int) tachoPilot.getAngle(), 0, 1);
                    LCD.refresh();
                    reply[i] = (int) tachoPilot.getAngle();
                    i++;
                    tachoPilot.rotate(-15);
                }
            }

        } while (tachoPilot.getAngle() > -360);


        return reply;
    }

    public boolean disconnect(int[] reply) {
        boolean keepItRunning;
        keepItRunning = false;
        for (int k = 0; k < 8; k++) {
            reply[k] = 255;
        }
        return keepItRunning;
    }
}
