package it.poa.lab.simlib.datamodel.smz;

import it.poa.lab.simlib.datamodel.smz.api.AKP;
import it.poa.lab.simlib.datamodel.Arrow;
import it.poa.lab.simlib.datamodel.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by giorgio on 12/05/16.
 */
public class SmzNode extends Node {

    private int occurrence = 0;

    public SmzNode(String id, SmzGraph graphRef) {
        super(id, graphRef);
    }

    @Override
    public HashMap<Arrow, HashSet<Node>> getArrows(){
        if(arrows.size() == 0) {
            HashMap<Arrow, HashSet<Node>> nodeArrows = new HashMap<>();
            SmzGraph smzGraph = (SmzGraph) graphRef;
            List<AKP> akpListOut = smzGraph.getAkps(smzGraph.getGraphURI(), id, null, null, null, String.valueOf(Integer.MAX_VALUE), null);
            List<AKP> akpListIn = smzGraph.getAkps(smzGraph.getGraphURI(), null, null, id, null, String.valueOf(Integer.MAX_VALUE), null);

            for(AKP akp : akpListOut){
                String pred = akp.getPredValue();
                String obj = akp.getObjValue();

                smzGraph.createNode(pred);
                smzGraph.createNode(obj);

                SmzNode property = (SmzNode) smzGraph.getNode(akp.getPredValue());
                SmzNode object = (SmzNode) smzGraph.getNode(akp.getObjValue());

                SmzArrow smzArrow = new SmzArrow(property, Arrow.DIR_OUT);
                if(!nodeArrows.containsKey(smzArrow)){
                    nodeArrows.put(smzArrow, new HashSet<>());
                }
                nodeArrows.get(smzArrow).add(object);
            }


            for(AKP akp : akpListIn){
                String pred = akp.getPredValue();
                String subj = akp.getSubjValue();

                smzGraph.createNode(pred);
                smzGraph.createNode(subj);

                SmzNode property = (SmzNode) smzGraph.getNode(akp.getPredValue());
                SmzNode subject = (SmzNode) smzGraph.getNode(akp.getSubjValue());

                SmzArrow smzArrow = new SmzArrow(property, Arrow.DIR_IN);
                if(!nodeArrows.containsKey(smzArrow)){
                    nodeArrows.put(smzArrow, new HashSet<>());
                }
                nodeArrows.get(smzArrow).add(subject);
            }
            arrows = nodeArrows;
        }
        return arrows;

    }

}
