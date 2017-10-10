package accountlogin.registrationapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ViewDatabase extends AppCompatActivity {
    private static final String TAG = "Information: ";
    //add firebase stuff
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String userID;
    private TextView myTV;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_database);

        Intent intent = getIntent();
        myTV = (TextView)findViewById(R.id.textView4);
        mListView = (ListView)findViewById(R.id.listviewX);

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
                    // user auth state is changed - user is null
                    // launch login activity
                    //startActivity(new Intent(MainActivity.this, LoginActivity.class));

                }
            }
        };
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void showData(DataSnapshot dataSnapshot) {

        zAllUserData zInfo = new zAllUserData();
        for(DataSnapshot ds : dataSnapshot.getChildren()) {
            zInfo.setNumAisles(ds.child(userID).child("aisles").getValue(String.class));
            zInfo.setNumBays(ds.child(userID).child("genBays").getValue(String.class));
            zInfo.setDeptNames(ds.child(userID).child("deptNames").getValue(String.class));
            zInfo.setNumDepartments(ds.child(userID).child("numDepartments").getValue(String.class));
            zInfo.setStoreName(ds.child(userID).child("storeName").getValue(String.class));


            Log.i(TAG, "showData: deptNames " + zInfo.getDeptNames());
            Log.i(TAG, "showData: numDept " + zInfo.getNumDepartments());
            Log.i(TAG, "showData: storeName " + zInfo.getStoreName());

            ArrayList<String> array = new ArrayList<>();
            array.add("Aisles: " + zInfo.getNumAisles());
            array.add(zInfo.getNumBays());
            //array.add("Dept Names: " + zInfo.getDeptNames());
           //array.add(zInfo.getNumDepartments());
           //array.add(zInfo.getStoreName());


            ArrayAdapter arrayAdapter = new ArrayAdapter(ViewDatabase.this,android.R.layout.simple_list_item_1,array);
            mListView.setAdapter(arrayAdapter);

        }
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
