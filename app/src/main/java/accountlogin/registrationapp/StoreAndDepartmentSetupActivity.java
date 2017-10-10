package accountlogin.registrationapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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


    Button submit, aislebay;
    EditText storeName, numDept, listDept;
    //Firebase Variables
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String userID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_and_department_setup);

        Intent intent = getIntent();

        aislebay = (Button)findViewById(R.id.btnbtn);
        submit = (Button)findViewById(R.id.Submit_Info);
        storeName = (EditText)findViewById(R.id.Storename);
        numDept = (EditText)findViewById(R.id.NumDept);
        listDept = (EditText)findViewById(R.id.ListDept);

        //Firebase Initialization
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        final FirebaseUser user = mAuth.getCurrentUser();
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

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String StoreName = storeName.getText().toString();
                String NumDept = numDept.getText().toString();
                String ListDept = listDept.getText().toString();

                Log.d("OnClick - S&D Setup", "\nDeptNames: " +  ListDept
                        + "\nNumDepts: " + NumDept + "\nStoreName: " + StoreName);

                if (!StoreName.equals("") && !NumDept.equals("") && !ListDept.equals("")) {
                    //UserInformation userInformation = new UserInformation();
                    //userInformation.setDeptNames(ListDept);
                    //userInformation.setNumDepartments(NumDept);
                    //userInformation.setStoreName(StoreName);

                    //myRef.child("users").child(userID).setValue(userInformation);

                    myRef.child(userID).child("deptNames").setValue(ListDept);
                    myRef.child(userID).child("numDepartments").setValue(NumDept);
                    myRef.child(userID).child("storeName").setValue(StoreName);



                    Intent intent1 = new Intent(StoreAndDepartmentSetupActivity.this,AisleBaySetup.class);
                    startActivity(intent1);

                }else {
                    toastMessage("Please Fill Out All Fields!");
                }

            }
        });

        aislebay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(StoreAndDepartmentSetupActivity.this,ViewDatabase.class);
                startActivity(intent1);
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
    private void toastMessage(String message) {
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }
}
