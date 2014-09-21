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
 *     BitmapConverter.java
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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

/**
 * Creating bitmaps from different sources
 * 
 * @author A.Tsvetkov 2014 http://tsvetkov.at.ua mailto:al@ukr.net
 */
public class BitmapConverter {

   /**
    * Generate bitmap from view.
    * 
    * @param context
    * @param id
    * @return
    */
   public static Bitmap createBitmapFromView(Context context, int id) {
      View view = LayoutInflater.from(context).inflate(id, null);
      return createBitmapFromView(view);
   }

   /**
    * Generate bitmap from view.
    * 
    * @param view
    * @return
    */
   public static Bitmap createBitmapFromView(View view) {
      if (view.getMeasuredHeight() <= 0) {
         view.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
         Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
         Canvas canvas = new Canvas(bitmap);
         view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
         view.draw(canvas);
         return bitmap;
      } else {
         Bitmap bitmap = Bitmap.createBitmap(view.getLayoutParams().width, view.getLayoutParams().height, Bitmap.Config.ARGB_8888);
         Canvas canvas = new Canvas(bitmap);
         view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
         view.draw(canvas);
         return bitmap;

      }
   }

   /**
    * Combine bitmap with mask and create shaped bitmap
    * 
    * @param mask
    * @param bitmap
    * @return shaped bitmap
    */
   public Bitmap createShapedBitmap(Bitmap mask, Bitmap bitmap) {
      Bitmap bmp;

      int width = mask.getWidth() > bitmap.getWidth() ? mask.getWidth() : bitmap.getWidth();
      int height = mask.getHeight() > bitmap.getHeight() ? mask.getHeight() : bitmap.getHeight();

      bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
      Paint paint = new Paint();
      paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));

      Canvas canvas = new Canvas(bmp);
      canvas.drawBitmap(mask, 0, 0, null);
      canvas.drawBitmap(bitmap, 0, 0, paint);

      return bmp;
   }
}
