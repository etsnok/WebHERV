# WebHERV
WebHERV front-end with the key-value store DRUMS.

WebHERV (http://calypso.informatik.uni-halle.de/WebHERV/) is a web GUI that enables the user to access HERV (Human endogenous retrovirus) genome positions stored in the underlying highly optimized key-value store DRUMS (https://github.com/mgledi/DRUMS).

The following subsections describe how to set up the WebHERV frontend as well as the backend. 

#### 1. Determine HERV like sequences

To determine the HERV like sequences in the human genome, we used the standalone version of BLAST (Basic Local Alignment Search Tool). 

The latest version of BLAST can be downloaded from ftp://ftp.ncbi.nlm.nih.gov/blast/executables/blast+/LATEST/.

The current human genome can be downloaded from ftp://ftp.ncbi.nih.gov/genomes/H_sapiens/.

The program `blastn` with the output option `-outfmt 6` was used to search for more HERVs like sequences in the human genome using known HERV sequences.
```
  ./blastn -query hervSequences.fa -db ./human_genome_dir -out blast_output.out -outfmt 6 -evalue 1e-10
```

#### 2. Clone WebHERV project

```
 git clone https://github.com/etsnok/WebHERV.git
 
 cd ./WebHERV/
 
  mvn package
``

#### 2. Fill DRUMS database

TODO the user must checkout DRUMS or BioDRUMS first? How can he do it? Where to store it? Must he compile it? Is there a jar?

To manage the billions of BLAST hits we use the highly optimized key-value store DRUMS. DRUMS is set up and filled as follows. First, set the location for the DRUMS store in the `WebContent/WEB-INF/drums.properties` directory.
```
  DATABASE_DIRECTORY=/path/to/drums/db/`
```

Second, use the java program `org.kkruse.webherv.upload.WebHERVWriter` to write the BLAST results to the DRUMS store in consideration of your `drums.properties` file.
```
  TODO is it needed to compile the java code first?
  TODO full command
```

Third, register the database directory in the `WebContent/WEB-INF/portal-config.properties`. The WebHERV frontend is designed to access multiple DRUMS stores located in one folder, so in `portal-config.properties` the parameter `drums.directory.path` must be set to the parent directory of your DRUMS stores. Configure the specific stores with the `drums.databases.hervs.hg19.{id,dir,genome}` parameters.

```
  drums.directory.path="/Users/Konstantin/Documents/HERV-PS-AssocHomepage/drumsDatabases"

  # hg19 drums database
  drums.databases.hervs.hg19.id=hervs_hg19
  drums.databases.hervs.hg19.dir=hervs_hg19
  drums.databases.hervs.hg19.genome=hg19

  # hg18 drums database
  drums.databases.hervs.hg18.id=hervs_hg18
  drums.databases.hervs.hg18.dir=hervs_hg18
  drums.databases.hervs.hg18.genome=hg18
```

Forth, add the path to the DRUMS directory to `WebContent/WEB-INF/web.xml`. 
```
 <context-param>
		<param-name>drumsDatabasesDir</param-name>
		<param-value>/path/to/drums/db</param-value>
  </context-param>
```

#### 3. Fill Probe sets

Currently, the WebHERV accesses a simple SQLite database holding the genomic postions for the `Affymetrix Human Exon 1.0 ST arrays` hg18 and hg19 stored in the database file. 

TODO provide CREATE TABLE statements, 
TODO provide example data or real data
TODO provide script for filling the tables

#### 4. Create .WAR and deploy

Finally, to be able to run the web-server you must create a `.war` file containing the frontend with all properties and deploy it in the webapps directory of your server e.g. tomcat.  

```
  TODO command to create the war file. 
```

