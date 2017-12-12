import java.util.List;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.aksw.simba.fedsum.util.FedSumGenerator;

class generateHibiscusStatistics {

    public static void main(String[] args) {
        String outputFolder = args[0];
        String federationFile = args[1];
        List<String> endpoints = readEndpoints(federationFile);
        try {
            FedSumGenerator fsg = new FedSumGenerator(outputFolder);
            fsg.generateSummaries(endpoints);
        } catch (Exception e) {
            System.err.println("Problems generating statistics for federation: "+federationFile);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static List<String> readEndpoints(String file) {
        List<String> es = new Vector<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String l = br.readLine();
            while (l!=null) {
                es.add(l); 
                l = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            System.err.println("Problems reading file: "+file);
            System.exit(1);
        }
        return es;
    }
}
