package scripts.utils;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Mouse;
import org.tribot.api.interfaces.Positionable;
import org.tribot.api2007.Game;
import org.tribot.api2007.GameTab;
import org.tribot.api2007.GameTab.TABS;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Player;
import org.tribot.api2007.Walking;
import org.tribot.api2007.types.RSGroundItem;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSTile;

public class CommonUtils {

  // -- temp methods
  public boolean isAutoRetaliateIsEnabled() {
    return Game.getSetting(172) == 0;
  }
  public void toggleAutoRetaliate() {
    int[] settingsArray = Game.getSettingsArray();
    if(settingsArray != null && settingsArray.length >= 172) {
      if(!GameTab.getOpen().equals(TABS.COMBAT)) {
        GameTab.open(TABS.COMBAT);
      }
      Mouse.clickBox(604, 359, 716, 401, 1);
      sleep(300, 600);
    }
  }
  // -- end temp methods

  public boolean sleepwalkTo(final Positionable toTile) {
    RSTile dest = Game.getDestination();
    if(dest != null) {
      if(dest.distanceTo(toTile) > 3) {
        if(Walking.walkPath(Walking.randomizePath(Walking.generateStraightPath(toTile), 1, 1))) {
          return true;
        }
      }
    } else {
      if(Walking.walkPath(Walking.randomizePath(Walking.generateStraightPath(toTile), 1, 1))) {
        return true;
      }
    }
    return false;
  }
  public boolean sleepwalkPath(final RSTile[] path) {
    RSTile dest = Game.getDestination();
    if(dest != null) {
      if(dest.distanceTo(path[path.length - 1]) > 3) {
        if(Walking.walkPath(Walking.randomizePath(path, 1, 1))) {
          return true;
        }
      }
    } else {
      if(Walking.walkPath(path)) {
        return true;
      }
    }
    return false;
  }
  public boolean dropItems(int[] items) {
    if(Inventory.getCount(items) > 0) {
      if(!GameTab.getOpen().equals(TABS.INVENTORY)) {
        GameTab.open(TABS.INVENTORY);
        sleep(500, 600);
      }
      String upText = Game.getUptext();
      if(upText != null && upText.contains("->")) {
        clickChatBox();
      }
      if(Inventory.drop(items) > 0) {
        return true;
      }
    }
    return false;
  }

  public void moveMouseRandom(int min, int max) {
    int prevSpeed = Mouse.getSpeed();
    Mouse.setSpeed(200);
    Mouse.move(General.random(min, max), General.random(min, max));
    Mouse.setSpeed(prevSpeed);
  }
  public void clickChatBox() {
    Mouse.clickBox(15, 350, 480, 440, 1);
  }
  public void waitForDestination() {
    int k = 0;
    while(Game.getDestination() != null) {
      if(k > 10)
        break;
      k++;
      sleep(300, 600);
    }
  }
  public void waitUntilIdle(int start, int end) {
    long t = System.currentTimeMillis();

    while(Timing.timeFromMark(t) < General.random(start, end)) {
      if(Player.isMoving() || Player.getAnimation() != -1) {
        t = System.currentTimeMillis();
      }
      sleep(25, 50);
    }
  }
  public void sleep(int i, int j) {
    try {
      Thread.sleep(General.random(i, j));
    } catch (InterruptedException e) {
    }
  }

  public int getItemId(RSItem item) {
    if(item != null)
      return item.getID();

    return 0;
  }

  public int getItemId(RSGroundItem item) {
    if(item != null)
      return item.getID();

    return 0;

  }

  public RSTile randomizeTile(RSTile tile, int x, int y) {
    return new RSTile(tile.getX() + General.random(-x, x), tile.getY() + General.random(-y, y), tile.getPlane());
  }
}
