package accountlogin.registrationapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ManageAccountActivity extends AppCompatActivity {
    Button submit_btn, main_menu_btn;
    EditText enter_pass, enter_store, new_password;
    Spinner account_options;
    TextView displayText;

    //Firebase Variables & References
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private DatabaseReference DeleteStoreUsers;
    private String userID;

    ArrayList<String> getUserInfo = new ArrayList<>();

    int currentSpinnerPosition=0;

    String currentStoreName="";

    //Intent Data Variables
    private String getStoreName = "";
    private String employeeID;
    private String getUserPermissions="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_account);
        Intent intent = getIntent();
        getStoreName = intent.getStringExtra("STORE_NAME");
        getUserPermissions = intent.getStringExtra("USER_PERMISSIONS");
        employeeID = intent.getStringExtra("STORE_USER");

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();


        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        DeleteStoreUsers = mFirebaseDatabase.getReference().child("StoreUsers");
        mainMenuBtnListner();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser userCheck = firebaseAuth.getCurrentUser();
                if (userCheck == null) {
                }
            }
        };

        myRef = mFirebaseDatabase.getReference().child(userID);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentStoreName="";
                getUserInfo = new ArrayList<String>();
                for (DataSnapshot data: dataSnapshot.getChildren()) {
                    if (data.getKey().equals("storeName")){
                        currentStoreName = data.getValue().toString();
                    }
                    if (data.getKey().equals("StoreInfo")){
                        String info = data.getValue().toString();
                        String[] infoArr = info.split(",");
                        getUserInfo.add(infoArr[0].substring(12,infoArr[0].length())); // Add email
                        getUserInfo.add(infoArr[1].substring(11,infoArr[1].length()-1)); //Add pass

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Layout Variable Initialization
        submit_btn = (Button)findViewById(R.id.submit_btn);
        enter_store = (EditText)findViewById(R.id.enter_store);
        new_password = (EditText)findViewById(R.id.new_password);
        displayText = (TextView)findViewById(R.id.displayText);

        /*== Create & Generate the Account_Options Spinner ==*/
        account_options = (Spinner)findViewById(R.id.account_options);
        generateAccountOptionsSpinner();

        /*=== Account Options Spinner On Item Selected Listener ===*/
        account_options.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position==0){ //Nothing
                    displayText.setVisibility(View.INVISIBLE);
                    enter_store.setVisibility(View.INVISIBLE);
                    new_password.setVisibility(View.INVISIBLE);
                    submit_btn.setVisibility(View.INVISIBLE);
                    currentSpinnerPosition=0;
                }
                if (position==1) { //Edit StoreName
                    enter_store.setText("");
                    new_password.setText("");
                    displayText.setVisibility(View.VISIBLE);
                    displayText.setText("Current Store Name:" + currentStoreName);

                    enter_store.setVisibility(View.VISIBLE);
                    enter_store.setHint("New Store Name");

                    submit_btn.setVisibility(View.VISIBLE);
                    submit_btn.setText("Update Store Name");

                    new_password.setVisibility(View.INVISIBLE);
                    currentSpinnerPosition=1;
                }
                if (position==2) { //Change Password
                    enter_store.setText("");
                    new_password.setText("");
                    displayText.setVisibility(View.VISIBLE);
                    displayText.setText("Change Password Form");

                    enter_store.setVisibility(View.VISIBLE);
                    enter_store.setHint("Enter Current Password");

                    new_password.setVisibility(View.VISIBLE);
                    new_password.setHint("Enter New Password");

                    submit_btn.setVisibility(View.VISIBLE);
                    submit_btn.setText("Change Password");
                    currentSpinnerPosition=2;
                }
                if (position==3) { //Delete Store
                    enter_store.setText("");
                    new_password.setText("");

                    displayText.setVisibility(View.VISIBLE);
                    displayText.setText("Store Deletion Form");

                    enter_store.setVisibility(View.VISIBLE);
                    enter_store.setHint("Enter Account Name");

                    new_password.setVisibility(View.VISIBLE);
                    new_password.setHint("Enter Current Password");

                    submit_btn.setVisibility(View.VISIBLE);
                    submit_btn.setText("Confirm");
                    currentSpinnerPosition=3;

                }
                if (position==4) { //Delete Account
                    enter_store.setText("");
                    new_password.setText("");

                    displayText.setVisibility(View.VISIBLE);
                    displayText.setText("Account Deletion Form");

                    enter_store.setVisibility(View.VISIBLE);
                    enter_store.setHint("Enter Account Name");

                    new_password.setVisibility(View.VISIBLE);
                    new_password.setHint("Enter Password");

                    submit_btn.setVisibility(View.VISIBLE);
                    submit_btn.setText("Confirm");
                    currentSpinnerPosition=4;
                }
                if (position==5){ //Manage Employee Permissions
                    enter_store.setText("");
                    new_password.setText("");

                    displayText.setVisibility(View.INVISIBLE);
                    enter_store.setVisibility(View.INVISIBLE);
                    new_password.setVisibility(View.INVISIBLE);
                    submit_btn.setVisibility(View.INVISIBLE);
                    Intent intentx= new Intent(ManageAccountActivity.this, ManageUserPermissionsMainPage.class);
                    sendIntentData(intentx);
                    currentSpinnerPosition=5;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        /*== Submit button listener for all 4 cases ==*/
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentSpinnerPosition==1){
                    changeStoreName();
                }
                if (currentSpinnerPosition==2){
                    changePassword();
                }
                if (currentSpinnerPosition==3){
                    deleteStore();
                }
                if (currentSpinnerPosition==4){
                    deleteAccount();
                }
            }
        });

    }
    /*== Delete Store Method ==*/
    public void deleteStore(){

        String userEmail = enter_store.getText().toString();
        String userPass = new_password.getText().toString();
        if (mAuth.getCurrentUser().getEmail().equals(userEmail) && userPass.equals(getUserInfo.get(1))) {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(submit_btn.getContext(), android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(submit_btn.getContext());
            }
            builder.setTitle("Deleting Store Can't Be Undone!")
                    .setMessage("Deleting your store will delete everything assoicated with your store, do you wish to proceed?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            myRef.child("BaySetup").removeValue();
                            myRef.child("BayType").removeValue();
                            myRef.child("ShelfSetup").removeValue();
                            myRef.child("aisles").removeValue();
                            myRef.child("deptNames").removeValue();
                            myRef.child("storeName").removeValue();
                            DeleteStoreUsers.child(userID).child("Users").removeValue();
                            //Update the storeName data to be sent through the intent
                            getStoreName = "?¿NA¿?";
                            Intent intent = new Intent(ManageAccountActivity.this,MainMenu.class);
                            sendIntentData(intent);
                            toastMessage("Store has been removed... Relocating to main page");

                        }
                    })
                    /*==== dont delete data ===*/
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        } else {
            toastMessage("Please double check your login information.");
        }
    }

    /*== Delete Account Method ==*/
    public void deleteAccount(){
        String userEmailx = enter_store.getText().toString();
        String userPassx = new_password.getText().toString();
        if (mAuth.getCurrentUser().getEmail().equals(userEmailx) && userPassx.equals(getUserInfo.get(1))) {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(submit_btn.getContext(), android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(submit_btn.getContext());
            }
            builder.setTitle("Deleting Account Can't Be Undone!")
                    .setMessage("Deleting your account will delete everything assoicated with your store, do you wish to proceed?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DeleteStoreUsers = mFirebaseDatabase.getReference().child("StoreUsers").child(userID);
                            DeleteStoreUsers.removeValue();
                            myRef.removeValue();
                            toastMessage("Account has been deleted.. Relocating to Login Page");
                            mAuth.getCurrentUser().delete();
                            signOut();
                        }
                    })
                    /*==== dont delete data ===*/
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        } else {
            toastMessage("Please double check your login information.");
        }
    }


    /*== Change Store Name Method ==*/
    public void changeStoreName(){
         String newStoreName = enter_store.getText().toString().trim();
        if (newStoreName.trim().length() >= 1) {
            myRef.child("storeName").setValue(newStoreName);
            DeleteStoreUsers.child(userID).child("StoreName").setValue(newStoreName);
            toastMessage("Store name updated: " + newStoreName);
            //Update storeName data to be sent with intent
            getStoreName = newStoreName;
            Intent intent = new Intent(ManageAccountActivity.this, MainMenu.class);
            sendIntentData(intent);
        } else {
            toastMessage("Store name must be a valid entry.");
        }
    }

    /*== Change Store Owner Password Method==*/
    public void changePassword(){
        String currentPass = enter_store.getText().toString();
        String newPass = new_password.getText().toString();
        /*-- check if currentPass matches & if newPass >= 6 --*/
        if (currentPass.equals(getUserInfo.get(1))) {
            if (newPass.contains(",") && newPass.trim().length() <= 5){ //Commas complicate stuff for me
                toastMessage("New cant contain commas and length must be >=6");
            } else { //success
                mAuth.getCurrentUser().updatePassword(new_password.getText().toString());
                toastMessage("Password has been updated, please sign in again.");
                signOut();
            }
        } else { //Not long enough
            toastMessage("Incorrect Password.");
        }
    }
    /*== Generating account options spinner ==*/
    public void generateAccountOptionsSpinner(){
        ArrayList<String> optionsList = new ArrayList<>();
        optionsList.add("View Account Options");
        optionsList.add("Change Store Name");
        optionsList.add("Change Password");
        optionsList.add("Delete Store");
        optionsList.add("Delete Account");
        optionsList.add("Manage Employee Permissions");
        ArrayAdapter arrayAdapter = new ArrayAdapter(ManageAccountActivity.this,R.layout.custom_spinner_layout,optionsList);
        account_options.setAdapter(arrayAdapter);
    }

    /*== Main menu btn initializer & listener ==*/
    public void mainMenuBtnListner(){
        main_menu_btn = (Button)findViewById(R.id.main_menu_btn);
        main_menu_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageAccountActivity.this, MainMenu.class);
                sendIntentData(intent);
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
    //sign out method
    public void signOut() {
        mAuth.signOut();
        startActivity(new Intent(ManageAccountActivity.this, LoginActivity.class));
        finish();
    }
    public void sendIntentData(Intent intent){
        intent.putExtra("STORE_USER", employeeID);
        intent.putExtra("STORE_NAME", getStoreName);
        intent.putExtra("USER_PERMISSIONS", getUserPermissions);
        startActivity(intent);
    }
}
