package scripts.utils;

import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Mouse;
import org.tribot.api2007.*;
import org.tribot.api2007.Equipment.SLOTS;
import org.tribot.api2007.GameTab.TABS;
import org.tribot.api2007.Skills.SKILLS;
import org.tribot.api2007.types.*;
import scripts.utils.camera.CameraUtils;

import java.awt.*;

public class KMUtils {

    private final CameraUtils cameraUtils;
    private final CommonUtils commonUtils;

    public KMUtils(CameraUtils cameraUtils, CommonUtils commonUtils) {
        this.commonUtils = commonUtils;
        this.cameraUtils = cameraUtils;
    }

    public boolean isCharacterHealthy(int offset) {
        return Skills.getCurrentLevel(SKILLS.HITPOINTS) + offset >= Skills.getActualLevel(SKILLS.HITPOINTS);
    }

    public boolean forceEat(int foodID) {
        if (!GameTab.getOpen().equals(TABS.INVENTORY)) {
            GameTab.open(TABS.INVENTORY);
            commonUtils.sleep(200, 300);
        }
        if (Inventory.getCount(foodID) > 0) {
            while (!isCharacterHealthy(3)) {
                RSItem[] foods = Inventory.find(foodID);
                if (foods.length == 0)
                    break;
                if (foods != null && foods.length > 0) {
                    int k = 0;
                    while (Inventory.getCount(foodID) == foods.length) {
                        if (k > 5)
                            break;
                        foods[0].click("Eat");
                        commonUtils.sleep(100, 400);
                        k++;
                    }
                }
            }
        }
        return false;
    }

    public String getLastMessage() {
        if (Interfaces.get(137, 2) != null) {
            for (int j = Interfaces.get(137, 2).getChildren().length - 1; j > 0; j--) {
                if (Interfaces.get(137, 2).getChildren()[j].getText() != "") {
                    return Interfaces.get(137, 2).getChildren()[j].getText();
                }
            }
        }
        return null;
    }

    public RSItem getInventoryItemById(int id) {
        RSItem[] items = Inventory.getAll();
        for (RSItem item : items) {
            if (item.getID() == id) {
                return item;
            }
        }
        return null;
    }

    public RSObject getNearestObjectByMenuAction(int distance, String menuAction) {
        RSObject[] objects = Objects.sortByDistance(Player.getPosition(), Objects.getAll(distance));
        for (int i = 0; i < objects.length; i++) {
            if (objects[i].getDefinition() != null) {
                String[] actions = objects[i].getDefinition().getActions();
                for (int j = 0; j < actions.length; j++) {
                    if (actions[j].equalsIgnoreCase(menuAction)) {
                        return objects[i];
                    }
                }
            }
        }
        return null;
    }

    public RSObject getNearestObject(int id, int dist) {
        RSObject[] objects = Objects.findNearest(dist, id);
        if (objects != null && objects.length > 0) {
            for (RSObject obj : objects) {
                if (obj != null) {
                    return obj;
                }
            }
        }
        return null;
    }

    public RSObject getNearestObject(int[] ids, int dist) {
        RSObject[] objects = Objects.find(dist, ids);
        if (objects != null && objects.length > 0) {
            return objects[0];
        }
        return null;
    }

    public boolean moveMouseToModel(RSModel model) {
        if (model != null) {
            Point[] ps = model.getAllVisiblePoints();
            if (ps.length > 0) {
                Point p = average(ps);
                Mouse.move(p);
                return true;
            }
        }
        return false;
    }

    // Snippet by JJ
    private void waitPath(RSTile tile) {
        long t = System.currentTimeMillis();
        while (Timing.timeFromMark(t) < General.random(500, 1000)) {
            if (Player.getPosition().distanceToDouble(tile) <= General.random(1, 3)) {
                break;
            }
            commonUtils.sleep(100, 200);
            if (Player.isMoving()) {
                t = System.currentTimeMillis();
            }
        }
    }

    // Snippet by JJ
    public boolean walkPath(RSTile[] tiles, boolean reverse) {
        if (reverse) {
            RSTile[] reversed = new RSTile[tiles.length];
            for (int i = tiles.length - 1; i >= 0; i--) {
                reversed[tiles.length - 1 - i] = tiles[i];
            }
            tiles = reversed;
        }

        if (Walking.walkPath(tiles)) {
            waitPath(tiles[tiles.length - 1]);
        }

        return false;
    }

    public boolean isAutoRetaliateIsEnabled() {
        return Game.getSetting(172) == 0;
    }

    public boolean pickUpLoot(RSGroundItem loot) {
        if (loot == null)
            return false;

        RSTile lootTile = loot.getPosition();
        if (lootTile == null)
            return false;

        if (Player.getPosition().distanceTo(lootTile) > 7) {
            if (!cameraUtils.isCameraPitching() && !cameraUtils.isCameraRotating()) {
                Walking.walkTo(lootTile);
            }
            commonUtils.sleep(150, 300);
            cameraUtils.pitchCameraAsync(General.random(33, 45));
            cameraUtils.rotateCameraToTileAsync(lootTile);
            commonUtils.sleep(500, 750);
        } else if (!loot.isOnScreen()) {
            cameraUtils.pitchCameraAsync(General.random(33, 45));
            Camera.turnToTile(lootTile);
        } else {
            RSItemDefinition itemDefinition = loot.getDefinition();
            if (itemDefinition == null)
                return false;

            int lootID = loot.getID();
            int prevLootCount = Inventory.getCount(lootID);

            if (takeGroundItem(loot, "Take " + itemDefinition.getName())) {
                long k = System.currentTimeMillis();
                while (Inventory.getCount(lootID) <= prevLootCount) {
                    if (Inventory.isFull() || Timing.timeFromMark(k) > 4000)
                        break;
                    General.sleep(50, 100);
                }
            }

            if (Inventory.getCount(lootID) > prevLootCount) {
                return true;
            }
        }
        return false;
    }

    public Point average(Point[] ps) {
        int tx = 0;
        int ty = 0;
        for (int i = 0; i < ps.length; i++) {
            tx += ps[i].x;
            ty += ps[i].y;
        }
        return new Point((tx / ps.length), (ty / ps.length));
    }

    public boolean teleportHome() {
        TABS prevTab = GameTab.getOpen();
        if (prevTab != TABS.MAGIC) {
            GameTab.open(TABS.MAGIC);
        }
        commonUtils.sleep(500, 600);
        RSInterface homeTeleport = Interfaces.get(192, 0);
        commonUtils.sleep(1000, 1750);
        if (homeTeleport != null) {
            if (homeTeleport.click("Cast")) {
                commonUtils.sleep(1000, 2000);
                GameTab.open(prevTab);
                commonUtils.sleep(14000, 16000);
                return true;
            }
        }
        return false;
    }

    public RSGroundItem findNearestLoot(int[] LOOT_IDS) {
        RSGroundItem[] hides = GroundItems.findNearest(LOOT_IDS);
        if (hides != null && hides.length > 0) {
            return hides[0];
        }
        return null;
    }

    public RSNPC findNearestTarget(String TARGET_NAME) {
        RSNPC[] targets = NPCs.findNearest(TARGET_NAME);
        if (targets != null && targets.length > 0) {
            for (int i = 0; i < targets.length; i++) {
                if (!targets[i].isInCombat()) {
                    return targets[i];
                }
            }
        }
        return null;
    }

    public RSNPC findNearestNPC(int... targetIDs) {
        RSNPC[] targets = NPCs.findNearest(targetIDs);
        if (targets != null && targets.length > 0) {
            return targets[0];
        }
        return null;
    }

    public boolean toggleRun(boolean on) {
        GameTab.TABS previousTab = GameTab.getOpen();
        if (Game.isRunOn() != on) {
            if (Options.setRunOn(on)) {
                if (!GameTab.getOpen().equals(previousTab)) {
                    GameTab.open(previousTab);
                }
                return true;
            }
        }
        return false;
    }

    public boolean operateNeck() {
        RSItem amulet = Equipment.getItem(SLOTS.AMULET);
        if (amulet == null) {
            return false;
        }
        if (!GameTab.getOpen().equals(TABS.EQUIPMENT)) {
            GameTab.open(TABS.EQUIPMENT);
            commonUtils.sleep(150, 200);
        }
        if (Interfaces.get(387, 28) != null) {
            for (RSItem i : Interfaces.get(387, 28).getItems()) {
                if (i.getID() == amulet.getID()) {
                    Mouse.clickBox(636, 255, 653, 273, 3);
                    commonUtils.sleep(689, 918);
                    if (ChooseOption.getOptions() != null) {
                        for (String option : ChooseOption.getOptions()) {
                            if (option.contains("Operate")) {
                                if (ChooseOption.select(option)) {
                                    commonUtils.sleep(890, 1251);
                                    if (NPCChat.getOptions() != null) {
                                        for (String teleportTo : NPCChat.getOptions()) {
                                            if (teleportTo.contains("Edgeville")) {
                                                if (NPCChat.selectOption(teleportTo, true)) {
                                                    commonUtils.sleep(3500, 6000);
                                                    return true;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean takeGroundItem(RSGroundItem item, String upText) {
        if (item == null)
            return false;

        RSTile pos = item.getPosition();
        if (pos == null)
            return false;

        String currentUptext = Game.getUptext();
        if (currentUptext != null && upText.contains("->")) {
            item.click();
        }

        return DynamicClicking.clickRSTile(pos, upText);
    }

    public int getEmptyInventoryCount() {
        RSItem[] items = Inventory.getAll();
        if (items != null && items.length > 0) {
            return 28 - items.length;
        }
        return 28;
    }

    public void onEnd() {
        cameraUtils.setCameraPitching(false);
        cameraUtils.setCameraRotating(false);
        cameraUtils.setCameraMovement(false);

        while (cameraUtils.getRotationThread().isAlive() || cameraUtils.getPitchThread().isAlive()) {
            cameraUtils.getRotationThread().interrupt();
            cameraUtils.getPitchThread().interrupt();
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
            }
        }
    }

    public CameraUtils getCameraUtils() {
        return cameraUtils;
    }

    public CommonUtils getCommonUtils() {
        return commonUtils;
    }
}