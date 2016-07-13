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
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

import com.milano.migrate.util.Constants;
import com.milano.migrate.util.ImageSize;
import com.milano.migrate.util.Utils;

//TODO - write in the log file
public class MigrateProduct {
	
		private final static String PRODUCT_MIGRATION_LOG_FILE = "ProductMigration"; 
		private static Path file = null;
		private static Connection msSqlConnection;
		private static Connection mySqlConnection;

		
		
		public static void migrateTestProductsForStoreAndCategory(String storeId_, String categoryId_) throws SQLException, IOException, ClassNotFoundException{
			try {
				 msSqlConnection = Main.getMilanoUser().getMsSqlConnection();
				 mySqlConnection = Main.getMilanoUser().getMySqlConnection();
				 file = Utils.getFile(PRODUCT_MIGRATION_LOG_FILE, true);
				 //We are assuming that category is already migrated
				 // get category ID from db
				 mySqlConnection.setAutoCommit(false); // make 
			     String query = "Select category_Id, url from blc_category where external_id=?";
	    	     PreparedStatement preparedStmt1 = mySqlConnection.prepareStatement(query);
			     preparedStmt1.setString (1, categoryId_);
			     ResultSet resultSet1 = preparedStmt1.executeQuery();
			     resultSet1.next();
				 int iCategoryId = resultSet1.getInt(1);
				 Utils.appendToFile(file, "Product Migration Start");
				 Utils.appendToFile(file, "Getting List of Products");
				 query =" select  m.manufactid, m.short, m.tabcod, m.description, i.inventid, i.icate, i.gridid, i.icode, i.idesc, "+
						 " c.short as cshort, i.created, i.storeId, i.iprce, cl.description as cDescription, i.icolor "+
						 " from dbo.invent i, dbo.manufact m, dbo.category c, dbo.color cl " +
						 " where i.STOREID=? and i.iCATE=? and i.MANUFACTID=m.MANUFACTID and i.icate=c.tabcod and "+
						 " cl.tabcod=i.icolor and i.ISACTIVE='1' and i.inventId=?";
	     	     PreparedStatement preparedStmt = msSqlConnection.prepareStatement(query);
			     preparedStmt.setString (1, storeId_);
			     preparedStmt.setString (2, categoryId_);
			     preparedStmt.setString (3, "B8BA1D10-F79B-4655-9015-9BB800F9BD5A");//"2AC43284-BDF2-4D70-811C-2705A4404B3B");
				 ResultSet mResultSet = preparedStmt.executeQuery();
				 int nextProductId = 0;
				 while (mResultSet.next()) {
					 try {	
						 	 nextProductId = Utils.getNextSequenceNumber("ProductImpl", true);//Utils.getNextProductId(mySqlConnection);
						 	 Utils.appendToFile(file, "Next Product Id to insert is ="+nextProductId);
							 mySqlConnection.setAutoCommit(false); // make 
							 migrateProductRecord(mResultSet, nextProductId, iCategoryId, storeId_);
							 
					 } catch (SQLException e) {
						// Logger.getLogger(MigrateCategory.class.getName()).log(Level.SEVERE, null, e);
				         if (mySqlConnection != null)
				            { try  {
				            		mySqlConnection.rollback(); 
				            	} catch (SQLException i){}
				            }
				            e.printStackTrace();
				     } 
			  }
		 	 mySqlConnection.commit();

			 Utils.appendToFile(file, "Done Migrating Categories");
			 //TODO check if any categories were deactivated in Milano and update if necessary
			} catch (Exception e){
				if (file!=null){
				 Utils.appendToFile(file, "Exception during Categories Migration: "+e.getMessage());
				} else {
					System.err.println("Exception during Categories Migration: "+e.getMessage());
				}
				e.printStackTrace();
			} finally {
	            /*if (mySqlConnection != null){
	                try {mySqlConnection.close();
	                } catch (SQLException e) { e.printStackTrace();}
	            }*/
	        }
		}

		
		public static void migrateProductsForStoreAndCategory(String storeId_, String categoryId_, boolean separateCommit) throws SQLException, IOException, ClassNotFoundException{
			try {
				 msSqlConnection = Main.getMilanoUser().getMsSqlConnection();
				 mySqlConnection = Main.getMilanoUser().getMySqlConnection();
				 file = Utils.getFile(PRODUCT_MIGRATION_LOG_FILE, true);
				 //We are assuming that category is already migrated
				 // get category ID from db
			     String query = "Select category_Id, url from blc_category where external_id=?";
	    	     PreparedStatement preparedStmt1 = mySqlConnection.prepareStatement(query);
			     preparedStmt1.setString (1, categoryId_);
			     ResultSet resultSet1 = preparedStmt1.executeQuery();
			     resultSet1.next();
				 int iCategoryId = resultSet1.getInt(1);
				 Utils.appendToFile(file, "Product Migration Start for category with tabcod="+categoryId_+" and storeId="+storeId_);
				 Utils.appendToFile(file, "Getting List of Products");
				 query =" select  m.manufactid, m.short, m.tabcod, m.description, i.inventid, i.icate, i.gridid, i.icode, i.idesc, "+
						 " c.short as cshort, i.created, i.storeId, i.iprce, cl.description as cDescription, i.icolor "+
						 " from dbo.invent i, dbo.manufact m, dbo.category c, dbo.color cl " +
						 " where i.STOREID=? and i.iCATE=? and i.MANUFACTID=m.MANUFACTID and i.icate=c.tabcod and "+
						 " cl.tabcod=i.icolor and i.ISACTIVE='1'";
	     	     PreparedStatement preparedStmt = msSqlConnection.prepareStatement(query);
			     preparedStmt.setString (1, storeId_);
			     preparedStmt.setString (2, categoryId_);
				 ResultSet mResultSet = preparedStmt.executeQuery();
				 int nextProductId = 0;
				 int numberOfMigratedProducts = 0;
				 Utils.appendToFile(file, "Migrating "+Main.numberOfProductsPerCategory+" products for category "+categoryId_);
				 while (mResultSet.next() && numberOfMigratedProducts<Main.numberOfProductsPerCategory) {
					 try {	
						 	 nextProductId = Utils.getNextSequenceNumber("ProductImpl", true);//Utils.getNextProductId(mySqlConnection);
						 	 Utils.appendToFile(file, "Next Product Id to insert is ="+nextProductId);
							 if (separateCommit){
								 mySqlConnection.setAutoCommit(false); // make 
							 }
							 migrateProductRecord(mResultSet, nextProductId, iCategoryId, storeId_);
							 if (separateCommit){
								 mySqlConnection.commit();
							 }
							 numberOfMigratedProducts++;
							 
					 } catch (SQLException e) {
						// Logger.getLogger(MigrateCategory.class.getName()).log(Level.SEVERE, null, e);
				         if (mySqlConnection != null)
				            { try  {
				            		mySqlConnection.rollback(); 
				            	} catch (SQLException i){}
				            }
				            e.printStackTrace();
				     } 
					 //mResultSet.next();
			  }
			 Utils.appendToFile(file, "Done Migrating Product for category with tabcod="+categoryId_+" and storeId="+storeId_);
			 //TODO check if any categories were deactivated in Milano and update if necessary
			} catch (Exception e){
				if (file!=null){
				 Utils.appendToFile(file, "Exception during Categories Migration: "+e.getMessage());
				} else {
					System.err.println("Exception during Categories Migration: "+e.getMessage());
				}
				e.printStackTrace();
			} finally {
	          /*  if (mySqlConnection != null){
	                try {mySqlConnection.close();
	                } catch (SQLException e) { e.printStackTrace();}
	            }*/
	        }
		}
		
		private static void insertPicture(ResultSet mResultSet, int productId, int categoryId) throws SQLException, ClassNotFoundException, IOException{
			 //Find out if file with picture exists - its icode.*
			 // FOR NOW we assume that jpg is only extension
			//TODO for bmp, jpg, jpeg etc
			 String noSpacesDesc = mResultSet.getString("idesc").replaceAll("\\s+","_");			 
	    	 String broadleafFileName = noSpacesDesc;//"/"+mResultSet.getString("cshort")+"_"+noSpacesDesc;

			 String iCode = mResultSet.getString("ICODE");
			 String fileName = Utils.getFileNameWithExtension(Main.getMilanoUser(), iCode);
			 String milanoFilePathString = null;
			 String broadleafFilePathString = null;
			 broadleafFileName = broadleafFileName.concat(".png");
			 if (fileName==null){
				 //here we assume that merchandise.png file exists in milano pic folder
				 milanoFilePathString = Main.getMilanoUser().getMilanoPicsFolder().concat("/").concat(broadleafFileName);
				 broadleafFilePathString = Main.getMilanoUser().getBroadleafPicsFolder().concat("/").concat(broadleafFileName);
			     Utils.appendToFile(file, "File does not exists, copying default file");
			     FileUtils.copyFile(new File(Main.getMilanoUser().getMilanoPicsFolder().concat("/").concat(Constants.DEFAULT_PICTURE_NAME)), new File(milanoFilePathString));;
			     Utils.appendToFile(file, "Copied file from "+milanoFilePathString+" to "+broadleafFilePathString);
			 } else {
				 milanoFilePathString = Main.getMilanoUser().getMilanoPicsFolder().concat("/").concat(fileName);
				 broadleafFilePathString = Main.getMilanoUser().getBroadleafPicsFolder().concat("/").concat(broadleafFileName);
			 }
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
			 System.out.println("File Size: " + String.format("%.2f", size) + "kb");
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
	         String[] filenameList = broadleafFileName.split("\\.(?=[^\\.]+$)"); //split filename from it's extension
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
				 stmt.setString(7,"/product/".concat(productId+"/").concat(broadleafFileName));  //FULL_URL ///product/1/Lelitka.jpg
				 stmt.setString(8, mimeType); //MIME_TYPE
				 stmt.setString(9, mResultSet.getString("idesc"));//broadleafFileName); //NAME
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
				 stmt.setString(2,"/cmsstatic/product/".concat(productId+"/").concat(broadleafFileName));
				 stmt.execute();
				 Utils.appendToFile(file, "Insert into blc_media done");
				 int defaultSkuId = Utils.getDefaultSkuId(productId);
				 int skuId =Utils.getSkuMediaMapId(defaultSkuId);
				 if (skuId==-1){
					 int skuMediaMapId  = Utils.getNextSkuMediaMapId();
//					 int skuMediaMapId  = Utils.getNextSequenceNumber("SkuMediaXrefImpl", true);//Utils.getNextSkuMediaMapId();
					 query = "INSERT INTO blc_sku_media_map (SKU_MEDIA_ID, MAP_KEY, MEDIA_ID, BLC_SKU_SKU_ID)"+
						 " VALUES (?,?,?,?)";
					 stmt = mySqlConnection.prepareStatement(query);
					 stmt.setInt(1,skuMediaMapId); //SKU_MEDIA_ID
					 stmt.setString(2,"primary"); //MAP_KEY
					 stmt.setInt(3,mediaId);  //MEDIA_ID
					 stmt.setInt(4, defaultSkuId); //BLC_SKU_SKU_ID
					 stmt.execute();
					 Utils.appendToFile(file, "Insert into blc_sku_media_map done");
				 } else {
					 query = "update blc_sku_media_map set media_id =? where BLC_SKU_SKU_ID=?"+
							 " VALUES (?,?,?,?)";
						 stmt = mySqlConnection.prepareStatement(query);
						 stmt.setInt(1,skuId); //SKU_MEDIA_ID
						 stmt.setInt(2, defaultSkuId); //BLC_SKU_SKU_ID
						 stmt.execute();
						 Utils.appendToFile(file, "Updated blc_sku_media_map done");
					 
				 }
				 stmt.close();
	         }
					 //SELECT * FROM broadleaf.blc_static_asset;
				/*	 SELECT * FROM broadleaf.blc_img_static_asset;
					 File source = new File("H:\\work-temp\\file");
					 File dest = new File("H:\\work-temp\\file2");
					 //check if folder exists for product
					 try {
					     FileUtils.copyDirectory(source, dest);
					 } catch (IOException e) {
					     e.printStackTrace();
					 }
			    	/* Utils.appendToFile(file, " Updating default sku in product");
				     String query="select default_sku_id, icode from blc_product  where product_id=?";
			    	 PreparedStatement stmt = mySqlConnection.prepareStatement(query);
			    	 stmt.setInt (2, productId);
			    	 stmt.execute();
					select 
					blc_media
					INSERT INTO `broadleaf`.`blc_sku_media_map` (`SKU_MEDIA_ID`, `MAP_KEY`, `MEDIA_ID`, `BLC_SKU_SKU_ID`) VALUES ('1', 'primary', '100000', '10002');
					SELECT * FROM broadleaf.blc_sku_media_map;*/
		}
		
		private static void insertProduct(ResultSet mResultSet, int productId, int categoryId) throws SQLException, ClassNotFoundException, IOException{
			 Connection mySqlConnection = Main.getMilanoUser().getMySqlConnection();

			 Utils.appendToFile(file, "Inserting into blc_product ...");
			 String query="INSERT INTO blc_product "+
			 "(PRODUCT_ID, ARCHIVED, CAN_SELL_WITHOUT_OPTIONS, DISPLAY_TEMPLATE, IS_FEATURED_PRODUCT,"+
			 " MANUFACTURE, URL, DEFAULT_CATEGORY_ID)"+//, DEFAULT_SKU_ID) 
			 " VALUES (?,?,?,?,?,?,?,?)" ;
			 String noSpacesDesc = mResultSet.getString("idesc").replaceAll("\\s+","");			 
	    	 String url = "/"+mResultSet.getString("cshort")+"/"+noSpacesDesc;

	    	 PreparedStatement pstmt = mySqlConnection.prepareStatement(query);
	    	 pstmt.setInt (1, productId); //PRODUCT_ID
	    	 pstmt.setString(2, "N"); //ARCHIVED
	    	 pstmt.setInt (3, 1); //CAN_SELL_WITHOUT_OPTIONS//this is for now so we dont have to set sku?
	    	 pstmt.setString (4, null); //DISPLAY_TEMPLATE
	    	 pstmt.setInt (5, 1); //IS_FEATURED_PRODUCT
	    	 pstmt.setString (6, mResultSet.getString("description")); //MANUFACTURE
	    	 pstmt.setString (7, url); //URL
	    	 pstmt.setInt (8, categoryId); //DEFAULT_CATEGORY_ID
	    // 	 mySqlPreparedStatement.setInt (9, nextSkuId);
	    	 pstmt.execute();
		     //for now we are inserting default sku without options 
	    	 int nextId = Utils.getNextSequenceNumber("CategoryProductImpl", true);//Utils.getNextCategoryProductId();
			 int nextDisplayId = Utils.getNextSequenceNumber("CategoryProductXrefImpl", true); //getNextOrderCategoryProductId(categoryId);
			 Utils.appendToFile(file, "Inserting into milano_product_migrate ...");
			 query = "INSERT INTO milano_product_migrate (blc_product_id, milano_product_id) VALUES (?,?)";
			 pstmt = mySqlConnection.prepareStatement(query);
			 pstmt.setInt (1, productId);
			 pstmt.setString(2, mResultSet.getString("inventid"));
			 pstmt.execute();

			 Utils.appendToFile(file, "Inserting into blc_category_product_xref ...");
			 query = "INSERT INTO blc_category_product_xref "+
					 "(CATEGORY_PRODUCT_ID, DEFAULT_REFERENCE, DISPLAY_ORDER, CATEGORY_ID, PRODUCT_ID) "+
					 " VALUES (?,?,?,?,?)";
			 pstmt = mySqlConnection.prepareStatement(query);
			 pstmt.setInt (1, nextId); //CATEGORY_PRODUCT_ID
			 pstmt.setInt (2, 1); //DEFAULT_REFERENCE
			 pstmt.setInt (3, nextDisplayId); //DISPLAY_ORDER
			 pstmt.setInt (4, categoryId); //CATEGORY_ID
			 pstmt.setInt (5, productId); //PRODUCT_ID
			 pstmt.execute();
/*			 Utils.appendToFile(file, "UPDATING  blc_sku ...");
			 query = "UPDATE BLC_SKU SET DEFAULT_PRODUCT_ID=? WHERE SKU_ID=?";
				     mySqlPreparedStatement = mySqlConnection.prepareStatement(query);
	     	 mySqlPreparedStatement.setInt (1, nextId);
	     	 mySqlPreparedStatement.setInt (2, 1);*/

			 MigrateGrid.migrateGrid(mResultSet, productId); 
			 //handleProductOptions(productId,mResultSet, mySqlConnection);
			 Utils.appendToFile(file, "Done  Inserting  for Product "+mResultSet.getString(1) + ", " + mResultSet.getString(2));
		}
		
/*		private static void insertProduct(ResultSet mResultSet, int productId, int categoryId) throws SQLException, ClassNotFoundException, IOException{
			 Connection mySqlConnection = Main.getMilanoUser().getMySqlConnection();

			 Utils.appendToFile(file, "Inserting into blc_product ...");
			 String query="INSERT INTO blc_product "+
			 "(PRODUCT_ID, ARCHIVED, CAN_SELL_WITHOUT_OPTIONS, DISPLAY_TEMPLATE, IS_FEATURED_PRODUCT,"+
			 " MANUFACTURE, URL, DEFAULT_CATEGORY_ID)"+//, DEFAULT_SKU_ID) 
			 " VALUES (?,?,?,?,?,?,?,?)" ;
			 String noSpacesDesc = mResultSet.getString("idesc").replaceAll("\\s+","");			 
	    	 String url = "/"+mResultSet.getString("cshort")+"/"+noSpacesDesc;

	    	 PreparedStatement pstmt = mySqlConnection.prepareStatement(query);
	    	 pstmt.setInt (1, productId); //PRODUCT_ID
	    	 pstmt.setString(2, "N"); //ARCHIVED
	    	 pstmt.setInt (3, 1); //CAN_SELL_WITHOUT_OPTIONS//this is for now so we dont have to set sku?
	    	 pstmt.setString (4, null); //DISPLAY_TEMPLATE
	    	 pstmt.setInt (5, 1); //IS_FEATURED_PRODUCT
	    	 pstmt.setString (6, mResultSet.getString("description")); //MANUFACTURE
	    	 pstmt.setString (7, url); //URL
	    	 pstmt.setInt (8, categoryId); //DEFAULT_CATEGORY_ID
	    // 	 mySqlPreparedStatement.setInt (9, nextSkuId);
	    	 pstmt.execute();
		     //for now we are inserting default sku without options 
	    	 int nextId = Utils.getNextSequenceNumber("CategoryProductImpl", true);//Utils.getNextCategoryProductId();
			 int nextDisplayId = Utils.getNextSequenceNumber("CategoryProductXrefImpl", true); //getNextOrderCategoryProductId(categoryId);
			 Utils.appendToFile(file, "Inserting into milano_product_migrate ...");
			 query = "INSERT INTO milano_product_migrate (blc_product_id, milano_product_id) VALUES (?,?)";
			 pstmt = mySqlConnection.prepareStatement(query);
			 pstmt.setInt (1, productId);
			 pstmt.setString(2, mResultSet.getString("inventid"));
			 pstmt.execute();

			 Utils.appendToFile(file, "Inserting into blc_category_product_xref ...");
			 query = "INSERT INTO blc_category_product_xref "+
					 "(CATEGORY_PRODUCT_ID, DEFAULT_REFERENCE, DISPLAY_ORDER, CATEGORY_ID, PRODUCT_ID) "+
					 " VALUES (?,?,?,?,?)";
			 pstmt = mySqlConnection.prepareStatement(query);
			 pstmt.setInt (1, nextId); //CATEGORY_PRODUCT_ID
			 pstmt.setInt (2, 1); //DEFAULT_REFERENCE
			 pstmt.setInt (3, nextDisplayId); //DISPLAY_ORDER
			 pstmt.setInt (4, categoryId); //CATEGORY_ID
			 pstmt.setInt (5, productId); //PRODUCT_ID
			 pstmt.execute();
/*			 Utils.appendToFile(file, "UPDATING  blc_sku ...");
			 query = "UPDATE BLC_SKU SET DEFAULT_PRODUCT_ID=? WHERE SKU_ID=?";
				     mySqlPreparedStatement = mySqlConnection.prepareStatement(query);
	     	 mySqlPreparedStatement.setInt (1, nextId);
	     	 mySqlPreparedStatement.setInt (2, 1);*/
	//		 MigrateGrid.migrateGrid(mResultSet, productId); 
			 //handleProductOptions(productId,mResultSet, mySqlConnection);
		//	 Utils.appendToFile(file, "Done  Inserting  for Product "+mResultSet.getString(1) + ", " + mResultSet.getString(2));
		//}
		
		
		private static void updateProduct(ResultSet mResultSet, int productId, int categoryId) throws SQLException, ClassNotFoundException, IOException{
			 Connection mySqlConnection = Main.getMilanoUser().getMySqlConnection();

			 Utils.appendToFile(file, "Updating blc_product ...");
			 String query="Update blc_product set ARCHIVED=?, CAN_SELL_WITHOUT_OPTIONS=?, "+
			 "DISPLAY_TEMPLATE=?, IS_FEATURED_PRODUCT=?,"+
			 " MANUFACTURE=?, URL=?, DEFAULT_CATEGORY_ID=? where PRODUCT_ID=?" ;
			 String noSpacesDesc = mResultSet.getString("idesc").replaceAll("\\s+","");			 
	    	 String url = "/"+mResultSet.getString("cshort")+"/"+noSpacesDesc;

	    	 PreparedStatement pstmt = mySqlConnection.prepareStatement(query);
	    	 pstmt.setString(1, "N"); //ARCHIVED
	    	 pstmt.setInt (2, 1); //CAN_SELL_WITHOUT_OPTIONS//this is for now so we dont have to set sku?
	    	 pstmt.setString (3, null); //DISPLAY_TEMPLATE
	    	 pstmt.setInt (4, 1); //IS_FEATURED_PRODUCT
	    	 pstmt.setString (5, mResultSet.getString("description")); //MANUFACTURE
	    	 pstmt.setString (6, url); //URL
	    	 pstmt.setInt (7, categoryId); //DEFAULT_CATEGORY_ID
	    	 pstmt.setInt (8, productId); //PRODUCT_ID
	    // 	 mySqlPreparedStatement.setInt (9, nextSkuId);
	    	 pstmt.execute();
		     //for now we are inserting default sku without options 

/*			 Utils.appendToFile(file, "Inserting into blc_category_product_xref ...");
			 query = "INSERT INTO blc_category_product_xref "+
					 "(CATEGORY_PRODUCT_ID, DEFAULT_REFERENCE, DISPLAY_ORDER, CATEGORY_ID, PRODUCT_ID) "+
					 " VALUES (?,?,?,?,?)";
			 pstmt = mySqlConnection.prepareStatement(query);
			 pstmt.setInt (1, nextId); //CATEGORY_PRODUCT_ID
			 pstmt.setInt (2, 1); //DEFAULT_REFERENCE
			 pstmt.setInt (3, nextDisplayId); //DISPLAY_ORDER
			 pstmt.setInt (4, categoryId); //CATEGORY_ID
			 pstmt.setInt (5, productId); //PRODUCT_ID
			 pstmt.execute();
/*			 Utils.appendToFile(file, "UPDATING  blc_sku ...");
			 query = "UPDATE BLC_SKU SET DEFAULT_PRODUCT_ID=? WHERE SKU_ID=?";
				     mySqlPreparedStatement = mySqlConnection.prepareStatement(query);
	     	 mySqlPreparedStatement.setInt (1, nextId);
	     	 mySqlPreparedStatement.setInt (2, 1);*/
			 MigrateGrid.migrateGrid(mResultSet, productId); 
			 //handleProductOptions(productId,mResultSet, mySqlConnection);
			 Utils.appendToFile(file, "Done  Inserting  for Product "+mResultSet.getString(1) + ", " + mResultSet.getString(2));
		}
		
		
		
		
		
		private static void migrateColorOptions(){
			 Connection msSqlConnection = Main.getMilanoUser().getMsSqlConnection();
			 Connection mySqlConnection = Main.getMilanoUser().getMySqlConnection();
			 String query = "";
		}
		
		private static void getProductWithAllColours(String[] r, String productName, String storeId, int categoryId) throws SQLException{
//			 String[] r = Utils.doesContainColor(name);
			 ResultSet rs = Utils.getAllWithName(productName, storeId, categoryId);
			 ArrayList<String> coloursList = new ArrayList<String>();
			 boolean optionWasInserted = false;
			 String color = null;
			 String colorCode = null;
			 while (rs.next()){
				 if (optionWasInserted){
					 optionWasInserted = true;
					 color = rs.getString("description");
					 colorCode = rs.getString("tabcod");
					 String finalColor = colorCode.equals(Constants.ASSSORTED_COLOR_CODE)? r[0]: color;  
					 coloursList.add(finalColor);
				     String insertQ =	"INSERT INTO blc_product_option "+
		    		 			"(PRODUCT_OPTION_ID, ATTRIBUTE_NAME, DISPLAY_ORDER, LABEL, REQUIRED, OPTION_TYPE, "+
		    		 			" USE_IN_SKU_GENERATION, VALIDATION_STRATEGY_TYPE) "+
		    		 			" VALUES (?,?,?,?,?,?,?,?)";
				     PreparedStatement preparedStmt = mySqlConnection.prepareStatement(insertQ);
				     //OPTION_TYPE COLOR, SIZE, TEXT
			 		 int prodOptionId = Utils.getNextSequenceNumber("ProductOptionImpl", true);
				     String[] r1 = Utils.TryToGuessOption(productName);
		   	     	 preparedStmt.setInt(1, prodOptionId); //PRODUCT_OPTION_ID
		   	     	 preparedStmt.setString (2, r1[0]); //ATTRIBUTE_NAME
		   	     	 preparedStmt.setInt (3, 1); //DISPLAY_ORDER
		   	     	 preparedStmt.setString (4, r1[0]); //LABEL
		   	     	 preparedStmt.setInt (5, 0); //REQUIRED
		   	     	 preparedStmt.setString (6, r1[1]); //OPTION_TYPE
		   	     	 preparedStmt.setInt (7, 0); //USE_IN_SKU_GENERATION
		   	     	 preparedStmt.setString (8, "ADD_ITEM"); //VALIDATION_STRATEGY_TYPE
		   	     	 preparedStmt.execute();
		   	     	 preparedStmt.close();
					 //get name without the color
					 //if icolor in invent = '033' , that means that color is in the description, otherwise get from color table 
				 }
		   	     String name = rs.getString("idesc");
		   	     String inventId = rs.getString("inventId");
				 String query =  "select  m.manufactid, m.short, m.tabcod, m.description, i.inventid, i.icate, i.gridid, i.icode, i.idesc, "+
						 		 " c.short as cshort, i.created, i.storeId, i.iprce, i.icolor, c.tabcod as ctabcod "+
						 		 "from dbo.invent i, dbo.manufact m, dbo.category c "+
						 		 " where i.inventId=?";
				 PreparedStatement preparedStmt = msSqlConnection.prepareStatement(query);
				 preparedStmt.setString (1, inventId);
				 ResultSet set = preparedStmt.executeQuery();
				 String finalColor = colorCode.equals(Constants.ASSSORTED_COLOR_CODE)? r[0]: color;  
				 String finalDesc = r[1];
				 //For every product in Invent, insert product
				 //TODO  finish product insert
			 }

		}
		
		
		private static void migrateTestProductRecord(ResultSet mResultSet,int productId, int categoryId, String storeId) throws SQLException, ClassNotFoundException, IOException{
			 String desc = mResultSet.getString("idesc");
			 String inventId = mResultSet.getString("inventId");
			 Utils.appendToFile(file, "Checking for Product with tabcod "+productId + ", " + desc);
			 Utils.appendToFile(file, "Checking if "+desc+" contains color ");
			 String[] res = Utils.doesContainColor(desc);
			 if (res[0] == null){
				 //continue with inserting just 1 product
				 Utils.appendToFile(file, desc+" does not contain color ");
				 int resultProdId = migrateOneProductRecord(inventId, desc,productId, categoryId, mResultSet);
				 insertPicture(mResultSet, resultProdId, categoryId);
			 } else {
				 Utils.appendToFile(file, desc+" contains color not inserting for now ...");
				 //TODO finish the code
			 }
		}
		
		private static void migrateProductRecord(ResultSet mResultSet,int productId, int categoryId, String storeId) throws SQLException, ClassNotFoundException, IOException{
			 String desc = mResultSet.getString("idesc");
			 String inventId = mResultSet.getString("inventId");
			 int resultProdId = migrateOneProductRecord(inventId, desc,productId, categoryId, mResultSet);
			 insertPicture(mResultSet, resultProdId, categoryId);

			 /*NOT SURE I need to do this
			 Utils.appendToFile(file, "Checking for Product with tabcod "+productId + ", " + desc);
			 Utils.appendToFile(file, "Checking if "+desc+" contains color ");
			 String[] res = Utils.doesContainColor(desc);
			 if (res[0] == null){
				 //continue with inserting just 1 product
				 Utils.appendToFile(file, desc+" does not contain color ");
				 int resultProdId = migrateOneProductRecord(inventId, desc,productId, categoryId, mResultSet);
				 insertPicture(mResultSet, resultProdId, categoryId);
			 } else {
				 Utils.appendToFile(file, desc+" contains color not inserting for now ...");
				 //TODO finish the code
			 }*/
		}
			 
		private static int migrateOneProductRecord(String inventId, String prodDesc,int productId, int categoryId, ResultSet mResultSet) throws SQLException, ClassNotFoundException, IOException{
			 Connection mySqlConnection = Main.getMilanoUser().getMySqlConnection();
			 Utils.appendToFile(file, "Checking for Product with tabcod "+inventId + ", " + prodDesc);
			 Utils.appendToFile(file, "Migrating product with inventId="+inventId + ", " + prodDesc);
		     String query = "Select blc_product_id from milano_product_migrate where milano_product_id=?";
    	     PreparedStatement pStmt = mySqlConnection.prepareStatement(query);
		     pStmt.setString (1, inventId);
		     ResultSet resultSet = pStmt.executeQuery();
		     resultSet.next();
		     int resultProdId=0;
		     Boolean recordExists = resultSet.getRow()!=0;
		     if (!recordExists){
				 Utils.appendToFile(file, "Product does not exist in Boradleaf, inserting...");
		    	 insertProduct(mResultSet, productId, categoryId);
		    	 resultProdId = productId;
		     } else {
				 Utils.appendToFile(file, "Product exists in Boradleaf, updating...");
		    	 updateProduct(mResultSet, resultSet.getInt(1), categoryId);	
		    	 resultProdId = resultSet.getInt(1);
		     }
		     return resultProdId;
		}
		
}
