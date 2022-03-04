package es.urjc.etsii.grafo.SCP_v010.neighborhoods;

import es.urjc.etsii.grafo.SCP_v010.model.MDGInstance;
import es.urjc.etsii.grafo.SCP_v010.model.MDGSolution;
import es.urjc.etsii.grafo.solution.LazyMove;

import java.util.HashMap;

public abstract class AbstractOperation extends LazyMove<MDGSolution, MDGInstance> {

    protected double simulationResult;
    protected boolean improves;
    protected HashMap<Integer, Integer> movesList;

    public AbstractOperation(MDGSolution solution) {
        super(solution);
        this.movesList = new HashMap<>();
    }

    public void constructor(){
        this.initializeData();
        this.simulationResult = this.getResult();
        updateImprovement();
    }

    protected abstract double getResult();

    protected abstract void initializeData();

    protected void updateImprovement() {
        improves = this.simulationResult < this.s.getFCBBeforeDivision();
    }

    @Override
    public double getValue() {
        return this.simulationResult;
    }

    @Override
    public boolean improves() {
        return this.improves;
    }

}
