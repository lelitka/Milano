package com.milano.migrate;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

import com.milano.migrate.util.Constants;
import com.milano.migrate.util.ImageSize;
import com.milano.migrate.util.Utils;

//TODO - write in the log file
public class MigrateCategory {
	
		private final static String CATEGORY_MIGRATION_LOG_FILE = "CategoryMigration"; 
		private static Path file = null;
		private static Connection msSqlConnection;
		private static Connection mySqlConnection;
		
		public static void migrateCategories(String storeId) throws SQLException, IOException, ClassNotFoundException{
			 msSqlConnection = Main.getMilanoUser().getMsSqlConnection();
			 mySqlConnection = Main.getMilanoUser().getMySqlConnection();

			/*1. Get Call categories from Milano
			 *2. For every category do:
			 *			2.a check if category with tabcod exists in Broadleaf
			 *			  if exists, update
			 *			  if does not, insert
			 *			  write into log file
			 */
			try {
				 mySqlConnection.setAutoCommit(false); // make 
				 file = Utils.getFile(CATEGORY_MIGRATION_LOG_FILE, true);
				 Utils.appendToFile(file, "Category Migration Start");
				 Utils.appendToFile(file, "Getting List of Categories");
				 String query = "SELECT tabcod, description, created, short, categoryId "+
				 " FROM dbo.category where isactive=1 and storeId=?";
				 insertAdditionalCategories();
			//	 mySqlConnection.commit(); 
	     	     PreparedStatement preparedStmt = msSqlConnection.prepareStatement(query);
			     preparedStmt.setString (1, storeId);
				 ResultSet mResultSet = preparedStmt.executeQuery();
				 int count = 0;
				 while (mResultSet.next() && count<20) {
					 try {
							 int nextCategoryId = Utils.getNextSequenceNumber("CategoryImpl", true);//getNextCategoryId();
							 System.out.println("Next Category Id="+nextCategoryId);
							 HashMap<String, Object> map = insertCategoryRecord(mResultSet, nextCategoryId);
							 prepareMenuItemForCategory(mResultSet,  map);
							 MigrateProduct.migrateProductsForStoreAndCategory(storeId, mResultSet.getString("tabcod"), false);
							 count++;
						//	 mySqlConnection.commit(); 
					 } catch (SQLException e) {
						// Logger.getLogger(MigrateCategory.class.getName()).log(Level.SEVERE, null, e);
				         if (mySqlConnection != null)
				            { try  {
				            		mySqlConnection.rollback(); 
				            	} catch (SQLException i){}
				            }
				            e.printStackTrace();
				     } finally {
			         /*   if (mySqlConnection != null){
				                try {mySqlConnection.close();
				                } catch (SQLException e) { e.printStackTrace();}
				            }*/
				        }
					 //mResultSet.next();
			  }
			 Utils.appendToFile(file, "Done Migrating Categories");
			 mySqlConnection.commit();
			 //TODO check if any categories were deactivated in Milano and update if necessary
			} catch (Exception e){
				if (file!=null){
				 Utils.appendToFile(file, "Exception during Categories Migration: "+e.getMessage());
				} else {
					System.err.println("Exception during Categories Migration: "+e.getMessage());
				}
				e.printStackTrace();
			}
		}
		
		private static void insertAdditionalCategories() throws SQLException, ClassNotFoundException, IOException{
		     
		    		 
		     int nextCategoryId = Utils.getNextSequenceNumber("CategoryImpl", true);//getNextCategoryId();
	    	 java.util.Date date= new java.util.Date();
	    	 Timestamp created = new Timestamp(date.getTime());
	    	 String msqQuery = "insert into blc_category "+
	    	 "(CATEGORY_ID, ACTIVE_START_DATE, DESCRIPTION, DISPLAY_TEMPLATE, EXTERNAL_ID, LONG_DESCRIPTION, NAME, URL) values "+
	    	 "(?,?,?,?,?,?,?,?)";
		     PreparedStatement mySqlPreparedStatement = mySqlConnection.prepareStatement(msqQuery);
	    	 int catId = Utils.getCategoryId("kids");
	    	 if (catId==-1){
			     	mySqlPreparedStatement.setInt (1, nextCategoryId); //CATEGORY_ID
		     	    mySqlPreparedStatement.setTimestamp(2, created);//ACTIVE_START_DATE
		     	    mySqlPreparedStatement.setString (3, "Kids"); //DESCRIPTION
		     	    mySqlPreparedStatement.setString (4, null);//DISPLAY_TEMPLATE
		     	    mySqlPreparedStatement.setString (5, null);//EXTERNAL_ID
		     	    mySqlPreparedStatement.setString (6, "Kids"); //LONG_DESCRIPTION
		     	    mySqlPreparedStatement.setString (7, "Kids"); //NAME
		     	    mySqlPreparedStatement.setString (8, "/Kids"); //URL
		     	    mySqlPreparedStatement.execute();
	    	 }
	    	 catId = Utils.getCategoryId("other");
	    	 if (catId==-1){
		     	mySqlPreparedStatement.setInt (1, Utils.getNextSequenceNumber("CategoryImpl", true)); //CATEGORY_ID
	     	    mySqlPreparedStatement.setTimestamp(2, created);//ACTIVE_START_DATE
	     	    mySqlPreparedStatement.setString (3, "Other"); //DESCRIPTION
	     	    mySqlPreparedStatement.setString (4, null);//DISPLAY_TEMPLATE
	     	    mySqlPreparedStatement.setString (5, null);//EXTERNAL_ID
	     	    mySqlPreparedStatement.setString (6, "Other"); //LONG_DESCRIPTION
	     	    mySqlPreparedStatement.setString (7, "Other"); //NAME
	     	    mySqlPreparedStatement.setString (8, "/Other"); //URL
	     	    mySqlPreparedStatement.execute();
	    	 }
	    	 int shoesCategoryId = Utils.getCategoryId("shoes");
	    	 if (shoesCategoryId==-1){
	     	    shoesCategoryId = Utils.getNextSequenceNumber("CategoryImpl", true);
		     	mySqlPreparedStatement.setInt (1, shoesCategoryId); //CATEGORY_ID
	     	    mySqlPreparedStatement.setTimestamp(2, created);//ACTIVE_START_DATE
	     	    mySqlPreparedStatement.setString (3, "Shoes"); //DESCRIPTION
	     	    mySqlPreparedStatement.setString (4, null);//DISPLAY_TEMPLATE
	     	    mySqlPreparedStatement.setString (5, null);//EXTERNAL_ID
	     	    mySqlPreparedStatement.setString (6, "Shoes"); //LONG_DESCRIPTION
	     	    mySqlPreparedStatement.setString (7, "Shoes"); //NAME
	     	    mySqlPreparedStatement.setString (8, "/shoes"); //URL
	     	    mySqlPreparedStatement.execute();
	    	 }
	    	 int womensShoesCategoryId = Utils.getCategoryId("womens shoes");
	    	 if (womensShoesCategoryId==-1){
	    		 womensShoesCategoryId = Utils.getNextSequenceNumber("CategoryImpl", true);
		     	 mySqlPreparedStatement.setInt (1, womensShoesCategoryId); //CATEGORY_ID
	     	     mySqlPreparedStatement.setTimestamp(2, created);//ACTIVE_START_DATE
	     	     mySqlPreparedStatement.setString (3, "Womens Shoes"); //DESCRIPTION
	     	     mySqlPreparedStatement.setString (4, null);//DISPLAY_TEMPLATE
	     	     mySqlPreparedStatement.setString (5, null);//EXTERNAL_ID
	     	     mySqlPreparedStatement.setString (6, "Womens Shoes"); //LONG_DESCRIPTION
	     	     mySqlPreparedStatement.setString (7, "Womens Shoes"); //NAME
	     	     mySqlPreparedStatement.setString (8, "/womensshoes"); //URL
	     	     mySqlPreparedStatement.execute();
	    	 }
	    	 int mensShoesCategoryId = Utils.getCategoryId("mens shoes");
	    	 if (mensShoesCategoryId==-1){
	     	    mensShoesCategoryId = Utils.getNextSequenceNumber("CategoryImpl", true);
		     	mySqlPreparedStatement.setInt (1, mensShoesCategoryId); //CATEGORY_ID
	     	    mySqlPreparedStatement.setTimestamp(2, created);//ACTIVE_START_DATE
	     	    mySqlPreparedStatement.setString (3, "Mens Shoes"); //DESCRIPTION
	     	    mySqlPreparedStatement.setString (4, null);//DISPLAY_TEMPLATE
	     	    mySqlPreparedStatement.setString (5, null);//EXTERNAL_ID
	     	    mySqlPreparedStatement.setString (6, "Mens Shoes"); //LONG_DESCRIPTION
	     	    mySqlPreparedStatement.setString (7, "Mens Shoes"); //NAME
	     	    mySqlPreparedStatement.setString (8, "/mensshoes"); //URL
	     	    mySqlPreparedStatement.execute();
	    	 }
	    	 int kidsShoesCategoryId = Utils.getCategoryId("kids shoes");
	    	 if (kidsShoesCategoryId==-1){
	     	    kidsShoesCategoryId = Utils.getNextSequenceNumber("CategoryImpl", true);
		     	mySqlPreparedStatement.setInt (1, kidsShoesCategoryId); //CATEGORY_ID
	     	    mySqlPreparedStatement.setTimestamp(2, created);//ACTIVE_START_DATE
	     	    mySqlPreparedStatement.setString (3, "Kids Shoes"); //DESCRIPTION
	     	    mySqlPreparedStatement.setString (4, null);//DISPLAY_TEMPLATE
	     	    mySqlPreparedStatement.setString (5, null);//EXTERNAL_ID
	     	    mySqlPreparedStatement.setString (6, "Kids Shoes"); //LONG_DESCRIPTION
	     	    mySqlPreparedStatement.setString (7, "Kids Shoes"); //NAME
	     	    mySqlPreparedStatement.setString (8, "/kidsshoes"); //URL
	     	    mySqlPreparedStatement.execute();
	    	 }
	     	    int merchandiseCategoryId = Utils.getCategoryId("merchandise");
				int mensCategoryId = Utils.getCategoryId("mens");
				int womensCategoryId = Utils.getCategoryId("womens");
				int kidsCategoryId = Utils.getCategoryId("kids");
				int otherCategoryId = Utils.getCategoryId("other");
				 
				insertCategoryPicture(merchandiseCategoryId, "merchandise");
		     	insertCategoryPicture(mensCategoryId, "men");
		     	insertCategoryPicture(womensCategoryId, "women");
		     	insertCategoryPicture(kidsCategoryId, "kids");
		     	insertCategoryPicture(otherCategoryId, "other");
		     	insertCategoryPicture(shoesCategoryId, "shoes");
		     	insertCategoryPicture(mensShoesCategoryId, "mensshoes");
		     	insertCategoryPicture(womensShoesCategoryId, "womensshoes");
		     	insertCategoryPicture(kidsShoesCategoryId, "kidsshoes");

	     	    
	     	    Integer[] r = Utils.getNextSequenceForMenuItem();
				int nextSequenceId = r[0];
				int menuItemId = r[1];
				//getMenuId
				int shoesMenuId = menuItemId;
				menuItemId++;
				int mensMenuId = menuItemId;
				menuItemId++;
				int womensMenuId = menuItemId;
				menuItemId++;
				int kidsMenuId = menuItemId;
				menuItemId++;
				int otherMenuId = menuItemId;
				menuItemId++;
				int itemId = Utils.getCmsMenuItemId("/shoes");
				if (itemId==-1){
			     	    msqQuery = "insert into blc_cms_menu_item "+
						    	 "(MENU_ITEM_ID, ACTION_URL, LABEL, SEQUENCE, MENU_ITEM_TYPE, PARENT_MENU_ID) values "+
						    	 "(?,?,?,?,?,?)";
							     mySqlPreparedStatement = mySqlConnection.prepareStatement(msqQuery);
						     	 mySqlPreparedStatement.setInt (1, shoesMenuId); //MENU_ITEM_ID
						     	 mySqlPreparedStatement.setString(2, "/shoes"); //ACTION_URL
						     	 mySqlPreparedStatement.setString (3, "Shoes"); //LABEL
						     	 mySqlPreparedStatement.setInt (4, nextSequenceId++); //SEQUENCE
						     	 mySqlPreparedStatement.setString (5, "CATEGORY"); //MENU_ITEM_TYPE
						     	 mySqlPreparedStatement.setInt (6, 1); //PARENT_MENU_ID
						mySqlPreparedStatement.execute();
				}
/*	     	    msqQuery = "insert into blc_cms_menu_item "+
				    	 "(MENU_ITEM_ID, ACTION_URL, LABEL, SEQUENCE, MENU_ITEM_TYPE, PARENT_MENU_ID) values "+
				    	 "(?,?,?,?,?,?)";
					     mySqlPreparedStatement = mySqlConnection.prepareStatement(msqQuery);
				     	 mySqlPreparedStatement.setInt (1, menuItemId++); //MENU_ITEM_ID
				     	 mySqlPreparedStatement.setString(2, "/mensshoes"); //ACTION_URL
				     	 mySqlPreparedStatement.setString (3, "Mens Shoes"); //LABEL
				     	 mySqlPreparedStatement.setInt (4, nextSequenceId++); //SEQUENCE
				     	 mySqlPreparedStatement.setString (5, "CATEGORY"); //MENU_ITEM_TYPE
				     	 mySqlPreparedStatement.setInt (6, shoesMenuId); //PARENT_MENU_ID
				mySqlPreparedStatement.execute();
	     	    msqQuery = "insert into blc_cms_menu_item "+
				    	 "(MENU_ITEM_ID, ACTION_URL, LABEL, SEQUENCE, MENU_ITEM_TYPE, PARENT_MENU_ID) values "+
				    	 "(?,?,?,?,?,?)";
					     mySqlPreparedStatement = mySqlConnection.prepareStatement(msqQuery);
				     	 mySqlPreparedStatement.setInt (1, menuItemId++); //MENU_ITEM_ID
				     	 mySqlPreparedStatement.setString(2, "/womenshoes"); //ACTION_URL
				     	 mySqlPreparedStatement.setString (3, "Womens Shoes"); //LABEL
				     	 mySqlPreparedStatement.setInt (4, nextSequenceId++); //SEQUENCE
				     	 mySqlPreparedStatement.setString (5, "CATEGORY"); //MENU_ITEM_TYPE
				     	 mySqlPreparedStatement.setInt (6, shoesMenuId); //PARENT_MENU_ID
				mySqlPreparedStatement.execute();

	     	    msqQuery = "insert into blc_cms_menu_item "+
				    	 "(MENU_ITEM_ID, ACTION_URL, LABEL, SEQUENCE, MENU_ITEM_TYPE, PARENT_MENU_ID) values "+
				    	 "(?,?,?,?,?,?)";
					     mySqlPreparedStatement = mySqlConnection.prepareStatement(msqQuery);
				     	 mySqlPreparedStatement.setInt (1, menuItemId++); //MENU_ITEM_ID
				     	 mySqlPreparedStatement.setString(2, "/kidsshoes"); //ACTION_URL
				     	 mySqlPreparedStatement.setString (3, "Kids Shoes"); //LABEL
				     	 mySqlPreparedStatement.setInt (4, shoesMenuId); //SEQUENCE
				     	 mySqlPreparedStatement.setString (5, "CATEGORY"); //MENU_ITEM_TYPE
				     	 mySqlPreparedStatement.setInt (6, 1); //PARENT_MENU_ID
				mySqlPreparedStatement.execute();
				msqQuery = "insert into blc_cms_menu_item "+
				    	 "(MENU_ITEM_ID, ACTION_URL, LABEL, SEQUENCE, MENU_ITEM_TYPE, PARENT_MENU_ID) values "+
				    	 "(?,?,?,?,?,?)";
					     mySqlPreparedStatement = mySqlConnection.prepareStatement(msqQuery);
				     	 mySqlPreparedStatement.setInt (1, mensMenuId); //MENU_ITEM_ID
				     	 mySqlPreparedStatement.setString(2, "/mens"); //ACTION_URL
				     	 mySqlPreparedStatement.setString (3, "Mens"); //LABEL
				     	 mySqlPreparedStatement.setInt (4, nextSequenceId++); //SEQUENCE
				     	 mySqlPreparedStatement.setString (5, "CATEGORY"); //MENU_ITEM_TYPE
				     	 mySqlPreparedStatement.setInt (6, 1); //PARENT_MENU_ID
				mySqlPreparedStatement.execute();
	     	    msqQuery = "insert into blc_cms_menu_item "+
				    	 "(MENU_ITEM_ID, ACTION_URL, LABEL, SEQUENCE, MENU_ITEM_TYPE, PARENT_MENU_ID) values "+
				    	 "(?,?,?,?,?,?)";
					     mySqlPreparedStatement = mySqlConnection.prepareStatement(msqQuery);
				     	 mySqlPreparedStatement.setInt (1, womensMenuId);
				     	 mySqlPreparedStatement.setString(2, "/womens");
				     	 mySqlPreparedStatement.setString (3, "Womens");
				     	 mySqlPreparedStatement.setInt (4, nextSequenceId++);
				     	 mySqlPreparedStatement.setString (5, "CATEGORY");
				     	 mySqlPreparedStatement.setInt (6, 1);
				mySqlPreparedStatement.execute();
*/
				itemId = Utils.getCmsMenuItemId("/kids");
				if (itemId==-1){
					msqQuery = "insert into blc_cms_menu_item "+
					    	 "(MENU_ITEM_ID, ACTION_URL, LABEL, SEQUENCE, MENU_ITEM_TYPE, PARENT_MENU_ID) values "+
					    	 "(?,?,?,?,?,?)";
						     mySqlPreparedStatement = mySqlConnection.prepareStatement(msqQuery);
					     	 mySqlPreparedStatement.setInt (1, kidsMenuId);
					     	 mySqlPreparedStatement.setString(2, "/kids");
					     	 mySqlPreparedStatement.setString (3, "Kids");
					     	 mySqlPreparedStatement.setInt (4, nextSequenceId++);
					     	 mySqlPreparedStatement.setString (5, "CATEGORY");
					     	 mySqlPreparedStatement.setInt (6, 1);
					mySqlPreparedStatement.execute();
				}
				itemId = Utils.getCmsMenuItemId("/other");
				if (itemId==-1){
		     	    msqQuery = "insert into blc_cms_menu_item "+
					    	 "(MENU_ITEM_ID, ACTION_URL, LABEL, SEQUENCE, MENU_ITEM_TYPE, PARENT_MENU_ID) values "+
					    	 "(?,?,?,?,?,?)";
						     mySqlPreparedStatement = mySqlConnection.prepareStatement(msqQuery);
					     	 mySqlPreparedStatement.setInt (1, otherMenuId);
					     	 mySqlPreparedStatement.setString(2, "/other");
					     	 mySqlPreparedStatement.setString (3, "Other");
					     	 mySqlPreparedStatement.setInt (4, nextSequenceId++);
					     	 mySqlPreparedStatement.setString (5, "CATEGORY");
					     	 mySqlPreparedStatement.setInt (6, 1);
					mySqlPreparedStatement.execute();
				}
		
				//womens shoes
				 String query = "select max(DISPLAY_ORDER) from  BLC_CATEGORY_XREF "+
					 		"where CATEGORY_ID=?";
				 mySqlPreparedStatement = mySqlConnection.prepareStatement(query);
				 mySqlPreparedStatement.setInt(1, shoesCategoryId);
				 ResultSet rs = mySqlPreparedStatement.executeQuery();
			     Boolean recordExists = rs.getRow()!=0;
			     int displayOrder = 1;
			     if (recordExists){
			    	 rs.next();
			    	 displayOrder = rs.getInt(1);
			     }
			     int categoryXrefId = 0;
			     catId = Utils.getCategoryXRefId(mensShoesCategoryId);
			     if (catId==-1){
					 categoryXrefId = Utils.getNextSequenceNumber("CategoryXrefImpl", true);
				     query = "INSERT INTO BLC_CATEGORY_XREF "+
							 		"(CATEGORY_XREF_ID, DEFAULT_REFERENCE, DISPLAY_ORDER, CATEGORY_ID, SUB_CATEGORY_ID) "+
							 		" VALUES (?,?,?,?,?)";
					 mySqlPreparedStatement = mySqlConnection.prepareStatement(query);
					 mySqlPreparedStatement.setInt(1, categoryXrefId);
					 mySqlPreparedStatement.setInt(2, 1);
					 mySqlPreparedStatement.setInt(3, displayOrder);
					 mySqlPreparedStatement.setInt(4, shoesCategoryId); //CATEGORY_ID
					 mySqlPreparedStatement.setInt(5, mensShoesCategoryId); //SUB_CATEGORY_ID
					 mySqlPreparedStatement.execute();
			     }
			     catId = Utils.getCategoryXRefId(womensShoesCategoryId);
			     if (catId==-1){
					 categoryXrefId = Utils.getNextSequenceNumber("CategoryXrefImpl", true);
					 mySqlPreparedStatement = mySqlConnection.prepareStatement(query);
					 mySqlPreparedStatement.setInt(1, categoryXrefId);
					 mySqlPreparedStatement.setInt(2, 1);
					 mySqlPreparedStatement.setInt(3, displayOrder);
					 mySqlPreparedStatement.setInt(4, shoesCategoryId); //CATEGORY_ID
					 mySqlPreparedStatement.setInt(5, womensShoesCategoryId); //SUB_CATEGORY_ID
					 mySqlPreparedStatement.execute();
			     }
			     catId = Utils.getCategoryXRefId(kidsShoesCategoryId);
			     if (catId==-1){
					 categoryXrefId = Utils.getNextSequenceNumber("CategoryXrefImpl", true);
					 mySqlPreparedStatement = mySqlConnection.prepareStatement(query);
					 mySqlPreparedStatement.setInt(1, categoryXrefId);
					 mySqlPreparedStatement.setInt(2, 1);
					 mySqlPreparedStatement.setInt(3, displayOrder);
					 mySqlPreparedStatement.setInt(4, shoesCategoryId); //CATEGORY_ID
					 mySqlPreparedStatement.setInt(5, kidsShoesCategoryId); //SUB_CATEGORY_ID
					 mySqlPreparedStatement.execute();
			     }
		
		}
		
		private static HashMap<String, Object> insertCategoryRecord(ResultSet mResultSet, int categoryId) throws SQLException{
			 Connection mySqlConnection = Main.getMilanoUser().getMySqlConnection();

			 Utils.appendToFile(file, "Checking for Category with tabcod "+mResultSet.getString(1) + ", " + mResultSet.getString(2));
			 HashMap<String, Object> map = new HashMap<String, Object>();
			 int realCategoryId = categoryId;
			 String url = null;
			 System.out.println(mResultSet.getString(1) + ", " + mResultSet.getString(2));
		     String query1 = "Select category_Id, url from blc_category where external_id=?";
    	     PreparedStatement preparedStmt1 = mySqlConnection.prepareStatement(query1);
		     preparedStmt1.setString (1, mResultSet.getString(1));
		     ResultSet resultSet1 = preparedStmt1.executeQuery();
		    // if (resultSet1.getRow()==0)
		     resultSet1.next();
		     Boolean recordExists = resultSet1.getRow()!=0;
		     String msqQuery;
		     if (!recordExists){
		    	 //Insert
		    	 url = "/"+mResultSet.getString("short");
				 Utils.appendToFile(file, "Category does not exist in Boradleaf, inserting");
		    	 msqQuery = "insert into blc_category "+
		    	 "(CATEGORY_ID, ACTIVE_START_DATE, DESCRIPTION, DISPLAY_TEMPLATE, EXTERNAL_ID, LONG_DESCRIPTION, NAME, URL) values "+
		    	 "(?,?,?,?,?,?,?,?)";
		     } else {
		    	 realCategoryId = resultSet1.getInt(1);
		    	 url = resultSet1.getString(2);
				 Utils.appendToFile(file, "Category exists in Boradleaf, updating");
		    	 msqQuery = "Update blc_category "+
		    	 "set ACTIVE_START_DATE=?, DESCRIPTION=?, DISPLAY_TEMPLATE=?, EXTERNAL_ID=?, LONG_DESCRIPTION=?, NAME=?, URL=? "+
		    	 "where CATEGORY_ID=?";
		     }
		     PreparedStatement mySqlPreparedStatement = mySqlConnection.prepareStatement(msqQuery);
		     if (!recordExists){
	     	    mySqlPreparedStatement.setInt (1, realCategoryId);
	     	    mySqlPreparedStatement.setTimestamp(2, mResultSet.getTimestamp("created"));
	     	    mySqlPreparedStatement.setString (3, mResultSet.getString("description"));
	     	    mySqlPreparedStatement.setString (4, null);//"layout/"+mResultSet.getString("short"));
	     	    mySqlPreparedStatement.setString (5, mResultSet.getString("tabcod"));//mResultSet.getString("categoryId"));
	     	    mySqlPreparedStatement.setString (6, mResultSet.getString("description"));
	     	    mySqlPreparedStatement.setString (7, mResultSet.getString("description"));
	     	    mySqlPreparedStatement.setString (8, "/"+mResultSet.getString("short"));
		     } else {
		     	    mySqlPreparedStatement.setInt(8, realCategoryId);
		     	    mySqlPreparedStatement.setTimestamp(1, mResultSet.getTimestamp("created"));
		     	    mySqlPreparedStatement.setString (2, mResultSet.getString("description"));
		     	    mySqlPreparedStatement.setString (3, null);//"layout/"+mResultSet.getString("short"));
		     	    mySqlPreparedStatement.setString (4, mResultSet.getString("tabcod"));//mResultSet.getString("categoryId"));
		     	    mySqlPreparedStatement.setString (5, mResultSet.getString("description"));
		     	    mySqlPreparedStatement.setString (6, mResultSet.getString("description"));
		     	    mySqlPreparedStatement.setString (7, "/"+mResultSet.getString("short"));			    	 
		     }
		     mySqlPreparedStatement.execute();
			 Utils.appendToFile(file, "Done "+(recordExists? "Updating ": "Inserting ")+" for Category "+mResultSet.getString(1) + ", " + mResultSet.getString(2));
			 map.put("categoryId", realCategoryId);
			 map.put("url", url);
			 return map;
		}
		
		private static void prepareMenuItemForCategory(ResultSet mResultSet, HashMap<String, Object> map) throws SQLException, ClassNotFoundException, IOException{

			int categoryId = ((Integer)map.get("categoryId")).intValue();
			String url = (String)map.get("url");
			//1. get next sequence number for in menu blc_cms_menu_item;
			// insert category item into blc_cms_menu_item;
			//select max(sequence) from blc_cms_menu_item;
//			 int nextSequenceId = Utils.getNextSequenceForMenuItem(mySqlConnection);
			 int mensCategoryId = Utils.getCategoryId("mens");
			 int womensCategoryId = Utils.getCategoryId("womens");
			 int kidsCategoryId = Utils.getCategoryId("kids");
			 int otherCategoryId = Utils.getCategoryId("other");
			 int shoesCategoryId = Utils.getCategoryId("shoes");
			 int mensShoesCategoryId = Utils.getCategoryId("Mens Shoes");
			 int womensShoesCategoryId = Utils.getCategoryId("Womens Shoes");
			 int kidsShoesCategoryId = Utils.getCategoryId("Kids Shoes");
			 

			 
			 
			 //Utils.appendToFile(file, "Merchandise categoryId = "+merchandiseCategoryId+" mensCategoryId="+mensCategoryId+
				//	 " womensCategoryId="+womensCategoryId);
			 String categoryName = mResultSet.getString("description");
			 int parentCategoryid =0;
			 if ((categoryName.toLowerCase().contains("mens")) &&
					 (categoryName.toLowerCase().contains("sandals") || categoryName.toLowerCase().contains("shoes") || categoryName.toLowerCase().contains("boots")
					 || categoryName.toLowerCase().contains("slippers"))){
				 parentCategoryid = mensShoesCategoryId; 
			 } else if ((categoryName.toLowerCase().contains("womens") || categoryName.toLowerCase().contains("ladies")) &&
					 (categoryName.toLowerCase().contains("sandals") || categoryName.toLowerCase().contains("shoes") || categoryName.toLowerCase().contains("boots")
					 || categoryName.toLowerCase().contains("slippers"))){
				 parentCategoryid = womensShoesCategoryId; 
			 } else if ((categoryName.toLowerCase().contains("kids")) &&
					 (categoryName.toLowerCase().contains("sandals") || categoryName.toLowerCase().contains("shoes") || categoryName.toLowerCase().contains("boots")
					 || categoryName.toLowerCase().contains("slippers"))){
				 parentCategoryid = kidsShoesCategoryId; 
			 } else if (categoryName.toLowerCase().contains("womens") || categoryName.toLowerCase().contains("ladies")){
				 parentCategoryid = womensCategoryId; 
			 } else if (categoryName.toLowerCase().contains("mens")){
				 parentCategoryid = mensCategoryId; 
			 } else if (categoryName.toLowerCase().contains("kids")){
				 parentCategoryid = kidsCategoryId; 
			 } else {
				 parentCategoryid = otherCategoryId; 
			 } 
			 int categoryXrefId = Utils.getNextSequenceNumber("CategoryXrefImpl", true);
			 String query = "select max(DISPLAY_ORDER) from  BLC_CATEGORY_XREF "+
				 		"where CATEGORY_ID=?";
			 PreparedStatement mySqlPreparedStatement = mySqlConnection.prepareStatement(query);
			 mySqlPreparedStatement.setInt(1, parentCategoryid);
			 ResultSet rs = mySqlPreparedStatement.executeQuery();
		     Boolean recordExists = rs.getRow()!=0;
		     int displayOrder = 1;
		     if (recordExists){
		    	 rs.next();
		    	 displayOrder = rs.getInt(1);
		     }
		     int catId = Utils.getCategoryXRefId(categoryId);
		     if (catId==-1){
				     query = "INSERT INTO BLC_CATEGORY_XREF "+
							 		"(CATEGORY_XREF_ID, DEFAULT_REFERENCE, DISPLAY_ORDER, CATEGORY_ID, SUB_CATEGORY_ID) "+
							 		" VALUES (?,?,?,?,?)";
					 mySqlPreparedStatement = mySqlConnection.prepareStatement(query);
					 mySqlPreparedStatement.setInt(1, categoryXrefId); //CATEGORY_XREF_ID
					 mySqlPreparedStatement.setInt(2, 1); //DEFAULT_REFERENCE
					 mySqlPreparedStatement.setInt(3, displayOrder); //DISPLAY_ORDER
					 mySqlPreparedStatement.setInt(4, parentCategoryid); //CATEGORY_ID
					 mySqlPreparedStatement.setInt(5, categoryId); //SUB_CATEGORY_ID
					 mySqlPreparedStatement.execute();
		     }
/*		 Utils.appendToFile(file, "Checking for blc_cms_menu_item with tabcod "+mResultSet.getString(1) + ", " + mResultSet.getString(2));
			 System.out.println(mResultSet.getString(1) + ", " + mResultSet.getString(2));
		     String query1 = "Select sequence, menu_item_id as count from blc_cms_menu_item where action_url=?";
		     PreparedStatement preparedStmt1 = mySqlConnection.prepareStatement(query1);
		     preparedStmt1.setString (1, url);
		     ResultSet resultSet1 = preparedStmt1.executeQuery();
		     resultSet1.next();
		     Boolean recordExists = resultSet1.getRow()!=0;
		     String msqQuery;
		     PreparedStatement mySqlPreparedStatement = null;
		     if (!recordExists){
		    	 //Insert
		    	 //TODO insert into BLC_CATEGORY_XREF
		    	 //1.check if 'mens' is in ategory description
				 Utils.appendToFile(file, "Row does not exist in Boradleaf.blc_cms_menu_item, inserting");
			 	 Integer[] r = Utils.getNextSequenceForMenuItem();
				 int nextSequenceId = r[0];
				 int menuItemId = r[1];
				 msqQuery = "insert into blc_cms_menu_item "+
		    	 "(MENU_ITEM_ID, ACTION_URL, LABEL, SEQUENCE, MENU_ITEM_TYPE, PARENT_MENU_ID) values "+
		    	 "(?,?,?,?,?,?)";
			     mySqlPreparedStatement = mySqlConnection.prepareStatement(msqQuery);
		     	 mySqlPreparedStatement.setInt (1, menuItemId++);
		     	 mySqlPreparedStatement.setString(2, url);
		     	 mySqlPreparedStatement.setString (3, mResultSet.getString("description"));
		     	 mySqlPreparedStatement.setInt (4, nextSequenceId++);
		     	 mySqlPreparedStatement.setString (5, "CATEGORY");
		     	 mySqlPreparedStatement.setInt (6, 1);
		     } else {
				 Utils.appendToFile(file, "Row exists in Boradleaf.blc_cms_menu_item, updating");
		    	 msqQuery = "Update blc_cms_menu_item "+
		    	 "set ACTION_URL=?, LABEL=?, SEQUENCE=?, MENU_ITEM_TYPE=?, PARENT_MENU_ID=? "+
		    	 "where MENU_ITEM_ID=?";
			     mySqlPreparedStatement = mySqlConnection.prepareStatement(msqQuery);
		     	 mySqlPreparedStatement.setString(1, url);
		     	 mySqlPreparedStatement.setString (2, mResultSet.getString("description"));
		     	 mySqlPreparedStatement.setInt (3, resultSet1.getInt(1));
		     	 mySqlPreparedStatement.setString (4, "CATEGORY");
		     	 mySqlPreparedStatement.setInt (5, 1);
		     	 mySqlPreparedStatement.setInt (6, resultSet1.getInt(2));
		     }
		     mySqlPreparedStatement.execute();
			 Utils.appendToFile(file, "Done "+(url!=null? "Updating ": "Inserting ")+" Menu Item for Category "+categoryId+" url "+url+" "+mResultSet.getString(1) + ", " + mResultSet.getString(2));
*/
			
		}
		
		private static void insertCategoryPicture(int categoryId, String categoryName) throws SQLException, ClassNotFoundException, IOException{
			 Utils.appendToFile(file, "Handling picture stuff for categoryId="+categoryId+" "+categoryName);
			 String fileName = categoryName.concat(".jpg");
			 String milanoFilePathString = Utils.getPicFolder(Main.getMilanoUser(), "categoryPictures").concat("/").concat(fileName);
			 String broadleafFilePathString = Main.getMilanoUser().getBroadleafPicsFolder().concat("/").concat(fileName);
			 Path broadleafPath = Paths.get(Main.getMilanoUser().getBroadleafPicsFolder());
			 try {
					 if (Files.exists(broadleafPath)) {
							 Utils.appendToFile(file, "Folder "+Main.getMilanoUser().getBroadleafPicsFolder()+" exists");
					 }
			 } catch (Exception e){
					 Utils.appendToFile(file, "Exception while checking if file "+broadleafPath+" exists: "+e.getMessage());
					 try {
						 Files.createDirectory(broadleafPath); 
					 } catch (Exception e1){
						 Utils.appendToFile(file,"Exception while creating directory "+broadleafPath+": "+e1.getMessage());
					 }
			 }
			 Dimension dim = ImageSize.get(new File(milanoFilePathString));	
			 int width = dim.width;
			 int height = dim.height;
			 File f = new File(milanoFilePathString);
			 double size = f.length();
			 Utils.appendToFile(file,"File Size: " + String.format("%.2f", size) + "kb");
			 Utils.appendToFile(file,"Dimensions for the file - width="+width+" height="+height);
			 try {
					     FileUtils.copyFile(new File(milanoFilePathString), new File(broadleafFilePathString));;
					     Utils.appendToFile(file, "Copied file from "+milanoFilePathString+" to "+broadleafFilePathString);
			 } catch (IOException e) {
					     e.printStackTrace();
			 }
					 
			 int staticAssetId = Utils.getNextSequenceNumber("StaticAssetImpl", true);
	    	 java.util.Date date= new java.util.Date();
	    	 Timestamp created = new Timestamp(date.getTime());
	         String[] filenameList = fileName.split("\\.(?=[^\\.]+$)"); //split filename from it's extension
	         String mimeType = "image/jpeg";
	         if (filenameList[1].toLowerCase().equalsIgnoreCase("jpg")){
		         mimeType = "image/jpeg";
	         } else if (filenameList[1].toLowerCase().equalsIgnoreCase("png")){
		         mimeType = "image/png";
	         } else if (filenameList[1].toLowerCase().equalsIgnoreCase("bmp")){
		         mimeType = "image/bmp";
	         }
	         int assetId = Utils.getStaticAssetId(fileName);
	         if (assetId==-1){
				 String query = "INSERT INTO blc_static_asset "+
					   "(STATIC_ASSET_ID, CREATED_BY, DATE_CREATED, DATE_UPDATED,"+
					   " FILE_EXTENSION, FILE_SIZE, FULL_URL, MIME_TYPE, NAME, STORAGE_TYPE, TITLE)"+
					   " VALUES (?,?,?,?,   ?,?,?,?,?,?,?)";
				 PreparedStatement stmt = mySqlConnection.prepareStatement(query);
				 stmt.setInt(1,staticAssetId); //STATIC_ASSET_ID
				 stmt.setInt(2,-1); //CREATED_BY
				 stmt.setTimestamp(3, created); //DATE_CREATED
				 stmt.setTimestamp(4, created); //DATE_UPDATED
				 stmt.setString(5,filenameList[1]); //FILE_EXTENSION
				 stmt.setInt(6,(int)Math.ceil(size)); //FILE_SIZE
				 stmt.setString(7,"/category/".concat(fileName));  //FULL_URL ///product/1/Lelitka.jpg
				 stmt.setString(8, mimeType); //MIME_TYPE
				 stmt.setString(9, fileName); //NAME
				 stmt.setString(10, "FILESYSTEM");//STORAGE_TYPE
				 stmt.setString(11, null); //TITLE
				 stmt.execute();
				 Utils.appendToFile(file, "Insert into blc_static_asset done");
						 
				 query = "INSERT INTO blc_img_static_asset (HEIGHT, WIDTH, STATIC_ASSET_ID)"+
				 	  " VALUES (?,?,?)";
				 stmt = mySqlConnection.prepareStatement(query);
				 stmt.setInt(1,height);
				 stmt.setInt(2,width);
				 stmt.setInt(3,staticAssetId); //STATIC_ASSET_ID
				 stmt.execute();
				 Utils.appendToFile(file, "Insert into blc_img_static_asset done");
				 
				 int mediaId = Utils.getNextSequenceNumber("MediaImpl", true);
					 
				 query = "INSERT INTO blc_media (MEDIA_ID, URL)"+
						 " VALUES (?,?)";
				 stmt = mySqlConnection.prepareStatement(query);
				 stmt.setInt(1,mediaId);
				 stmt.setString(2,"/cmsstatic/category/".concat(fileName));
				 stmt.execute();
				 Utils.appendToFile(file, "Insert into blc_media done");
				 int categoryMediaMapId  = Utils.getNextCategoryMediaMapId();
				 query = "INSERT INTO blc_category_media_map (CATEGORY_MEDIA_ID, MAP_KEY, MEDIA_ID, BLC_CATEGORY_CATEGORY_ID)"+
					 " VALUES (?,?,?,?)";
				 stmt = mySqlConnection.prepareStatement(query);
				 stmt.setInt(1,categoryMediaMapId); //CATEGORY_MEDIA_ID
				 stmt.setString(2,"primary"); //MAP_KEY
				 stmt.setInt(3,mediaId); //MEDIA_ID
				 stmt.setInt(4, categoryId); //BLC_CATEGORY_CATEGORY_ID
				 stmt.execute();
				 Utils.appendToFile(file, "Insert into blc_category_media_map done");
	         }
		}
	
		
		
		
}
