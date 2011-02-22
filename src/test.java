import lejos.nxt.ColorLightSensor;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;

public class test {

    public static void main(String[] args) {

        Motor mA = Motor.A;
        Motor mC = Motor.C;
        ColorLightSensor cs = new ColorLightSensor(SensorPort.S2, ColorLightSensor.TYPE_COLORFULL);
        cs.setFloodlight(true);
        final int kp = 150;
        final int offset = 33;
        final int Tp =50;

        int error = 0;
        int turn = 0;
        int valueBeingRead = 0;
        int powerA = 0;
        int powerC = 0;
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
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
        }
     }
}