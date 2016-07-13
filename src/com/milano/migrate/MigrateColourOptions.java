package com.milano.migrate;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import com.milano.migrate.util.Constants;
import com.milano.migrate.util.Utils;

//TODO - write in the log file
public class MigrateColourOptions {
	
		private final static String COLOUR_OPTIONS_MIGRATION_LOG_FILE = "ColourOptionsMigration"; 
		private static Path file = null;
		private static Connection msSqlConnection;
		private static Connection mySqlConnection;
		private static int COLOUR_OPTION_ID = 99999;
		
		public static void migrateColourOptions() throws SQLException, IOException, ClassNotFoundException{
		try {
			 msSqlConnection = Main.getMilanoUser().getMsSqlConnection();
			 mySqlConnection = Main.getMilanoUser().getMySqlConnection();

			try {
				 file = Utils.getFile(COLOUR_OPTIONS_MIGRATION_LOG_FILE, true);
				 Utils.appendToFile(file, "Colour Options Migrate Start");
				 mySqlConnection.setAutoCommit(false); // make 
				 Utils.appendToFile(file, "Delete Colour Options");
				 String query = "Delete from blc_product_option_value where PRODUCT_OPTION_ID=?";
	     	     PreparedStatement preparedStmt = mySqlConnection.prepareStatement(query);
	   	     	 preparedStmt.setInt(1, COLOUR_OPTION_ID); //PRODUCT_OPTION_ID
				 preparedStmt.execute();
				 preparedStmt.close();
				 query = "Delete from  blc_product_option where PRODUCT_OPTION_ID=?";
	     	     preparedStmt = mySqlConnection.prepareStatement(query);
	   	     	 preparedStmt.setInt(1, COLOUR_OPTION_ID); //PRODUCT_OPTION_ID
				 preparedStmt.execute();
				 preparedStmt.close();
				 query = "Delete from milano_color";
	     	     preparedStmt = mySqlConnection.prepareStatement(query);
				 preparedStmt.execute();
				 preparedStmt.close();
				 Utils.appendToFile(file, "Getting List of Colours");
				 query = "SELECT distinct tabcod, description FROM dbo.color";
	     	     preparedStmt = msSqlConnection.prepareStatement(query);
				 ResultSet mResultSet = preparedStmt.executeQuery();

			     String insertQ =	"INSERT INTO blc_product_option "+
	    		 			"(PRODUCT_OPTION_ID, ATTRIBUTE_NAME, DISPLAY_ORDER, LABEL, REQUIRED, OPTION_TYPE, USE_IN_SKU_GENERATION, VALIDATION_STRATEGY_TYPE) "+
	    		 			" VALUES (?,?,?,?,?,?,?,?)";
			     preparedStmt = mySqlConnection.prepareStatement(insertQ);
	   	     	 preparedStmt.setInt(1, COLOUR_OPTION_ID); //PRODUCT_OPTION_ID
	   	     	 preparedStmt.setString (2, "COLOR"); //ATTRIBUTE_NAME
	   	     	 preparedStmt.setInt (3, 1); //DISPLAY_ORDER
	   	     	 preparedStmt.setString (4, "Color"); //LABEL
	   	     	 preparedStmt.setInt (5, 1); //REQUIRED
	   	     	 preparedStmt.setString (6, "COLOR"); //OPTION_TYPE
	   	     	 preparedStmt.setInt (7, 0); //USE_IN_SKU_GENERATION
	   	     	 preparedStmt.setString (8, "ADD_ITEM"); //VALIDATION_STRATEGY_TYPE
	   	     	 preparedStmt.execute();
	   	     	 preparedStmt.close();
	   	     	 int i = 1;
			     while (mResultSet.next()) {
			    	 int prodOptionValueId = Utils.getNextSequenceNumber("ProductOptionValueImpl", true);
		   	     	 insertQ = "INSERT INTO blc_product_option_value "+
		   	    		 	"(PRODUCT_OPTION_VALUE_ID, ATTRIBUTE_VALUE,"+ //DISPLAY_ORDER,
		   	     			 " PRODUCT_OPTION_ID) "+
		   	    		 	" VALUES (?,?,?) ";
		   	     	 preparedStmt = mySqlConnection.prepareStatement(insertQ);
		   	     	 preparedStmt.setInt(1, prodOptionValueId);
		   	     	 preparedStmt.setString(2, mResultSet.getString("description"));
		   	     	 //preparedStmt.setInt(3, 1);
		   	     	 preparedStmt.setInt(3, COLOUR_OPTION_ID);
		   	     	 preparedStmt.execute();
		   	     	 preparedStmt.close();
		   	     	 insertQ = "INSERT INTO milano_color "+
			   	    		 	"(milano_color_id, milano_tabcod, product_option_value_id) "+
			   	    		 	" VALUES (?,?,?) ";
			   	     preparedStmt = mySqlConnection.prepareStatement(insertQ);
		   	     	 preparedStmt.setInt(1, i++);
			   	     preparedStmt.setString(2, mResultSet.getString("tabcod"));
		   	     	 preparedStmt.setInt(3, prodOptionValueId);
			   	     preparedStmt.execute();
			   	     preparedStmt.close();
					 Utils.appendToFile(file, "Color Option with tabcod="+mResultSet.getString("tabcod")+" "+mResultSet.getString("description")+" "+prodOptionValueId+" DONE");
			     }
				 Utils.appendToFile(file, "All Colour Options Are migrated");
				 mySqlConnection.commit(); 
			} catch (SQLException e) {
						// Logger.getLogger(MigrateCategory.class.getName()).log(Level.SEVERE, null, e);
				         if (mySqlConnection != null)
				            { try  {
				            		mySqlConnection.rollback(); 
				            	} catch (SQLException i){}
				            }
				            e.printStackTrace();
				            throw new Exception(e.getMessage());
			   } finally {
			         /*   if (mySqlConnection != null){
				                try {mySqlConnection.close();
				                } catch (SQLException e) { e.printStackTrace();}
				            }*/
				      //  }
					 //mResultSet.next();
			  }
			 Utils.appendToFile(file, "Done Migrating Color Options");
			} catch (Exception e){
				if (file!=null){
				 Utils.appendToFile(file, "Exception during Colour Options Migration: "+e.getMessage());
				} else {
					System.err.println("Exception during Categories Migration: "+e.getMessage());
				}
				e.printStackTrace();
			}
		}
		
}
