package com.example.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class TouchCropView extends ImageView implements OnTouchListener {
    private Paint paint;
    public static List<PointFloat> PointFloats;
    public static List<PointFloat> pathPoints;
    
    PointFloat[] PointFloatsNew;
    
    Bitmap bitmapMain;
           
    Context mContext;
    
    private float leftX = 0;
    private float rightX = 0;
    private float upY = 0;
    private float downY = 0;
    
    float scaledHeight;
	float scaledWidth;

    public TouchCropView(Context c) {
        super(c);

        mContext = c;
        setFocusable(true);
        setFocusableInTouchMode(true);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
   
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);

        this.setOnTouchListener(this);
        
        PointFloats = new ArrayList<PointFloat>();
        pathPoints = new ArrayList<PointFloat>();

      
    }

    public TouchCropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setFocusable(true);
        setFocusableInTouchMode(true);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);

        this.setOnTouchListener(this);
        
        PointFloats = new ArrayList<PointFloat>();
        pathPoints = new ArrayList<PointFloat>();
    }

    public void setBitmap(Bitmap bit)
    {
    	bitmapMain = bit;
    }
    
    public void setScaledSize(float height, float width)
    {
    	scaledHeight = height;
    	scaledWidth = width;
    }
 
    public void onDraw(Canvas canvas) {
    	super.onDraw(canvas);
    
        Path path = new Path();
        boolean first = true;
        int length = pathPoints.size();
        
        for (int i = 0; i < length; i += 2) {
            PointFloat PointFloat = pathPoints.get(i);
            if (first) {
                first = false;
                path.moveTo(PointFloat.x, PointFloat.y);
            } else if (i < length - 1) {
                PointFloat next = pathPoints.get(i + 1);
                path.quadTo(PointFloat.x, PointFloat.y, next.x, next.y);
            } else {
                path.lineTo(PointFloat.x, PointFloat.y);
            }
        }
        canvas.drawPath(path, paint);
    }

    public boolean onTouch(View view, MotionEvent event) {
    	
      
    	if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_OUTSIDE || event.getAction() == MotionEvent.ACTION_CANCEL) 
    	{
    		 return true;
    	}
    	    	     
    	Log.i("location on screen", "event.getX() " + event.getX() + "  event.getY()  " + event.getY());
    	
        float intX = event.getX() * scaledWidth;
        float intY = event.getY() * scaledHeight;
      
        Log.i("location on bitmap", "intX " + intX + "  intY  " + intY);
        
        if(event.getAction() == MotionEvent.ACTION_DOWN)
        {
        	leftX = intX;
        	rightX = intX;
        	downY = intY;
        	upY = intY;
        }
        
        PointFloat PointFloat = new PointFloat(intX, intY);

        if(intX < leftX)
        	leftX =  intX;
        if(intX > rightX)
           	rightX = intX;
        if(intY > downY)
        	downY =  intY;
        if(intY < upY)
        	upY = intY;

        PointFloats.add(PointFloat);
        
        pathPoints.add(new PointFloat(event.getX(), event.getY()));

        invalidate();

        return true;
    }

    public void clear() 
    {   	

        PointFloats = new ArrayList<PointFloat>();
        pathPoints = new ArrayList<PointFloat>();
        
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        
        invalidate();
    }
  
    public Bitmap getCropedBitmap()
    {
    	bitmapMain = convertToMutable(bitmapMain);
    	
    	PointFloatsNew = PointFloats.toArray(new PointFloat[PointFloats.size()]);
    	
    	PolygonHelper polygonHelper = new PolygonHelper(PointFloatsNew);
 
    	for(int heightPixel = (int) upY; heightPixel <= (int) downY; heightPixel++)
    		for(int widthPixel = (int) leftX; widthPixel <= (int) rightX; widthPixel++)
    		{ 			
    				if(!polygonHelper.contains(widthPixel, heightPixel))
    					bitmapMain.setPixel((int) widthPixel, (int) heightPixel, Color.TRANSPARENT);			
    		}
    	
    	Matrix matrix = new Matrix();

    	Bitmap croppedBitmap = Bitmap.createBitmap(bitmapMain, (int) leftX + 1, (int) upY + 1, (int)  (rightX - leftX), (int) (downY - upY), matrix, false);

    	return croppedBitmap;
    }

    public boolean contains(PointFloat test) {
        int i;
        int j;
         
        boolean result = false;
        for (i = 0, j = PointFloatsNew.length - 1; i < PointFloatsNew.length; j = i++) {
          if ((PointFloatsNew[i].y > test.y) != (PointFloatsNew[j].y > test.y) &&
              (test.x < (PointFloatsNew[j].x - PointFloatsNew[i].x) * (test.y - PointFloatsNew[i].y) / (PointFloatsNew[j].y-PointFloatsNew[i].y) + PointFloatsNew[i].x)) {
            result = !result;
           }
        }
        return result;
      }
    
    public static Bitmap convertToMutable(Bitmap imgIn) {
        try {
            //this is the file going to use temporally to save the bytes. 
            // This file will not be a image, it will store the raw image data.
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "temp.tmp");

            //Open an RandomAccessFile
            //Make sure you have added uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
            //into AndroidManifest.xml file
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

            // get the width and height of the source bitmap.
            int width = imgIn.getWidth();
            int height = imgIn.getHeight();
            Config type = imgIn.getConfig();

            //Copy the byte to the file
            //Assume source bitmap loaded using options.inPreferredConfig = Config.ARGB_8888;
            FileChannel channel = randomAccessFile.getChannel();
            MappedByteBuffer map = channel.map(MapMode.READ_WRITE, 0, imgIn.getRowBytes()*height);
            imgIn.copyPixelsToBuffer(map);
            //recycle the source bitmap, this will be no longer used.
            
            //imgIn.recycle();
            
            System.gc();// try to force the bytes from the imgIn to be released

            //Create a new bitmap to load the bitmap again. Probably the memory will be available. 
            imgIn = Bitmap.createBitmap(width, height, type);
            map.position(0);
            //load it back from temporary 
            imgIn.copyPixelsFromBuffer(map);
            //close the temporary file and channel , then delete that also
            channel.close();
            randomAccessFile.close();

            // delete the temp file
            file.delete();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } 

        return imgIn;
    }
   
    public class PolygonHelper
    {
    	// Polygon coodinates.
    	private  int[] polyY, polyX;

    	// Number of sides in the polygon.
    	private int polySides;

    	//

    	/**
    	 * Default constructor.
    	 * @param px Polygon y coods.
    	 * @param py Polygon x coods.
    	 * @param ps Polygon sides count.
    	 */
    	public PolygonHelper( final int[] px, final int[] py, final int ps ) {
    	    polyX = px;
    	    polyY = py;
    	    polySides = ps;
    	}
    	
    	public PolygonHelper (PointFloat[] PointFloats){
            polySides = PointFloats.length;
            polyY = new int[polySides];
            polyX = new int[polySides];

            for(int i = 0; i < polySides; i++){
                polyY[i] = Math.round(PointFloats[i].y);
                polyX[i] = Math.round(PointFloats[i].x);
            }
        }
    	
    	/**
    	 * Checks if the Polygon contains a PointFloat.
    	 * @see "http://alienryderflex.com/polygon/"
    	 * @param x PointFloat horizontal pos.
    	 * @param y PointFloat vertical pos.
    	 * @return PointFloat is in Poly flag.
    	 */
    	public boolean contains( final float x, final float y ) {

    	    boolean oddTransitions = false;
    	    for( int i = 0, j = polySides -1; i < polySides; j = i++ ) {
    	        if( ( polyY[ i ] < y && polyY[ j ] >= y ) || ( polyY[ j ] < y && polyY[ i ] >= y ) ) {
    	            if( polyX[ i ] + ( y - polyY[ i ] ) / ( polyY[ j ] - polyY[ i ] ) * ( polyX[ j ] - polyX[ i ] ) < x ) {
    	                oddTransitions = !oddTransitions;          
    	            }
    	        }
    	    }
    	    return oddTransitions;
    	}

    }
   
    
   }
