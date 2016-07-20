package com.marceljm.service.impl;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.marceljm.persistence.GenericDAO;
import com.marceljm.service.GenericService;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

@Service
public class GenericServiceImpl<T> implements GenericService<T> {

	@Inject
	private GenericDAO<T> dao;

	@Override
	public MongoDatabase getDatabase() {
		return dao.getDatabase();
	}

	@Override
	public MongoClient getClient() {
		return dao.getMongoClient();
	}
}