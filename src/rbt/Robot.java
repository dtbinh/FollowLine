package rbt;

import lejos.nxt.LCD;
import lejos.nxt.Sound;
import lejos.nxt.comm.*;

import java.io.*;

public class Robot {


    public static void main(String[] args) throws Exception {
        Operations operations = new Operations();

        final int COMMAND_BATTERY_VOLTAGE = 0;
        final int COMMAND_TRAVEL = 1;
        final int COMMAND_TURN = 2;
        final int COMMAND_READ_COLOR = 3;
        final int COMMAND_DISCONNECT = 4;
        final int SWEEP = 5;
        final int READ_VALUE = 6;
        int[] command = new int[3];
        int[] reply = new int[8];
        boolean keepItRunning = true;
        String connected = "Connected";
        String waiting = "Waiting...";
        String closing = "Closing...";
        DataInputStream dis;
        DataOutputStream dos;
        BTConnection btc;

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

            switch (command[0]) {
                case COMMAND_BATTERY_VOLTAGE:
                    reply = operations.battery_Voltage();
                    break;

                case COMMAND_TRAVEL:
                    reply = operations.travel(command[1]);
                    break;

                case COMMAND_READ_COLOR:
                    reply = operations.read_Color();
                    break;

                case COMMAND_TURN:
                    operations.turn(command[1]);
                    break;

                case SWEEP:
                    reply = operations.sweep();
                    break;

                case COMMAND_DISCONNECT:
                    keepItRunning = operations.disconnect(reply);
                    break;
                case READ_VALUE:
                    reply = operations.readValue();
            }

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
}

