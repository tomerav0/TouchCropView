package com.example.demo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database {

	public static final String DATABASE_NAME = "db";
	public static final int DATABASE_VERSION = 1;
	
	public static final String COLUMN_MAIN_ID = "main_id";	
	public static final String COLUMN_BITMAP_STRING = "bitmap_string";
	
	public static final String TABLE_IMAGES = "tabe_images";

	
	private DbHelper ourHelper;
	private final Context ourContext;
	private SQLiteDatabase ourDatabase;

	private static class DbHelper extends SQLiteOpenHelper {

		public DbHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION); //
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
		
			db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_IMAGES + " ("  					
					+ COLUMN_MAIN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
					+ COLUMN_BITMAP_STRING + " STRING);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
					

			onCreate(db);
		}
	}

	public Database(Context c) {
		ourContext = c;
	}

	public Database open() throws SQLException { // open the database to
						// writing to database
		ourHelper = new DbHelper(ourContext);
		ourDatabase = ourHelper.getWritableDatabase();
		return this;
	}

	public void close() { // closing the database for writing, avoids error.
		ourHelper.close();
	}

	public boolean isOpen()
	{
		if(ourDatabase.isOpen())
			return true;
		return false;
	}
	
	public void setImage(String image)
	{
	
		ContentValues cv = new ContentValues();

		cv.put(COLUMN_BITMAP_STRING, image);
		ourDatabase.insert(TABLE_IMAGES, null, cv);	
	}
	
	public String[][] getAllImages()
	{
		String[] columns = new String[] {COLUMN_MAIN_ID, COLUMN_BITMAP_STRING};
		
		Cursor c = ourDatabase.query(TABLE_IMAGES, columns, null,null, null, null, null);
		
		if(c.getCount() < 1)
		{
			c.close();
			return null;
		}
		
		String[][] list = new String[c.getCount()][2];
		
		int id = c.getColumnIndex(COLUMN_MAIN_ID);
		int image = c.getColumnIndex(COLUMN_BITMAP_STRING);
		
		int p = 0;
		
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
		{
			list[p][0] = c.getString(id);
			list[p][1] = c.getString(image);
			p++;
		}
		
		c.close();
		return list;
	}
	
	public String getImage(String id)
	{
		String[] columns = new String[] {COLUMN_MAIN_ID, COLUMN_BITMAP_STRING};
		
		String whereClause = COLUMN_MAIN_ID + "= ?";
		
		String[] args = new String[]{id};
		
		Cursor c = ourDatabase.query(TABLE_IMAGES, columns, whereClause,args, null, null, null);
		
		if(c.getCount() < 1)
		{
			c.close();
			return null;
		}
		
		c.moveToFirst();
		
		int image = c.getColumnIndex(COLUMN_BITMAP_STRING);
		
		String mImage = c.getString(image);
		
		c.close();
		
		return mImage;
		
	}
	
	
	
	
}
