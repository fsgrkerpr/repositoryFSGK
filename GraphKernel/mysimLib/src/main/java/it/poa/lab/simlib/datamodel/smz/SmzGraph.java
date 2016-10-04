package it.poa.lab.simlib.datamodel.smz;

import it.poa.lab.simlib.datamodel.Arrow;
import it.poa.lab.simlib.datamodel.Graph;
import it.poa.lab.simlib.datamodel.Node;
import it.poa.lab.simlib.datamodel.smz.api.AKP;
import it.poa.lab.simlib.datamodel.smz.api.SmzResponse;
import retrofit2.Call;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import it.poa.lab.simlib.datamodel.smz.api.SmzService;

/**
 * Created by giorgio on 12/05/16.
 */
public class SmzGraph extends Graph {

    private SmzService smzService;
    private int statementsCount = -1;
    private String graphURI;

    public SmzGraph(String graphURI){
        super();
        this.graphURI = graphURI;
        this.smzService = SmzService.serviceFactory();
    }

    @Override
    public Node createNode(String id){
        if(!nodes.containsKey(id)){
            nodes.put(id, new SmzNode(id, this));
        }
        return nodes.get(id);
    }

    @Override
    public Node getNode(String id){
        return createNode(id);
    }

    @Override
    public int getArrowOccurrences(Arrow arrow){
        return getAkps(graphURI, null, arrow.getProperty().getId(), null,
                null, String.valueOf(Integer.MAX_VALUE), null).size();
    }

    @Override
    public int getStatementsCount(){
        if(statementsCount == -1){
            statementsCount = getAkps(graphURI, null, null, null,
                    null, String.valueOf(Integer.MAX_VALUE), null).size();
            // 374157 in dbpedia-2015-10
        }
        return statementsCount;
    }

    public SmzService getSmzService() {
        return smzService;
    }

    public void setSmzService(SmzService smzService) {
        this.smzService = smzService;
    }

    public List<AKP> getAkps(String dataset, String subject, String predicate, String object,
                              String ranking, String limit, String format){

        Call<SmzResponse> smzResponse =
                getSmzService().minimalPattern(dataset, subject, predicate, object, ranking, limit, format);

        try {

            SmzResponse resp = smzResponse.execute().body();
            List<AKP> akps = resp.listAKPs();
            return akps;

        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public String getGraphURI() {
        return graphURI;
    }

    public void setGraphURI(String graphURI) {
        this.graphURI = graphURI;
    }
}
