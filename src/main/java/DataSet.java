import AIS.Antibody;
import AIS.Antigen;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.stream.Stream;

public class DataSet {

    ArrayList<Antigen> antigenList;
    public Antigen[] antigens;
    public HashMap<String, ArrayList<Antigen>> antigenMap;
    public DataSet(String path){
        readFile(path);
    }

    private void readFile(String path) {
        antigenMap = new HashMap<>();
        antigenList = new ArrayList<>();

        try (
                Stream<String> stream = Files.lines(Paths.get(path))) {
            stream.forEach(this::processLine);

            //iterate over all the lines in the dataset
        } catch (IOException e) {
            e.printStackTrace();
        }

        antigens = new Antigen[antigenList.size()];
        antigens = antigenList.toArray(antigens);
    }

    private void processLine(String line){

        var list = Arrays.asList(line.split(","));
        if(list.size() == 1){
            return;
        }

        double[] attributes = new double[list.size()-1];
        var label = "";

        for(int i=0;i<list.size();i++){
            if(i == list.size()-1){
                label = list.get(i);
            }
            else{
                attributes[i] = Double.parseDouble(list.get(i));
            }
        }

        Antigen antigen = new Antigen(attributes,label);
        if(!antigenMap.containsKey(label)){
            antigenMap.put(label,new ArrayList<>(){{add(antigen);}});
        }else{
            antigenMap.get(label).add(antigen);
        }

        antigenList.add(antigen);
    }
 }
