package rbt;

import lejos.nxt.*;
import lejos.robotics.navigation.TachoPilot;

public class Operations {

    private Motor motorA = Motor.A;
    private Motor motorC = Motor.C;
    private TachoPilot tachoPilot;
    private ColorLightSensor colorLightSensor;
    int offset = 33;     //33
    int tp = 50;         //50
    int kp = 170;        //150
    int kd = 400;        //400

    final int BLACK_VALUE = 25;


    public Operations() {
        tachoPilot = new TachoPilot(56.0f, 110.0f, motorA, motorC);
        colorLightSensor = new ColorLightSensor(SensorPort.S2, ColorLightSensor.TYPE_COLORRED);
    }

    public int[] travel(int length, int findColor) {
        int[] reply = new int[8];
        double le = 0.8;
        if (findColor == 0) {
            le = 1;
            reply[0] = 0;
        }
        PIDmove(length, le);
        if (findColor == 1) {
            reply[0] = moveFindColor(length, reply);
        }
        for (int n = 1; n < 8; n++) {
            reply[n] = 0;
        }
        return reply;

    }

    private void resetTacho() {
        motorA.resetTachoCount();
        motorC.resetTachoCount();
        tachoPilot.reset();
    }

    private int moveFindColor(int length, int[] reply) {
        int readValue = 0;
        colorLightSensor.setType(ColorLightSensor.TYPE_COLORFULL);
        tachoPilot.travel((float) (length * 0.3), true);
        while (tachoPilot.isMoving()) {
            readValue = colorLightSensor.readValue();
            if (readValue == 4 || readValue == 2 || readValue == 3 || readValue == 5) {
                tachoPilot.stop();
                colorLightSensor.setType(ColorLightSensor.TYPE_COLORRED);
                break;
            }
        }
        colorLightSensor.setType(ColorLightSensor.TYPE_COLORRED);
        for (int n = 1; n < 8; n++) {
            reply[n] = 0;
        }
        return readValue;
    }

    private void PIDmove(int length, double le) {
        int lightValue;
        int turn;
        int powerA;
        int error;
        int powerC;
        int lastError = 0;
        int derivative;
        resetTacho();
        while (getMM(motorA.getTachoCount()) < length * le) {
            lightValue = colorLightSensor.readValue();

            error = lightValue - offset;
            derivative = error - lastError;
            turn = (kp * error) + (kd * derivative);
            turn = turn / 100;
            powerA = tp - turn;
            powerC = tp + turn;

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
            lastError = error;
        }
        motorA.stop();
        motorC.stop();
    }

    public void turn(int degrees) {
        tachoPilot.setMoveSpeed(200);
        resetTacho();
        tachoPilot.rotate(degrees * 1.1f, true);
        while (tachoPilot.isMoving()) {
            if (Math.abs(tachoPilot.getAngle()) > Math.abs(degrees * 0.8) && colorLightSensor.readValue() < BLACK_VALUE + 10) {
                tachoPilot.stop();
                Sound.beepSequence();
            }
        }
        calibrate();
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
        colorLightSensor.setType(ColorLightSensor.TYPE_COLORFULL);
        reply[0] = colorLightSensor.readValue();
        for (int n = 1; n < 8; n++) {
            reply[n] = 0;
        }
        colorLightSensor.setType(ColorLightSensor.TYPE_COLORRED);
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
        resetTacho();
        tachoPilot.rotate(15);
        int i = 0;
        do {
            tachoPilot.rotate(-360, true);
            while (tachoPilot.isMoving()) {
                if (colorLightSensor.readValue() < BLACK_VALUE + 10) {
                    tachoPilot.stop();
                    reply[i] = (int) tachoPilot.getAngle();
                    i++;
                    Sound.beep();
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

    private void calibrate() {
        int value = colorLightSensor.readValue();
        float angle = 2;
        while (value > BLACK_VALUE) {
            tachoPilot.rotate(angle);
            angle = -angle;
            if (angle < 0) {
                angle -= 2;
            } else {
                angle += 2;
            }

            value = colorLightSensor.readValue();
        }
    }
}
