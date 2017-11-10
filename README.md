# edn to csv converter
This converter converts data from edn file to multiple csv files which then can be imported into neo4j graph database.

## How to run the converter 
To run the converter you should clone the repository from intelliJ IDEA and open the Test.java.
```java
public class Test {
    public static void main(String[] args) throws IOException {
        //Make instance of the converter
        Converter Converter = new Converter();
        //To convert edn file call the convert method which takes two arguments two arguments: sourceFile and destination for the output files.
        Converter.convert("sourceFile" ,"destinationDirectory");     
    }
}
```
Change "sourceFile" to the edn file you are trying to convert from and "destinationDirectory" to **import** folder in your NEO4J_HOME directory. Then run the main method. 



## How to import csv files into neo4j
From top level directory which is referred  as NEO4J_HOME run this command in the terminal

```sh

 bin/neo4j-import --into data/database/graph.db \
                  --nodes import/RunConfigurations_Entity.csv \
                  --nodes import/Individual_Entity.csv \
                  --nodes import/Gene_Entity.csv \
                  --nodes import/TotalError_Entity.csv \
                  --nodes import/SingleError_Entity.csv \
                  --relationships import/RunConfigurations_Individual_Edge.csv \
                  --relationships import/Individual_Gene_Edge.csv \
                  --relationships import/Individual_TotalError_Edge.csv \
                  --relationships import/ParentOf_Edge.csv \
                  --relationships import/TotalError_SingleError_Edge.csv \
                  --relationships import/Inherited_Gene_Edge.csv

```
Where instead of <b>test.db</b> you can choose any other name that you prefer.

In order to run your database you need to set it as the default one. For that go to the <b>conf</b> directory and open the <b>neo4j.conf</b> file.
Find the line which says ``` dbms.active_database=graph.db ``` and uncomment it.
    
**Note:** *If you chose another name instead of graph.db then you need to change it in the configuration file as well*
 
## To run the database
From top level directory which is referred  as NEO4J_HOME run this command in the terminal

```sh
bin/neo4j console
```

