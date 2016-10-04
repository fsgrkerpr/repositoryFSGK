/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.poa.lab.simlib;

import com.jcraft.jsch.JSchException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import it.poa.lab.simlib.datamodel.Graph;
import it.poa.lab.simlib.datamodel.GraphFactory;
import it.poa.lab.simlib.datamodel.Node;
import it.poa.lab.simlib.datamodel.Path;
import it.poa.lab.simlib.exceptions.InvalidGraphException;
import it.poa.lab.simlib.input.StatementFileReader;
import it.poa.lab.simlib.neighborhood.KernelMetric;
import it.poa.lab.simlib.neighborhood.pathbaseditem.NeighborhoodPathKernelMetric;
import it.poa.lab.simlib.neighborhood.pathbaseditem.ReachablePaths;
import java.io.BufferedReader;



import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Corrado on 05/04/2016.
 */
public class MainTestEvalPath {
    
    //public static final String METADATA = "data/movies/metadata.txt";
    public static final String METADATA2 = "data/graph_wikidata.txt";
    public static final String ITEMS2 = "data/mapping_wikidata.txt";
    //public static final String ITEMS = "data/movies/mapping_completo";
    //public static final String FEATURED_PATHS = "data/movies/featuredPaths.txt";
    
    
    //public static final String METADATA = "data/movies/metadata.txt";
    //public static final String METADATA = "data/movies/graph_dbpedia_film_SMZeatures5OP_utf8.txt";
    //public static final String METADATA = "data/movies/graph_dbpedia_film_SMZfeatures5OPnoonto_utf8.txt";
    //public static final String METADATA = "data/movies/graph_dbpedia_film_SMZfeatures5OPwithoutrepet_utf8.txt";
    //public static final String METADATA = "data/movies/graph_dbpedia_film_cleaningIGfeatures15withoutprop_utf8.txt";
    //public static final String METADATA = "data/movies/graph_dbpedia_film_cleaningIGfeatures15withoutonto_utf8.txt";
    public static final String METADATA = "data/movies/graph_dbpedia_film_cleaningIGfeatures5withoutrepet_utf8.txt"; 
    //public static final String METADATA = "data/movies/graph_dbpedia_film_SMZfeatures5OP+DTP_utf8.txt";
    //public static final String METADATA = "data/movies/graph_dbpedia_film_SMZTfeatures10OP+DTPnoonto_utf8.txt";
    
    public static final String ITEMS = "./data/movies/mapping_completo";
    public static final String FEATURED_PATHS = "./data/movies/featuredPaths.txt";

    public static void main(String[] args) throws JSchException {
        try {
            // Creating a File object that represents the disk file.
            //PrintStream o = new PrintStream(new File("Output.txt"));
            //System.setOut(o);
            
            int distance = 2;

            File graphFile = new File(METADATA);
            File itemsFile = new File(ITEMS);
            
            List<String> itemsIDs = readItems(itemsFile);
            Graph graph = GraphFactory.create(graphFile, itemsIDs, GraphFactory.TRIPLE_GRAPH);
            HashSet<Path> featuredPaths = new HashSet<>(readPaths(new File(FEATURED_PATHS), graph));
            
            System.out.println();

            long startTime = System.currentTimeMillis();
            Set<String> items = graph.getItems().keySet();
            
            KernelMetric neighborhoodpathKernel = new NeighborhoodPathKernelMetric(graph,distance, StatementFileReader.TAB_TRIPLES);
            Map<Node, Double> totmap = new HashMap<>();
            List<String> raccomandations = new ArrayList<>();
            Map<String,ReachablePaths> reachpathvectors = new HashMap<>();
            
            
            //MODIFICA
            Map<String,LinkedHashMap<Integer,Float>> weightvectors = new HashMap<>();

            itemsIDs.parallelStream().filter(p->graph.getItem(p)!=null).forEach(
                    //.filter(p->!p.contains("\""))
                    (String s)->
                    {
                        System.out.println(s);
                        weightvectors.put(s, neighborhoodpathKernel.computeSingleVector3(graph.getNode(s)));
                    }
                            );

            int z=0;
            Map<String,Map<String,Double>> totracc3 = new HashMap<>();
            for(Map.Entry<String, LinkedHashMap<Integer,Float>> wv:weightvectors.entrySet())
            {         
            z++;
            Map<String, Double> rank =
                weightvectors.entrySet().parallelStream()
                        .filter(e -> !e.getKey().equals(wv.getKey())).filter(e->!totracc3.containsKey(e.getKey()))
                        .collect(Collectors.toMap(
                                e -> e.getKey(),
                                e -> compareVectorandDot(wv, e)
                                //e -> Utilities.cosineSimilarity(wv.getValue(), e.getValue())
                        ));
            
            Map<String, Double> orderedrank = Utilities.sortByValues(rank);
            totracc3.put(wv.getKey(), orderedrank);
            if(orderedrank!=null)
            {
                orderedrank.entrySet().parallelStream().filter(p->!p.getValue().equals(0.0)).forEachOrdered(r->raccomandations.add(wv.getKey()+"\t"+r.getKey()+"\t"+r.getValue()));
            }
            System.out.println(z);            
            
            }
            createFileRaccomandation(raccomandations);
            
            //FINE MODIFICA
            
            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            System.out.println("Execution time item: " + elapsedTime / 1000D);

      
        }catch (IOException | InvalidGraphException e){
            e.printStackTrace();
        }
    }

    
    @SuppressWarnings("empty-statement")
     private static double compareVectorandDot(Entry<String,LinkedHashMap<Integer,Float>> weightvectors1,Entry<String,LinkedHashMap<Integer,Float>> weightvectors2)
   {
        LinkedHashMap<Integer,Float> wv1 = new LinkedHashMap<>();
        LinkedHashMap<Integer,Float> wv2 = new LinkedHashMap<>();       
        weightvectors1.getValue().entrySet().stream().forEach(
                e-> {
                        wv1.put(e.getKey(), (float)0.0);
                        wv2.put(e.getKey(), (float)0.0);
                        }
        );
                        
        weightvectors2.getValue().entrySet().stream().forEach(
                e-> {
                        wv1.put(e.getKey(), (float)0.0);
                        wv2.put(e.getKey(), (float)0.0);
                        }
        );
        
        weightvectors1.getValue().entrySet().parallelStream().forEach(
                e-> wv1.put(e.getKey(), e.getValue()));
        weightvectors2.getValue().entrySet().parallelStream().forEach(
                e-> wv2.put(e.getKey(), e.getValue()));
        
        double[] weight1 = wv1.values().parallelStream().mapToDouble(e -> e).toArray();
        double[] weight2 = wv2.values().parallelStream().mapToDouble(e -> e).toArray();
        
        return Utilities.cosineSimilarity(weight1, weight2);
  }
    
    
    private static Set<Path> readPaths(File inputFile, Graph graph) throws IOException {
        return Files.lines(Paths.get(inputFile.getPath()), Charset.defaultCharset())
                .map(l -> graph.createPath(l.split("--")))
                .collect(toSet());
    }

    private static List<String> readItems(File inputFile) throws IOException {
        return Files.lines(Paths.get(inputFile.getPath()), Charset.forName("UTF-8"))
                .map(l -> l.split("\t"))
                .filter(f -> f[1].equals("movie"))
                .map(f -> f[2])
                .collect(toList());
    }
    
    private static void createFileRaccomandation(List<String> raccomandations) throws IOException
    {
          //java.nio.file.Path file2 = Paths.get("data/10Path3distrecomendetions.txt");
          java.nio.file.Path file2 = Paths.get("data/NOREPETIG10Path3distrecommendations.txt"); 
          Files.write(file2, raccomandations, Charset.forName("UTF-8"));

    }
    
     private static List<String> readFile(String filename)throws Exception
    {
        String line = null;
        List<String> records = new ArrayList<String>();

        BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
        /*do{
            line = bufferedReader.readLine();
        }while (!line.startsWith("@data"));*/
        
        line = bufferedReader.readLine();
        while(line!=null){
            
            records.add(line);
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        return records;
    }
}