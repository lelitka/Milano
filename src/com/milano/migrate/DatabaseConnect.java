package com.milano.migrate;

import org.jasypt.contrib.org.apache.commons.codec_1_3.binary.Base64;

public class DatabaseConnect {
	private String url;
	private String databaseName;
	private String user;
	private String password;
	
	 public String getDecodedPassword(){
		 return new String(Base64.decodeBase64(this.password.getBytes()));
	 }

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getDatabaseName() {
		return databaseName;
	}
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}
	
	
}
