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

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * IO for working with Bitmap
 * 
 * @author A.Tsvetkov 2014 http://tsvetkov.at.ua mailto:al@ukr.net
 */
public class BitmapIO {

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

}
