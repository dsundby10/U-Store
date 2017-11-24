package accountlogin.registrationapp;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import java.util.List;


public class AisleBaySetup extends AppCompatActivity {
    int dialogChecker = 0;
    LinearLayout advBay_linearLayout, genBay_linearLayout;
    EditText aisle_creation, gen_bay_creation, adv_bay_creation;
    Button aisles_creation_btn, gen_bay_btn, adv_bay_btn, getAssign_Shelving_Btn, getMainMenu_Btn, getView_Layout_Btn;
    ListView listView;
    Spinner getAisle_Spinner;

    List<String> spinnerValues = new ArrayList<String>();
    String aisleChecker = "";
    String bayType = "";
    String regexStr = "^[0-9]*$";

    //Firebase Variables & References
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private DatabaseReference AisleBayRef;
    private DatabaseReference AisleRef;
    private DatabaseReference ShelfRef;
    private String userID;

    //Intent Data Variables
    private String getStoreName = "";
    private String employeeID;
    private String getUserPermissions="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aisle_bay_setup);

        setTitle("Part 2: Aisle & Bay Setup");

        Intent intent = getIntent();
        getStoreName = intent.getStringExtra("STORE_NAME");
        getUserPermissions = intent.getStringExtra("USER_PERMISSIONS");
        employeeID = intent.getStringExtra("STORE_USER");

        listView = (ListView)findViewById(R.id.listViewX);

        //EditTexts
        aisle_creation = (EditText)findViewById(R.id.aisle_creation);
        gen_bay_creation = (EditText)findViewById(R.id.gen_bay_creation);
        adv_bay_creation = (EditText)findViewById(R.id.adv_bay_creation);

        //Buttons
        aisles_creation_btn = (Button)findViewById(R.id.aisle_creation_btn);
        gen_bay_btn = (Button)findViewById(R.id.gen_bay_btn);
        adv_bay_btn = (Button)findViewById(R.id.adv_bay_btn);
        getAssign_Shelving_Btn = (Button)findViewById(R.id.Assign_Shelving_Btn);
        getMainMenu_Btn = (Button)findViewById(R.id.Add_Products_Btn);
        getView_Layout_Btn = (Button)findViewById(R.id.View_Layout_Btn);

        //Spinner Initialization
        getAisle_Spinner = (Spinner) findViewById(R.id.aisle_num_spinner);

        //Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        AisleRef = mFirebaseDatabase.getReference();
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

        generateListView();

        aisles_creation_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!aisleChecker.equals("None")) {
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(aisles_creation_btn.getContext(), android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(aisles_creation_btn.getContext());
                    }
                    builder.setTitle("Aisle Data Already Exists!")
                            .setMessage("Updating your number of Aisles will delete all Aisle/Bay/Shelf data, are you sure you want to proceed?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String aisle = aisle_creation.getText().toString();
                                    if (aisle.trim().matches(regexStr) && aisle.trim().length() >= 1) {
                                        /*=== assign aisles to the database ==== */
                                        AisleRef.child("aisles").setValue(aisle);
                                        AisleBayRef.removeValue();
                                        ShelfRef.removeValue();
                                        AisleBayRef.child(userID).child("BaySetup").push().getKey();
                                        int abCount = 1;
                                        for (int i = 1; i <= Integer.parseInt(aisle); i++) {
                                            AisleBayRef.child("AisleBay" + abCount).child("aisle").setValue(String.valueOf(i));
                                            AisleBayRef.child("AisleBay" + abCount).child("bays").setValue(String.valueOf(0));
                                            abCount++;
                                        }
                                    } else {
                                        toastMessage("Your entry must be a valid integer!");
                                    }
                                }
                            })
                            /*==== dont delete data ===*/
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialogChecker = 2;
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                } else { /*====Don't have any Aisle info in database yet ===*/
                    String aisle = aisle_creation.getText().toString();
                    if (aisle.trim().matches(regexStr) && aisle.trim().length() >= 1) {
                        /*=== assign aisles to the database ==== */
                        int abCount = 1;
                        AisleRef.child("aisles").setValue(aisle);
                        for (int i = 1; i <= Integer.parseInt(aisle); i++) {
                            AisleBayRef.child("AisleBay" + abCount).child("aisle").setValue(String.valueOf(i));
                            AisleBayRef.child("AisleBay" + abCount).child("bays").setValue(String.valueOf(0));
                            abCount++;
                        }
                    } else {
                        toastMessage("Your entry must be a valid integer!");
                    }
                }
            }
        });

        adv_bay_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String advBayText = adv_bay_creation.getText().toString();
                String curAisle = getAisle_Spinner.getSelectedItem().toString();
                int abCount = Integer.parseInt(curAisle);

                //Validating users input
                if (advBayText.trim().matches(regexStr) && advBayText.trim().length() >= 1) {

                    int newCount = 1;
                    //If General Bay Setup has been attempted ... Clear all Shelf data & update BayType Value.
                    if (bayType.equals("Gen")) {
                        myRef.child(userID).child("ShelfSetup").removeValue();
                        myRef.child(userID).child("BayType").setValue("Adv");
                        myRef.child(userID).child("BaySetup").removeValue();

                     /*Auto-Generate a fresh BaySetup in Database*/
                        for (int i = 1; i <= Integer.parseInt(aisleChecker); i++) {
                            AisleBayRef.child("AisleBay" + newCount).child("aisle").setValue(String.valueOf(i));
                            AisleBayRef.child("AisleBay" + newCount).child("bays").setValue(String.valueOf(0));
                            newCount++;
                        }
                    }

                    /*Update the BaySetup in the Database with current aisle & bay submitted*/
                    AisleBayRef.child("AisleBay" + abCount).child("aisle").setValue(String.valueOf(abCount));
                    AisleBayRef.child("AisleBay" + abCount).child("bays").setValue(advBayText);


                    //Set letter placers for easy readability in database
                    String alphabet = "abcdefghijklmnopqrstuvwxyz";
                    String a = "";
                    for (int i = 1; i <= Integer.parseInt(aisleChecker); i++) {
                        String[] alphabetSplitter = alphabet.split("");
                        a = alphabetSplitter[i].toString();
                        if (i == Integer.parseInt(curAisle)) {
                            for (int j = 1; j <= Integer.parseInt(advBayText); j++) {
                                //Auto Generate ShelfSetup in Database
                                myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).setValue(String.valueOf(j));
                                myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child("aisle_num").setValue(String.valueOf(i));
                                myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child("bay_num").setValue(String.valueOf(j));
                                myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child("num_of_shelves").setValue(String.valueOf(0));
                            }
                        }
                    }
                } else{
                    toastMessage("Your entry must be a valid integer!");
                }
            }
        });

        gen_bay_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String genBayText = gen_bay_creation.getText().toString();
                int abCount = 1;
                //Validating users input
                if (genBayText.trim().matches(regexStr) && genBayText.trim().length() >= 1) {

                    //If Adv bays have been assigned remove the ShelfSetup Values to avoid data issues
                    if (bayType.equals("Adv")) {
                        myRef.child(userID).child("ShelfSetup").removeValue();
                    }

                    /*Auto-Generate a fresh BaySetup in Database with same num of bays*/
                    for (int i = 1; i <= Integer.parseInt(aisleChecker); i++) {
                        AisleBayRef.child("AisleBay" + abCount).child("aisle").setValue(String.valueOf(i));
                        AisleBayRef.child("AisleBay" + abCount).child("bays").setValue(genBayText);
                        abCount++;
                    }

                    //Add a "BayType" to Database to help control the values (if they need to be deleted or not if user tries to use both adv & gen)
                    myRef.child(userID).child("BayType").push();
                    myRef.child(userID).child("BayType").setValue("Gen");

                    //Set letter placers for easy readability in database
                    String alphabet = "abcdefghijklmnopqrstuvwxyz";
                    String a = "";
                    for (int i = 1; i <= Integer.parseInt(aisleChecker); i++) {
                        String[] alphabetSplitter = alphabet.split("");
                        a = alphabetSplitter[i].toString();

                        for (int j = 1; j <= Integer.parseInt(genBayText); j++) {
                            //Auto Generate ShelfSetup in Database
                            myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).setValue(String.valueOf(j));
                            myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child("aisle_num").setValue(String.valueOf(i));
                            myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child("bay_num").setValue(String.valueOf(j));
                            myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child("num_of_shelves").setValue(String.valueOf(0));
                        }
                    }
                } else {
                    toastMessage("Your entry must a be a valid integer!");
                }
            }
        });
        /*== Database Listener for Aisles & Bay Type ==*/
        AisleRef = mFirebaseDatabase.getReference().child(userID);
        AisleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                bayType = "0";
                aisleChecker = "None";
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    if (data.getKey().equals("aisles")) {
                        aisleChecker = data.getValue().toString();
                        aisle_creation.setText(aisleChecker);
                        createSpinner();
                    }
                    if (data.getKey().equals("BayType")){
                        bayType = data.getValue().toString();
                        generateListView();
                    }
                    //Hide or show the adv/gen bay setup
                    determineBottomLayoutVisibility();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        /*== Bay Setup Database Listener ==*/
        AisleBayRef = mFirebaseDatabase.getReference().child(userID).child("BaySetup");
        AisleBayRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    String advAisle = (String) data.child("aisle").getValue();
                    String advBay = (String) data.child("bays").getValue();
                    if (advAisle!=null && advBay!=null){

                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        /*== ShelfSetup Database Listener ==*/
        ShelfRef = mFirebaseDatabase.getReference().child(userID).child("ShelfSetup");
        ShelfRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*----AssignShelving Btn Listener------*/
        getAssign_Shelving_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AisleBaySetup.this, ShelvingSetup.class);
                sendIntentData(intent);
            }
        });
        /*----Add Product Btn Listener ----*/
        getMainMenu_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AisleBaySetup.this, MainMenu.class);
                sendIntentData(intent);
            }
        });
        /*---- View Layout Btn Listener ----*/
        getView_Layout_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AisleBaySetup.this, StoreLayoutActivity.class);
                sendIntentData(intent);
            }
        });
    }

    public void generateListView(){
        listView = (ListView)findViewById(R.id.listViewX);

        AisleBayRef = mFirebaseDatabase.getReference().child(userID).child("BaySetup");
        AisleBayRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               ArrayList<String> ABarrayList = new ArrayList<String>();
                if (!dataSnapshot.exists()) {
                    return;
                }
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    String a = "";
                    String b = "";
                    String spacer = "\t\t\t\t\t";
                    if (data.child("bays").exists()){
                         a = data.child("aisle").getValue().toString();
                         b = data.child("bays").getValue().toString();
                         ABarrayList.add("\t\t\tAisle: " + a + spacer + " Bays: " + b);
                    } else {
                        ABarrayList.add("Nothing");
                    }
                }
                ArrayAdapter arrayAdapter = new ArrayAdapter(AisleBaySetup.this,android.R.layout.simple_list_item_1,ABarrayList);
                listView.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void determineBottomLayoutVisibility(){
        if (aisleChecker.equals("None")){
            hideBottomLayout();
        } else {
            showBottomLayout();
        }
    }
    public void hideBottomLayout(){
        advBay_linearLayout = (LinearLayout)findViewById(R.id.advBay_linearLayout);
        genBay_linearLayout = (LinearLayout)findViewById(R.id.genBay_linearLayout);
        advBay_linearLayout.setVisibility(View.INVISIBLE);
        genBay_linearLayout.setVisibility(View.INVISIBLE);
    }
    public void showBottomLayout(){
        advBay_linearLayout = (LinearLayout)findViewById(R.id.advBay_linearLayout);
        genBay_linearLayout = (LinearLayout)findViewById(R.id.genBay_linearLayout);
        advBay_linearLayout.setVisibility(View.VISIBLE);
        genBay_linearLayout.setVisibility(View.VISIBLE);
    }
    public void sendIntentData(Intent intent){
        intent.putExtra("STORE_USER", employeeID);
        intent.putExtra("STORE_NAME", getStoreName);
        intent.putExtra("USER_PERMISSIONS", getUserPermissions);
        startActivity(intent);
    }

    /*=== Generate Aisle Spinner for Advanced Bay Setup ===*/
    public void createSpinner() {
        getAisle_Spinner = (Spinner) findViewById(R.id.aisle_num_spinner);
        spinnerValues = new ArrayList<>(); //ensures spinner values wont duplicate
        if (Integer.parseInt(aisleChecker) < 0) {
            Log.i("Checking Aisle in DB: ", "There's no value!");
        } else { //Generate Spinner
            for (int i = 1; i <= Integer.parseInt(aisleChecker); i++) {
                String x = String.valueOf(i);
                spinnerValues.add(x);
            }
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                    (this, android.R.layout.simple_spinner_item, spinnerValues);
            dataAdapter.setDropDownViewResource
                    (android.R.layout.simple_spinner_dropdown_item);
            getAisle_Spinner.setAdapter(dataAdapter);
        }
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


