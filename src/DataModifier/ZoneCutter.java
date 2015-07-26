package DataModifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import jp.ac.ut.csis.pflow.geom.GeometryChecker;

public class ZoneCutter {

	public static void main (String args[]) throws IOException{
		File allLogs = new File("C:/Users/yabetaka/Desktop/#201111.CDR-data/#201402.CDR-data/2_data/1_gps/alldata.csv");
		ExtractIDbyZone(allLogs,gtokyo,"tokyo");
	}

	static File tokyo = new File("C:/Users/yabetaka/Desktop/pt08tky.zoneshape");
	static GeometryChecker gtokyo = new GeometryChecker(tokyo);

	public static File ExtractIDbyZone(File alllogs, GeometryChecker gc, String place) throws IOException{
		File out = new File ("c:/users/yabetaka/desktop/#201111.CDR-data/#201402.CDR-data/2_data/1_gps/alldata_inKanto.csv");
		BufferedReader br = new BufferedReader(new FileReader(alllogs));
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		String line = null;
		int count = 0;
		while((line=br.readLine())!=null){
			String[] tokens = line.split(",");
			Double lon = Double.parseDouble(tokens[2]);
			Double lat = Double.parseDouble(tokens[3]);
			List<String> zonecodeList = gc.listOverlaps("zonecode",lon,lat);
			if( zonecodeList == null || zonecodeList.isEmpty() ) 
			{continue;}
			else{
				bw.write(line);
				bw.newLine();
				count++;
			}
		}
		bw.close();
		br.close();
		System.out.println(place +"'s ids : " + count);
		return out;
	}

}
