/**
 * Created by IntelliJ IDEA.
 * User: eirik
 * Date: 2/21/11
 * Time: 1:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class PIDController
{
   /* Double gives a more accurate calculation in PID
    * I initially used Integers but after reading the above articles I learned
    * that the difference from 1.0 to 2.0 is quite a big difference in this algorithm
    * Long is used because of Java's system time is measured in Long and is needed
    * for Delta time below.
    */

   private double Kp, Ki, Kd;
   private long count, lastCount;
    private double proportional, integral, derivative;
    private double error, lastError;
    private long integratedError;
    private double output;

    private boolean currentlyRunning;
    private boolean data;


   public PIDController(double Kp, double Ki, double Kd)
   {
      this.Kp = Kp;
      this.Ki = Ki;
      this.Kd = Kd;
      reset();
   }

   public double getOutput(double input)
   {
      /* A check to see if PID is actually running
       * First run will calculate the proportional,
       * Second run, now has data and will add and calculate Integral and Derivative
       */
      if(currentlyRunning)
      {
         count = System.currentTimeMillis();
         error = input;
         proportional = Kp * error;
         long dt = count - lastCount;

         if(!data)
         {
            data = true;
            output = proportional;
         }
         else if(dt != 0)
         {
            integratedError += error * dt;
            integral = Ki * integratedError;
            derivative = (Kd * (error - lastError) / dt);
            output = (long) (proportional + integral + derivative);
         }
         lastCount = count;
         lastError = error;
      }
      return output;
   }


   /*
    * This allows me to start and stop the algorithm.
    * This needs to be done when an object is detected
    * The algorithm is stopped, object is detecte, direction is chosen
    * and alogrithm starts again
    */
   public void start()
   {
      if(!currentlyRunning)
      {
         reset();
         currentlyRunning = true;
         data = false;
      }
   }

   public void stop()
   {
      currentlyRunning  = false;
   }

   public void reset()
   {
      integratedError = 0;
      proportional = 0;
      integral = 0;
      derivative = 0;
   }

   //Getters
   public double getProportional()
   {
      return proportional;
   }

   public double getIntegral()
   {
      return integral;
   }

   public double getDerivative()
   {
      return derivative;
   }

   public double getOutput()
   {
      return output;
   }
}
