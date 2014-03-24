package scripts.utils;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;

import org.tribot.api.General;
import org.tribot.api.Screen;
import org.tribot.api.input.Mouse;
import org.tribot.api2007.Combat;
import org.tribot.api2007.Game;
import org.tribot.api2007.GameTab;
import org.tribot.api2007.Login;
import org.tribot.api2007.Login.STATE;

/**
 * @author TehRhio
 * @description Hops to the selected world
 */
public class WorldHopper {

   private static final int[] WORLDS = { 1, 2, 3, 4, 5, 6, 8, 9, 10, 11, 12,
      13, 14, 16, 17, 18, 19, 20, 21, 22, 25, 26, 27, 28, 29, 30, 33, 34,
      35, 36, 37, 38, 41, 42, 43, 44, 45, 46, 49, 50, 51, 52, 53, 54, 57,
      58, 59, 60, 61, 62, 65, 66, 67, 68, 69, 70, 73, 74, 75, 76, 77, 78 };
   private static final int[] FREE_WORLDS = { 8, 16 };
   private static final int[] PVP_WORLDS = { 25, 37 };
   private static final int[] POPULAR_WORLDS = { 1 };

   private static final Color LOGIN_SCREEN = new Color(255, 255, 0);
   private static final Color WORLD_SELECT = new Color(0, 0, 0);
   private static final Color NEW_USER = new Color(255, 255, 255);

   private static boolean quickLogout = false;
   private static boolean excludeFree = true;
   private static boolean excludePVP = true;
   private static boolean excludePopular = true;

   public static boolean hop() {
      // Hops to a random world
      if (!logout()) {
         return false;
      }
      openWorldSelect();
      int world = 0;
      // Make sure world is valid
      while (!isValid(world) || excludeFree && isFree(world) || excludePVP
            && isPVP(world) || excludePopular && isPopular(world)) {
         world = WORLDS[General.random(0, 61)];
      }
      long time = System.currentTimeMillis();
      while (System.currentTimeMillis() - 15000 < time
            && getCurrentWorld() != world) {
         if (!isOnWorldSelect()) {
            openWorldSelect();
         } else {
            selectWorld(world);
         }
      }
      openLoginInfo();
      return login() && getCurrentWorld() == world;
   }
   public static boolean hop(int[] exclusions) {
      // Hops to a random world excluding exclusions
      if (!logout()) {
         return false;
      }
      openWorldSelect();
      int world = 0;
      // Make sure world is valid
      while (!isValid(world) || excludeFree && isFree(world) || excludePVP
            && isPVP(world) || excludePopular && isPopular(world)
            || isExclusion(exclusions, world)) {
         world = WORLDS[General.random(0, 61)];
      }
      long time = System.currentTimeMillis();
      while (System.currentTimeMillis() - 15000 < time
            && getCurrentWorld() != world) {
         if (!isOnWorldSelect()) {
            openWorldSelect();
         } else {
            selectWorld(world);
         }
      }
      openLoginInfo();
      return login() && getCurrentWorld() == world;
   }
   public static boolean hop(int world) {
      // Hop to a specific world
      if (!logout()) {
         return false;
      }
      openWorldSelect();
//      while (!isValid(world) || excludeFree && isFree(world) || excludePVP
//            && isPVP(world) || excludePopular && isPopular(world)) {
//         world = WORLDS[General.random(0, 61)];
//      }
      long time = System.currentTimeMillis();
      while (System.currentTimeMillis() - 15000 < time
            && getCurrentWorld() != world) {
         if (!isOnWorldSelect()) {
            openWorldSelect();
         } else {
            selectWorld(world);
         }
      }
      openLoginInfo();
      return login() && getCurrentWorld() == world;
   }

   private static boolean logout() {
      while (Login.getLoginState() == Login.STATE.INGAME
            && !Combat.isUnderAttack()) {
         if (quickLogout) {
            if (GameTab.getOpen() != GameTab.TABS.LOGOUT) {
               Mouse.hop(new Point(639, 470));
               Mouse.click(1);
            }
            Mouse.hop(new Point(639, 373));
            Mouse.click(1);
         } else if (Login.logout()) {
            long time = System.currentTimeMillis();
            while ((System.currentTimeMillis() - 1500L < time)
                  && (!isOnLoginscreen())) {
               General.sleep(250L);
            }
         }
      }
      return isOnLoginscreen();
   }
   private static boolean selectWorld(int world) {
      // Selects the world provided
      Rectangle r = getWorldRectangle(world);
      while (isOnWorldSelect()) {
         Mouse.clickBox((int) r.getX(), (int) r.getY(),
               (int) (r.getX() + r.getWidth()),
               (int) (r.getY() + r.getHeight()), 1);
         General.sleep(500, 750);
      }
      return !isOnWorldSelect();
   }
   private static boolean openWorldSelect() {
      while (!isOnWorldSelect()) {
         Mouse.clickBox(12, 466, 98, 489, 1);
         General.sleep(500, 750);
      }
      return isOnWorldSelect();
   }
   private static void openLoginInfo() {
      while (!isOnWorldSelect() && !isOnLoginInfo()
            && Login.getLoginState() != Login.STATE.INGAME) {
         if (!isOnWarningScreen() && !isOnNewUser()) {
            Mouse.moveBox(400, 275, 505, 300);
            General.sleep(50, 100);
            Mouse.click(1);
         } else if (isOnWarningScreen()) {
            Mouse.moveBox(245, 308, 360, 335);
            General.sleep(50, 100);
            Mouse.click(1);
         } else {
            Mouse.moveBox(329, 310, 443, 330);
            General.sleep(50, 100);
            Mouse.click(1);
         }
         General.sleep(300);
      }
   }
   public static boolean login() {
      while (Login.getLoginState() != STATE.INGAME) {
         Login.login();
         General.sleep(500, 750);
      }
      return Login.getLoginState() == STATE.INGAME;
   }

   public static String getState() {
      // Return the current login state
      if (Login.getLoginState() == STATE.INGAME)
         return "Ingame";
      if (Login.getLoginState() == STATE.WELCOMESCREEN)
         return "Welcome Screen";
      if (isOnLoginscreen())
         return "Login Screen";
      if (isOnWorldSelect())
         return "World Select";
      if (isOnWarningScreen())
         return "Warning Screen";
      if (isOnLoginInfo())
         return "Login Info";
      if (isOnNewUser())
         return "New User";
      return "Unknown";
   }
   private static boolean isOnLoginscreen() {
      return colorsMatch(Screen.getColourAt(386, 250), LOGIN_SCREEN);
   }
   private static boolean isOnNewUser() {
      return colorsMatch(Screen.getColourAt(383, 323), NEW_USER);
   }
   private static boolean isOnWorldSelect() {
      return (colorsMatch(Screen.getColourAt(100, 100), WORLD_SELECT))
            && (colorsMatch(Screen.getColourAt(600, 450), WORLD_SELECT))
            && (colorsMatch(Screen.getColourAt(600, 50), WORLD_SELECT))
            && (colorsMatch(Screen.getColourAt(100, 450), WORLD_SELECT));
   }
   private static boolean isOnLoginInfo() {
      return colorsMatch(Screen.getColourAt(275, 280), new Color(255, 255, 255));
   }
   private static boolean isOnWarningScreen() {
      return colorsMatch(Screen.getColourAt(408, 201), new Color(255, 255, 0));
   }

   private static boolean isValid(int world) {
      for (int i : WORLDS) {
         if (world == i) {
            return true;
         }
      }
      return false;
   }
   private static boolean isFree(int world) {
      for (int i : FREE_WORLDS) {
         if (world == i) {
            return true;
         }
      }
      return false;
   }
   private static boolean isPVP(int world) {
      for (int i : PVP_WORLDS) {
         if (world == i) {
            return true;
         }
      }
      return false;
   }
   private static boolean isPopular(int world) {
      for (int i : POPULAR_WORLDS) {
         if (world == i) {
            return true;
         }
      }
      return false;
   }
   private static boolean isExclusion(int[] exclusions, int world) {
      for (int i : exclusions) {
         if (i == world) {
            return true;
         }
      }
      return false;
   }
   private static Rectangle getRectangle(int index) {
      int x = ((int) ((Math.floor(index) / 16) % 4) * 93) + 205;
      int y = ((int) (Math.ceil(index) % 16) * 24) + 73;
      return new Rectangle(x, y, 81, 18);
   }
   private static Rectangle getWorldRectangle(int world) {
      for (int i = 0; i < WORLDS.length; i++) {
         if (WORLDS[i] == world) {
            return getRectangle(i);
         }
      }
      return new Rectangle(0, 0, 0, 0);
   }
   public static int getCurrentWorld() {
      return Game.getCurrentWorld() % 300;
   }

   private static boolean colorsMatch(Color col1, Color col2) {
      return (col1.getRed() == col2.getRed())
            && (col1.getGreen() == col2.getGreen())
            && (col1.getBlue() == col2.getBlue());
   }

   public static void setExcludeFree(boolean enabled) {
      excludeFree = enabled;
   }
   public static void setExcludePVP(boolean enabled) {
      excludePVP = enabled;
   }
   public static void setExcludePopular(boolean enabled) {
      excludePopular = enabled;
   }
   public static void setQuickLogout(boolean enabled) {
      quickLogout = enabled;
   }
}