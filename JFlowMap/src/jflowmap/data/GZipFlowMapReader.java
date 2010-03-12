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

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import prefuse.data.Graph;
import prefuse.data.io.AbstractGraphReader;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphReader;

/**
 * TODO: finish GZipFlowMapReader
 * @author Ilya Boyandin
 */
public class GZipFlowMapReader extends AbstractGraphReader {

    private final GraphReader reader;

    public GZipFlowMapReader(GraphReader reader) {
        this.reader = reader;
    }

    @Override
    public Graph readGraph(InputStream is) throws DataIOException {
        try {
            return reader.readGraph(new GZIPInputStream(is));
        } catch (IOException e) {
            throw new DataIOException(e);
        }
    }

}
