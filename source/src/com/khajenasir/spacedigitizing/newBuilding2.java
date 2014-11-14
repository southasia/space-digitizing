package com.khajenasir.spacedigitizing;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class newBuilding2 extends Activity 
{
	public static final int MENU_EDIT = Menu.FIRST + 1;
	public static final int MENU_REMOVE = Menu.FIRST + 2;
	
	Button BtnNew, BtnNext;
	private DatabaseHelper db=null;
	ListView list;
	private List<String> floors = new ArrayList<String>();
    String[] floorItems;
	ArrayAdapter<String> adaptor;
    
	int buildingID = -1;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newbiulding2);     
        
        BtnNew = (Button)findViewById(R.id.buttonNewFloor);
        BtnNext = (Button)findViewById(R.id.buttonNext);
        list = (ListView)findViewById(R.id.listViewFloors);
        
		try 
        {
			floors.clear();
			floorItems = null;
            
        	db = new DatabaseHelper(this);
        	

        	//if(floorItems.length == 0)
    		adaptor = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        	loadfloors();
        	//else
        		//adaptor = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, floorItems);  
        		
        	list.setAdapter(adaptor);
    		registerForContextMenu(list);
		} 
        catch (Exception e) 
		{
			e.printStackTrace();
		}
    }
    
    private void loadfloors()
    {
        if(getIntent().getExtras() != null)
        {
        	buildingID = getIntent().getExtras().getInt("BuildingID");
        	//Fill Mode
        	
			String[] select = {"ID","BuildingID","Name"};
			Cursor cursor = db.SelectTable("tbl_buildingfloor", select, "BuildingID = " + buildingID, "ID");
			cursor.moveToFirst();
			
	        for(int i=0;i<cursor.getCount();i++)
	        {
//	        	Floor newFloor = new Floor();
//	        	newFloor.ID = cursor.getInt(0);
//	        	newFloor.BuildingID = cursor.getInt(1);
//	        	newFloor.Name = cursor.getString(2);
	        	floors.add(cursor.getString(2));
	        	cursor.moveToNext();
	        }
	        
			//floorItems = new String[floors.size()];
			for(int i = 0; i< floors.size(); i++)
				adaptor.add( floors.get(i));
				//floorItems[i] = floors.get(i);			
        }
			  	
    }
    
    @Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenu.ContextMenuInfo menuInfo) 
	{
		menu.add(Menu.NONE, MENU_EDIT, Menu.NONE, "Edit");
		menu.add(Menu.NONE, MENU_REMOVE, Menu.NONE, "Remove");
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) 
	{
		final AdapterView.AdapterContextMenuInfo info= (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

		switch (item.getItemId()) 
		{		
			case MENU_EDIT:
				
				try
				{
	    			final View addView=getLayoutInflater().inflate(R.layout.add, null);
	    			EditText txtfloorName = (EditText)addView.findViewById(R.id.title);
	    			txtfloorName.setText(floors.get(info.position));
	    			
	    			new AlertDialog.Builder(this)
	    			.setTitle("Add a Floor")
	    			.setView(addView)
	    			.setPositiveButton("OK", new DialogInterface.OnClickListener() 
	    			{
	    				public void onClick(DialogInterface dialog,	int whichButton) 
	    				{
	    					EditText title=(EditText)addView.findViewById(R.id.title);	
	    					
	    					adaptor.remove(floors.get(info.position));
	    					floors.remove(info.position);
	    					adaptor.insert(title.getText().toString(), info.position);
	    					floors.add(info.position, title.getText().toString());
	    				}
	    			})
	    			.setNegativeButton("Cancel", null)
	    			.show();
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
				return(true);
			
			case MENU_REMOVE:
				
				try
				{
					adaptor.remove(floors.get(info.position));
					floors.remove(info.position);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
				return(true);
		}
		
		return(super.onContextItemSelected(item));
	}
	
    
    public void onclick(View v)
    {
    	switch(v.getId()) 
    	{
    		case R.id.buttonNext:
    			
    			//Save Preference
            	//SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
            	//SharedPreferences.Editor editor = prefs.edit();
            	
            	//editor.putString("unit1", txtBuilding1.getText().toString());

                //editor.commit();
                
    			Intent i = new Intent().setClass(this, newBuilding3.class);
    			floorItems = new String[floors.size()];
    			for(int k = 0; k< floors.size(); k++)
    				floorItems[k] = floors.get(k);	
    			i.putExtra("floors", floorItems);
    			i.putExtra("BuildingID", buildingID);
    			startActivity(i); 
    			finish();
    			break;
    		
    		case R.id.buttonNewFloor:
    			final View addView=getLayoutInflater().inflate(R.layout.add, null);
    			
    			new AlertDialog.Builder(this)
    			.setTitle("Add a Floor")
    			.setView(addView)
    			.setPositiveButton("OK", new DialogInterface.OnClickListener() 
    			{
    				public void onClick(DialogInterface dialog,	int whichButton) 
    				{
    					try
    					{
    						EditText title=(EditText)addView.findViewById(R.id.title);
    						adaptor.add(title.getText().toString());
    						floors.add(title.getText().toString());
    					}
    					catch(Exception ex)
    					{
    						ex.printStackTrace();
    					}
    				}
    			})
    			.setNegativeButton("Cancel", null)
    			.show();
    			break;
    			
    	}
    }
}
