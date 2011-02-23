import lejos.nxt.*;
import lejos.nxt.comm.*;

import java.io.*;

public class test {

    public static void main(String[] args) throws IOException {

        Motor mA = Motor.A;
        Motor mC = Motor.C;
        ColorLightSensor cs = new ColorLightSensor(SensorPort.S2, ColorLightSensor.TYPE_COLORFULL);
        cs.setFloodlight(true);
        final int kp = 110;
        final int offset = 33;
        final int Tp = 35;
        final int COMMAND_TRAVEL = 1;
        int[] command = new int[3];
        int[] reply = new int[8];
        boolean keepItRunning = true;
        String connected = "Connected";
        String waiting = "Waiting...";
        String closing = "Closing...";
        DataInputStream dis;
        DataOutputStream dos;
        BTConnection btc;

        int error = 0;
        int turn = 0;
        int valueBeingRead = 0;
        int powerA = 0;
        int powerC = 0;

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

        while (keepItRunning) {
            // Fetch the Master's command
            for (int k = 0; k < 3; k++) {
                command[k] = dis.readInt();
            }


            // Respond to the Master's command which is stored in command[0]
            LCD.clear();

            while (true) {
                valueBeingRead = cs.readValue();
                LCD.drawInt(valueBeingRead, 0, 0);
                error = valueBeingRead - offset;


                turn = (kp * error);
                turn = turn / 100;
                powerA = Tp - turn;
                powerC = Tp + turn;
                LCD.drawInt(powerA, 0, 1);
                LCD.drawInt(powerC, 0, 2);

                if (powerA > 0) {
                    mA.setPower(powerA);
                    mA.forward();
                } else {
                    powerA = powerA * (-1);
                    mA.setPower(powerA);
                    mA.backward();
                }
                if (powerC > 0) {
                    mC.setPower(powerC);
                    mC.forward();
                } else {
                    powerC = powerC * (-1);
                    mC.setPower(powerC);
                    mC.backward();
                }
                if(Button.ENTER.isPressed()) {
                    break;
                }

            }
        }
        for (int k = 0; k < 8; k++) {
                dos.writeInt(reply[k]);
                dos.flush();
            }
        dis.close();
        dos.close();
        LCD.clear();
        LCD.drawString(closing, 0, 0);
        LCD.refresh();
        // Close the bluetooth connection from the Slave's point-of-view
        btc.close();
        LCD.clear();
    }
}