package com.khajenasir.spacedigitizing;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class Navigate_buildinglist extends Activity
{
	private DatabaseHelper db=null;
	ArrayAdapter<String> adaptor;
	private List<Building> buildings = new ArrayList<Building>();
	ListView list;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigate_buildinglist);
        
        list = (ListView)findViewById(R.id.listViewNavBuilding);
        
		list.setOnItemClickListener(new OnItemClickListener() 
		{

			public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) 
			{
				Intent floorList = new Intent(Navigate_buildinglist.this, Navigate_floorlist.class);
				floorList.putExtra("BuildingID", buildings.get(position).ID);
    			startActivity(floorList);
    			finish();
			}
		});
        
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
    		//registerForContextMenu(list);
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
	
}
