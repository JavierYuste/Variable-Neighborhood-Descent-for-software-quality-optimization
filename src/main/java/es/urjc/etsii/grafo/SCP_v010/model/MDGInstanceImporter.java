package es.urjc.etsii.grafo.SCP_v010.model;

import es.urjc.etsii.grafo.io.InstanceImporter;

import java.io.BufferedReader;
import java.io.IOException;

public class MDGInstanceImporter extends InstanceImporter<MDGInstance> {

    @Override
    public MDGInstance importInstance(BufferedReader reader, String filename) throws IOException {
        return new MDGInstance(filename);
    }
}
