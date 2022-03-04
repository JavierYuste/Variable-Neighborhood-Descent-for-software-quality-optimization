package es.urjc.etsii.grafo.SCP_v010.neighborhoods;

import es.urjc.etsii.grafo.SCP_v010.model.MDGSolution;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.LazyNeighborhood;

import java.util.stream.Stream;

public class InsertNeighborhood extends LazyNeighborhood {


    @Override
    public Stream stream(Solution solution) {
        if (solution.getClass() != MDGSolution.class){
            return null;
        }
        InsertOperation firstMove = new InsertOperation((MDGSolution) solution);
        return this.buildStream(firstMove);
    }
}
