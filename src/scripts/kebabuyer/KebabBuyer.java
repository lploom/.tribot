package scripts.kebabuyer;

import java.awt.Color;
import java.awt.Graphics;

import org.tribot.api.General;
import org.tribot.api.input.Mouse;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Camera;
import org.tribot.api2007.Game;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.NPCChat;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.Objects;
import org.tribot.api2007.Player;
import org.tribot.api2007.Walking;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Painting;
import org.tribot.script.interfaces.RandomEvents;

import scripts.utils.CameraUtils;
import scripts.utils.CommonUtils;
import scripts.utils.KMUtils;
import scripts.utils.StaticUtils;

@ScriptManifest(
      authors = "karlrais && MrHat", 
      category = "MoneyMakingLOL", 
      name = "KebabBuyer", 
      description = "This script will buy kebabs in Al-Kharid. "
            + "Start in near Al-Kharid bank or kebab house. "
            + "It will withdraw random round amount of gold from the bank should you not have any or should you run out."
            + "It also runs away from Evil chicken and Swarm."
      )
public class KebabBuyer extends Script implements Painting, RandomEvents {

   private final static RSArea BANK_AREA = new RSArea(new RSTile[] {
         new RSTile(3268, 3174), new RSTile(3273, 3174),
         new RSTile(3273, 3161), new RSTile(3268, 3161) });
   private final static RSArea KEBAB_AREA = new RSArea(
         new RSTile[]{ 
               new RSTile(3271, 3179, 0), 
               new RSTile(3276, 3179, 0), 
               new RSTile(3276, 3183, 0), 
               new RSTile(3271, 3183, 0)
         });

   private final static RSTile CENTRE_TILE = new RSTile(3293, 3179);
   private final static RSTile BANK_TILE = new RSTile(3269, 3168);
   private final static RSTile KEBAB_TILE = new RSTile(3274, 3181);
   private final static RSTile DOOR_TILE = new RSTile(3275, 3180);
   private final static RSTile MIDDLE_TILE = new RSTile(3276, 3175);
   private final static RSTile CLOSED_DOOR_TILE = new RSTile(3275, 3180,0);
   private final static RSTile OPEN_DOOR_TILE = new RSTile(3276, 3180, 0);

   private static final int COINS_ID = 995;
   private final static int KEBAB_ID = 1971;

   private State state;
   private CameraUtils cameraUtils = new CameraUtils();
   private CommonUtils commonUtils = new CommonUtils(cameraUtils);
   private KMUtils utils = new KMUtils(cameraUtils, commonUtils);

   // paint variables
   private long runTime;
   private final long startTime = System.currentTimeMillis();
   private String runTimeString;
   private int kebabsBought;
   private int kebabsPerHour;

   @Override
   public void run() {
      Mouse.setSpeed(175);
      while (true) {
         calcPaint();
         state = getState();
         switch (state) {
            case BANKING:
               doBank();
               break;
            case BUYING:
               buyKebab();
               break;
            case MOVING_TO_BANK:
               travelToBank();
               break;
            case MOVING_TO_KEBAB:
               travelToKebab();
               break;
            case SHITHOLE:
               break;
         }
         sleep(50, 100);
      }
   }

   private void doBank() {
      if (!Banking.isBankScreenOpen()) {
         if(Inventory.open())
            sleep(300, 500);
         if(Banking.openBankBooth())
            commonUtils.waitUntilIdle(300, 500);
      } else {
         if(Banking.depositAllExcept(COINS_ID) > 0)
            sleep(300, 500);
         if (Inventory.getCount(COINS_ID) < General.random(50, 90)) {
            if(Banking.withdraw(1000, COINS_ID))
               sleep(300, 500);
         }
         Banking.close();
         sleep(300, 500);
      }
   }
   private void chatWithNPC() {
      while (NPCChat.getMessage() != null || NPCChat.getOptions() != null) {

         String message = NPCChat.getMessage();

         if (message != null && message != "") {
            if (message.contains("Yes please")) {
               if(NPCChat.clickContinue(false)) {
                  kebabsBought++;
               }
            }
            else {
               NPCChat.clickContinue(true);
               sleep(50, 100);
            }
         }

         String[] options = NPCChat.getOptions();
         if (options != null) {
            for(String s : NPCChat.getOptions()) {
               if(s.contains("Yes please")) {
                  NPCChat.selectOption(s, true);
               }
            }
         }

         sleep(50, 100);
      }
   }
   private void calcPaint() {
      runTime = System.currentTimeMillis() - startTime;
      runTimeString = StaticUtils.getDurationBreakdown(runTime);
      double d = runTime;
      if (d > 0) {
         kebabsPerHour = (int) (kebabsBought / (d / 1000 / 3600));
      }
   }
   private void buyKebab() {
      if(NPCChat.getMessage() == null && NPCChat.getOptions() == null) {
         RSNPC karim = findKarim();
         if (karim != null) {
            if (Player.getRSPlayer().getInteractingCharacter() == null || !KEBAB_AREA.contains(Player.getPosition())) {
               utils.clickNPC(karim.getModel(), "Talk-to");
               sleep(350, 500);
            }
         }
      } else {
         chatWithNPC();
      }
   }
   private void travelToBank() {
      if (inKebabHouse() && !isDoorOpen()) {
         openDoor();
      }
      else if (!Player.isMoving()) {
         if (Player.getPosition().distanceTo(BANK_TILE) > 13) {
            Walking.walkPath(Walking.generateStraightPath(BANK_TILE));
         }
         if (Walking.walkTo(BANK_TILE)) {
            cameraUtils.rotateCameraAsync(General.random(0, 359));
            Camera.setCameraAngle(General.random(33, 100));
            sleep(2000, 2500);
            if (Camera.getCameraRotation() < 40 || Camera.getCameraRotation() > 130) {
               cameraUtils.rotateCameraAsync(General.random(50, 120));
               cameraUtils.pitchCameraAsync(General.random(50, 100));
            }
         }
      }
   }
   private void travelToKebab() {
      if (inBank()) {
         if (!Player.isMoving()) {
            General.random(0, 4);
            if (General.random(0, 4) == 4) {
               utils.toggleRun(true);
            }
            Walking.walkTo(MIDDLE_TILE);
            sleep(1000, 1500);
            cameraUtils.pitchCameraAsync(General.random(50, 100));
            Camera.setCameraRotation(General.random(0, 359));
            if (Camera.getCameraRotation() < 40
                  || Camera.getCameraRotation() > 130) {
               cameraUtils.pitchCameraAsync(General.random(50, 100));
               Camera.setCameraRotation(General.random(50, 120));
            }
         }
      }
      else if (!inKebabHouse()) {
         if (isDoorOpen()) {
            Walking.walkTo(KEBAB_TILE);
            RSTile dest = Game.getDestination();
            if (dest != null && !KEBAB_AREA.contains(dest)) {
               return;
            }
            sleep(2000, 3000);
         }
         else {
            if (nearDoor()) {
               openDoor();
            }
            else {
               Walking.walkTo(CLOSED_DOOR_TILE);
               sleep(2000, 3000);
            }
         }
      }
   }
   private void openDoor() {
      RSObject[] door = Objects.getAt(CLOSED_DOOR_TILE);
      if (door != null && door.length > 0) {
         if (!door[0].isOnScreen()) {
            cameraUtils.pitchCameraAsync(General.random(22, 110));
            Camera.turnToTile(CLOSED_DOOR_TILE);
         }
         if (utils.clickModelExact(door[0].getModel(), "Open Door")) {
            commonUtils.waitUntilIdle(221, 341);
         }
      }
   }

   private boolean isDoorOpen() {
      RSObject[] door = Objects.getAt(OPEN_DOOR_TILE);
      return door != null && door.length > 0;
   }
   private boolean nearDoor() {
      return Player.getPosition().distanceTo(DOOR_TILE) < 5;
   }
   private boolean inAlkharid() {
      return Player.getPosition().distanceTo(CENTRE_TILE) < 40;
   }
   private boolean inKebabHouse() {
      return KEBAB_AREA.contains(Player.getPosition());
   }
   private boolean inBank() {
      return BANK_AREA.contains(Player.getPosition());
   }

   private RSNPC findKarim() {
      RSNPC[] npcs = NPCs.getAll();
      if(npcs != null && npcs.length > 0) {
         for(RSNPC npc : npcs) {
            if(npc.getModel() != null) {
               if(npc.getModel().getTriangles().length == 530) {
                  return npc;
               }
            }
         }
      }
      return null;
   }
   
   private State getState() {
      if (inBank()) {
         if(Inventory.getCount(KEBAB_ID) == 0 && Inventory.getCount(COINS_ID) > 99)
            return State.MOVING_TO_KEBAB;
         else
            return State.BANKING;
      } else if (inKebabHouse()) {
         if(Inventory.isFull() || Inventory.getCount(COINS_ID) <= 0) 
            return State.MOVING_TO_BANK;
         else 
            return State.BUYING;
      } else if (inAlkharid()) {
         if(Inventory.getCount(KEBAB_ID) > 0 || Inventory.getCount(COINS_ID) <= 0)
            return State.MOVING_TO_BANK;
         else 
            return State.MOVING_TO_KEBAB;
      } else {
         return State.SHITHOLE;
      }
   }
   private enum State {
      MOVING_TO_KEBAB, MOVING_TO_BANK, BANKING, BUYING, SHITHOLE
   }

   @Override
   public void onPaint(Graphics g) {
      g.setColor(Color.WHITE);
      g.drawString("State: " + state, 370, 290);
      g.drawString("Running Time: " + runTimeString, 370, 305);
      g.drawString("Kebabs Bought: " + kebabsBought, 370, 320);
      g.drawString("Kebabs per Hour: " + kebabsPerHour, 370, 335);
   }

   @Override
   public void onRandom(RANDOM_SOLVERS random) {
   }

   @Override
   public boolean randomFailed(RANDOM_SOLVERS random) {
      return true;
   }

   @Override
   public void randomSolved(RANDOM_SOLVERS random) {

   }
}
