package com.khajenasir.spacedigitizing;

import android.app.Activity;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Visibility;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


public class Routing_buildingselect  extends Activity implements SensorEventListener
{
	private SensorManager sensorManager;

	private float x, y, z;
	//EditText txtDirection;
	Boolean[] S, T;
	int s,t;
	ImageView SU,SD,SL,SR,T1,T3;
	Boolean IsSquare = true, IsTriangle = true;
	String Shape ="";
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.routing_buildingselect);
        
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		//txtDirection = (EditText) findViewById(R.id.editTextDirection);
		
		SU =(ImageView)findViewById(R.id.imageViewu);
		SD =(ImageView)findViewById(R.id.imageViewD);
		SL =(ImageView)findViewById(R.id.imageViewL);
		SR =(ImageView)findViewById(R.id.imageViewR);
		
		T1 =(ImageView)findViewById(R.id.imageViewt1);
		T3 =(ImageView)findViewById(R.id.imageViewt3);
		
		S = new Boolean[4];
		T = new Boolean[3];
		resetSqure();
		s=-1;
		t=-1;
    }
    
    void resetSqure()
    {
    	S[0] = S[1] = S[2] = S[3] = false;
    	SU.setVisibility(View.INVISIBLE);
    	SL.setVisibility(View.INVISIBLE);
    	SD.setVisibility(View.INVISIBLE);
    	SR.setVisibility(View.INVISIBLE);
    	
    	T[0] = T[1] = T[2] = false;
    	T1.setVisibility(View.INVISIBLE);
    	T3.setVisibility(View.INVISIBLE);
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
		
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
		
		//sensorManager.registerListener(this,
		//		sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
        //        SensorManager.SENSOR_DELAY_NORMAL);
//		
//		sensorManager.registerListener(this, 
//				sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
//                SensorManager.SENSOR_DELAY_NORMAL);
    }
    
	@Override
	public void onPause() 
	{
		super.onPause();
		sensorManager.unregisterListener(this);
    }
    
	public void onSensorChanged(SensorEvent event) 
	{
		synchronized (this) 
		{	

			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			{
				//Movement
				z = event.values[0];
				x = event.values[1];
				//y = event.values[2]; 

				if(IsSquare)
				{
					//Square
					//-X,-Z,X,Z
					// -X
					if((Math.pow(x, 2) > Math.pow(y, 2) + Math.pow(z, 2)) && x < -11 && S[0] == false && S[1] == false && S[2] == false && S[3] == false)
					{
						S[0] = true;
				    	SL.setVisibility(View.VISIBLE);
				    	IsTriangle = false;
					}
					// -Z	
					else if ((Math.pow(z, 2) > Math.pow(y, 2) + Math.pow(x, 2)) && z < -11 && S[0] == true && S[1] == false && S[2] == false && S[3] == false)
					{
						S[1] = true;
				    	SU.setVisibility(View.VISIBLE);
					}
					// X
					else if((Math.pow(x, 2) > Math.pow(y, 2) + Math.pow(z, 2)) && x > 11 && S[0] == true && S[1] == true && S[2] == false && S[3] == false)
					{
						S[2] = true;
				    	SR.setVisibility(View.VISIBLE);
					}
					// Z	
					else if ((Math.pow(z, 2) > Math.pow(y, 2) + Math.pow(x, 2)) && z > 11 && S[0] == true && S[1] == true && S[2] == true && S[3] == false)
					{
						S[3] = true;
				    	SD.setVisibility(View.VISIBLE);
				    	Shape = "Square";
					}
				
				}

				

				
//				if(S[0] == S[1] &&  S[1] == S[2] &&  S[2]== S[3] && S[3] == true)
//				{
//					//txtDirection.setText("Squre");
//					//resetSqure();
//				}
				if(IsTriangle)
				{
					//T1
					if(z<-11 && x > 11 && (Math.pow(z, 2) + Math.pow(x, 2) >  Math.pow(y, 2))  && T[0] == false && T[1] == false && T[2] == false )
					{
						T[0] = true;
						T1.setVisibility(View.VISIBLE);
						IsSquare = false;
					}
					//T2
					else if ((Math.pow(z, 2) > Math.pow(y, 2) + Math.pow(x, 2)) && z > 11  && T[0] == true && T[1] == false && T[2] == false )
					{
						T[1] = true;
						SD.setVisibility(View.VISIBLE);
					}
					else if(z < -11 && x < -11 && (Math.pow(z, 2) + Math.pow(x, 2) >  Math.pow(y, 2))  && T[0] == true && T[1] == true && T[2] == false )
					{
						T[2] = true;
						T3.setVisibility(View.VISIBLE);
				    	Shape = "Triangle";
					}
				}
				
			}	
			
		}
	}
	
	public void onAccuracyChanged(Sensor sensor, int accuracy) 
	{}
	
	public void onClick(View v)
	{
		//Save Preference
    	SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
    	SharedPreferences.Editor editor = prefs.edit();
    	
    	editor.putString("SelectedShape", Shape);

        editor.commit();
        
        finish();
	}
	
}
