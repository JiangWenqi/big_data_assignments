# How to run this project
## Run it on local

1. `git clone` this repository: 
   `git clone https://github.com/JiangWenqi/big_data_assignments.git`
2. get into `flight-arrival-delay-predictor` folder: 
   `cd flight-arrival-delay-predictor` 
3. run maven package: 
   `mvn package`
4. run jar: 
   `spark-submit --master 'local[4]' target/flight-arrival-delay-predictor-1.0-SNAPSHOT-jar-with-dependencies.jar`