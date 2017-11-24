package accountlogin.registrationapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

    LinearLayout storeOwner_linearLayout, storeUser_linearLayout;
    Button store_owner_btn, store_user_btn, btnLogin1;
    EditText password1,unique_pass, store_name1, email_name1;

    String storeID = "";
    String storeInfoEmail="";
    String storeInfoPass="";
    String storeUniquePass="";
    String storeName = "";

    ArrayList<String> verifyStoreOwnerID = new ArrayList<>();
    ArrayList<String> verifyStoreName = new ArrayList<>();
    ArrayList<String> verifyStoreEmail = new ArrayList<>();
    ArrayList<String> verifyStorePass = new ArrayList<>();
    ArrayList<String> verifyStoreUniquePass = new ArrayList<>();
    ArrayList<String> verifyStoreUserList = new ArrayList<>();


    String currentStoreName = "";
    String currentStoreID= "";
    String currentStoreEmail="";
    String currentStorePass="";

    String currentUserPass = "";
    String currentUserEmail="";
    String currentUserPerm="";



    //Firebase Variables
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private DatabaseReference verifyStore;
    DatabaseReference ManageStore;
    private String userID;

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

        //Get Firebase auth instance
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_login);
        /*== Initialize Variables & storeOwner/User btn listeners for visibility ==*/
        initializeLayoutVariables();
        visibilityListeners();


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



                //authenticate user
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the mAuth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
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
        /*==Store User Login Listener==*/
        btnLogin1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int failCount=0;
                for (int i = 0; i < verifyStoreName.size() ; i++) {
                    if (!store_name1.getText().toString().equals(verifyStoreName.get(i))
                            && !unique_pass.getText().toString().equals(verifyStoreUniquePass.get(i))) {
                        failCount++;
                        if (failCount==verifyStoreName.size()){
                            toastMessage("FAILED!");
                        }

                    } else {
                        if (store_name1.getText().toString().equals(verifyStoreName.get(i))
                                && unique_pass.getText().toString().equals(verifyStoreUniquePass.get(i))) {

                            currentStoreName = verifyStoreName.get(i);
                            currentStoreID = verifyStoreOwnerID.get(i);
                            currentStoreEmail = verifyStoreEmail.get(i);
                            currentStorePass = verifyStorePass.get(i);

                            String strHold = verifyStoreUserList.get(i);

                            String[] splitter = strHold.split(",");
                            int x = 0;
                            int y = 1;
                            int z = 2;

                            for (int j = 0; j < splitter.length / 3; j++) {
                                currentUserPerm = splitter[x].substring(32, splitter[x].length());
                                currentUserPass = splitter[y].substring(10, splitter[y].length());
                                currentUserEmail = splitter[z].substring(11, splitter[z].length() - 1);

                               // System.out.println("PASS: " + splitter[1].substring(10, splitter[1].length()));
                               // System.out.println("Email: " + splitter[2].substring(11, splitter[2].length() - 1));

                                if (email_name1.getText().toString().equals(currentUserEmail) && password1.getText().toString().equals(currentUserPass)) {
                                    String email = currentStoreEmail;
                                    final String password = currentStorePass;
                                    //authenticate user
                                    mAuth.signInWithEmailAndPassword(email, password)
                                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if (!task.isSuccessful()) {
                                                        // there was an error
                                                        if (password.length() < 6) {
                                                        } else {
                                                            Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                                        }
                                                    } else {
                                                        //if exists determine which screen they get sent too
                                                        if (mAuth.getCurrentUser() != null) {
                                                            FirebaseUser user = mAuth.getCurrentUser();
                                                            userID = user.getUid();
                                                            Intent intent = new Intent(LoginActivity.this, MainMenu.class);
                                                            intent.putExtra("STORE_NAME", storeName);
                                                            intent.putExtra("STORE_USER", currentUserEmail);
                                                            intent.putExtra("USER_PERMISSIONS", currentUserPerm);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    }
                                                }
                                            });
                                } else {
                                    x += 3;
                                    y += 3;
                                    z += 3;
                                }
                            }
                        } else {
                            toastMessage("Login Failed.");
                        }
                    }
                }
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
    public void initializeLayoutVariables(){

        store_owner_btn = (Button)findViewById(R.id.store_owner_btn);
        store_owner_btn.setTextColor(getResources().getColor(R.color.loginPageGreen));

        store_user_btn = (Button)findViewById(R.id.store_user_btn);

        //Layout fields for storeOwner view
        storeOwner_linearLayout = (LinearLayout)findViewById(R.id.storeOwner_linearLayout);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnSignup = (Button) findViewById(R.id.btn_signup);
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnReset = (Button) findViewById(R.id.btn_reset_password);

        //LayoutFields for storeUser view
        storeUser_linearLayout = (LinearLayout)findViewById(R.id.storeUser_linearLayout);
        store_name1 = (EditText)findViewById(R.id.store_name1);
        email_name1 = (EditText)findViewById(R.id.email_name1);
        unique_pass = (EditText)findViewById(R.id.unique_pass);
        password1 = (EditText)findViewById(R.id.password1);
        btnLogin1 = (Button) findViewById(R.id.btn_login1);

    }
    public void visibilityListeners(){
        store_owner_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Set Text colors of both buttons
                store_owner_btn.setTextColor(getResources().getColor(R.color.loginPageGreen));
                store_user_btn.setTextColor(Color.BLACK);
                storeOwner_linearLayout.setVisibility(View.VISIBLE);
                storeUser_linearLayout.setVisibility(View.INVISIBLE);
            }
        });

        store_user_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateStoreUserDatabaseValues(); // generate the Database listener to validate login
                store_owner_btn.setTextColor(Color.BLACK);
                store_user_btn.setTextColor(getResources().getColor(R.color.loginPageGreen));
                storeOwner_linearLayout.setVisibility(View.INVISIBLE);
                storeUser_linearLayout.setVisibility(View.VISIBLE);

            }
        });
    }
    public void generateStoreUserDatabaseValues(){
        /*=== Database Listener to pull all active stores with Store Users Enabled ===*/
        ManageStore = mFirebaseDatabase.getReference().child("StoreUsers");
        //ManageStore.addValueEventListener(new ValueEventListener() {
        ManageStore.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                verifyStoreName = new ArrayList<String>();
                verifyStoreOwnerID = new ArrayList<String>();
                verifyStoreUniquePass = new ArrayList<String>();
                verifyStoreEmail = new ArrayList<String>();
                verifyStorePass = new ArrayList<String>();
                verifyStoreUserList = new ArrayList<String>();

                for (DataSnapshot data : dataSnapshot.getChildren()) {

                    storeID = data.getKey();
                    //Don't want to add any stores that don't have Users added to their database (else DB error)
                    if (data.child("StoreName").exists() && data.child("Users").exists()){
                        //Pull data from each store
                        storeName = data.child("StoreName").getValue().toString();
                        storeInfoEmail = data.child("StoreInfoEmail").getValue().toString();
                        storeInfoPass = data.child("StoreInfoPass").getValue().toString();
                        storeUniquePass = data.child("StoreUniquePass").getValue().toString();

                        //Add the data to be used for the verification of the store & users
                        verifyStoreName.add(storeName);
                        verifyStoreOwnerID.add(data.getKey());
                        verifyStoreEmail.add(storeInfoEmail);
                        verifyStorePass.add(storeInfoPass);
                        verifyStoreUniquePass.add(storeUniquePass);
                        String x = data.child("Users").getValue().toString();
                        x = x.substring(0, x.length() - 1);
                        verifyStoreUserList.add(x);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void toastMessage(String message) {
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }
}



