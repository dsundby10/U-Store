package accountlogin.registrationapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class StoreLayoutActivity extends AppCompatActivity {
    //add firebase stuff
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private DatabaseReference testRef;
    private DatabaseReference shelfRef;
    private String userID;
    private TextView myTV;
    private ListView mListView;

    Spinner spin0, spin1, spin2, spin3, spin4, spin5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_layout);
        Intent intent = getIntent();
        setTitle("Store Layout");
        myTV = (TextView)findViewById(R.id.textView4);
        mListView = (ListView)findViewById(R.id.listviewX);
     //   spin0 = (Spinner)findViewById(R.id.spinner0);
      //  spin1 = (Spinner)findViewById(R.id.spinner1);
       // spin2 = (Spinner)findViewById(R.id.spinner2);
     //   spin3 = (Spinner)findViewById(R.id.spinner3);
      //  spin4 = (Spinner)findViewById(R.id.spinner4);
       // spin5 = (Spinner)findViewById(R.id.spinner5);


        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {

                }
            }
        };

        testRef = mFirebaseDatabase.getReference().child(userID).child("BaySetup");
        testRef.addListenerForSingleValueEvent(new ValueEventListener() {
            ArrayList<String>advAarr = new ArrayList<String>();
            ArrayList<String>advBarr = new ArrayList<String>();

            ArrayList<String> arrayz = new ArrayList<>();
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()){
                    String advAisle = (String) childSnapShot.child("aisle").getValue();
                    String advBay = (String)childSnapShot.child("bays").getValue();
                    Log.i("zCheckCheck: ", advAisle + " " + advBay);

                    String advA = "ADVAisle: " + advAisle + "ADVBays: " + advBay;
                    arrayz.add(advA);
                    advAarr.add(advAisle);
                    advBarr.add(advBay);
                }
              //  ArrayAdapter advAadp = new ArrayAdapter(StoreLayoutActivity.this,android.R.layout.simple_spinner_dropdown_item, advAarr);
                //spin0.setAdapter(advAadp);
                //ArrayAdapter advBadp = new ArrayAdapter(StoreLayoutActivity.this,android.R.layout.simple_spinner_dropdown_item, advBarr);
               // spin1.setAdapter(advBadp);
                //ArrayAdapter arrayAdapter = new ArrayAdapter(StoreLayoutActivity.this,android.R.layout.simple_list_item_1,arrayz);
                //mListView.setAdapter(arrayAdapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        shelfRef = mFirebaseDatabase.getReference().child(userID).child("ShelfSetup");
        shelfRef.addListenerForSingleValueEvent(new ValueEventListener() {
            ArrayList<String> array2 = new ArrayList<>();
            ArrayList<String>Aarr = new ArrayList<String>();
            ArrayList<String>Barr = new ArrayList<String>();
            ArrayList<String>Sarr = new ArrayList<String>();
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()){
                    String aisleNum = (String)childSnapShot.child("aisle_num").getValue();
                    String bay_num = (String) childSnapShot.child("bay_num").getValue();
                    String num_of_shelves = (String) childSnapShot.child("num_of_shelves").getValue();
                    Log.i("zAisle: ", aisleNum);
                    String shelfSet = "Aisle: " + aisleNum + " Bay: " + bay_num + " shelves: " + num_of_shelves;
                    array2.add(shelfSet);
                    Aarr.add(aisleNum);
                    Barr.add(bay_num);
                    Sarr.add(num_of_shelves);
                }
                ArrayAdapter arrayAdapter1 = new ArrayAdapter(StoreLayoutActivity.this,android.R.layout.simple_list_item_1,array2);
                mListView.setAdapter(arrayAdapter1);
               // ArrayAdapter Aadp = new ArrayAdapter(StoreLayoutActivity.this,android.R.layout.simple_spinner_dropdown_item,Aarr);
               // spin3.setAdapter(Aadp);
               // ArrayAdapter Badp = new ArrayAdapter(StoreLayoutActivity.this,android.R.layout.simple_spinner_dropdown_item,Barr);
               // spin4.setAdapter(Badp);
               // ArrayAdapter Sadp = new ArrayAdapter(StoreLayoutActivity.this,android.R.layout.simple_spinner_dropdown_item, Sarr);
               // spin5.setAdapter(Sadp);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}