package Mongo;

import Exceptions.NothingUpdatedException;
import com.mongodb.MongoClient;
import com.mongodb.bulk.InsertRequest;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;


import java.util.ArrayList;
import java.util.List;

import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.currentDate;
import static com.mongodb.client.model.Updates.set;


/**
 * Created by scapista on 8/27/17.
 * +
 * + Priority:
 * + 1. build failing
 * + 2. function not working
 * + 3. nice to have but would optimize
 * + 4. nice to have no benefit
 * + 5. future state
 */

public class MongoApi {
    MongoClient mongoClient         = null;
    MongoDatabase database          = null;
    MongoCollection<Document> coll  = null;

    //private String Collection;
    public MongoApi(String inDatabase, String inCollection){
        this.mongoClient = new MongoClient();
        this.database = mongoClient.getDatabase(inDatabase);
        this.coll = database.getCollection(inCollection);
    }

    public void setDatabaseCollection(String inDatabase, String inCollection) {
        this.database = mongoClient.getDatabase(inDatabase);
        this.coll = database.getCollection(inCollection);
    }
    public void findReplace(Bson filter, Document doc , Boolean close) {
        try {
            System.out.println("replacing " + coll.getNamespace());
            this.coll.findOneAndReplace(filter,doc);
        } catch(Exception e){
            e.printStackTrace();
            System.out.println("findReplace-->" + coll.getNamespace());
        } finally {
            if (close)this.mongoClient.close();
        }
    }
    public void insertMany(List<Document> docs, Boolean close) {
        try {
            System.out.println("Inserting into " + coll.getNamespace());
            this.coll.insertMany(docs);
        } catch(Exception e){
            e.printStackTrace();
            System.out.println("insertMany-->" + coll.getNamespace());
        } finally {
            if (close)this.mongoClient.close();
        }
    }
    public void insertOne(Document doc, Boolean close) {
        try {
            System.out.println("Inserting into " + this.coll.getNamespace());
            this.coll.insertOne(doc);
        } catch(Exception e){
            e.printStackTrace();
            System.out.println("insertOne" + "-->" + this.coll.getNamespace());
        } finally {
            if (close) this.mongoClient.close();
        }
    }
    public void upsertOne(Bson filter, Bson update, Boolean Close ){
        try {
            UpdateResult updateResult = this.coll.updateOne(filter, update,
                    new UpdateOptions().upsert(true));
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("upsertOne" + "-->" + this.coll.getNamespace() + " failed");
        } finally {
            if (Close) this.mongoClient.close();
        }
    }

    public void upsertMany(Document doc, Bson update, Boolean Close) {
        System.out.println("Inserting into " + this.coll.getNamespace());
        try {
            this.coll.updateOne(
                    new Document("league", doc.get("league"))
                            .append("season_year", doc.get("season"))
                            .append("team", doc.get("team")),
                    update,
                    new UpdateOptions().upsert(true).bypassDocumentValidation(true));
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("upsertMany" + "-->" + this.database + "." + this.coll);
        } finally {
            if (Close) this.mongoClient.close();
        }
    }

    public Document getMongoDocument(Bson filter, Bson projection, Boolean Close){
        Document doc = null;
        try {
            doc = coll.find(filter).projection(projection).first();
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("getMongoDocument" + "-->" + this.coll.getNamespace());
        } finally {
            if (Close) this.mongoClient.close();
        }
        return doc;
    }
    public Document getMongoDocument(Bson filter, Boolean Close){
        Document doc = null;
        try {
            doc = coll.find(filter).first();
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("getMongoDocument" + "-->" + this.coll.getNamespace());
        } finally {
            if (Close) this.mongoClient.close();
        }
        return doc;
    }
    public FindIterable<Document> getMongoDocumentList(Bson filter, Bson projection, Boolean Close){
        FindIterable<Document> docs = null;
        try {
            docs = coll.find(filter).projection(projection);
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("getMongoDocumentList" + "-->" + this.coll.getNamespace());
        } finally {
            if (Close) this.mongoClient.close();
        }
        return docs;
    }
    public FindIterable<Document> getMongoDocumentList(Bson projection, Boolean Close){
        FindIterable<Document> docs = null;
        try {
            docs = coll.find().projection(projection);
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("getMongoDocumentList" + "-->" + this.coll.getNamespace());
        } finally {
            if (Close) this.mongoClient.close();
        }
        return docs;
    }

    public FindIterable<Document> getMongoDocumentList(Boolean Close,Bson filter){
        FindIterable<Document> docs = null;
        try {
            docs = coll.find(filter);
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("getMongoDocumentList" + "-->" + this.coll.getNamespace());
        } finally {
            if (Close) this.mongoClient.close();
        }
        return docs;
    }
    public FindIterable<Document> getMongoDocumentList(Boolean Close,Bson filter,Bson sort){
        FindIterable<Document> docs = null;
        try {
            docs = coll.find(filter).sort(sort);
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("getMongoDocumentList" + "-->" + this.coll.getNamespace());
        } finally {
            if (Close) this.mongoClient.close();
        }
        return docs;
    }
    public FindIterable<Document> getMongoDocumentList(Boolean Close){
        FindIterable<Document> docs = null;
        try {
            docs = coll.find();
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("getMongoDocumentList" + "-->" + this.coll.getNamespace());
        } finally {
            if (Close) this.mongoClient.close();
        }
        return docs;
    }
    public MongoCursor<String> getDatabaseDistinctString(String fieldName,Boolean Close){
        MongoCursor<String> result = null;
        try {
            result = this.coll.distinct(fieldName, String.class).iterator();
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("getDatabaseDistinctString" + "-->" + this.coll.getNamespace());
        } finally {
            if (Close) this.mongoClient.close();
        }
        return result;
    }
    public void deleteMany(Bson filter, Boolean Close ){
        try {
            this.coll.deleteMany(filter);
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("putApiCalls" + "-->" + this.coll.getNamespace());
        } finally {
            if (Close) this.mongoClient.close();
        }
    }
    public void dropCollection(Boolean Close ){
        try {
            this.coll.drop();
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("putApiCalls" + "-->" + this.coll.getNamespace());
        } finally {
            if (Close) this.mongoClient.close();
        }
    }
//    public void upsertOne(Bson filter, Bson update, Boolean Close) {
//        System.out.println("Inserting into " + this.coll.getNamespace());
//        try {
//            this.coll.updateOne( filter, update,
//                    new UpdateOptions().upsert(true));
//            System.out.println(this.coll.getWriteConcern());
//        } catch (Exception e){
//            e.printStackTrace();
//            System.out.println("upsertOne-->" + this.coll.getNamespace());
//        } finally {
//            if (Close) this.mongoClient.close();
//        }
//    }
    public void closeConnection(){
        this.mongoClient.close();
    }


}



