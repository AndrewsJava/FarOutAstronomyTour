package co.astronomy.harlequinmettle.deepskyexploration;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;

public class DeepSkyViewer extends Activity implements OnTouchListener, SensorEventListener, StringConstantArrays {

	private static final String INDEX = "lastPossition";
	private static final String SAVE_SIZE = "lastScale";
	private static final String CATEGORY = "currentCategorie";
	private static final String GUIDE = "showGuide";
	private static final float shakeThresholdInGForce = 2.05F;
	private static final float gravityEarth = SensorManager.GRAVITY_EARTH;
	private static float x = 0, y = 0, x2 = 0, y2 = 0, xi = 0, yi = 0, x2i = 0, y2i = 0;
	private int fingers = 0, controlsHeight = 40;
	private static boolean lock = false;

	private Object mDiskCacheLock = new Object();
	private DisplayMetrics metrics = new DisplayMetrics();
	private static MyImageView myImV;
	private static LruCache<String, Bitmap> dynamicImages;
	private SensorManager mSensorManager;
	private Bitmap controlsBM;
	Button hideMe;
	float scale = 0f;
	float textHeightDensityPixels = 24.0f;
	WebView webview;
	LinearLayout container;
	private final OnClickListener hideViewOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			setContentView(myImV);

		}

	};
	private boolean hasResumedTour = true;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				if (webview.canGoBack()) {
					webview.goBack();
				} else if (!hasResumedTour) {
					setContentView(myImV);

					hasResumedTour = true;
				} else {
					finish();
				}
				return true;
			}

		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		webview = new WebView(this);
		hideMe = new Button(this);
		hideMe.setText("close");
		hideMe.setBackgroundColor(0xffcddcff);
		hideMe.setOnClickListener(hideViewOnClickListener);
		container = new LinearLayout(this);
		container.setOrientation(LinearLayout.VERTICAL);
		// overlay.addView(container);
		container.addView(hideMe);

		container.addView(webview);
		webview.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// do your handling codes here, which url is the
				// requested url
				// probably you need to open that url rather than
				// redirect:
				view.loadUrl(url);
				return false; // then it is not handled by default
								// action
			}
		});

		// always set metrics for screen width and height
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		controlsHeight = metrics.heightPixels / 12;
		// stuff? needed for sense detector
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensorManager
				.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		// my inner class canvas for drawing bitmaps
		myImV = new MyImageView(this);
		// my inner class for loading bitmaps off main thread
		// cache for next/current/previous bitmaps
		dynamicImages = new LruCache<String, Bitmap>(2);

		lock = true;
		new AsyncLruLoader(myImV).execute(myImV.getResIdFromIndex(myImV.getIndex()));

		controlsBM = BitmapFactory.decodeResource(getResources(), R.drawable.controls3graphic);
		controlsBM = Bitmap.createScaledBitmap(controlsBM, metrics.widthPixels - 1, controlsHeight, true);
		// are we resuming from before or is it brand new
		if (savedInstanceState != null) {

			myImV.resetBM(savedInstanceState.getInt(INDEX), savedInstanceState.getFloat(SAVE_SIZE), savedInstanceState.getInt(CATEGORY),
					savedInstanceState.getBoolean(GUIDE));
		} else {
			// start at sun
			myImV.setBM(0);
		}
		// show our graphic canvas
		setContentView(myImV);
		// register canvas to receive events as defined in this
		myImV.setOnTouchListener(this);

	}

	// /////////////////////////////////////////////////////////////
	public Bitmap getBitmapFromDiskCache(String key) {
		synchronized (mDiskCacheLock) {
			// Wait while disk cache is started from background thread
			while (lock) {
				try {
					mDiskCacheLock.wait();
				} catch (InterruptedException e) {
				}
			}
			if (dynamicImages != null) {
				return dynamicImages.get(key);
			}
		}
		return null;
	}

	// ////////////////////////////////////////////////////////////
	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		switch (arg1.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			fingers = 1;
			x = arg1.getX(0);
			y = arg1.getY(0);
			if (y > controlsHeight && y < metrics.heightPixels - controlsHeight) {

				myImV.dScalar = 0f;
				myImV.repaintOn = false;
			}
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			fingers = 2;
			x2 = arg1.getX(1);
			y2 = arg1.getY(1);
			break;

		case MotionEvent.ACTION_MOVE:
			processImageChanges(arg1);
			break;
		case MotionEvent.ACTION_POINTER_UP:
			fingers--;
			break;
		case MotionEvent.ACTION_UP:
			fingers--;
			if (arg1.getX(0) > metrics.widthPixels - 60 * scale && arg1.getY(0) < textHeightDensityPixels * scale
					&& Math.abs(x - arg1.getX(0)) < 6 && Math.abs(y - arg1.getY(0)) < 6) {
				Uri website = null;
				if (myImV.getIndex() >= 0)
					website = Uri.parse(APOD_BASE + APOD_ADDRESS_TAG[myImV.getIndex()]);
				if (myImV.getIndex() < 0)
					website = Uri.parse(WIKI_BASE + LOCAL_ASTRONOMY[LOCAL_ASTRONOMY.length + myImV.getIndex()]);

				if (website != null) {
					showInfoWebView(website.toString());
				}
			} else if (arg1.getY(0) > metrics.heightPixels - controlsHeight - 20) {
				if (arg1.getX(0) < metrics.widthPixels / 5) {
					myImV.dScalar = -0.005f;
					myImV.repaintOn = true;
				} else if (arg1.getX(0) > 4 * metrics.widthPixels / 5) {

					myImV.dScalar = 0.005f;
					myImV.repaintOn = true;
				} else if (arg1.getX(0) > 2 * metrics.widthPixels / 5 && arg1.getX(0) < 3 * metrics.widthPixels / 5) {
					myImV.displayControlsHelp = !myImV.displayControlsHelp;
					myImV.repaintOn = false;
					myImV.dScalar = 0;
				} else if (arg1.getX(0) > 3 * metrics.widthPixels / 5 && arg1.getX(0) < 4 * metrics.widthPixels / 5) {

					myImV.setBM(1.1f);
				} else if (arg1.getX(0) > 1 * metrics.widthPixels / 5 && arg1.getX(0) < 2 * metrics.widthPixels / 5) {

					myImV.setBM(-1.1f);
				}

			} else {

				myImV.displayControlsHelp = false;
			}
			break;
		}
		myImV.invalidate();
		return true;
	}

	private void showInfoWebView(String website) {
		// LinearLayout superlayout = new LinearLayout(this);
		// ScrollView overlay = new ScrollView(this);
		hasResumedTour = false;
		webview.clearHistory();
		webview.loadUrl(website);
		// webview.setBackgroundColor(0x00000000);
		// webview.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);

		setContentView(container);
	}

	private void processImageChanges(MotionEvent event) {
		switch (event.getPointerCount()) {

		case 1:

			xi = event.getX(0);
			yi = event.getY(0);
			if (Math.abs(x - xi) < 100 && Math.abs(y - yi) < 100) {
				myImV.changePositionBy((x - xi), (y - yi));
				if (myImV.getImageCenterX() > metrics.widthPixels)
					myImV.changePositionBy(-(x - xi), 0);
				if (myImV.getImageCenterY() > metrics.heightPixels)
					myImV.changePositionBy(0, -(y - yi));
				if (myImV.getImageCenterX() < 0)
					myImV.changePositionBy(-(x - xi), 0);
				if (myImV.getImageCenterY() < 0)
					myImV.changePositionBy(0, -(y - yi));
				x = xi;
				y = yi;
			}
			break;
		case 2:

			// current touch points
			xi = event.getX(0);
			yi = event.getY(0);
			x2i = event.getX(1);
			y2i = event.getY(1);
			// if new touch points are further apart than last increase scale

			if (Math.abs(x2i - xi) > Math.abs(x2 - x) && Math.abs(y2i - yi) > Math.abs(y2 - y))
				myImV.changeScalarBy(0.03f);

			if (Math.abs(x2i - xi) < Math.abs(x2 - x) && Math.abs(y2i - yi) < Math.abs(y2 - y))
				myImV.changeScalarBy(-0.03f);

			// last touch points
			x2 = x2i;
			y2 = y2i;
			x = xi;
			y = yi;
			break;
		}
	}

	// ///////////////---------------start inner
	// class-----------------------------------
	// #####################################################
	class MyImageView extends View {
		public boolean displayControlsHelp = false;
		private final float SCALARI = 0.3F;
		private final float SCALARK = 1.9f;
		int index = -1;
		float transX = 0;
		float transY = 0;
		float scalar = SCALARI;
		float dScalar = 0.000f;
		float angle = 10;
		Bitmap bm;
		boolean repaintOn = false;
		float textAlpha = 110;
		float controlsAlpha = 30;
		float textHeight = 0f;
		private Paint textPaint = new Paint();
		String units;
		String info;
		Canvas canvas;

		MyImageView(Context ctx) {
			super(ctx);
			displayControlsHelp = true;
			units = getString(R.string.units);
			info = getString(R.string.info);

			// Convert the dips to pixels
			scale = getContext().getResources().getDisplayMetrics().density;
			// Log.v("scale", "" + scale);
			textHeight = (int) (textHeightDensityPixels * scale + 0.5f);
			textPaint.setTextSize(textHeight);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.view.View#onDraw(android.graphics.Canvas)
		 */
		@Override
		protected void onDraw(Canvas canvas) {
			this.canvas = canvas;
			canvas.drawARGB(255, 0, 0, 0);
			canvas.save();

			canvas.translate(transX, transY);
			canvas.scale(scalar, scalar);
			canvas.drawBitmap(bm, 0, 0, null);

			canvas.restore();

			if (!repaintOn)
				textPaint.setAlpha((int) textAlpha);
			if (index >= 0) {
				String dist = readableNumber(Long.valueOf(IMAGE_NAMES_ARRAY[index].split("_")[1]));
				float textLength = textPaint.measureText(dist);
				canvas.drawText(units, 10 * scale + textLength, textHeight + 2, textPaint);
				textPaint.setAlpha((int) 220);
				canvas.drawText(dist, 5 * scale, textHeight + 2, textPaint);

			} else
				canvas.drawText(LOCAL_ASTRONOMY[LOCAL_ASTRONOMY.length + myImV.getIndex()].split("_")[0], 5 * scale, textHeight + 2,
						textPaint);

			textPaint.setAlpha((int) textAlpha);
			canvas.drawText(info, metrics.widthPixels - 50 * scale, textHeight + 2, textPaint);
			if (repaintOn)
				textPaint.setAlpha((int) (controlsAlpha));
			float bWid = metrics.widthPixels / 5;
			for (int i = 0; i < 5; i++)
				canvas.drawRect(1 + i * bWid, metrics.heightPixels - controlsHeight, (i + 1) * bWid, metrics.heightPixels - 1, textPaint);
			canvas.drawBitmap(controlsBM, 1, metrics.heightPixels - controlsHeight, null);
			if (repaintOn) {
				changeScalarBy(dScalar);
				invalidate();
			}
			if (displayControlsHelp) {
				// Bitmap uparrow = BitmapFactory.decodeResource(getResources(),
				// R.drawable.uparrow25a);
				drawTransparentBackground();
				Paint overlayTextP = new Paint();
				overlayTextP.setARGB(255, 0, 0, 0);
				int size = metrics.heightPixels > metrics.widthPixels ? metrics.heightPixels / 24 : metrics.widthPixels / 28;
				overlayTextP.setTextSize(size);
				int textSize = size + 2;
				canvas.drawText("FEATURES", 5, 4 + textSize * 2, overlayTextP);
				canvas.drawText("-Distance in Light-Years", 5, textSize * 3, overlayTextP);
				canvas.drawText("-Tap \"" + info + "\" for more info", 5, textSize * 4, overlayTextP);
				canvas.drawText("-Touch and drag to pan", 5, textSize * 5, overlayTextP);
				canvas.drawText("-Touch for auto zoom OFF", 5, textSize * 6, overlayTextP);
				canvas.drawText("-Touch to close this guide", 5, textSize * 7, overlayTextP);
				canvas.drawText("-Two finger pinch to zoom", 5, textSize * 8, overlayTextP);
				canvas.drawText("-Shake for a random distance", 5, textSize * 9, overlayTextP);

				int left = 5;
				int over = (int) (50 * scale);
				if (metrics.widthPixels > metrics.heightPixels)
					left = metrics.widthPixels / 2;
				canvas.drawText("<  ", left, metrics.heightPixels - controlsHeight - textSize * 1, overlayTextP);
				canvas.drawText("auto zoom reverse ON", over + left, metrics.heightPixels - controlsHeight - textSize * 1, overlayTextP);
				canvas.drawText("<< ", left, metrics.heightPixels - controlsHeight - textSize * 2, overlayTextP);
				canvas.drawText("jump back one", over + left, metrics.heightPixels - controlsHeight - textSize * 2, overlayTextP);
				canvas.drawText("[]", left, metrics.heightPixels - controlsHeight - textSize * 3, overlayTextP);
				canvas.drawText("display this guide", over + left, metrics.heightPixels - controlsHeight - textSize * 3, overlayTextP);
				canvas.drawText(">>", left, metrics.heightPixels - controlsHeight - textSize * 4, overlayTextP);
				canvas.drawText("jump forward one", over + left, metrics.heightPixels - controlsHeight - textSize * 4, overlayTextP);
				canvas.drawText(">", left, metrics.heightPixels - controlsHeight - textSize * 5, overlayTextP);
				canvas.drawText("auto zoom forward ON", over + left, metrics.heightPixels - controlsHeight - textSize * 5, overlayTextP);
				canvas.drawText("CONTROLS", left, metrics.heightPixels - controlsHeight - textSize * 6, overlayTextP);

			}
		}// ////////////END ONDRAW

		private void drawTransparentBackground() {
			if (canvas == null)
				return;
			Paint overlayP = new Paint();
			overlayP.setARGB(190, 190, 190, 190);
			canvas.drawRect(0, 0, metrics.widthPixels, metrics.heightPixels, overlayP);
		}

		public void changeRotationBy(float f) {
			angle += f;
		}

		// ///////////////////////
		// takes in index point and calculates the R file int from the static
		// final string array
		private int getResIdFromIndex(int ix) {

			String resName = "sun";
			// if at the last local jump to farthest
			if (ix < -LOCAL_ASTRONOMY.length) {
				ix = IMAGE_NAMES_ARRAY.length - 1;
			}
			if (ix < 0)
				resName = LOCAL_ASTRONOMY[LOCAL_ASTRONOMY.length + ix];
			else if (ix >= 0 && ix < IMAGE_NAMES_ARRAY.length)
				resName = IMAGE_NAMES_ARRAY[ix];
			// if at the farthest galaxy restart at sirius
			if (ix >= IMAGE_NAMES_ARRAY.length)
				resName = IMAGE_NAMES_ARRAY[0];
			try {
				Class res = R.drawable.class;
				Field field = res.getField(resName);
				int drawableId = field.getInt(null);
				return drawableId;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return 0;// possible to cause a problem
		}

		// ///////////////////////////////////////////////
		// adds param doWhat to current index then loads an async task to the
		// possible future image and loads the new image from cache
		//
		private void setBM(float doWhat) {

			index += (int) doWhat;
			if (index >= IMAGE_NAMES_ARRAY.length)
				index = 0;
			if (index < -LOCAL_ASTRONOMY.length)
				index = -1;
			if (dynamicImages.get(String.valueOf(getResIdFromIndex((int) (index + doWhat)))) == null)
				new AsyncLruLoader(myImV).execute(getResIdFromIndex((int) (index + doWhat)));

			final String imageKey = String.valueOf(getResIdFromIndex(index));
			bm = getBitmapFromDiskCache(imageKey);
			// bm = dynamicImages.get(imageKey);
			if (bm == null)
				bm = getBitmapFromId(getResIdFromIndex(index));
			// if we have zoomed in set next image with small scale
			if (doWhat == 1)
				setScaleMin();
			// if we have zoomed out set next image with large scale
			else if (doWhat == -1)
				setScaleMax();
			// reposition to center
			transX = metrics.widthPixels / 2 - scalar * (float) (bm.getWidth()) / 2;
			transY = metrics.heightPixels / 2 - scalar * (float) (bm.getHeight()) / 2;
			// calculate a matching color for text
			int red = 0, green = 0, blue = 0;
			int counter = 0;
			int colorLimit = 0;
			for (; counter < 50;) {
				int px = bm.getWidth() / 2 - bm.getWidth() / 6 + 2 * ((int) Math.random() * bm.getWidth()) / 6;
				int py = bm.getHeight() / 2 - bm.getHeight() / 6 + 2 * ((int) Math.random()) / 6;
				int pixel = bm.getPixel(px, py);

				int r = pixel >> 16 & 0xff;
				int g = pixel >> 8 & 0xff;
				int b = pixel & 0xff;

				// if ((r < colorLimit && g < colorLimit) || (r < colorLimit &&
				// b < colorLimit) || (b < colorLimit && g < colorLimit))
				if (r + g + b < colorLimit)
					continue;
				red += r;
				green += g;
				blue += b;
				counter++;
			}
			float colorMax = red > blue ? red : blue;
			colorMax = colorMax > green ? colorMax : green;
			final float MAX = 190;
			final float FACTOR = MAX / colorMax;
			if (colorMax == 0) {
				textPaint.setARGB(90, 150, 90, 230);
				return;
			}

			textPaint.setARGB(90, (int) (FACTOR * red), (int) (FACTOR * green), (int) (FACTOR * blue));
		}

		// ///////////////////////////////////////
		private void resetBM(int i, float sc, int cg, boolean b) {
			scalar = sc;
			setBM(i + 1);
			displayControlsHelp = b;
		}

		// //////////////////////////////////////////////
		// used to create zoom effect
		public void changeScalarBy(float change) {
			scalar += change;
			changePositionBy(change * bm.getWidth() / 2, change * bm.getHeight() / 2);
			if (scalar >= SCALARK) {
				setBM(1);
			}
			if (scalar <= 0.03) {
				setBM(-1);
			}
		}

		// ////////////////////////////////////////
		// used to convert light year long value to a traditional comma
		// separated number
		private String readableNumber(long in) {
			String distance = "";
			char[] digits = ("" + in).toCharArray();
			for (int i = digits.length - 1; i >= 0; i--) {
				distance = digits[i] + distance;
				if (i > 0 && (digits.length - i) % 3 == 0)
					distance = "," + distance;
			}
			return distance;
		}

		public void changePositionBy(float dtrX, float dtrY) {
			transX -= (dtrX);
			transY -= (dtrY);
		}

		private Bitmap getBitmapFromId(int idFromR) {
			return BitmapFactory.decodeResource(getResources(), idFromR);
		}

		public int getIndex() {
			return index;
		}

		public void jumpPoint() {
			int max = IMAGE_NAMES_ARRAY.length;
			int jump = (int) (Math.random() * max) - index;
			setBM(jump);
		}

		public double getImageCenterX() {
			return transX + 0.5 * scalar * bm.getWidth();
		}

		public double getImageCenterY() {
			return transY + 0.5 * scalar * bm.getHeight();
		}

		private void setScaleMax() {
			// TODO Auto-generated method stub
			scalar = SCALARK;
		}

		private void setScaleMin() {
			// TODO Auto-generated method stub
			scalar = SCALARI;
		}
	}

	// #############################################
	// ///////////---------------------end inner
	// class-----------------------------------------

	// /////////////////////////////////////////////////////////////////////////////

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// ignore

	}

	// works with setup in onCreate
	// snagged code from stackoverflow submission
	@Override
	public void onSensorChanged(SensorEvent event) {
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];

		float gX = x / gravityEarth;
		float gY = y / gravityEarth;
		float gZ = z / gravityEarth;
		// good to know, thanks
		// G-Force will be 1 when there is no movement. (gravity)
		double gForce = Math.sqrt(gX * gX + gY * gY + gZ * gZ);

		if (gForce > shakeThresholdInGForce) {
			myImV.jumpPoint();
			myImV.invalidate();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save the user's state
		savedInstanceState.putInt(INDEX, myImV.index);
		savedInstanceState.putFloat(SAVE_SIZE, myImV.scalar);
		savedInstanceState.putInt(CATEGORY, 0);
		savedInstanceState.putBoolean(GUIDE, myImV.displayControlsHelp);
		// if you say so ...
		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager
				.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		mSensorManager.unregisterListener(this);
		super.onPause();
	}

	// ////////////////////////
	public void addBitmapToMemoryCache(String key, Bitmap val) {

		if (getBitmapFromMemCache(key) == null) {
			dynamicImages.put(key, val);
		}
	}

	// /////////
	private Object getBitmapFromMemCache(String key) {
		// TODO Auto-generated method stub
		return dynamicImages.get(key);
	}

	// ////////////inner class
	class AsyncLruLoader extends AsyncTask<Integer, Void, Void> {

		private Boolean imageLoadLock = new Boolean(false);
		private final WeakReference<View> imageViewReference;
		private int data = 0;

		public boolean isLoading() {
			return imageLoadLock;
		}

		public AsyncLruLoader(View bmView) {
			// Use a WeakReference to ensure the ImageView can be garbage
			// collected
			imageViewReference = new WeakReference<View>(bmView);
		}

		// Decode image in background.
		@Override
		protected Void doInBackground(Integer... params) {
			imageLoadLock = true;
			// passed in the R file int value decoded from index point
			data = params[0];
			final String imageKey = String.valueOf(params[0]);
			synchronized (mDiskCacheLock) {
				addBitmapToMemoryCache(imageKey, myImV.getBitmapFromId(data));

				lock = false; // Finished initialization
				mDiskCacheLock.notifyAll(); // Wake any waiting threads
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			imageLoadLock = false;
			lock = false;
		}

	}

}
