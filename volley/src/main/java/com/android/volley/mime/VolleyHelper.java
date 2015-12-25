package com.android.volley.mime;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.squareup.okhttp.OkHttpClient;

public class VolleyHelper
{
	// Volley request queue
	private static RequestQueue mRequestQueue;
	// Volley image loader
	private ImageLoader mImageLoader;
	// Volley image cache
	private LruBitmapCache mLruBitmapCache;

	private static RequestQueue getVolleyRequestQueue(Context context)
	{
		if (mRequestQueue == null)
		{
			mRequestQueue = Volley.newRequestQueue(context, new OkHttpStack(new OkHttpClient()));
		}
		return mRequestQueue;
	}
	
	/**
	 * Adds a request to the Volley request queue
	 */
	private static void addRequest(Context context, Request<?> request)
	{
		getVolleyRequestQueue(context).add(request);
	}

	/**
	 * Adds a request to the Volley request queue with a given tag
	 */
	public static void addRequest(Context context, Request<?> request, String tag)
	{
		request.setTag(tag);
		addRequest(context, request);
	}

	/**
	 * Cancels all the request in the Volley queue for a given tag
	 */
	public static void cancelAllRequests(Context context, String tag)
	{
		if (getVolleyRequestQueue(context) != null)
		{
			getVolleyRequestQueue(context).cancelAll(tag);
		}
	}

	/**
	 * Returns an image loader instance to be used with Volley.
	 * 
	 * @return {@link com.android.volley.toolbox.ImageLoader}
	 */
	public ImageLoader getVolleyImageLoader(Context context)
	{
		if (mImageLoader == null)
		{
			mImageLoader = new ImageLoader(getVolleyRequestQueue(context), getVolleyImageCache(context));
		}
		
		return mImageLoader;
	}

	/**
	 * Returns a bitmap cache to use with volley.
	 * 
	 * @return {@link LruBitmapCache}
	 */
	private LruBitmapCache getVolleyImageCache(Context context)
	{
		if (mLruBitmapCache == null)
		{
			mLruBitmapCache = new LruBitmapCache(context);
		}
		return mLruBitmapCache;
	}
}
