package DataModifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Modify {

	public static void main(String args[]) throws IOException{
		File out = new File("C:/Users/yabetaka/Desktop/#201111.CDR-data/#201402.CDR-data/2_data/1_gps/alldata.csv");
		Gatchanko(out);

	}

	public static File Gatchanko(File out) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		for(int i=1;i<=48;i++){
			String n = String.format("%02d", i);
			File file = new File("C:/Users/yabetaka/Desktop/#201111.CDR-data/#201402.CDR-data/2_data/1_gps/2_00" +n+ ".csv");
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			while((line=br.readLine())!=null){
				String[] tokens = line.split(",");
				//				String num = tokens[0];
				String id = tokens[1];
				String t = tokens[2];
//				System.out.println(t);
				String[] times = t.split("\\+");
				String time = times[0];
				System.out.println(time);
				Double lon = Double.parseDouble(tokens[4]);
				Double lat = Double.parseDouble(tokens[5]);
				bw.write(id + "," + time + "," + lon + "," + lat);
				bw.newLine();
			}
			br.close();
		}
		bw.close();

		return out;
	}

}
