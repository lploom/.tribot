package scripts.cowhidekiller;

import java.awt.Color;
import java.awt.Graphics;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Mouse;
import org.tribot.api.interfaces.Positionable;
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
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSObjectDefinition;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Breaking;
import org.tribot.script.interfaces.Ending;
import org.tribot.script.interfaces.Painting;
import org.tribot.script.interfaces.RandomEvents;

import scripts.utils.CameraUtils;
import scripts.utils.CommonUtils;
import scripts.utils.KMUtils;
import scripts.utils.StaticUtils;
import scripts.utils.WorldHopper;

@ScriptManifest(authors = "karlrais", category = "Combat_local", name = "CowHideKiller", description = "The script will kill cows at the Lumbridge cow area. "
    + "You can choose to loot cowhides or just kill cows. "
    + "You can choose to bank in the Lumbridge top floor, Lumbridge cellar or Al-Kharid. "
    + "It runs away from Evil Chicken and Swarm. It death-walks and re-equips your items upon death. It also resets your combat stance so you do not gain defence XP.")
public class CowHideKiller extends Script implements Painting, RandomEvents, Ending, Breaking {

  private final long startTime = System.currentTimeMillis();
  private long runTime;
  private String runTimeString;
  private int hidesGathered;
  private int hidesPerHour;
  private int expGained;
  private int expPerHour;
  private int expStart;

  private final static RSTile GATE_TILE = new RSTile(3253, 3266);
  private final static RSTile GATE_TILE_OUTSIDE = new RSTile(3249, 3263);
  private final static RSTile GATE_TILE_INSIDE = new RSTile(3254, 3267);
  private final static RSTile CENTRE_TILE = new RSTile(3243, 3235);
  private final static RSTile BANK_TILE = new RSTile(3208, 3220, 2);
  private final static RSTile CELLAR_BANK_TILE = new RSTile(3218, 9623, 0);
  private final static RSTile TRAPDOOR_TILE = new RSTile(3210, 3216, 0);
  private final static RSTile LADDER_TILE = new RSTile(3209, 9616, 0);
  private final static RSTile UPPER_AREA_TILE = new RSTile(3249, 3293, 0);
  private final static RSTile LOWER_AREA_TILE = new RSTile(3260, 3265, 0);
  private final static RSTile GROUND_FLOOR_TILE = new RSTile(3208, 3211, 0);
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

  private final static RSTile[] PATH_TO_COWS = { new RSTile(3213, 3214),
    new RSTile(3216, 3218), new RSTile(3222, 3218), new RSTile(3235, 3220),
    new RSTile(3245, 3225), new RSTile(3254, 3225), new RSTile(3259, 3232),
    new RSTile(3259, 3239), new RSTile(3255, 3246), new RSTile(3251, 3252),
    new RSTile(3250, 3258), new RSTile(3250, 3266), };
  private final static RSTile[] PATH_TO_STAIRS = { new RSTile(3250, 3265),
    new RSTile(3251, 3254), new RSTile(3260, 3245), new RSTile(3260, 3238),
    new RSTile(3260, 3231), new RSTile(3254, 3226), new RSTile(3245, 3226),
    new RSTile(3236, 3226), new RSTile(3234, 3218), new RSTile(3222, 3219),
    new RSTile(3216, 3219), new RSTile(3208, 3212) };
  private final static RSTile[] PATH_TO_AK_GATE = { new RSTile(3268, 3227),
    new RSTile(3250, 3265), new RSTile(3251, 3254), new RSTile(3260, 3245),
    new RSTile(3260, 3238), new RSTile(3260, 3231), new RSTile(3266, 3227) };
  private final static RSTile[] PATH_TO_AK_BANK = { new RSTile(3268, 3227),
    new RSTile(3271, 3219), new RSTile(3272, 3213), new RSTile(3274, 3208),
    new RSTile(3277, 3201), new RSTile(3280, 3196), new RSTile(3282, 3191),
    new RSTile(3282, 3185), new RSTile(3278, 3180), new RSTile(3277, 3176),
    new RSTile(3276, 3170), new RSTile(3269, 3168) };

  private final static RSArea COW_PIT = new RSArea(new RSTile[] {
      new RSTile(3252, 3273, 0), new RSTile(3240, 3285, 0),
      new RSTile(3240, 3300, 0), new RSTile(3266, 3300, 0),
      new RSTile(3266, 3255, 0), new RSTile(3253, 3255, 0) });
  private final static RSArea BANKAREA_TOP = new RSArea(new RSTile[] {
      new RSTile(3207, 3221, 2), new RSTile(3211, 3221, 2),
      new RSTile(3210, 3216, 2), new RSTile(3208, 3216, 2) });
  private final static RSArea BANKAREA_AK = new RSArea(new RSTile[] {
      new RSTile(3268, 3174), new RSTile(3273, 3174), new RSTile(3273, 3161),
      new RSTile(3268, 3161) });

  private final static int COW_DEATH_ANIMATION_ID = 5851;
  private final static int[] JUNK = { 526, 2132, 1971, 1917, 1969 };
  private final static int COWHIDE_ID = 1739;
  private final static int COINS_ID = 995;
  private final static int TRAPDOOR_ID = 14880;
  private final static int CELLAR_CHEST_ID = 12308;
  private final static int AK_BANK_BOOTH_ID = 2196;
  private final static int AK_GATE_ID = 2882;
  private final static int AK_GATE_ID2 = 2883;

  private CameraUtils cameraUtils = new CameraUtils();
  private CommonUtils commonUtils = new CommonUtils(cameraUtils);
  private KMUtils utils = new KMUtils(cameraUtils, commonUtils);
  private State state;

  private int[] gear = new int[11];
  private int selectedCombatStyleIndex = -1;
  private int startingWorld;
  private int plane = -1;
  private int distToStairs = -1;
  private int bankLocation = -1;
  private int floorToToggleRun = 0;

  private boolean isRunning = true;
  private boolean getLoot = false;
  private boolean donePrinceAliQuest;

  private CowHideKillerGui gui;
  private RSTile stairsTile = GROUND_FLOOR_TILE;

  @Override
  public void run() {
    onStart();
    while (isRunning) {
      calcPaint();
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
    this.plane = Player.getPosition().getPlane();
    updateStairVariables(plane);
    Walking.setWalkingTimeout(7500L);
    if (Inventory.getCount(gear) > 0)
      afterLife();
    else
      moveToCows();

  }
  private void travellingToBank() {
    this.plane = Player.getPosition().getPlane();
    updateStairVariables(plane);
    Walking.setWalkingTimeout(9000L);
    if (Inventory.getCount(gear) > 0)
      afterLife();
    else
      travelToBank(bankLocation);

  }
  private void killingCows() {
    closeSkillInterfaceIfOpen();
    if (!isOriginalCombatStance())
      switchToOriginalStance();
    else
      killCows();
  }
  private void switchToOriginalStance() {
    TABS open = GameTab.getOpen();
    if (Combat.selectIndex(selectedCombatStyleIndex))
      GameTab.open(open);
  }
  private void hopWorlds() {
    boolean wasInCombat = false;
    while (Player.getRSPlayer().isInCombat()) {
      if (!commonUtils.isAutoRetaliateIsEnabled()) {
        commonUtils.toggleAutoRetaliate();
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
      if (Timing.timeFromMark(k) > 15000)
        break;

      String uptext = Game.getUptext();
      if (uptext != null && uptext.contains("->"))
        commonUtils.clickChatBox();

      RSItem[] items = Inventory.find(gear);
      for (RSItem item : items) {
        if (item.click("Wield", "Wear"))
          sleep(1500, 2500);
      }
      sleep(100, 200);
    }
    k = System.currentTimeMillis();
    while (isOriginalCombatStance()) {
      if (Timing.timeFromMark(k) > 15000)
        break;
      Combat.selectIndex(selectedCombatStyleIndex);
      sleep(1500, 2500);
    }
    k = System.currentTimeMillis();

    while (!utils.isAutoRetaliateIsEnabled()) {
      if (Timing.timeFromMark(k) > 15000)
        break;
      commonUtils.toggleAutoRetaliate();
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
        floorToToggleRun = General.random(0, 2);
        doBank(bankLocation);
      }
    } else
      doBank(bankLocation);
  }
  private void killCows() {
    utils.dropJunk(JUNK);

    if (!getLoot && Game.getRunEnergy() > General.random(10, 100)) {
      utils.toggleRun(true);
    }

    RSGroundItem loot = findNearestLoot(COWHIDE_ID);
    RSNPC cow = findNearestTarget();

    // Loot or Kill?
    if (loot == null && cow == null) {
      return;
    } else if (loot == null) {
      killTarget(cow, COW_DEATH_ANIMATION_ID, 0, getLoot);
    } else if (getLoot && cow == null) {
      if (utils.pickUpLoot(loot))
        hidesGathered++;
    } else if (getLoot
        && Player.getPosition().distanceTo(loot) <= Player.getPosition()
        .distanceTo(cow.getPosition())) {
      if (utils.pickUpLoot(loot))
        hidesGathered++;
    } else if (cow != null) {
      killTarget(cow, COW_DEATH_ANIMATION_ID, 0, getLoot);
    }
  }
  private void killTarget(RSNPC target, int deathAnimationID, int minHp,
      boolean getLoot) {
    if (Player.getPosition().distanceTo(target) > 7) {
      if (!cameraUtils.isCameraPitching() && !cameraUtils.isCameraRotating()) {
        Walking.walkTo(target);
        commonUtils.sleep(150, 300);
      }
      cameraUtils.pitchCameraAsync(General.random(23, 50));
      cameraUtils.rotateCameraToTileAsync(target.getPosition());
      commonUtils.sleep(1000, 1500);
    } else if (!target.isOnScreen()) {
      cameraUtils.pitchCameraAsync(General.random(23, 60));
      cameraUtils.rotateCameraToTileAsync(target.getPosition());
      commonUtils.sleep(450, 600);
    } else {
      if (commonUtils.clickModel(target.getModel(), "Attack")) {

        int antibanIndex = General.random(0, 4);
        if (antibanIndex < 3) {
          Mouse.move(General.random(-1000, 1000), General.random(-1000, 1000));
        } else if (antibanIndex == 3) {

        } else {
          cameraUtils.rotateCameraAsync(General.random(0, 360));
        }

        long timer = System.currentTimeMillis();
        while (target.isValid()) {

          if (Skills.getCurrentLevel(SKILLS.HITPOINTS) < minHp) {
            break;
          }

          if (!COW_PIT.contains(target)) {
            break;
          }

          // target just died or someone else attacked it
          if (target.isInCombat()
              && Player.getRSPlayer().getInteractingCharacter() == null) {
            // target didnt die
            if (target.getHealth() > 0 || !getLoot) {
              break;
            }
          }

          // In case of a misclick the bot will just finish off the
          // misclicked mob
          if (Player.getRSPlayer().isInCombat()) {
            timer = System.currentTimeMillis();
          }
          // Target is still legit but character got stuck
          else {
            if (System.currentTimeMillis() - timer > General.random(2612, 2919)) {
              if (!target.isOnScreen()) {
                Camera.turnToTile(target.getPosition());
              }
              if (Player.getPosition().distanceTo(target) > 7) {
                Walking.walkTo(target);
                commonUtils.sleep(150, 300);
              }
              if (!Player.isMoving()
                  && target.getAnimation() != deathAnimationID) {
                if (commonUtils.clickModel(target.getModel(), "Attack")) {
                  timer = System.currentTimeMillis();
                }
              }

            }
          }
          commonUtils.sleep(151, 267);
        }

      }

    }
  }
  private void moveToCows() {
    if (plane > 0) {
      if (floorToToggleRun == 2 && plane == 2)
        utils.toggleRun(true);
      else if (floorToToggleRun == 1 && plane == 1)
        utils.toggleRun(true);

      if (distToStairs >= 7)
        walkToStairs(plane);
      else
        climbStairs(plane);

    } else if (inCellar()) {
      if (Player.getPosition().distanceTo(LADDER_TILE) < 7)
        climbLadder();
      else {
        Walking.walkTo(LADDER_TILE);
        sleep(2500, 3500);
      }
    } else if (plane == 0) {
      if (floorToToggleRun == 0) {
        utils.toggleRun(true);
      }
      travelToCows();
    } else {
      println("Error! Plane returned < 0; Please report this!");
    }
  }
  private void climbLadder() {
    RSObject[] ladder = Objects.getAt(LADDER_TILE);
    if (ladder != null && ladder.length > 0) {
      if (!ladder[0].isOnScreen()) {
        cameraUtils.pitchCameraAsync(General.random(45, 100));
        Camera.turnToTile(ladder[0].getPosition());
      }
      if (commonUtils.clickModel(ladder[0].getModel(), "Climb-up")) {
        commonUtils.waitUntilIdle(1000, 1500);
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
              Walking.walkTo(GATE_TILE_OUTSIDE);
              sleep(100, 200);
            }
          }
        }
      } else if (bankLocation == 0) {
        if (distToStairs <= 5) {
          climbStairs(plane);
        } else if (distToStairs < 15 && distToStairs >= 6) {
          if (!Player.isMoving())
            Walking.walkTo(stairsTile);
        } else {
          cameraUtils.setCameraMovement(true);
          cameraUtils.rotateCameraAsync(Camera.getCameraRotation());
          if (bankLocation == 0) {
            if (Walking.walkPath(PATH_TO_STAIRS)) {
              cameraUtils.setCameraMovement(false);
            }
          } else if (bankLocation == 1) {
            if (Walking.walkPath(PATH_TO_STAIRS)) {
              cameraUtils.setCameraMovement(false);
            }
          }
        }
      } else if (bankLocation == 1) {
        if (getDistanceToTrapdoor() <= 6) {
          climbTrapdoor();
        } else if (getDistanceToTrapdoor() < 15 && getDistanceToTrapdoor() >= 7) {
          if (!Player.isMoving()) {
            Walking.walkTo(TRAPDOOR_TILE);
          }
        } else if (inCellar()) {
          if (Player.getPosition().distanceTo(CELLAR_BANK_TILE) > 6) {
            Walking.walkTo(CELLAR_BANK_TILE);
            sleep(1500, 2500);
          }
        } else {
          cameraUtils.setCameraMovement(true);
          cameraUtils.rotateCameraAsync(Camera.getCameraRotation());
          if (bankLocation == 0) {
            if (Walking.walkPath(PATH_TO_STAIRS)) {
              cameraUtils.setCameraMovement(false);
            }
          } else if (bankLocation == 1) {
            if (Walking.walkPath(PATH_TO_STAIRS)) {
              cameraUtils.setCameraMovement(false);
            }
          }
        }
      } else if (bankLocation == 2) {
        if (!BANKAREA_AK.contains(Player.getPosition())) {
          if (!inAlKharid()) {
            if (!atAKGate()) {
              commonUtils.sleepwalkPath(PATH_TO_AK_GATE);
            } else {
              passGate();
            }
          } else {
            commonUtils.sleepwalkPath(PATH_TO_AK_BANK);
          }
        }
      }
    } else if (plane > 0) {
      if (distToStairs >= 7)
        walkToStairs(plane);
      else
        climbStairs(plane);
    }

  }
  private void runAway() {
    utils.toggleRun(true);
    sleep(200, 300);
    Walking.walkTo(Player.getPosition());
    sleep(100, 300);
    if (Player.getPosition().distanceTo(LOWER_AREA_TILE) <= Player
        .getPosition().distanceTo(UPPER_AREA_TILE)) {
      if (Walking.walkPath(Walking.generateStraightPath(UPPER_AREA_TILE))) {
        commonUtils.waitForDestination();
      }
    } else {
      if (Walking.walkPath(Walking.generateStraightPath(LOWER_AREA_TILE))) {
        commonUtils.waitForDestination();
      }
    }
    sleep(750, 1000);
  }
  private void onStart() {
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

    if (isRunning == false)
      return;

    getLoot = gui.getLoot();
    bankLocation = gui.getBankLocation();
    if (getLoot)
      println("Killing cows and looting hides!");
    else
      println("Only killing cows, not looting or banking!");

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
    selectedCombatStyleIndex = Combat.getSelectedStyleIndex();
    startingWorld = Game.getCurrentWorld();
    if (!commonUtils.isAutoRetaliateIsEnabled()) {
      commonUtils.toggleAutoRetaliate();
    }
    sleep(General.random(100, 200));
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
    Walking.walkTo(Player.getPosition());
    sleep(100, 200);
    if (Walking.blindWalkTo(GATE_TILE_INSIDE)) {
      cameraUtils.rotateCameraToTileAsync(GATE_OPEN_TILE);
    }
  }
  private void moveToBanktile() {
    if (!Player.isMoving()) {
      if (Walking.walkTo(BANK_TILE)) {
        cameraUtils.rotateCameraToTileAsync(BANK_TILE);
        cameraUtils.pitchCameraAsync(General.random(22, 110));
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
    if (Inventory.getAll().length != 0
        || (bankLocation == 2 && !donePrinceAliQuest && Inventory
        .getCount(COINS_ID) < 20)) {
      if (Banking.isBankScreenOpen()) {
        if (bankLocation == 2) {
          Banking.depositAllExcept(995);
        } else {
          Banking.depositAll();
        }
        sleep(300, 500);
        if (bankLocation == 2 && Inventory.getCount(COINS_ID) < 20
            && !donePrinceAliQuest) {
          Banking.withdraw(1000, COINS_ID);
          sleep(300, 500);
        }
        Banking.close();
        sleep(300, 500);
      } else {
        if (bankLocation == 0) {
          cameraUtils.pitchCameraAsync(General.random(45, 110));
          cameraUtils.rotateCameraToTileAsync(new RSTile(BANK_TILE
              .getPosition().getX(), BANK_TILE.getPosition().getY() + 2));
          if (utils.openBankBooth(utils.getLumbyBankBoothID())) {
            sleep(1246, 1542);
          }
        } else if (bankLocation == 1) {
          RSObject[] bankChest = Objects.findNearest(20, CELLAR_CHEST_ID);
          if (bankChest != null && bankChest.length > 0) {
            if (Player.getPosition().distanceTo(bankChest[0]) < 7) {
              if (!bankChest[0].isOnScreen()) {
                cameraUtils.pitchCameraAsync(General.random(45, 110));
                Camera.turnToTile(bankChest[0].getPosition());
              } else {
                if (commonUtils.clickModel(bankChest[0].getModel(),
                    "Bank Chest")) {
                  sleep(1246, 1542);
                }
              }
            } else {
              if (Walking.walkPath(Walking.generateStraightPath(bankChest[0]))) {
                cameraUtils.pitchCameraAsync(General.random(45, 110));
                Camera.turnToTile(bankChest[0].getPosition());
                commonUtils.waitUntilIdle(400, 800);
              }
            }
          }
        } else if (bankLocation == 2) {
          RSObject[] bank = Objects.findNearest(20, AK_BANK_BOOTH_ID);
          if (bank != null && bank.length > 0) {
            cameraUtils.pitchCameraAsync(General.random(45, 110));
            Camera.turnToTile(bank[0].getPosition());
            if (commonUtils.clickModel(bank[0].getModel(), "Bank Bank booth")) {
              sleep(1246, 1542);
            }
          }
        }
      }
    }
  }
  private void travelToCows() {
    if (!atGate()) {
      cameraUtils.setCameraMovement(true);
      cameraUtils.rotateCameraAsync(Camera.getCameraRotation());
      sleep(125, 150);
      if (bankLocation == -1 || bankLocation == 0 || bankLocation == 1
          || (bankLocation == 2 && !inAlKharid())) {
        if (GATE_TILE_OUTSIDE.getY() + 3 > Player.getPosition().getY()) { // failsafe
          // if
          // north
          // of
          // the
          // gate
          if (Walking.walkPath(PATH_TO_COWS)) {
            cameraUtils.setCameraMovement(false);
            commonUtils.waitUntilIdle(750, 1000);
          }
        } else {
          if (Walking.walkPath(Walking.generateStraightPath(GATE_TILE_OUTSIDE))) {
            cameraUtils.setCameraMovement(false);
            commonUtils.waitForDestination();
          }
        }
      } else if (bankLocation == 2 && inAlKharid()) {
        if (!atAKGate()) {
          utils.walkPath(PATH_TO_AK_BANK, true);
        } else {
          passGate();
        }

      }
    } else {
      if (!isGateOpen()) {
        openGate();
      } else {
        Walking.walkTo(new RSTile(3259 + General.random(-1, 3), 3271 + General
            .random(-3, +4), 0));
        sleep(500, 750);
      }
    }
  }
  private void openGate() {
    int random = General.random(0, 1);
    RSTile gateTile;
    if (random == 0)
      gateTile = GATE_CLOSED_TILE;
    else
      gateTile = GATE_CLOSED_TILE2;

    RSObject gate = Doors.getDoorAt(gateTile);
    if (gate != null) {
      if (!gate.isOnScreen()) {
        cameraUtils.rotateCameraToTileAsync(gate.getPosition());
        sleep(250, 450);
      }
      if (gate.click("Open gate")) {
        commonUtils.waitUntilIdle(541, 751);
      }
    }
  }
  private void walkToStairs(int plane) {
    sleepwalkTo(stairsTile);
  }
  private void sleepwalkTo(RSTile tile) {
    RSTile dest = Game.getDestination();
    if (dest != null) {
      if (dest.distanceTo(tile) > 3) {
        Walking.walkPath(Walking.generateStraightPath(tile));
      }
    } else {
      if (Walking.walkPath(Walking.generateStraightPath(tile))) {
        cameraUtils.rotateCameraToTileAsync(stairsTile);
        cameraUtils.pitchCameraAsync(General.random(23, 110));
      }
    }

  }
  private void passGate() {
    RSObject[] gate = Objects.findNearest(20, AK_GATE_ID, AK_GATE_ID2);
    if (gate != null && gate.length > 0) {
      if (!gate[0].isOnScreen()) {
        cameraUtils.pitchCameraAsync(General.random(45, 75));
        Camera.turnToTile(gate[0].getPosition());
      } else {
        if (commonUtils.rightClickOnModel(gate[0].getModel(), "Pay-toll(10gp)")) {
          commonUtils.waitUntilIdle(1000, 1500);
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
        cameraUtils.pitchCameraAsync(General.random(22, 45));
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
          cameraUtils.pitchCameraAsync(General.random(65, 110));
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
      if (utils.rightClickOnModel(stairs.getModel(), uptext + " Staircase")) {
        if (plane == 0)
          commonUtils.waitUntilIdle(125, 175);
        else
          sleep(1251, 1567);
      }
    }

  }
  private void climbTrapdoor() {
    RSObject[] trapdoor = Objects.findNearest(20, TRAPDOOR_ID);
    if (trapdoor != null && trapdoor.length > 0) {
      cameraUtils.pitchCameraAsync(General.random(60, 100));
      if (!trapdoor[0].isOnScreen()) {
        Camera.turnToTile(stairsTile);
        sleep(100, 200);
      }
      if (commonUtils.clickModel(trapdoor[0].getModel(), "Climb-down Trapdoor")) {
        commonUtils.waitUntilIdle(1000, 2000);
      }
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

  private boolean isOriginalCombatStance() {
    return selectedCombatStyleIndex == Combat.getSelectedStyleIndex();
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
    return (Player.getPosition().getPlane() == 2
        && Player.getPosition().distanceTo(BANK_TILE) < 13 || inCellar() || inAlKharidBank());
  }
  private boolean inAlKharidBank() {
    return BANKAREA_AK.contains(Player.getPosition());
  }
  private boolean atCows() {
    return COW_PIT.contains(Player.getPosition());
  }
  private boolean isTeleportRandom(RANDOM_SOLVERS r) {
    return r == RANDOM_SOLVERS.APPENDAGE || r == RANDOM_SOLVERS.BEEKEEPER
        || r == RANDOM_SOLVERS.CAPTARNAV || r == RANDOM_SOLVERS.DRILLDEMON
        || r == RANDOM_SOLVERS.FREAKYFORESTER || r == RANDOM_SOLVERS.FROGCAVE
        || r == RANDOM_SOLVERS.GRAVEDIGGER || r == RANDOM_SOLVERS.MAZE
        || r == RANDOM_SOLVERS.MIME || r == RANDOM_SOLVERS.MOLLY
        || r == RANDOM_SOLVERS.MORDAUT || r == RANDOM_SOLVERS.PILLORY
        || r == RANDOM_SOLVERS.PINBALL || r == RANDOM_SOLVERS.PRISONPETE
        || r == RANDOM_SOLVERS.QUIZ || r == RANDOM_SOLVERS.SCAPERUNE;
  }

  private int getDistanceToTrapdoor() {
    return Player.getPosition().distanceTo(TRAPDOOR_TILE);
  }
  private int getTotalCombatExp() {
    return Skills.getXP(SKILLS.ATTACK) + Skills.getXP(SKILLS.STRENGTH)
        + Skills.getXP(SKILLS.DEFENCE) + Skills.getXP(SKILLS.HITPOINTS)
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
    RSGroundItem[] hides = GroundItems.findNearest(lootId);
    for (int i = 0; i < hides.length; i++) {
      if (COW_PIT.contains(hides[i].getPosition())) {
        return hides[i];
      }
    }
    return null;
  }
  private RSNPC findNearestTarget() {
    RSCharacter rsCharacter = Player.getRSPlayer().getInteractingCharacter();
    if (rsCharacter != null) {
      RSNPC target = (RSNPC) rsCharacter;
      if (target.getCombatLevel() == 2) {
        return target;
      }
    }

    RSNPC[] cows = NPCs.getAll();
    cows = NPCs.sortByDistance(Player.getPosition(), cows);
    for (int i = 0; i < cows.length; i++) {
      if (!cows[i].isInCombat() && cows[i].getCombatLevel() == 2
          && COW_PIT.contains(cows[i].getPosition())) {
        return cows[i];
      }
    }
    return null;
  }

  private enum State {
    TRAVELLING_TO_COWS, TRAVELLING_TO_BANK, KILLING_COWS, BANKING, SHITHOLE, HOPPING_WORLDS
  }

  private State getState() { // TODO BANKING ENUM
    if (Game.getCurrentWorld() != startingWorld) {
      return State.HOPPING_WORLDS;
    } else if (atCows()) {
      if (!Inventory.isFull())
        return State.KILLING_COWS;
      else
        return State.TRAVELLING_TO_BANK;
    } else if (atAnyBank()) {
      if ((Inventory.getAll().length <= 0 && (bankLocation == 0 || bankLocation == 1))
          || (bankLocation == 2 && Inventory.getCount(COINS_ID) >= 20 && !donePrinceAliQuest && Inventory.getCount(COWHIDE_ID) <= 0)
          || (bankLocation == 2 && donePrinceAliQuest && Inventory.getAll().length <= 0)
          || bankLocation == -1) {
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
    g.setColor(Color.WHITE);
    g.drawString("State: " + state, 370, 260);
    g.drawString("Running Time: " + runTimeString, 370, 275);
    g.drawString("Hides Gathered: " + hidesGathered, 370, 290);
    g.drawString("Hides per Hour: " + hidesPerHour, 370, 305);
    g.drawString("Exp gained: " + expGained, 370, 320);
    g.drawString("Exp per Hour: " + ">" + expPerHour + "k", 370, 335);
    // g.drawString("CurrentWorld: " + Game.getCurrentWorld(), 370, 365);
    // g.drawString("Camera rotation: " + Camera.getCameraRotation(), 370,
    // 20);
    // g.drawString("Camera angle: " + Camera.getCameraAngle(), 370, 35);
  }

  @Override
  public void onRandom(RANDOM_SOLVERS r) {
    if (isTeleportRandom(r))
      return;

    if (r == RANDOM_SOLVERS.SECURITYGUARD || r == RANDOM_SOLVERS.STRANGEPLANT)
      return;

    if (r == RANDOM_SOLVERS.COMBATRANDOM) {
      if (COW_PIT.contains(Player.getPosition()))
        runAway();
      return;
    }

    long k = System.currentTimeMillis();
    while (Player.getRSPlayer().isInCombat()) {
      if (Timing.timeFromMark(k) > 5000)
        break;
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
  public void onBreakStart() {
    while (Player.getRSPlayer().isInCombat()) {
      if (!utils.isAutoRetaliateIsEnabled()) {
        Combat.setAutoRetaliate(true);
      }
      sleep(100, 200);
    }

    long t = System.currentTimeMillis();
    while (System.currentTimeMillis() - t < General.random(10000, 11000)) {
      sleep(100, 200);
    }
  }
}