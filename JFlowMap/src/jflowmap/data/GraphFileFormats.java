/*
 * This file is part of JFlowMap.
 *
 * Copyright 2009 Ilya Boyandin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jflowmap.data;

import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;
import prefuse.data.io.GraphReader;
import at.fhj.utils.misc.FileUtils;

/**
 * @author Ilya Boyandin
 */
public enum GraphFileFormats {
    GRAPH_ML("graphml") {
        @Override
        public GraphReader createReader() {
            return new GraphMLReader();
        }
    },
    XML("xml") {
        @Override
        public GraphReader createReader() {
            return GRAPH_ML.createReader();
        }
    },
    CSV("csv") {
        @Override
        public GraphReader createReader() {
            return new CsvFlowMapReader();
        }
    }
    ;
    private String extension;

    private GraphFileFormats(String extension) {
        this.extension = extension;
    }
    
    public abstract GraphReader createReader();
    
    public static GraphReader createReaderFor(String filename) throws DataIOException {
        String ext = FileUtils.getExtension(filename).toLowerCase();
        for (GraphFileFormats fmt : values()) {
            if (fmt.extension.equals(ext)) {
                return fmt.createReader();
            }
        }
        throw new DataIOException("Unsupported graph file format extension: " + ext);
    }
}