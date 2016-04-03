package scripts.utils.navigation;

import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.interfaces.Positionable;
import org.tribot.api2007.Camera;
import org.tribot.api2007.Objects;
import org.tribot.api2007.Player;
import org.tribot.api2007.Walking;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSObjectDefinition;
import org.tribot.api2007.types.RSTile;
import scripts.utils.CommonUtils;
import scripts.utils.KMUtils;
import scripts.utils.camera.CameraUtils;

public class LumbridgeStairsNavigator {

    private final CameraUtils cam;
    private final CommonUtils com;
    private final KMUtils km;

    private final static RSTile STAIRS_TILE_0 = new RSTile(3204, 3207, 0);
    private final static RSTile STAIRS_TILE_1 = new RSTile(3204, 3207, 1);
    private final static RSTile STAIRS_TILE_2 = new RSTile(3205, 3208, 2);

    private final static RSTile GROUND_FLOOR_TILE = new RSTile(3206, 3209, 0);
    private final static RSTile FIRST_FLOOR_TILE = new RSTile(3205, 3209, 1);
    private final static RSTile SECOND_FLOOR_TILE = new RSTile(3205, 3209, 2);

    public LumbridgeStairsNavigator(KMUtils kmUtils) {
        this.cam = kmUtils.getCameraUtils();
        this.com = kmUtils.getCommonUtils();
        this.km = kmUtils;
    }

    public void walkToStairs() {
        if (!Player.isMoving()) {
            if (Walking.walkTo(getStairTile())) {
                com.sleep(500, 750);
            }
            cam.rotateCameraToTileAsync(getStairTile());
            cam.pitchCameraAsync(General.random(23, 110));
            com.sleep(315, 612);
        }
    }

    public void climbStairs(NavigationCondition navCondition) {
        String uptext = "";
        RSTile stairTile;
        int plane = Player.getPosition().getPlane();

        switch (plane) {
            case 0:
                uptext = "Climb-up";
                cam.pitchCameraAsync(General.random(22, 45));
                stairTile = STAIRS_TILE_0;
                cam.rotateCameraToTileAsync(stairTile);
                break;
            case 1:
                stairTile = STAIRS_TILE_1;
                if (navCondition.navigateUp()) {
                    uptext = "Climb-up";
                } else {
                    uptext = "Climb-down";
                }
                break;
            case 2:
                stairTile = STAIRS_TILE_2;
                if (Camera.getCameraAngle() < 65) {
                    cam.pitchCameraAsync(General.random(65, 110));
                    com.sleep(212, 312);
                }
                uptext = "Climb-down";
                break;
            default:
                stairTile = STAIRS_TILE_0;
                com.sleep(200, 300);
                break;
        }

        RSObject stairs = getStair(stairTile);
        if (stairs != null) {
            if (!stairs.isOnScreen()) {
                cam.rotateCameraToTileAsync(stairs);
                com.sleep(100, 200);
            }
            if (DynamicClicking.clickRSObject(stairs, uptext + " Staircase")) {
                if (plane == 0) {
                    com.waitUntilIdle(125, 175);
                } else {
                    com.sleep(1251, 1567);
                }
            }
        }
    }

    private RSObject getStair(Positionable stairTile) {
        RSObject[] stairs = Objects.getAt(stairTile);
        if (stairs != null && stairs.length > 0) {
            for (RSObject stair : stairs) {
                if (stair != null) {
                    RSObjectDefinition def = stair.getDefinition();
                    if (def != null) {
                        String[] actions = def.getActions();
                        if (actions != null) {
                            for (String action : actions) {
                                if (action != null) {
                                    if (action.contains("Climb")) {
                                        return stair;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public RSTile getStairTile() {
        int plane = Player.getPosition().getPlane();
        if (plane == 0) {
            return GROUND_FLOOR_TILE;
        } else if (plane == 1) {
            return FIRST_FLOOR_TILE;
        } else if (plane == 2) {
            return SECOND_FLOOR_TILE;
        } else {
            com.println("Player.getPosition().getPlane() returned invalid value! Trying luck with default values.");
            return GROUND_FLOOR_TILE;
        }
    }

}
