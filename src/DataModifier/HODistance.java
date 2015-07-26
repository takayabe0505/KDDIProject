package DataModifier;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import jp.ac.ut.csis.pflow.geom.LonLat;
import Tools.GetMap;

public class HODistance {

	public static void main(String args[]) throws IOException{
		
		File homes = new File("C:/Users/yabetaka/Desktop/#201111.CDR-data/#201402.CDR-data/2_data/1_gps/id_RealHome.csv");
		File offices = new File("C:/Users/yabetaka/Desktop/#201111.CDR-data/#201402.CDR-data/2_data/1_gps/id_RealOffice.csv");

		HashMap<String,LonLat> idhome = GetMap.getHomeMap_String(homes);
		HashMap<String,LonLat> idoffice = GetMap.getOfficeMap_String(offices);
		
		for(String id : idhome.keySet()){
			double dis = idhome.get(id).distance(idoffice.get(id));
			System.out.println(id + "," + dis);
		}


	}
	
}
