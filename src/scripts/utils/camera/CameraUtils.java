package scripts.utils.camera;

import org.tribot.api.General;
import org.tribot.api.interfaces.Positionable;
import org.tribot.api2007.Camera;

public class CameraUtils {

    private volatile boolean isCameraRotating = false;
    private volatile boolean isCameraPitching = false;
    private volatile boolean cameraMovement = false;
    private CameraRotation rotationThread;
    private CameraPitch pitchThread;

    public CameraUtils() {
        rotationThread = new CameraRotation(this);
        pitchThread = new CameraPitch(this);
        rotationThread.start();
        pitchThread.start();
    }

    public CameraRotation getRotationThread() {
        return rotationThread;
    }

    public CameraPitch getPitchThread() {
        return pitchThread;
    }

    public boolean isCameraPitching() {
        return isCameraPitching;
    }

    public boolean isCameraRotating() {
        return isCameraRotating;
    }

    public boolean isCameraMovement() {
        return cameraMovement;
    }

    public void setCameraPitching(boolean isCameraPitching) {
        this.isCameraPitching = isCameraPitching;
    }

    public void setCameraRotating(boolean isCameraRotating) {
        this.isCameraRotating = isCameraRotating;
    }

    public void setCameraMovement(boolean cameraMovement) {
        this.cameraMovement = cameraMovement;
    }

    public void rotateCameraAsync(int deg) {
        if (!isCameraRotating) {
            synchronized (rotationThread) {
                rotationThread.setRotation(deg);
                rotationThread.notify();
            }
        }
    }

    public void rotateCameraToTileAsync(Positionable tile) {
        if (!isCameraRotating) {
            synchronized (rotationThread) {
                rotationThread.setTileRotation(tile);
                rotationThread.notify();
            }
        }
    }

    public void pitchCameraAsync(int angle) {
        if (!isCameraPitching) {
            synchronized (pitchThread) {
                pitchThread.setAngle(angle);
                pitchThread.notify();
            }
        }
    }

    public enum CARDINAL {
        NORTH,
        WEST,
        EAST,
        SOUTH
    }

    public void turnCameraTo(CARDINAL cardinal) {
        if (!isCameraRotating) {
            switch (cardinal) {
                case EAST:
                    if (Camera.getCameraRotation() < 245 || Camera.getCameraRotation() > 290) {
                        rotateCameraAsync(General.random(250, 285));
                    }
                    break;
                case NORTH:
                    if (Camera.getCameraRotation() > 25 && Camera.getCameraRotation() < 335) {
                        rotateCameraAsync(General.random(0, 15));
                    }
                    break;
                case SOUTH:
                    if (Camera.getCameraRotation() < 165 || Camera.getCameraRotation() > 195) {
                        rotateCameraAsync(General.random(170, 190));
                    }
                    break;
                case WEST:
                    if (Camera.getCameraRotation() < 75 || Camera.getCameraRotation() > 105) {
                        rotateCameraAsync(General.random(80, 100));
                    }
                    break;
            }
        }

    }

    public void onEnd() {
        setCameraPitching(false);
        setCameraRotating(false);
        setCameraMovement(false);

        while (getRotationThread().isAlive() || getPitchThread().isAlive()) {
            getRotationThread().interrupt();
            getPitchThread().interrupt();
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
            }
        }
    }

}
