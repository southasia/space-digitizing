package com.khajenasir.spacedigitizing;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class BuildingManager extends Activity
{
	public static final int MENU_EDIT = Menu.FIRST + 1;
	public static final int MENU_REMOVE = Menu.FIRST + 2;
	
	private DatabaseHelper db=null;
	ArrayAdapter<String> adaptor;
	private List<Building> buildings = new ArrayList<Building>();
	String[] items;
	ListView list;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buildingmanager); 
        
        list = (ListView)findViewById(R.id.listViewBuildingManager);
    }
    
	public void onResume() 
	{
		super.onResume();

		try 
        {           
        	db = new DatabaseHelper(this);
        	      	
        	adaptor = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        	loadBuildings();
        	
        	list.setAdapter(adaptor);
    		registerForContextMenu(list);
		} 
        catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
    
	private void loadBuildings()
	{
		try 
		{		
			buildings.clear();
			items = null;
			
			String[] select = {"ID","Name","Shape","CategoryID","ParentID"};
			Cursor cursor = db.SelectTable("tbl_building", select, "ID>=0", "ID");
			cursor.moveToFirst();
			
	        for(int i=0;i<cursor.getCount();i++)
	        {
	        	Building newBuilding = new Building();
	        	newBuilding.ID = cursor.getInt(0);
	        	newBuilding.Name = cursor.getString(1);
	        	newBuilding.Shape = cursor.getString(2);
	        	newBuilding.CategoryID = cursor.getInt(3);
	        	buildings.add(newBuilding);
	        	cursor.moveToNext();
	        }
	        
			//items = new String[buildings.size()];
			for(int i = 0; i< buildings.size(); i++)
				adaptor.add(buildings.get(i).Name);
				//items[i] = buildings.get(i).Name;
		}  
		catch (Exception e) 
		{
			e.printStackTrace();
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
		AdapterView.AdapterContextMenuInfo info= (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

		switch (item.getItemId()) 
		{		
			case MENU_EDIT:
				
				try
				{
					Intent i = new Intent().setClass(this, newBuilding.class);
					i.putExtra("BuildingID", buildings.get(info.position).ID);
    				startActivity(i);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
				return(true);
			
			case MENU_REMOVE:
				
				try
				{
					db.DeleteRow("tbl_building", "ID=" + buildings.get(info.position).ID);
					db.DeleteRow("tbl_buildingfloor", "BuildingID=" + buildings.get(info.position).ID);
					db.DeleteRow("tbl_buildingpoint", "BuildingID=" + buildings.get(info.position).ID);
					
					adaptor.remove(buildings.get(info.position).Name);
					buildings.remove(info.position);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
				return(true);
		}
		
		return(super.onContextItemSelected(item));
	}
	
    public void menu_click(View v)
    {
    	Intent i;
    	
    	switch(v.getId())
    	{
    		case R.id.buttonNewBuilding :
    			i = new Intent().setClass(this, newBuilding.class);
    			startActivity(i);
    			break;
    	}
    }
    
    
}