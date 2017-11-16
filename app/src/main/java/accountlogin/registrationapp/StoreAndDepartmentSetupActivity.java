package accountlogin.registrationapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class StoreAndDepartmentSetupActivity extends AppCompatActivity {

    Button submit;
    EditText storeName, listDept;

    //Firebase Variables
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private DatabaseReference myStoreRef;
    private DatabaseReference manageStoreRef;
    private String userID;

    String currentStoreName = "";
    String currentDeptNames = "";

    //Intent Data Variables
    private String getStoreName = "";
    private String employeeID;
    private String getUserPermissions="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_and_department_setup);
        setTitle("Part 1: Store & Department Setup");

        Intent intent = getIntent();
        getStoreName = intent.getStringExtra("STORE_NAME");
        getUserPermissions = intent.getStringExtra("USER_PERMISSIONS");
        employeeID = intent.getStringExtra("STORE_USER");


        submit = (Button)findViewById(R.id.Submit_Info);
        storeName = (EditText)findViewById(R.id.Storename);
        listDept = (EditText)findViewById(R.id.ListDept);

        //Firebase Initialization
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        myStoreRef = mFirebaseDatabase.getReference();
        manageStoreRef = mFirebaseDatabase.getReference();
        final FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {

                }
            }
        };

        /*====== Value Event Listener To Check if store & dept names exists & display them if they already do ====*/
        myStoreRef = mFirebaseDatabase.getReference().child(userID);
        myStoreRef.orderByChild("storeName").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                storeName = (EditText)findViewById(R.id.Storename);
                for(DataSnapshot data: dataSnapshot.getChildren()) {
                    String store_name_string = data.toString();

                    //Check if storeName key exists & the key's value doesn't equal null & display value
                    if (data.getKey().equals("storeName") && !data.getValue().toString().trim().equals("")){
                        currentStoreName = data.getValue().toString();
                        storeName.setText(data.getValue().toString());
                    }
                    //Check if deptName key exists & the key's value doesn't equal null & display value
                    if (data.getKey().equals("deptNames") && !data.getValue().toString().trim().equals("")){
                        currentDeptNames = data.getValue().toString();
                        listDept.setText(data.getValue().toString());
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    /*========= ON SUBMIT BUTTON LISTENER ========*/
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String StoreName = storeName.getText().toString();
                String ListDept = listDept.getText().toString();

                //Add dept names to database
                myRef.child(userID).child("deptNames").setValue(ListDept);

                //Verify storeName has a real string value
                if (!StoreName.trim().equals("")) {
                    myRef.child(userID).child("storeName").setValue(StoreName);
                    manageStoreRef.child("StoreUsers").child(userID).child("StoreName").setValue(StoreName);
                    manageStoreRef.child("StoreUsers").child(userID).child("StoreUniquePass").setValue("randomvalue");
                    String genPassKey = manageStoreRef.child("StoreUsers").child(userID).child("StoreUniquePass").push().getKey();
                    manageStoreRef.child("StoreUsers").child(userID).child("StoreUniquePass").setValue(genPassKey);

                    //Set hint back to original color & text
                    storeName.setHintTextColor(getResources().getColor(R.color.editTextHintColor));
                    storeName.setHint("Store Name");


                    Intent intent = new Intent(StoreAndDepartmentSetupActivity.this,AisleBaySetup.class);
                    intent.putExtra("STORE_USER", employeeID);
                    intent.putExtra("STORE_NAME", StoreName);
                    intent.putExtra("USER_PERMISSIONS", getUserPermissions);
                    startActivity(intent);


                } else { //Try again
                        storeName.setHint("* You Must Name Your Store");
                        storeName.setHintTextColor(Color.RED);
                }

            }
        });
    }
    public void sendIntentData(Intent intent){
        intent.putExtra("STORE_USER", employeeID);
        intent.putExtra("STORE_NAME", getStoreName);
        intent.putExtra("USER_PERMISSIONS", getUserPermissions);
        startActivity(intent);
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
    private void toastMessage(String message) {
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }

}
