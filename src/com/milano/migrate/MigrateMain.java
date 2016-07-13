package com.milano.migrate;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import com.milano.migrate.util.Utils;

public class MigrateMain {
//	public static String storeId = "B37ED152-277F-4273-B177-C0BC6FDC0214";//"40288DB5-3575-4167-B6F8-C1D933EED62C";
	//public static String categoryId = "005";//"40288DB5-3575-4167-B6F8-C1D933EED62C";
	private static Connection msSqlConnection;
	private static Connection mySqlConnection;

	public static void main(String[] args) throws Exception {
		String[] texts = { "text1", "text2", "text3", "text4", "text5" };
		LocalDate date = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd");
		String text = date.format(formatter);
		Utils.appendToFile("categoryMigration_" + date.format(formatter) + ".log", texts);
	}

	public static void removeRedundandHeatClinicItems() throws SQLException{
		 //Removing heat clinic menu items we don't need
		 try {
			 mySqlConnection = Main.getMilanoUser().getMySqlConnection();
			 mySqlConnection.setAutoCommit(false); // make 
			 String msqQuery = "delete from blc_cms_menu_item where action_url = '/hot-sauces' or action_url = '/clearance'";
		     PreparedStatement mySqlPreparedStatement = mySqlConnection.prepareStatement(msqQuery);
		     mySqlPreparedStatement.execute();
		     msqQuery = "delete from blc_cms_menu_item where label='New to Hot Sauce?'";	
		     mySqlPreparedStatement = mySqlConnection.prepareStatement(msqQuery);
		     mySqlPreparedStatement.execute();
			 mySqlConnection.commit(); 
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
	}

	
	

	
	public static void migrateCategories(String storeId) throws SQLException, IOException, ClassNotFoundException {
		try {
			msSqlConnection = Main.getMilanoUser().getMsSqlConnection();
			mySqlConnection = Main.getMilanoUser().getMySqlConnection();
			MigrateCategory.migrateCategories(storeId);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		} finally {
			msSqlConnection.close();
			mySqlConnection.close();
		}
	}

	public static void migrateColours(boolean closeConnection) throws SQLException, IOException, ClassNotFoundException {
		try {
			msSqlConnection = Main.getMilanoUser().getMsSqlConnection();
			mySqlConnection = Main.getMilanoUser().getMySqlConnection();
			MigrateColourOptions.migrateColourOptions();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		} finally {
			if (closeConnection){
				msSqlConnection.close();
				mySqlConnection.close();
			}
		}
	}
	public static void migrateProducts(String storeId, String categoryId) throws SQLException, IOException, ClassNotFoundException {
		migrateProducts(false, storeId, categoryId);
	}
	
	public static void migrateProducts(boolean testMode, String storeId, String categoryId) throws SQLException, IOException, ClassNotFoundException {
		try {
			msSqlConnection = Main.getMilanoUser().getMsSqlConnection();
			mySqlConnection = Main.getMilanoUser().getMySqlConnection();
			if (!testMode){
				MigrateProduct.migrateProductsForStoreAndCategory(storeId, categoryId, true);
			} else {
				MigrateProduct.migrateTestProductsForStoreAndCategory(storeId, categoryId);
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		} finally {
			msSqlConnection.close();
			msSqlConnection.close();
		}
	}

}
