package cs451.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class ConfigParser {

    private String filePath;
    private File file;

    public boolean populate(String value) {
        this.file = new File(value);
        filePath = file.getPath();
        if (file == null) {
            System.out.println("File is null");
        }
        System.out.println(filePath);

        return true;
    }

    public String getPath() {
        return filePath;
    }


    public int getNumberOfMessage() {
        int numMessages = 20000;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(getPath())));
            String l = reader.readLine();
            numMessages = Integer.parseInt(l);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return numMessages;
    }


    public HashMap<Integer, HashSet<Integer>> getCausalities() {
        HashMap<Integer, HashSet<Integer>> causalities = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(getPath())));
            String line = null;
            reader.readLine();
            int hostNbr = 1;
            while ((line = reader.readLine()) != null) {
                HashSet<Integer> dependenciesSet = new HashSet<>();
                System.out.println("Line read " + line);
                String[] separated = line.split(" ");
                //Add self to the dependencies because of FIFO property
                for (String s : separated) {
                    dependenciesSet.add(Integer.parseInt(s));
                }
                causalities.put(hostNbr, dependenciesSet);
                hostNbr += 1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(causalities.toString());
        return causalities;
    }
}
