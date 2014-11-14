package com.khajenasir.spacedigitizing;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class Navigate_floorlist extends Activity 
{
	private DatabaseHelper db=null;
	
	ListView list;
	//boolean CanSendData = false;
	ArrayAdapter<String> adaptor;
	private List<Floor> floors = new ArrayList<Floor>();
	
	int buildingID = -1;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigate_floorlist);

    	buildingID = getIntent().getExtras().getInt("BuildingID");
		
		list = (ListView)findViewById(R.id.listViewNavFloors);
		list.setOnItemClickListener(new OnItemClickListener() 
		{

			public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) 
			{
				Intent buildingFinder = new Intent(Navigate_floorlist.this, Navigate_buildingfinder.class);
				buildingFinder.putExtra("FloorNUM", position + 1);
				buildingFinder.putExtra("BuildingID", buildingID);
    			startActivity(buildingFinder);
    			finish();
			}
		});
        
    	db = new DatabaseHelper(this);
    }
	
	@Override
    protected void onResume()
    {
        super.onResume();	
	
			adaptor = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
			loadfloors();
    	
    		list.setAdapter(adaptor);
    }
	
	void loadfloors()
	{
		try
		{
		
		adaptor.clear();
		floors.clear();
		
		String[] select = {"ID","BuildingID","Name"};
		Cursor cursor = db.SelectTable("tbl_buildingfloor", select, "BuildingID=" + buildingID, "ID");
		cursor.moveToFirst();
		
		for(int i=0;i<cursor.getCount();i++)
		{
			Floor newFloor = new Floor();
			newFloor.ID = cursor.getInt(0);
			newFloor.Name = cursor.getString(2);
			floors.add(newFloor);
			
			cursor.moveToNext();
		}		
		
		for(int j=0;j<floors.size();j++)
		    adaptor.add((j+1) + " - " + floors.get(j).Name);
		
		
	}
	catch(Exception ex)
	{
		ex.printStackTrace();
	}
		
	}
	
}
