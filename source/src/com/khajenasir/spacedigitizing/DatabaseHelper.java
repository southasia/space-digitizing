package com.khajenasir.spacedigitizing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper
{
	private static String DATABASE_NAME="SpaceDigitizingDB.db";
	private static String DATABASE_PATH = "/data/data/com.khajenasir.spacedigitizing/databases/";
	private Context myContext=null;
	SQLiteDatabase db;
	
	
	public DatabaseHelper(Context context) 
	{
		super(context, DATABASE_NAME, null, 1);
		this.myContext = context;
		
    	if(!checkDataBase())
    	{
    		//empty database will be created into the default system path 
            //of your application so we are gonna be able to overwrite that database with our database.
        	this.getReadableDatabase();
   
        	try 
        	{ 
    			copyDataBase();
    			
    			//copyPictures();
    		} 
        	catch (Exception e) 
        	{
        		e.printStackTrace();
        		throw new Error("Error copying database");
        	}
    	}
	}

	private boolean checkDataBase()
	{
    	SQLiteDatabase checkDB = null;
 
    	try
    	{
    		String myPath = DATABASE_PATH + DATABASE_NAME;
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    	}
    	catch(SQLiteException e)
    	{
    		e.printStackTrace();
    	}
 
    	if(checkDB != null)
    	{
    		checkDB.close();
    	}
 
    	return (checkDB != null ? true : false);
    }
	
	private void copyDataBase()
	{
		try
		{
    	InputStream myInput = myContext.getAssets().open(DATABASE_NAME);
    	String outFileName = DATABASE_PATH + DATABASE_NAME;
    	OutputStream myOutput = new FileOutputStream(outFileName);
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = myInput.read(buffer))>0)
    	{
    		myOutput.write(buffer, 0, length);
    	}

    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
    }
	
//	private void copyPictures() 
//	{
//		
//		try
//		{
//			String fullPath = "/data/data/com.sabaware.TourNama/picture";
//            File dir = new File(fullPath);
//            if (!dir.exists())
//                dir.mkdir();
//			AssetManager assetManager = myContext.getAssets();
//			String[] files = assetManager.list("picture");
//			for(String filename : files) 
//			{
//				InputStream in = assetManager.open("picture/" + filename);
//				//make "files" default folder
//		    	//FileOutputStream out = myContext.openFileOutput(filename, Context.MODE_PRIVATE);
//		    	OutputStream out = new FileOutputStream(fullPath + "/" + filename);
//		    	byte[] buffer = new byte[1024];
//		    	int length;
//		    	while ((length = in.read(buffer)) != -1)
//		    	{
//		    		out.write(buffer, 0, length);
//		    	}
//		
//		    	in.close();
//		    	in = null;
//		    	out.flush();
//		    	out.close();
//		    	out = null;		    	
//			}
//		}
//		catch(Exception ex)
//		{
//			ex.printStackTrace();
//		}
//	}
 	
	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		this.db = db;
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{}
	
	public void Query(String query)
	{
		db.execSQL(query);
	}
	
	public Cursor SelectTable(String table, String[] select, String where, String order)
	{
		return getReadableDatabase().query(table, select, where, new String[]{}, "", "", order);
	}
	
	public void DeleteRow(String table, String where)
	{
		getWritableDatabase().delete(table, where, new String[]{});
	}
	
	public long InsertRow(String table, String fields[], String values[])
	{		 
		ContentValues value = new ContentValues();
		for(int i=0;i<fields.length;i++)
			value.put(fields[i], values[i]);
		return getWritableDatabase().insert(table, null, value);
	}
	
	public void UpdateRow(String table, String fields[], String values[], String where)
	{
		ContentValues value = new ContentValues();
		for(int i=0;i<fields.length;i++)
			value.put(fields[i], values[i]);
		getWritableDatabase().update(table, value, where, new String[]{});
	}	

}
