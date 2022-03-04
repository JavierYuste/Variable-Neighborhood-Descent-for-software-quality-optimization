package es.urjc.etsii.grafo.SCP_v010.constructives;

import es.urjc.etsii.grafo.SCP_v010.model.MDGInstance;
import es.urjc.etsii.grafo.SCP_v010.model.MDGSolution;
import es.urjc.etsii.grafo.solver.create.Constructive;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;

public class RandomConstructive extends Constructive<MDGSolution, MDGInstance> {

    private static final Logger log = Logger.getLogger(es.urjc.etsii.grafo.SCP_v010.constructives.RandomConstructive.class.getName());

    @Override
    public MDGSolution construct(MDGSolution solution) {
        // IN --> Empty solution from solution(instance) constructor
        // OUT --> Feasible solution with an assigned score
        // Remember to call solution.updateLastModifiedTime() before returning the solution!!
        // Move all vertices out of the solution
        List<Integer> verticesSolution = new ArrayList<>();
        for (int vertex = 0; vertex < solution.getDependencyMatrix().length; vertex++) {
            solution.moveVertexPartialSolution(vertex, MDGSolution.NON_EXISTENT_CLUSTER);
            verticesSolution.add(vertex);
        }

        RandomGenerator random = RandomManager.getRandom();
        // Move each vertex to an existent module or to a new module
        int index = random.nextInt(verticesSolution.size());
        int vertex = verticesSolution.get(index);
        verticesSolution.remove(index);
        solution.moveVertexPartialSolution(vertex, solution.getAvailableModule());
            // Randomize the order of the vertices
        while(!verticesSolution.isEmpty()){
            index = random.nextInt(verticesSolution.size());
            vertex = verticesSolution.get(index);
            verticesSolution.remove(index);
            Object[] usedClusters = solution.getUsedClusters().toArray();
            int destinationModule = random.nextInt(-1, usedClusters.length);
            if (destinationModule == -1){
                solution.moveVertexPartialSolution(vertex, solution.getAvailableModule());
            }
            else {
                solution.moveVertexPartialSolution(vertex, (Integer) usedClusters[destinationModule]);
            }
        }

        solution.updateLastModifiedTime();
        return solution;
    }
}
