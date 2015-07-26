package Analyzer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import DataModifier.ID_Counter;
import jp.ac.ut.csis.pflow.geom.LonLat;
import jp.ac.ut.csis.pflow.geom.STPoint;

public class KDDIHomeGetter {

	public static void main(String args[]) throws IOException, NumberFormatException, ParseException{

		File in = new File("C:/Users/yabetaka/Desktop/#201111.CDR-data/#201402.CDR-data/2_data/1_gps/alldata_inKanto.csv");

		HashMap<String,HashMap<LonLat,ArrayList<STPoint>>> SPmap = StayPointGetter_String.getSPs(in, "00:00:00", "08:00:00", 10, 2000, 1000);

		HashMap<String, ArrayList<STPoint>> alldatamap = StayPointGetter_String.sortintoMap(in,",");
		HashMap<String, ArrayList<STPoint>> targetmap = StayPointGetter_String.getTargetMap(alldatamap,"00:00:00","08:00:00");
		HashMap<String,Integer> numberofLogs = new HashMap<String,Integer>();
		for(String id : targetmap.keySet()){
			int days = ID_Counter.NumberofDays(targetmap.get(id));
			numberofLogs.put(id, days);
		}
		
		File res = new File ("C:/Users/yabetaka/Desktop/#201111.CDR-data/#201402.CDR-data/2_data/1_gps/id_home4.csv");

		HashMap<String,HashMap<LonLat,Double>> id_SP_visitcount = ExcludeLowFrequentSPsbyVisitRate(SPmap,numberofLogs,10);

		HashMap<String,LonLat> resmap = getHomePointsbyVisitRate(id_SP_visitcount);
		System.out.println("#got result map!");

		writeOut(resmap, res);
		
		File id_RealHome = new File("C:/Users/yabetaka/Desktop/#201111.CDR-data/#201402.CDR-data/2_data/1_gps/id_realhome.csv");
		LocationHantei.Hantei(res,id_RealHome);
	}

	public static HashMap<String,HashMap<LonLat,Integer>> ExcludeLowFrequentSPsbyNumberofPoints(HashMap<String,HashMap<LonLat,ArrayList<STPoint>>> map, HashMap<String,Integer> totaldays, double minrate){
		HashMap<String,HashMap<LonLat,Integer>> res = new HashMap<String,HashMap<LonLat,Integer>>();
		for(String id : map.keySet()){
			HashMap<LonLat,Integer> tempmap = new HashMap<LonLat,Integer>();
			for(LonLat sp : map.get(id).keySet()){
				HashSet<String> temp = new HashSet<String>();
				for(STPoint stp : map.get(id).get(sp)){
					String date = (new SimpleDateFormat("yyyy-MM-dd")).format(stp.getTimeStamp());
					String[] youso = date.split("-");
					temp.add(youso[2]);
				}
				double rate = (double)temp.size()/(double)totaldays.get(id);
				if(rate>minrate){
					tempmap.put(sp, map.get(id).get(sp).size());
				}
			}
			res.put(id, tempmap);
		}
		return res;
	}
	
	public static HashMap<String,HashMap<LonLat,Double>> ExcludeLowFrequentSPsbyVisitRate(HashMap<String,HashMap<LonLat,ArrayList<STPoint>>> map, HashMap<String,Integer> totaldays, int minpoints){
		HashMap<String,HashMap<LonLat,Double>> res = new HashMap<String,HashMap<LonLat,Double>>();
		for(String id : map.keySet()){
			HashMap<LonLat,Double> tempmap = new HashMap<LonLat,Double>();
			for(LonLat sp : map.get(id).keySet()){
				HashSet<String> temp = new HashSet<String>();
				for(STPoint stp : map.get(id).get(sp)){
					String date = (new SimpleDateFormat("yyyy-MM-dd")).format(stp.getTimeStamp());
					String[] youso = date.split("-");
					temp.add(youso[2]);
				}
				if(map.get(id).get(sp).size()>minpoints){
					double rate = (double)temp.size()/(double)totaldays.get(id);
					tempmap.put(sp, rate);
				}
			}
			res.put(id, tempmap);
		}
		return res;
	}

	public static HashMap<Integer,HashMap<LonLat,Integer>> FrequentStayPointsintoMap(File in) throws IOException{
		HashMap<Integer,HashMap<LonLat,Integer>> res = new HashMap<Integer,HashMap<LonLat,Integer>>();
		BufferedReader br = new BufferedReader(new FileReader(in));
		String line = null;
		while ((line=br.readLine()) != null){
			String[] tokens = line.split(",");
			int id = Integer.parseInt(tokens[0]);
			LonLat point = new LonLat(Double.parseDouble(tokens[1]),Double.parseDouble(tokens[2]));
			int count = Integer.parseInt(tokens[3]);
			if(res.containsKey(id)){
				res.get(id).put(point, count);
			}
			else{
				HashMap<LonLat,Integer> map = new HashMap<LonLat,Integer>();
				map.put(point, count);
				res.put(id, map);
			}
		}
		br.close();
		return res;
	}

	public static HashMap<String,LonLat> getHomePointsbyNumberofPoints(HashMap<String,HashMap<LonLat,Integer>> map){
		HashMap<String,LonLat> res = new HashMap<String,LonLat>();
		for(String id : map.keySet()){
			if(map.get(id).size()>0){
				HashMap<LonLat,Integer> mapofID = map.get(id);
				LonLat point = getHome(mapofID);
				if(point!=null){
					res.put(id, point);
				}
			}
		}
		return res;
	}
	
	public static HashMap<String,LonLat> getHomePointsbyVisitRate(HashMap<String,HashMap<LonLat,Double>> map){
		HashMap<String,LonLat> res = new HashMap<String,LonLat>();
		for(String id : map.keySet()){
			if(map.get(id).size()>0){
				HashMap<LonLat,Double> mapofID = map.get(id);
				LonLat point = getHomebyVisitRate(mapofID);
				if(point!=null){
					res.put(id, point);
				}
			}
		}
		return res;
	}

	
	public static LonLat sortbyOrder(HashMap<LonLat,Integer> map, int rank){
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(LonLat p:map.keySet()){
			list.add(map.get(p));
		}
		Collections.sort(list);
		Collections.reverse(list);
		if(list.size()>=rank){
			int count = list.get(rank-1);
			LonLat point = null;
			for(LonLat p :map.keySet()){
				if(map.get(p)==count){
					point = p;
				}
			}
			return point;
		}
		else{
			return null;
		}
	}

	public static LonLat getHome(HashMap<LonLat,Integer> map){
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(LonLat p:map.keySet()){
			list.add(map.get(p));
		}
		Collections.sort(list);
		Collections.reverse(list);
		int count = list.get(0);
		LonLat point = null;
		for(LonLat p :map.keySet()){
			if(map.get(p)==count){
				point = p;
			}
		}
		return point;
	}
	
	public static LonLat getHomebyVisitRate(HashMap<LonLat,Double> map){
		ArrayList<Double> list = new ArrayList<Double>();
		for(LonLat p:map.keySet()){
			list.add(map.get(p));
		}
		Collections.sort(list);
		Collections.reverse(list);
		Double count = list.get(0);
		LonLat point = null;
		for(LonLat p :map.keySet()){
			if(map.get(p)==count){
				point = p;
			}
		}
		return point;
	}

	public static File writeOut(HashMap<String,LonLat> map, File out) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		int count = 0;
		for(String id:map.keySet()){
			bw.write(id + "," + map.get(id).getLon() + "," + map.get(id).getLat());
			bw.newLine();
			count++;
		}
		bw.close();
		System.out.println("IDs with Home: " + count);
		return out;
	}

}
