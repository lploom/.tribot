package scripts.utils;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Mouse;
import org.tribot.api2007.Camera;
import org.tribot.api2007.ChooseOption;
import org.tribot.api2007.Game;
import org.tribot.api2007.GameTab;
import org.tribot.api2007.GameTab.TABS;
import org.tribot.api2007.GroundItems;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.NPCChat;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.Objects;
import org.tribot.api2007.Options;
import org.tribot.api2007.Player;
import org.tribot.api2007.Screen;
import org.tribot.api2007.Skills;
import org.tribot.api2007.Skills.SKILLS;
import org.tribot.api2007.Walking;
import org.tribot.api2007.types.RSGroundItem;
import org.tribot.api2007.types.RSInterface;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSItemDefinition;
import org.tribot.api2007.types.RSModel;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;

import scripts.utils.KMUtils.Equipment.EQUIPMENT;

public class KMUtils {

   private static final int LUMBY_BOOTH_IDS = 18491;
   private static final int EDGE_BANK_BOOTH_ID = 2196;

   private CameraUtils cameraUtils;
   private CommonUtils commonUtils;

   public KMUtils(CameraUtils cameraUtils, CommonUtils commonUtils) {
      this.commonUtils = commonUtils;
      this.cameraUtils = cameraUtils;
   }

   public int[] getAllEquipmentIds() {  
      ArrayList<Integer> eq = new ArrayList<>(11);

      eq.add(Equipment.getEquipment(EQUIPMENT.HELM));
      eq.add(Equipment.getEquipment(EQUIPMENT.CAPE));
      eq.add(Equipment.getEquipment(EQUIPMENT.NECK));
      eq.add(Equipment.getEquipment(EQUIPMENT.ARROW));
      eq.add(Equipment.getEquipment(EQUIPMENT.WEAPON));
      eq.add(Equipment.getEquipment(EQUIPMENT.BODY));
      eq.add(Equipment.getEquipment(EQUIPMENT.SHIELD));
      eq.add(Equipment.getEquipment(EQUIPMENT.LEGS));
      eq.add(Equipment.getEquipment(EQUIPMENT.GLOVES));
      eq.add(Equipment.getEquipment(EQUIPMENT.BOOTS));
      eq.add(Equipment.getEquipment(EQUIPMENT.RING));

      int[] answer = new int[eq.size()];
      for (int i = 0; i < eq.size(); i++) {
         answer[i] = eq.get(i).intValue();
      }

      return answer;
   }

   public int getEdgeBankBoothID() {
      return EDGE_BANK_BOOTH_ID;
   }

   public int getLumbyBankBoothID() {
      return LUMBY_BOOTH_IDS;
   }

   public boolean isCharacterHealthy(int offset) {
      return Skills.getCurrentLevel(SKILLS.HITPOINTS) + offset >= Skills.getActualLevel(SKILLS.HITPOINTS);
   }

   public RSModel getNearestModel(int id, int maxDistance) {
      RSObject[] objects = Objects.findNearest(maxDistance, id);
      if (objects != null && objects.length > 0) {
         return objects[0].getModel();
      }
      return null;
   }

   public RSModel getNearestModel(String name, int maxDistance) {
      RSObject[] objects = Objects.findNearest(maxDistance, name);
      if (objects != null && objects.length > 0) {
         return objects[0].getModel();
      }
      return null;
   }

   public RSModel getNearestModel(String[] names, int maxDistance) {
      RSObject[] objects = Objects.findNearest(maxDistance, names);
      if (objects != null && objects.length > 0) {
         return objects[0].getModel();
      }
      return null;
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

   public boolean clickModelExact(RSModel model, String upText) {
      if (model != null) {
         Point[] ps = model.getAllVisiblePoints();
         if (ps.length > 0) {
            Point p = average(ps);
            Mouse.move(p);
            if (!cameraUtils.isCameraPitching() && !cameraUtils.isCameraRotating()
                  && Timing.waitUptext(upText, General.random(250, 350))) {
               Mouse.click(1);
               return true;

            }
            else {
               Mouse.click(3);
               long t = System.currentTimeMillis();
               while (!ChooseOption.isOpen()) {
                  commonUtils.sleep(250, 350);
                  if (Timing.timeFromMark(t) >= General.random(5000, 7000))
                     break;
               }
               return ChooseOption.select(upText);
            }
         }
      }
      return false;
   }

   public boolean leftClickModel(RSModel model, String upText) {
      if (model != null) {
         Point[] ps = model.getAllVisiblePoints();
         if (ps.length > 0) {
            Point p = average(ps);
            p.setLocation(p.x + General.random(-7, 7),
                  p.y + General.random(-7, 7));
            Mouse.move(p);
            if (Timing.waitUptext(upText, General.random(500, 600))) {
               Mouse.click(1);
               commonUtils.sleep(50, 75);
               return Screen.getColorAt(Mouse.getPos()).equals(Color.RED);
            }
         }
      }
      return false;
   }

   public boolean leftClickModelExactInstant(RSModel model) {
      if (model != null) {
         Point[] ps = model.getAllVisiblePoints();
         if (ps.length > 0) {
            Point p = average(ps);
            Mouse.move(p);
            Mouse.click(1);
            return Screen.getColorAt(Mouse.getPos()).equals(Color.RED);
         }
      }
      return false;
   }

   public boolean leftClickModelExact(RSModel model, String upText) {
      if (model != null) {
         Point[] ps = model.getAllVisiblePoints();
         if (ps.length > 0) {
            Point p = average(ps);
            Mouse.move(p);
            if (Timing.waitUptext(upText, General.random(500, 600))) {
               Mouse.click(1);
               commonUtils.sleep(50, 75);
               return Screen.getColorAt(Mouse.getPos()).equals(Color.RED);
            }
         }
      }
      return false;
   }

   public boolean rightClickOnModel(RSModel model, String upText) {
      if (model != null) {
         Point[] ps = model.getAllVisiblePoints();
         if (ps.length > 0) {
            Point p = average(ps);
            p.setLocation(p.x + General.random(-5, 5), p.y + General.random(-5, 5));
            Mouse.move(p);
            Mouse.click(3);
            long t = System.currentTimeMillis();
            while (!ChooseOption.isOpen()) {
               commonUtils.sleep(100, 250);
               if (Timing.timeFromMark(t) >= General.random(5000, 7000))
                  break;
            }
            return ChooseOption.select(upText);
         }
      }
      return false;
   }

   public boolean clickNPC(RSModel model, String upText) {
      if (model != null) {
         Point[] ps = model.getAllVisiblePoints();
         if (ps.length > 0) {
            Point p = average(ps);
            p.setLocation(p.x + General.random(-3, 3),
                  p.y + General.random(-3, 3));
            Mouse.hop(p);
            if (!cameraUtils.isCameraPitching() && !cameraUtils.isCameraRotating()
                  && Timing.waitUptext(upText, General.random(250, 350))) {
               Mouse.click(1);
               commonUtils.sleep(250, 350);
               return Player.getRSPlayer().getInteractingCharacter() != null;
            }
         }
         else {
            Mouse.click(3);
            long t = System.currentTimeMillis();
            while (!ChooseOption.isOpen()) {
               commonUtils.sleep(100, 250);
               if (Timing.timeFromMark(t) >= General.random(5000, 7000))
                  break;
            }
            return ChooseOption.select(upText);
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

   public boolean isItemEquipped(int itemID) {
      int[] eq = getAllEquipmentIds();
      if (eq != null && eq.length > 0) {
         for (int i = 0; i < eq.length; i++) {
            if (eq[i] == itemID) {
               return true;
            }
         }
      }
      return false;
   }

   public RSObject getNearestObject(int[] ids, int dist) {
      RSObject[] objects = Objects.find(dist, ids);
      if (objects != null && objects.length > 0) {
         return objects[0];
      }
      return null;
   }

   public RSObject getNearestObject(String name, int dist) {
      RSObject[] objects = Objects.findNearest(dist, name);
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
         if (Player.getPosition().distanceToDouble(tile) <= General
               .random(1, 3)) {
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

   public void moveMouseRandom(int min, int max) {
      int prevSpeed = Mouse.getSpeed();
      Mouse.setSpeed(200);
      Mouse.move(General.random(min, max), General.random(min, max));
      Mouse.setSpeed(prevSpeed);
   }

   public boolean pickUpLoot(RSGroundItem loot) {
      if(loot == null) return false;

      RSTile lootTile = loot.getPosition();
      if(lootTile == null) return false;
      
      if (Player.getPosition().distanceTo(lootTile) > 7) {
         if (!cameraUtils.isCameraPitching() && !cameraUtils.isCameraRotating()) {
            Walking.walkTo(lootTile);
         }
         commonUtils.sleep(150, 300);
         cameraUtils.pitchCameraAsync(General.random(33, 45));
         cameraUtils.rotateCameraToTileAsync(lootTile);
         commonUtils.sleep(500, 750);
      }
      else if (!loot.isOnScreen()) {
         cameraUtils.pitchCameraAsync(General.random(33, 45));
         Camera.turnToTile(lootTile);
      }
      else {
         RSItemDefinition itemDefinition = loot.getDefinition();
         if(itemDefinition == null) return false;
         
         int lootID = loot.getID();
         int prevLootCount = Inventory.getCount(lootID);
         
         if (takeGroundItem(loot, "Take " + itemDefinition.getName())) {
            long k = System.currentTimeMillis();
            while (Inventory.getCount(lootID) <= prevLootCount) {
               if(Inventory.isFull() || Timing.timeFromMark(k) > 4000) 
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
      if (GameTab.getOpen() != TABS.MAGIC)
         GameTab.open(TABS.MAGIC);
      commonUtils.sleep(500, 600);
      RSInterface homeTeleport = Interfaces.get(192, 0);
      commonUtils.sleep(1000, 1750);
      if (homeTeleport != null) {
         if (homeTeleport.click("Cast")) {
            commonUtils.sleep(13000, 16000);
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

   public RSGroundItem findNearestLoot(int LOOT_ID) {
      RSGroundItem[] hides = GroundItems.findNearest(LOOT_ID);
      if (hides != null && hides.length > 0) {
         return hides[0];
      }
      return null;
   }

   public RSNPC findNearestTarget(String[] TARGET_NAMES) {
      RSNPC[] targets = NPCs.findNearest(TARGET_NAMES);
      if (targets != null && targets.length > 0) {
         for (int i = 0; i < targets.length; i++) {
            if (!targets[i].isInCombat()) {
               return targets[i];
            }
         }
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

   public boolean useItemOnOtherItem(RSItem firstItem, RSItem secondItem,
         String upText) {
      if (GameTab.getOpen() != TABS.INVENTORY) {
         Inventory.open();
      }
      if (firstItem.click("Use")) {
         if (secondItem.click(upText)) {
            return true;
         }
      }
      return false;
   }

   public boolean useItemOnEnvironment(RSItem item, RSObject obj, String upText) {
      if (GameTab.getOpen() != TABS.INVENTORY) {
         Inventory.open();
      }
      if (item.click("Use")) {
         if (clickModelExact(obj.getModel(), upText)) {
            return true;
         }
      }
      return false;
   }

   public boolean openBankBooth(String boothName) {
      RSObject[] booths = Objects.findNearest(20, "Bank booth");
      if (booths != null && booths.length > 0) {
         if (booths[0].click("Bank bank booth")) {
            return true;
         }
      }
      return false;
   }

   public boolean openBankBooth(int bankBoothId) {
      RSObject[] booths = Objects.findNearest(20, bankBoothId);
      if (booths != null && booths.length > 0) {
         if (booths[0].click("Bank bank booth")) {
            commonUtils.waitUntilIdle(312, 512);
            return true;
         }
      }
      return false;
   }

   public boolean operateNeck() {
      int neckID = KMUtils.Equipment
            .getEquipment(KMUtils.Equipment.EQUIPMENT.NECK);
      if (neckID == -1)
         return false;
      if (!GameTab.getOpen().equals(TABS.EQUIPMENT)) {
         GameTab.open(TABS.EQUIPMENT);
         commonUtils.sleep(150, 200);
      }
      if (Interfaces.get(387, 28) != null) {
         for (RSItem i : Interfaces.get(387, 28).getItems()) {
            if (i.getID() == neckID) {
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

   public boolean unequipHelm(int... items) {
      int helmID = KMUtils.Equipment.getEquipment(EQUIPMENT.HELM);
      if (helmID == -1)
         return false;
      for (int i : items) {
         if (i == helmID)
            return false;
      }
      if (!GameTab.getOpen().equals(TABS.EQUIPMENT)) {
         GameTab.open(TABS.EQUIPMENT);
         commonUtils.sleep(150, 200);
      }
      if (Interfaces.get(387, 28) != null) {
         for (RSItem i : Interfaces.get(387, 28).getItems()) {
            if (i.getID() == helmID) {
               Mouse.clickBox(633, 216, 653, 235, 1);
               return true;
            }
         }
      }
      return false;
   }

   public boolean unequipCape(int... items) {
      int capeID = KMUtils.Equipment.getEquipment(EQUIPMENT.CAPE);
      if (capeID == -1)
         return false;
      for (int i : items) {
         if (i == capeID)
            return false;
      }
      if (!GameTab.getOpen().equals(TABS.EQUIPMENT)) {
         GameTab.open(TABS.EQUIPMENT);
         commonUtils.sleep(150, 200);
      }
      if (Interfaces.get(387, 28) != null) {
         for (RSItem i : Interfaces.get(387, 28).getItems()) {
            if (i.getID() == capeID) {
               Mouse.clickBox(592, 256, 611, 273, 1);
               return true;
            }
         }
      }
      return false;
   }

   public boolean unequipNeck(int... items) {
      int neckID = KMUtils.Equipment.getEquipment(EQUIPMENT.NECK);
      if (neckID == -1)
         return false;
      for (int i : items) {
         if (i == neckID)
            return false;
      }
      if (!GameTab.getOpen().equals(TABS.EQUIPMENT)) {
         GameTab.open(TABS.EQUIPMENT);
         commonUtils.sleep(150, 200);
      }
      if (Interfaces.get(387, 28) != null) {
         for (RSItem i : Interfaces.get(387, 28).getItems()) {
            if (i.getID() == neckID) {
               Mouse.clickBox(636, 255, 653, 273, 1);
               return true;
            }
         }
      }
      return false;
   }

   public boolean unequipArrows(int... items) {
      int arrowID = KMUtils.Equipment.getEquipment(EQUIPMENT.ARROW);
      if (arrowID == -1)
         return false;
      for (int i : items) {
         if (i == arrowID)
            return false;
      }
      if (!GameTab.getOpen().equals(TABS.EQUIPMENT)) {
         GameTab.open(TABS.EQUIPMENT);
         commonUtils.sleep(150, 200);
      }
      if (Interfaces.get(387, 28) != null) {
         for (RSItem i : Interfaces.get(387, 28).getItems()) {
            if (i.getID() == arrowID) {
               Mouse.clickBox(674, 255, 694, 273, 1);
               return true;
            }
         }
      }
      return false;
   }

   public boolean unequipWeapon(int... items) {
      int weaponID = KMUtils.Equipment.getEquipment(EQUIPMENT.WEAPON);
      if (weaponID == -1)
         return false;
      for (int i : items) {
         if (i == weaponID)
            return false;
      }
      if (!GameTab.getOpen().equals(TABS.EQUIPMENT)) {
         GameTab.open(TABS.EQUIPMENT);
         commonUtils.sleep(150, 200);
      }
      if (Interfaces.get(387, 28) != null) {
         for (RSItem i : Interfaces.get(387, 28).getItems()) {
            if (i.getID() == weaponID) {
               Mouse.clickBox(580, 295, 595, 312, 1);
               return true;
            }
         }
      }
      return false;
   }

   public boolean unequipBody(int... items) {
      int bodyID = KMUtils.Equipment.getEquipment(EQUIPMENT.BODY);
      if (bodyID == -1)
         return false;
      for (int i : items) {
         if (i == bodyID)
            return false;
      }
      if (!GameTab.getOpen().equals(TABS.EQUIPMENT)) {
         GameTab.open(TABS.EQUIPMENT);
         commonUtils.sleep(150, 200);
      }
      if (Interfaces.get(387, 28) != null) {
         for (RSItem i : Interfaces.get(387, 28).getItems()) {
            if (i.getID() == bodyID) {
               Mouse.clickBox(634, 297, 652, 313, 1);
               return true;
            }
         }
      }
      return false;
   }

   public boolean unequipShield(int... items) {
      int sheildID = KMUtils.Equipment.getEquipment(EQUIPMENT.SHIELD);
      if (sheildID == -1)
         return false;
      for (int i : items) {
         if (i == sheildID)
            return false;
      }
      if (!GameTab.getOpen().equals(TABS.EQUIPMENT)) {
         GameTab.open(TABS.EQUIPMENT);
         commonUtils.sleep(150, 200);
      }
      if (Interfaces.get(387, 28) != null) {
         for (RSItem i : Interfaces.get(387, 28).getItems()) {
            if (i.getID() == sheildID) {
               Mouse.clickBox(689, 295, 710, 314, 1);
               return true;
            }
         }
      }
      return false;
   }

   public boolean unequipLegs(int... items) {
      int legsID = KMUtils.Equipment.getEquipment(EQUIPMENT.LEGS);
      if (legsID == -1)
         return false;
      for (int i : items) {
         if (i == legsID)
            return false;
      }
      if (!GameTab.getOpen().equals(TABS.EQUIPMENT)) {
         GameTab.open(TABS.EQUIPMENT);
         commonUtils.sleep(150, 200);
      }
      if (Interfaces.get(387, 28) != null) {
         for (RSItem i : Interfaces.get(387, 28).getItems()) {
            if (i.getID() == legsID) {
               Mouse.clickBox(630, 335, 652, 353, 1);
               return true;
            }
         }
      }
      return false;
   }

   public boolean unequipGloves(int... items) {
      int glovesID = KMUtils.Equipment.getEquipment(EQUIPMENT.GLOVES);
      if (glovesID == -1)
         return false;
      for (int i : items) {
         if (i == glovesID)
            return false;
      }
      if (!GameTab.getOpen().equals(TABS.EQUIPMENT)) {
         GameTab.open(TABS.EQUIPMENT);
         commonUtils.sleep(150, 200);
      }
      if (Interfaces.get(387, 28) != null) {
         for (RSItem i : Interfaces.get(387, 28).getItems()) {
            if (i.getID() == glovesID) {
               Mouse.clickBox(579, 375, 597, 394, 1);
               return true;
            }
         }
      }
      return false;
   }

   public boolean unequipBoots(int... items) {
      int bootsID = KMUtils.Equipment.getEquipment(EQUIPMENT.BOOTS);
      if (bootsID == -1)
         return false;
      for (int i : items) {
         if (i == bootsID)
            return false;
      }
      if (!GameTab.getOpen().equals(TABS.EQUIPMENT)) {
         GameTab.open(TABS.EQUIPMENT);
         commonUtils.sleep(150, 200);
      }
      if (Interfaces.get(387, 28) != null) {
         for (RSItem i : Interfaces.get(387, 28).getItems()) {
            if (i.getID() == bootsID) {
               Mouse.clickBox(634, 374, 651, 395, 1);
               return true;
            }
         }
      }
      return false;
   }

   public boolean unequipRing(int... items) {
      int ringID = KMUtils.Equipment.getEquipment(EQUIPMENT.RING);
      if (ringID == -1)
         return false;
      for (int i : items) {
         if (i == ringID)
            return false;
      }
      if (!GameTab.getOpen().equals(TABS.EQUIPMENT)) {
         GameTab.open(TABS.EQUIPMENT);
         commonUtils.sleep(150, 200);
      }
      if (Interfaces.get(387, 28) != null) {
         for (RSItem i : Interfaces.get(387, 28).getItems()) {
            if (i.getID() == ringID) {
               Mouse.clickBox(690, 374, 709, 390, 1);
               return true;
            }
         }
      }
      return false;
   }

   public void unequipAllExcept(int... items) {
      unequipHelm(items);
      unequipCape(items);
      unequipNeck(items);
      unequipArrows(items);
      unequipWeapon(items);
      unequipBody(items);
      unequipShield(items);
      unequipLegs(items);
      unequipGloves(items);
      unequipBoots(items);
      unequipRing(items);
   }

   public void unequipAll() {
      unequipHelm(0);
      unequipCape(0);
      unequipNeck(0);
      unequipArrows(0);
      unequipWeapon(0);
      unequipBody(0);
      unequipShield(0);
      unequipLegs(0);
      unequipGloves(0);
      unequipBoots(0);
      unequipRing(0);
   }

   public void dropJunk(int[] JUNK) {
      if (Inventory.getCount(JUNK) > 0) {
         if (!GameTab.getOpen().equals(TABS.INVENTORY)) {
            GameTab.open(TABS.INVENTORY);
            commonUtils.sleep(500, 600);
         }
         Inventory.drop(JUNK);
      }
   }

   public static class Equipment {
      public enum EQUIPMENT {
         helm(0), HELM(0), cape(1), CAPE(1), neck(2), NECK(2), weapon(3), WEAPON(
               3), body(4), BODY(4), shield(5), SHIELD(5), legs(7), LEGS(7), gloves(
                     9), GLOVES(9), boots(10), BOOTS(10), ring(12), RING(12), arrow(
                           13), ARROW(13);
         private int value;

         private EQUIPMENT(int value) {
            this.value = value;
         }
      };

      public static int getEquipment(EQUIPMENT equip) {
         if (Interfaces.get(387, 28) != null) {
            for (RSItem i : Interfaces.get(387, 28).getItems()) {
               if (i.getIndex() == equip.value) {
                  return i.getID();
               }
            }
         }
         return -1;
      }

      public static int getArrowStack() {
         if (Interfaces.get(387, 28) != null) {
            for (RSItem i : Interfaces.get(387, 28).getItems()) {
               if (i.getIndex() == 13) {
                  return i.getStack();
               }
            }
         }
         return -1;
      }
   }

   private boolean takeGroundItem(RSGroundItem item, String upText) {
      if(item == null) return false;

      RSTile pos = item.getPosition();
      if (pos == null) return false;

      String currentUptext = Game.getUptext();
      if(currentUptext != null && upText.contains("->")) {
         item.click();
      }

      return DynamicClicking.clickRSTile(pos, upText);
   }

   public void openLumbyBank() {
      RSObject[] bankBooths = Objects.findNearest(20, LUMBY_BOOTH_IDS);
      if (bankBooths != null && bankBooths.length > 0) {
         if (commonUtils.clickModel(bankBooths[0].getModel(), "Bank Bank booth")) {
            commonUtils.waitUntilIdle(300, 750);
         }
      }
   }


   public void setCameraAngle(int i) {
      Camera.setCameraAngle(i);

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
         } catch (InterruptedException e) {}
      }
   }
}