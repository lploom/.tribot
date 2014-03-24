package scripts.utils;

import org.tribot.api2007.Camera;

public class CameraPitch extends Thread {

   private int angle;
   private CameraUtils utils;

   public CameraPitch(CameraUtils tere) {
      utils = tere;
   }

   @Override
   public void run() {
      while (true) {
         synchronized (this) {
            try {
               utils.setCameraPitching(false);
               wait();
            } catch (InterruptedException e) {
               break;
            }
            utils.setCameraPitching(true);
            Camera.setCameraAngle(angle);
         }
      }
   }

   public void setAngle(int angle) {
      this.angle = angle;
   }
}