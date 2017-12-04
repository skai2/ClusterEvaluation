# Cluster Evaluation
Simple cluster evaluator program with the following metrics:
  1. Silhouette Coefficient
  2. Dunn Index
  
Usage:
  1. Open CSV database which must have the cluster info in the last column with the naming scheme "cluster[number]".
  2. If necessary, change the separators to the ones used in the loaded database.
  3. Select desired metric and pres "evaluate".
  
Current Limitations:
  1. Database must be composed of numeric attributes. Other attribute types are ignored.
