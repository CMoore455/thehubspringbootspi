package moore.christian.thehub;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;

@Configuration
public class MongoConfig extends AbstractMongoConfiguration{

  @Bean
   public GridFsTemplate gridFsTemplate() throws Exception {
      return new GridFsTemplate(mongoDbFactory(), mappingMongoConverter());
   }

	@Override
	public MongoClient mongoClient() {
		
		return new MongoClient("127.0.0.1", 27017);
	}

	@Override
	protected String getDatabaseName() {
		return "thehubuserimages";
	}
	
	public GridFSBucket getGridFs() {
        MongoDatabase db = mongoDbFactory().getDb();
        return GridFSBuckets.create(db);
    }

    public MongoDatabase getDb() {
        MongoDatabase db = mongoDbFactory().getDb();
        return db;
    }
}
