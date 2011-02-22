import lejos.nxt.*;
import lejos.nxt.addon.CompassSensor;
import lejos.robotics.navigation.*;

//import lejos.navigation;

public class LineFollower {

	//final static LightSensor light = new LightSensor(SensorPort.S2);
    	final static ColorLightSensor light = new ColorLightSensor(SensorPort.S2, ColorLightSensor.TYPE_COLORFULL);

	final static UltrasonicSensor sonic = new UltrasonicSensor(SensorPort.S3);
    	final static CompassSensor _cs = new CompassSensor(SensorPort.S3);

	public static void main (String[] aArg)
	throws Exception
	{

        final TachoPilot pilot = new TachoPilot(5.5f,11.2f,Motor.A, Motor.C);
        //final CompassPilot pilot = new CompassPilot(_cs, 5.5f,11.2f,Motor.A, Motor.C);
        light.setFloodlight(true);
		final Motor led = Motor.B;

		final long ITERATION_TIME = 100;   // miliseconds
		final int SPEED = 500;

        boolean lastSweepRight = true;
        int black;
        int distance;
		int sweep;


        black=calibraBlack();
        //distance=calibraDistance();
		laCuca();

        LCD.clear();
        LCD.refresh();
		pilot.setSpeed(SPEED);

        /* itera hasta pulsar ESCAPE + ENTER*/
		while(true)
		{


			led.stop();

            /* si está en negro, avanza*/
			if (light.readValue() <= black)
			{
				pilot.forward();
				continue;
			}

			/* si está en blanco, hará barridos hasta encontrar negro, luego parará*/
			if (light.readValue() > black)
			{
                boolean blackAgain = false;
		        sweep = 5;

    			if  (!lastSweepRight) sweep*=-1;

    			while (!blackAgain) {
    				pilot.rotate(sweep,true);
    				while (pilot.isMoving()){
    					if (light.readValue() <= black)
    					{
                       	    blackAgain=true;
                           	lastSweepRight = (sweep > 0);
    						break;
                        }
    				}
    				sweep *= -2;
    			}
    			pilot.forward();
            }/* blanco */

		} /* bucle principal */

	}/* main */



	public static int calibraBlack()
    {
        int black=0;

        LCD.clear();
        LCD.drawString("#Calibra black#", 0, 0);
        LCD.drawString("Current:", 0, 3);
        LCD.drawString("Stored:", 0, 4);

		while(!Button.ESCAPE.isPressed())
        {
            LCD.drawInt(light.readValue(), 10, 3);
            LCD.refresh();
            if (Button.ENTER.isPressed())
            {
                Sound.playTone(1800, 50);
                if (black < light.readValue())
                {
                    black = light.readValue();
                    LCD.drawInt(light.readValue(), 9, 4);
                }
            }/* calibra pulsando ENTER*/
        }/* hasta pulsar ESCAPE*/

	    return black;

    }/* calibraBlack */

    public static int calibraDistance()
    {
		LCD.clear();
        LCD.drawString("#Calibra dist.#", 0, 0);
        LCD.drawString("Current:", 0, 3);

		while(!Button.ENTER.isPressed())
        {
            LCD.drawInt(-8, 11, 3);
            LCD.drawInt(sonic.getDistance(), 9, 3);
            LCD.refresh();
        }
		Sound.playTone(1800, 50);
        return sonic.getDistance();
    }/* calibraDistance */

	public static void laCuca() throws Exception
	{
		short [] note = {523,10,523,10,523,10,698,20,880,10,0,10,
			 			 523,10,523,10,523,10,698,20,880,10,0,10,
						 698,10,698,10,659,10,659,10,587,10,587,10,523,20};

   		for(int i=0;i<note.length; i+=2) {
        	final short w = note[i+1];
        	final int n = note[i];
        	if (n != 0) Sound.playTone(n*4, w*3);
        	try { Thread.sleep(w*8); } catch (InterruptedException e) {}
      	}
	} /* laCuca */

} /* LineFollower */

