# WebHERV
WebHERV front-end with DRUMS database backend.

WebHERV (http://calypso.informatik.uni-halle.de/WebHERV/) is a web GUI that enables the user to access a HERV (Human endogenous retrovirus) genome positions stores in a DRUMS database (https://github.com/mgledi/DRUMS).

So set up a version with our own data you need to do the following steps.

#### 1. Blast/Genome positions

For WebHERV we used the standalone version BLAST (Basic Local Alignment Search Tool) that can be downloaded from: ftp://ftp.ncbi.nlm.nih.gov/blast/executables/blast+/LATEST/

The program "blastn" with the output option -outfmt 6 was used to search for more HERVs like sequences in the human genome using known HERV sequences.

#### 2. DRUMS database

To set up a DRUMs database you need to customize the drums.properties as provided in the WebContent/WEB-INF directory and the the parameter `DATABASE_DIRECTORY=/path/to/drums/db/` to a path where you want to store your database.

The program `org.kkruse.webherv.upload.WebHERVWriter` is able to write the data that was generated using BLAST into your own DRUMS DB using the custom `drums.properties` file.

This database directory must also be registered in the `portal-config.properties`, the WebHERV is designed to access multiple DRUMS DBs that are stored in one folder, so in `portal-config.properties` the parameter `drums.directory.path` needs to be set to the parent directory of your drums databases. With the parameter 'drums.databases.hervs.hg19.id' ('dir', 'genome') you need to register the specific database.

To enable the WebHERV to access the DRUMS directory, this directory must also be registered in the 'web.xml' file in 'WebContent/WEB-INF' under the parameter name 'drumsDatabasesDir'. Otherwise the programm wouldn't have the rights to read from this directory.

#### 3. Probe sets

Currently the WebHERV accesses a simple SQLite databse holding the genomic postions for the 'Affymetrix Human Exon 1.0 ST arrays' hg18 and hg19 stored in the database file. 

#### 4. Create .WAR and deploy

To run the WebHERV fou need to create .war` file containing the WebHERV and all it's proberties and deploy it in the webapps directory of your server e.g. tomcat.  

