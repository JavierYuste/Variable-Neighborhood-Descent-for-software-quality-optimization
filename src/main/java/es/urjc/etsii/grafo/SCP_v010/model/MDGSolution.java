package es.urjc.etsii.grafo.SCP_v010.model;

import com.google.common.collect.Iterators;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.Traverser;
import com.google.common.graph.ValueGraphBuilder;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class MDGSolution extends Solution<MDGSolution, MDGInstance> {

    private int fcb;
    private final int[] clusters;
    private final int[][] dependencyMatrix;
    private final int[] numberOfVerticesInCluster;
    private final HashMap<Integer, Set<Integer>> verticesInCluster;
    private int[] connectionsSum;
    private final Deque<Integer> availableClusters;
    protected Set<Integer> usedClusters;
    private int coupling = 0;
    private int maxCohesion = 0;
    private int[] cohesion;
    private int fcbT = 0;
    private MutableValueGraph<Integer, Integer> guavaGraph = null;
    public static final int NON_EXISTENT_CLUSTER = -1;
    private int nonExistentVertices = 0;
    private static final Logger log = Logger.getLogger(MDGSolution.class.toString());

    public MDGSolution(MDGInstance ins) {
        super(ins);
        this.clusters = new int[ins.clustersInstance.length];
        this.dependencyMatrix = ins.getDependencyMatrix().clone();
        this.preprocessGraph();
        this.numberOfVerticesInCluster = new int[this.clusters.length];
        this.availableClusters = new ArrayDeque<>();
        this.usedClusters = new HashSet<>();
        this.verticesInCluster = new HashMap<>();
        for (int i = 0; i < this.clusters.length; i++){
            this.clusters[i] = i;
            this.numberOfVerticesInCluster[i] = 1;
            HashSet<Integer> moduleList = new HashSet<>();
            moduleList.add(i);
            this.verticesInCluster.put(i, moduleList);
            this.usedClusters.add(i);
        }
        this.createInternalGuavaGraph();
        this.fcbT = this.getTotalWeights();
        this.initializeCachedInformation();
    }


    public MDGSolution(MDGSolution s) {
        super(s);
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Object[] getAdjacentVerticesArray(int vertex){
        return this.guavaGraph.adjacentNodes(vertex).toArray();
    }

    public Set<Integer> getUsedClusters() {
        return usedClusters;
    }

    public Deque<Integer> getAvailableClusters() {
        return availableClusters;
    }

    public int getAvailableModule(){
        if (this.availableClusters.size() > 0) {
            return this.availableClusters.peek();
        }
        else{
            return this.clusters[RandomManager.getRandom().nextInt(this.clusters.length)];
        }
    }

    public Set<Integer> getAdjacentVertices(int vertex){
        return this.guavaGraph.adjacentNodes(vertex);
    }

    public MutableValueGraph<Integer, Integer> createGuavaGraph(int[][] dependencyMatrix){
        MutableValueGraph<Integer, Integer> localGuavaGraph = ValueGraphBuilder.undirected().
                incidentEdgeOrder(ElementOrder.stable()).
                allowsSelfLoops(true).
                expectedNodeCount(dependencyMatrix.length-1).
                build();
        for (int row = 0; row < dependencyMatrix.length; row++){
            localGuavaGraph.addNode(row);
        }
        for (int row = 0; row < dependencyMatrix.length; row++){
            for (int column = 0; column <= row; column++){
                if (Math.max(dependencyMatrix[row][column], dependencyMatrix[column][row]) > 0) {
                    localGuavaGraph.putEdgeValue(row, column, Math.max(dependencyMatrix[row][column], dependencyMatrix[column][row]));
                }
            }
        }
        return localGuavaGraph;
    }

    private void createInternalGuavaGraph(){
        if (this.guavaGraph == null) {
            this.guavaGraph = this.createGuavaGraph(this.dependencyMatrix);
        }
    }

    @Override
    public MDGSolution cloneSolution() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    protected boolean _isBetterThan(MDGSolution other) {
        return this.getScore() < other.getScore();
    }

    @Override
    public double getScore() {
        if (this.fcbT > 0) {
            return (double) this.fcb / this.fcbT;
        }
        else{
            return 0d;
        }
    }

    public double getFCBBeforeDivision() {
        return this.fcb;
    }

    @Override
    public double recalculateScore() {
        int[] localCohesion = new int[this.clusters.length];
        int localCoupling = 0;
        for (int vertex_1 = 0; vertex_1 < this.clusters.length; vertex_1++){
            for (int vertex_2 = 0; vertex_2 <= vertex_1; vertex_2++){
                if (this.clusters[vertex_1] != NON_EXISTENT_CLUSTER && this.clusters[vertex_2] != NON_EXISTENT_CLUSTER && this.clusters[vertex_1] == this.clusters[vertex_2]){
                    localCohesion[this.clusters[vertex_1]] += this.getConnectionValue(vertex_1, vertex_2);
                }
                else if (this.clusters[vertex_1] != NON_EXISTENT_CLUSTER && this.clusters[vertex_2] != NON_EXISTENT_CLUSTER){
                    localCoupling += this.getConnectionValue(vertex_1, vertex_2);
                }
            }
        }

        int localMaxCohesion = Integer.MIN_VALUE;
        for (int i = 0; i < this.cohesion.length; i++){
            if (localCohesion[i] > localMaxCohesion){
                localMaxCohesion = localCohesion[i];
            }
        }

        int localFcb = localCoupling + localMaxCohesion;
        if (this.fcbT > 0) {
            return (double) localFcb / this.fcbT;
        }
        else {
            return 0d;
        }
    }

    public int getTotalWeights(){
        int result = 0;
        for (int i = 0; i < this.dependencyMatrix.length; i++){
            for (int j = 0; j <= i; j++){
                result += this.getConnectionValue(i, j);
            }
        }
        return result;
    }

    public void initializeCachedInformation() {
        this.cohesion = new int[this.clusters.length];
        this.coupling = 0;
        for (int vertex_1 = 0; vertex_1 < this.clusters.length; vertex_1++){
            for (int vertex_2 = 0; vertex_2 <= vertex_1; vertex_2++){
                if (this.clusters[vertex_1] == this.clusters[vertex_2]){
                    this.cohesion[this.clusters[vertex_1]] += this.getConnectionValue(vertex_1, vertex_2);
                }
                else{
                    this.coupling += this.getConnectionValue(vertex_1, vertex_2);
                }
            }
        }

        this.maxCohesion = Integer.MIN_VALUE;
        for (int i = 0; i < this.cohesion.length; i++){
            if (this.cohesion[i] > this.maxCohesion){
                this.maxCohesion = this.cohesion[i];
            }
        }

        this.fcb = this.coupling + this.maxCohesion;
    }

    public void preprocessGraph(){
        int[] connectionsCount = new int[this.dependencyMatrix.length];

        this.connectionsSum = new int[this.clusters.length];
        for (int vertex_1 = 0; vertex_1 < this.dependencyMatrix.length; vertex_1++){
            this.connectionsSum[vertex_1] = 0;
            for (int vertex_2 = 0; vertex_2 < this.dependencyMatrix.length; vertex_2++){
                if (this.dependencyMatrix[vertex_1][vertex_2] > 0 || this.dependencyMatrix[vertex_2][vertex_1] > 0){
                    connectionsCount[vertex_1]++;
                }
                this.connectionsSum[vertex_1] += this.dependencyMatrix[vertex_1][vertex_2];
                if (vertex_1 != vertex_2) {
                    this.connectionsSum[vertex_1] += this.dependencyMatrix[vertex_2][vertex_1];
                }
            }
        }

        for (int i = 0; i < this.clusters.length; i++){
            this.clusters[i] = i;
        }
    }

    @Override
    public String toString() {
        return String.format("FCB: %.4f - Vertices: %d - Clusters: %d - Coupling: %d - Max Cohesion: %d",
                this.getScore(), this.getClusters().length, this.getNumberOfClusters(), this.coupling, this.maxCohesion);
    }

    public int getConnectionValue(int vertex1, int vertex2){
        return this.guavaGraph.edgeValue(vertex1, vertex2).orElse(0);
    }

    public int getNumberOfClusters(){
        int numberOfClusters = 0;
        int[] nodesPerCluster = new int[this.clusters.length];
        for(int i = 0; i < this.clusters.length; i++){
            nodesPerCluster[i] = 0;
        }
        for(int i = 0; i < this.clusters.length; i++){
            if (this.clusters[i] >= 0) {
                nodesPerCluster[this.clusters[i]]++;
            }
        }
        for(int i = 0; i < this.clusters.length; i++){
            if (nodesPerCluster[i] > 0){
                numberOfClusters++;
            }
        }
        return numberOfClusters;
    }

    public void moveVertexPartialSolution(int vertex, int targetCluster){
        if (this.clusters[vertex] == targetCluster){
            return;
        }
        int originalCluster = this.clusters[vertex];
        for (int connectedVertex : this.guavaGraph.adjacentNodes(vertex)) {
            int connectionValue = this.getConnectionValue(vertex, connectedVertex);
            int connectedVertexCluster = this.clusters[connectedVertex];

            if(vertex == connectedVertex){
                if (originalCluster == NON_EXISTENT_CLUSTER && targetCluster != NON_EXISTENT_CLUSTER) {
                    this.cohesion[targetCluster] += connectionValue;
                    this.fcbT += connectionValue;
                }
                else if (originalCluster != NON_EXISTENT_CLUSTER && targetCluster == NON_EXISTENT_CLUSTER){
                    this.cohesion[originalCluster] -= connectionValue;
                    this.fcbT -= connectionValue;
                }
                else if (originalCluster != NON_EXISTENT_CLUSTER && targetCluster != NON_EXISTENT_CLUSTER){
                    this.cohesion[originalCluster] -= connectionValue;
                    this.cohesion[targetCluster] += connectionValue;
                }
            }
            else if (originalCluster != NON_EXISTENT_CLUSTER && targetCluster != NON_EXISTENT_CLUSTER && connectedVertexCluster != NON_EXISTENT_CLUSTER){
                if (vertex == connectedVertex) {
                    this.cohesion[originalCluster] -= connectionValue;
                    this.cohesion[targetCluster] += connectionValue;
                }
                else if (originalCluster == connectedVertexCluster) {
                    this.coupling += connectionValue;
                    this.cohesion[originalCluster] -= connectionValue;
                }
                else if (targetCluster == connectedVertexCluster) {
                    this.coupling -= connectionValue;
                    this.cohesion[targetCluster] += connectionValue;
                }
            }
            else if (originalCluster == NON_EXISTENT_CLUSTER && targetCluster != NON_EXISTENT_CLUSTER && connectedVertexCluster != NON_EXISTENT_CLUSTER){
                if (vertex == connectedVertex) {
                    this.cohesion[targetCluster] += connectionValue;
                }
                else if (targetCluster != connectedVertexCluster) {
                    this.coupling += connectionValue;
                }
                else if (targetCluster == connectedVertexCluster) {
                    this.cohesion[targetCluster] += connectionValue;
                }
                this.fcbT += connectionValue;
            }
            else if (originalCluster != NON_EXISTENT_CLUSTER && targetCluster == NON_EXISTENT_CLUSTER && connectedVertexCluster != NON_EXISTENT_CLUSTER) {
                if (vertex == connectedVertex) {
                    this.cohesion[originalCluster] -= connectionValue;
                }
                else if (originalCluster == connectedVertexCluster) {
                    this.cohesion[originalCluster] -= connectionValue;
                }
                else if (originalCluster != connectedVertexCluster) {
                    this.coupling -= connectionValue;
                }
                this.fcbT -= connectionValue;
            }
        }

        this.maxCohesion = Integer.MIN_VALUE;
        for (int module = 0; module < this.clusters.length; module++) {
            if (this.cohesion[module] > this.maxCohesion) {
                this.maxCohesion = this.cohesion[module];
            }
        }
        this.fcb = this.maxCohesion + this.coupling;
        this.clusters[vertex] = targetCluster;

        if (originalCluster != NON_EXISTENT_CLUSTER && targetCluster == NON_EXISTENT_CLUSTER) {
            this.nonExistentVertices++;
            this.numberOfVerticesInCluster[originalCluster]--;
            this.verticesInCluster.get(originalCluster).remove(vertex);
            if (this.numberOfVerticesInCluster[originalCluster] == 0){
                this.usedClusters.remove(originalCluster);
                this.availableClusters.push(originalCluster);
            }
        }
        else if (originalCluster == NON_EXISTENT_CLUSTER && targetCluster != NON_EXISTENT_CLUSTER){
            this.nonExistentVertices--;
            if (this.numberOfVerticesInCluster[targetCluster] == 0){
                this.usedClusters.add(this.availableClusters.pop());
            }
            this.numberOfVerticesInCluster[targetCluster]++;
            this.verticesInCluster.get(targetCluster).add(vertex);
        }
        else if (originalCluster != NON_EXISTENT_CLUSTER && targetCluster != NON_EXISTENT_CLUSTER){
            this.numberOfVerticesInCluster[originalCluster]--;
            this.verticesInCluster.get(originalCluster).remove(vertex);
            if (this.numberOfVerticesInCluster[originalCluster] == 0){
                this.usedClusters.remove(originalCluster);
                this.availableClusters.push(originalCluster);
            }
            if (this.numberOfVerticesInCluster[targetCluster] == 0){
                this.usedClusters.add(this.availableClusters.pop());
            }
            this.numberOfVerticesInCluster[targetCluster]++;
            this.verticesInCluster.get(targetCluster).add(vertex);
        }
        this.updateLastModifiedTime();
    }

    public void moveVertex(int vertex, int targetCluster){
        if (this.nonExistentVertices > 0 || targetCluster == NON_EXISTENT_CLUSTER){
            this.moveVertexPartialSolution(vertex, targetCluster);
            return;
        }
        if (this.clusters[vertex] == targetCluster){
            return;
        }
        int originalCluster = this.clusters[vertex];
        for (int connectedVertex : this.guavaGraph.adjacentNodes(vertex)) {
            int connectionValue = this.getConnectionValue(vertex, connectedVertex);
            int connectedVertexCluster = this.clusters[connectedVertex];
            if (vertex == connectedVertex) {
                this.cohesion[originalCluster] -= connectionValue;
                this.cohesion[targetCluster] += connectionValue;
            }
            else if (originalCluster == connectedVertexCluster) {
                this.cohesion[originalCluster] -= connectionValue;
                this.coupling += connectionValue;
            }
            else if (targetCluster == connectedVertexCluster) {
                this.coupling -= connectionValue;
                this.cohesion[targetCluster] += connectionValue;
            }
        }

        this.maxCohesion = Integer.MIN_VALUE;
        for (int module = 0; module < this.clusters.length; module++) {
            if (this.cohesion[module] > this.maxCohesion) {
                this.maxCohesion = this.cohesion[module];
            }
        }
        this.fcb = this.maxCohesion + this.coupling;
        this.clusters[vertex] = targetCluster;
        this.numberOfVerticesInCluster[originalCluster]--;
        this.verticesInCluster.get(originalCluster).remove(vertex);
        if (this.numberOfVerticesInCluster[originalCluster] == 0){
            this.usedClusters.remove(originalCluster);
            if (!availableClusters.contains(originalCluster)) {
                this.availableClusters.push(originalCluster);
            }
        }
        if (this.numberOfVerticesInCluster[targetCluster] == 0){
            if (!usedClusters.contains(targetCluster)) {
                this.usedClusters.add(targetCluster);
            }
            this.availableClusters.remove(targetCluster);
        }
        this.numberOfVerticesInCluster[targetCluster]++;
        this.verticesInCluster.get(targetCluster).add(vertex);
        this.updateLastModifiedTime();
    }

    private int calculateUpdatesSimulatedMove(int vertex, int targetCluster, int[] cohesionUpdate){
        int originalCluster = this.clusters[vertex];
        int couplingUpdate = 0;
        for (int connectedVertex : this.guavaGraph.adjacentNodes(vertex)) {
            int connectionValue = this.getConnectionValue(vertex, connectedVertex);
            int connectedVertexCluster = this.clusters[connectedVertex];
            if (vertex == connectedVertex) {
                cohesionUpdate[originalCluster] -= connectionValue;
                cohesionUpdate[targetCluster] += connectionValue;
            }
            else if (originalCluster == connectedVertexCluster) {
                cohesionUpdate[originalCluster] -= connectionValue;
                couplingUpdate += connectionValue;
            }
            else if (targetCluster == connectedVertexCluster) {
                couplingUpdate -= connectionValue;
                cohesionUpdate[targetCluster] += connectionValue;
            }
        }

        return couplingUpdate;
    }

    public Set<Integer> getVerticesInCluster(int cluster) {
        return this.verticesInCluster.get(cluster);
    }

    public boolean existsCluster(int cluster){
        return this.usedClusters.contains(cluster);
    }

    public double simulateMoves(HashMap<Integer, Integer> movesList) {
        HashMap<Integer, Integer> restorationList = new HashMap<>();
        for (Integer vertex: movesList.keySet()){
            restorationList.put(vertex, this.clusters[vertex]);
        }

        int originalMaxCohesion = this.maxCohesion;
        int originalCoupling = this.coupling;
        int originalFCB = this.fcb;
        int couplingUpdate = 0;
        int[] originalClusters = Arrays.copyOf(this.clusters, this.clusters.length);
        int[] cohesionUpdate = new int[this.clusters.length];

        for (Integer vertex : movesList.keySet()) {
            int targetCluster = movesList.get(vertex);
            if (this.clusters[vertex] != targetCluster){
                couplingUpdate += this.calculateUpdatesSimulatedMove(vertex, targetCluster, cohesionUpdate);
                this.clusters[vertex] = targetCluster;
            }
        }

        int localMaxCohesion = Integer.MIN_VALUE;
        for (int module = 0; module < this.clusters.length; module++) {
            if (this.cohesion[module]+cohesionUpdate[module] > localMaxCohesion) {
                localMaxCohesion = this.cohesion[module]+cohesionUpdate[module];
            }
        }

        for (Integer vertex : restorationList.keySet()) {
            this.clusters[vertex] = restorationList.get(vertex);
        }

        assert ((originalCoupling == this.coupling) && Double.compare(originalFCB, this.fcb)==0 && (originalMaxCohesion == this.maxCohesion)): "The solution has been changed in a simulated move!";
        assert (Arrays.equals(this.clusters, originalClusters)): "The solution has been changed in a simulated move!";
        return localMaxCohesion + (this.coupling + couplingUpdate);
    }

    public int[] getClusters(){
        return this.clusters;
    }

    public int[][] getDependencyMatrix() {
        return dependencyMatrix;
    }

    public int getClusterOfVertex(int vertex){
        return this.clusters[vertex];
    }
}
