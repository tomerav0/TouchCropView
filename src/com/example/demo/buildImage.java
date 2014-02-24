package com.example.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class buildImage extends Activity implements OnTouchListener, OnDragListener, OnLongClickListener, OnClickListener
{
	private LinearLayout slider;
	private RelativeLayout board, bigParent;
	Database data;
	String[][] imagesArray;
	float mX, mY;
	Button save;
	ImageView currentDragedView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.build_image);
	
		//drawing = (SignatureViewModed) findViewById(R.id.myTest);
		slider = (LinearLayout) findViewById(R.id.imageSlider);
		board = (RelativeLayout) findViewById(R.id.borad);
		bigParent = (RelativeLayout) findViewById(R.id.bigParent);
		save = (Button) findViewById(R.id.button1);
		
		bigParent.setOnTouchListener(this);
		save.setOnClickListener(this);
		
		bigParent.setDrawingCacheEnabled(true);
		
		data = new Database(this);
		data.open();
		
		setImagesSlider();
		
	}
	
	public void setImagesSlider()
	{
		imagesArray = data.getAllImages();
		
		if(imagesArray == null || imagesArray.length < 1)
			return;
		
		int length = imagesArray.length;
		
		for(int i = 0; i < length; i++)
		{
			ImageView image = new ImageView(this);
			image.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
			
			image.setImageBitmap(decodeBase64(imagesArray[i][1]));
			image.setOnDragListener(this);
			image.setOnLongClickListener(this);
			
			image.setId(i);		
			slider.addView(image);
		}
	}
	
	  public  Bitmap decodeBase64(String input) {
			byte[] decodedByte = Base64.decode(input, 0);
			return BitmapFactory
					.decodeByteArray(decodedByte, 0, decodedByte.length);
		}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// TODO Auto-generated method stub

		if (event.getAction() == MotionEvent.ACTION_UP) 
		{
			currentDragedView = null;
			return true;
		}
		
		if(currentDragedView != null)
		{
			float height = (float) currentDragedView.getHeight();
			float width = (float) currentDragedView.getWidth();

			mX = event.getX();
		    mY = event.getY();
		    
		    currentDragedView.setX(mX - (height/2));
			currentDragedView.setY(mY - (width/2));
			return true;
		}

		if (event.getAction() == MotionEvent.ACTION_DOWN) 
		{
			
			/**
	        ClipData data = ClipData.newPlainText("tests", "test");
	        DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
	        view.startDrag(data, shadowBuilder, view, 0);
	        return true;
	        */
			
			return true;
			
	      } else {
	        return false;
	      }
	}

	@Override
	public boolean onDrag(View v, DragEvent event) {
		
	      switch (event.getAction()) {
	      case DragEvent.ACTION_DRAG_STARTED:
	      {
	        return true;
	      }
	      case DragEvent.ACTION_DRAG_ENTERED:
	      {
	    	  return true;
	      }
	      case DragEvent.ACTION_DRAG_EXITED:
	      {
	    	  return true;
	      }
	      case DragEvent.ACTION_DROP:
	      {
	    	  addNewImage(v.getId());
	    	   
	    	  return true;
	      }
	      case DragEvent.ACTION_DRAG_ENDED:
	      {
	    	  addNewImage(v.getId());
	    	  return true;
	      }

	      case DragEvent.ACTION_DRAG_LOCATION:
	      { 
	    
	    	  return true;
	      }
	      
	      default:
	        break;
	      }
	      return true;
	    }
	
	public void addNewImage(int id)
	{
		ImageView image = new ImageView(this);
		image.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
		
		Bitmap bitmap = decodeBase64(data.getImage(imagesArray[id][0]));
		
		image.setImageBitmap(bitmap);

		board.addView(image);
	
		image.setX(mX);
		image.setY(mY);
		
		Log.i("position", "x " + mX + " y " + mY);	
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		
		createDraggedImageView(v, v.getX(), v.getY());
		
		return false;
	}
	
	public void createDraggedImageView(View v, final float x, final float y)
	{
		currentDragedView = new ImageView(this);
		currentDragedView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
		
		Bitmap bitmap = decodeBase64(data.getImage(imagesArray[v.getId()][0]));
		
		currentDragedView.setImageBitmap(bitmap);

		board.addView(currentDragedView);
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				//currentDragedView.setX(x);
				//currentDragedView.setY(y);
			}
		}, 150);
		
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
		//Bitmap bitmap = board.getDrawingCache();
	
		showCutImage();
		
	}
	
	
	public void showCutImage()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		View prefView = View.inflate(this,R.layout.image_show, null);
		
		ImageView image = (ImageView) prefView.findViewById(R.id.imageView1);

		board.buildDrawingCache();
		
		image.setImageBitmap(board.getDrawingCache());
	
		builder.setView(prefView);
		
		builder.show();
		
	}

}
