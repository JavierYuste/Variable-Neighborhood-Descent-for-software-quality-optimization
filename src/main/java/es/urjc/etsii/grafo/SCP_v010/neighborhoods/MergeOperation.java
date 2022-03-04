package es.urjc.etsii.grafo.SCP_v010.neighborhoods;

import es.urjc.etsii.grafo.SCP_v010.model.MDGSolution;

public class MergeOperation extends AbstractOperation {

    protected int currentModuleIndex;
    protected int currentModule;
    protected boolean improves;
    protected int candidateModuleIndex;
    protected int candidateModule;

    public MergeOperation(MDGSolution mdgSolution) {
        super(mdgSolution);
        this.currentModuleIndex = 0;
        if (this.s.getUsedClusters().size() > 1) {
            this.candidateModuleIndex = 1;
        }
        else {
            this.currentModuleIndex = 0;
        }
        this.currentModule = (int) this.s.getUsedClusters().toArray()[this.currentModuleIndex];
        this.candidateModule = (int) this.s.getUsedClusters().toArray()[this.candidateModuleIndex];
        this.constructor();
    }

    public MergeOperation(MDGSolution mdgSolution, int currentModuleIndex, int candidateModuleIndex) {
        super(mdgSolution);
        this.currentModuleIndex = currentModuleIndex;
        this.candidateModuleIndex = candidateModuleIndex;
        this.currentModule = (int) this.s.getUsedClusters().toArray()[this.currentModuleIndex];
        this.candidateModule = (int) this.s.getUsedClusters().toArray()[this.candidateModuleIndex];
        this.constructor();
    }

    protected double getResult(){
        return s.simulateMoves(movesList);
    }

    @Override
    protected void initializeData() {
        for (Integer vertex : this.s.getVerticesInCluster(this.candidateModule)) {
            this.movesList.put(vertex, this.currentModule);
        }
    }

    @Override
    public MergeOperation next() {
        if (this.candidateModuleIndex < this.s.getUsedClusters().size() - 1){
            return new MergeOperation(this.s, this.currentModuleIndex, this.candidateModuleIndex + 1);
        }
        else if (this.currentModuleIndex < this.s.getUsedClusters().size() - 2) {
            return new MergeOperation(this.s, this.currentModuleIndex + 1, this.currentModuleIndex + 2);
        }
        else{
            return null;
        }
    }

    @Override
    public boolean isValid() {
        return s.existsCluster(currentModule) && s.existsCluster(candidateModule) && currentModule != candidateModule;
    }

    @Override
    protected void _execute() {
        Object[] vertices = this.s.getVerticesInCluster(this.candidateModule).toArray();
        for (Object vertex : vertices) {
            this.s.moveVertex((Integer) vertex, this.currentModule);
        }
    }

    @Override
    public String toString() {
        return "MergeOperation";
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
