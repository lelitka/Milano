package com.milano.migrate;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import com.milano.migrate.util.Utils;

//TODO - write in the log file
public class MigrateGrid {
	
		private final static String PRODUCT_MIGRATION_LOG_FILE = "ProductMigration"; 
		private static Path file = null;
		
		
		public static void migrateGrid(ResultSet rsGridSet, int productId) throws SQLException, IOException, ClassNotFoundException{
			 Connection msSqlConnection = Main.getMilanoUser().getMsSqlConnection();
			 Connection mySqlConnection = Main.getMilanoUser().getMySqlConnection();

			//TODO
			// ***********************MSSQL***************************************
			// 
			// get grid and griddet record for gridId
			//Invent - description, color, price
			//		select  i.* --m.manufactid, m.short, m.tabcod, m.description,  i.inventid, i.icate, i.gridid, i.icode, i.idesc 
			/*		from INDEPENDANTDEMO.dbo.invent i, INDEPENDANTDEMO.dbo.manufact m
			/*		where i.STOREID='40288DB5-3575-4167-B6F8-C1D933EED62C' and
			/*		i.iCATE='048' and i.MANUFACTID=m.MANUFACTID and i.ISACTIVE='1'
			/*		and i.IDESC='WAFFLE STOMPER PARAMOUNT'*/
//			 String storeId="40288DB5-3575-4167-B6F8-C1D933EED62C";
			 String productId_=rsGridSet.getString("inventId"); //"B248333301";
			 file = Utils.getFile(PRODUCT_MIGRATION_LOG_FILE, true);
			 //We are assuming that category is already migrated
			 // get gridId  from invent for given productId_
		     String query = "select  m.manufactid, m.short, m.tabcod, m.description,  i.inventid, i.icate, i.gridid, i.icode, i.idesc, i.storeId, "+
		    		" i.ISACTIVE "+
					" from dbo.invent i, dbo.manufact m "+
					" where i.STOREID=? and "+
					" i.MANUFACTID=m.MANUFACTID and "+
					" i.icode=?";
//		     		" and i.iCATE=? and i.ISACTIVE='1' "+

		     PreparedStatement preparedStmt = msSqlConnection.prepareStatement(query);
   	     	 preparedStmt.setString (1, rsGridSet.getString("storeId"));
   	     	 preparedStmt.setString (2, rsGridSet.getString("icode"));
//		     preparedStmt1.setString (3, categoryId_);
		     ResultSet resultSet = preparedStmt.executeQuery();
		     resultSet.next();
			 query = "SELECT QTYONHAND from dbo.InvGrid where inventid=?"+//'4C025879-F77D-49AC-8611-002EE7D48706' 
				 		" and gridtype='I'";
			 PreparedStatement stmt = msSqlConnection.prepareStatement(query);
			 stmt.setString (1, productId_);
			 ResultSet rs = stmt.executeQuery();
			 Boolean recordExists = rs.getRow()!=0;
			 if (recordExists){
	 		     query = "Delete from blc_sku where DEFAULT_PRODUCT_ID=?";
		    	 PreparedStatement mySqlPreparedStatement = mySqlConnection.prepareStatement(query);
				 mySqlPreparedStatement.setInt (1, productId); //DEFAULT_PRODUCT_ID
		     	 mySqlPreparedStatement.execute();
				 // first delete all records, that way its cleaner when record exists
		    	 java.util.Date date= new java.util.Date();
		    	 Timestamp created = new Timestamp(date.getTime());
	 		     query = "INSERT INTO blc_sku (SKU_ID, ACTIVE_END_DATE, ACTIVE_START_DATE, AVAILABLE_FLAG, DESCRIPTION, "+
							 " DISCOUNTABLE_FLAG, DISPLAY_TEMPLATE, EXTERNAL_ID, FULFILLMENT_TYPE,  INVENTORY_TYPE, "+
				    		 " IS_MACHINE_SORTABLE, LONG_DESCRIPTION, NAME, QUANTITY_AVAILABLE,  RETAIL_PRICE, "+ 
							 " SALE_PRICE, TAX_CODE, TAXABLE_FLAG, UPC, URL_KEY"+//, DEFAULT_PRODUCT_ID, ADDL_PRODUCT_ID "+
							//	 " WEIGHT, WEIGHT_UNIT_OF_MEASURE, CURRENCY_CODE,  CONTAINER_SHAPE, DEPTH, "+
						    // " DIMENSION_UNIT_OF_MEASURE, GIRTH, HEIGHT, CONTAINER_SIZE, WIDTH,"+ 
							") VALUES (?,?,?,?,?,  ?,?,?,?,?,  ?,?,?,?,?,  ?,?,?,?,?"+//,   ?,?"+
							//",?,?,?,?,?,  ?,?,?,?,?)"+
							")";
		    	 mySqlPreparedStatement = mySqlConnection.prepareStatement(query);
	    		 int skuId = Utils.getNextSequenceNumber("SkuImpl", true); 
	    		 int qty = rs.getInt("QTYONHAND");
		     	 mySqlPreparedStatement.setInt (1, skuId); 
		     	 mySqlPreparedStatement.setString(2, null); //active_end_date
		     	 mySqlPreparedStatement.setTimestamp(3,created); //ACTIVE_START_DATE
		     	 mySqlPreparedStatement.setInt (4, 1); //this is for now so we dont have to set sku? //AVAILABLE_FLAG
		     	 mySqlPreparedStatement.setString (5,rsGridSet.getString("IDESC")); //DESCRIPTION
			     mySqlPreparedStatement.setString (6,"Y"); //DISCOUNTABLE_FLAG
				 mySqlPreparedStatement.setString (7, null); //DISPLAY_TEMPLATE
				 mySqlPreparedStatement.setString (8, null); //EXTERNAL_ID
				 mySqlPreparedStatement.setString (9,null ); //FULFILLMENT_TYPE
				 mySqlPreparedStatement.setString (10, "ALWAYS_AVAILABLE"); //INVENTORY_TYPE
				 mySqlPreparedStatement.setInt (11, 1); //IS_MACHINE_SORTABLE
				 mySqlPreparedStatement.setString (12, rsGridSet.getString("IDESC")); //LONG_DESCRIPTION
				 mySqlPreparedStatement.setString (13, rsGridSet.getString("IDESC")); //NAME
				 mySqlPreparedStatement.setInt (14, qty); //QUANTITY_AVAILABLE
				 mySqlPreparedStatement.setDouble(15, rsGridSet.getFloat("IPRCE")); //RETAIL_PRICE
				 mySqlPreparedStatement.setDouble (16, rsGridSet.getFloat("IPRCE")); //SALE_PRICE
				 mySqlPreparedStatement.setString (17, null); //TAX_CODE
				 mySqlPreparedStatement.setString (18, null); //TAXABLE_FLAG
				 mySqlPreparedStatement.setString (19, null); //UPC
				 // mySqlPreparedStatement.setString (20, null); //URL_KEY
				 mySqlPreparedStatement.setInt (20, productId); //DEFAULT_PRODUCT_ID
				 //mySqlPreparedStatement.setInt (22, productId); //ADDL_PRODUCT_ID
				 mySqlPreparedStatement.execute();
			     query="Update blc_product set default_sku_id=? where product_id=?";
		    	 mySqlPreparedStatement = mySqlConnection.prepareStatement(query);
		     	 mySqlPreparedStatement.setInt (1, skuId); 
		     	 mySqlPreparedStatement.setInt (2, productId);
		     	 mySqlPreparedStatement.execute();


			 } else {
				 String gridId = resultSet.getString("gridid");
				 
					// get grid and griddet record for gridId
				 query = "select gd.griddetid, dimindex, dimno, gd.description, g.DESCRIPTION as GridDescription from "+
						 " dbo.grid g, dbo.GridDet gd where g.GRIDID=?"+//'64D7ACE8-E072-4C48-8B14-B2752D30E1C4' 
						 " and g.GRIDID=gd.GRIDID and g.storeId=? "+
						 //+ "--and GRIDDETID='4FC9CCD9-95A9-4B5E-9111-140DA0EAA02A'
						 " order by gd.dimindex, gd.dimno asc";
			     PreparedStatement preparedStmt1 = msSqlConnection.prepareStatement(query);
	   	     	 preparedStmt1.setString (1, gridId);
	   	     	 preparedStmt1.setString (2, rsGridSet.getString("storeId"));
			     ResultSet resultSet1 = preparedStmt1.executeQuery();
			     handleProductOptions(productId,rsGridSet.getString("INVENTID"),rsGridSet.getFloat("IPRCE"),rsGridSet.getString("IDESC"),rsGridSet.getString("ICOLOR"), rsGridSet.getString("CDESCRIPTION"),resultSet1);
			     //TODO - int sqProduct = Utils.getNextSequenceNumber(mySqlConnection, "ProductImpl");
			 }
			     

			//select * from INDEPENDANTDEMO.dbo.InvGrid  where INVENTID='FB7011B9-065F-447C-A043-041A7B716D48'
			//and GRIDDET1ID='4FC9CCD9-95A9-4B5E-9111-140DA0EAA02A'
			//and griddet2id='15603378-ECC3-4776-904F-1ECA43A05937'
			// get  QTYONHAND for given 
			
			
			//********************************MYSQL********************
			/*
			 * SELECT * FROM broadleaf.blc_product_option_xref where product_id=10002
			 * SELECT * FROM broadleaf.blc_product_option where PRODUCT_OPTION_ID in (1000,1001)
			 * SELECT * FROM broadleaf.blc_product_option_value where PRODUCT_OPTION_ID=1000
			 * SELECT * FROM broadleaf.blc_sku_option_value_xref where PRODUCT_OPTION_VALUE_ID=1007;
			 * SELECT * FROM broadleaf.blc_sku where product_id = 10002 --sku_id=10015 
			 */
		
		}
			 
			 
		public static void handleProductOptions(int prodId_,String productId_, float iPrice, String productDescription, String iColor, String cDescription, ResultSet resultSet) throws SQLException, IOException, ClassNotFoundException{
				 Utils.appendToFile(file, "Migrating product options for product "+prodId_+" and inventId="+productId_);
				 Connection mySqlConnection = Main.getMilanoUser().getMySqlConnection();
				 String query="select PRODUCT_OPTION_ID from blc_product_option_xref where PRODUCT_ID=?";
				 PreparedStatement preparedStmt = mySqlConnection.prepareStatement(query);
				 preparedStmt.setInt(1, prodId_);
				 ResultSet rs = preparedStmt.executeQuery();
			 	 query="Delete from  blc_product_option_xref where PRODUCT_ID=?";
				 preparedStmt = mySqlConnection.prepareStatement(query);
				 preparedStmt.setInt(1, prodId_);
				 preparedStmt.execute();
			 	 query="Delete from blc_product_option_value where PRODUCT_OPTION_ID in "+
				 			"(select PRODUCT_OPTION_ID from blc_product_option_xref where PRODUCT_ID=?)";
				     preparedStmt = mySqlConnection.prepareStatement(query);
				     preparedStmt.setInt(1, prodId_);
				     preparedStmt.execute();
			     while (rs.next()) {
			    	 query = "delete from blc_sku_option_value_xref where product_option_value_id in "+
				    			 " (select product_option_value_id from blc_product_option_value where PRODUCT_OPTION_ID=?)";
				 	 preparedStmt = mySqlConnection.prepareStatement(query);
				 	 preparedStmt.setInt(1, rs.getInt(1));
				 	 preparedStmt.execute();
				 	 query="Delete from blc_product_option_value where PRODUCT_OPTION_ID =?";
				 	 preparedStmt = mySqlConnection.prepareStatement(query);
				 	 preparedStmt.setInt(1, rs.getInt(1));
				 	 preparedStmt.execute();
				 	 query="Delete from blc_product_option where PRODUCT_OPTION_ID =?";
				 	 		//+ "in "+
				 			//"(select PRODUCT_OPTION_ID from blc_product_option_xref where PRODUCT_ID=?)";
				     preparedStmt = mySqlConnection.prepareStatement(query);
				     preparedStmt.setInt(1, rs.getInt(1));
				     preparedStmt.execute();
			     }
				 //TODO check with Paul what price to take select * from INDEPENDANTDEMO.dbo.InvCost where inventid='CDC754D7-0191-4488-9151-3C523AC0474A'
				 //TODO color
				 //TODO migrate Color
				 Utils.appendToFile(file, "Migrating product color for product "+prodId_+" and inventId="+productId_+" and icolor="+iColor);
/*				 query = "select product_option_value_id from milano_color where milano_tabcod=?";
				 preparedStmt = mySqlConnection.prepareStatement(query);
				 preparedStmt.setString(1, resultSet.getString("icolor"));
				 int colorId = 0;
				 ResultSet rs1 = preparedStmt.executeQuery();
			     if (rs1.getRow()!=0){
			    	 colorId = rs1.getInt(1);
		 	     } else {
		 	    	 //could not find color
					 Utils.appendToFile(file, "Could not find product_option_value_id in milano_color for color ="+resultSet.getString("icolor"));
		 	     }
			     preparedStmt.close();*/
		 		 int prodOptionId = Utils.getNextSequenceNumber("ProductOptionImpl", true);
			     String insertQ =	"INSERT INTO blc_product_option "+
	    		 			"(PRODUCT_OPTION_ID, ATTRIBUTE_NAME, DISPLAY_ORDER, LABEL, REQUIRED, OPTION_TYPE, USE_IN_SKU_GENERATION, VALIDATION_STRATEGY_TYPE) "+
	    		 			" VALUES (?,?,?,?,?,?,?,?)";
			     preparedStmt = mySqlConnection.prepareStatement(insertQ);
	   	     	 preparedStmt.setInt(1, prodOptionId); //PRODUCT_OPTION_ID
	   	     	 preparedStmt.setString (2, "COLOR"); //ATTRIBUTE_NAME
	   	     	 preparedStmt.setInt (3, 1); //DISPLAY_ORDER
	   	     	 preparedStmt.setString (4, "Color"); //LABEL
	   	     	 preparedStmt.setInt (5, 1); //REQUIRED
	   	     	 preparedStmt.setString (6, "COLOR"); //OPTION_TYPE
	   	     	 preparedStmt.setInt (7, 0); //USE_IN_SKU_GENERATION
	   	     	 preparedStmt.setString (8, "ADD_ITEM"); //VALIDATION_STRATEGY_TYPE
	   	     	 preparedStmt.execute();
	   	     	 preparedStmt.close();
		    	 int prodOptionValueId = Utils.getNextSequenceNumber("ProductOptionValueImpl", true);
	   	     	 insertQ = "INSERT INTO blc_product_option_value "+
	   	    		 	"(PRODUCT_OPTION_VALUE_ID, ATTRIBUTE_VALUE,"+ //DISPLAY_ORDER,
	   	     			 " PRODUCT_OPTION_ID) "+
	   	    		 	" VALUES (?,?,?) ";
	   	     	 preparedStmt = mySqlConnection.prepareStatement(insertQ);
	   	     	 preparedStmt.setInt(1, prodOptionValueId);
	   	     	 preparedStmt.setString(2, cDescription);
	   	     	 //preparedStmt.setInt(3, 1);
	   	     	 preparedStmt.setInt(3, prodOptionId);
	   	     	 preparedStmt.execute();
	   	     	 preparedStmt.close();

	   	     	 int prodOptionXrefId = Utils.getNextSequenceNumber("ProductOptionXrefImpl", true);
	   	     	 insertQ = 	"INSERT INTO blc_product_option_xref "+
	   	     			 	"(PRODUCT_OPTION_XREF_ID, PRODUCT_ID, PRODUCT_OPTION_ID) "+
	   	     			 	" VALUES (?,?,?)";
	   	     	 preparedStmt = mySqlConnection.prepareStatement(insertQ);
	   	     	 preparedStmt.setInt(1, prodOptionXrefId);
	   	     	 preparedStmt.setInt(2, prodId_);
	   	     	 preparedStmt.setInt(3, prodOptionId);
	   	     	 preparedStmt.execute();
	   	     	 preparedStmt.close();
			     
	   	     	
	   	     	
	   	     	// Finding if there is any record in InvGrid, if there is, that means that Grid is irrelevant and options are not needed
			     ArrayList<String[]> option1 = new ArrayList<String[]>();
			     ArrayList<String[]> option2 = new ArrayList<String[]>();
			     ArrayList<Integer> optionId1 = new ArrayList<Integer>();
			     ArrayList<Integer> optionId2 = new ArrayList<Integer>();
 			 	 prodOptionId = 0;
		    	 String gridDescription = null;
		    	 int prodOptionId1=0;
		    	 int prodOptionId2=0;
			     while (resultSet.next()) {
			    	 	gridDescription = resultSet.getString("GridDescription");//NO GRID
			    	 	if ("NO GRID".equalsIgnoreCase(gridDescription)){
			    	 		Utils.appendToFile(file, "Product "+prodId_+" does not have options");
			    	 	}
			    	 	if (!("NO GRID".equalsIgnoreCase(gridDescription))){
					    	 	int dimindex = resultSet.getInt("dimindex");
					    	 	int dimno = resultSet.getInt("dimno");
							 	String desc = resultSet.getString("description");
							 	String gridDetId = resultSet.getString("griddetid");
						 		String[] o = {desc, gridDetId}; 
						 		Utils.appendToFile(file,"****************dimindex"+dimindex+" dimno="+dimno+" desc="+desc);
		
							 	if (dimno==1){
							 		option1.add(o);
							 	} else {
							 		option2.add(o);
							 	}
							 	if (dimindex==0){
							 		Utils.appendToFile(file,"dimindex=0 dimno="+dimno+" desc="+desc);
							 		//TODO resolve other options
								     insertQ =	"INSERT INTO blc_product_option "+
						    		 			"(PRODUCT_OPTION_ID, ATTRIBUTE_NAME, DISPLAY_ORDER, LABEL, REQUIRED, OPTION_TYPE, USE_IN_SKU_GENERATION, VALIDATION_STRATEGY_TYPE) "+
						    		 			" VALUES (?,?,?,?,?,?,?,?)";
								     preparedStmt = mySqlConnection.prepareStatement(insertQ);
								     //OPTION_TYPE COLOR, SIZE, TEXT
							 		 prodOptionId = Utils.getNextSequenceNumber("ProductOptionImpl", true);
								     String[] r = Utils.TryToGuessOption(desc);
						   	     	 preparedStmt.setInt(1, prodOptionId); //PRODUCT_OPTION_ID
						   	     	 preparedStmt.setString (2, r[0]); //ATTRIBUTE_NAME
						   	     	 preparedStmt.setInt (3, dimno); //DISPLAY_ORDER
						   	     	 preparedStmt.setString (4, r[0]); //LABEL
						   	     	 preparedStmt.setInt (5, 1); //REQUIRED
						   	     	 preparedStmt.setString (6, r[1]); //OPTION_TYPE
						   	     	 preparedStmt.setInt (7, 0); //USE_IN_SKU_GENERATION
						   	     	 preparedStmt.setString (8, "ADD_ITEM"); //VALIDATION_STRATEGY_TYPE
						   	     	 preparedStmt.execute();
						   	     	 preparedStmt.close();
							 		 if (dimno==1){
							 			prodOptionId1 = prodOptionId;
							 		 } else prodOptionId2=prodOptionId;
						   		     prodOptionXrefId = Utils.getNextSequenceNumber("ProductOptionXrefImpl", true);
						   	     	 insertQ = 	"INSERT INTO blc_product_option_xref "+
						   	     			 	"(PRODUCT_OPTION_XREF_ID, PRODUCT_ID, PRODUCT_OPTION_ID) "+
						   	     			 	" VALUES (?,?,?)";
						   	     	 preparedStmt = mySqlConnection.prepareStatement(insertQ);
						   	     	 preparedStmt.setInt(1, prodOptionXrefId);
						   	     	 preparedStmt.setInt(2, prodId_);
						   	     	 preparedStmt.setInt(3, prodOptionId);
						   	     	 preparedStmt.execute();
						   	     	 preparedStmt.close();
							 	} 	     	
				
						   	     prodOptionValueId = Utils.getNextSequenceNumber("ProductOptionValueImpl", true);
						   	     insertQ = "INSERT INTO blc_product_option_value "+
						   	    		 	"(PRODUCT_OPTION_VALUE_ID, ATTRIBUTE_VALUE, DISPLAY_ORDER,  PRODUCT_OPTION_ID) "+
						   	    		 	" VALUES (?,?,?,?) ";
					   	     	 preparedStmt = mySqlConnection.prepareStatement(insertQ);
					   	     	 preparedStmt.setInt(1, prodOptionValueId);
					   	     	 preparedStmt.setString(2, desc);
					   	     	 preparedStmt.setInt(3, dimno);
					   	     	 preparedStmt.setInt(4, dimno==1? prodOptionId1: prodOptionId2);
					   	     	 preparedStmt.execute();
					   	     	 preparedStmt.close();
								 	if (dimno==1){
								 		optionId1.add(prodOptionValueId);
								 	} else {
								 		optionId2.add(prodOptionValueId);
								 	}
			    	 	}
			     }
		    	 java.util.Date date= new java.util.Date();
		    	 Timestamp created = new Timestamp(date.getTime());
		    	 Utils.appendToFile(file,"Creating bls_skus");
		    	 //TODO create DEFAULT SKU
			     query="Update blc_product set default_sku_id=null where product_id=?";
		 		 PreparedStatement mySqlPreparedStatement = mySqlConnection.prepareStatement(query);
		     	 mySqlPreparedStatement.setInt (1, prodId_); //DEFAULT_PRODUCT_ID
			     mySqlPreparedStatement.execute();
		    	 query = "select sku_id from blc_sku where URL_KEY=? or DEFAULT_PRODUCT_ID=?";
		 		 mySqlPreparedStatement = mySqlConnection.prepareStatement(query);
		     	 mySqlPreparedStatement.setString (1, prodId_+""); //URL_KEY
		     	 mySqlPreparedStatement.setInt (2, prodId_); //DEFAULT_PRODUCT_ID
			     ResultSet rs1 = mySqlPreparedStatement.executeQuery();
			     while (rs1.next()) {
				 	 query="Delete from blc_sku_media_map where BLC_SKU_SKU_ID =?";
				 	 		//+ "in "+
				 			//"(select PRODUCT_OPTION_ID from blc_product_option_xref where PRODUCT_ID=?)";
				     preparedStmt = mySqlConnection.prepareStatement(query);
				     preparedStmt.setInt(1, rs1.getInt(1));
				     preparedStmt.execute();
			     }
		    	 query = "Delete from blc_sku where URL_KEY=? or DEFAULT_PRODUCT_ID=?";
		 		 mySqlPreparedStatement = mySqlConnection.prepareStatement(query);
		     	 mySqlPreparedStatement.setString (1, prodId_+""); //URL_KEY
		     	 mySqlPreparedStatement.setInt (2, prodId_); //DEFAULT_PRODUCT_ID
			     mySqlPreparedStatement.execute();
		    	 int i=0;
		    	 System.out.println("optionId1 size="+optionId1.size()+" optionId2 size="+optionId2.size());
		    	 if (optionId1.size()==0 && optionId2.size()==0){
		    		 //No options
		    		 int skuId =Utils.getNextSequenceNumber("SkuImpl", true); 
		    		 int qty = 0;//Utils.getQty(productId_, o1[1], o2[1]);
			 		 query = "INSERT INTO blc_sku (SKU_ID, ACTIVE_END_DATE, ACTIVE_START_DATE, AVAILABLE_FLAG, DESCRIPTION, "+
							 " DISCOUNTABLE_FLAG, DISPLAY_TEMPLATE, EXTERNAL_ID, FULFILLMENT_TYPE,  INVENTORY_TYPE, "+
				    		 " IS_MACHINE_SORTABLE, LONG_DESCRIPTION, NAME, QUANTITY_AVAILABLE,  RETAIL_PRICE, "+ 
							 " SALE_PRICE, TAX_CODE, TAXABLE_FLAG, UPC, URL_KEY, DEFAULT_PRODUCT_ID, ADDL_PRODUCT_ID "+
							//	 " WEIGHT, WEIGHT_UNIT_OF_MEASURE, CURRENCY_CODE,  CONTAINER_SHAPE, DEPTH, "+
						    // " DIMENSION_UNIT_OF_MEASURE, GIRTH, HEIGHT, CONTAINER_SIZE, WIDTH,"+ 
							") VALUES (?,?,?,?,?,  ?,?,?,?,?,  ?,?,?,?,?,  ?,?,?,?,?,?,?"+//,   ?,?"+
							//",?,?,?,?,?,  ?,?,?,?,?)"+
							")";
			 		 mySqlPreparedStatement = mySqlConnection.prepareStatement(query);
			 		 mySqlPreparedStatement.setInt (1, skuId); 
			 		 mySqlPreparedStatement.setTimestamp(2, null); //active_end_date
			 		 mySqlPreparedStatement.setTimestamp(3,created); //ACTIVE_START_DATE
			 		 mySqlPreparedStatement.setInt (4, 1); //this is for now so we dont have to set sku? //AVAILABLE_FLAG
			 		 mySqlPreparedStatement.setString (5,productDescription); //DESCRIPTION
			 		 mySqlPreparedStatement.setString (6,"Y"); //DISCOUNTABLE_FLAG
			 		 mySqlPreparedStatement.setString (7, null); //DISPLAY_TEMPLATE
			 		 mySqlPreparedStatement.setString (8, null); //EXTERNAL_ID
			 		 mySqlPreparedStatement.setString (9,null ); //FULFILLMENT_TYPE
			 		 mySqlPreparedStatement.setString (10, "ALWAYS_AVAILABLE"); //INVENTORY_TYPE
			 		 mySqlPreparedStatement.setInt (11, 1); //IS_MACHINE_SORTABLE
			 		 mySqlPreparedStatement.setString (12, productDescription); //LONG_DESCRIPTION
			 		 mySqlPreparedStatement.setString (13, productDescription); //NAME
			 		 mySqlPreparedStatement.setInt (14, qty); //QUANTITY_AVAILABLE
			 		 mySqlPreparedStatement.setDouble(15, iPrice); //RETAIL_PRICE
			 		 mySqlPreparedStatement.setDouble (16, iPrice); //SALE_PRICE
			 		 mySqlPreparedStatement.setString (17, null); //TAX_CODE
			 		 mySqlPreparedStatement.setString (18, null); //TAXABLE_FLAG
			 		 mySqlPreparedStatement.setString (19, null); //UPC
			 		 mySqlPreparedStatement.setString (20, productDescription); //URL_KEY
			 		 mySqlPreparedStatement.setInt (21, prodId_); //DEFAULT_PRODUCT_ID
			 		 mySqlPreparedStatement.setInt (22, prodId_); //ADDL_PRODUCT_ID
			 		 mySqlPreparedStatement.execute();
			    	 Utils.appendToFile(file, " Updating default sku in product for productid="+prodId_);
				     query="Update blc_product set default_sku_id=? where product_id=?";
			    	 mySqlPreparedStatement = mySqlConnection.prepareStatement(query);
			     	 mySqlPreparedStatement.setInt (1, skuId); 
			     	 mySqlPreparedStatement.setInt (2, prodId_);
			     	 mySqlPreparedStatement.execute();

		    	 } else {
				     for (String[] o1 : option1) {
				    	 i++;
				    	 int j=0;
				    	 for (String[] o2 : option2) {
				    		 j++;
				    		// i++;
				    		 int skuId =Utils.getNextSequenceNumber("SkuImpl", true); 
				    		 int qty = Utils.getQty(productId_, o1[1], o2[1]);
				    		 Utils.appendToFile(file,"Qty for product "+productId_+" and Options: "+o1[1]+" and "+o2[1]+" is: "+qty);
				    		 System.out.println("i="+i+" j="+j);
				    		 Utils.appendToFile(file,"Sku_option_id for product "+productId_+" are:"+optionId1.get(i-1)+" and "+optionId2.get(j-1));
				    		 if (i==1 || qty>0){
						 		 query = "INSERT INTO blc_sku (SKU_ID, ACTIVE_END_DATE, ACTIVE_START_DATE, AVAILABLE_FLAG, DESCRIPTION, "+
											 " DISCOUNTABLE_FLAG, DISPLAY_TEMPLATE, EXTERNAL_ID, FULFILLMENT_TYPE,  INVENTORY_TYPE, "+
								    		 " IS_MACHINE_SORTABLE, LONG_DESCRIPTION, NAME, QUANTITY_AVAILABLE,  RETAIL_PRICE, "+ 
											 " SALE_PRICE, TAX_CODE, TAXABLE_FLAG, UPC, URL_KEY, DEFAULT_PRODUCT_ID, ADDL_PRODUCT_ID "+
											//	 " WEIGHT, WEIGHT_UNIT_OF_MEASURE, CURRENCY_CODE,  CONTAINER_SHAPE, DEPTH, "+
										    // " DIMENSION_UNIT_OF_MEASURE, GIRTH, HEIGHT, CONTAINER_SIZE, WIDTH,"+ 
											") VALUES (?,?,?,?,?,  ?,?,?,?,?,  ?,?,?,?,?,  ?,?,?,?,?,?,?"+//,   ?,?"+
											//",?,?,?,?,?,  ?,?,?,?,?)"+
											")";
				    			 mySqlPreparedStatement = mySqlConnection.prepareStatement(query);
						     	 mySqlPreparedStatement.setInt (1, skuId); 
						     	 mySqlPreparedStatement.setTimestamp(2, null); //active_end_date
						     	 mySqlPreparedStatement.setTimestamp(3,created); //ACTIVE_START_DATE
						     	 mySqlPreparedStatement.setInt (4, 1); //this is for now so we dont have to set sku? //AVAILABLE_FLAG
						     	 mySqlPreparedStatement.setString (5,i==1? productDescription: gridDescription); //DESCRIPTION
						     	 mySqlPreparedStatement.setString (6,"Y"); //DISCOUNTABLE_FLAG
						     	 mySqlPreparedStatement.setString (7, null); //DISPLAY_TEMPLATE
						     	 mySqlPreparedStatement.setString (8, null); //EXTERNAL_ID
						     	 mySqlPreparedStatement.setString (9,null ); //FULFILLMENT_TYPE
						     	 mySqlPreparedStatement.setString (10, "ALWAYS_AVAILABLE"); //INVENTORY_TYPE
						     	 mySqlPreparedStatement.setInt (11, 1); //IS_MACHINE_SORTABLE
						     	 mySqlPreparedStatement.setString (12, i==1? productDescription: gridDescription+" "+o1[0]+" "+o2[0]); //LONG_DESCRIPTION
						     	 mySqlPreparedStatement.setString (13, i==1? productDescription: gridDescription); //NAME
						     	 mySqlPreparedStatement.setInt (14, qty); //QUANTITY_AVAILABLE
						     	 mySqlPreparedStatement.setDouble(15, iPrice); //RETAIL_PRICE
						     	 mySqlPreparedStatement.setDouble (16, iPrice); //SALE_PRICE
						     	 mySqlPreparedStatement.setString (17, null); //TAX_CODE
						     	 mySqlPreparedStatement.setString (18, null); //TAXABLE_FLAG
						     	 mySqlPreparedStatement.setString (19, null); //UPC
						     	 mySqlPreparedStatement.setString (20, o1[0]+"_"+o2[0]); //URL_KEY
						     	 mySqlPreparedStatement.setInt (21, prodId_); //DEFAULT_PRODUCT_ID
						     	 mySqlPreparedStatement.setInt (22, prodId_); //ADDL_PRODUCT_ID
							     mySqlPreparedStatement.execute();
							     
					   	     	 int skuProductOptionValueXrefId = Utils.getNextSequenceNumber("SkuProductOptionValueXrefImpl", true);
						   	     	
					   	     	 insertQ = 	"INSERT INTO blc_sku_option_value_xref "+
					   	     			 	"(SKU_OPTION_VALUE_XREF_ID, PRODUCT_OPTION_VALUE_ID, SKU_ID) "+
					   	     			 	" VALUES (?,?,?)";
					   	     	 preparedStmt = mySqlConnection.prepareStatement(insertQ);
					   	     	 preparedStmt.setInt(1, skuProductOptionValueXrefId);
					   	     	 preparedStmt.setInt(2, optionId1.get(i-1));
					   	     	 preparedStmt.setInt(3, skuId);
					   	     	 preparedStmt.execute();
					   	     	 skuProductOptionValueXrefId = Utils.getNextSequenceNumber("SkuProductOptionValueXrefImpl", true);
					   	     	 preparedStmt.setInt(1, skuProductOptionValueXrefId);
					   	     	 preparedStmt.setInt(2, optionId2.get(j-1));
					   	     	 preparedStmt.setInt(3, skuId);
					   	     	 preparedStmt.execute();
					   	     	 preparedStmt.close();
				    		 }
						     if (i==1){
						    	 Utils.appendToFile(file, " Updating default sku in product for productid="+prodId_);
							     query="Update blc_product set default_sku_id=? where product_id=?";
						    	 mySqlPreparedStatement = mySqlConnection.prepareStatement(query);
						     	 mySqlPreparedStatement.setInt (1, skuId); 
						     	 mySqlPreparedStatement.setInt (2, prodId_);
						     	 mySqlPreparedStatement.execute();
						     }
				    	 }
				     }
				}
			   	     	 /*
*/
		//********************************MYSQL********************
		/*
		 * SELECT * FROM broadleaf.blc_product_option_xref where product_id=10002
		 * SELECT * FROM broadleaf.blc_product_option where PRODUCT_OPTION_ID in (1000,1001)
		 * SELECT * FROM broadleaf.blc_product_option_value where PRODUCT_OPTION_ID=1000
		 * SELECT * FROM broadleaf.blc_sku_option_value_xref where PRODUCT_OPTION_VALUE_ID=1007;
*/
			 }
		
}
