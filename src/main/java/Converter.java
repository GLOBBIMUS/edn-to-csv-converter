import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Readable;
import java.io.File;
import java.util.*;

import us.bpsm.edn.parser.Parseable;
import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.parser.Parsers;
import us.bpsm.edn.TaggedValue;
import static us.bpsm.edn.Keyword.newKeyword;
import static us.bpsm.edn.parser.Parsers.defaultConfiguration;

/**
 * @author GLOBBIMUS
 */
public class Converter {

    //Declare the output CSV files
    private FileWriter RunConfiguration_Node_File;
    private FileWriter Individual_Entity_File;
    private FileWriter Genome_Entity_File;
    private FileWriter SingleError_Entity_File;
    private FileWriter TotalError_Entity_File;
    private FileWriter RunConfiguration_Individual_Edge_File;
    private FileWriter ParentOf_Edge_File;
    private FileWriter Individual_Genome_Edge_File;
    private FileWriter Individual_TotalError_Edge_File;
    private FileWriter TotalError_SingleError_Edge_File;

    //HashMaps for keeping track of unique entities
    private HashMap<String, String> uniqueGenomes = new HashMap<String, String>();
    private HashMap<String, String> uniqueSingleErrors = new HashMap<String, String>();
    private HashMap<String, String> uniqueTotalErrors = new HashMap<String, String>();


    private final  String NEW_LINE = "\n";
    private final String COMMA = ",";

    private Parser parser = Parsers.newParser(defaultConfiguration());
    private Parseable ednFile;


    public void convert(String sourceFile, String target){
        try {
            setUp_source_File(sourceFile);
            setUp_CSV_Files(target);
            fillUpFiles();
            flushAndClose();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void setUp_source_File(String sourceFile) throws IOException{
        File file = new File(sourceFile);
        Readable input = new FileReader(file);
        ednFile = Parsers.newParseable(input);
    }

    private void setUp_CSV_Files(String target) throws IOException{
        RunConfiguration_Node_File = new FileWriter(target + "/RunConfigurations_Entity.csv");
        Individual_Entity_File = new FileWriter( target + "/Individual_Entity.csv");
        Genome_Entity_File = new FileWriter(target + "/Genome_Entity.csv");
        SingleError_Entity_File = new FileWriter(target + "/SingleError_Entity.csv");
        TotalError_Entity_File = new FileWriter(target + "/TotalError_Entity.csv");

        RunConfiguration_Individual_Edge_File = new FileWriter(target + "/RunConfigurations_Individual_Edge.csv");
        ParentOf_Edge_File = new FileWriter(target + "/ParentOf_Edge.csv");
        Individual_Genome_Edge_File = new FileWriter(target + "/Individual_Genome_Edge.csv");
        Individual_TotalError_Edge_File = new FileWriter(target + "/Individual_TotalError_Edge.csv");
        TotalError_SingleError_Edge_File = new FileWriter(target + "/TotalError_SingleError_Edge.csv");

        appendHeaders();
    }

    private void appendHeaders() throws  IOException{
        Individual_Entity_File.append("uuid:ID(Individual),generation:int,location:int,:LABEL");
        Genome_Entity_File.append("uuid:ID(Genome),close:int,instruction,:LABEL");
        SingleError_Entity_File.append("uuid:ID(SingleError),error:long,position:int,:LABEL");
        TotalError_Entity_File.append("uuid:ID(TotalError),TotalError:long,:LABEL");

        RunConfiguration_Individual_Edge_File.append(":START_ID(RunConfigurations),:END_ID(Individual),:TYPE");
        ParentOf_Edge_File.append(":START_ID(Individual),genetic-operator,:END_ID(Individual),:TYPE");
        Individual_Genome_Edge_File.append(":START_ID(Individual),:END_ID(Genome),:TYPE");
        Individual_TotalError_Edge_File.append(":START_ID(Individual),:END_ID(TotalError),:TYPE");
        TotalError_SingleError_Edge_File.append(":START_ID(TotalError),:END_ID(SingleError),:TYPE");
    }


    private Object setUp_RunConfigurations()throws IOException{

        StringBuilder runConfigurations_HEADER = new StringBuilder();

        TaggedValue runConfigs = (TaggedValue) parser.nextValue(ednFile);
        Map<?, ?> runConfigMap = (Map<?, ?>) runConfigs.getValue();

        Object[] keys = (runConfigMap.keySet()).toArray(new Object[(runConfigMap.keySet()).size()]);

        runConfigurations_HEADER.append("uuid:ID(RunConfigurations)");
        runConfigurations_HEADER.append(COMMA);
        for (Object obj: keys) {
            runConfigurations_HEADER.append(obj.toString());
            runConfigurations_HEADER.append(COMMA);
        }

        runConfigurations_HEADER.append(":LABEL");
        String runConfig_ID = UUID.randomUUID().toString();
        RunConfiguration_Node_File.append(runConfigurations_HEADER.toString());
        RunConfiguration_Node_File.append(NEW_LINE);
        RunConfiguration_Node_File.append(runConfig_ID);
        RunConfiguration_Node_File.append(COMMA);

        for (Object obj: keys) {
            RunConfiguration_Node_File.append("\"");
            RunConfiguration_Node_File.append(obj.toString());
            RunConfiguration_Node_File.append("\"");
            runConfigurations_HEADER.append(COMMA);
        }

        RunConfiguration_Node_File.append("RunConfigurations");

        Object firstIndividual = parser.nextValue(ednFile);
        TaggedValue firstIndividualValue =  (TaggedValue)firstIndividual;
        Map<?, ?> individualMap = (Map<?, ?>) firstIndividualValue.getValue();

        RunConfiguration_Individual_Edge_File.append(NEW_LINE);
        RunConfiguration_Individual_Edge_File.append(runConfig_ID);
        RunConfiguration_Individual_Edge_File.append(COMMA);
        RunConfiguration_Individual_Edge_File.append(individualMap.get(newKeyword("uuid")).toString());
        RunConfiguration_Individual_Edge_File.append(COMMA);
        RunConfiguration_Individual_Edge_File.append("RunConfigurations");
        return firstIndividual;
    }


    private void fillUpFiles() throws IOException{

        Individual Individual = new Individual();

        Object currentRow = setUp_RunConfigurations();

        while((currentRow != Parser.END_OF_INPUT)){
            TaggedValue individual = (TaggedValue)currentRow;
            Map<?, ?> individualMap = (Map<?, ?>) individual.getValue();
            Individual.set(individualMap);
            Individual_Entity_File.append(NEW_LINE);
            Individual_Entity_File.append(Individual.Individual_ID);
            Individual_Entity_File.append(COMMA);
            Individual_Entity_File.append(Individual.Generation);
            Individual_Entity_File.append(COMMA);
            Individual_Entity_File.append(Individual.Location);
            Individual_Entity_File.append(COMMA);
            Individual_Entity_File.append("Individual");
            extractParents(Individual);
            handle_TotalError(Individual);
            handle_Genome(Individual);
            currentRow = parser.nextValue(ednFile);
        }

    }


    private void extractParents(Individual Individual) throws IOException{
        if(Individual.Parents != null) {
            ArrayList<Object> Parents = new ArrayList<Object>(Individual.Parents);
            StringBuilder gen = new StringBuilder();
            gen.append("\"");
            gen.append(Individual.Genetic_Operators);
            gen.append("\"");
            if (Parents.size() == 1){
                ParentOf_Edge_File.append(NEW_LINE);
                ParentOf_Edge_File.append(Parents.get(0).toString());
                ParentOf_Edge_File.append(COMMA);
                ParentOf_Edge_File.append(gen);
                ParentOf_Edge_File.append(COMMA);
                ParentOf_Edge_File.append(Individual.Individual_ID);
                ParentOf_Edge_File.append(COMMA);
                ParentOf_Edge_File.append("ParentOf");
            } else if (Parents.size() > 1) {
                ParentOf_Edge_File.append(NEW_LINE);
                ParentOf_Edge_File.append(Parents.get(0).toString());
                ParentOf_Edge_File.append(COMMA);
                ParentOf_Edge_File.append(gen);
                ParentOf_Edge_File.append(COMMA);
                ParentOf_Edge_File.append(Individual.Individual_ID);
                ParentOf_Edge_File.append(COMMA);
                ParentOf_Edge_File.append("ParentOf");
                ParentOf_Edge_File.append(NEW_LINE);
                ParentOf_Edge_File.append(Parents.get(1).toString());
                ParentOf_Edge_File.append(COMMA);
                ParentOf_Edge_File.append(gen);
                ParentOf_Edge_File.append(COMMA);
                ParentOf_Edge_File.append(Individual.Individual_ID);
                ParentOf_Edge_File.append(COMMA);
                ParentOf_Edge_File.append("ParentOf");
            }
        }
    }


    private void handle_Genome(Individual Individual) throws IOException{
        StringBuilder key = new StringBuilder();
        String Close;
        String Instruction;
        String Genome_ID;
        String replace;
        String mapKey;

        for(int i = 0; i < Individual.Genomes.size(); i++){
            Close = ((Map<?, ?>)Individual.Genomes.get(i)).get(newKeyword("close")).toString();
            Instruction = ((Map<?, ?>)Individual.Genomes.get(i)).get(newKeyword("instruction")).toString();
            if(Instruction.contains("\n"))Instruction = "\\newline";

            if(Instruction.contains("\\")){
                replace = Instruction.replace("\\","\\\\");
                Instruction = replace;
            }

            if(Instruction.contains("\"")){
                replace = Instruction.replace("\"","\\\"");
                Instruction = replace;
            }

            if(Instruction.contains("\'")){
                replace = Instruction.replace("\'","\\\'");
                Instruction = replace;
            }

            key.append(Close);
            key.append(COMMA);
            key.append("\"");
            key.append(Instruction);
            key.append("\"");
            mapKey = key.toString();

            if(!(uniqueGenomes.containsKey(mapKey))){
                Genome_Entity_File.append(NEW_LINE);
                Genome_ID = UUID.randomUUID().toString();
                Genome_Entity_File.append(Genome_ID);
                Genome_Entity_File.append(COMMA);
                Genome_Entity_File.append(key);
                Genome_Entity_File.append(COMMA);
                Genome_Entity_File.append("Genome");
                uniqueGenomes.put(mapKey, Genome_ID);
                handle_Genome_Edge(Individual.Individual_ID, Genome_ID);
            }else{
                handle_Genome_Edge( Individual.Individual_ID, uniqueGenomes.get(mapKey));
            }
            key.setLength(0);
        }
    }

    private void handle_Genome_Edge(String IndividualID, String Genome_ID) throws IOException{
        Individual_Genome_Edge_File.append(NEW_LINE);
        Individual_Genome_Edge_File.append(IndividualID);
        Individual_Genome_Edge_File.append(COMMA);
        Individual_Genome_Edge_File.append(Genome_ID);
        Individual_Genome_Edge_File.append(COMMA);
        Individual_Genome_Edge_File.append("HasGenome");
    }

    private void handle_TotalError(Individual Individual) throws IOException{
        String key = Individual.ErrorVector.toString();
        String TotalError_ID;

        if(!uniqueTotalErrors.containsKey(key)){
            TotalError_ID = UUID.randomUUID().toString();
            TotalError_Entity_File.append(NEW_LINE);
            TotalError_Entity_File.append(TotalError_ID);
            TotalError_Entity_File.append(COMMA);
            TotalError_Entity_File.append(Individual.TotalError);
            TotalError_Entity_File.append(COMMA);
            TotalError_Entity_File.append("TotalError");
            uniqueTotalErrors.put(key,TotalError_ID);
            handle_Individual_TotalError_Edge(Individual.Individual_ID, TotalError_ID);
            handle_SingleError(Individual, TotalError_ID);
        }else{
            handle_Individual_TotalError_Edge(Individual.Individual_ID, uniqueTotalErrors.get(key));
        }
    }

    private void handle_SingleError(Individual Individual, String TotalError_ID) throws IOException{
        StringBuilder key = new StringBuilder();
        String SingleError_ID;
        String mapKey;
        for(int i = 0; i < Individual.ErrorVector.size(); i++){
            key.append(Individual.ErrorVector.get(i));
            key.append(COMMA);
            key.append(i+1);
            mapKey = key.toString();
            if(!uniqueSingleErrors.containsKey(mapKey)){
                SingleError_ID = UUID.randomUUID().toString();
                SingleError_Entity_File.append(NEW_LINE);
                SingleError_Entity_File.append(SingleError_ID);
                SingleError_Entity_File.append(COMMA);
                SingleError_Entity_File.append(key);
                SingleError_Entity_File.append(COMMA);
                SingleError_Entity_File.append("SingleError");
                uniqueSingleErrors.put(mapKey, SingleError_ID);
                handle_TotalError_SingleError_Edge(TotalError_ID, SingleError_ID);
            }else{
                handle_TotalError_SingleError_Edge(TotalError_ID, uniqueSingleErrors.get(mapKey));
            }
            key.setLength(0);
        }
    }

    private   void handle_Individual_TotalError_Edge(String Individual_ID, String TotalError_ID) throws IOException{
        Individual_TotalError_Edge_File.append(NEW_LINE);
        Individual_TotalError_Edge_File.append(Individual_ID);
        Individual_TotalError_Edge_File.append(COMMA);
        Individual_TotalError_Edge_File.append(TotalError_ID);
        Individual_TotalError_Edge_File.append(COMMA);
        Individual_TotalError_Edge_File.append("HasTotalError");
    }

    private   void handle_TotalError_SingleError_Edge(String TotalError_ID, String SingleError_ID) throws IOException{
        TotalError_SingleError_Edge_File.append(NEW_LINE);
        TotalError_SingleError_Edge_File.append(TotalError_ID);
        TotalError_SingleError_Edge_File.append(COMMA);
        TotalError_SingleError_Edge_File.append(SingleError_ID);
        TotalError_SingleError_Edge_File.append(COMMA);
        TotalError_SingleError_Edge_File.append("ContainsError");
    }

    private void flushAndClose() throws IOException{
        RunConfiguration_Node_File.flush();
        RunConfiguration_Node_File.close();

        Individual_Entity_File.flush();
        Individual_Entity_File.close();

        Genome_Entity_File.flush();
        Genome_Entity_File.close();

        SingleError_Entity_File.flush();
        SingleError_Entity_File.close();

        TotalError_Entity_File.flush();
        TotalError_Entity_File.close();

        RunConfiguration_Individual_Edge_File.flush();
        RunConfiguration_Individual_Edge_File.close();

        ParentOf_Edge_File.flush();
        ParentOf_Edge_File.close();

        Individual_Genome_Edge_File.flush();
        Individual_Genome_Edge_File.close();

        Individual_TotalError_Edge_File.flush();
        Individual_TotalError_Edge_File.close();

        TotalError_SingleError_Edge_File.flush();
        TotalError_SingleError_Edge_File.close();
    }

    private class Individual{
        String Individual_ID;
        String Generation;
        String Location;
        String Genetic_Operators;
        String TotalError;
        ArrayList<Object> ErrorVector;
        ArrayList<Object> Genomes;
        Collection<Object> Parents;


        private   void set(Map<?,?> individualMap){
            Individual_ID = individualMap.get(newKeyword("uuid")).toString();
            Generation = individualMap.get(newKeyword("generation")).toString();
            Location = individualMap.get(newKeyword("location")).toString();
            Genetic_Operators = individualMap.get(newKeyword("genetic-operators")).toString();
            TotalError = individualMap.get(newKeyword("total-error")).toString();
            ErrorVector = new ArrayList<Object>((Collection<Object>)individualMap.get(newKeyword("errors")));
            Genomes = new ArrayList<Object>((Collection<Object>)individualMap.get(newKeyword("genome")));
            Parents = (Collection<Object>)individualMap.get(newKeyword("parent-uuids"));
        }

    }

}
