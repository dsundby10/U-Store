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
import android.widget.LinearLayout;
import android.widget.ListView;
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

public class ManageUserPermissionsMainPage extends AppCompatActivity {
    Spinner user_options;
    CheckBox enable_perm_cbox;
    LinearLayout mainPageLinearLayout, listViewLinearLayout;
    ListView listViewX;
    TextView unique_password;
    Button gen_pass_btn, main_menu_btn, back_btn;

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
    private DatabaseReference PermListener;
    private DatabaseReference StoreInfo;
    private String userID;
    //Intent Data Variables
    private String getStoreName = "";
    private String employeeID;
    private String getUserPermissions="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_user_permissions_main_page);

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

        unique_password = (TextView)findViewById(R.id.unique_password);
        mainPageLinearLayout = (LinearLayout)findViewById(R.id.mainPageLinearLayout);
        listViewLinearLayout = (LinearLayout)findViewById(R.id.listViewLinearLayout);
        mainPageLinearLayout.setVisibility(View.INVISIBLE);
        listViewLinearLayout.setVisibility(View.INVISIBLE);

        //Main Page & Go Back Listeners
        bottomButtonListeners();

        //Permission enabled listener
        permissionCheckBoxListener();

        /*==Generate User Options Spinner && Listener ==*/
        user_options = (Spinner)findViewById(R.id.user_options);
        user_options.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (userPermissionsEnabled==1){

                    if (user_options.getSelectedItemPosition()==0) {
                        listViewLinearLayout.setVisibility(View.VISIBLE);
                        mainPageLinearLayout.setVisibility(View.VISIBLE);
                    }
                    if (user_options.getSelectedItemPosition()==1){
                        Intent intent = new Intent(ManageUserPermissionsMainPage.this, ManageUserPermissionsAddUser.class);
                        sendIntentData(intent);

                    }
                    if (user_options.getSelectedItemPosition()==2) {
                        Intent intent = new Intent(ManageUserPermissionsMainPage.this, ManageUserPermissionsEditUser.class);
                        sendIntentData(intent);
                    }
                    if (user_options.getSelectedItemPosition()==3){
                        Intent intent = new Intent(ManageUserPermissionsMainPage.this, ManageUserPermissionsDeleteUser.class);
                        sendIntentData(intent);
                    }
                } else {
                    listViewLinearLayout.setVisibility(View.INVISIBLE);
                    mainPageLinearLayout.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        /*=== Check if user permission is enabled & set checkbox accordingly ===*/
        PermListener = mFirebaseDatabase.getReference().child(userID);
        PermListener.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    if (data.getKey().equals("Permissions")){
                        String x = data.getValue().toString();
                        if (x.equals("1")){
                            enable_perm_cbox.setChecked(true);
                        } else {
                            enable_perm_cbox.setChecked(false);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
                printCurrentList();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        /*=== Handle Generate new store unique password ===*/
        StoreInfo = mFirebaseDatabase.getReference().child("StoreUsers").child(userID);
        StoreInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data: dataSnapshot.getChildren()){
                    if (data.getKey().equals("StoreUniquePass")){
                        unique_password.setText(data.getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        generateUniquePass();

    }
    public void generateUniquePass(){
        unique_password = (TextView)findViewById(R.id.unique_password);
        gen_pass_btn = (Button)findViewById(R.id.gen_pass_btn);

        gen_pass_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String genPassKey = myRef.child("StoreUsers").child(userID).child("StoreUniquePass").push().getKey();
                myRef.child("StoreUsers").child(userID).child("StoreUniquePass").setValue(genPassKey);
                unique_password.setText(genPassKey);
            }
        });


    }

    public void printCurrentList(){
        listViewX = (ListView)findViewById(R.id.listViewX);
        currentUserList = new ArrayList<>();
        for (int i = 0; i < storeUserEmail.size() ; i++) {
            currentUserList.add("\tUsername: " + storeUserEmail.get(i) + "\nPassword: " + storeUserPass.get(i));
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter(ManageUserPermissionsMainPage.this, R.layout.custom_listview_layout, currentUserList);
        listViewX.setAdapter(arrayAdapter);
    }

    public void permissionCheckBoxListener(){
        enable_perm_cbox = (CheckBox)findViewById(R.id.enable_perm_cBox);
        enable_perm_cbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    userPermissionsEnabled=1;
                    myRef.child(userID).child("Permissions").setValue(String.valueOf(userPermissionsEnabled));
                    listViewLinearLayout.setVisibility(View.VISIBLE);
                    mainPageLinearLayout.setVisibility(View.VISIBLE);
                    user_options.setEnabled(true);
                    generateUserOptions();
                } else {
                    userPermissionsEnabled=0;
                    listViewLinearLayout.setVisibility(View.INVISIBLE);
                    mainPageLinearLayout.setVisibility(View.INVISIBLE);
                    user_options.setEnabled(false);
                    myRef.child(userID).child("Permissions").setValue(String.valueOf(userPermissionsEnabled));
                }
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

        ArrayAdapter arrayAdapter = new ArrayAdapter(ManageUserPermissionsMainPage.this, R.layout.custom_spinner_layout,userOptionList);
        user_options.setAdapter(arrayAdapter);
        user_options.setSelection(0);
    }
    private void bottomButtonListeners(){
        main_menu_btn = (Button)findViewById(R.id.main_menu_btn);
        back_btn = (Button)findViewById(R.id.back_btn);
        main_menu_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageUserPermissionsMainPage.this,MainMenu.class);
                sendIntentData(intent);
            }
        });
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageUserPermissionsMainPage.this, ManageAccountActivity.class);
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
