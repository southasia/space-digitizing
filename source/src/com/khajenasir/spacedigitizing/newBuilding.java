package com.khajenasir.spacedigitizing;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class newBuilding extends Activity
{
	private DatabaseHelper db=null;
	TextView txtName;
	Spinner spinShape, spinCategory, spinParent;
	boolean CanSendData = false;
    String[] categoryItems,buildingItems;
	private List<Building> buildings = new ArrayList<Building>();
	private List<Category> categories = new ArrayList<Category>();
	
	int buildingID = -1;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newbiulding);
        
    	db = new DatabaseHelper(this);
        
        txtName = (TextView)findViewById(R.id.editTextName);
        
        spinShape = (Spinner)findViewById(R.id.spinnerShape);
        spinCategory = (Spinner)findViewById(R.id.spinnerCategory);
        spinParent =  (Spinner)findViewById(R.id.spinnerParent);
        
        ArrayAdapter<String> adapterShape = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapterShape.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterShape.add("Square");
        adapterShape.add("Triangle");
        spinShape.setAdapter(adapterShape);
        
        loadCategory();
        ArrayAdapter<String> adapterCategory = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categoryItems);
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinCategory.setAdapter(adapterCategory);
        
        loadBuildings();
        ArrayAdapter<String> adapterParent = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, buildingItems);
        adapterParent.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinParent.setAdapter(adapterParent);
        
        
        if(getIntent().getExtras() != null)
        {
        	buildingID = getIntent().getExtras().getInt("BuildingID");
        	//Fill Mode
        	
			String[] select = {"ID","Name","Shape","CategoryID","ParentID"};
			Cursor cursor = db.SelectTable("tbl_building", select, "ID = " + buildingID, "ID");
			cursor.moveToFirst();
			
			txtName.setText(cursor.getString(1));
			
			if(cursor.getString(2) == "Square")
				spinCategory.setSelection(0);
			else
				spinCategory.setSelection(1);
			
			for(int i = 0 ; i< categories.size(); i++)
			{
				if(categories.get(i).ID == cursor.getInt(3))
				{
					spinCategory.setSelection(i);
					break;
				}
				
			}
			
			for(int j = 0 ; j< buildings.size(); j++)
			{
				if(buildings.get(j).ID == cursor.getInt(4))
				{
					spinParent.setSelection(j);
					break;
				}
				
			}
        }
    }
    
	private void loadCategory()
	{
		try 
		{		
			String[] select = {"ID","Name"};
			Cursor cursor = db.SelectTable("tbl_category", select, "", "ID");
			cursor.moveToFirst();
			
	        for(int i=0;i<cursor.getCount();i++)
	        {
	        	Category newCategory = new Category();
	        	newCategory.ID = cursor.getInt(0);
	        	newCategory.Name = cursor.getString(1);
	        	categories.add(newCategory);
	        	cursor.moveToNext();
	        }
	        
			categoryItems = new String[categories.size()];
			for(int i = 0; i< categories.size(); i++)
				categoryItems[i] = categories.get(i).Name;
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
			String[] select = {"ID","Name","Shape","CategoryID","ParentID"};
			Cursor cursor = db.SelectTable("tbl_building", select, "", "ID");
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
	        
			buildingItems = new String[buildings.size()];
			for(int i = 0; i< buildings.size(); i++)
				buildingItems[i] = buildings.get(i).Name;			
		}  
		catch (Exception e) 
		{
			e.printStackTrace();
		}		
	}
        
    public void onclick(View v)
    {
    	switch(v.getId())
    	{
    		case R.id.buttonNext:
    			
    			//Save Preference
            	SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
            	SharedPreferences.Editor editor = prefs.edit();
            	
            	editor.putString("Name", txtName.getText().toString());
            	editor.putString("Shape", spinShape.getSelectedItem().toString());
            	editor.putInt("CategoryID", categories.get(spinCategory.getSelectedItemPosition()).ID);
            	editor.putInt("ParentID", buildings.get(spinParent.getSelectedItemPosition()).ID);
                editor.commit();
                
    			Intent i = new Intent().setClass(this, newBuilding2.class);
    			i.putExtra("BuildingID", buildingID);
    			startActivity(i); 
    			finish();
    			break;
    	}
    }
}
