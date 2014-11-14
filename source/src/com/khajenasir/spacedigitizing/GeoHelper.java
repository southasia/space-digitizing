package com.khajenasir.spacedigitizing;

public class GeoHelper 
{
	double sa = 6378137.000000 , sb = 6356752.314245;
	double e = (Math.pow((Math.pow(sa, 2)-Math.pow(sb, 2)), 0.5))*(Math.pow(sa, -1));
	double e2 = (Math.pow((Math.pow(sa, 2)-Math.pow(sb, 2)), 0.5))*(Math.pow(sb, -1));
	double e2cuadrada = Math.pow(e2, 2);
	double c = Math.pow( sa , 2 ) * Math .pow(sb,-1);
	double alpha = ( (sa - sb )) * Math.pow(sa,-1);
	double ablandamiento = 1 * Math.pow(alpha, -1);
	double Huso,S,deltaS,a,epsilon,nu,kk,v,ta, a1,a2,j2,j4,j6,alfa,beta,gama,Bm,x_coordinate,y_coordinate;

	public double[] convert_Geodetic_To_Cartesian(double latitude,double longitude, double altitude)
    {   	
    	double lat=(double)((latitude*Math.PI)/180);
    	double lon=(double)((longitude*Math.PI)/180);

    	 Huso=Math.ceil((double)(longitude/6))+30; 
    	 S=( ( Huso * 6 ) - 183 );
    	 deltaS = lon - ( S * ( (double)(Math.PI  / 180) ) );    	 
    	 a = Math.cos(lat) * Math.sin(deltaS);
    	 epsilon = 0.5 * Math.log( (double)( 1 +  a) / ( 1 - a ) );
    	 nu = Math.atan( (double)(Math.tan(lat) / Math.cos(deltaS)) ) - lat;
    	 kk=( 1 + ( e2cuadrada * Math.pow(Math.cos(lat), 2)) );
    	 v = (double)( (double)(c / Math.pow(kk , 0.5 ))) * 0.9996;
    	 ta = (double)(( (double)(e2cuadrada / 2) ) * Math.pow(epsilon , 2) * Math.pow( Math.cos(lat) ,  2));
    	 a1 = Math.sin( (double)2 * lat );
    	 a2 = (double)a1 * Math.pow( Math.cos(lat) ,  2);
    	 j2 = lat + ( (double)(a1 / 2) );
    	 j4 = (double)(( ( 3 * j2 ) + a2 ) / 4);
    	 j6 = (double)(( ( 5 * j4 ) + ( a2 * Math.pow( Math.cos(lat) , 2) )) / 3);
      	 alfa =(double)((3* e2cuadrada)/4);    	 
    	 beta = (double)((( 5  * Math.pow(alfa , 2)))/3);    	 
    	 gama = (double)( (35  * Math.pow(alfa , 3))/27);
    	 Bm = (double)(0.9996 * c * ( lat - ((double)(alfa * j2)) + ((double)(beta * j4)) - ((double)(gama * j6 ))));
    	 x_coordinate = (double)(epsilon * v * ( 1 + ((double) (ta / 3) ) ) + 500000);
    	 y_coordinate = (double)((nu * v * ( 1 + ta )) + Bm);
    	 if (y_coordinate<0)
    	 {
    	    y_coordinate=9999999+y_coordinate;
    	 }
    	 
   	     double[] ret = {x_coordinate, y_coordinate, altitude};
   	     return ret;
    	 

    }

}
