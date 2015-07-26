package DataModifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import jp.ac.ut.csis.pflow.geom.LonLat;
import Tools.GetMap;

public class RealTimeChecker {

	public static void main(String args[]) throws NumberFormatException, IOException, ParseException{
		
		/* TODO make shinchi for home and office!
		 * ALSO, extract IDs which were incorrectly identified...
		 */
		
		File alllogs = new File("C:/Users/yabetaka/Desktop/#201111.CDR-data/#201402.CDR-data/2_data/1_gps/alldata.csv");
		File homes = new File("C:/Users/yabetaka/Desktop/#201111.CDR-data/#201402.CDR-data/2_data/1_gps/id_RealHome.csv");
		File offices = new File("C:/Users/yabetaka/Desktop/#201111.CDR-data/#201402.CDR-data/2_data/1_gps/id_RealOffice.csv");

		HashMap<String,ArrayList<Integer>> id_hometime = createHomeMap(alllogs,homes);
		HashMap<String,ArrayList<Integer>> id_officetime = createOfficeMap(alllogs,offices);

		File id_ht = new File("C:/Users/yabetaka/Desktop/#201111.CDR-data/#201402.CDR-data/2_data/1_gps/allHomeLogs.csv");
		File id_ot = new File("C:/Users/yabetaka/Desktop/#201111.CDR-data/#201402.CDR-data/2_data/1_gps/allOfficeLogs.csv");
		
		writeOut(id_hometime,id_ht);
		writeOut(id_officetime,id_ot);

	}

	protected static final SimpleDateFormat SDF_TS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//change time format
	protected static final SimpleDateFormat SDF_MDS = new SimpleDateFormat("HH:mm:ss");//change time format

	public static HashMap<String,ArrayList<Integer>> createHomeMap(File AllLogs, File homes) throws IOException, NumberFormatException, ParseException{

		HashMap<String,LonLat> id_homes = GetMap.getHomeMap_String(homes);
		HashMap<String,ArrayList<Integer>> id_logs = getLogsnearOffice(AllLogs,id_homes); //get id_logsnearOffice from TargetDayLogs
		return id_logs;
	}

	public static HashMap<String,ArrayList<Integer>> createOfficeMap(File AllLogs, File offices) throws IOException, NumberFormatException, ParseException{

		HashMap<String,LonLat> id_offices = GetMap.getOfficeMap_String(offices);
		HashMap<String,ArrayList<Integer>> id_logs = getLogsnearOffice(AllLogs,id_offices); //get id_logsnearOffice from TargetDayLogs
		return id_logs;
	}

	public static HashMap<String,ArrayList<Integer>> getLogsnearOffice(File in, HashMap<String,LonLat> id_offices) throws IOException, NumberFormatException, ParseException{
		HashMap<String,ArrayList<Integer>> res = new HashMap<String,ArrayList<Integer>>();
		BufferedReader br = new BufferedReader(new FileReader(in));
		String line = null;
		while((line=br.readLine())!=null){
			String[] tokens = line.split(",");
			String id = tokens[0];
			if(id_offices.containsKey(id)){
				Double lon = Double.parseDouble(tokens[2]);
				Double lat = Double.parseDouble(tokens[3]);
				LonLat point = new LonLat(lon,lat);

				if(point.distance(id_offices.get(id))<1000){
					Integer time = converttoSecs(SDF_MDS.format(SDF_TS.parse(tokens[1])));

					if(res.containsKey(id)){
						res.get(id).add(time);
					}
					else{
						ArrayList<Integer> list = new ArrayList<Integer>();
						list.add(time);
						res.put(id, list);
					}
				}
			}
		}		
		br.close();
		return res;
	}

	public static HashMap<String,ArrayList<Integer>> getLogsnearHome(File in, HashMap<String,LonLat> id_homes, String day) throws IOException, NumberFormatException, ParseException{
		HashMap<String,ArrayList<Integer>> res = new HashMap<String,ArrayList<Integer>>();
		BufferedReader br = new BufferedReader(new FileReader(in));
		String line = null;
		while((line=br.readLine())!=null){
			String[] tokens = line.split(",");
			String id = tokens[0];
			if(id_homes.containsKey(id)){
				Double lon = Double.parseDouble(tokens[2]);
				Double lat = Double.parseDouble(tokens[3]);
				LonLat point = new LonLat(lon,lat);

				if(point.distance(id_homes.get(id))<1000){
					Integer time = converttoSecs(SDF_MDS.format(SDF_TS.parse(tokens[1])));

					if(res.containsKey(id)){
						res.get(id).add(time);
					}
					else{
						ArrayList<Integer> list = new ArrayList<Integer>();
						list.add(time);
						res.put(id, list);
					}
				}
			}
		}		
		br.close();
		return res;
	}

	public static int converttoSecs(String time){
		String[] tokens = time.split(":");
		int hour = Integer.parseInt(tokens[0]);
		int min  = Integer.parseInt(tokens[1]);
		int sec  = Integer.parseInt(tokens[2]);

		int totalsec = hour*3600+min*60+sec;		
		return totalsec;
	}

	public static File writeOut(HashMap<String,ArrayList<Integer>> id_xtime, File out) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		for(String id : id_xtime.keySet()){
			for(Integer time : id_xtime.get(id)){
				bw.write(id + "," + time);
				bw.newLine();
			}
		}
		bw.close();
		return out;
	}
}
