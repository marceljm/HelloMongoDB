package com.marceljm.persistence;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public interface GenericDAO<T> {

	public MongoClient getMongoClient();

	public MongoDatabase getDatabase();

	public void init();
}