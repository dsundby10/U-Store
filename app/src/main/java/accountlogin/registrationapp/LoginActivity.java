package accountlogin.registrationapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG ="Something Important: ";
    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private Button btnSignup, btnLogin, btnReset;

    //Added
    Button storeEmployee;

    //Firebase Variables
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private DatabaseReference verifyStore;
    private String userID;

    private String checkStoreName;
    private String verifyStoreName;
    ArrayList<String> getStoreNameString = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("U-Store Login");

        //Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        //Get Firebase auth instance
        mAuth = FirebaseAuth.getInstance();

        //Uncomment this during testing phase to avoid having to login every time
      // if (mAuth.getCurrentUser() != null  ) {
          //  startActivity(new Intent(LoginActivity.this, MainMenu.class));
          //  finish();
       //}

        setContentView(R.layout.activity_login);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnSignup = (Button) findViewById(R.id.btn_signup);
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnReset = (Button) findViewById(R.id.btn_reset_password);


        //Get Firebase auth instance
        mAuth = FirebaseAuth.getInstance();

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });

        //added storeEmployee login
        storeEmployee = (Button)findViewById(R.id.storeEmployee);
        storeEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, LoginStoreEmployee.class));
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Must enter your email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //authenticate user
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the mAuth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                progressBar.setVisibility(View.GONE);
                                if (!task.isSuccessful()) {
                                    // there was an error
                                    if (password.length() < 6) {
                                        inputPassword.setError(getString(R.string.minimum_password));
                                    } else {
                                        Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                    }
                                }else{
                                    //if exists determine which screen they get sent too
                                    if (mAuth.getCurrentUser() != null){
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        userID = user.getUid();
                                        //Checks to see if the storeName has been created in the database
                                        //checkStoreName = myRef.child(userID).child("storeName").toString();
                                        String store = "";
                                        /*Added this as temporary for allowing additional users to login under the store*/
                                        myRef.child(userID).child("StoreInfo").child("storeEmail").push();
                                        myRef.child(userID).child("StoreInfo").child("storePass").push();
                                        myRef.child(userID).child("StoreInfo").child("storeEmail").setValue(inputEmail.getText().toString());
                                        myRef.child(userID).child("StoreInfo").child("storePass").setValue(inputPassword.getText().toString());
                                        myRef.child("StoreUsers").child(userID).child("StoreInfoEmail").setValue(inputEmail.getText().toString());
                                        myRef.child("StoreUsers").child(userID).child("StoreInfoPass").setValue(inputPassword.getText().toString());
                                        verifyIfStoreExists(userID);
                                    }
                                }
                            }
                        });
            }
        });



    }

    public void verifyIfStoreExists(String currentUserID){
        final Intent intent = new Intent(LoginActivity.this,MainMenu.class);

        verifyStore = mFirebaseDatabase.getReference().child(currentUserID);
        verifyStore.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String x = "";
                getStoreNameString = new ArrayList<String>();
                for (DataSnapshot data: dataSnapshot.getChildren()){
                    if (data.getKey().equals("storeName") && data.getValue().toString().length() > 1){
                        x = data.getValue().toString().trim();
                        intent.putExtra("STORE_NAME", x);
                        intent.putExtra("STORE_USER", "null");
                        intent.putExtra("USER_PERMISSIONS", "111111111");
                        startActivity(intent);
                        finish();
                        break;

                    } else {
                        intent.putExtra("STORE_NAME","?¿NA¿?");
                        intent.putExtra("STORE_USER", "null");
                        intent.putExtra("USER_PERMISSIONS", "111111111");
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void sendIntentData(){
        Intent intent = new Intent(LoginActivity.this, MainMenu.class);
        intent.putExtra("STORE_NAME", "StoreName");
        intent.putExtra("STORE_USER", "null");
        intent.putExtra("USER_PERMISSIONS", "1111111");
        startActivity(intent);
    }


    public void onLoginGenerateDB(){
         /*Added this as temporary for allowing additional users to login under the store*/
        myRef.child(userID).child("StoreInfo").child("storeEmail").push();
        myRef.child(userID).child("StoreInfo").child("storePass").push();
        myRef.child(userID).child("StoreInfo").child("storeEmail").setValue(inputEmail.getText().toString());
        myRef.child(userID).child("StoreInfo").child("storePass").setValue(inputPassword.getText().toString());
        myRef.child("StoreUsers").child(userID).child("StoreInfoEmail").setValue(inputEmail.getText().toString());
        myRef.child("StoreUsers").child(userID).child("StoreInfoPass").setValue(inputPassword.getText().toString());
    }
}



