package com.shequtong.yishequ.volley;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import com.android.volley.toolbox.ImageLoader;
import android.os.Build.VERSION;
import android.support.v4.util.LruCache;

/**
 * 
 * TODO Bitmap缓存设置
 * @author  Wanglei 
 * @data:  2015年6月4日 下午3:21:12 
 * @version:  V1.0
 */
public class BitmapLruCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {
    public BitmapLruCache(int maxSize) {
        super(maxSize);
    }
    
    @SuppressLint("NewApi")
	@Override
    protected int sizeOf(String key, Bitmap bitmap) {
        if (VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        }
        
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    @Override
    public Bitmap getBitmap(String url) {
        return get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }
}

