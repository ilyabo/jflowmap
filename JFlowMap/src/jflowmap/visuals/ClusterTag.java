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

package jflowmap.visuals;

import java.awt.Paint;

/**
 * @author Ilya Boyandin
 */
public class ClusterTag {

    private final int clusterId;
    private final Paint clusterPaint;
    private final boolean visible;
    
    public ClusterTag(int clusterId, Paint clusterPaint) {
        this(clusterId, clusterPaint, true);
    }

    public ClusterTag(int clusterId, Paint clusterPaint, boolean visible) {
        this.clusterId = clusterId;
        this.clusterPaint = clusterPaint;
        this.visible = visible;
    }

    public int getClusterId() {
        return clusterId;
    }

    public Paint getClusterPaint() {
        return clusterPaint;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public ClusterTag withVisible(boolean visible) {
        if (visible == this.visible) {
            return this;
        } else {
            return new ClusterTag(clusterId, clusterPaint, visible);
        }
    }
    
    public static ClusterTag createFor(int clusterId, Paint clusterColor) {
        return new ClusterTag(clusterId, clusterColor);
        
    }

}
