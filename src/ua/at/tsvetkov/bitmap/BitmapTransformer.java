/*******************************************************************************
 * Copyright (c) 2014 Alexandr Tsvetkov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the The BSD 3-Clause License
 * which accompanies this distribution, and is available at
 * http://opensource.org/licenses/BSD-3-Clause *
 * Contributors:
 *     Alexandr Tsvetkov - initial API and implementation
 *
 * Project:
 *     TAO Data Processor
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
 ******************************************************************************/
package ua.at.tsvetkov.bitmap;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.InputStream;

import ua.at.tsvetkov.util.Log;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Base64;

/**
 * Transform the new Bitmap with given parameters.
 * 
 * @author Alexandr Tsvetkov 2014
 */
public class BitmapTransformer {

   private static final int NO_RESOURCE      = -1;
   // Bitmap data source
   private Bitmap           bitmap           = null;
   private byte[]           data             = null;
   private String           fileName         = null;
   private String           assetsFileName   = null;
   private int              resourceId       = NO_RESOURCE;
   private FileDescriptor   fileDescriptor   = null;
   private InputStream      inputStream      = null;

   // Bitmap modifiers
   private Options          options          = null;

   private float            rotateAngel      = 0;
   private boolean          isFlipHorizontal = false;
   private boolean          isFlipVertical   = false;
   private boolean          isUseCacheFile   = true;
   private float            scaleX           = 1;
   private float            scaleY           = 1;
   private float            width            = -1;
   private float            height           = -1;
   private boolean          isNeedToResize   = true;
   private boolean          isUseFilter      = true;

   private Context          context;
   private String           cacheFileName    = null;
   private boolean          isNeedToCrop     = false;
   private float            cropLeft;
   private float            cropTop;
   private float            cropWidth;
   private float            cropHeight;

   private BitmapTransformer(Context context) {
      this.context = context;
   }

   public static BitmapTransformer getInstance(Context context) {
      return new BitmapTransformer(context);
   }

   /**
    * Transform the new Bitmap with given parameters.
    * 
    * @return Bitmap object
    */
   public Bitmap transform() {
      checkDataSource();
      checkScaling();
      prepareBitmap();
      transformBitmap();
      return bitmap;
   }

   // ************************* Private transform methods ******************************

   private void checkDataSource() {
      if (data != null) { // Source is byte array
         if (isUseCacheFile) {
            cacheFileName = BitmapCaсheIO.saveCaсheFile(data);
         } else {
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
         }
      } else if (fileName != null) { // Source is file
         if (isUseCacheFile) {
            cacheFileName = BitmapCaсheIO.copyToCaсheFile(fileName);
         } else {
            bitmap = BitmapFactory.decodeFile(fileName, options);
         }
      } else if (assetsFileName != null) { // Source is file from assets
         if (isUseCacheFile) {
            try {
               InputStream in = context.getAssets().open(assetsFileName);
               cacheFileName = BitmapCaсheIO.copyToCaсheFile(in);
            } catch (Exception e) {
               Log.e("Can't load from assets file " + assetsFileName, e);
            }
         } else {
            bitmap = BitmapFactory.decodeFile(fileName, options);
         }
      } else if (inputStream != null) { // Source is inputStream
         if (isUseCacheFile) {
            cacheFileName = BitmapCaсheIO.copyToCaсheFile(inputStream);
         } else {
            bitmap = BitmapFactory.decodeFile(fileName, options);
         }
      } else if (resourceId != NO_RESOURCE) { // Source is raw resource
         if (isUseCacheFile) {
            try {
               InputStream in = context.getResources().openRawResource(resourceId);
               cacheFileName = BitmapCaсheIO.copyToCaсheFile(in);
            } catch (Exception e) {
               Log.e("Can't load from resource " + resourceId, e);
            }
         } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
         }
      } else if (fileDescriptor != null) { // Source is file descriptor
         if (isUseCacheFile) {
            try {
               InputStream in = new FileInputStream(fileDescriptor);
               cacheFileName = BitmapCaсheIO.copyToCaсheFile(in);
            } catch (Exception e) {
               Log.e("Can't load from resource " + resourceId, e);
            }
         } else {
            bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
         }
      } else if (bitmap != null) { // Source is bitmap
         if (isUseCacheFile) {
            try {
               cacheFileName = BitmapCaсheIO.copyToCaсheFile(bitmap);
            } catch (Exception e) {
               Log.e("Can't load from resource " + resourceId, e);
            }
         } else {
            // throw new IllegalArgumentException("Bitmap data source was not set.");
         }
      }
   }

   private void checkScaling() {
      if ((width <= 0 && height <= 0) && (scaleX == 1 && scaleY == 1)) {
         isNeedToResize = false;
      }
      PointF point = new PointF();
      if (isUseCacheFile) {
         point = BitmapCaсheIO.getImageDimensions(cacheFileName);
      } else {
         point.x = bitmap.getWidth();
         point.y = bitmap.getHeight();
      }
      if (isNeedToResize) {
         if (scaleX != 1 || scaleY != 1) {
            width = point.x * scaleX;
            height = point.y * scaleY;
         }
      } else {
         width = point.x;
         height = point.y;
      }
   }

   private void prepareBitmap() {
      if (cacheFileName != null) {
         if (isNeedToResize) {
            bitmap = BitmapCaсheIO.decodeSampledBitmapFromFile(cacheFileName, width, height, options);
            bitmap = Bitmap.createScaledBitmap(bitmap, (int) width, (int) height, isUseFilter);
         } else {
            bitmap = BitmapFactory.decodeFile(cacheFileName, options);
         }
      } else {
         if (isNeedToResize) {
            bitmap = Bitmap.createScaledBitmap(bitmap, (int) width, (int) height, isUseFilter);
         } else {
            // Bitmap already exist, nothing to do
         }
      }
   }

   private void transformBitmap() {
      Matrix matrix = new Matrix();
      if (isFlipHorizontal && isFlipVertical) {
         matrix.setScale(-1, -1);
         matrix.postTranslate(bitmap.getWidth(), bitmap.getHeight());
      } else if (isFlipHorizontal) {
         matrix.setScale(-1, 1);
         matrix.postTranslate(bitmap.getWidth(), 0);
      } else if (isFlipVertical) {
         matrix.setScale(1, -1);
         matrix.postTranslate(0, bitmap.getHeight());
      }
      if (rotateAngel != 0) {
         matrix.postRotate(rotateAngel);
      }
      if (isNeedToCrop) {
         try {
            bitmap = Bitmap.createBitmap(bitmap, (int) cropLeft, (int) cropTop, (int) cropWidth, (int) cropHeight, matrix, isUseFilter);
         } catch (Exception e) {
            Log.e("Wrong crop parameters. Transformed bitmap has width=" + bitmap.getWidth() + " and height=" + bitmap.getHeight() + ", but you try to crop to width=" + cropWidth + " and height="
                  + cropHeight, e);
         }
      } else {
         bitmap = Bitmap.createBitmap(bitmap, 0, 0, (int) width, (int) height, matrix, isUseFilter);
      }
   }

   // *********************** Getters and Setters ************************

   /**
    * Set source bitmap
    * 
    * @param bitmap
    * @return
    */
   public BitmapTransformer setSourceBitmap(Bitmap bitmap) {
      if (data != null || assetsFileName != null || fileName != null || inputStream != null || fileDescriptor != null || resourceId != NO_RESOURCE) {
         throw new IllegalArgumentException("Bitmap data source must be only once.");
      }
      this.bitmap = bitmap;
      return this;
   }

   /**
    * Set as source the specified byte array.
    * 
    * @param data byte array of compressed image data
    * @return
    */
   public BitmapTransformer setSourceByteArray(byte[] data) {
      if (bitmap != null || assetsFileName != null || fileName != null || inputStream != null || fileDescriptor != null || resourceId != NO_RESOURCE) {
         throw new IllegalArgumentException("Bitmap data source must be only once.");
      }
      this.data = data;
      return this;
   }

   /**
    * Set as source the Base64-encoded data.
    * 
    * @param string
    * @return
    */
   public BitmapTransformer setSourceEncodedString(String string) {
      if (bitmap != null || assetsFileName != null || fileName != null || inputStream != null || fileDescriptor != null || resourceId != NO_RESOURCE) {
         throw new IllegalArgumentException("Bitmap data source must be only once.");
      }
      try {
         this.data = Base64.decode(string, Base64.DEFAULT);
      } catch (Exception e) {
         Log.e("Can't decode source data.", e);
      }
      return this;
   }

   /**
    * Set as source the image file.
    * 
    * @param fileName
    * @return
    */
   public BitmapTransformer setSourceFile(String fileName) {
      if (bitmap != null || data != null || assetsFileName != null || inputStream != null || fileDescriptor != null || resourceId != NO_RESOURCE) {
         throw new IllegalArgumentException("Bitmap data source must be only once.");
      }
      this.fileName = fileName;
      return this;
   }

   /**
    * Set as source the file from resources.
    * 
    * @param resourceId
    * @return
    */
   public BitmapTransformer setSourceFromResourse(int resourceId) {
      if (bitmap != null || data != null || assetsFileName != null || fileName != null || inputStream != null || fileDescriptor != null) {
         throw new IllegalArgumentException("Bitmap data source must be only once.");
      }
      this.resourceId = resourceId;
      return this;
   }

   /**
    * Set as source the FileDescriptor.
    * 
    * @param fileDescriptor
    * @return
    */
   public BitmapTransformer setSourceFileDescriptor(FileDescriptor fileDescriptor) {
      if (bitmap != null || data != null || assetsFileName != null || fileName != null || inputStream != null || resourceId != NO_RESOURCE) {
         throw new IllegalArgumentException("Bitmap data source must be only once.");
      }
      this.fileDescriptor = fileDescriptor;
      return this;
   }

   /**
    * Set as source the InputStream.
    * 
    * @param inputStream
    * @return
    */
   public BitmapTransformer setSourceInputStream(InputStream inputStream) {
      if (bitmap != null || data != null || assetsFileName != null || fileName != null || fileDescriptor != null || resourceId != NO_RESOURCE) {
         throw new IllegalArgumentException("Bitmap data source must be only once.");
      }
      this.inputStream = inputStream;
      return this;
   }

   /**
    * Set as source the file from assets.
    * 
    * @param assetsFileName
    * @return
    */
   public BitmapTransformer setSourceFromAssets(String assetsFileName) {
      if (bitmap != null || data != null || fileName != null || inputStream != null || fileDescriptor != null || resourceId != NO_RESOURCE) {
         throw new IllegalArgumentException("Bitmap data source must be only once.");
      }
      this.assetsFileName = assetsFileName;
      return this;
   }

   /**
    * Set {@link BitmapFactory.Options BitmapFactory.Options}
    * 
    * @param options
    * @return
    */
   public BitmapTransformer setBitmapOptions(BitmapFactory.Options options) {
      this.options = options;
      return this;
   }

   /**
    * Set bitmap rotate angle
    * 
    * @param rotateAngel
    * @return
    */
   public BitmapTransformer setRotateAngel(float rotateAngel) {
      this.rotateAngel = rotateAngel;
      return this;
   }

   /**
    * Set target bitmap size
    * 
    * @param width
    * @param height
    * @return
    */
   public BitmapTransformer setSize(float width, float height) {
      if (width <= 0 || height <= 0) {
         throw new IllegalArgumentException("Wrong bitmap size parameters.");
      }
      this.width = width;
      this.height = height;
      return this;
   }

   /**
    * Set bitmap scaling
    * 
    * @param scaleX
    * @param scaleY
    * @return
    */
   public BitmapTransformer setScaling(float scaleX, float scaleY) {
      if (scaleX <= 0 || scaleY <= 0) {
         throw new IllegalArgumentException("Wrong bitmap skale parameters.");
      }
      this.scaleX = scaleX;
      this.scaleY = scaleY;
      return this;
   }

   /**
    * False by default
    * 
    * @param isFlipHorizontal
    * @return
    */
   public BitmapTransformer setFlipHorizontal(boolean isFlipHorizontal) {
      this.isFlipHorizontal = isFlipHorizontal;
      return this;
   }

   /**
    * False by default
    * 
    * @param isFlipVertical
    * @return
    */
   public BitmapTransformer setFlipVertical(boolean isFlipVertical) {
      this.isFlipVertical = isFlipVertical;
      return this;
   }

   /**
    * True by default
    * 
    * @param isUseCacheFile
    * @return
    */
   public BitmapTransformer setCreateCacheFile(boolean isUseCacheFile) {
      this.isUseCacheFile = isUseCacheFile;
      return this;
   }

   /**
    * True by default
    * 
    * @param isUseFilter
    * @return
    */
   public BitmapTransformer setUseFilter(boolean isUseFilter) {
      this.isUseFilter = isUseFilter;
      return this;
   }

   /**
    * Crop transformed bitmap
    * 
    * @param cropLeft
    * @param cropTop
    * @param cropWidth
    * @param cropHeight
    * @return
    */
   public BitmapTransformer setCrop(float cropLeft, float cropTop, float cropWidth, float cropHeight) {
      if (cropLeft < 0 || cropTop < 0) {
         throw new IllegalArgumentException("Wrong crop parameters. Left and top of bitmap must be >= 0");
      }
      if (cropWidth <= 0 || cropHeight <= 0) {
         throw new IllegalArgumentException("Wrong crop parameters. Width and height must be > 0");
      }
      this.cropLeft = cropLeft;
      this.cropTop = cropTop;
      this.cropWidth = cropWidth;
      this.cropHeight = cropHeight;
      isNeedToCrop = true;
      return this;
   }

   /**
    * Crop transformed bitmap
    * 
    * @param cropArea
    * @return
    */
   public BitmapTransformer setCrop(RectF cropArea) {
      if (cropArea.left < 0 || cropArea.top < 0) {
         throw new IllegalArgumentException("Wrong crop parameters. Left and top of bitmap must be >= 0");
      }
      if (cropArea.width() <= 0 || cropArea.height() <= 0) {
         throw new IllegalArgumentException("Wrong crop parameters. Width and height must be > 0");
      }
      this.cropLeft = cropArea.left;
      this.cropTop = cropArea.top;
      this.cropWidth = cropArea.width();
      this.cropHeight = cropArea.height();
      isNeedToCrop = true;
      return this;
   }

}
