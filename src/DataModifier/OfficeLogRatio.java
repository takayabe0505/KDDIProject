package DataModifier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import jp.ac.ut.csis.pflow.geom.LonLat;
import jp.ac.ut.csis.pflow.geom.STPoint;
import Analyzer.StayPointGetter_String;
import Tools.GetMap;

public class OfficeLogRatio {

	public static void main(String args[]) throws NumberFormatException, ParseException, IOException{
		File in = new File("C:/Users/yabetaka/Desktop/#201111.CDR-data/#201402.CDR-data/2_data/1_gps/alldata_inKanto.csv");
		File out = new File("C:/Users/yabetaka/Desktop/#201111.CDR-data/#201402.CDR-data/2_data/1_gps/id_lunchlogs_officelogs_ratio.csv");
		HashMap<String, ArrayList<STPoint>> alldatamap = StayPointGetter_String.sortintoMap(in,",");
		HashMap<String, ArrayList<STPoint>> targetmap = StayPointGetter_String.getTargetMap(alldatamap,"08:00:00","18:00:00");
		File offices = new File("C:/Users/yabetaka/Desktop/#201111.CDR-data/#201402.CDR-data/2_data/1_gps/id_RealOffice.csv");
		HashMap<String,LonLat> id_offices = GetMap.getOfficeMap_String(offices);

		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		for(String id : targetmap.keySet()){
			int yes = 0;
			HashSet<String> temp = new HashSet<String>();
			for(STPoint stp : targetmap.get(id)){
				if(stp.distance(id_offices.get(id))<5000){
					String date = (new SimpleDateFormat("yyyy-MM-dd")).format(stp.getTimeStamp());
					String[] youso = date.split("-");
					Integer d = Integer.valueOf(youso[2]);
					if(!((d==3)||(d==10)||(d==17)||(d==24)||(d==2)||(d==9)||(d==16)||(d==23)||(d==11))){
						temp.add(youso[2]);
					}
				}
			}
			System.out.println(id + "," + temp.size() + "," + 20 + ","+ (double)temp.size()/20d);
			bw.write(id + "," + targetmap.get(id).size() + "," + yes + "," + (double)yes/(double)targetmap.get(id).size());
			bw.newLine();
		}
		bw.close();
	}

}
