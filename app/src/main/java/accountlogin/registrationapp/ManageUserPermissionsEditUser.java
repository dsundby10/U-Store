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

public class ManageUserPermissionsEditUser extends AppCompatActivity {
    LinearLayout checkBoxLinearLayout, editUserLinearLayout;
    Spinner user_options, currentUserSpinner;
    CheckBox add_inv_cbox, edit_inv_cbox, search_inv_cbox, view_layout_cbox,
            edit_layout_cbox, add_shelving_cbox, full_access_cbox, edit_dept_cbox, edit_ab_cbox;
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
    private String userID;

    //Intent Data Variables
    private String getStoreName = "";
    private String employeeID;
    private String getUserPermissions="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_user_permissions_edit_user);

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
        /*--Initialize layouts and Main Menu / Back Button Listeners --*/
        currentUserSpinner = (Spinner) findViewById(R.id.currentUserSpinner);
        userEmail = (EditText)findViewById(R.id.userEmail);
        userPass = (EditText)findViewById(R.id.userPass);
        bottomButtonListeners();
        initializeCheckBoxesAndListeners();

        /*==Generate User Options Spinner && Listener ==*/
        generateUserOptions();
        user_options = (Spinner)findViewById(R.id.user_options);
        user_options.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    if (user_options.getSelectedItemPosition()==0) {
                        Intent intent = new Intent(ManageUserPermissionsEditUser.this, ManageUserPermissionsMainPage.class);
                        sendIntentData(intent);
                    }
                    if (user_options.getSelectedItemPosition()==1){
                        Intent intent = new Intent(ManageUserPermissionsEditUser.this, ManageUserPermissionsAddUser.class);
                        sendIntentData(intent);

                    }
                    if (user_options.getSelectedItemPosition()==2) {

                    }
                    if (user_options.getSelectedItemPosition()==3){
                        Intent intent = new Intent(ManageUserPermissionsEditUser.this, ManageUserPermissionsDeleteUser.class);
                        sendIntentData(intent);
                    }
                }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ManageStore = mFirebaseDatabase.getReference().child("StoreUsers").child(userID).child("Users");
        ManageStore.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                storeUserEmail = new ArrayList<String>();
                storeUserPass = new ArrayList<String>();
                storeUserPerm = new ArrayList<String>();
                storeUserKey = new ArrayList<String>();
                currentUserList = new ArrayList<String>();
                for (DataSnapshot data: dataSnapshot.getChildren()){
                        String PPE = data.getValue().toString();
                        String[] xp = new String[3];
                        xp = PPE.split(",");
                        String perm = xp[0].trim();
                        String pass = xp[1].trim();
                        String email = xp[2].trim();


                        storeUserKey.add(data.getKey());
                        storeUserPerm.add(perm.substring(10, perm.length()));
                        storeUserPass.add(pass.substring(9, pass.length()));
                        storeUserEmail.add(email.substring(10, email.length() - 1));

                }
                /*==Generate values for currrent user spinner==*/
                for (int i = 0; i <storeUserEmail.size(); i++) {
                    currentUserList.add("USER " + i + ": " + storeUserEmail.get(i));
                }
                ArrayAdapter arrayAdapter1 = new ArrayAdapter(ManageUserPermissionsEditUser.this, android.R.layout.simple_spinner_dropdown_item, currentUserList);
                currentUserSpinner.setAdapter(arrayAdapter1);
                currentUserSpinner.setSelection(0);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //Listener for current user spinner .. on item select generate permissions / name / pass
        currentUserSpinnerListener();
        //Edit user button listener -- updates data
        editUserBtnListner();
    }

    public void editUserBtnListner(){
        submit_btn = (Button)findViewById(R.id.submit_btn);
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userEmail.getText().toString().trim().length() < 2 && userPass.getText().toString().trim().length() < 2) {
                    toastMessage("User name & pass must be < 2");
                } else {
                    if (userEmail.getText().toString().contains(",") || userPass.getText().toString().contains(",")) {
                        toastMessage("Your password or email can not contain a comma");
                    } else {
                        String permissionValues = addInvCbox + "" + editInvCbox + "" + searchInvCbox + "" + addShelvingCbox + "" + editABcBox + "" +
                                editLayoutCbox + "" + editDeptcBox + "" + viewLayoutCbox + "" + fullAccessCbox;
                        String idKey = storeUserKey.get(currentUserSpinner.getSelectedItemPosition());
                        myRef.child("StoreUsers").child(userID).child("Users").child(idKey).child("userPerm").setValue(permissionValues);
                        myRef.child("StoreUsers").child(userID).child("Users").child(idKey).child("userEmail").setValue(userEmail.getText().toString());
                        myRef.child("StoreUsers").child(userID).child("Users").child(idKey).child("userPass").setValue(userPass.getText().toString());
                        updateCurrentDatabaseValuesOnClick();
                        toastMessage(userEmail.getText().toString() + " has been updated!");
                        Intent intent = new Intent(ManageUserPermissionsEditUser.this, ManageUserPermissionsEditUser.class);
                        sendIntentData(intent);
                    }
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
                        userPass.setText(storeUserPass.get(i));
                        generateCheckboxValuesForEditUser(i);
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

    /*=== when spinner is selected generate that users permission values ===*/
    public void generateCheckboxValuesForEditUser(int x){
        initializeCheckBoxesAndListeners();
        String permValue = storeUserPerm.get(x).trim();
        String[] arrPerm = permValue.split("");

        if (arrPerm[1].equals("1")){
            add_inv_cbox.setChecked(true);
        } else {
            add_inv_cbox.setChecked(false);
        }
        if (arrPerm[2].equals("1")){
            edit_inv_cbox.setChecked(true);
        } else {
            edit_inv_cbox.setChecked(false);
        }
        if (arrPerm[3].equals("1")){
            search_inv_cbox.setChecked(true);
        } else {
            search_inv_cbox.setChecked(false);
        }
        if (arrPerm[4].equals("1")){
            add_shelving_cbox.setChecked(true);
        } else {
            add_shelving_cbox.setChecked(false);
        }
        if (arrPerm[5].equals("1")){
            edit_ab_cbox.setChecked(true);
        } else {
            edit_ab_cbox.setChecked(false);
        }
        if (arrPerm[6].equals("1")){
            edit_layout_cbox.setChecked(true);
        } else {
            edit_layout_cbox.setChecked(false);
        }
        if (arrPerm[7].equals("1")){
            edit_dept_cbox.setChecked(true);
        } else {
            edit_dept_cbox.setChecked(false);
        }
        if (arrPerm[8].equals("1")){
            view_layout_cbox.setChecked(true);
        } else {
            view_layout_cbox.setChecked(false);
        }
        if (arrPerm[9].equals("1")){
            full_access_cbox.setChecked(true);
        } else {
            full_access_cbox.setChecked(false);
        }
    }



    /*=== Genereate Array List & Adapater for user_Options spinner ===*/
    public void generateUserOptions(){
        user_options = (Spinner)findViewById(R.id.user_options);
        ArrayList<String> userOptionList = new ArrayList<>();
        userOptionList.add("Employee Permissions Options");
        userOptionList.add("Add New User");
        userOptionList.add("Edit Current User");
        userOptionList.add("Remove User");

        ArrayAdapter arrayAdapter = new ArrayAdapter(ManageUserPermissionsEditUser.this,android.R.layout.simple_spinner_dropdown_item,userOptionList);
        user_options.setAdapter(arrayAdapter);
        user_options.setSelection(2);
    }
    private void bottomButtonListeners(){
        main_menu_btn = (Button)findViewById(R.id.main_menu_btn);
        back_btn = (Button)findViewById(R.id.back_btn);
        main_menu_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageUserPermissionsEditUser.this,MainMenu.class);
                sendIntentData(intent);
            }
        });
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageUserPermissionsEditUser.this, ManageAccountActivity.class);
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
    public void updateCurrentDatabaseValuesOnClick(){
        ManageStore = mFirebaseDatabase.getReference().child("StoreUsers").child(userID).child("Users");
        ManageStore.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                storeUserEmail = new ArrayList<String>();
                storeUserPass = new ArrayList<String>();
                storeUserPerm = new ArrayList<String>();
                storeUserKey = new ArrayList<String>();
                currentUserList = new ArrayList<String>();

                for (DataSnapshot data: dataSnapshot.getChildren()){

                    String PPE = data.getValue().toString();

                    //String[] xp = new String[3];
                    String[] xp = PPE.split(",");
                    if (xp.length==2) {
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
                ArrayAdapter arrayAdapter1 = new ArrayAdapter(ManageUserPermissionsEditUser.this, R.layout.custom_spinner_layout, currentUserList);
                currentUserSpinner.setAdapter(arrayAdapter1);
                currentUserSpinner.setSelection(0);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

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
