package scripts.cowhidekiller;

import java.awt.Color;
import java.awt.Graphics;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.SwingUtilities;

import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Mouse;
import org.tribot.api.interfaces.Positionable;
import org.tribot.api.types.generic.Filter;
import org.tribot.api.util.ABCUtil;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Camera;
import org.tribot.api2007.Combat;
import org.tribot.api2007.Equipment;
import org.tribot.api2007.Game;
import org.tribot.api2007.GameTab;
import org.tribot.api2007.GameTab.TABS;
import org.tribot.api2007.GroundItems;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.Objects;
import org.tribot.api2007.Options;
import org.tribot.api2007.Player;
import org.tribot.api2007.Skills;
import org.tribot.api2007.Skills.SKILLS;
import org.tribot.api2007.Walking;
import org.tribot.api2007.ext.Doors;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSCharacter;
import org.tribot.api2007.types.RSGroundItem;
import org.tribot.api2007.types.RSInterfaceChild;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSItemDefinition;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSObjectDefinition;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Breaking;
import org.tribot.script.interfaces.Ending;
import org.tribot.script.interfaces.MessageListening07;
import org.tribot.script.interfaces.Painting;
import org.tribot.script.interfaces.RandomEvents;
import org.tribot.script.interfaces.Starting;

import scripts.utils.CameraUtils;
import scripts.utils.CameraUtils.CARDINAL;
import scripts.utils.CommonUtils;
import scripts.utils.FixedSizedList;
import scripts.utils.KMUtils;
import scripts.utils.StaticUtils;
import scripts.utils.WorldHopper;

@ScriptManifest(authors = "karlrais", category = "Combat_local", name = "CowHideKiller", description = "The script will kill cows at the Lumbridge cow area. "
        + "You can choose to loot cowhides or just kill cows. " + "You can choose to bank in the Lumbridge top floor, Lumbridge cellar or Al-Kharid. "
        + "It runs away from Evil Chicken and Swarm. It death-walks and re-equips your items upon death. It also resets your combat stance so you do not gain defence XP.")
public class CowHideKiller extends Script implements Painting, RandomEvents, MessageListening07, Ending, Breaking, Starting {

    private long startTime = System.currentTimeMillis();
    private long runTime;
    private String runTimeString;
    private int hidesGathered;
    private int hidesPerHour;
    private int expGained;
    private int expPerHour;
    private int expStart;

    private final static RSTile GATE_TILE = new RSTile(3253, 3266);
    private final static RSTile CENTRE_TILE = new RSTile(3243, 3235);
    private final static RSTile BANK_TILE = new RSTile(3208, 3220, 2);
    private final static RSTile CELLAR_BANK_TILE = new RSTile(3218, 9623, 0);
    private final static RSTile TRAPDOOR_TILE = new RSTile(3210, 3216, 0);
    private final static RSTile LADDER_TILE = new RSTile(3209, 9616, 0);
    private final static RSTile UPPER_AREA_TILE = new RSTile(3249, 3293, 0);
    private final static RSTile LOWER_AREA_TILE = new RSTile(3260, 3265, 0);
    private final static RSTile GROUND_FLOOR_TILE = new RSTile(3206, 3209, 0);
    private final static RSTile FIRST_FLOOR_TILE = new RSTile(3205, 3209, 1);
    private final static RSTile SECOND_FLOOR_TILE = new RSTile(3205, 3209, 2);
    private final static RSTile AK_GATE_TILE = new RSTile(3267, 3227);
    private final static RSTile STAIRS_TILE_0 = new RSTile(3204, 3207, 0);
    private final static RSTile STAIRS_TILE_1 = new RSTile(3204, 3207, 1);
    private final static RSTile STAIRS_TILE_2 = new RSTile(3205, 3208, 2);
    private final static RSTile GATE_OPEN_TILE = new RSTile(3252, 3266);
    private final static RSTile GATE_OPEN_TILE2 = new RSTile(3251, 3266);
    private final static RSTile GATE_CLOSED_TILE = new RSTile(3253, 3266);
    private final static RSTile GATE_CLOSED_TILE2 = new RSTile(3253, 3267);

    private RSTile[] pathToCows = {new RSTile(3213, 3214), new RSTile(3216, 3218), new RSTile(3222, 3218), new RSTile(3235, 3220), new RSTile(3245, 3225),
            new RSTile(3254, 3225), new RSTile(3259, 3232), new RSTile(3259, 3239), new RSTile(3255, 3246), new RSTile(3251, 3252), new RSTile(3250, 3258),
            new RSTile(3250, 3266),};
    private RSTile[] pathToStairs = {new RSTile(3250, 3265), new RSTile(3251, 3254), new RSTile(3260, 3245), new RSTile(3260, 3238), new RSTile(3260, 3231),
            new RSTile(3254, 3226), new RSTile(3245, 3226), new RSTile(3236, 3226), new RSTile(3234, 3218), new RSTile(3222, 3219), new RSTile(3216, 3219),
            new RSTile(3206, 3209)};
    private RSTile[] pathToAkGate = {new RSTile(3268, 3227), new RSTile(3250, 3265), new RSTile(3251, 3254), new RSTile(3260, 3245), new RSTile(3260, 3238),
            new RSTile(3260, 3231), new RSTile(3266, 3227)};
    private RSTile[] pathToAkBank = {new RSTile(3268, 3227), new RSTile(3271, 3219), new RSTile(3272, 3213), new RSTile(3274, 3208), new RSTile(3277, 3201),
            new RSTile(3280, 3196), new RSTile(3282, 3191), new RSTile(3282, 3185), new RSTile(3278, 3180), new RSTile(3277, 3176), new RSTile(3276, 3170),
            new RSTile(3269, 3168)};
    private RSTile gateTileOutside = new RSTile(3249, 3263);
    private RSTile gateTileInside = new RSTile(3254, 3267);

    private final static RSArea COW_PIT = new RSArea(new RSTile[]{new RSTile(3252, 3273, 0), new RSTile(3240, 3285, 0), new RSTile(3240, 3300, 0),
            new RSTile(3266, 3300, 0), new RSTile(3266, 3255, 0), new RSTile(3253, 3255, 0)});
    private final static RSArea BANKAREA_TOP = new RSArea(
            new RSTile[]{new RSTile(3207, 3221, 2), new RSTile(3211, 3221, 2), new RSTile(3210, 3216, 2), new RSTile(3208, 3216, 2)});
    private final static RSArea BANKAREA_AK = new RSArea(
            new RSTile[]{new RSTile(3268, 3174), new RSTile(3273, 3174), new RSTile(3273, 3161), new RSTile(3268, 3161)});

    private final static int[] JUNK = {526, 2132, 1971, 1917, 1969};
    private final static int COW_DEATH_ANIMATION_ID = 5851;
    private final static int COWHIDE_ID = 1739;
    private final static int COINS_ID = 995;
    private final static int TRAPDOOR_ID = 14880;
    private final static int CELLAR_CHEST_ID = 12308;
    private final static int AK_GATE_ID = 2882;
    private final static int AK_GATE_ID2 = 2883;

    private CameraUtils camera = new CameraUtils();
    private CommonUtils commons = new CommonUtils();
    private KMUtils utils = new KMUtils(camera, commons);
    private State state;

    private int[] gear = new int[11];
    private int combatStyleIndexOnStart = -1;
    private int startingWorld;
    private int plane = -1;
    private int distToStairs = -1;
    private int bankLocation = -1;
    private int attackXPOnStart;
    private int strXPOnStart;
    private int defXPOnStart;

    private boolean isRunning = true;
    private boolean getLoot = false;
    private boolean donePrinceAliQuest;
    private boolean killingTarget = false;

    private ABCUtil abc = new ABCUtil();
    private boolean abcDelayUsed;
    private long timeAtLooting;
    private long timeAtCombat;

    private CowHideKillerGui gui;
    private RSTile stairsTile = GROUND_FLOOR_TILE;
    private List<RSCharacter> blackList = new FixedSizedList<RSCharacter>(3);

    @Override
    public void run() {
        while (isRunning) {
            state = getState();
            switch (state) {
                case BANKING:
                    banking(bankLocation);
                    break;
                case KILLING_COWS:
                    killingCows();
                    break;
                case TRAVELLING_TO_BANK:
                    travellingToBank();
                    break;
                case TRAVELLING_TO_COWS:
                    travellingToCows();
                    break;
                case HOPPING_WORLDS:
                    hopWorlds();
                    break;
                case SHITHOLE:
                    utils.teleportHome();
                    break;
            }
            sleep(100, 200);
        }
    }

    private void travellingToCows() {
        updateStairVariables();
        Walking.setWalkingTimeout(7500L);
        if (Inventory.getCount(gear) > 0) {
            afterLife();
        } else {
            moveToCows();
        }
    }

    private void travellingToBank() {
        updateStairVariables();
        Walking.setWalkingTimeout(9000L);
        if (Inventory.getCount(gear) > 0) {
            afterLife();
        } else {
            travelToBank(bankLocation);
        }
    }

    private void killingCows() {
        closeSkillInterfaceIfOpen();
        if (!isOriginalCombatStance()) {
            switchToOriginalStance();
        } else {
            killCows();
        }
    }

    private void switchToOriginalStance() {
        TABS open = GameTab.getOpen();
        if (Combat.selectIndex(combatStyleIndexOnStart)) {
            GameTab.open(open);
        }
    }

    private void hopWorlds() {
        boolean wasInCombat = false;
        while (Player.getRSPlayer().isInCombat()) {
            if (!commons.isAutoRetaliateIsEnabled()) {
                commons.toggleAutoRetaliate();
            }
            wasInCombat = true;
            sleep(100, 200);
        }
        if (wasInCombat) {
            sleep(7000, 8000);
        }
        super.setLoginBotState(false);
        WorldHopper.hop(startingWorld - 300);
        super.setLoginBotState(true);
    }

    private void closeSkillInterfaceIfOpen() {
        if (Interfaces.get(499) != null) {
            RSInterfaceChild closeButton = Interfaces.get(499, 24);
            if (closeButton != null) {
                closeButton.click();
            }
        }
    }

    private void afterLife() {
        println("You PROBABLY just died. Trying to re-equip items.");
        sleep(1500, 2500);
        if (bankLocation == 2) {
            println("Changed bank location to Lumbridge top floor!");
            bankLocation = 0;
        }

        if (!GameTab.getOpen().equals(TABS.INVENTORY)) {
            GameTab.open(TABS.INVENTORY);
            sleep(500, 750);
        }

        long k = System.currentTimeMillis();
        while (Inventory.getCount(gear) > 0) {
            if (Timing.timeFromMark(k) > 15000) {
                break;
            }
            String uptext = Game.getUptext();
            if (uptext != null && uptext.contains("->")) {
                commons.clickChatBox();
            }
            RSItem[] items = Inventory.find(gear);
            for (RSItem item : items) {
                if (item.click("Wield", "Wear")) {
                    sleep(1500, 2500);
                }
            }
            sleep(100, 200);
        }

        Combat.selectIndex(combatStyleIndexOnStart);

        k = System.currentTimeMillis();
        while (!Combat.isAutoRetaliateOn()) {
            if (Timing.timeFromMark(k) > 15000) {
                break;
            }
            Combat.setAutoRetaliate(true);
            sleep(2000, 2500);
        }

        if (!GameTab.getOpen().equals(TABS.INVENTORY)) {
            GameTab.open(TABS.INVENTORY);
            sleep(1500, 2500);
        }
    }

    private void banking(int bankLocation) {
        if (bankLocation == 0) {
            if (!inLumbyBank()) {
                moveToBanktile();
            } else {
                doBank(bankLocation);
            }
        } else {
            doBank(bankLocation);
        }
    }

    private void killCows() {
        Inventory.drop(JUNK);

        RSGroundItem loot = findNearestLoot(COWHIDE_ID);
        RSNPC cow = findNearestTarget();

        // Loot or Kill?
        if (loot == null && cow == null) {
            return;
        } else if (loot == null) {
            killTarget(cow, COW_DEATH_ANIMATION_ID, 0, getLoot);
        } else if (getLoot && cow == null) {
            pickUpLoot(loot);
        } else if (getLoot && Player.getPosition().distanceTo(loot) <= Player.getPosition().distanceTo(cow.getPosition())) {
            pickUpLoot(loot);
        } else if (cow != null) {
            killTarget(cow, COW_DEATH_ANIMATION_ID, 0, getLoot);
        }

        abc.BOOL_TRACKER.USE_CLOSEST.reset();
    }

    public boolean pickUpLoot(RSGroundItem loot) {
        if (loot == null)
            return false;

        RSTile lootTile = loot.getPosition();
        if (lootTile == null)
            return false;

        if (Player.getPosition().distanceTo(lootTile) > 7) {
            if (!camera.isCameraPitching() && !camera.isCameraRotating()) {
                if (!abcDelayUsed) {
                    abc.waitNewOrSwitchDelay(getLastBusyTime(), true);
                    abcDelayUsed = true;
                }
                abcToggleRunOn();
                Walking.walkTo(lootTile);
            }
            sleep(150, 300);
            camera.pitchCameraAsync(General.random(33, 45));
            camera.rotateCameraToTileAsync(lootTile);
            sleep(500, 750);
        } else if (!loot.isOnScreen()) {
            camera.pitchCameraAsync(General.random(33, 45));
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
                abcDelayUsed = false;
                timeAtLooting = System.currentTimeMillis();
                hidesGathered++;
                return true;
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

        if (!abcDelayUsed) {
            abc.waitNewOrSwitchDelay(getLastBusyTime(), true);
            abcDelayUsed = true;
        }

        return DynamicClicking.clickRSTile(pos, upText);
    }

    private void killTarget(RSNPC target, int deathAnimationID, int minHp, boolean waitForLoot) {
        boolean misclicked = false;
        if (Player.getPosition().distanceTo(target) > 7) {
            if (!abcDelayUsed) {
                abc.waitNewOrSwitchDelay(getLastBusyTime(), true);
                abcDelayUsed = true;
            }
            abcToggleRunOn();
            Walking.walkTo(target);
            General.sleep(150, 300);
            camera.pitchCameraAsync(General.random(23, 50));
            camera.rotateCameraToTileAsync(target.getPosition());
            General.sleep(750, 1250);
        } else if (!target.isOnScreen()) {
            camera.pitchCameraAsync(General.random(23, 60));
            camera.rotateCameraToTileAsync(target.getPosition());
            General.sleep(450, 600);
        } else {
            killingTarget = true;
            if (!abcDelayUsed) {
                abc.waitNewOrSwitchDelay(getLastBusyTime(), true);
                abcDelayUsed = true;
            }
            if (DynamicClicking.clickRSNPC(target, "Attack")) {
                int antibanIndex = General.random(0, 4);
                if (antibanIndex < 3) {
                    Mouse.move(General.random(-1000, 1000), General.random(-1000, 1000));
                } else if (antibanIndex == 3) {
                    // do nothing
                } else {
                    camera.rotateCameraAsync(General.random(0, 360));
                }

                long timer = System.currentTimeMillis();
                while (target.isValid()) {
                    if (Skills.getCurrentLevel(SKILLS.HITPOINTS) < minHp) {
                        break;
                    }
                    if (!COW_PIT.contains(target)) {
                        break;
                    }
                    if (!killingTarget) { // 'Someone else is fighting that' server
                        // message received
                        if (!target.isInCombat() && !target.isInteractingWithMe()) {
                            blackList.add(target); // probably some dude splashing the
                            // target
                        }
                        break;
                    }
                    // target just died or someone else attacked it
                    if (target.isInCombat() && Player.getRSPlayer().getInteractingCharacter() == null) {
                        if (target.getHealth() > 0 || !waitForLoot) { // target didnt
                            // die
                            break;
                        }
                    }

                    // In case of a misclick the bot will just finish off the
                    // misclicked mob
                    if (Player.getRSPlayer().isInCombat()) {
                        timer = System.currentTimeMillis();
                    } else { // Target is still legit but character got stuck
                        if (System.currentTimeMillis() - timer > General.random(2612, 2919)) {
                            if (!target.isOnScreen()) {
                                Camera.turnToTile(target.getPosition());
                            }
                            if (Player.getPosition().distanceTo(target) > 7) {
                                abcToggleRunOn();
                                Walking.walkTo(target);
                                General.sleep(150, 300);
                            }
                            if (!Player.isMoving() && target.getAnimation() != deathAnimationID) {
                                if (DynamicClicking.clickRSNPC(target, "Attack")) {
                                    timer = System.currentTimeMillis();
                                }
                            }

                        }
                    }
                    abc.performTimedActions(getStatToTrack());
                    abcPerformHoverNext();
                    General.sleep(151, 267);
                }
                timeAtCombat = System.currentTimeMillis();
            } else {
                misclicked = true;
            }
        }

        killingTarget = false;

        // target was misclicked but delay was used, return so abcDelay used stays
        // true
        if (misclicked && abcDelayUsed) {
            return;
        }

        abcDelayUsed = false;
    }

    private long getLastBusyTime() {
        return timeAtCombat > timeAtLooting ? timeAtCombat : timeAtLooting;
    }

    private void abcPerformHoverNext() {
        if (abc.BOOL_TRACKER.HOVER_NEXT.next()) {
            RSNPC nextTarget = findNextHoverableCow();
            if (nextTarget != null) {
                if (nextTarget.hover()) {
                    abc.BOOL_TRACKER.HOVER_NEXT.reset();
                }
            }
        }
    }

    private RSNPC findNextHoverableCow() {
        RSNPC[] targets = NPCs.findNearest("Cow", "Cow calf");
        for (RSNPC target : targets) {
            if (target != null) {
                if (!target.isInCombat() && !target.isInteractingWithMe() && target.isOnScreen()) {
                    return target;
                }
            }
        }
        return null;
    }

    private void moveToCows() {
        if (plane > 0) {
            if (distToStairs >= 7)
                walkToStairs();
            else
                climbStairs(plane);

        } else if (inCellar()) {
            if (Player.getPosition().distanceTo(LADDER_TILE) < 7)
                climbLadder();
            else {
                abcToggleRunOn();
                Walking.walkTo(LADDER_TILE);
                sleep(2500, 3500);
            }
        } else if (plane == 0) {
            travelToCows();
        } else {
            println("Error! Plane returned < 0; Please report this!");
        }
    }

    private void climbLadder() {
        RSObject[] ladder = Objects.getAt(LADDER_TILE);
        if (ladder != null && ladder.length > 0) {
            if (!ladder[0].isOnScreen()) {
                camera.pitchCameraAsync(General.random(45, 100));
                Camera.turnToTile(ladder[0].getPosition());
            }
            if (DynamicClicking.clickRSObject(ladder[0], "Climb-up")) {
                commons.waitUntilIdle(1000, 1500);
            }
        }

    }

    private void travelToBank(int bankLocation) {
        if (plane == 0) {
            if (atCows()) {
                if (!atGate()) {
                    if (!Player.isMoving())
                        moveToGate();
                } else {
                    if (!isGateOpen()) {
                        openGate();
                    } else {
                        if (!Player.isMoving()) {
                            abcToggleRunOn();
                            Walking.walkTo(gateTileOutside);
                            sleep(100, 200);
                        }
                    }
                }
            } else if (bankLocation == 0) { //lumbridge top
                if (distToStairs <= 5) {
                    climbStairs(plane);
                } else if (distToStairs < 20 && distToStairs >= 6) {
                    if (!isDestinationCorrect()) {
                        abcToggleRunOn();
                        commons.sleepwalkTo(GROUND_FLOOR_TILE);
                    }
                } else {
                    camera.setCameraMovement(true);
                    camera.rotateCameraAsync(Camera.getCameraRotation());
                    abcToggleRunOn();
                    if (commons.sleepwalkPath(pathToStairs)) {
                        camera.setCameraMovement(false);
                    }
                }
            } else if (bankLocation == 1) { // lumbridge cellar
                if (getDistanceToTrapdoor() <= 6) {
                    climbTrapdoor();
                } else if (getDistanceToTrapdoor() < 15 && getDistanceToTrapdoor() >= 7) {
                    if (!Player.isMoving()) {
                        abcToggleRunOn();
                        Walking.walkTo(TRAPDOOR_TILE);
                    }
                } else if (inCellar()) {
                    if (Player.getPosition().distanceTo(CELLAR_BANK_TILE) > 6) {
                        abcToggleRunOn();
                        Walking.walkTo(CELLAR_BANK_TILE);
                        sleep(1500, 2500);
                    }
                } else {
                    camera.setCameraMovement(true);
                    camera.rotateCameraAsync(Camera.getCameraRotation());
                    if (bankLocation == 0) {
                        abcToggleRunOn();
                        if (Walking.walkPath(pathToStairs)) {
                            camera.setCameraMovement(false);
                        }
                    } else if (bankLocation == 1) {
                        abcToggleRunOn();
                        if (Walking.walkPath(pathToStairs)) {
                            camera.setCameraMovement(false);
                        }
                    }
                }
            } else if (bankLocation == 2) { // al-kharid
                if (!BANKAREA_AK.contains(Player.getPosition())) {
                    if (!inAlKharid()) {
                        if (!atAKGate()) {
                            sleepwalkPath(pathToAkGate);
                        } else {
                            passGate();
                        }
                    } else {
                        sleepwalkPath(pathToAkBank);
                    }
                }
            }
        } else if (plane > 0) {
            if (distToStairs >= 7)
                walkToStairs();
            else
                climbStairs(plane);
        }

    }

    private boolean isDestinationCorrect() {
        RSTile dest = Game.getDestination();
        if (dest != null) {
            return isDestinationInsideCastle(dest);
        }
        return false;
    }

    private boolean isDestinationInsideCastle(RSTile dest) {
        RSTile xTile = new RSTile(3206, 3208, 0);
        if (dest.equals(xTile))
            return true;
        if (dest.getPosition().getY() < 3209)
            return false;
        if (dest.getPosition().getX() < 3205)
            return false;
        if (dest.getPosition().getY() > 3211)
            return false;

        return true;
    }

    public boolean sleepwalkPath(final RSTile[] path) {
        RSTile dest = Game.getDestination();
        if (dest != null) {
            if (dest.distanceTo(path[path.length - 1]) > 3) {
                abcToggleRunOn();
                if (Walking.walkPath(Walking.randomizePath(path, 1, 1))) {
                    return true;
                }
            }
        } else {
            abcToggleRunOn();
            if (Walking.walkPath(path)) {
                return true;
            }
        }
        return false;
    }

    private void runAway() {
        Options.setRunOn(true);
        sleep(100, 300);
        if (Player.getPosition().distanceTo(LOWER_AREA_TILE) <= Player.getPosition().distanceTo(UPPER_AREA_TILE)) {
            if (sleepwalkToTile(UPPER_AREA_TILE)) {
                commons.waitForDestination();
            }
        } else {
            if (sleepwalkToTile(LOWER_AREA_TILE)) {
                commons.waitForDestination();
            }
        }
        sleep(750, 1000);
    }

    private void saveInitialEquipment() {
        RSItem[] equippedItems = Equipment.getItems();
        for (int i = 0; i < equippedItems.length; i++) {
            gear[i] = equippedItems[i].getID();
        }

    }

    private void calcPaint() {
        runTime = System.currentTimeMillis() - startTime;
        runTimeString = StaticUtils.getDurationBreakdown(runTime);
        expGained = getTotalCombatExp() - expStart;
        double d = runTime;
        if (d > 0) {
            expPerHour = (int) (expGained / (d / 1000 / 3600)) / 1000;
            hidesPerHour = (int) (hidesGathered / (d / 1000 / 3600));
        }
    }

    private void moveToGate() {
        if (sleepwalkToTile(gateTileInside)) {
            camera.rotateCameraToTileAsync(GATE_OPEN_TILE);
        }
    }

    private void moveToBanktile() {
        if (!Player.isMoving()) {
            abcToggleRunOn();
            if (Walking.walkTo(BANK_TILE)) {
                camera.rotateCameraToTileAsync(BANK_TILE);
                camera.pitchCameraAsync(General.random(22, 110));
                for (int i = 0; i < 3; i++) {
                    RSTile destination = Game.getDestination();
                    if (destination == null || BANKAREA_TOP.contains(destination)) {
                        break;
                    }
                    sleep(121, 251);
                }
                sleep(500, 750);
            }
        }
    }

    private void doBank(int bankLocation) {
        if (GameTab.getOpen() != TABS.INVENTORY) {
            GameTab.open(TABS.INVENTORY);
            sleep(100, 200);
        }
        if (Inventory.getAll().length != 0 || (bankLocation == 2 && !donePrinceAliQuest && Inventory.getCount(COINS_ID) < 20)) {
            if (Banking.isBankScreenOpen()) {
                if (bankLocation == 2) {
                    Banking.depositAllExcept(995);
                } else {
                    Banking.depositAll();
                }
                sleep(300, 500);
                if (bankLocation == 2 && Inventory.getCount(COINS_ID) < 20 && !donePrinceAliQuest) {
                    Banking.withdraw(1000, COINS_ID);
                    sleep(300, 500);
                }
                Banking.close();
                sleep(300, 500);
            } else {
                if (bankLocation == 0) {
                    camera.pitchCameraAsync(General.random(45, 110));
                    camera.rotateCameraToTileAsync(new RSTile(BANK_TILE.getPosition().getX(), BANK_TILE.getPosition().getY() + 2));
                    if (Banking.openBank()) {
                        sleep(123, 321);
                    }
                } else if (bankLocation == 1) {
                    RSObject[] bankChest = Objects.findNearest(20, CELLAR_CHEST_ID);
                    if (bankChest != null && bankChest.length > 0) {
                        if (Player.getPosition().distanceTo(bankChest[0]) < 7) {
                            if (!bankChest[0].isOnScreen()) {
                                camera.pitchCameraAsync(General.random(45, 110));
                                Camera.turnToTile(bankChest[0].getPosition());
                            } else {
                                if (DynamicClicking.clickRSObject(bankChest[0], "Bank Chest")) {
                                    sleep(1246, 1542);
                                }
                            }
                        } else {
                            abcToggleRunOn();
                            if (Walking.walkPath(Walking.generateStraightPath(bankChest[0]))) {
                                camera.pitchCameraAsync(General.random(45, 110));
                                Camera.turnToTile(bankChest[0].getPosition());
                                commons.waitUntilIdle(400, 800);
                            }
                        }
                    }
                } else if (bankLocation == 2) {
                    camera.turnCameraTo(CARDINAL.WEST);
                    if (Banking.openBank()) {
                        sleep(123, 321);
                    }
                }
            }
        }
    }

    private void travelToCows() {
        if (!atGate()) {
            camera.setCameraMovement(true);
            camera.rotateCameraAsync(Camera.getCameraRotation());
            sleep(125, 150);
            if (bankLocation == -1 || bankLocation == 0 || bankLocation == 1 || (bankLocation == 2 && !inAlKharid())) {
                // failsafe if north of the gate
                if (gateTileOutside.getY() + 3 > Player.getPosition().getY()) {
                    abcToggleRunOn();
                    if (Walking.walkPath(pathToCows)) {
                        camera.setCameraMovement(false);
                        commons.waitUntilIdle(750, 1000);
                    }
                } else {
                    abcToggleRunOn();
                    if (Walking.walkPath(Walking.generateStraightPath(gateTileOutside))) {
                        camera.setCameraMovement(false);
                        commons.waitForDestination();
                    }
                }
            } else if (bankLocation == 2 && inAlKharid()) {
                if (!atAKGate()) {
                    abcToggleRunOn();
                    sleepwalkPath(reverse(pathToAkBank));
                } else {
                    passGate();
                }
            }
        } else {
            if (!isGateOpen()) {
                openGate();
            } else {
                killingCows();
            }
        }
    }

    public RSTile[] reverse(RSTile[] tiles) {
        RSTile[] reversed = new RSTile[tiles.length];
        for (int i = tiles.length - 1; i >= 0; i--) {
            reversed[tiles.length - 1 - i] = tiles[i];
        }
        return reversed;
    }

    private void openGate() {
        RSTile gateTile = getRandomGateTile();
        RSObject gate = Doors.getDoorAt(gateTile);
        if (gate != null) {
            if (!gate.isOnScreen()) {
                camera.rotateCameraToTileAsync(gate.getPosition());
                sleep(250, 450);
            }
            if (Doors.handleDoor(gate, true)) {
                commons.waitForDestination();
                sleep(50, 100);
            }
        }
    }

    private RSTile getRandomGateTile() {
        int random = General.random(0, 1);
        RSTile gateTile;
        if (random == 0) {
            gateTile = GATE_CLOSED_TILE;
        } else {
            gateTile = GATE_CLOSED_TILE2;
        }
        return gateTile;
    }

    private void walkToStairs() {
        sleepwalkToTile(stairsTile);
    }

    private boolean sleepwalkToTile(RSTile tile) {
        RSTile dest = Game.getDestination();
        if (dest != null) {
            if (dest.distanceTo(tile) > 3) {
                abcToggleRunOn();
                if (Walking.walkPath(Walking.generateStraightPath(tile))) {
                    return true;
                }
            }
        } else {
            abcToggleRunOn();
            if (Walking.walkPath(Walking.generateStraightPath(tile))) {
                camera.rotateCameraToTileAsync(tile);
                camera.pitchCameraAsync(General.random(23, 110));
                return true;
            }
        }
        return false;
    }

    private boolean abcToggleRunOn() {
        if (!Game.isRunOn() && Game.getRunEnergy() >= abc.INT_TRACKER.NEXT_RUN_AT.next()) {
            abc.INT_TRACKER.NEXT_RUN_AT.reset();
            return Options.setRunOn(true);
        }
        return false;
    }

    private void passGate() {
        RSObject[] gate = Objects.findNearest(20, AK_GATE_ID, AK_GATE_ID2);
        if (gate != null && gate.length > 0) {
            if (!gate[0].isOnScreen()) {
                camera.pitchCameraAsync(General.random(45, 75));
                Camera.turnToTile(gate[0].getPosition());
            } else {
                if (DynamicClicking.clickRSObject(gate[0], "Pay-toll(10gp)")) {
                    commons.waitUntilIdle(1000, 1500);
                }
            }
        }
    }

    private void climbStairs(int plane) {
        String uptext = "";
        RSTile stairTile;

        switch (plane) {
            case 0:
                uptext = "Climb-up";
                camera.pitchCameraAsync(General.random(22, 45));
                stairTile = STAIRS_TILE_0;
                Camera.turnToTile(stairTile);
                break;
            case 1:
                stairTile = STAIRS_TILE_1;
                if (state == State.TRAVELLING_TO_COWS) {
                    uptext = "Climb-down";
                } else if (state == State.TRAVELLING_TO_BANK) {
                    uptext = "Climb-up";
                }
                break;
            case 2:
                stairTile = STAIRS_TILE_2;
                if (Camera.getCameraAngle() < 65) {
                    camera.pitchCameraAsync(General.random(65, 110));
                    sleep(212, 312);
                }
                uptext = "Climb-down";
                break;
            default:
                stairTile = STAIRS_TILE_0;
                sleep(200);
                break;
        }

        RSObject stairs = getStair(stairTile);
        if (stairs != null) {
            if (!stairs.isOnScreen()) {
                Camera.turnToTile(stairsTile);
                sleep(100, 200);
            }
            if (DynamicClicking.clickRSObject(stairs, uptext + " Staircase")) {
                if (plane == 0)
                    commons.waitUntilIdle(125, 175);
                else
                    sleep(1251, 1567);
            }
        }

    }

    private void climbTrapdoor() {
        RSObject[] trapdoor = Objects.findNearest(20, TRAPDOOR_ID);
        if (trapdoor != null && trapdoor.length > 0) {
            camera.pitchCameraAsync(General.random(60, 100));
            if (!trapdoor[0].isOnScreen()) {
                Camera.turnToTile(stairsTile);
                sleep(100, 200);
            }
            if (DynamicClicking.clickRSObject(trapdoor[0], "Climb-down Trapdoor")) {
                commons.waitUntilIdle(1000, 2000);
            }
        }
    }

    private void updateStairVariables() {
        this.plane = Player.getPosition().getPlane();
        switch (plane) {
            case 0:
                stairsTile = GROUND_FLOOR_TILE;
                distToStairs = Player.getPosition().distanceTo(GROUND_FLOOR_TILE);
                break;
            case 1:
                stairsTile = FIRST_FLOOR_TILE;
                distToStairs = Player.getPosition().distanceTo(FIRST_FLOOR_TILE);
                break;
            case 2:
                stairsTile = SECOND_FLOOR_TILE;
                distToStairs = Player.getPosition().distanceTo(SECOND_FLOOR_TILE);
                break;
            default:
                println("Player.getPosition().getPlane() returned invalid value! Trying luck with default values.");
                stairsTile = GROUND_FLOOR_TILE;
                distToStairs = Player.getPosition().distanceTo(GROUND_FLOOR_TILE);
                break;
        }
    }

    private boolean isOriginalCombatStance() {
        return combatStyleIndexOnStart == Combat.getSelectedStyleIndex();
    }

    private boolean atAKGate() {
        return Player.getPosition().distanceTo(AK_GATE_TILE) < 7;
    }

    private boolean isGateOpen() {
        return Doors.isDoorAt(GATE_OPEN_TILE2, true);
    }

    private boolean inCellar() {
        return Player.getPosition().distanceTo(CELLAR_BANK_TILE) < 20;
    }

    private boolean inAlKharid() {
        return Player.getPosition().getX() > 3267;
    }

    private boolean atGate() {
        return Player.getPosition().distanceTo(GATE_TILE) < 7;
    }

    private boolean inLumbyBank() {
        return BANKAREA_TOP.contains(Player.getPosition());
    }

    private boolean atAnyBank() {
        return (Player.getPosition().getPlane() == 2 && Player.getPosition().distanceTo(BANK_TILE) < 13 || inCellar() || inAlKharidBank());
    }

    private boolean inAlKharidBank() {
        return BANKAREA_AK.contains(Player.getPosition());
    }

    private boolean atCows() {
        return COW_PIT.contains(Player.getPosition());
    }

    private boolean isTeleportRandom(RANDOM_SOLVERS r) {
        return r == RANDOM_SOLVERS.APPENDAGE || r == RANDOM_SOLVERS.BEEKEEPER || r == RANDOM_SOLVERS.CAPTARNAV || r == RANDOM_SOLVERS.DRILLDEMON
                || r == RANDOM_SOLVERS.FREAKYFORESTER || r == RANDOM_SOLVERS.FROGCAVE || r == RANDOM_SOLVERS.GRAVEDIGGER || r == RANDOM_SOLVERS.MAZE
                || r == RANDOM_SOLVERS.MIME || r == RANDOM_SOLVERS.MOLLY || r == RANDOM_SOLVERS.MORDAUT || r == RANDOM_SOLVERS.PILLORY
                || r == RANDOM_SOLVERS.PINBALL || r == RANDOM_SOLVERS.PRISONPETE || r == RANDOM_SOLVERS.QUIZ || r == RANDOM_SOLVERS.SCAPERUNE;
    }

    private int getDistanceToTrapdoor() {
        return Player.getPosition().distanceTo(TRAPDOOR_TILE);
    }

    private int getTotalCombatExp() {
        return Skills.getXP(SKILLS.ATTACK) + Skills.getXP(SKILLS.STRENGTH) + Skills.getXP(SKILLS.DEFENCE) + Skills.getXP(SKILLS.HITPOINTS)
                + Skills.getXP(SKILLS.MAGIC) + Skills.getXP(SKILLS.RANGED);
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

    private RSGroundItem findNearestLoot(int lootId) {
        RSGroundItem[] hides = GroundItems.findNearest(new Filter<RSGroundItem>() {
            @Override
            public boolean accept(RSGroundItem item) {
                return item != null && item.getID() == lootId && COW_PIT.contains(item);
            }
        });

        if (hides.length == 0)
            return null;

        if (abc.BOOL_TRACKER.USE_CLOSEST.next()) {
            if (hides.length > 1) {
                return hides[1];
            }
        }

        return hides[0];
    }

    private RSNPC findNearestTarget() {
        RSCharacter character = Player.getRSPlayer().getInteractingCharacter();
        if (character instanceof RSNPC) {
            RSNPC target = (RSNPC) character;
            if (target.getCombatLevel() == 2) {
                return target;
            }
        }
        character = Combat.getTargetEntity();
        if (character instanceof RSNPC) {
            RSNPC target = (RSNPC) character;
            if (target.getCombatLevel() == 2) {
                return target;
            }
        }

        RSNPC[] cows = NPCs.findNearest(new Filter<RSNPC>() {
            @Override
            public boolean accept(RSNPC cow) {
                return cow != null && !cow.isInCombat() && cow.getCombatLevel() == 2 && !blackList.contains(cow) && COW_PIT.contains(cow.getPosition());
            }
        });

        if (cows.length == 0)
            return null;

        if (abc.BOOL_TRACKER.USE_CLOSEST.next()) {
            if (cows.length > 1) {
                return cows[1];
            }
        }

        return cows[0];
    }

    private Skills.SKILLS getStatToTrack() {
        int attXpDiff = Skills.getXP(SKILLS.ATTACK) - attackXPOnStart;
        int strXpDiff = Skills.getXP(SKILLS.STRENGTH) - strXPOnStart;
        int defXpDiff = Skills.getXP(SKILLS.DEFENCE) - defXPOnStart;
        if (attXpDiff > strXpDiff && attXpDiff > defXpDiff) {
            return SKILLS.ATTACK;
        } else if (strXpDiff > attXpDiff && strXpDiff > defXpDiff) {
            return SKILLS.STRENGTH;
        } else if (defXpDiff > attXpDiff && defXpDiff > strXpDiff) {
            return SKILLS.DEFENCE;
        }
        return SKILLS.HITPOINTS;
    }

    private enum State {
        TRAVELLING_TO_COWS, TRAVELLING_TO_BANK, KILLING_COWS, BANKING, SHITHOLE, HOPPING_WORLDS
    }

    private State getState() {
        if (Game.getCurrentWorld() != startingWorld) {
            return State.HOPPING_WORLDS;
        } else if (atCows()) {
            if (!Inventory.isFull())
                return State.KILLING_COWS;
            else
                return State.TRAVELLING_TO_BANK;
        } else if (atAnyBank()) {
            if ((Inventory.getAll().length == 0 && (bankLocation == 0 || bankLocation == 1))
                    || (bankLocation == 2 && Inventory.getCount(COINS_ID) >= 20 && !donePrinceAliQuest && Inventory.getCount(COWHIDE_ID) <= 0)
                    || (bankLocation == 2 && donePrinceAliQuest && Inventory.getAll().length == 0) || bankLocation == -1) {
                return State.TRAVELLING_TO_COWS;
            } else {
                return State.BANKING;
            }
        } else if (Player.getPosition().distanceTo(CENTRE_TILE) > 100 && !inCellar()) {
            return State.SHITHOLE;
        } else {
            if (Inventory.isFull())
                return State.TRAVELLING_TO_BANK;
            else
                return State.TRAVELLING_TO_COWS;
        }
    }

    @Override
    public void onPaint(Graphics g) {
        calcPaint();
        g.setColor(Color.WHITE);
        g.drawString("State: " + state, 370, 260);
        g.drawString("Running Time: " + runTimeString, 370, 275);
        g.drawString("Hides Gathered: " + hidesGathered, 370, 290);
        g.drawString("Hides per Hour: " + hidesPerHour, 370, 305);
        g.drawString("Exp gained: " + expGained, 370, 320);
        g.drawString("Exp per Hour: " + ">" + expPerHour + "k", 370, 335);
        g.drawString("Dist to stairs: " + distToStairs, 370, 365);
        g.drawString("Camera rotation: " + camera.isCameraMovement(), 370, 20);
        // g.drawString("Camera angle: " + Camera.getCameraAngle(), 370, 35);
    }

    @Override
    public void onRandom(RANDOM_SOLVERS r) {
        if (isTeleportRandom(r)) {
            return;
        }

        if (r == RANDOM_SOLVERS.SECURITYGUARD || r == RANDOM_SOLVERS.STRANGEPLANT) {
            return;
        }

        if (r == RANDOM_SOLVERS.COMBATRANDOM) {
            if (COW_PIT.contains(Player.getPosition())) {
                runAway();
            }
            return;
        }

        long k = System.currentTimeMillis();
        while (Player.getRSPlayer().isInCombat()) {
            if (Timing.timeFromMark(k) > 5000) {
                break;
            }
            sleep(100, 200);
        }

    }

    @Override
    public void randomSolved(RANDOM_SOLVERS arg0) {

    }

    @Override
    public boolean randomFailed(RANDOM_SOLVERS r) {
        if (isTeleportRandom(r)) {
            return false;
        }
        return true;
    }

    @Override
    public void onEnd() {
        utils.onEnd();
        gui.dispose();
    }

    @Override
    public void onBreakEnd() {

    }

    @Override
    public void onBreakStart(long breakTime) {
        if (!utils.isAutoRetaliateIsEnabled()) {
            Combat.setAutoRetaliate(true);
        }
        while (Player.getRSPlayer().isInCombat()) {
            sleep(100, 200);
        }
        long t = System.currentTimeMillis();
        while (System.currentTimeMillis() - t < General.random(10000, 11000)) {
            sleep(100, 200);
        }
        startTime += breakTime;
    }

    @Override
    public void onStart() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {
                        gui = new CowHideKillerGui();
                        gui.setVisible(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Error creating GUI!");
                    }
                }
            });
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (gui == null) {
            sleep(100);
        }
        while (!gui.isComplete()) {
            if (!gui.isVisible()) {
                sleep(1000);
                if (!gui.isComplete()) {
                    isRunning = false;
                    break;
                }
            }
            sleep(100);
        }

        if (isRunning == false) {
            return;
        }

        getLoot = gui.getLoot();
        bankLocation = gui.getBankLocation();
        if (getLoot) {
            println("Killing cows and looting hides!");
        } else {
            println("Only killing cows, not looting or banking!");
        }

        if (bankLocation == -1) {

        } else if (bankLocation == 0) {
            println("Banking in Lumbridge castle top floor!");
        } else if (bankLocation == 1) {
            println("Banking in Lumbridge cellar!");
        } else if (bankLocation == 2) {
            println("Banking in Al-Kharid!");
        } else {
            println("This should never be printed!");
        }

        donePrinceAliQuest = gui.getPrinceAliQuest();

        gui.dispose();

        Mouse.setSpeed(250);
        expStart = getTotalCombatExp();
        Walking.setWalkingTimeout(7500L);
        saveInitialEquipment();
        combatStyleIndexOnStart = Combat.getSelectedStyleIndex();
        startingWorld = Game.getCurrentWorld();
        if (!Combat.isAutoRetaliateOn()) {
            Combat.setAutoRetaliate(true);
        }
        attackXPOnStart = Skills.getXP(SKILLS.ATTACK);
        strXPOnStart = Skills.getXP(SKILLS.STRENGTH);
        defXPOnStart = Skills.getXP(SKILLS.DEFENCE);

        General.useAntiBanCompliance(true);

        sleep(100, 200);
    }

    @Override
    public void clanMessageReceived(String name, String message) {
    }

    @Override
    public void duelRequestReceived(String arg0, String arg1) {
    }

    @Override
    public void personalMessageReceived(String name, String message) {
    }

    @Override
    public void playerMessageReceived(String name, String message) {
    }

    @Override
    public void serverMessageReceived(String message) {
        if (message.contains("Someone else is fighting that")) {
            killingTarget = false;
        }
    }

    @Override
    public void tradeRequestReceived(String name) {

    }
}