# WebHERV
WebHERV front-end with the key-value store DRUMS.

WebHERV (http://calypso.informatik.uni-halle.de/WebHERV/) is a web GUI that enables the user to access HERV (Human endogenous retrovirus) or any other genome positions stored in the underlying highly optimized key-value store DRUMS (https://github.com/mgledi/DRUMS).

The following subsections describe how to set up the WebHERV frontend as well as the backend with HERVs but it can by customized to any other type of genomic positions.

### 1. Determine HERV like sequences

To determine the HERV like sequences in the human genome, we used the standalone version of BLAST (Basic Local Alignment Search Tool). 

The latest version of BLAST can be downloaded from ftp://ftp.ncbi.nlm.nih.gov/blast/executables/blast+/LATEST/.

The current human genome can be downloaded from ftp://ftp.ncbi.nih.gov/genomes/H_sapiens/.

The program `blastn` with the output option `-outfmt 6` was used to search for more HERVs like sequences in the human genome using known HERV sequences.
```
  ./blastn -query hervSequences.fa -db ./human_genome_dir -out blast_output.out -outfmt 6 -evalue 1e-10
```

#### 1.1 Other genomic positions

Similar to the HERV like sequences any other type of sequences or refernece genomes ca be used
to determine genomic positional information.  

### 2. WebHERV sources

Download the WebHERV sources via from GitHub:

```
 git clone https://github.com/etsnok/WebHERV.git
```

After downloading the sources change in the WebHERV directory.
```
cd ./WebHERV/
```

This directory contains all the necceary files to set up and compile the WebHERV webapp.

### 3. Setup DRUMS store and WebHERV
The directory `WebContent/WEB-INF/` contains all configuration files that are need to set up your own WebHERV. 

#### 3.1 DRUMS setup
To manage the billions of BLAST hits we use the highly optimized key-value store DRUMS. 
 
To set up a DRUMS DB, set the location for the DRUMS store in the 
`WebContent/WEB-INF/drums.properties` directory.

```
  DATABASE_DIRECTORY=/path/to/drums/db/your_db/`
```

`your_db` is the directory where the DRUMS DB will be stored (or is stored if it's already exists).

**NOTE:**
The WebHERV is designed to hold multiple DRUMS DBs.
Therefore you can create additional DRUMS DBs next to `your_db`
 e.g.: `/path/to/drums/db/your_db_2/`
 
#### 3.2 WebHERV setup

To register your DRUMS DB for the WebHERV front end you need to edit the following to configuration files:

##### web.xml

First, to enable accessing the file system directory containing the DRUMS
 directories (`/path/to/drums/db/`) 
in `WebContent/WEB-INF/web.xml` you need to set the following parameter:

```
<context-param>
	<param-name>drumsDatabasesDir</param-name>
	<param-value>/path/to/drums/db</param-value>
</context-param>
```

Otherwise the WebHERV would have not the rights to read the file system.

**NOTE:** the path is the parent directory of `your_db` so that other directories 
like `your_db_2` are also accessible.


##### drums.properties
Second, to register your DRUMS DBs in the WebHERV front end you need to edit the 
`WebContent/WEB-INF/drums.properties` file.
There you need to set the `drums.directory.path` to the directory containing your DRUMS DBs.
The individual databases need to be registered by `drums.databases.hervs.your_db`.


```
  drums.directory.path="/path/to/drums/db"

  # hg19 drums database
  drums.databases.hervs.your_db.id=your_db
  drums.databases.hervs.your_db.dir=your_db
  drums.databases.hervs.your_db.genome=hg19

  # hg18 drums database
  drums.databases.hervs.your_db_2.id=your_db_2
  drums.databases.hervs.your_db_2.dir=your_db_2
  drums.databases.hervs.your_db_2.genome=hg18
```

**NOTE:** the parameter `genome` must be set to either `hg18` or `hg19`.

#### 3.3 WebHERV packaging

After setting up the WebHERV and DRUMS configurations you can package your WebHERV
by running:
```
mvn package
```

in the `./WebHERV/` directory.

This will produce the files `WebHERV-X.X.X.war` and a `WebHERV-X.X.X.jar` in the `./target/` directory.

### 4. Fill DRUMS database

After packaging the WebHERV you can use the `WebHERV-X.X.X.jar` to create and fill your DRUMS DB with data. 

Use the java program `org.kkruse.webherv.upload.WebHERVWriter` to create the DRUMS store in consideration of your `drums.properties` file.

Creating the DRUMS store:
```
java -jar ./target/WebHERV-0.0.1.jar ./WebContent/WEB-INF/drums.properties 0
```

After this you can fill the created DRUMS store with your BLAST output data (`blast_output.out`) by running:
```
java -jar ./target/WebHERV-0.0.1.jar ./WebContent/WEB-INF/drums.properties 1 path/to/your/blast_output.out
```

### 5. Fill Probe sets

Currently, the WebHERV accesses a simple SQLite database holding the genomic postions 
for the `Affymetrix Human Exon 1.0 ST arrays` hg18 and hg19 stored in the database file. 

### 6. Run WebHERV Server

Finally, you are able to run the web-server by deploying it to server (e.g. tomcat) or
starting a local Jetty instance.

#### 6.1 Server

You can take the `.war` file containing the front end with all properties 
and deploy it in the webapps directory of your server e.g. tomcat.  

The front end will be accessible by something like: `http://your_server_url/WebHERV/`

#### 6.1 Local Jetty instance

Or you can start a local Jetty server instance from `WebHERV/ via:
```
java -jar target/dependency/jetty-runner.jar target/*.war
```

and in your browser enter the following URL: `http://localhost:8080/` 
