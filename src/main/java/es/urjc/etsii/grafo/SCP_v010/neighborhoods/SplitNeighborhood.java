package es.urjc.etsii.grafo.SCP_v010.neighborhoods;

import es.urjc.etsii.grafo.SCP_v010.model.MDGSolution;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.LazyNeighborhood;

import java.util.stream.Stream;

public class SplitNeighborhood extends LazyNeighborhood {


    @Override
    public Stream stream(Solution solution) {
        if (solution.getClass() != MDGSolution.class){
            return null;
        }
        SplitOperation firstMove = new SplitOperation((MDGSolution) solution);
        return this.buildStream(firstMove);
    }
}
