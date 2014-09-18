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
 *     BitmapCaсheIO.java
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import ua.at.tsvetkov.application.AppConfig;
import ua.at.tsvetkov.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

/**
 * Work with cached bitmaps
 * 
 * @author Alexandr Tsvetkov 2014
 */
public class BitmapCaсheIO {

   /**
    * Return hash sum String for given data
    * 
    * @param data
    * @return
    */
   public static String md5(byte[] data) {
      byte[] hash;

      try {
         hash = MessageDigest.getInstance("MD5").digest(data);
      } catch (NoSuchAlgorithmException e) {
         throw new RuntimeException("Huh, MD5 should be supported?", e);
      }

      StringBuilder hex = new StringBuilder(hash.length * 2);
      for (byte b : hash) {
         if ((b & 0xFF) < 0x10)
            hex.append("0");
         hex.append(Integer.toHexString(b & 0xFF));
      }
      return hex.toString();
   }

   /**
    * Generate file name ({@link md5} string and ".bin" extension) in to the directory on the primary external filesystem (that is somewhere
    * on {@link AppConfig.getCacheFileName} where the application can place cache files it owns. These files are internal to
    * the application, and not typically visible to the user as media.
    * 
    * @param context
    * @param data
    * @return
    */
   public static String getCachedFileName(byte[] data) {
      return AppConfig.getCacheFileName(md5(data) + ".bin");
   }

   /**
    * Create cached resized bitmap with NEAREST size from byte array. Cache file placed in cache directory, see
    * {@link BitmapCaсheIO.getCachedFileName}
    * 
    * @param data
    * @param reqWidth
    * @param reqHeight
    * @retur
    */
   public static Bitmap createSampledCaсhedBitmap(byte[] data, float reqWidth, float reqHeight) {
      String caсhedFileName = getCachedFileName(data);
      return createSampledCaсhedBitmap(caсhedFileName, data, reqWidth, reqHeight);
   }

   /**
    * Create cached resized bitmap with NEAREST size from byte array.
    * 
    * @param caсhedFileName
    * @param data
    * @param reqWidth
    * @param reqHeight
    * @return
    */
   public static Bitmap createSampledCaсhedBitmap(String caсhedFileName, byte[] data, float reqWidth, float reqHeight) {
      if (checkCaсhedFile(caсhedFileName, data))
         return decodeSampledBitmapFromFile(caсhedFileName, reqWidth, reqHeight);
      else
         return null;
   }

   /**
    * Check of presence the cached file and create it from data if file is not present. Cache file placed in cache directory, see
    * {@link BitmapCaсheIO.getCachedFileName}
    * 
    * @param data
    * @return
    */
   public static boolean checkCaсhedFile(  byte[] data) {
      String caсhedFileName = getCachedFileName( data);
      return checkCaсhedFile(caсhedFileName, data);
   }

   /**
    * Check of presence the cached file and create it from data if file is not present
    * 
    * @param caсhedFileName
    * @param data
    * @return
    */
   public static boolean checkCaсhedFile(String caсhedFileName, byte[] data) {
      File file = new File(caсhedFileName);
      if (!file.exists()) {
         try {
            FileOutputStream fOut = new FileOutputStream(file);
            fOut.write(data, 0, data.length);
            fOut.flush();
            fOut.close();
         } catch (IOException e) {
            Log.w(e);
            return false;
         }
      }
      return true;
   }

   /**
    * Return resized bitmap with NEAREST size.
    * 
    * @param pathName
    * @param reqWidth
    * @param reqHeight
    * @return
    */
   public static Bitmap decodeSampledBitmapFromFile(String pathName, float reqWidth, float reqHeight) {

      // First decode with inJustDecodeBounds=true to check dimensions
      final BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;
      BitmapFactory.decodeFile(pathName, options);

      // Calculate inSampleSize
      options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

      // Decode bitmap with inSampleSize set
      options.inJustDecodeBounds = false;
      return BitmapFactory.decodeFile(pathName, options);
   }

   /**
    * Return an image dimensions for image data. Cache file placed in cache directory, see {@link BitmapCaсheIO.getCachedFileName}
    * 
    * @param pathName
    * @return point.x = width, point.y = height
    */
   public static Point getImageDimensions(  byte[] data) {
      String caсhedFileName = getCachedFileName( data);
      return getImageDimensions(caсhedFileName, data);
   }

   /**
    * Return an image dimensions for image data
    * 
    * @param caсhedFileName
    * @param pathName
    * @return point.x = width, point.y = height
    */
   public static Point getImageDimensions(String caсhedFileName, byte[] data) {
      Point size = new Point();
      if (checkCaсhedFile(caсhedFileName, data)) {
         BitmapFactory.Options options = new BitmapFactory.Options();
         options.inJustDecodeBounds = true;
         BitmapFactory.decodeFile(caсhedFileName, options);
         if (options.outWidth == -1 || options.outHeight == -1) {
            Log.e("Can't decode " + caсhedFileName);
            return size;
         } else {
            return new Point(options.outWidth, options.outHeight);
         }
      } else {
         Log.e("Can't decode " + caсhedFileName);
         return size;
      }

   }

   /**
    * Calculate nearest sizes
    * 
    * @param options
    * @param reqWidth
    * @param reqHeight
    * @return
    */
   private static int calculateInSampleSize(BitmapFactory.Options options, float reqWidth, float reqHeight) {
      // Raw height and width of image
      final int height = options.outHeight;
      final int width = options.outWidth;
      int inSampleSize = 1;

      if (height > reqHeight || width > reqWidth) {

         final int halfHeight = height / 2;
         final int halfWidth = width / 2;

         // Calculate the largest inSampleSize value that is a power of 2 and keeps both
         // height and width larger than the requested height and width.
         while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
         }
      }

      return inSampleSize;
   }

}
