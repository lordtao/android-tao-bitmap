/*******************************************************************************
 * Copyright (c) 2014 Alexandr Tsvetkov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the The BSD 3-Clause License
 * which accompanies this distribution, and is available at
 * http://opensource.org/licenses/BSD-3-Clause
 * 
 * Contributors:
 *     Alexandr Tsvetkov - initial API and implementation
 * 
 * Project:
 *     TAO Bitmap Utils
 * 
 * File name:
 *     BitmapIO.java
 *     
 * License agreement:
 *
 * 1. This code is published AS IS. Author is not responsible for any damage that can be
 *    caused by any application that uses this code.
 * 2. Author does not give a garantee, that this code is error free.
 * 3. This code can be used in NON-COMMERCIAL applications AS IS without any special
 *    permission from author.
 * 4. This code can be modified without any special permission from author IF AND ONLY IF
 *    this license agreement will remain unchanged.
 * 5. SPECIAL PERMISSION for this code usage in COMMERCIAL application SHOULD be obtained
 *    from author.
 ******************************************************************************/
package ua.at.tsvetkov.bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ua.at.tsvetkov.io.FileIO;
import ua.at.tsvetkov.io.FilePath;
import ua.at.tsvetkov.security.Md5;
import ua.at.tsvetkov.util.Log;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;

/**
 * IO for working with Bitmap
 * 
 * @author A.Tsvetkov 2014 http://tsvetkov.at.ua mailto:al@ukr.net
 */
public class BitmapIO {

   private BitmapIO() {

   }

   /**
    * Load Bitmap from Assets.
    * 
    * @param context
    * @param name
    * @return Bitmap
    * @throws IOException
    */
   public static Bitmap loadBitmapFromAssets(Context context, String name) throws IOException {
      InputStream is = null;
      Bitmap res = null;
      is = context.getAssets().open(name);
      res = BitmapFactory.decodeStream(is);
      is.close();
      return res;
   }

   /**
    * Compress bitmap to file. If compressing was success then bitmap will recycle.
    * 
    * @param bitmap
    * @param fileName
    * @return true if success
    */
   public static boolean saveToFile(Bitmap bitmap, String fileName) {
      File file = new File(fileName); // Stub file name
      try {
         bitmap.compress(Bitmap.CompressFormat.PNG, 0, new FileOutputStream(file));
         bitmap.recycle();
      } catch (Exception e) {
         Log.e("Can't compress bitmap to file " + fileName, e);
         return false;
      }
      return true;
   }

   /**
    * Copy src image file to dst file with resizing.
    * 
    * @param srcFile
    * @param dstFile
    * @param reqWidth
    * @param reqHeight
    * @return true if success
    */
   public static boolean resizeAndSaveTo(String srcFile, String dstFile, float reqWidth, float reqHeight) {
      Bitmap bitmap = BitmapCaсheIO.decodeSampledBitmapFromFile(srcFile, reqWidth, reqHeight);

      PointF dim = BitmapData.getImageProportions(srcFile);
      int dstWidth;
      int dstHeight;
      float scaleX = dim.x / reqWidth;
      float scaleY = dim.y / reqWidth;
      if (scaleX > scaleY) {
         dstWidth = (int) (dim.x / scaleX);
         dstHeight = (int) (dim.y / scaleX);
      } else {
         dstWidth = (int) (dim.x / scaleY);
         dstHeight = (int) (dim.y / scaleY);
      }

      bitmap = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, false);
      boolean result = saveToFile(bitmap, dstFile);
      if (bitmap != null && !bitmap.isRecycled()) {
         bitmap.recycle();
      }
      return result;
   }

   /**
    * Resize image file to nearest size.
    * 
    * @param fileName
    * @param reqWidth
    * @param reqHeight
    * @return
    * @return true if success
    */
   public static boolean resizeAndSave(String fileName, float reqWidth, float reqHeight) {
      String tmpFileName = FilePath.getFilePath(fileName) + Md5.getHasheString(fileName) + ".bin";
      boolean result = resizeAndSaveTo(fileName, tmpFileName, reqWidth, reqHeight);
      if (result) {
         return FileIO.rename(tmpFileName, fileName);
      } else {
         return false;
      }
   }

   /**
    * Decrease if need image file to nearest size.
    * 
    * @param fileName
    * @param maxWidth
    * @param maxHeight
    * @return
    * @return true if success
    */
   public static boolean decreaseAndSave(String fileName, float maxWidth, float maxHeight) {
      PointF dim = BitmapData.getImageProportions(fileName);
      if (maxWidth > dim.x && maxHeight > dim.y) {
         return true;
      }
      String tmpFileName = FilePath.getFilePath(fileName) + Md5.getHasheString(fileName) + ".bin";
      boolean result = resizeAndSaveTo(fileName, tmpFileName, maxWidth, maxHeight);
      if (result) {
         return FileIO.rename(tmpFileName, fileName);
      } else {
         return false;
      }
   }

}
