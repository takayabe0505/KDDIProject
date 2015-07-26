package Analyzer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import jp.ac.ut.csis.pflow.geom.LonLat;
import jp.ac.ut.csis.pflow.geom.STPoint;
import jp.ac.ut.csis.pflow.geom.clustering.MeanShift;
import jp.ac.ut.csis.pflow.geom.clustering.MeanShift.IKernel;
import GPSOrganize.IDExtractor;

public class StayPointGetter_String {
	public static HashMap<String,HashMap<LonLat,ArrayList<STPoint>>> getSPs(File in, String start, String end, int min, double sigma, double threshold) throws NumberFormatException, ParseException, IOException{
		long startTime = System.currentTimeMillis();
		
		HashMap<String, ArrayList<STPoint>> alldatamap = sortintoMap(in,",");
		long time1 = System.currentTimeMillis();
		System.out.println("#done getting Map ; " + (time1-startTime)/1000 +"secs; number of ids:" + alldatamap.size());

		HashMap<String, ArrayList<STPoint>> targetmap = getTargetMap(alldatamap,start,end);
		long time2 = System.currentTimeMillis();
		System.out.println("#done getting target Map ; " + (time2-startTime)/1000+"secs");
		
		HashMap<String,HashMap<LonLat,ArrayList<STPoint>>> res = new HashMap<String,HashMap<LonLat,ArrayList<STPoint>>>();
		
		int counter = 0;
		for(String id:targetmap.keySet()){
			HashMap<LonLat, ArrayList<STPoint>> SPmap = getStayPoints(targetmap.get(id),sigma,threshold,min);
			res.put(id, SPmap);
			if(SPmap.size()>0){
				counter++;
			}
		}
		System.out.println(counter+" IDs have SPs more than 10 points in this timespan");
		return res;
	}

	public static HashMap<String, ArrayList<STPoint>> sortintoMap(File in, String divider) throws ParseException, NumberFormatException, IOException{
		HashMap<String, ArrayList<STPoint>> id_count = new HashMap<String, ArrayList<STPoint>>();
		BufferedReader br = new BufferedReader(new FileReader(in));
		String line = null;

		while ((line=br.readLine())!=null){
			String[] tokens = line.split(",");
			String id = tokens[0];
			STPoint point = new STPoint(SDF_TS2.parse(tokens[1]),Double.parseDouble(tokens[2]),Double.parseDouble(tokens[3]));
			if(id_count.containsKey(id)){
				id_count.get(id).add(point);
			}
			else{
				ArrayList<STPoint> list = new ArrayList<STPoint>();
				list.add(point);
				id_count.put(id, list);
			}
		}
		br.close();	

		return id_count;
	}
	
	public static HashMap<String, ArrayList<STPoint>> getTargetMap(HashMap<String, ArrayList<STPoint>> alldata, String start, String end) throws ParseException{
		HashMap<String, ArrayList<STPoint>> targetmap = new HashMap<String, ArrayList<STPoint>>();
		Date startdate = SDF_TS.parse(start);
		Date finishdate = SDF_TS.parse(end);
		for(String id: alldata.keySet()){
			for(STPoint p: alldata.get(id)){
//				System.out.println(p);	
				String date = (new SimpleDateFormat("HH:mm:ss")).format(p.getTimeStamp());
				Date date1 = SDF_TS.parse(date);
				if( (date1.after(startdate))&&(date1.before(finishdate)) ){
					if(targetmap.containsKey(id)){
						targetmap.get(id).add(p);
					}
					else{
						ArrayList<STPoint> list = new ArrayList<STPoint>();
						list.add(p);
						targetmap.put(id, list);
					}
				}
			}
		}
		return targetmap;
	}

	public static double getAverageSPs(HashSet<Integer> set,HashMap<Integer, ArrayList<STPoint>> map, double d, double r, int min) throws NumberFormatException, ParseException{
		ArrayList<Integer> NumofSPs = new ArrayList<Integer>();
		int counter = 0;
		HashMap<LonLat, ArrayList<STPoint>> SPmap = null;
		for(int id:set){
			ArrayList<STPoint> list = IDExtractor.getDataofIDListwithIntervalfromList(map.get(id),",",id);
			SPmap = getStayPoints(list,d,r,min);
			int num = 0;
			for(LonLat p:SPmap.keySet()){
				if(SPmap.get(p).size()>=min){
					num++;
				}
			}
			NumofSPs.add(num);
			counter++;
			//			System.out.println(counter);
			if(counter%100==0){
				System.out.println(counter);
			}
		}
		int sum= 0;
		for(int n:NumofSPs){
			sum = sum+n;
		}
		double avgpoints = sum/(double)counter;
		return avgpoints;
	}

	public static File getHistogramofSPs(File out, HashSet<Integer> set,HashMap<Integer, ArrayList<STPoint>> map, double d, double r, int min) throws NumberFormatException, ParseException, IOException{
		int counter = 0;
		HashMap<LonLat, ArrayList<STPoint>> SPmap = null;
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		for(int id:set){
			ArrayList<STPoint> list = IDExtractor.getDataofIDListwithIntervalfromList(map.get(id),",",id);
			SPmap = getStayPoints(list,d,r,min);
			int num = 0;
			for(LonLat p:SPmap.keySet()){
				if(SPmap.get(p).size()>=min){
					num++;
				}
			}
			bw.write(id + "," + num);
			bw.newLine();
			counter++;
			//			System.out.println(counter);
			if(counter%100==0){
				System.out.println(counter);
			}
		}
		bw.close();
		return out;
	}

	public static File writeout(int id,HashMap<LonLat, ArrayList<STPoint>> map, File out){
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(out, true));
			//			System.out.println("number of staypoints of "+ id+ " is " + map.keySet().size());
			for(LonLat p:map.keySet()){
				for(STPoint sp:map.get(p)){
					bw.write(id+","+p.getLon()+","+p.getLat()+","+(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(sp.getTimeStamp()));
					bw.newLine();
				}
			}
			bw.close();
		}
		catch(FileNotFoundException xx) {
			System.out.println("File not found 5");
		}
		catch(IOException xxx) {
			System.out.println(xxx);
		}
		return out;
	}

	public static File writeoutonlyFrequentSPs(int id, HashMap<LonLat, ArrayList<STPoint>> map, File out, int min){
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(out, true));
			for(LonLat p:map.keySet()){
				if(map.get(p).size()>=min){
					//for (STPoint sp:map.get(p)){
						//bw.write(id+","+p.getLon()+","+p.getLat()+","+map.get(p).size()+","+STPoint.FORMAT_YMDHMS.format(sp.getTimeStamp()));
						bw.write(id+","+p.getLon()+","+p.getLat()+","+map.get(p).size());
						bw.newLine();
					//}
				}
			}
			bw.close();
		}
		catch(FileNotFoundException xx) {
			System.out.println("File not found 5");
		}
		catch(IOException xxx) {
			System.out.println(xxx);
		}
		return out;
	}

	protected static final SimpleDateFormat SDF_TS = new SimpleDateFormat("HH:mm:ss");//change time format
	protected static final SimpleDateFormat SDF_TS2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//change time format

	
	public static HashMap<LonLat, ArrayList<STPoint>> getStayPoints(ArrayList<STPoint> list, double h, double e, int min){
		HashMap<LonLat, ArrayList<STPoint>> map = new HashMap<LonLat, ArrayList<STPoint>>();
		map = clustering2dKNSG(MeanShift.GAUSSIAN,list,h,e);
		HashMap<LonLat, ArrayList<STPoint>> Cutmap = cutbyPoints(map,min);		
		return Cutmap;
	}

	public static HashMap<LonLat, ArrayList<STPoint>> cutbyPoints(HashMap<LonLat, ArrayList<STPoint>> in, int min){
		HashMap<LonLat, ArrayList<STPoint>> res = new HashMap<LonLat, ArrayList<STPoint>>();
		for(LonLat p : in.keySet()){
			if(in.get(p).size()>=min){
				res.put(p, in.get(p));
			}
		}
		return res;
	}
	
	//h:determines how far points can influence eachother, e:determines closeness of same cluster
	public static HashMap<LonLat,ArrayList<STPoint>> clustering2dKNSG(IKernel kernel,ArrayList<STPoint> data,double h,double e) {
		HashMap<LonLat,ArrayList<STPoint>> result = new HashMap<LonLat,ArrayList<STPoint>>();
		int N = data.size();
//		System.out.println("#number of points : "+N);

		for(STPoint point:data) {
			// seek mean value //////////////////
			LonLat mean = new LonLat(point.getLon(),point.getLat());

			//loop from here for meanshift
			while(true) {
				double numx = 0d;
				double numy = 0d;
				double din = 0d;
				for(int j=0;j<N;j++) {
					LonLat p = new LonLat(data.get(j).getLon(),data.get(j).getLat());
					double k = kernel.getDensity(mean,p,h);
					numx += k * p.getLon();
					numy += k * p.getLat();
					din  += k;
				}
				LonLat m = new LonLat(numx/din,numy/din);
				if( mean.distance(m) < e ) { mean = m; break; }
				mean = m;
			}
//			System.out.println("#mean is : " + mean);
			// make cluster /////////////////////
			ArrayList<STPoint> cluster = null;
			for(LonLat p:result.keySet()) {
				if( mean.distance(p) < e ) { cluster = result.get(p); break; }
			}
			if( cluster == null ) {
				cluster = new ArrayList<STPoint>();
				result.put(mean,cluster);
			}
			cluster.add(point);
		}
		return result;
	}

}
