package lab.meteor.visualize;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.MongoClient;

public class MongoConnector {
	String dbName;
	private MongoClient mongoClient = null;
	private DB db = null;
	
	public MongoConnector(String dbName) {
		this.dbName = dbName;
	}
	
	public DB getDB() {
		return db;
	}
	
	public void open() {
		try {
			mongoClient = new MongoClient("localhost");
			db = mongoClient.getDB(dbName);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		db = null;
		mongoClient.close();
	}
}
