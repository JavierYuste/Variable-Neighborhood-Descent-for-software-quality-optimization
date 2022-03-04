package es.urjc.etsii.grafo.SCP_v010.neighborhoods;

import es.urjc.etsii.grafo.SCP_v010.model.MDGSolution;

public class InsertOperation extends AbstractOperation {

    protected int vertexToMove;
    protected int connectedVertexIndex;
    protected Object[] connectedVertices;
    protected boolean improves;
    protected int destinationCluster;

    public InsertOperation(MDGSolution mdgSolution) {
        super(mdgSolution);
        this.vertexToMove = 0;
        this.connectedVertexIndex = 0;
        this.constructor();
    }

    public InsertOperation(MDGSolution mdgSolution, int vertexToMove, int connectedVertex) {
        super(mdgSolution);
        this.vertexToMove = vertexToMove;
        this.connectedVertexIndex = connectedVertex;
        this.constructor();
    }

    protected double getResult(){
        return this.s.simulateMoves(movesList);
    }

    @Override
    protected void initializeData() {
        connectedVertices = this.s.getAdjacentVerticesArray(this.vertexToMove);
        destinationCluster = this.s.getClusterOfVertex((Integer) connectedVertices[connectedVertexIndex]);
        movesList.put(this.vertexToMove, this.destinationCluster);
    }

    @Override
    public InsertOperation next() {
        if (this.vertexToMove == (this.s.getClusters().length - 1) && this.connectedVertexIndex == (connectedVertices.length - 1)){
            return null;
        }
        else if ((connectedVertices.length - 1) == connectedVertexIndex){
            return new InsertOperation(this.s, this.vertexToMove+1, 0);
        }
        else {
            return new InsertOperation(this.s, this.vertexToMove, this.connectedVertexIndex+1);
        }
    }

    @Override
    public boolean isValid() {
        if (this.connectedVertexIndex >= this.s.getAdjacentVertices(this.vertexToMove).size()){
            return false;
        }
        else if (this.s.getClusterOfVertex(vertexToMove) == this.destinationCluster) {
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    protected void _execute() {
        this.s.moveVertex(this.vertexToMove, this.destinationCluster);
    }

    @Override
    public String toString() {
        return "InsertOperation";
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
