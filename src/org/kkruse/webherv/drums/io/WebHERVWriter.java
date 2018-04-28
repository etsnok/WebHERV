package org.kkruse.webherv.drums.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

import com.unister.semweb.biodrums.herv.HERV;
import com.unister.semweb.biodrums.herv.HitFileParser;
import com.unister.semweb.drums.DRUMSParameterSet;
import com.unister.semweb.drums.api.DRUMS;
import com.unister.semweb.drums.api.DRUMS.AccessMode;
import com.unister.semweb.drums.api.DRUMSException;
import com.unister.semweb.drums.api.DRUMSInstantiator;
import com.unister.semweb.drums.bucket.hashfunction.RangeHashFunction;

public class WebHERVWriter {

	public static void createNewTable( DRUMSParameterSet<HERV> globalParameters, boolean deleteExistingTable ) throws IOException, InterruptedException {

		//        DRUMSParameterSet<HERV> globalParameters = new DRUMSParameterSet<HERV>( propsFileName , new HERV());
		System.err.println( "Creating new Table..." );
		
		if( deleteExistingTable ){
			/** to repeat the test we have to delete the table first */ 
			FileUtils.deleteQuietly( new File( globalParameters.DATABASE_DIRECTORY ) );
			System.err.println( "Deleted existing table:" + new File( globalParameters.DATABASE_DIRECTORY ) );
		}

		/**
		 * {@link DRUMS} needs a consistent hash function. The {@link HERV} class provides a method to generate a
		 * {@link RangeHashFunction} for Arabidopsis thaliana.
		 */
		RangeHashFunction hashFunction = HERV.createHashFunction();
		System.err.println( "Created new hash function." );

		/**
		 * The {@link DRUMSInstantiator}-class provides several factory methods to instantiate a DRUMS-table. The table
		 * does not exists. It must be created before you can insert data.
		 */
		DRUMS<HERV> drums = DRUMSInstantiator.createTable(hashFunction, globalParameters);
		System.err.println( "Finished creating new table!" );
		drums.close();
	}


	public static void writeBlastHitFile( DRUMSParameterSet<HERV> globalParameters, String blastFile ) throws IOException, InterruptedException{

		DRUMS<HERV> drums = DRUMSInstantiator.openTable( AccessMode.READ_WRITE, globalParameters );

		try {
			HitFileParser parser = new HitFileParser( blastFile, 1024 * 64);
			/**
			 * Add all {@link HERV}s to your {@link DRUMS}-instance.
			 */
			HERV herv;
			while ( ( herv = parser.readNext() ) != null ) {
				drums.insertOrMerge( herv );
			}

		} catch ( DRUMSException e ) {
			System.err.println( "Exception while writing HitFileParser file:'" + blastFile + "', ex:" + e.getLocalizedMessage() );
		} finally{
			if( drums != null ){
				drums.close();
			}
		}

	}

	public static DRUMSParameterSet<HERV> readDRUMSParameterSet( String propertiesFile ) throws IOException{
		return new DRUMSParameterSet<HERV>( propertiesFile , new HERV() );
	}
	
	
	/**
	 * This method starts the WriteTutorial. The location of your DRUMS is defined in the property file in the
	 * "./src/main/resources/HERVExample/". Please see the parameter <b>DATABASE_DIRECTORY</b>.
	 * 
	 * @param args
	 *            no arguments are needed
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws DRUMSException
	 */
	public static void main(String[] args) throws IOException, DRUMSException, InterruptedException {

		CmdArgs cmdArgs = parseArgs( args );

		Path propsPath = Paths.get( cmdArgs.propsFileName );
		if( ! Files.isRegularFile( propsPath ) ){
			throw new FileNotFoundException( "File not found:" + propsPath.toString() );
		}
		
		System.err.println( "Reading:" + propsPath );
		
		DRUMSParameterSet<HERV> globalParameters = readDRUMSParameterSet( cmdArgs.propsFileName );
		
		if( cmdArgs.program == 0 ){
			createNewTable( globalParameters, true );
		} else if( cmdArgs.program == 1 ){
			writeBlastHitFile( globalParameters, cmdArgs.blastHitFileName );
		} else {
			System.err.println( "Wrong parameter program: " + cmdArgs.program );
		}
		
	}

	private static class CmdArgs{
		public String propsFileName;
		public int program;
		public String blastHitFileName;
	}


	private static CmdArgs parseArgs( String[] args ){
		if(  args == null || args.length <= 1 ){
			System.err.println( "Missing parameter for drums properties file!" );
			printHelp( 1 );
		}

		CmdArgs cmdArgs = new CmdArgs();
		cmdArgs.propsFileName = args[0];

		System.err.println( "Conf file:" + cmdArgs.propsFileName );
		
		cmdArgs.program = Integer.parseInt( args[1] );

		if( cmdArgs.program < 0 || cmdArgs.program > 1 ){
			System.err.println( "Wrong value:'"+ cmdArgs.program +"' for program!" );
			printHelp( 1 );
		}


		switch ( cmdArgs.program ) {
		case 0: 
			System.err.println( "Program:" + cmdArgs.program );
			System.err.println( "Create new table." );
			break;
		case 1: 
			cmdArgs.blastHitFileName = args[2];
			if( cmdArgs.blastHitFileName == null || cmdArgs.blastHitFileName.isEmpty() ){
				System.err.println( "Wrong missing value for parameter blast hit file!" );
				printHelp( 1 );	
			}
			System.err.println( "Program:" + cmdArgs.program );
			System.err.println( "Blast file:" + cmdArgs.blastHitFileName );
			break;
		default:
			System.err.println( "Wrong parameter program:" + cmdArgs.program );
			printHelp( 1 );
			break;
		}

		return cmdArgs;
	}

	private static void printHelp( int status ){
		System.err.println( "WebHERVWriter commandline:\n"
				+ "[1] STRING  drums properies file path." 
				+ "[2] INTEGER program: 0: create table; 1:write blast hit file; " 
				+ "[3] STRING  blast file path." 
				);
		System.exit( status );
	}

}
