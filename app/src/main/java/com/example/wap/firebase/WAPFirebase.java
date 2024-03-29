package com.example.wap.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class WAPFirebase<TEntity>{
    private static final String TAG = "WAP Firebase Operations";
    private final Class<TEntity> entityClass;
    private final CollectionReference collectionRef;
    FirebaseFirestore db;

    /**
     Constructor
     Instantiation: WAPFirebase<Signal> wapFirebaseSignal = new WAPFirebase<>(Signal.class,"signals");

     * @param entityClass: the Class of the object that is to be stored in Firebase
     * @param collectionName (String): the name of the collection in the Firebase relevant to the class.
     */

    public WAPFirebase(Class <TEntity> entityClass, String collectionName){
        this.entityClass = entityClass;
        db = FirebaseFirestore.getInstance();
        this.collectionRef = db.collection(collectionName);

    }

    /**
     *  Fetching a single document from the Firestore database
     * @param uuid (String): the particular uuid that you want to fetch from the db
     * @return an instance of the particular class, null if the document does not exist
     */
    public Task<TEntity> query(String uuid){
        final String documentID = uuid;
        final DocumentReference docRef = collectionRef.document(documentID);
        return docRef.get().continueWith(new Continuation<DocumentSnapshot, TEntity>() {
            @Override
            public TEntity then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                DocumentSnapshot docSnap = task.getResult();
                if(docSnap.exists()){
                    return docSnap.toObject(entityClass);
                }
                else{
                    Log.w(TAG,"Document "+documentID+" does not exist");
                    return null;
                }
            }
        });
    }
    // for querying multiple documents with certain values
    public Task<ArrayList<TEntity>> compoundQuery(String field, Object value){
        final String valString = value.toString();
        Query query = collectionRef.whereEqualTo(field,value);
        return query.get().continueWith(new Continuation<QuerySnapshot, ArrayList<TEntity>>() {
            @Override
            public ArrayList<TEntity> then(@NonNull Task<QuerySnapshot> task) throws Exception {
                QuerySnapshot querySnap = task.getResult();
                if(querySnap.isEmpty()){
                    Log.w(TAG,"Query " + valString + "does not yield any result");
                    return null;
                }
                else{
                    List<DocumentSnapshot> docList= querySnap.getDocuments();
                    ArrayList<TEntity> returnList = new ArrayList<>();
                    for(DocumentSnapshot doc:docList){
                        returnList.add(doc.toObject(entityClass));
                    }
                    return returnList;
                }
            }
        });
    }

    public Task<ArrayList<TEntity>> getCollection() {
        return collectionRef.get().continueWith(new Continuation<QuerySnapshot, ArrayList<TEntity>>() {
            @Override
            public ArrayList<TEntity> then(@NonNull Task<QuerySnapshot> task) throws Exception {
                QuerySnapshot querySnap = task.getResult();
                if (querySnap.isEmpty()) {
                    Log.w(TAG, "Collection is empty");
                    return null;
                } else {
                    List<DocumentSnapshot> docList = querySnap.getDocuments();
                    ArrayList<TEntity> returnList = new ArrayList<>();
                    for (DocumentSnapshot doc : docList) {
                        returnList.add(doc.toObject(entityClass));
                    }
                    return returnList;
                }

            }
        });
    }

    public Task<Void> create(TEntity entity, String uuid){
        final String uuidString = uuid;
        DocumentReference docRef = collectionRef.document(uuidString);
        return docRef.set(entity)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "SUCCESS in create method");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,"Error in creating document: "+uuidString);
            }
        });
    }
    public Task<Void> update(TEntity entity, String uuid){
        final String uuidString = uuid;
        DocumentReference docRef = collectionRef.document(uuidString);
        return docRef.set(entity).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG,"Error in updating document: "+uuidString);
            }
        });
    }
    public Task<Void> delete (String uuid){
        final String uuidString = uuid;
        DocumentReference docRef = collectionRef.document(uuidString);
        return docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG,"Successful in deleting document: "+uuidString);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG,"Error in deleting document: "+uuidString);
            }
        });
    }
}