package accountlogin.registrationapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ManageUserPermissionsDeleteUser extends AppCompatActivity {
    Spinner user_options, currentUserSpinner;
    Button main_menu_btn, back_btn, submit_btn;
    EditText userEmail;

    //10 values to account for
    int userPermissionsEnabled=0;

    ArrayList<String> storeUserEmail = new ArrayList<>();
    ArrayList<String> storeUserPass = new ArrayList<>();
    ArrayList<String> storeUserPerm = new ArrayList<>();
    ArrayList<String> storeUserKey = new ArrayList<>();
    ArrayList<String> currentUserList = new ArrayList<>();

    //Firebase Variables & References
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private DatabaseReference ManageStore;

    private String userID;
    //Intent Data Variables
    private String getStoreName = "";
    private String employeeID;
    private String getUserPermissions="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_user_permissions_delete_user);
        Intent intent = getIntent();
        getStoreName = intent.getStringExtra("STORE_NAME");
        getUserPermissions = intent.getStringExtra("USER_PERMISSIONS");
        employeeID = intent.getStringExtra("STORE_USER");

        //Firebase Initialization
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
        currentUserSpinner = (Spinner) findViewById(R.id.currentUserSpinner);
        userEmail = (EditText)findViewById(R.id.userEmail);


        bottomButtonListeners();
        generateUserOptions();

        /*==Generate User Options Spinner && Listener ==*/
        user_options = (Spinner)findViewById(R.id.user_options);
        user_options.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    if (user_options.getSelectedItemPosition()==0) {
                        Intent intent = new Intent(ManageUserPermissionsDeleteUser.this, ManageUserPermissionsMainPage.class);
                        sendIntentData(intent);
                    }
                    if (user_options.getSelectedItemPosition()==1){
                        Intent intent = new Intent(ManageUserPermissionsDeleteUser.this, ManageUserPermissionsAddUser.class);
                        sendIntentData(intent);

                    }
                    if (user_options.getSelectedItemPosition()==2) {
                        Intent intent = new Intent(ManageUserPermissionsDeleteUser.this, ManageUserPermissionsEditUser.class);
                        sendIntentData(intent);
                    }
                    if (user_options.getSelectedItemPosition()==3){

                    }
                }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ManageStore = mFirebaseDatabase.getReference().child("StoreUsers").child(userID).child("Users");
        ManageStore.addListenerForSingleValueEvent(new ValueEventListener() {
            //ManageStore.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                storeUserEmail = new ArrayList<String>();
                storeUserPass = new ArrayList<String>();
                storeUserPerm = new ArrayList<String>();
                storeUserKey = new ArrayList<String>();
                for (DataSnapshot data: dataSnapshot.getChildren()){

                        String PPE = data.getValue().toString();
                        String[] xp = PPE.split(",");
                        if (xp.length==2){
                            String perm = xp[0].trim();
                            String pass = xp[1].trim();
                            String email = xp[2].trim();

                            storeUserKey.add(data.getKey());
                            storeUserPerm.add(perm.substring(10, perm.length()));
                            storeUserPass.add(pass.substring(9, pass.length()));
                            storeUserEmail.add(email.substring(10, email.length() - 1));
                        }

                }
                  /*==Generate values for currrent user spinner==*/
                for (int i = 0; i <storeUserEmail.size(); i++) {
                    currentUserList.add("USER " + i + ": " + storeUserEmail.get(i));
                }
                ArrayAdapter arrayAdapter1 = new ArrayAdapter(ManageUserPermissionsDeleteUser.this, android.R.layout.simple_spinner_dropdown_item, currentUserList);
                currentUserSpinner.setAdapter(arrayAdapter1);
                currentUserSpinner.setSelection(0);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        currentUserSpinnerListener();
        deleteUserButtonListener();
    }

    public void deleteUserButtonListener(){
        submit_btn = (Button)findViewById(R.id.submit_btn);
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int deleteCount=0;
                for (int i = 0; i < storeUserEmail.size() ; i++) {
                    if (storeUserEmail.get(i).equals(userEmail.getText().toString())){
                        myRef.child("StoreUsers").child(userID).child("Users").child(storeUserKey.get(i)).removeValue();
                        toastMessage(userEmail.getText().toString() + " has been removed!");
                        deleteCount=1;
                        Intent intent=new Intent(ManageUserPermissionsDeleteUser.this,ManageUserPermissionsDeleteUser.class);
                        sendIntentData(intent);
                        break;
                    } else {

                    }
                }
                if (deleteCount==0){
                    toastMessage("That user doesn't exist, please select from the spinner");
                }
            }
        });
    }
    public void currentUserSpinnerListener(){
        currentUserSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < storeUserEmail.size(); i++) {
                    if (currentUserSpinner.getSelectedItemPosition()==i){
                        userEmail.setText(storeUserEmail.get(i));
                        break;
                    } else {

                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /*=== Genereate Array List & Adapater for user_Options spinner ===*/
    public void generateUserOptions(){
        user_options = (Spinner)findViewById(R.id.user_options);
        ArrayList<String> userOptionList = new ArrayList<>();
        userOptionList.add("Employee Permissions Options");
        userOptionList.add("Add New User");
        userOptionList.add("Edit Current User");
        userOptionList.add("Remove User");

        ArrayAdapter arrayAdapter = new ArrayAdapter(ManageUserPermissionsDeleteUser.this,android.R.layout.simple_spinner_dropdown_item,userOptionList);
        user_options.setAdapter(arrayAdapter);
        user_options.setSelection(3);
    }
    private void bottomButtonListeners(){
        main_menu_btn = (Button)findViewById(R.id.main_menu_btn);
        back_btn = (Button)findViewById(R.id.back_btn);
        main_menu_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageUserPermissionsDeleteUser.this,MainMenu.class);
                sendIntentData(intent);
            }
        });
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageUserPermissionsDeleteUser.this, ManageAccountActivity.class);
                sendIntentData(intent);
            }
        });
    }
    public void sendIntentData(Intent intent){
        intent.putExtra("STORE_USER", employeeID);
        intent.putExtra("STORE_NAME", getStoreName);
        intent.putExtra("USER_PERMISSIONS", getUserPermissions);
        startActivity(intent);
    }
    private void toastMessage(String message) {
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }
}
