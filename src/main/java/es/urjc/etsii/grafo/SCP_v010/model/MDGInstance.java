package es.urjc.etsii.grafo.SCP_v010.model;

import es.urjc.etsii.grafo.io.Instance;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public class MDGInstance extends Instance {

    private int[][] dependencyMatrix;
    private HashMap<String, Integer> vertexMapNames = new HashMap<>();
    public HashMap<Integer, String> vertexMapPositions = new HashMap<>();
    private String[] nodesNames;
    public int[] clustersInstance;
    public int numberOfInitialEdges=0;
    public boolean[] erasedVertex;
    private HashMap<String, Integer> initialClustersMapNames = new HashMap<>();
    private HashMap<Integer, String> initialClustersMapPositions = new HashMap<>();
    public List<List<Integer>> modulesOnCluster;
    public int[] modulesOnClusterSum;
    private int[] intraclusterEdges;
    private int[] interclusterEdges;
    public double[] modularizationFactor;
    private boolean[] existentClusters;
    public Deque<Integer> availableClusters;
    public List<Integer> usedClusters;
    public String instanceName;
    private static final Logger log = Logger.getLogger(MDGSolution.class.toString());

    public MDGInstance(String name){
        super(name);
        this.instanceName = name;
        try {
            String path = new File("instances/" + name).getAbsolutePath();
            this.parseTextFileWithWeights(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MDGInstance(String name, boolean absoluteName){
        super(name);
        this.instanceName = name;
        try {
            if (absoluteName){
                this.parseTextFileWithWeights(name);
            }
            else{
                String path = new File("instances/" + name).getAbsolutePath();
                this.parseTextFileWithWeights(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int[][] getDependencyMatrix(){
        return this.dependencyMatrix;
    }

    /**
     * Build a MDG from a txt file containing the data of an instance
     * @param txtFileName Absolute path of the txt file containing the instance
     * @return true if successfully parsed, flase otherwise
     * @throws Exception
     */
    private boolean parseTextFileWithWeights(String txtFileName) throws Exception {
        File txtFile = new File(txtFileName);
        String[] splitted;
        int currVertex = 0;
        int currCluster = 0;
        int i;
        Scanner sc = new Scanner(txtFile);
        while (sc.hasNextLine()) {
            splitted = sc.nextLine().split("\\s+");
            if (splitted.length < 2){
                break;
            }
            if (!this.vertexMapNames.containsKey(splitted[0])){
                this.vertexMapNames.put(splitted[0], currVertex);
                this.vertexMapPositions.put(currVertex, splitted[0]);
                currVertex++;
                i = splitted[0].lastIndexOf(".");
                if (i > 0) {
                    String[] clusterAndClass = {splitted[0].substring(0, i), splitted[0].substring(i + 1)};
                    if (!this.initialClustersMapNames.containsKey(clusterAndClass[0])) {
                        this.initialClustersMapNames.put(clusterAndClass[0], currCluster);
                        this.initialClustersMapPositions.put(currCluster, clusterAndClass[0]);
                        currCluster++;
                    }
                }
                else {
                    this.initialClustersMapNames.put(splitted[0], currCluster);
                    this.initialClustersMapPositions.put(currCluster, splitted[0]);
                    currCluster++;
                }
            }
            if (!this.vertexMapNames.containsKey(splitted[1])){
                this.vertexMapNames.put(splitted[1], currVertex);
                this.vertexMapPositions.put(currVertex, splitted[1]);
                currVertex++;
                i = splitted[1].lastIndexOf(".");
                if (i > 0) {
                    String[] clusterAndClass = {splitted[1].substring(0, i), splitted[1].substring(i + 1)};
                    if (!this.initialClustersMapNames.containsKey(clusterAndClass[0])) {
                        this.initialClustersMapNames.put(clusterAndClass[0], currCluster);
                        this.initialClustersMapPositions.put(currCluster, clusterAndClass[0]);
                        currCluster++;
                    }
                }
                else {
                    this.initialClustersMapNames.put(splitted[1], currCluster);
                    this.initialClustersMapPositions.put(currCluster, splitted[1]);
                    currCluster++;
                }
            }
        }

        this.nodesNames = new String[this.vertexMapNames.size()];
        for ( String key : this.vertexMapNames.keySet() ) {
            this.nodesNames[this.vertexMapNames.get(key)] = key;
        }

        int lastIndex;
        this.usedClusters = new ArrayList<>();
        this.availableClusters = new ArrayDeque<>();
        this.dependencyMatrix = new int[this.vertexMapNames.size()][this.vertexMapNames.size()];
        this.clustersInstance = new int[this.vertexMapNames.size()];
        this.modulesOnClusterSum = new int[this.vertexMapNames.size()];
        this.interclusterEdges = new int[this.clustersInstance.length];
        this.intraclusterEdges = new int[this.clustersInstance.length];
        this.modularizationFactor = new double[this.clustersInstance.length];
        this.erasedVertex = new boolean[this.vertexMapNames.size()];
        this.existentClusters = new boolean[this.clustersInstance.length];
        this.modulesOnCluster = new ArrayList<>();
        for (i = 0; i < dependencyMatrix.length; i++){
            lastIndex = this.vertexMapPositions.get(i).lastIndexOf(".");
            this.erasedVertex[i] = false;
            this.interclusterEdges[i] = 0;
            this.intraclusterEdges[i] = 0;
            this.modularizationFactor[i] = 0.0;
            this.existentClusters[i] = false;
            if (lastIndex > 0) {
                this.clustersInstance[i] = this.initialClustersMapNames.get(this.vertexMapPositions.get(i).substring(0, lastIndex));
            }
            else{
                this.clustersInstance[i] = this.initialClustersMapNames.get(this.vertexMapPositions.get(i));
            }
            for (int j = 0; j < dependencyMatrix[0].length; j++){
                this.dependencyMatrix[i][j] = 0;
            }
            this.modulesOnClusterSum[i] = 0;
        }

        sc = new Scanner(txtFile);
        while (sc.hasNextLine()) {
            splitted = sc.nextLine().split("\\s+");
            if (splitted.length == 2){
                this.numberOfInitialEdges++;
                this.addEdge(splitted[0], splitted[1]);
            }
            else if (splitted.length == 3){
                this.numberOfInitialEdges++;
                this.addEdgeWithWeight(splitted[0], splitted[1], splitted[2]);
            }
        }
        sc.close();

        return true;
    }

    public boolean addEdge(String node_src, String node_dst){
        if (this.vertexMapNames.containsKey(node_src) && this.vertexMapNames.containsKey(node_dst)){
            int index_dst = this.vertexMapNames.get(node_dst);
            int index_src = this.vertexMapNames.get(node_src);
            this.dependencyMatrix[index_src][index_dst]++;
            return true;
        }
        else{
            log.severe("Could not find one of the following nodes: " + node_src + "; " + node_dst);
            return false;
        }
    }

    public boolean addEdgeWithWeight(String node_src, String node_dst, String weight){
        if (this.vertexMapNames.containsKey(node_src) && this.vertexMapNames.containsKey(node_dst)){
            int index_dst = this.vertexMapNames.get(node_dst);
            int index_src = this.vertexMapNames.get(node_src);
            this.dependencyMatrix[index_src][index_dst] += Integer.parseInt(weight);
            return true;
        }
        else{
            log.severe("Could not find one of the following nodes: " + node_src + ", " + node_dst);
            return false;
        }
    }
}
