package scripts.utils;

import org.tribot.api.General;
import org.tribot.api2007.Camera;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSTile;
import org.tribot.api.interfaces.Positionable;

public class CameraRotation extends Thread {

   private int degrees;
   private CameraUtils utils;
   private Positionable tileRotation;
   RotationState state;

   public CameraRotation(CameraUtils cameraUtils) {
      utils = cameraUtils;
   }

    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    break;
                }
                utils.setCameraRotating(true);
                switch (state) {
                    case TILE:
                        Camera.turnToTile(tileRotation);
                        break;
                    case DEGREES:
                        int temp = 0;
                        if (!utils.isCameraMovement())
                            Camera.setCameraRotation(degrees);

                        while (utils.isCameraMovement()) {
                            if (temp > 50)
                                break;
                            temp++;
                            rotateCameraBasedOnMovement();
                        }

                        break;
                }

                utils.setCameraRotating(false);

            }
        }
    }

   public void rotateCameraBasedOnMovement() {
      int deg = getMovementDirection();
      switch (deg) {
         case 0:
            utils.pitchCameraAsync(General.random(23, 110));
            Camera.setCameraRotation(General.random(0, 20));
            break;
         case 7:
            utils.pitchCameraAsync(General.random(23, 110));
            Camera.setCameraRotation(General.random(21, 79));
            break;
         case 6:
            utils.pitchCameraAsync(General.random(23, 110));
            Camera.setCameraRotation(General.random(80, 100));
            break;
         case 5:
            utils.pitchCameraAsync(General.random(23, 110));
            Camera.setCameraRotation(General.random(101, 150));
            break;
         case 4:
            utils.pitchCameraAsync(33);
            Camera.setCameraRotation(General.random(151, 190));
            break;
         case 3:
            utils.pitchCameraAsync(General.random(23, 110));
            Camera.setCameraRotation(General.random(191, 240));
            break;
         case 2:
            utils.pitchCameraAsync(General.random(23, 110));
            Camera.setCameraRotation(General.random(241, 292));
            break;
         case 1:
            utils.pitchCameraAsync(General.random(23, 110));
            Camera.setCameraRotation(General.random(293, 359));
            break;
         case 8:
            break;
         default:
            break;
      }
   }

   private int getMovementDirection() {
      RSTile p1 = Player.getPosition();
      try {
         Thread.sleep(General.random(500, 2000));
      } catch (InterruptedException e) {
      }
      RSTile p2 = Player.getPosition();

      if (p1.equals(p2)) {
         return 8;
      }

      int p1x = p1.getX();
      int p1y = p1.getY();
      int p2x = p2.getX();
      int p2y = p2.getY();

      if (p2x == p1x) {
         if (p2y > p1y)
            return 0;
         else
            return 4;
      }
      if (p2y == p1y) {
         if (p2x > p1x)
            return 2;
         else
            return 6;
      }
      if (p2y > p1y) {
         if (p2x > p1x)
            return 1;
         else
            return 7;
      }
      else {
         if (p2x > p1x)
            return 3;
         else
            return 5;
      }
   }

   public void setRotation(int deg) {
      this.degrees = deg;
      state = RotationState.DEGREES;
   }

   public void setTileRotation(Positionable tile) {
      this.tileRotation = tile;
      state = RotationState.TILE;
   }

   public static enum RotationState {
      DEGREES, TILE
   }
}