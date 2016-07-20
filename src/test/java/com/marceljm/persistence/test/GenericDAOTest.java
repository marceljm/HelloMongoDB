package com.marceljm.persistence.test;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Filters.lte;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Sorts.descending;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import com.marceljm.entity.Hero;
import com.marceljm.persistence.GenericDAO;
import com.marceljm.persistence.impl.GenericDAOImpl;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

public class GenericDAOTest {

	private GenericDAO<Hero> dao = new GenericDAOImpl<Hero>();

	private Document doc;

	private MongoCollection<Document> collection;

	@Before
	public void init() {
		dao.init();
		collection = dao.getDatabase().getCollection("test");
	}

	private Block<Document> printBlock = new Block<Document>() {
		@Override
		public void apply(final Document document) {
			System.out.println(document.toJson());
		}
	};

	@Test
	public void insertOne() {
		doc = new Document("name", "MongoDB").append("type", "database").append("count", 1).append("info",
				new Document("x", 203).append("y", 102));
		collection.insertOne(doc);
		System.out.println(collection.find().first().toJson());
	}

	@Test
	public void insertMany() {
		List<Document> documents = new ArrayList<Document>();
		for (int i = 0; i < 100; i++)
			documents.add(new Document("i", i));
		collection.insertMany(documents);

		MongoCursor<Document> cursor = collection.find().iterator();
		try {
			while (cursor.hasNext())
				System.out.println(cursor.next().toJson());
		} finally {
			cursor.close();
		}
		System.out.println(collection.count());
	}

	@Test
	public void findOne() {
		doc = collection.find(eq("i", 71)).first();
		System.out.println(doc.toJson());
	}

	@Test
	public void findMany() {
		collection.find(gt("i", 50)).forEach(printBlock);
	}

	@Test
	public void findRange() {
		collection.find(and(gt("i", 50), lte("i", 55))).forEach(printBlock);
	}

	@Test
	public void sort() {
		doc = collection.find(exists("i")).sort(descending("i")).first();
		System.out.println(doc.toJson());
	}

	@Test
	public void projection() {
		doc = collection.find().projection(excludeId()).first();
		System.out.println(doc.toJson());
	}

	@Test
	public void updateOne() {
		collection.updateOne(eq("i", 10), new Document("$set", new Document("i", 110)));
	}

	@Test
	public void updateMany() {
		UpdateResult updateResult = collection.updateMany(lt("i", 100), new Document("$inc", new Document("i", 100)));
		System.out.println(updateResult.getModifiedCount());
	}

	@Test
	public void deleteOne() {
		collection.deleteOne(eq("i", 71));
	}

	@Test
	public void deleteMany() {
		DeleteResult deleteResult = collection.deleteMany(gte("i", 100));
		System.out.println(deleteResult.getDeletedCount());
	}

	@Test
	public void orderedBulk() {
		collection.bulkWrite(Arrays.asList(new InsertOneModel<>(new Document("_id", 4)),
				new InsertOneModel<>(new Document("_id", 5)), new InsertOneModel<>(new Document("_id", 6)),
				new UpdateOneModel<>(new Document("_id", 1), new Document("$set", new Document("x", 2))),
				new DeleteOneModel<>(new Document("_id", 2)),
				new ReplaceOneModel<>(new Document("_id", 3), new Document("_id", 3).append("x", 4))));
	}

	@Test
	public void unorderedBulk() {
		collection.bulkWrite(
				Arrays.asList(new InsertOneModel<>(new Document("_id", 4)),
						new InsertOneModel<>(new Document("_id", 5)), new InsertOneModel<>(new Document("_id", 6)),
						new UpdateOneModel<>(new Document("_id", 1), new Document("$set", new Document("x", 2))),
						new DeleteOneModel<>(new Document("_id", 2)),
						new ReplaceOneModel<>(new Document("_id", 3), new Document("_id", 3).append("x", 4))),
				new BulkWriteOptions().ordered(false));
	}

	@Test
	public void listDatabases() {
		for (String name : dao.getMongoClient().listDatabaseNames()) {
			System.out.println(name);
		}
	}

	@Test
	public void dropDatabase() {
		dao.getMongoClient().getDatabase("databaseToBeDropped").drop();
	}

	@Test
	public void createCollection() {
		dao.getDatabase().createCollection("cappedCollection",
				new CreateCollectionOptions().capped(true).sizeInBytes(0x100000));
	}

	@Test
	public void listCollections() {
		for (String name : dao.getDatabase().listCollectionNames()) {
			System.out.println(name);
		}
	}

	@Test
	public void dropCollection() {
		collection.drop();
	}

	@Test
	public void createIndex() {
		// create an ascending (1) index on the "i" field
		collection.createIndex(new Document("i", 1));
	}

	@Test
	public void listCollectionIndexes() {
		for (final Document index : collection.listIndexes()) {
			System.out.println(index.toJson());
		}
	}

	@Test
	public void createTextIndex() {
		// create a text index on the "content" field
		collection.createIndex(new Document("content", "text"));
	}

	@Test
	public void findUsingTextIndex() {
		// Insert some documents
		collection.insertOne(new Document("_id", 0).append("content", "textual content"));
		collection.insertOne(new Document("_id", 1).append("content", "additional content"));
		collection.insertOne(new Document("_id", 2).append("content", "irrelevant content"));

		// Find using the text index
		long matchCount = collection.count(Filters.text("textual content -irrelevant"));
		System.out.println("Text search matches: " + matchCount);
	}

	@Test
	public void runningCommand() {
		Document buildInfo = dao.getDatabase().runCommand(new Document("buildInfo", 1));
		System.out.println(buildInfo);
	}
}
