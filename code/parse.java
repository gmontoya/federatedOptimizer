
import java.nio.charset.StandardCharsets;
import java.io.*;
import com.hp.hpl.jena.rdf.model.*;

public class parse {

    public static void main(String[] args) throws Exception {

        String file = args[0];
        String outputFile = args[1];

        //File f = new File(folder);
        //File[] content = f.listFiles();
        Model m = ModelFactory.createDefaultModel();

        //if (content != null) {
        //    for (File g : content) {
                //String path = g.getAbsolutePath();
                //InputStream is = new FileInputStream(path);
                //Reader r = new InputStreamReader(is);
                m.read(file);

                OutputStream os = new FileOutputStream(outputFile, true);
                Writer w = new OutputStreamWriter(os);
                m.write(w, "N-TRIPLE");
                m = ModelFactory.createDefaultModel();
        //    }
        //}
    }
}
