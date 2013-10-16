package lab.meteor.test;

import com.mongodb.MongoClient;
import com.mongodb.DB;
import java.net.UnknownHostException;

public class Connector {
	
	String dbName;
	private MongoClient mongoClient = null;
	private DB db = null;
	
	public Connector(String dbName) {
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
