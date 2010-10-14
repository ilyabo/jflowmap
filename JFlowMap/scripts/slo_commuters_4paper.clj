(ns slo-commuters-4paper)

(import
  '(javax.swing JFrame)
  '(jflowmap DatasetSpec FlowMapToImageRenderer)
  '(jflowmap.geo MapProjections))


(deftype FlowMapModelInitializer []
  jflowmap.FlowMapToImageRenderer$FlowMapModelInitializer
  (setupFlowMapModel [this model]
    (doto model
      (.setVisualLegendScale 3.0)
      (.setMaxEdgeWidth 30))))


(defn renderCommuters [^String name]
  (let [renderer (FlowMapToImageRenderer.
              (str name ".png")
              (DatasetSpec.
                   (str "data/commuters/slo/4paper/" name ".xml") "value" "lon" "lat" "name" 
                   nil "data/shapefiles/slo/SVN_adm2.shp.gz" (MapProjections/MERCATOR))
                   (into-array String [name]))
        ;model-init (proxy [jflowmap.FlowMapToImageRenderer$FlowMapModelInitializer] []
        ;  (setupFlowMapModel [model]
        ;    (doto model
        ;      (.setVisualLegendScale 3.5)
        ;      (.setMaxEdgeWidth 30))))
        ]
    (doto renderer
      ;(.setFlowMapModelInitializer model-init)
      (.setFlowMapModelInitializer (FlowMapModelInitializer.))
      (.makeFullscreen)
      (.setPaddingX 0)
      (.setPaddingY 0)
      (.setVisible true)
      (.start))))


(renderCommuters "CommutersBetweenAllMunicaipalities_Over100Commuters")
(renderCommuters "CommutersBetweenAllMunicaipalities_Over50Commuters")
(renderCommuters "CommutersBetweenFunctionalRegionsAfterFirstAggregation")
(renderCommuters "CommutersTo10CentresOfFunctionalRegionsAfterSeconAggregation")
(renderCommuters "CommutersTo31CentresOfFunctionalRegionsAfterFirstAggregation")

