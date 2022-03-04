package es.urjc.etsii.grafo.SCP_v010.neighborhoods;

import es.urjc.etsii.grafo.SCP_v010.model.MDGSolution;
import org.apache.commons.math3.util.Pair;

import java.util.*;

public class SplitOperation extends AbstractOperation {

    protected int mainModule;
    protected int mainModuleIndex;
    protected int destinationModule;
    protected int affectedVertexIndex;
    protected Object[] candidateVertices;
    protected boolean improves;
    public static final int NULL_VERTEX = -1;

    public SplitOperation(MDGSolution mdgSolution) {
        super(mdgSolution);
        this.mainModuleIndex = 0;
        this.mainModule = (int) mdgSolution.getUsedClusters().toArray()[this.mainModuleIndex];
        this.affectedVertexIndex = 0;
        candidateVertices = mdgSolution.getVerticesInCluster(mainModule).toArray();
        this.destinationModule = mdgSolution.getAvailableModule();
        this.constructor();
    }

    public SplitOperation(MDGSolution mdgSolution, int mainModuleIndex, int affectedVertexIndex) {
        super(mdgSolution);
        this.mainModuleIndex = mainModuleIndex;
        this.mainModule = (int) mdgSolution.getUsedClusters().toArray()[this.mainModuleIndex];
        this.affectedVertexIndex = affectedVertexIndex;
        candidateVertices = mdgSolution.getVerticesInCluster(mainModule).toArray();
        this.destinationModule = mdgSolution.getAvailableModule();
        this.constructor();
    }

    @Override
    protected void initializeData() {
        int numberOfVerticesToMove = this.candidateVertices.length/2;
        this.movesList.put((Integer) this.candidateVertices[affectedVertexIndex], destinationModule);
        if (numberOfVerticesToMove > 0){
            List<Pair<Integer, Integer>> connections = new ArrayList<>();
            for (int i = 0; i < this.candidateVertices.length; i++) {
                if (i != affectedVertexIndex){
                    connections.add(new Pair(i, this.s.getConnectionValue((Integer) candidateVertices[affectedVertexIndex], (Integer) candidateVertices[i])));
                }
            }
            connections.sort((o1, o2) -> {
                if (o1.getValue() > o2.getValue()) {
                    return -1;
                } else if (o1.getValue().equals(o2.getValue())) {
                    return 0;
                } else {
                    return 1;
                }
            });
            for (int i = 0; i < numberOfVerticesToMove; i++) {
                this.movesList.put((Integer) this.candidateVertices[connections.get(i).getKey()], destinationModule);
            }
        }
    }

    protected double getResult(){
        return this.s.simulateMoves(this.movesList);
    }

    @Override
    public SplitOperation next() {
        if (this.affectedVertexIndex < this.candidateVertices.length - 1){
            return new SplitOperation(this.s, this.mainModuleIndex, this.affectedVertexIndex + 1);
        }
        else if (this.affectedVertexIndex >= this.candidateVertices.length - 1 && mainModuleIndex < this.s.getUsedClusters().size() - 1){
            return new SplitOperation(this.s, this.mainModuleIndex + 1, 0);
        }
        else{
            return null;
        }
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    protected void _execute() {
        for (Integer vertex : this.movesList.keySet()) {
            this.s.moveVertex(vertex, this.movesList.get(vertex));
        }
    }

    @Override
    public String toString() {
        return "SplitOperation";
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

}
