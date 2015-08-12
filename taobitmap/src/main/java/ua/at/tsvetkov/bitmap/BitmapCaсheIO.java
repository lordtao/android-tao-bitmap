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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import ua.at.tsvetkov.application.AppConfig;
import ua.at.tsvetkov.io.FileIO;
import ua.at.tsvetkov.security.Md5;
import ua.at.tsvetkov.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Work with cached bitmap
 * 
 * @author Alexandr Tsvetkov 2014
 */
public class BitmapCaсheIO {

   private BitmapCaсheIO() {

   }

   /**
    * Create cached resized bitmap with NEAREST size from byte array. Cache file placed in cache directory, see
    * {@link FileIO#getCacheFileName(String str)}
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
      if (saveCaсheFile(caсhedFileName, data)) {
         return decodeSampledBitmapFromFile(caсhedFileName, reqWidth, reqHeight);
      } else {
         return null;
      }
   }

   /**
    * Create cache file from data if file is not present. Cache file placed in cache directory, see {@link FileIO#getCacheFileName(String str)}
    * 
    * @param data
    * @return cache file name or null
    */
   public static String saveCaсheFile(byte[] data) {
      String caсhedFileName = getCachedFileName(data);
      if (saveCaсheFile(caсhedFileName, data)) {
         return caсhedFileName;
      } else {
         return null;
      }
   }

   /**
    * Copy source file to cache file placed in cache directory, see {@link FileIO#getCacheFileName(String str)}
    * 
    * @param sourceFileName
    * @return cache file name or null
    */
   public static String copyToCaсheFile(String sourceFileName) {
      byte[] data = null;
      try {
         File file = new File(sourceFileName);
         FileInputStream in = new FileInputStream(file);
         data = new byte[(int) file.length()];
         in.read(data);
         in.close();
      } catch (Exception e) {
         Log.e(e);
         return null;
      }
      String caсhedFileName = getCachedFileName(data);
      if (!saveCaсheFile(caсhedFileName, data)) {
         Log.e("Can't create cache file " + caсhedFileName);
         return null;
      }
      return caсhedFileName;
   }

   /**
    * Copy source file to cache file placed in cache directory, see {@link FileIO#getCacheFileName(String str)}
    * 
    * @param in
    * @return cache file name or null
    */
   public static String copyToCaсheFile(InputStream in) {
      try {
         File file = new File(getCachedFileName(new byte[] { 7, 7, 7, 7, 7, 7, 7 })); // Stub file name
         FileOutputStream fOut = new FileOutputStream(file);
         byte[] buffer = new byte[1024 * 8];
         int bytesRead = -1;
         while ((bytesRead = in.read(buffer)) != -1) {
            fOut.write(buffer, 0, bytesRead);
         }
         fOut.flush();
         fOut.close();
         in.close();
         buffer = null;
         String caсhedFileName = Md5.getHashString(file.getName());
         if (!file.renameTo(new File(caсhedFileName))) {
            Log.e("Can't rename cache file" + file.getName());
            return null;
         }
         return caсhedFileName;
      } catch (Exception e) {
         Log.e("Can't create cache file from InputStream", e);
         return null;
      }
   }

   /**
    * Compress bitmap to cache file placed in cache directory, see {@link FileIO#getCacheFileName(String str)}. If compressing was success then
    * bitmap will recicle.
    * 
    * @param bitmap
    * @return cache file name or null
    */
   public static String copyToCaсheFile(Bitmap bitmap) {
      File file = new File(getCachedFileName(new byte[] { 7, 7, 7, 7, 7, 7, 7 })); // Stub file name
      try {
         bitmap.compress(Bitmap.CompressFormat.PNG, 0, new FileOutputStream(file));
         String caсhedFileName = Md5.getHashString(file.getName());
         if (!file.renameTo(new File(caсhedFileName))) {
            Log.e("Can't rename cache file" + file.getName());
            return null;
         }
         bitmap.recycle();
         return caсhedFileName;
      } catch (Exception e) {
         Log.e("Can't compress bitmap to file", e);
         return null;
      }
   }

   /**
    * Create cache file from data if file is not present
    * 
    * @param caсhedFileName
    * @param data
    * @return true if success
    */
   public static boolean saveCaсheFile(String caсhedFileName, byte[] data) {
      File file = new File(caсhedFileName);
      if (!file.exists()) {
         try {
            FileOutputStream fOut = new FileOutputStream(file);
            fOut.write(data, 0, data.length);
            fOut.flush();
            fOut.close();
         } catch (Exception e) {
            Log.w("Can't create cache file " + caсhedFileName, e);
            return false;
         }
      }
      return true;
   }

   /**
    * Read cache file
    * 
    * @param fileName
    * @return data
    */
   public static byte[] readDataFile(String fileName) {
      File file = new File(fileName);
      byte[] data = new byte[(int) file.length()];
      byte[] buffer = new byte[1024];
      if (file.exists()) {
         try {
            FileInputStream fIn = new FileInputStream(file);
            int count = 0;
            int pos = 0;
            while ((count = fIn.read(buffer)) > 0) {
               for (int i = 0; i < count; i++) {
                  data[pos++] = buffer[i];
               }
            }
            fIn.close();
         } catch (Exception e) {
            Log.w("Can't read cache file " + fileName, e);
            return null;
         }
      }
      return data;
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
      return decodeSampledBitmapFromFile(pathName, reqWidth, reqHeight, null);
   }

   /**
    * Return resized bitmap with NEAREST size.
    * 
    * @param pathName
    * @param reqWidth
    * @param reqHeight
    * @return
    */
   public static Bitmap decodeSampledBitmapFromFile(String pathName, float reqWidth, float reqHeight, BitmapFactory.Options options) {
      // First decode with inJustDecodeBounds=true to check dimensions
      if (options == null) {
         options = new BitmapFactory.Options();
      }
      options.inJustDecodeBounds = true;
      BitmapFactory.decodeFile(pathName, options);

      // Calculate inSampleSize
      options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

      // Decode bitmap with inSampleSize set
      options.inJustDecodeBounds = false;
      return BitmapFactory.decodeFile(pathName, options);
   }

   /**
    * Calculate nearest sizes
    * 
    * @param options
    * @param reqWidth
    * @param reqHeight
    * @return
    */
   public static int calculateInSampleSize(BitmapFactory.Options options, float reqWidth, float reqHeight) {
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

   /**
    * Generate file name ({@link Md5} string and ".bin" extension) in to the directory on the primary external filesystem (that is somewhere
    * on {@link FileIO#getCacheFileName(String str)} where the application can place cache files it owns. These files are internal to the
    * application, and not typically visible to the user as media.
    * 
    * @param data
    * @return
    */
   static String getCachedFileName(byte[] data) {
      return getCachedFileName(data, ".bin");
   }

   /**
    * Generate file name ({@link Md5} string and your extension) in to the directory on the primary external filesystem (that is somewhere
    * on {@link FileIO#getCacheFileName(String str)} where the application can place cache files it owns. These files are internal to the
    * application, and not typically visible to the user as media.
    * 
    * @param data
    * @return
    */
   static String getCachedFileName(byte[] data, String extension) {
      return FileIO.getCacheFileName(Md5.getHashString(data) + extension);
   }

}
