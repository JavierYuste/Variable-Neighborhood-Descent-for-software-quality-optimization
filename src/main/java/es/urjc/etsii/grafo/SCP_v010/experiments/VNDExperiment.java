package es.urjc.etsii.grafo.SCP_v010.experiments;

import es.urjc.etsii.grafo.SCP_v010.constructives.RandomConstructive;
import es.urjc.etsii.grafo.SCP_v010.model.MDGInstance;
import es.urjc.etsii.grafo.SCP_v010.model.MDGSolution;
import es.urjc.etsii.grafo.SCP_v010.neighborhoods.InsertNeighborhood;
import es.urjc.etsii.grafo.SCP_v010.neighborhoods.SplitNeighborhood;
import es.urjc.etsii.grafo.SCP_v010.neighborhoods.MergeNeighborhood;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.solver.improve.VND;
import es.urjc.etsii.grafo.solver.improve.ls.LocalSearchFirstImprovement;
import es.urjc.etsii.grafo.solver.services.AbstractExperiment;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

public class VNDExperiment extends AbstractExperiment<MDGSolution, MDGInstance> {

    public VNDExperiment(@Value("${solver.maximizing}") boolean maximizing) {
        super(maximizing);
    }

    @Override
    public List<Algorithm<MDGSolution, MDGInstance>> getAlgorithms() {
        // In this experiment we will compare a random constructive with several GRASP constructive configurations
        boolean maximizing = super.isMaximizing();
        var algorithms = new ArrayList<Algorithm<MDGSolution, MDGInstance>>();

        List improvers = new ArrayList();
        improvers.add(new LocalSearchFirstImprovement<>(maximizing, new InsertNeighborhood()));
        improvers.add(new LocalSearchFirstImprovement<>(maximizing, new SplitNeighborhood()));
        improvers.add(new LocalSearchFirstImprovement<>(maximizing, new MergeNeighborhood()));

        // Minimize FCB
        algorithms.add(new SimpleAlgorithm<>("VND", new RandomConstructive(), new VND<>(improvers, maximizing)));

        return algorithms;
    }
}
