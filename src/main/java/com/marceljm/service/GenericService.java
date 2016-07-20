package com.marceljm.service;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public interface GenericService<T> {

	public MongoDatabase getDatabase();

	public MongoClient getClient();

}
