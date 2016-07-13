package com.milano.migrate;

import java.io.IOException;

import com.milano.migrate.util.MilanoUser;
import com.milano.migrate.util.Utils;




public class Main {
	
	public static String storeId = "B37ED152-277F-4273-B177-C0BC6FDC0214";//"40288DB5-3575-4167-B6F8-C1D933EED62C";
	public static String categoryId = "000";//005";//"40288DB5-3575-4167-B6F8-C1D933EED62C";
	public static String productId = "046";//005";//"40288DB5-3575-4167-B6F8-C1D933EED62C";
	private static MilanoUser mUser;
	public static int numberOfProductsPerCategory = 1;
	
	public static MilanoUser getMilanoUser(){
		 return mUser;
	 }
	 
	 
	 public static void main(String[] args) throws Exception {
		 mUser = new MilanoUser();
		 System.out.println(Utils.getMilanoHome());
		 setMilanoUser(mUser);
		 Utils.getConfigFile(mUser);
		 mUser.setMsSqlConnection(Utils.getDbConnection(mUser, "msSql"));
		 mUser.setMySqlConnection(Utils.getDbConnection(mUser, "mySql"));
		 numberOfProductsPerCategory = Utils.getProductsPerCategoryMigrate(mUser);
		 storeId = Utils.getStoreId(mUser);
		 if (args!=null && args.length>=1){
			 if ("RemoveHeat".equals(args[0])){
				 MigrateMain.removeRedundandHeatClinicItems();
			 }else if ("Colours".equals(args[0])){
				 MigrateMain.migrateColours(false);
			 } else if ("Categories".equals(args[0])){
				 MigrateMain.migrateCategories(storeId); 
			 } else if ("Products".equals(args[0])){
				 if (args.length>=2 && "test".equals(args[1]))
					 MigrateMain.migrateProducts(true, storeId, categoryId);
				 else  MigrateMain.migrateProducts(storeId, categoryId);
			 }
		 } else {
			 String fileName = Utils.getFileNameWithExtension(mUser, "merchandise");
			 System.out.println("file name="+fileName);
		 }
	  }
	 
	 
	 
	 
	 
	 
	 private static void setMilanoUser(MilanoUser user){
		 try {
			 user.setConfigFile(Utils.getConfigFile(user));
			 user.setMsSql(Utils.getDbInforFromConfigFile(user,"MSSQL"));
			 user.setMySql(Utils.getDbInforFromConfigFile(user, "MYSQL"));
			 user.setMilanoPicsFolder(Utils.getPicFolder(user, "milano"));
			 user.setBroadleafPicsFolder(Utils.getPicFolder(user, "broadleaf"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
}
