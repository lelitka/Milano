package com.milano.migrate.util;

import java.sql.Connection;

import org.ini4j.Ini;

import com.milano.migrate.DatabaseConnect;

public class MilanoUser {
	private Ini configFile;
	private DatabaseConnect msSqlConnect;
	private DatabaseConnect mySqlConnect;
	private Connection msSqlConnection;
	private Connection mySqlConnection;
	private String milanoPicsFolder;
	
	public String getMilanoPicsFolder() {
		return milanoPicsFolder;
	}
	public void setMilanoPicsFolder(String milanoPicsFolder) {
		this.milanoPicsFolder = milanoPicsFolder;
	}
	public String getBroadleafPicsFolder() {
		return broadleafPicsFolder;
	}
	public void setBroadleafPicsFolder(String broadleafPicsFolder) {
		this.broadleafPicsFolder = broadleafPicsFolder;
	}



	private String broadleafPicsFolder;
	
	public Ini getConfigFile() {
		return this.configFile;
	}
	public Connection getMsSqlConnection() {
		return msSqlConnection;
	}
	public void setMsSqlConnection(Connection msSqlConnection) {
		this.msSqlConnection = msSqlConnection;
	}
	public Connection getMySqlConnection() {
		return mySqlConnection;
	}
	public void setMySqlConnection(Connection mySqlConnection) {
		this.mySqlConnection = mySqlConnection;
	}
	public  void setConfigFile(Ini configFile) {
		this.configFile = configFile;
	}
	public DatabaseConnect getMsSql() {
		return this.msSqlConnect;
	}
	public void setMsSql(DatabaseConnect msSqlConnect_) {
		this.msSqlConnect = msSqlConnect_;
	}
	public DatabaseConnect getMySql() {
		return this.mySqlConnect;
	}
	public void setMySql(DatabaseConnect mySqlConnect_) {
		this.mySqlConnect = mySqlConnect_;
	}

	
	
	public DatabaseConnect getConnectByName(String name){
		 try {
			 if (name.equalsIgnoreCase("mssql")){
				Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			return this.getMsSql();
			} else if (name.equalsIgnoreCase("mysql")){
				 Class.forName("com.mysql.jdbc.Driver");
				return this.getMySql();
			} else {
				return null;
			}
		} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
		}
	}
}
