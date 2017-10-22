package accountlogin.registrationapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

public class AisleBaySetup extends AppCompatActivity {
    private static final String TAG = "Information: ";
    EditText aisle_creation, gen_bay_creation, adv_bay_creation;
    Button aisles_creation_btn, gen_bay_btn, adv_bay_btn, getAssign_Shelving_Btn, getMainMenu_Btn, getView_Layout_Btn;
    ListView listView;
    Spinner getAisle_Spinner;

    List<String> spinnerValues = new ArrayList<String>();
    ArrayList<String> listViewArray = new ArrayList<>();
    ArrayList<String> currentAisleBayShelfList = new ArrayList<>();

    static int aislesInt =0;
    static int genBayInt=0;
    static int currentSpinnerInt=0;
    int aislebayshelfChecker = 0;

    String regexStr;

    String aisleChecker = "";
    String genBayChecker = "";
    String advBayCounter ="";
    ArrayList<String> advBayCounterList = new ArrayList<>();

    //add firebase variables
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private DatabaseReference AisleBayRef;
    private DatabaseReference AisleRef;
    private String userID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aisle_bay_setup);

        //Prevents keyboard from auto popping up
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Intent intent = getIntent();

       // listView = (ListView)findViewById(R.id.listView);

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

        //Input checker to verify only integers have been entered
        regexStr  = "^[0-9]*$";

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                }
            }
        };
        /*======= Aisle DB Reference =======*/
        AisleRef = mFirebaseDatabase.getReference().child(userID);
        AisleRef.orderByChild("aisles").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                genBayChecker = "A";
                aislebayshelfChecker = 0;
                aisleChecker = "B";

                currentAisleBayShelfList = new ArrayList<String>();
                aisle_creation = (EditText)findViewById(R.id.aisle_creation);
                for(DataSnapshot data: dataSnapshot.getChildren()) {

                    /*=== If User has an Existing Aisle Entry in the Database ===*/
                    if (data.getKey().equals("aisles") && !data.getValue().toString().trim().equals("")) {
                        aisleChecker = data.getValue().toString();
                        aisle_creation.setText(data.getValue().toString());
                        //Change Visibility for Gen & Adv
                        gen_bay_btn.setVisibility(View.VISIBLE);
                        adv_bay_btn.setVisibility(View.VISIBLE);
                        getAssign_Shelving_Btn.setVisibility(View.VISIBLE);
                        getView_Layout_Btn.setVisibility(View.VISIBLE);
                        getMainMenu_Btn.setVisibility(View.VISIBLE);
                        //Generate Spinner Values
                        createSpinner();
                        Log.i("Check Aisle Max: ", aisleChecker);
                    }
                    if (data.getKey().equals("GenBay") && !data.getValue().toString().trim().equals("")) {
                        genBayChecker = data.getValue().toString();
                        Log.i("Check genBayChecker: ", genBayChecker +" Real Data " + data.getValue().toString());
                    }
                    if (data.getKey().equals("ShelfSetup")) {
                        Log.i("Check Shelf: ", data.getKey() + " Val: " + data.getValue().toString());
                        currentAisleBayShelfList.add(data.getKey());
                        aislebayshelfChecker = 1;
                    }
                    if (data.getKey().equals("AdvBayCounter") && !data.getValue().toString().trim().equals("")) {
                        advBayCounter = data.getValue().toString();
                       // Log.i("AdvBayCounter: " , advBayCounter);
                    }
                }
                if (aisleChecker.equals("B")){
                    gen_bay_btn.setVisibility(View.GONE);
                    adv_bay_btn.setVisibility(View.GONE);
                    getAssign_Shelving_Btn.setVisibility(View.GONE);
                    getView_Layout_Btn.setVisibility(View.GONE);
                    getMainMenu_Btn.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*======== AisleBay DB Reference =========*/
        AisleBayRef = mFirebaseDatabase.getReference().child(userID).child("BaySetup");
        AisleBayRef.orderByChild("BaySetup").addValueEventListener(new ValueEventListener() {
            String bayHold = "";
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listViewArray = new ArrayList<String>();
                for(DataSnapshot data: dataSnapshot.getChildren()) {
                        bayHold = data.getValue().toString();
                        //listViewArray.add(bayHold); //will be used for the listView
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
       //ArrayAdapter arrayAdapter = new ArrayAdapter(AisleBaySetup.this,android.R.layout.simple_list_item_1,listViewArray);
       //listView.setAdapter(arrayAdapter);


        /*---------Aisles Button Listener----*/
        aisles_creation_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String aisle = aisle_creation.getText().toString();
                if (aisle.trim().matches(regexStr)){
                    aislesInt=0;
                    aislesInt = Integer.parseInt(aisle);
                     /*=== assign aisles to the database ==== */
                    myRef.child(userID).child("aisles").setValue(aisle);
                    int counter = 0;
                    for (int i = 0; i < aislesInt; i++) {
                        if (i == 0) { //Remove all Preexisting data entries for AisleBays & ShelfSetup
                            myRef.child(userID).child("BaySetup").removeValue();
                            myRef.child(userID).child("ShelfSetup").removeValue();
                            myRef.child(userID).child("GenBay").removeValue();
                        }
                        myRef.child(userID).child("BaySetup").child("AisleBays" + counter).child("aisle").setValue(String.valueOf(i));
                        myRef.child(userID).child("BaySetup").child("AisleBays" + counter).child("bays").setValue(String.valueOf(0));
                        counter++;
                    }
                    //Set hint back to original color & text
                    aisle_creation.setHintTextColor(getResources().getColor(R.color.editTextHintColor));
                    aisle_creation.setHint("Number of Aisles in Store");
                } else {
                    //Set hint to standout more
                    aisle_creation.setText("");
                    aisle_creation.setHintTextColor(Color.RED);
                    aisle_creation.setHint("* Must be an Integer");
                }
            }
        });
        /*---------General Bay Btn Listener------------*/
        gen_bay_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Checking to make sure aisles exist within the database first
                if (aisleChecker.trim().length()<1) {
                    aisle_creation.setHintTextColor(Color.RED);
                    aisle_creation.setHint("* Assign Aisles First!");
                } else {
                    genBayInt = 0;
                    String genBay = gen_bay_creation.getText().toString();
                    genBayInt = Integer.parseInt(genBay);

                    //Assign GenBay to database to for value checking in Adv Bay
                    myRef.child(userID).child("GenBay").setValue(genBay);


                    /*======Generate BaySetup In DB =====*/
                    int counter = 0;
                    for (int i = 0; i < Integer.parseInt(aisleChecker); i++) {
                        myRef.child(userID).child("BaySetup").child("AisleBays" + counter).child("aisle").setValue(String.valueOf(i));
                        myRef.child(userID).child("BaySetup").child("AisleBays" + counter).child("bays").setValue(String.valueOf(genBayInt));
                    counter++;
                }
                /*==== Check to see if ShelfSetup Exists, if so Remove it & Recreate ====*/
                if (aislebayshelfChecker == 1){
                    myRef.child(userID).child("ShelfSetup").removeValue();
                }

                /*===== Auto Generate ShelfSetup in the Database ==== */
                    String a = "";
                    String alphabet = "abcdefghijklmnopqrstuvwxyz";

                    for (int i = 0; i < Integer.parseInt(aisleChecker); i++) {
                        String[] alphabetSplitter = alphabet.split("");
                        a = alphabetSplitter[i+1].toString();

                        for (int j = 0; j < genBayInt; j++) {
                            //Auto Generate ShelfSetup in Database
                            myRef.child(userID).child("ShelfSetup").child(a+"AisleID" + j).setValue(String.valueOf(j));
                            myRef.child(userID).child("ShelfSetup").child(a+"AisleID" + j).child("aisle_num").setValue(String.valueOf(i));
                            myRef.child(userID).child("ShelfSetup").child(a+"AisleID" + j).child("bay_num").setValue(String.valueOf(j));
                            myRef.child(userID).child("ShelfSetup").child(a+"AisleID" + j).child("num_of_shelves").setValue(String.valueOf(0));
                        }
                    }

                //Set hint back to original color & text
                aisle_creation.setHintTextColor(getResources().getColor(R.color.editTextHintColor));
                aisle_creation.setText("Number of Aisles in Store");
            }
            }
        });

        /*----------Advanced Bay Btn Listener----------*/
        adv_bay_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Check if theres a different value than the preassigned value of 'A' for the genBay Creation
                 *Meaning the user assigned a general bay value & now is trying to assign advanced values
                 *so we delete all the already generated values and replace them with adv bay values. */

                int counter = 0;
                if (!genBayChecker.equals("A")) {
                    //Remove all Preexisting data entries for AisleBays & ShelfSetup
                    Log.i("genBayChcker VALUE: ",genBayChecker );
                    myRef.child(userID).child("GenBay").removeValue();
                    myRef.child(userID).child("ShelfSetup").removeValue();
                    myRef.child(userID).child("BaySetup").removeValue();

                    //Create an advBayCounter in database to manage flow better
                    myRef.child(userID).child("AdvBayCounter").setValue("A");
                    advBayCounterList = new ArrayList<String>(); // reset the counterList
                    /*======== Generate BaySetup In DB - Reset all bay values to 0==========*/
                    for (int i = 0; i < Integer.parseInt(aisleChecker); i++) {
                        myRef.child(userID).child("BaySetup").child("AisleBays" + counter).child("aisle").setValue(String.valueOf(i));
                        myRef.child(userID).child("BaySetup").child("AisleBays" + counter).child("bays").setValue(String.valueOf(0));
                        counter++;
                    }
                }
                   // String advString = advBayCounter;
                   // Log.i("AdvBayCounter STring ", advString);
                    //Spinner Initialization
                    getAisle_Spinner = (Spinner) findViewById(R.id.aisle_num_spinner);
                    String aisleSpinnerTxt = getAisle_Spinner.getSelectedItem().toString();
                    currentSpinnerInt = Integer.parseInt(aisleSpinnerTxt);
                    //EditText value
                    String advBay = adv_bay_creation.getText().toString();
                    /* Assigning a single bay value to a single aisle value in the DB to "BaySetup" */
                    myRef.child(userID).child("BaySetup").child("AisleBays" + currentSpinnerInt).child("aisle").setValue(aisleSpinnerTxt);
                    myRef.child(userID).child("BaySetup").child("AisleBays" + currentSpinnerInt).child("bays").setValue(advBay);
                   // myRef.child(userID).child("AdvBayCounter").setValue(String.valueOf(currentSpinnerInt));
                    String myADV = advBayCounter;

                /*Come back to Later -- Trying to make it so they have to complete bay setup to advance to mainmenu / shelving*/
             //  advBayCounterList.add(currentSpinnerInt, advBayCounter);
               // for (int i = 0; i < advBayCounterList.size(); i++) {
               //     Log.i("AdvBayCounter List " , advBayCounterList.get(i));
              //  }


                String a = "";
                String alphabet = "abcdefghijklmnopqrstuvwxyz";
                /*==== Auto - Generate Shelfs for this specific Aisle&Bay ====*/
                for (int i = 0; i < Integer.parseInt(aisleSpinnerTxt) + 1; i++) {
                    String[] alphabetSplitter = alphabet.split("");
                    a = alphabetSplitter[i + 1].toString();
                    if (i == Integer.parseInt(aisleSpinnerTxt)) {
                        Log.i("Its equals It equals!!", "kaskd");
                        for (int j = 0; j < Integer.parseInt(advBay); j++) {
                            myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).setValue(String.valueOf(j));
                            myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child("aisle_num").setValue(String.valueOf(i));
                            myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child("bay_num").setValue(String.valueOf(j));
                            myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child("num_of_shelves").setValue(String.valueOf(0));

                        }
                    }
                }
            }
        });
        /*----AssignShelving Btn Listener------*/
        getAssign_Shelving_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(AisleBaySetup.this, ShelvingSetup.class);
                startActivity(intent1);
            }
        });
        /*----Add Product Btn Listener ----*/
        getMainMenu_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(AisleBaySetup.this, MainMenu.class);
                startActivity(intent2);
            }
        });
        getView_Layout_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent3 = new Intent(AisleBaySetup.this, StoreLayoutActivity.class);
                startActivity(intent3);
            }
        });

    }

    public void createSpinner() {
        getAisle_Spinner = (Spinner) findViewById(R.id.aisle_num_spinner);
        spinnerValues = new ArrayList<>(); //ensures spinner values wont duplicate
        if (Integer.parseInt(aisleChecker) < 0) {
            Log.i("Checking Aisle in DB: ", "There's no value!");
        } else { //Generate Spinner
            for (int i = 0; i < Integer.parseInt(aisleChecker); i++) {
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


