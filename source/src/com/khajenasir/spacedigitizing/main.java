package com.khajenasir.spacedigitizing;

import com.khajenasir.spacedigitizing.DatabaseHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class main extends Activity 
{
	private DatabaseHelper db=null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main); 
        
        db = new DatabaseHelper(this);
    }
    
    public void menu_click(View v)
    {
    	Intent i;
    	
    	switch(v.getId())
    	{
    		case R.id.Button1 :
    			i = new Intent().setClass(this, BuildingManager.class);
    			startActivity(i);
    			break;
    		case R.id.Button2 :
    			i = new Intent().setClass(this, Routing_buildinglist.class);
    			startActivity(i);
    			break;
    		case R.id.Button3 :
    			i = new Intent().setClass(this, Navigate_buildinglist.class);
    			startActivity(i);
    			break;
    	}
    }
}