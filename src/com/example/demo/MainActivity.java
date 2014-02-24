package com.example.demo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private TouchCropView drawing;
	 private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
	 public static final int MEDIA_TYPE_IMAGE = 1;
	 private static final String IMAGE_DIRECTORY_NAME = "demo pictures";
	 private Uri fileUri;
	 Bitmap bitbit;
	 Bitmap cameraBitMap;
	 Database data;
	 
	 int screenHeight;
	 int screenWidth;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		//drawing = (SignatureViewModed) findViewById(R.id.myTest);
		findViewById(R.id.button1).setOnClickListener(this);
		findViewById(R.id.button2).setOnClickListener(this);
		findViewById(R.id.button3).setOnClickListener(this);
		
		drawing = (TouchCropView) findViewById(R.id.images);
		
		drawing.getViewTreeObserver().addOnGlobalLayoutListener(
			     new ViewTreeObserver.OnGlobalLayoutListener() {
			          public void onGlobalLayout() {
			        
			        	  	if(cameraBitMap != null)
			        	  	{
			        	  		
							float scaledHeight = (float) cameraBitMap.getHeight()/ (float) drawing.getHeight();
							float scaledWidth = (float) cameraBitMap.getWidth()/ (float) drawing.getWidth();

							drawing.setScaledSize(scaledHeight, scaledWidth);
							
			        	  	}
			          }
			     });
		
		data = new Database(this);
        data.open();
        
        checkScreenSize();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public interface ImageSaverListener
    {
    	void saveImage();
    }

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
		switch(arg0.getId())
		{
		
		case R.id.button1:
		{
			drawing.clear();
			drawing.setBitmap(cameraBitMap);
			break;
		}
		
		case R.id.button2:
		{
			if(isDeviceSupportCamera())
				captureImage();
			else
				Toast.makeText(this, "No camera", Toast.LENGTH_SHORT).show();
			break;
		}
		
		case R.id.button3:
		{
			new GetVideos().execute();
			break;
		}
		
		}
		
	}
	
	private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
	
	private void captureImage() {
	
	    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	 
	    fileUri = getOutputMediaFileUri();
	    
	    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

	    // start the image capture Intent
	    startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
	}

	public Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }
 
    /**
     * returning image / video
     */
    private static File getOutputMediaFile() {
 
        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);
 
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }
 
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
       
         mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
     
        return mediaFile;
    }
	
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
 
        // save file url in bundle as it will be null on scren orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }
 
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
 
        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }
       
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent data2) {

		if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
			
			if (resultCode == RESULT_OK)
			{

				String filePath = fileUri.getPath();
				cameraBitMap = decodeSampledBitmapFromPath(filePath, true);
				
				Log.i("cameraBitMap", "cameraBitMap height " + cameraBitMap.getHeight() + " cameraBitMap width " + cameraBitMap.getWidth());
				
				drawing.setBitmap(cameraBitMap);
				
				drawing.setImageBitmap(cameraBitMap);
			
			}
					
		}
		
			super.onActivityResult(requestCode, resultCode, data2);
		}
	
	@SuppressLint("NewApi")
	public void checkScreenSize()
	{
		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE))
				.getDefaultDisplay();
		

		if (android.os.Build.VERSION.SDK_INT >= 13) {
			Point size = new Point();
			display.getSize(size);
			screenHeight = size.y;
			screenWidth = size.x;

		} else
		{
			screenWidth = display.getWidth();
			screenHeight = display.getHeight();
		}

	}
	
	public Bitmap decodeSampledBitmapFromPath(String path, boolean isFullScreen) {

	 	Bitmap bmp = null;
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
	
		BitmapFactory.decodeFile(path, options);
					
		if(isFullScreen)
			options.inSampleSize = calculateInSampleSize(options, getScreenWidth() , getScreenHeight());
		else
			options.inSampleSize = calculateInSampleSize(options, 200, 200);
		       
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		bmp = BitmapFactory.decodeFile(path, options);
	
		return bmp;
		}
	
	public int calculateInSampleSize(BitmapFactory.Options options,
		    int reqWidth, int reqHeight) {

		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
		    if (width > height) {
		        inSampleSize = Math.round((float) height / (float) reqHeight);
		    } else {
		        inSampleSize = Math.round((float) width / (float) reqWidth);
		     }
		 }
		 return inSampleSize;
		}
	
	@SuppressLint("NewApi")
	public int getScreenHeight()
	{
		Display display = ((WindowManager) this.getSystemService(WINDOW_SERVICE))
				.getDefaultDisplay();

		int height = 0;
		
		if (android.os.Build.VERSION.SDK_INT >= 13) {
			Point size = new Point();
			display.getSize(size);
			height = size.y;

		} else {
			height = display.getHeight();
			
		}
		
		return height;
	}
	
	@SuppressLint("NewApi")
	public int getScreenWidth()
	{
		Display display = ((WindowManager) this.getSystemService(WINDOW_SERVICE))
				.getDefaultDisplay();

		int width = 0;
		
		if (android.os.Build.VERSION.SDK_INT >= 13) {
			Point size = new Point();
			display.getSize(size);
			
			width = size.x;
		} else {
			
			width = display.getWidth();
		}
		
		return width;
	}
	
	public void showCutImage()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		View prefView = View.inflate(this,R.layout.image_show, null);
		
		ImageView image = (ImageView) prefView.findViewById(R.id.imageView1);

		image.setImageBitmap(bitbit);
	
		builder.setPositiveButton("save", new DialogInterface.OnClickListener(
				) {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				data.setImage(encodeTobase64(bitbit));
			}
		});
		
		builder.setNegativeButton("cance", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
		
		builder.setView(prefView);
		
		builder.show();
		
	}

	class GetVideos extends AsyncTask<String, Void, Void> {
		
		ProgressDialog pDialog;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage("proccessing");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		
		protected Void doInBackground(String... args) {

			bitbit = drawing.getCropedBitmap();
			return null;
		}

		protected void onPostExecute(Void file_url) {

			if(pDialog != null)
				pDialog.dismiss();
			
			showCutImage();
			
		}
	}
	
	 public static String encodeTobase64(Bitmap image) {
			Bitmap immagex = image;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			immagex.compress(Bitmap.CompressFormat.PNG, 100, baos);
			byte[] b = baos.toByteArray();
			String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

			return imageEncoded;
		}
	    
	  
	

}
