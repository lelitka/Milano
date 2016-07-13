package com.milano.migrate.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import org.apache.commons.lang.math.NumberUtils;
import org.ini4j.Ini;
import org.jasypt.contrib.org.apache.commons.codec_1_3.binary.Base64;

import com.milano.migrate.DatabaseConnect;
import com.milano.migrate.Main;

public class Utils {
	
	
	public static Ini getConfigFile(MilanoUser user){
		if (user.getConfigFile()==null){
			try {
				String mH = System.getenv(Constants.MILANO_HOME);
				String milanoHome = mH==null? "C:/Milano": mH;
				user.setConfigFile(new Ini(new File(milanoHome+"/"+Constants.CONFIG_FILE_NAME)));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return user.getConfigFile();
	}
	
	
	public static DatabaseConnect getDbInforFromConfigFile(MilanoUser user, String dbName)throws IOException{
		DatabaseConnect dbConnect = new DatabaseConnect();
		dbConnect.setUrl(getConfigFile(user).get(dbName, "url"));
		dbConnect.setDatabaseName(getConfigFile(user).get(dbName, "databaseName"));
		dbConnect.setUser(getConfigFile(user).get(dbName, "user"));
		String ecryptedPassword = getConfigFile(user).get(dbName, "password");
		String decodedString = new String(Base64.decodeBase64(ecryptedPassword.getBytes()));
		System.err.println(decodedString);
		dbConnect.setPassword(decodedString);
		return dbConnect;
	}
	
	public static String getPicFolder(MilanoUser user, String provider)throws IOException{
		return getConfigFile(user).get("Path", provider);
	}
	
	
	public static int getProductsPerCategoryMigrate(MilanoUser user)throws IOException{
		return new Integer(getConfigFile(user).get("misc", "productsPerCategoryMigrate")).intValue();
	}
	
	public static String getStoreId(MilanoUser user)throws IOException{
		return new String(getConfigFile(user).get("misc", "storeId"));
	}

	
	public static void encryptPassword(String password){
		  System.out.println(new String(Base64.encodeBase64(password.getBytes())));
	 }
	 
	 
	 public static String getMilanoHome(){
		 return System.getenv("MILANO_HOME");

	 }
	 
	 public static void witeToFile(String fileName){
		 try {
			   File myFile = new File(fileName);
			   if ( myFile.createNewFile() ) {
			      System.out.println("Success!");
			   } else {
			      System.out.println("Failure!");
			   }
			} catch ( IOException ioe ) { ioe.printStackTrace(); }
	 }
	 
	 
	 public static Path getFile(String fileName, Boolean appendDateStamp) throws IOException{
		 String newFileName = fileName;
		 if (appendDateStamp){
			  LocalDate date = LocalDate.now();
			  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd");
			  newFileName = fileName+"_"+date.format(formatter)+".log";
		 }
		 Path file = Paths.get(getMilanoHome(),"logs", newFileName);
		 if (!Files.exists(file)) {
				Files.createFile(file);
		 }
		 return file;
	 }
	 
	 public static void appendToFile(String fileName, String[] textArray){
	     try {
			 Path file = getFile(fileName, true);
			 for (int i = 0; i < textArray.length; i++) {
				 appendToFile(file,textArray[i]);
			 }
		 
			} catch (IOException e) {
				e.printStackTrace();
			}
	 }
	 

	 public static void appendToFile(Path file, String text){
	     try {
	    	 System.out.println(text);
			 Files.write(file, System.getProperty( "line.separator" ).getBytes(), StandardOpenOption.APPEND);
			 Files.write(file, text.getBytes(), StandardOpenOption.APPEND);
			} catch (IOException e) {
				e.printStackTrace();
			}
	 }

	 public static void appendToFile(String fileName, String text){
	     try {
			 Path file = getFile(fileName, true);
			 Files.write(file, System.getProperty( "line.separator" ).getBytes(), StandardOpenOption.APPEND);
			 Files.write(file, text.getBytes(), StandardOpenOption.APPEND);
			} catch (IOException e) {
				e.printStackTrace();
			}
	 }
	 
	 
	 public static void testDecode(){
		 String originalInput = "admin";
		 String encodedString = new String(Base64.encodeBase64(originalInput.getBytes()));
		 String decodedString = new String(Base64.decodeBase64(encodedString.getBytes()));
		 System.err.println("decoded="+decodedString);
		 System.err.println("encoded="+encodedString);

	 }
	 
	 public static Connection getDbConnection(MilanoUser mUser, String dbName) throws SQLException{
		 DatabaseConnect dbConnect = mUser.getConnectByName(dbName);
		 String url = null;
		 if (dbName.equalsIgnoreCase("mssql")){
			 url = dbConnect.getUrl()+";DatabaseName="+dbConnect.getDatabaseName();
		 } else  if (dbName.equalsIgnoreCase("mysql")){
			 url = dbConnect.getUrl()+"/"+dbConnect.getDatabaseName();
		 } else {
			 return null;
		 }
		 //		 String url = "jdbc:mysql://localhost:3306/broadleaf";

		 Connection connection = DriverManager.getConnection(
				 url, dbConnect.getUser(), dbConnect.getPassword());
		 return connection;
//				 "jdbc:sqlserver://localhost:1433;DatabaseName=INDEPENDANTDEMO", "sa", "Eyesh0ck3y");

	 }
	 
	 
	 
	 public static int getNextCategoryId() throws SQLException{
		 String query = "Select max(category_id) from blc_category";
		 Connection con = Main.getMilanoUser().getMySqlConnection();

		 Statement mStatement = con.createStatement();
 	     ResultSet mResultSet = mStatement.executeQuery(query);
 	     mResultSet.next();
 	     int res = mResultSet.getInt(1)+1;
 	     mStatement.close();
 	     return res;
	 }
	 
	 public static int getNextSkuMediaMapId() throws SQLException{
		 String query = "Select max(sku_media_id) from blc_sku_media_map";
		 Connection con = Main.getMilanoUser().getMySqlConnection();

		 Statement mStatement = con.createStatement();
 	     ResultSet mResultSet = mStatement.executeQuery(query);
 	     mResultSet.next();
 	     int res = mResultSet.getInt(1)+1;
 	     mStatement.close();
 	     return res;
	 }

	 public static int getNextCategoryMediaMapId() throws SQLException{
		 String query = "Select max(category_media_id) from blc_category_media_map";
		 Connection con = Main.getMilanoUser().getMySqlConnection();

		 Statement mStatement = con.createStatement();
 	     ResultSet mResultSet = mStatement.executeQuery(query);
 	     mResultSet.next();
 	     int res = mResultSet.getInt(1)+1;
 	     mStatement.close();
 	     return res;
	 }

	 public static int getDefaultSkuId(int productId) throws SQLException{
		 System.out.println("getting default_sku_id for product="+productId);
		 String query = "Select default_sku_id from blc_product where product_id=?";
		 Connection con = Main.getMilanoUser().getMySqlConnection();

		 PreparedStatement mStatement = con.prepareStatement(query);
		 mStatement.setInt(1, productId);
 	     ResultSet mResultSet = mStatement.executeQuery();
 	     mResultSet.next();
 	     int res = mResultSet.getInt(1);
 	     mStatement.close();
 	     return res;
	 }

	 public static int getNextProductId() throws SQLException{
		 String query = "Select max(product_id) from blc_product";
		 Connection con = Main.getMilanoUser().getMySqlConnection();

		 Statement mStatement = con.createStatement();
 	     ResultSet mResultSet = mStatement.executeQuery(query);
 	     mResultSet.next();
 	     int res = mResultSet.getInt(1)+1;
 	     mStatement.close();
 	     return res;
	 }
	 
	 
	 public static int getNextCategoryProductId() throws SQLException{
		 String query = "Select max(category_product_id) from blc_category_product_xref";
		 Connection con = Main.getMilanoUser().getMySqlConnection();

		 Statement mStatement = con.createStatement();
 	     ResultSet mResultSet = mStatement.executeQuery(query);
 	     mResultSet.next();
 	     int res = mResultSet.getInt(1)+1;
 	     mStatement.close();
 	     return res;
	 }
	 
	 public static int getNextSkuId(Connection  con) throws SQLException{
		 String query = "Select max(sku_id) from blc_sku";
		 Statement mStatement = con.createStatement();
 	     ResultSet mResultSet = mStatement.executeQuery(query);
 	     mResultSet.next();
 	     int res = mResultSet.getInt(1)+1;
 	     mStatement.close();
 	     return res;
	 }
	 public static int getNextOrderCategoryProductId(int categoryId) throws SQLException{
		 String query = "select IFNULL(max(DISPLAY_ORDER),0) from blc_category_product_xref where category_id=?";
		 Connection con = Main.getMilanoUser().getMySqlConnection();

		 PreparedStatement mStatement = con.prepareStatement(query);
		 mStatement.setInt(1, categoryId);
 	     ResultSet mResultSet = mStatement.executeQuery();
 	     mResultSet.next();
 	     int res = mResultSet.getInt(1)+1;
 	     mStatement.close();
 	     return res;
	 }
	 
	 
	 
	 public static Integer[] getNextSequenceForMenuItem() throws SQLException{
		 String query = "select max(sequence), max(menu_item_id) from blc_cms_menu_item;";
		 Connection con = Main.getMilanoUser().getMySqlConnection();
		 Statement mStatement = con.createStatement();
 	     ResultSet mResultSet = mStatement.executeQuery(query);
 	     mResultSet.next();
 	     Integer[] r = new Integer[2];
 	     r[0]= mResultSet.getInt(1)+1;
	     r[1]= mResultSet.getInt(2)+1;
 	     mStatement.close();
	     return r;
	 }
	 
	 
	 
	 public static int getNextSequenceNumber(String name, boolean update) throws SQLException{
		 Connection con = Main.getMilanoUser().getMySqlConnection();
		 String query = null;
		 PreparedStatement mStatement = null;
		 if (update){
			 query = "update sequence_generator set ID_VAL=ID_VAL+1 where id_name=?";//'ProductOptionImpl'";
			 mStatement = con.prepareStatement(query);
			 mStatement.setString(1, name);
			 mStatement.execute();
		 }
 	     //ResultSet mResultSet = mStatement.executeQuery();
		 query = "SELECT ID_VAL FROM sequence_generator where id_name=?";//'ProductOptionImpl'";
		 mStatement = con.prepareStatement(query);
		 mStatement.setString(1, name);
		 ResultSet mResultSet = mStatement.executeQuery();
 	     mResultSet.next();
 	     int res = mResultSet.getInt(1)+1;
 	     mStatement.close();
 	     return res;
	 }

	 public static int getQty(String inventId, String gridDet1, String gridDet2) throws SQLException{
		 String query = "select QTYONHAND from dbo.vQtyByGrid  where INVENTID=?"+//'FB7011B9-065F-447C-A043-041A7B716D48'
						" and ((GRIDDET1ID=? and GRIDDET2ID=?) or "+//'4FC9CCD9-95A9-4B5E-9111-140DA0EAA02A'
						" (GRIDDET2ID=? and GRIDDET1ID=?)) ";//'15603378-ECC3-4776-904F-1ECA43A05937'
		 Connection con = Main.getMilanoUser().getMsSqlConnection();

		 PreparedStatement mStatement = con.prepareStatement(query);
		 mStatement.setString(1, inventId);
		 mStatement.setString(2, gridDet1);
		 mStatement.setString(3, gridDet2);
		 mStatement.setString(4, gridDet1);
		 mStatement.setString(5, gridDet2);
 	     ResultSet mResultSet = mStatement.executeQuery();
 	     int res = 0;
 	     mResultSet.next();
 	     if (mResultSet.getRow()!=0){
 	    	 res = mResultSet.getInt(1);
 	     } 
 	     mStatement.close();
 	     return res;
 	     
	 }

	 
	 //TODO every day (timer) create a text file with all possible colors
	 final static String[] Colors = {"WHITE", "BLK", "BLUE", "BRN", "BURG","CHARC", "EGGPL", "GREEN", "GREY", "KHAKI", "LAV","NAT","NAVY","OCEAN",
			 "OLIVE", "PERIW", "RED", "TEAL","WHITE","BKBDY","BKBRN","BKRED","BLK","BONE","COFFE","ROSE","SAGE","TAN","GOLD", "MAHOGANY",
//			 "OLIVE", "PERIW", "RED", "SAND", "TEAL","WHITE","BKBDY","BKBRN","BKRED","BLK","BONE","COFFE","ROSE","SAGE","TAN","GOLD", "MAHOGANY",
			 "AQUA" ,"BLACK", "BUFF", "CHILI", "CLERY", "CORAL", "CORN", "COSMO", "CREAM","DEEP", "ESPRE","INDIG","MAUI", "PEWTR", "BROWN"};
	 final static String[] ShoeWidth = {"MED", "NARR", "SLIM", "WIDE", "XWIDE", "XXWID", "MED.","REG."};
			 
	 final static String[] ClosesSize ={ "XXS", "2XS", "XS", "S", "SM", "M", "MED", "L", "LRG", "XL", "XXL", "XLRG", "XSMAL", "XLARG", "0/S"};		 
	
	 public static String[] doesContainColor(String name){
		 String[] res = {null, null};
		 for (String color : Colors) {
			 if (name.contains(color)){
				String n = name.replace(color, "").trim();
				String[] resArray = {color, n};
				return resArray;
			 }
		 }
		 return res;
	 }
	 
	 
	 public static ResultSet getAllWithName(String name, String storeId, int categoryId) throws SQLException{
		 String query = "select i.inventId, i.idesc, c.tabcod, c.description from dbo.invent i, dbo.color "+
				 		" where i.icolor=c.tabcod and i.STOREID=? and i.iCATE=? and i.ISACTIVE='1' and i.idesc like '"+name+"%";
		 Connection con = Main.getMilanoUser().getMsSqlConnection();
		 PreparedStatement mStatement = con.prepareStatement(query);
		 mStatement.setString (1, storeId);
		 mStatement.setInt (2, categoryId);
	     ResultSet mResultSet = mStatement.executeQuery();
	     return mResultSet;
	
	 }
	 public static String[] TryToGuessOption(String option){
		 String[] res1 = {"Option", "SELECT"};
		 String[] res2 = {"Color", "COLOR"};
		 String[] res3 = {"Size", "SIZE"};
		 String[] res4 = {"Shoe Width", "SELECT"};
		 String[] res5 = {"Clothing Size", "SELECT"};
		 int res = 1;
		 if (Arrays.asList(Colors).contains(option)){
			 res = 2;
		 } else if (Arrays.asList(ShoeWidth).contains(option)){
			 res = 4;
		 } else if (Arrays.asList(ClosesSize).contains(option)){
			 res = 5;
		 } else if (NumberUtils.isNumber(option)){
			 res = 3;
		 }
		 switch (res) {
		 	case 1: return res1;
		 	case 2: return res2;
		 	case 3: return res3;
		 	case 4: return res4;
		 	case 5: return res5;
		 	default: return res1;
		 }
	 }
	 
	 public static final String JPG_EXTENSION = ".jpg";
	 public static final String BMP_EXTENSION = ".jpg";
	 public static final String PNG_EXTENSION = ".jpg";
	 
	 public static String getFileNameWithExtension(MilanoUser user, String iCode){
		 //final FileSystem fileSystem = FileSystems.getDefault();
		// String searchStr = iCode.concat("{,.*}");  //"a{,.*}"
		 File folder = new File(user.getMilanoPicsFolder());
	     File[] listOfFiles = folder.listFiles();

	    for (File file : listOfFiles)
	    {
	        if (file.isFile())
	        {
	            String[] filename = file.getName().split("\\.(?=[^\\.]+$)"); //split filename from it's extension
	            if(filename[0].equalsIgnoreCase(iCode)) {//matching defined filename
	                System.out.println("File exist: "+filename[0]+"."+filename[1]); // match occures.Apply any condition what you need
	            	return filename[0]+"."+filename[1];
	            }
	        }
	     }
	    return null;
	/*			 try (final DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("."), "a{,.*}")) {
			            for (final Path entry : stream) {
			                System.out.println(entry);
			            }
			       } catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	/*	 final PathMatcher pathMatcher = fileSystem.getPathMatcher("glob:a{,.*}");
		 final boolean matches = pathMatcher.matches(new File("a.txt").toPath());
		 String extension = null;
		 String milanoFilePathString = Constants.MILANO_PRODUCT_PICTURE_PATH.concat("/").
				 concat(iCode).concat(".jpg");
		 Path milanoPath = Paths.get(milanoFilePathString);
		 boolean fileExists = false;
		 try {
			 if (Files.exists(milanoPath)) {
				 eextension = ""
			 }
			 }
*/
	 }
	 
	 public static int getCategoryXRefId(int subCategoryId) throws SQLException{
		 //System.out.println("getting category id for category havng "+wordInside+" in the name");
		 String query = "Select CATEGORY_XREF_ID from BLC_CATEGORY_XREF where SUB_CATEGORY_ID=?";
		 Connection con = Main.getMilanoUser().getMySqlConnection();
		 PreparedStatement mStatement = con.prepareStatement(query);
		 mStatement.setInt(1, subCategoryId);
 	     ResultSet mResultSet = mStatement.executeQuery();
 	     mResultSet.next();
 	     int res=-1;
	     Boolean recordExists = mResultSet.getRow()!=0;
	     if (recordExists){
	    	 res = mResultSet.getInt(1);
	     }
 	     mStatement.close();
 	     return res;
	 }

	 
	 public static int getCmsMenuItemId(String wordInside) throws SQLException{
		 //System.out.println("getting category id for category havng "+wordInside+" in the name");
		 String query = "Select MENU_ITEM_ID from blc_cms_menu_item where LOWER(ACTION_URL) = LOWER(?)";
		 Connection con = Main.getMilanoUser().getMySqlConnection();
		 PreparedStatement mStatement = con.prepareStatement(query);
		 mStatement.setString(1, wordInside);
 	     ResultSet mResultSet = mStatement.executeQuery();
 	     mResultSet.next();
 	     int res=-1;
	     Boolean recordExists = mResultSet.getRow()!=0;
	     if (recordExists){
	    	 res = mResultSet.getInt(1);
	     }
 	     mStatement.close();
 	     return res;
	 }
	 
	 public static int getSkuMediaMapId(int skuId) throws SQLException{
		 //System.out.println("getting category id for category havng "+wordInside+" in the name");
		 String query = "Select SKU_MEDIA_ID from blc_sku_media_map where BLC_SKU_SKU_ID=?";
		 Connection con = Main.getMilanoUser().getMySqlConnection();
		 PreparedStatement mStatement = con.prepareStatement(query);
		 mStatement.setInt(1, skuId);
 	     ResultSet mResultSet = mStatement.executeQuery();
 	     mResultSet.next();
 	     int res=-1;
	     Boolean recordExists = mResultSet.getRow()!=0;
	     if (recordExists){
	    	 res = mResultSet.getInt(1);
	     }
 	     mStatement.close();
 	     return res;
	 }

	 public static int getStaticAssetId(String wordInside) throws SQLException{
		 //System.out.println("getting category id for category havng "+wordInside+" in the name");
		 String query = "Select STATIC_ASSET_ID from blc_static_asset where LOWER(name) = LOWER(?)";
		 Connection con = Main.getMilanoUser().getMySqlConnection();
		 PreparedStatement mStatement = con.prepareStatement(query);
		 mStatement.setString(1, wordInside);
 	     ResultSet mResultSet = mStatement.executeQuery();
 	     mResultSet.next();
 	     int res=-1;
	     Boolean recordExists = mResultSet.getRow()!=0;
	     if (recordExists){
	    	 res = mResultSet.getInt(1);
	     }
 	     mStatement.close();
 	     return res;
	 }

	 public static int getCategoryId(String wordInside) throws SQLException{
		 //System.out.println("getting category id for category havng "+wordInside+" in the name");
		 String query = "Select category_id from blc_category where LOWER(description)=LOWER(?)";
		 Connection con = Main.getMilanoUser().getMySqlConnection();
		 PreparedStatement mStatement = con.prepareStatement(query);
		 mStatement.setString(1, wordInside);
 	     ResultSet mResultSet = mStatement.executeQuery();
 	     mResultSet.next();
 	     int res=-1;
	     Boolean recordExists = mResultSet.getRow()!=0;
	     if (recordExists){
	    	 res = mResultSet.getInt(1);
	     }
 	     mStatement.close();
 	     return res;
	 }

	 public static int getMenuId(String wordInside) throws SQLException{
		 System.out.println("getting category id for catgeory havng "+wordInside+" in the name");
		 String query = "Select menu_item_id from blc_cms_menu_item where LOWER(description)=LOWER(?)";
		 Connection con = Main.getMilanoUser().getMySqlConnection();
		 PreparedStatement mStatement = con.prepareStatement(query);
		 mStatement.setString(1, wordInside);
 	     ResultSet mResultSet = mStatement.executeQuery();
	     int res=-1;
	     Boolean recordExists = mResultSet.getRow()!=0;
	     if (recordExists){
 	      res = mResultSet.getInt(1);
	     }
 	     mStatement.close();
 	     return res;
	 }
	 
}
