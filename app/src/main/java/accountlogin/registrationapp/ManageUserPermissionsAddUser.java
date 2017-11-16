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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
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

public class ManageUserPermissionsAddUser extends AppCompatActivity {
    LinearLayout checkBoxLinearLayout, addUserLinearLayout;
    Spinner user_options;
    CheckBox add_inv_cbox, edit_inv_cbox, search_inv_cbox, view_layout_cbox,
            edit_layout_cbox, add_shelving_cbox, full_access_cbox, edit_dept_cbox, edit_ab_cbox, enable_perm_cbox;
    Button main_menu_btn, back_btn, submit_btn;
    EditText userEmail, userPass;

    //10 values to account for
    int userPermissionsEnabled=0;
    int addInvCbox = 0;
    int editInvCbox = 0;
    int searchInvCbox = 0;
    int viewLayoutCbox = 0;
    int editLayoutCbox = 0;
    int addShelvingCbox = 0;
    int fullAccessCbox = 0;
    int editDeptcBox = 0;
    int editABcBox=0;

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
    private DatabaseReference PermListener;
    private String userID;
    //Intent Data Variables
    private String getStoreName = "";
    private String employeeID;
    private String getUserPermissions="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_user_permissions_add_user);
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
        //Permission enabled listener
        permissionCheckBoxListener();
        initializeCheckBoxesAndListeners();
        bottomButtonListeners();

        generateUserOptions();
        /*==Generate User Options Spinner && Listener ==*/
        user_options = (Spinner)findViewById(R.id.user_options);
        user_options.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (user_options.getSelectedItemPosition()==0) {
                        Intent intent = new Intent(ManageUserPermissionsAddUser.this, ManageUserPermissionsMainPage.class);
                        sendIntentData(intent);
                    }
                    if (user_options.getSelectedItemPosition()==1){

                    }
                    if (user_options.getSelectedItemPosition()==2) {
                        Intent intent = new Intent(ManageUserPermissionsAddUser.this, ManageUserPermissionsEditUser.class);
                        sendIntentData(intent);
                    }
                    if (user_options.getSelectedItemPosition()==3){
                        Intent intent = new Intent(ManageUserPermissionsAddUser.this, ManageUserPermissionsDeleteUser.class);
                        sendIntentData(intent);
                    }
                }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ManageStore = mFirebaseDatabase.getReference().child("StoreUsers").child(userID).child("Users");
        //ManageStore.addValueEventListener(new ValueEventListener() {
            ManageStore.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                storeUserEmail = new ArrayList<String>();
                storeUserPass = new ArrayList<String>();
                storeUserPerm = new ArrayList<String>();
                storeUserKey = new ArrayList<String>();
                for (DataSnapshot data: dataSnapshot.getChildren()){
                    String PPE = data.getValue().toString();
                    String[] xp = new String[3];
                    xp = PPE.split(",");
                    String perm = xp[0].trim();
                    String pass = xp[1].trim();
                    String email = xp[2].trim();
                    System.out.println("ZONKPermissions: " + perm.substring(10,perm.length()));
                    System.out.println("ZONKPassword: " + pass.substring(9, pass.length()));
                    System.out.println("ZONKEmail: " + email.substring(10,email.length()-1));

                    storeUserKey.add(data.getKey());
                    storeUserPerm.add(perm.substring(10, perm.length()));
                    storeUserPass.add(pass.substring(9, pass.length()));
                    storeUserEmail.add(email.substring(10, email.length() - 1));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        addUserFunction();


    }

    public void addUserFunction(){
        userEmail = (EditText)findViewById(R.id.userEmail);
        userPass = (EditText)findViewById(R.id.userPass);
        submit_btn = (Button)findViewById(R.id.submit_btn);
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int checkExist=0;
                    if (userEmail.getText().toString().contains(",") || userPass.getText().toString().contains(",")) {
                        System.out.println("Your password or email may not contain any commas!");
                    } else {
                        for (int i = 0; i < storeUserEmail.size(); i++) {
                            if (storeUserEmail.get(i).equals(userEmail.getText().toString())){
                                toastMessage("User already exists!");
                                checkExist=1;
                            } else {
                                System.out.println("check");
                            }
                        }
                        if (checkExist==1){
                            toastMessage("User already exists! ");
                        } else {
                            String permissionValues = addInvCbox + "" + editInvCbox + "" + searchInvCbox + "" + addShelvingCbox + "" + editABcBox + "" +
                                    editLayoutCbox + "" + editDeptcBox + "" + viewLayoutCbox + "" + fullAccessCbox;
                            String key = myRef.child("StoreUsers").child(userID).child("Users").push().getKey();
                            myRef.child("StoreUsers").child(userID).child("Users").child(key).child("userPerm").setValue(permissionValues);
                            myRef.child("StoreUsers").child(userID).child("Users").child(key).child("userPass").setValue(userPass.getText().toString());
                            myRef.child("StoreUsers").child(userID).child("Users").child(key).child("userEmail").setValue(userEmail.getText().toString());
                            toastMessage("Added Store User: " + userEmail.getText().toString());
                            Intent intent = new Intent(ManageUserPermissionsAddUser.this, ManageUserPermissionsAddUser.class);
                            sendIntentData(intent);
                        }

                    }
            }
        });

    }



    public void permissionCheckBoxListener(){
        enable_perm_cbox = (CheckBox)findViewById(R.id.enable_perm_cBox);
        enable_perm_cbox.setVisibility(View.INVISIBLE);
    }
    /*=== Genereate Array List & Adapater for user_Options spinner ===*/
    public void generateUserOptions(){
        user_options = (Spinner)findViewById(R.id.user_options);
        ArrayList<String> userOptionList = new ArrayList<>();
        userOptionList.add("Employee Permissions Options");
        userOptionList.add("Add New User");
        userOptionList.add("Edit Current User");
        userOptionList.add("Remove User");

        ArrayAdapter arrayAdapter = new ArrayAdapter(ManageUserPermissionsAddUser.this,android.R.layout.simple_spinner_dropdown_item,userOptionList);
        user_options.setAdapter(arrayAdapter);
        user_options.setSelection(1);
    }
    private void bottomButtonListeners(){
        main_menu_btn = (Button)findViewById(R.id.main_menu_btn);
        back_btn = (Button)findViewById(R.id.back_btn);
        main_menu_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageUserPermissionsAddUser.this,MainMenu.class);
                sendIntentData(intent);
            }
        });
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageUserPermissionsAddUser.this, ManageAccountActivity.class);
                sendIntentData(intent);
            }
        });
    }
    /*== initialize and set cbox listeners==*/
    public void initializeCheckBoxesAndListeners(){
        add_inv_cbox = (CheckBox)findViewById(R.id.add_inv_cbox);
        edit_inv_cbox = (CheckBox)findViewById(R.id.edit_inv_cbox);
        search_inv_cbox = (CheckBox)findViewById(R.id.search_inv_cbox);
        view_layout_cbox = (CheckBox)findViewById(R.id.view_layout_cbox);
        edit_layout_cbox = (CheckBox)findViewById(R.id.edit_layout_cbox);
        add_shelving_cbox = (CheckBox)findViewById(R.id.add_shelving_cbox);
        full_access_cbox = (CheckBox)findViewById(R.id.full_access_cbox);
        edit_dept_cbox = (CheckBox)findViewById(R.id.edit_dept_cbox);
        edit_ab_cbox = (CheckBox)findViewById(R.id.edit_ab_cbox);

        add_inv_cbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    addInvCbox = 1;
                }else {
                    addInvCbox = 0;
                }
            }
        });
        edit_inv_cbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    editInvCbox = 1;
                } else {
                    editInvCbox = 0;
                }
            }
        });
        search_inv_cbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    searchInvCbox = 1;
                } else {
                    searchInvCbox = 0;
                }
            }
        });
        add_shelving_cbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    addShelvingCbox = 1;
                } else {
                    addShelvingCbox = 0;
                }
            }
        });
        view_layout_cbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    viewLayoutCbox=1;
                } else {
                    viewLayoutCbox=0;
                }
            }
        });
        edit_layout_cbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    editLayoutCbox=1;
                } else {
                    editLayoutCbox=0;
                }
            }
        });
        full_access_cbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    fullAccessCbox=1;
                } else {
                    fullAccessCbox=0;
                }
            }
        });
        edit_dept_cbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    editDeptcBox=1;
                } else {
                    editDeptcBox=0;
                }
            }
        });
        edit_ab_cbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    editABcBox=1;
                } else {
                    editABcBox=0;
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
    private void toastMessage(String message) {
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }
}
