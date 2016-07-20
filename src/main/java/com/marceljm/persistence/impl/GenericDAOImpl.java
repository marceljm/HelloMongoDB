package com.marceljm.persistence.impl;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import com.marceljm.persistence.GenericDAO;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

@Repository
public class GenericDAOImpl<T> implements GenericDAO<T> {

	@PersistenceContext
	private EntityManager manager;

	private MongoClient mongoClient;
	private MongoDatabase database;
	private String host = "192.168.0.16";
	private int port = 27017;
	private String db = "mydb";

	@PostConstruct
	@Override
	public void init() {
		System.out.println("init");
		this.mongoClient = new MongoClient(host, port);
		this.database = mongoClient.getDatabase(db);
	}

	@Override
	public MongoClient getMongoClient() {
		return mongoClient;
	}

	@Override
	public MongoDatabase getDatabase() {
		return database;
	}

}
