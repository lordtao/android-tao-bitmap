/**
 * 
 */
package ua.at.tsvetkov.bitmap;

import ua.at.tsvetkov.io.FileIO;
import ua.at.tsvetkov.util.Log;
import android.graphics.BitmapFactory;
import android.graphics.PointF;

/**
 * @author Alexandr Tsvetkov 2015
 */
public class BitmapData {

   private BitmapData() {

   }

   /**
    * Return an image proportions for image data. Temporary cache file placed in default cache directory. Need to WRITE_EXTERNAL_STORAGE
    * permission.
    * 
    * @param data
    * @return point.x = width, point.y = height. On error return 0,0.
    */
   public static PointF getImageProportions(byte[] data) {
      String caсhedFileName = BitmapCaсheIO.getCachedFileName(data);
      caсhedFileName = BitmapCaсheIO.saveCaсheFile(data);
      PointF point = getImageProportions(caсhedFileName);
      FileIO.delete(caсhedFileName);
      return point;
   }

   /**
    * Return an image proportions for image file.
    * 
    * @param fileName
    * @return point.x = width, point.y = height. On error return 0,0.
    */
   public static PointF getImageProportions(String fileName) {
      PointF size = new PointF(0, 0);
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;
      BitmapFactory.decodeFile(fileName, options);
      if (options.outWidth == -1 || options.outHeight == -1) {
         Log.e("Can't decode " + fileName);
         return size;
      } else {
         return new PointF(options.outWidth, options.outHeight);
      }
   }

}
