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

    public ArrayList<Antigen> antigens;
    public HashMap<String, ArrayList<Antigen>> antigenMap;
    public DataSet(String path){
        readFile(path);

    }

    private void readFile(String path) {
        antigens = new ArrayList<>();
        antigenMap = new HashMap<>();
        try (
                Stream<String> stream = Files.lines(Paths.get(path))) {
            stream.forEach(this::processLine);

            //iterate over all the lines in the dataset
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*for (Antigen antigen:antigens){
            System.out.println(antigen.getLabel() +", "+Arrays.toString(antigen.getAttributes()));
        }*/
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
            antigenMap.put(label,new ArrayList<Antigen>(){{add(antigen);}});
        }else{
            antigenMap.get(label).add(antigen);
        }

        antigens.add(antigen);
    }
 }
