package scripts.utils;

import java.awt.Color;
import java.awt.Point;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Mouse;
import org.tribot.api2007.ChooseOption;
import org.tribot.api2007.Game;
import org.tribot.api2007.GameTab;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Player;
import org.tribot.api2007.Screen;
import org.tribot.api2007.Walking;
import org.tribot.api2007.GameTab.TABS;
import org.tribot.api2007.types.RSGroundItem;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSModel;
import org.tribot.api2007.types.RSTile;

public class CommonUtils {

  private CameraUtils cam;

  public CommonUtils(CameraUtils cameraUtils) {
    cam = cameraUtils;
  }

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

  public boolean clickModel(RSModel model, String upText) {
    if(model == null)
      return false;

    if(upText.contains("->"))
      model.click();

    Point p = model.getCentrePoint();
    p.setLocation(p.x + General.random(-7, 7), p.y + General.random(-7, 7));
    Mouse.hop(p);
    if(!cam.isCameraPitching() && !cam.isCameraRotating() && Timing.waitUptext(upText, General.random(350, 400))) {
      Mouse.click(1);
      long failsafe = System.currentTimeMillis();

      while(System.currentTimeMillis() - failsafe < 1000) {
        Color color = Screen.getColorAt(Mouse.getPos());
        // check if click was RED
        if(color.getRed() > 240 && color.getGreen() < 10 && color.getBlue() < 10) {
          return true;
        }
        // check if click was YELLOW
        else if(color.getRed() > 245 && color.getGreen() > 245 && color.getBlue() < 10) {
          return false;
        }
        sleep(10, 20);
      }
      return false;
    } else {
      Mouse.click(3);
      long t = System.currentTimeMillis();
      while(!ChooseOption.isOpen()) {
        sleep(100, 250);
        if(Timing.timeFromMark(t) >= General.random(500, 1000))
          break;
      }
      return ChooseOption.select(upText);
    }
  }
  public boolean rightClickOnModel(RSModel model, String upText) {
    if(model == null)
      return false;

    Point p = model.getCentrePoint();
    p.setLocation(p.x + General.random(-5, 5), p.y + General.random(-5, 5));
    Mouse.move(p);
    Mouse.click(3);
    long t = System.currentTimeMillis();
    while(!ChooseOption.isOpen()) {
      sleep(100, 250);
      if(Timing.timeFromMark(t) >= General.random(1000, 2000)) {
        break;
      }
    }
    return ChooseOption.select(upText);
  }
  public boolean rightClickOnModelExact(RSModel model, String upText) {
    if(model == null)
      return false;

    Mouse.hop(model.getCentrePoint());
    Mouse.click(3);
    long t = System.currentTimeMillis();
    while(!ChooseOption.isOpen()) {
      sleep(100, 250);
      if(Timing.timeFromMark(t) >= General.random(5000, 7000))
        break;
    }
    return ChooseOption.select(upText);
  }
  public boolean sleepwalkTo(RSTile toTile) {
    RSTile dest = Game.getDestination();
    if(dest != null) {
      if(dest.distanceTo(toTile) > 3) {
        if(Walking.walkPath(Walking.generateStraightPath(toTile))) {
          return true;
        }
      }
    } else {
      if(Walking.walkPath(Walking.generateStraightPath(toTile))) {
        return true;
      }
    }
    return false;
  }
  public boolean sleepwalkPath(RSTile[] path) {
    RSTile dest = Game.getDestination();
    if(dest != null) {
      if(dest.distanceTo(path[path.length - 1]) > 3) {
        if(Walking.walkPath(path)) {
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
