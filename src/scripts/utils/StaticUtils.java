package scripts.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.tribot.api.General;

public final class StaticUtils {

   private StaticUtils() {};

   public static BufferedImage getImage(String url) throws IOException {
      return ImageIO.read(new URL(url));
   }

  public static void sleep(int min, int max) {
    try {
      Thread.sleep(General.random(min, max));
    } catch (InterruptedException e) {}
  }
   
   /**
    * Convert a millisecond duration to a string format
    * 
    * @param millis
    *           A duration to convert to a string form
    * @return A string of the form "X Days Y Hours Z Minutes A Seconds".
    */
   public static String getDurationBreakdown(long millis) {
      if (millis < 0) {
         throw new IllegalArgumentException("Duration must be greater than zero!");
      }

      // long days = TimeUnit.MILLISECONDS.toDays(millis);
      // millis -= TimeUnit.DAYS.toMillis(days);
      long hours = TimeUnit.MILLISECONDS.toHours(millis);
      millis -= TimeUnit.HOURS.toMillis(hours);
      long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
      millis -= TimeUnit.MINUTES.toMillis(minutes);
      // long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

      StringBuilder sb = new StringBuilder(64);
      // sb.append(days);
      // sb.append(" Days ");
      sb.append(hours);
      sb.append("h ");
      sb.append(minutes);
      sb.append("min ");
      // sb.append(seconds);
      // sb.append(" Seconds");

      return (sb.toString());
   }  
}