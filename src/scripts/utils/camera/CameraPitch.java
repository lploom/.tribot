package scripts.utils.camera;

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
                    wait();
                } catch (InterruptedException e) {
                    break;
                }
                utils.setCameraPitching(true);
                Camera.setCameraAngle(angle);
                utils.setCameraPitching(false);
            }
        }
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }
}