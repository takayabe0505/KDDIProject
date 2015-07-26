package Analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import jp.ac.ut.csis.pflow.geom.LonLat;

public class LocationHantei {

	public static void main(String args[]) throws IOException{
		File id_home = new File("C:/Users/yabetaka/Desktop/#201111.CDR-data/#201402.CDR-data/2_data/1_gps/id_office.csv");
		File id_RealHome = new File("C:/Users/yabetaka/Desktop/#201111.CDR-data/#201402.CDR-data/2_data/1_gps/id_realoffice.csv");

		Hantei(id_home,id_RealHome);

	}

	public static double Hantei(File in, File in2) throws IOException{
		double rate = 0;
		HashMap<String,LonLat> idhome = intoMap(in);
		HashMap<String,LonLat> idrealhome = intoMap(in2);
		int count = 0;
		int yes = 0;
		for(String id : idrealhome.keySet()){
			if(idhome.get(id)!=null){
				if(idrealhome.get(id).distance(idhome.get(id))<5000){
					yes++;
				}
				else{
					System.out.println(id);
				}
				count++;
			}
			else{
				System.out.println("out of consideration"+id);
			}

		}
		System.out.println(yes + "," + count + "," + (double)yes/(double)count);
		rate = (double)yes/(double)count;
		return rate;
	}

	public static HashMap<String,LonLat> intoMap(File in) throws NumberFormatException, IOException{
		HashMap<String,LonLat> res = new HashMap<String,LonLat>();
		BufferedReader br1 = new BufferedReader(new FileReader(in));
		String line = null;
		while((line=br1.readLine())!=null){
			String[] tokens = line.split(",");
			String id = tokens[0];
			Double lon = Double.parseDouble(tokens[1]);
			Double lat = Double.parseDouble(tokens[2]);
			LonLat point = new LonLat(lon,lat);
			res.put(id, point);
		}
		br1.close();
		return res;
	}

}
