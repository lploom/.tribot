package scripts.clayfireplacemaker;

import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Mouse;
import org.tribot.api.interfaces.Positionable;
import org.tribot.api2007.*;
import org.tribot.api2007.Equipment.SLOTS;
import org.tribot.api2007.GameTab.TABS;
import org.tribot.api2007.Skills.SKILLS;
import org.tribot.api2007.types.*;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Ending;
import org.tribot.script.interfaces.Painting;
import scripts.utils.CommonUtils;
import scripts.utils.KMUtils;
import scripts.utils.StaticUtils;
import scripts.utils.camera.CameraUtils;
import scripts.utils.navigation.LumbridgeStairsNavigator;

import java.awt.*;

@ScriptManifest(authors = "karlrais && MrHat", category = "Construction_local", name = "ClayFireplaceMaker", description = "LEVELS CONSTRUCTION FOR ABSOLUTELY NO COST! + "
        + "Mines clay in Rimmington, makes soft clay at Rimmington well, builds fireplaces in house. "
        + "If your pick breaks it will teleport home, gets some cash from lumbridge bank, goes to Bob and fixes it. "
        + "It also attaches pickaxe head to handle should it break. "
        + "It uses house tabs to teleport back, script terminates if there are no tabs in bank. "
        + "IMPORTANT: You must have bucket or filled bucket, hammer, saw and pickaxe in inventory or equipped for this to work. "
        + "Your fireplace must be at the standard location when you first buy the house (north room from the portal). "
        + "And obviously your house must be at Rimmington.")
public class ClayFireplaceMaker extends Script implements Painting, Ending {

    private State state;

    private final CameraUtils cameraUtils = new CameraUtils();
    private final CommonUtils commonUtils = new CommonUtils();
    private final KMUtils utils = new KMUtils(cameraUtils, commonUtils);
    private final LumbridgeStairsNavigator stairsNavigator = new LumbridgeStairsNavigator(utils);

    private long runTime;
    private long startTime = System.currentTimeMillis();
    private long tnl;
    private long ttl47;

    private int constructionXpGained;
    private int constructionXpStart = Skills.getXP(SKILLS.CONSTRUCTION);
    private int miningXpGained;
    private int miningXpStart = Skills.getXP(SKILLS.MINING);
    private int[] clayRockIds = {0, 0};

    private String runTimeString;
    private String ttl47String;
    private String tnlString;
    private final static String[] BROKEN_PICKAXES = new String[]{"Broken pickaxe"};

    private final static int BUCKET_ID = 1925;
    private final static int COINS_ID = 995;
    private final static int PICKAXE_HANDLE_ID = 466;
    private final static int FILLED_BUCKET_ID = 1929;
    private final static int CLAY_ID = 434;
    private final static int SOFT_CLAY_ID = 1761;
    private final static int HAMMER_ID = 2347;
    private final static int SAW_ID = 8794;
    private final static int HOME_TELEPORT_ID = 8013;
    private final static int BRONZE_PICK_ID = 1265;
    private final static int IRON_PICK_ID = 1267;
    private final static int STEEL_PICK_ID = 1269;
    private final static int MITHRIL_PICK_ID = 1273;
    private final static int ADDY_PICK_ID = 1271;
    private final static int RUNE_PICK_ID = 1275;
    private final static int WELL_ID = 884;
    private final static int FIREPLACE_ID = 6780;
    private final static int EMPTY_FIREPLACE_ID = 4523;
    private final static int PORTAL_IN_HOUSE_ID = 4525;
    private final static int PORTAL_OUTSIDE_ID = 15478;
    private final static int[] MINING_ANIMATIONS = {624, 628, 629, 627, 626, 625};
    private final static int[] PICKAXE_IDS = {BRONZE_PICK_ID, IRON_PICK_ID, STEEL_PICK_ID, MITHRIL_PICK_ID,
            ADDY_PICK_ID, RUNE_PICK_ID};
    private final static int[] DO_NOT_DROP = {480, 482, 484, 486, 488, 490, 1265, 1267, 1269, 1273, 1271, 1275,
            FILLED_BUCKET_ID, BUCKET_ID, HAMMER_ID, SAW_ID, CLAY_ID, SOFT_CLAY_ID, HOME_TELEPORT_ID, PICKAXE_HANDLE_ID};
    private final static int[] PICK_HEADS = {480, 482, 484, 486, 488, 490};
    private final int[] NOT_TO_DEPOSIT = {HAMMER_ID, BUCKET_ID, FILLED_BUCKET_ID, SAW_ID, HOME_TELEPORT_ID,
            getPickaxeID("Bronze"), getPickaxeID("Iron"), getPickaxeID("Steel"), getPickaxeID("Mithril"),
            getPickaxeID("Addy"), getPickaxeID("Rune"), COINS_ID, 470, 471, 472, 473, 474, 475, 476, 477, 478};

    private final static RSTile BANK_TILE = new RSTile(3208, 3220, 2);
    private final static RSTile BOB_TILE = new RSTile(3231, 3203, 0);
    private final static RSTile RESPAWN_TILE = new RSTile(3223, 3215);
    private final static RSTile WELL_TILE = new RSTile(2956, 3212);
    private final static RSTile CENTRE_TILE = new RSTile(2961, 3221);
    private final static RSTile MINING_TILE1 = new RSTile(2986, 3240);
    private final static RSTile ROCK_TILE1 = new RSTile(2986, 3239);
    private final static RSTile ROCK_TILE2 = new RSTile(2987, 3240);
    private final static RSTile PORTAL_TILE = new RSTile(2953, 3224);
    private final static RSTile GROUND_FLOOR_TILE = new RSTile(3208, 3211, 0);
    private final static RSTile FIRST_FLOOR_TILE = new RSTile(3205, 3209, 1);
    private final static RSTile SECOND_FLOOR_TILE = new RSTile(3205, 3209, 2);
    private final static RSTile ROCK_TILE_WEST = new RSTile(2986, 3239, 0);
    private final static RSTile ROCK_TILE_EAST = new RSTile(2987, 3240, 0);

    private RSTile[] PATH_TO_WELL = {new RSTile(2979, 3236, 0), new RSTile(2973, 3232, 0), new RSTile(2969, 3226, 0),
            new RSTile(2964, 3222, 0), new RSTile(2959, 3219, 0), new RSTile(2957, 3214, 0)};
    private RSTile[] PATH_TO_MINE = {new RSTile(2961, 3227, 0), new RSTile(2967, 3226, 0), new RSTile(2972, 3231, 0),
            new RSTile(2977, 3235, 0), new RSTile(2980, 3238, 0), new RSTile(2986, 3240, 0)};
    private RSTile[] PATH_TO_BOB = {new RSTile(3208, 3211, 0), new RSTile(3213, 3212, 0), new RSTile(3216, 3218, 0),
            new RSTile(3222, 3218, 0), new RSTile(3228, 3218, 0), new RSTile(3233, 3218, 0), new RSTile(3235, 3213, 0),
            new RSTile(3235, 3208, 0), new RSTile(3231, 3204, 0)};

    private final static RSArea BANK_AREA = new RSArea(new RSTile[]{new RSTile(3207, 3221, 2),
            new RSTile(3211, 3221, 2), new RSTile(3210, 3216, 2), new RSTile(3208, 3216, 2)});

    private boolean cameraMovedAtWell = false;
    private boolean cameraMovedAtFireplace = false;
    private boolean isRunning = true;

    private RSTile stairsTile;
    private int distToStairs;

    @Override
    public void run() {
        onStart();
        while (isRunning) {
            calcPaint();
            state = getState();
            switch (state) {
                case WALKING_TO_RIMMINGTON:
                    walkToRimmington();
                    break;
                case MINING:
                    mining();
                    break;
                case TRAVELLING_TO_WELL:
                    travelToWell();
                    break;
                case MAKING_SOFT_CLAY:
                    makeClay();
                    break;
                case TRAVELLING_TO_PORTAL:
                    travelToPortal();
                    break;
                case CONSTRUCTION:
                    buildFireplaces();
                    break;
                case TRAVELLING_TO_MINE:
                    travelToMine();
                    break;
                case SHITHOLE:
                    shithole();
                    break;
                default:
                    sleep(100, 200);
                    break;
            }
            sleep(50, 100);
        }
    }

    private void shithole() {
        if (inLumbridge()) {
            updateStairVariables(Player.getPosition().getPlane());
            if (Inventory.getCount(BROKEN_PICKAXES) > 0 && Inventory.getCount(COINS_ID) > 500) {
                if (Player.getPosition().distanceTo(BOB_TILE) > 4) {
                    travelToBob();
                } else {
                    doBob();
                }
            } else if (Player.getPosition().getPlane() != 2) {
                travelToBank();
            } else if (atBank()) {
                if (!inBank()) {
                    moveToBanktile();
                } else {
                    doBank();
                }
            }
            if (invReady() == 1) {
                if (Inventory.getCount(PICKAXE_IDS) == 2) {

                }
                useHomeTablet();
            }
        } else {
            utils.teleportHome();
        }
    }

    private void travelToMine() {
        cameraUtils.setCameraMovement(true);
        cameraUtils.rotateCameraAsync(General.random(250, 290));
        cameraUtils.pitchCameraAsync(General.random(22, 75));
        sleep(300, 600);

        Walking.setWalkingTimeout(General.random(5232, 6541));
        if (Player.getPosition().distanceTo(MINING_TILE1) > 11) {
            if (Player.getPosition().distanceTo(PORTAL_TILE) < 4) {
                Walking.walkTo(PATH_TO_MINE[0]);
                sleep(451, 631);
            }
            Walking.walkPath(PATH_TO_MINE);
            sleep(5232, 5842);
        } else {
            if (!Player.isMoving()) {
                Walking.walkTo(MINING_TILE1);
            }
        }
    }

    private void buildFireplaces() {
        cameraUtils.setCameraMovement(false);
        // it should check if you accidentally entered in building mode
        // if(!Game.isInBuildingMode()) {
        // travelToPortal();
        // return;
        // }
        RSObject fireplace = utils.getNearestObject(new int[]{FIREPLACE_ID, EMPTY_FIREPLACE_ID}, 20);
        if (fireplace != null) {
            if (Player.getPosition().distanceTo(fireplace.getPosition()) < 3
                    && Player.getPosition().getY() <= fireplace.getPosition().getY()) { // craft
                // fireplaces
                if (!cameraMovedAtFireplace) {
                    moveCameraRandom(General.random(1, 3), fireplace);
                }
                if (fireplace.getID() == FIREPLACE_ID) {
                    if (!interfaceIsOpen()) {
                        removeFireplace(fireplace);
                    } else {
                        closeInterface();
                    }
                } else {
                    if (Camera.getCameraAngle() < 90) {
                        Camera.setCameraAngle(General.random(110, 120));
                    } else {
                        buildFireplace(fireplace);
                    }
                }
            } else { // move closer to fireplaces
                if (!Player.isMoving()) {
                    Walking.walkTo(new RSTile(fireplace.getPosition().getX() + General.random(0, 1),
                            fireplace.getPosition().getY() - 1));
                    sleep(212, 415);
                    cameraUtils.rotateCameraToTileAsync(fireplace.getPosition());
                    cameraUtils.rotateCameraAsync(General.random(22, 110));
                }
            }
        }
    }

    private void travelToPortal() {
        cameraMovedAtWell = false;
        cameraMovedAtFireplace = false;
        cameraUtils.setCameraMovement(true);
        RSObject portalOutside = utils.getNearestObject(PORTAL_OUTSIDE_ID, 20);
        RSObject portalInside = utils.getNearestObject(PORTAL_IN_HOUSE_ID, 20);
        if (Player.getPosition().distanceTo(PORTAL_TILE) < 30 && Player.getPosition().distanceTo(PORTAL_TILE) > 7) {
            Camera.turnToTile(PORTAL_TILE);
            cameraUtils.pitchCameraAsync(General.random(22, 110));
            Walking.walkPath(Walking.generateStraightPath(PORTAL_TILE));
        } else if (portalInside != null) {
            if (!portalInside.isOnScreen()) {
                int walkOrClick = General.random(0, 1);
                if (walkOrClick == 0) {
                    if (!Player.isMoving()) {
                        Walking.walkTo(portalInside.getPosition());
                        sleep(417, 716);
                        cameraUtils.rotateCameraToTileAsync(portalInside.getPosition());
                        sleep(151, 512);
                    }
                } else {
                    cameraUtils.rotateCameraToTileAsync(portalInside.getPosition());
                    cameraUtils.pitchCameraAsync(General.random(22, 50));
                    sleep(841, 1246);
                }
            } else {
                if (DynamicClicking.clickRSObject(portalInside, "Enter Portal")) {
                    commonUtils.waitUntilIdle(2578, 5847);
                }
            }
        } else if (atPortalOutside() && portalOutside != null) {
            try {
                if (!portalOutside.isOnScreen()) {
                    cameraUtils.rotateCameraToTileAsync(portalOutside.getPosition());
                    cameraUtils.pitchCameraAsync(General.random(22, 110));
                } else {
                    String[] options = NPCChat.getOptions();
                    if (options != null && options.length > 0) {
                        for (String s : options) {
                            if (s.contains("building mode")) {
                                if (NPCChat.selectOption(s, true)) {
                                    sleep(4714, 7272);
                                }
                            }
                        }
                    } else if (atPortalOutside() && DynamicClicking.clickRSObject(portalOutside, "Enter Portal")) {
                        commonUtils.waitUntilIdle(457, 738);
                    }
                }
            } catch (NullPointerException e) {
                //lel
            }
        }
    }

    private void travelToWell() {
        cameraUtils.setCameraMovement(true);
        cameraUtils.rotateCameraAsync(General.random(0, 360));
        cameraUtils.pitchCameraAsync(General.random(20, 110));
        sleep(350, 600);
        Walking.setWalkingTimeout(General.random(5232, 5842));
        if (Player.getPosition().distanceTo(WELL_TILE) > 13) {
            if (Player.getPosition().distanceTo(MINING_TILE1) < 4) {
                Walking.walkTo(PATH_TO_WELL[0]);
                sleep(155, 251);
            }
            Walking.walkPath(PATH_TO_WELL);
            sleep(4267, 5687);
        } else {
            if (!Player.isMoving()) {
                Walking.walkTo(WELL_TILE);
            }
        }
    }

    private void mining() {
        cameraUtils.setCameraMovement(false);
        if (GameTab.getOpen() != TABS.INVENTORY) {
            Inventory.open();
            sleep(150, 300);
        }
        if (Player.getPosition().distanceTo(MINING_TILE1) > 4) {
            if (Walking.walkPath(Walking.generateStraightPath(MINING_TILE1))) {
                commonUtils.waitUntilIdle(375, 671);
            }

        }
        if (Inventory.find(DO_NOT_DROP).length > 0) {
            Inventory.dropAllExcept(DO_NOT_DROP);
        }
        if (canBuildPickaxe()) {
            pickUpPickaxeHead();
            Equipment.remove(SLOTS.WEAPON);
            buildPickaxe();
        }
        if (!miningAnimation()) {
            if (!isClayIdsSet()) {
                setClayRockIds();
            } else {
                mineRocks();
            }
        }
    }

    private void walkToRimmington() {
        Walking.setWalkingTimeout(General.random(3500, 4000));
        Walking.walkPath(Walking.generateStraightPath(CENTRE_TILE));
    }

    private void onStart() {
        Mouse.setSpeed(General.random(180, 200));
    }

    private void mineRocks() {
        RSObject[] rocks = Objects.find(10, clayRockIds);
        if (rocks != null && rocks.length > 0) {
            if (DynamicClicking.clickRSObject(rocks[0], "Mine Rocks")) {
                if (rocks[0].getPosition().equals(ROCK_TILE1)) {
                    RSObject[] otherRock = Objects.getAt(ROCK_TILE2);
                    if (otherRock != null && otherRock.length > 0) {
                        sleep(100, 200);
                        utils.moveMouseToModel(otherRock[0].getModel());
                    }
                } else {
                    RSObject[] otherRock = Objects.getAt(ROCK_TILE1);
                    if (otherRock != null && otherRock.length > 0) {
                        sleep(100, 200);
                        utils.moveMouseToModel(otherRock[0].getModel());
                    }
                }
                sleep(General.random(900, 1300));
            }
        }
    }

    private void setClayRockIds() {
        RSObject[] rockW = Objects.getAt(ROCK_TILE_WEST);
        RSObject[] rockE = Objects.getAt(ROCK_TILE_EAST);

        if (rockW.length == 1 && rockE.length == 1) {
            clayRockIds[0] = rockW[0].getID();
            clayRockIds[1] = rockE[0].getID();
            return;
        }

        for (RSObject obj : rockW) {
            RSObjectDefinition definition = obj.getDefinition();
            if (definition != null) {
                if ("Rocks".equalsIgnoreCase(definition.getName())) {
                    clayRockIds[0] = obj.getID();
                }
            }
        }

        for (RSObject obj : rockE) {
            RSObjectDefinition definition = obj.getDefinition();
            if (definition != null) {
                if ("Rocks".equalsIgnoreCase(definition.getName())) {
                    clayRockIds[1] = obj.getID();
                }
            }
        }
    }

    private void travelToBob() {
        int plane = Player.getPosition().getPlane();
        if (plane > 0) {
            // walk to stairs or climb them
            if (distToStairs >= 7) {
                walkToStairs();
            } else if (distToStairs < 7)
                climbStairs();
        }
        // on ground floor
        else if (plane == 0) {
            if (!Player.isMoving()) {
                Walking.walkPath(PATH_TO_BOB);
            }
        }
    }

    private void doBob() {
        RSNPC bob = utils.findNearestTarget("Bob");
        if (bob != null) {
            if (GameTab.getOpen() != TABS.INVENTORY) {
                Inventory.open();
            } else {
                RSItem[] items = Inventory.find(BROKEN_PICKAXES);
                if (items != null && items.length > 0) {
                    if (!Game.getUptext().contains("->")) {
                        if (items[0].click("Use Broken pickaxe")) {
                            sleep(250, 300);
                            if (!bob.isOnScreen()) {
                                Camera.turnToTile(bob.getPosition());
                            }
                        }
                    }
                    if (bob.click("Use Broken pickaxe -> Bob")) {
                        commonUtils.waitUntilIdle(1300, 1500);
                        while (NPCChat.getMessage() != null || NPCChat.getOptions() != null) {

                            String message = NPCChat.getMessage();
                            String[] options = NPCChat.getOptions();

                            if (message != null && !message.equals("")) {
                                NPCChat.clickContinue(true);
                                sleep(1500, 2000);
                            } else {
                                if (options != null) {
                                    for (String s : options) {
                                        if (s.contains("Yes,")) {
                                            NPCChat.selectOption(s, true);
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

    private void buildPickaxe() {
        RSItem[] pickHead = Inventory.find(PICK_HEADS);
        RSItem[] handle = Inventory.find(PICKAXE_HANDLE_ID);
        if (pickHead != null && handle != null && pickHead.length > 0 && handle.length > 0) {
            handle[0].click("Use Pickaxe handle");
            sleep(1000, 1501);
            pickHead[0].click("Use");
        }

    }

    private void pickUpPickaxeHead() {
        RSGroundItem item = utils.findNearestLoot(PICK_HEADS);
        if (item != null) {
            if (!item.isOnScreen()) {
                Walking.walkPath(Walking.generateStraightScreenPath(item.getPosition()));
                Camera.turnToTile(item.getPosition());
            } else if (item.click("Take")) {
                commonUtils.waitUntilIdle(512, 712);
            }
        }

    }

    private void doBank() {
        RSItem weapon = Equipment.getItem(SLOTS.WEAPON);
        if (weapon != null) {
            Equipment.remove(SLOTS.WEAPON);
            sleep(250, 500);
        }
        if (GameTab.getOpen() != TABS.INVENTORY) {
            GameTab.open(TABS.INVENTORY);
            sleep(250, 500);
        } else if (!Banking.isBankScreenOpen()) {
            Banking.openBankBooth();
            sleep(250, 500);
        } else {
            Banking.depositAllExcept(NOT_TO_DEPOSIT);
            if (Inventory.getCount(BROKEN_PICKAXES) <= 0) {
                Banking.deposit(0, COINS_ID);
                int item = invReady();
                if (item != 1) {
                    // handle bucket withdrawal seperately because invready can
                    // return 2 values
                    if (item == BUCKET_ID || item == FILLED_BUCKET_ID) {
                        RSItem[] buckets = Banking.find(BUCKET_ID, FILLED_BUCKET_ID);
                        if (buckets != null && buckets.length > 0) {
                            Banking.withdraw(1, buckets[0].getID());
                            sleep(750, 1000);
                        } else {
                            println("No buckets in bank! Terminating the script!");
                            isRunning = false;
                        }
                    } else {
                        if (Banking.find(item) == null) {
                            println("Some items were not found in teh bank. Terminating script!");
                            isRunning = false;
                        }
                        Banking.withdraw(1, item);
                        sleep(750, 1000);
                    }
                } else {
                    Banking.close();
                }
            } else {
                Banking.withdraw(2000, COINS_ID);
                sleep(750, 1000);
            }
        }
    }

    private void useHomeTablet() {
        if (!GameTab.getOpen().equals(TABS.INVENTORY)) {
            GameTab.open(TABS.INVENTORY);
        }
        if (!inHouse()) {
            RSItem tab = utils.getInventoryItemById(HOME_TELEPORT_ID);
            if (tab != null) {
                if (tab.click("Break Teleport to house")) {
                    sleep(4000, 5000);
                    println("Successfully used teleport to house!");
                }
            }
        }
    }

    private void calcPaint() {
        runTime = System.currentTimeMillis() - startTime;
        runTimeString = StaticUtils.getDurationBreakdown(runTime);
        miningXpGained = Skills.getXP(SKILLS.MINING) - miningXpStart;

        constructionXpGained = Skills.getXP(SKILLS.CONSTRUCTION) - constructionXpStart;
        if (constructionXpGained > 0) {
            tnl = runTime * Skills.getXPToNextLevel(SKILLS.CONSTRUCTION) / constructionXpGained;
            ttl47 = runTime * Skills.getXPToLevel(SKILLS.CONSTRUCTION, 47) / constructionXpGained;
        }
        tnlString = StaticUtils.getDurationBreakdown(tnl);
        if (ttl47 > 0) {
            ttl47String = StaticUtils.getDurationBreakdown(ttl47);
        }
    }

    private void updateStairVariables(int plane) {
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

    private void travelToBank() {
        Walking.setWalkingTimeout(7500L);
        int plane = Player.getPosition().getPlane();
        if (plane == 0) {
            if (distToStairs <= 5) {
                climbStairs();
            } else if (distToStairs < 15 && distToStairs >= 6) {
                if (!Player.isMoving())
                    Walking.walkTo(stairsTile);
            } else {
                cameraUtils.setCameraMovement(true);
                cameraUtils.rotateCameraAsync(Camera.getCameraRotation());
                if (utils.walkPath(PATH_TO_BOB, true)) {
                    cameraUtils.setCameraMovement(false);
                }
            }
        } else if (plane > 0) {
            if (distToStairs >= 7) {
                walkToStairs();
            } else {
                climbStairs();
            }
        }
    }

    private void walkToStairs() {
        stairsNavigator.walkToStairs();
    }

    private void climbStairs() {
        stairsNavigator.climbStairs(() -> !(Inventory.getCount(COINS_ID) > 0 && Inventory.getCount(BROKEN_PICKAXES) > 0));
    }

    private void moveCameraRandom(int random, RSObject fireplace) {
        if (fireplace.getID() == FIREPLACE_ID || fireplace.getID() == EMPTY_FIREPLACE_ID) {
            for (int i = 0; i < random; i++) {
                if (i == (random - 1)) {
                    if (Player.getPosition().getX() <= fireplace.getPosition().getX()) {
                        cameraUtils.pitchCameraAsync(General.random(90, 110));
                        Camera.setCameraRotation(General.random(75, 40));
                    } else {
                        cameraUtils.pitchCameraAsync(General.random(90, 110));
                        Camera.setCameraRotation(General.random(300, 330));
                    }
                } else {
                    cameraUtils.pitchCameraAsync(General.random(22, 110));
                    Camera.setCameraRotation(General.random(0, 360));
                }
                sleep(General.random(750, 1000));
            }
            cameraMovedAtFireplace = true;
        } else {
            for (int i = 0; i < random; i++) {
                if (i == (random - 1)) {
                    cameraUtils.pitchCameraAsync(General.random(80, 110));
                    Camera.setCameraRotation(General.random(0, 360));
                } else {
                    cameraUtils.pitchCameraAsync(General.random(22, 110));
                    Camera.setCameraRotation(General.random(0, 360));
                }
                sleep(General.random(750, 1000));
            }
            cameraMovedAtWell = true;
        }
    }

    private void closeInterface() {
        if (interfaceIsOpen()) {
            Mouse.moveBox(480, 37, 487, 46);
            if (Timing.waitUptext("Close", 250)) {
                Mouse.click(1);
            }
        }
    }

    private void buildFireplace(RSObject emptyFireplace) {
        if (emptyFireplace != null) {
            if (!interfaceIsOpen()) {
                if (DynamicClicking.clickRSObject(emptyFireplace, "Build fireplace space")) {
                    sleep(650, 850);
                }
            } else {
                RSInterfaceChild buildFireplaceBtn = Interfaces.get(394, 4);
                if (buildFireplaceBtn != null) {
                    if (buildFireplaceBtn.click("Build Clay fireplace")) {
                        sleep(1121, 1612);
                    }
                }
            }
        }
    }

    private void removeFireplace(RSObject fireplace) {
        if (NPCChat.getOptions() == null) {
            if (fireplace.click("Remove Clay fireplace")) {
                sleep(750, 1000);
            }
        } else {
            if (NPCChat.getOptions() != null) {
                NPCChat.selectOption("Yes", true);
                sleep(500, 750);
            }
        }

    }

    private void makeClay() {
        cameraUtils.setCameraMovement(false);

        RSObject well = utils.getNearestObject(WELL_ID, 10);

        if (well != null && Inventory.getCount(CLAY_ID) > 0) {
            if (GameTab.getOpen() != TABS.INVENTORY) {
                Inventory.open();
                sleep(100, 200);
            }
            if (Player.getPosition().distanceTo(WELL_TILE) >= 4) {
                if (!Player.isMoving()) {
                    Walking.walkTo(WELL_TILE);
                }
            } else if (!cameraMovedAtWell) {
                moveCameraRandom(General.random(1, 3), well);
            } else if (!well.isOnScreen()) {
                Camera.turnToTile(well.getPosition());
            } else if (Camera.getCameraAngle() < 80) {
                Camera.setCameraAngle(General.random(81, 105));
            } else {
                RSItem[] inv = Inventory.getAll();
                if (inv != null && inv.length > 0) {
                    for (RSItem item : inv) {
                        if (item != null) {
                            if (item.getID() == FILLED_BUCKET_ID || item.getID() == BUCKET_ID) {
                                if (!Game.getUptext().contains("->")) {
                                    item.click("Use");
                                }
                                sleep(62, 124);
                                if (Inventory.getCount(FILLED_BUCKET_ID) > 0) {
                                    RSItem clay = utils.getInventoryItemById(CLAY_ID);
                                    if (clay != null) {
                                        clay.click();
                                        sleep(645, 845);
                                    }
                                } else if (Inventory.getCount(BUCKET_ID) > 0) {
                                    well.click();
                                    sleep(142, 269);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void moveToBanktile() {
        if (!Player.isMoving()) {
            if (Walking.walkTo(BANK_TILE)) {
                cameraUtils.rotateCameraToTileAsync(BANK_TILE);
                cameraUtils.pitchCameraAsync(General.random(22, 110));
                for (int i = 0; i < 3; i++) {
                    RSTile destination = Game.getDestination();
                    if (destination == null || BANK_AREA.contains(destination)) {
                        break;
                    }
                    sleep(121, 251);
                }
                sleep(500, 750);
            }
        }
    }

    private boolean havePickaxe() {
        if (Inventory.getCount(PICKAXE_IDS) > 0) {
            return true;
        }
        String weaponName = Combat.getWeaponName();
        if (weaponName != null && weaponName.contains("pickaxe") && !weaponName.contains("Broken")) {
            return true;
        }
        RSItem item = Equipment.getItem(SLOTS.WEAPON);
        if (item != null) {
            int itemId = item.getID();
            for (int i = 0; i < PICKAXE_IDS.length; i++) {
                if (itemId == PICKAXE_IDS[i]) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean nearCentreTile(RSTile pos) {
        return pos.distanceTo(CENTRE_TILE) < 15;
    }

    private boolean farFromCentreTile(RSTile pos) {
        return pos.distanceTo(CENTRE_TILE) < 30;
    }

    private boolean atMine(RSTile pos) {
        return pos.distanceTo(MINING_TILE1) < 14;
    }

    private boolean atWell(RSTile pos) {
        return pos.distanceTo(WELL_TILE) < 4;
    }

    private boolean inHouse() {
        return utils.getNearestObject(PORTAL_IN_HOUSE_ID, 20) != null;
    }

    private boolean interfaceIsOpen() {
        return (Interfaces.get(394) != null);
    }

    private boolean inLumbridge() {
        return Player.getPosition().distanceTo(RESPAWN_TILE) < 20;
    }

    private boolean atPortalOutside() {
        return Player.getPosition().distanceTo(PORTAL_TILE) < 8;
    }

    private boolean atBank() {
        return Player.getPosition().distanceTo(BANK_TILE) < 13;
    }

    private boolean inBank() {
        return BANK_AREA.contains(Player.getPosition());
    }

    private boolean miningAnimation() {
        for (int i = 0; i < MINING_ANIMATIONS.length; i++) {
            if (Player.getAnimation() == MINING_ANIMATIONS[i]) {
                return true;
            }
        }
        return false;
    }

    private boolean isClayIdsSet() {
        for (int id : clayRockIds) {
            if (id == 0)
                return false;
        }
        return true;
    }

    private boolean canBuildPickaxe() {
        return utils.findNearestLoot(PICK_HEADS) != null || Inventory.getCount(PICK_HEADS) > 0;
    }

    private int invReady() {
        if (Inventory.getAll().length <= 6) {
            if (Inventory.getCount(BUCKET_ID) == 1 || Inventory.getCount(FILLED_BUCKET_ID) == 1) {
                if (Inventory.getCount(SAW_ID) == 1) {
                    if (Inventory.getCount(HAMMER_ID) == 1) {
                        if (Inventory.getCount(HOME_TELEPORT_ID) >= 1) {
                            if (Inventory.getCount(PICKAXE_IDS) >= 1) {
                                return 1;
                            } else {
                                return getPickaxeID();
                            }
                        } else {
                            return HOME_TELEPORT_ID;
                        }

                    } else {
                        return HAMMER_ID;
                    }
                } else {
                    return SAW_ID;
                }
            }
            return BUCKET_ID;
        } else {
            return 0;
        }
    }

    private int getPickaxeID() {
        int lvl = Skills.getActualLevel(SKILLS.MINING);
        if (lvl >= 41) {
            if (Banking.find(getPickaxeID("Rune")) != null && Banking.find(getPickaxeID("Rune")).length > 0) {
                return getPickaxeID("Rune");
            }
        }
        if (lvl >= 31) {
            if (Banking.find(getPickaxeID("Addy")) != null && Banking.find(getPickaxeID("Addy")).length > 0) {
                println("addy");
                return getPickaxeID("Addy");
            }
        }
        if (lvl >= 21) {
            if (Banking.find(getPickaxeID("Mithril")) != null && Banking.find(getPickaxeID("Mithril")).length > 0) {
                println("mithril");
                return getPickaxeID("Mithril");
            }
        }
        if (lvl >= 6) {
            if (Banking.find(getPickaxeID("Steel")) != null && Banking.find(getPickaxeID("Steel")).length > 0) {
                return getPickaxeID("Steel");
            }
        }
        if (Banking.find(getPickaxeID("Iron")) != null && Banking.find(getPickaxeID("Iron")).length > 0) {
            return getPickaxeID("Iron");
        }
        if (Banking.find(getPickaxeID("Bronze")) != null && Banking.find(getPickaxeID("Bronze")).length > 0) {
            return getPickaxeID("Bronze");
        }
        return 0;
    }

    private int getPickaxeID(String type) {
        switch (type) {
            case "Bronze":
                return BRONZE_PICK_ID;
            case "Iron":
                return IRON_PICK_ID;
            case "Steel":
                return STEEL_PICK_ID;
            case "Mithril":
                return MITHRIL_PICK_ID;
            case "Addy":
                return ADDY_PICK_ID;
            case "Rune":
                return RUNE_PICK_ID;
        }
        return 0;
    }

    private int getEquipmentId(SLOTS slot) {
        RSItem piece = Equipment.getItem(slot);
        if (piece != null) {
            return piece.getID();
        }
        return -1;
    }

    private State getState() {
        RSTile pos = Player.getPosition();
        int clay = Inventory.getCount(CLAY_ID);
        int soft = Inventory.getCount(SOFT_CLAY_ID);
        if (Player.getRSPlayer().isInCombat()) {
            return State.RUNNING_FROM_RANDOM;
        }

        if (atWell(pos)) {
            if (clay > 0) {
                return State.MAKING_SOFT_CLAY;
            } else if (soft > 0) {
                return State.TRAVELLING_TO_PORTAL;
            } else {
                return State.TRAVELLING_TO_MINE;
            }
        } else if (atMine(pos)) {
            if (havePickaxe() || getEquipmentId(SLOTS.WEAPON) == PICKAXE_HANDLE_ID || Inventory.getCount(PICKAXE_HANDLE_ID) > 0) {
                if (!Inventory.isFull()) {
                    return State.MINING;
                } else {
                    return State.TRAVELLING_TO_WELL;
                }
            } else {
                return State.SHITHOLE;
            }
        } else if (inHouse()) {
            if (soft >= 3) {
                return State.CONSTRUCTION;
            } else {
                return State.TRAVELLING_TO_PORTAL;
            }
        } else {
            if (nearCentreTile(pos)) {
                if (Inventory.isFull()) {
                    if (soft > 0) {
                        if (clay == 0) {
                            return State.TRAVELLING_TO_PORTAL;
                        } else {
                            return State.TRAVELLING_TO_WELL;
                        }
                    } else {
                        return State.TRAVELLING_TO_WELL;
                    }
                } else {
                    return State.TRAVELLING_TO_MINE;
                }
            } else if (farFromCentreTile(pos)) {
                return State.WALKING_TO_RIMMINGTON;
            } else {
                return State.SHITHOLE;
            }
        }
    }

    private RSObject getStair(Positionable stairTile) {
        RSObject[] stairs = Objects.getAt(stairTile);
        if (stairs != null && stairs.length > 0) {
            for (RSObject stair : stairs) {
                println(stair.getID());
                if (stair.getDefinition() != null) {
                    for (String action : stair.getDefinition().getActions()) {
                        if (action.contains("Climb")) {
                            return stair;
                        }
                    }
                }
            }
        }
        return null;
    }

    private enum State {
        TRAVELLING_TO_MINE, TRAVELLING_TO_WELL, TRAVELLING_TO_PORTAL, MINING, MAKING_SOFT_CLAY, CONSTRUCTION, RUNNING_FROM_RANDOM, SHITHOLE, WALKING_TO_RIMMINGTON
    }

    @Override
    public void onPaint(Graphics g) {
        g.setColor(Color.WHITE);
        g.drawString("State: " + state, 370, 260);
        g.drawString("Running Time: " + runTimeString, 370, 275);
        g.drawString("Construction XP Gained: " + constructionXpGained, 370, 290);
        g.drawString("Mining XP Gained: " + miningXpGained, 370, 305);
        g.drawString("TNL: " + tnlString, 370, 320);
        g.drawString("TTL 47: " + ttl47String, 370, 335);
    }

    @Override
    public void onEnd() {
        utils.onEnd();
    }
}
