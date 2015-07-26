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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import jp.ac.ut.csis.pflow.geom.STPoint;
import Analyzer.StayPointGetter_String;

public class ID_Counter {

	public static void main(String args[]) throws IOException, ParseException{
		File in = new File("C:/Users/yabetaka/Desktop/#201111.CDR-data/#201402.CDR-data/2_data/1_gps/alldata.csv");
		File out = new File("C:/Users/yabetaka/Desktop/#201111.CDR-data/#201402.CDR-data/2_data/1_gps/id_allcount_morning.csv");

		HashMap<String, ArrayList<STPoint>> alldatamap = StayPointGetter_String.sortintoMap(in,",");
		HashMap<String, ArrayList<STPoint>> targetmap = StayPointGetter_String.getTargetMap(alldatamap,"00:00:00", "08:00:00");

		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		for(String i : targetmap.keySet()){
			int days = NumberofWeekDays(targetmap.get(i));
			bw.write(i + "," + alldatamap.get(i).size() + "," + targetmap.get(i).size() + "," + days);
			bw.newLine();
		}
		bw.close();
	}

	public static Integer NumberofDays(ArrayList<STPoint> list){
		int days = 0;
		HashSet<String> set = new HashSet<String>();
		for(STPoint p : list){
			Date date = p.getTimeStamp();
			String d =  (new SimpleDateFormat("yyyy-MM-dd")).format(date);
			String[] x = d.split("-");
			String xd = x[2];
			set.add(xd);
		}
		days = set.size();
		return days;
	}

	public static Integer NumberofWeekDays(ArrayList<STPoint> list){
		int days = 0;
		HashSet<String> set = new HashSet<String>();
		for(STPoint p : list){
			Date date = p.getTimeStamp();
			String d1 =  (new SimpleDateFormat("yyyy-MM-dd")).format(date);
			String[] x = d1.split("-");
			String xd = x[2];
			Integer d = Integer.valueOf(xd);
			if(!((d==3)||(d==10)||(d==17)||(d==24)||(d==2)||(d==9)||(d==16)||(d==23)||(d==11))){
				set.add(xd);
			}
		}
		days = set.size();
		return days;
	}

	public static File id_count(File in, File out) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(in));
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		String line = null;
		HashMap<String,Integer> res = new HashMap<String,Integer>();
		int counter = 0;
		while((line=br.readLine())!=null){
			String[] tokens = line.split(",");
			String id = tokens[0];
			if(res.containsKey(id)){
				int count = res.get(id);
				res.put(id, count+1);
			}
			else{
				res.put(id,1);
			}
			counter++;
		}
		System.out.println(counter);
		br.close();

		for(String i : res.keySet()){
			bw.write(i + "," + res.get(i));
			bw.newLine();
		}
		bw.close();

		return out;
	}



}
