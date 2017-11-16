package accountlogin.registrationapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class LoginStoreEmployee extends AppCompatActivity {
    static final String STORE_EMPLOYEE= "";
    EditText store_name, unique_pass, email_name, password;
    Button login_btn, user_login;
    ProgressBar progressBar;

    //Firebase Variables & References
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private DatabaseReference ManageStore;
    private DatabaseReference ManageStoreUsers;
    private DatabaseReference StoreRefID;
    private String userID;

    //Authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseUser user;
    String checkStoreName="";
    String userName = "";
    String storeID = "";
    String storeInfoEmail="";
    String storeInfoPass="";
    String storeUniquePass="";
    String storeName = "";

    ArrayList<String> storeUserEmail = new ArrayList<>();
    ArrayList<String> storeUserPass = new ArrayList<>();
    ArrayList<String> storeUserPerm = new ArrayList<>();
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
    String currentStoreUniquePass="";

    String currentUserPass = "";
    String currentUserEmail="";
    String currentUserPerm="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_store_employee);
        Intent intent = getIntent();
        store_name = (EditText)findViewById(R.id.store_name);
        unique_pass = (EditText)findViewById(R.id.unique_pass);
        email_name = (EditText)findViewById(R.id.email_name);
        password = (EditText)findViewById(R.id.password);
        login_btn = (Button)findViewById(R.id.login_btn);
        user_login = (Button)findViewById(R.id.user_login);

        //Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();


        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (int i = 0; i < verifyStoreName.size() ; i++) {
                    if (!store_name.getText().toString().equals(verifyStoreName.get(i))
                            && !unique_pass.getText().toString().equals(verifyStoreUniquePass.get(i))) {

                    } else {
                        currentStoreName = verifyStoreName.get(i);
                        currentStoreID = verifyStoreOwnerID.get(i);
                        currentStoreEmail = verifyStoreEmail.get(i);
                        currentStorePass = verifyStorePass.get(i);

                            String strHold = verifyStoreUserList.get(i);

                            String[] splitter = strHold.split(",");
                        int x = 0;
                        int y = 1;
                        int z = 2;

                        for (int j = 0; j < splitter.length/3; j++) {
                            currentUserPerm = splitter[x].substring(32,splitter[x].length());
                            currentUserPass = splitter[y].substring(10,splitter[y].length());
                            currentUserEmail = splitter[z].substring(11,splitter[z].length()-1);

                            System.out.println("PASS: " + splitter[1].substring(10,splitter[1].length()));
                            System.out.println("Email: " + splitter[2].substring(11,splitter[2].length()-1));

                            if (email_name.getText().toString().equals(currentUserEmail) && password.getText().toString().equals(currentUserPass)){
                                String email = currentStoreEmail;
                                final String password = currentStorePass;
                                //authenticate user
                                mAuth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(LoginStoreEmployee.this, new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (!task.isSuccessful()) {
                                                    // there was an error
                                                    if (password.length() < 6) {
                                                    } else {
                                                        Toast.makeText(LoginStoreEmployee.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                                    }
                                                } else {
                                                    //if exists determine which screen they get sent too
                                                    if (mAuth.getCurrentUser() != null) {
                                                        FirebaseUser user = mAuth.getCurrentUser();
                                                        userID = user.getUid();

                                                            Intent intent = new Intent(LoginStoreEmployee.this, MainMenu.class);
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
                                x+=3;
                                y+=3;
                                z+=3;
                            }
                         }

                        //email_name.setVisibility(View.VISIBLE);
                        //password.setVisibility(View.VISIBLE);

                    }
                }
            } // END ON CLICK


        });

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


