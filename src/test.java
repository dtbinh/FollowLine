import lejos.nxt.*;
import lejos.robotics.Colors;

public class test {

    public static void main(String[] args) {

        Motor mA = Motor.A;
        Motor mC = Motor.C;
        ColorLightSensor cs = new ColorLightSensor(SensorPort.S2, ColorLightSensor.TYPE_COLORFULL);
        //cs.setFloodlight(Colors.Color.WHITE);

        cs.setFloodlight(true);
        final int kp = 150;
        final int ki = 0;
        final int kd = 0;
        final int offset = 33;
        final int Tp = 50;
        int integral = 0;
        int lastError = 0;
        int derivative = 0;
        int error = 0;
        int turn = 0;
        int valueBeingRead = 0;
        int powerA = 0;
        int powerC = 0;
        boolean halt = false;
        int distance = 0;
        int tacho = 0;
        while (true) {
            tacho = mA.getTachoCount();
            tacho += mC.getTachoCount();
            tacho = tacho / 2;
            distance = getMm(tacho);
//            if (distance > 470 || halt) {
//                mA.stop();
//                mC.stop();
//                LCD.drawInt(distance, 0, 0);
//                LCD.drawInt(tacho, 0, 1);
//                halt = true;
//                try {
//                    Thread.sleep(3000);
//                } catch (InterruptedException e) {
//
//                }
//            } else {
                valueBeingRead = cs.readValue();
                LCD.drawInt(valueBeingRead, 0, 0);
                error = valueBeingRead - offset;
                integral = (2 / 3) * integral + error;
                derivative = error - lastError;

                turn = ((kp * error) + (ki * integral) + (kd * derivative));
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

                lastError = error;
            }
//        }
    }

    public static int getMm(int tacho) {
        int rounds = tacho / 360;
        double extra = tacho % 360;
        double cm = rounds * 56 * Math.PI;
        cm += 56.0 * Math.PI * (double) (extra / (double) 360);
        return (int) cm;

    }
}
