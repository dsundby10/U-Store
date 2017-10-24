package accountlogin.registrationapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

public class AisleBaySetup extends AppCompatActivity {
    private static final String TAG = "Information: ";
    int dialogChecker = 0;
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


    ArrayList<String> aisleCheckerList = new ArrayList<>();
    ArrayList<String> bayCheckerListz = new ArrayList<String>();
    ArrayList<String> shelfCheckerList = new ArrayList<>();
    ArrayList<String> allABS = new ArrayList<>();


    //Firebase Variables & References
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private DatabaseReference AisleBayRef;
    private DatabaseReference AisleRef;
    private DatabaseReference ShelfRef;
    private String userID;

    ArrayList<String> aisleBayToAdd = new ArrayList<>();
    ArrayList<String> aisleBayToCheck = new ArrayList<>();
    String[] splitter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aisle_bay_setup);
        setTitle("Part 2: Aisle & Bay Setup");
        //Prevents keyboard from auto popping up
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Intent intent = getIntent();

        //Input checker to verify only integers have been entered
        regexStr  = "^[0-9]*$";

        listView = (ListView)findViewById(R.id.listView);

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
        /*======= Aisle DB Reference =======*/
        AisleRef = mFirebaseDatabase.getReference().child(userID);
        AisleRef.orderByChild("aisles").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                genBayChecker = "A";
                aislebayshelfChecker = 0;
                aisleChecker = "B";
                String ValueHold = "";
                String place = "";
                listViewArray = new ArrayList<String>();
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
                    }
                    if (data.getKey().equals("ShelfSetup")) {
                        Log.i("Check Shelf: ", data.getKey() + " Val: " + data.getValue().toString());
                        currentAisleBayShelfList.add(data.getKey());
                        aislebayshelfChecker = 1;
                    }
                    if (data.getKey().equals("AdvBayCounter") && !data.getValue().toString().trim().equals("")) {
                        advBayCounter = data.getValue().toString();
                    }
                    if (data.getKey().equals("BaySetup")&& !data.getValue().toString().trim().equals("")){
                        ValueHold = data.getValue().toString();
                    }
                    place = ValueHold;
                }
                    //Format ValueHold with combineAisleBay method
                    listViewArray = combineAisleBay(place);
                ArrayAdapter arrayAdapter = new ArrayAdapter(AisleBaySetup.this,android.R.layout.simple_list_item_1,listViewArray);
                listView.setAdapter(arrayAdapter);
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
        //AisleBayRef = mFirebaseDatabase.getReference().child(userID).child("BaySetup");
        //AisleBayRef.orderByChild("aisle").addValueEventListener(new ValueEventListener() {
        AisleBayRef = mFirebaseDatabase.getReference().child(userID).child("BaySetup");
        AisleBayRef.addValueEventListener(new ValueEventListener() {
            String spacer = "\t\t\t\t\t\t";
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listViewArray = new ArrayList<String>();
                String aisleHold = "";
                String bayHold = "";
                for(DataSnapshot data: dataSnapshot.getChildren()) {
                    bayHold = data.getValue().toString();
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*---------Aisles Button Listener----*/
        aisles_creation_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ListView listxView = (ListView)findViewById(R.id.listView);
                listView = listxView;
                //String aisle = "";
                dialogChecker=0;
                if (aisleChecker!="B"){
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(aisles_creation_btn.getContext(), android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(aisles_creation_btn.getContext());
                    }
                    builder.setTitle("Aisle Already Exists")
                            .setMessage("Warning! Updating your number of Aisles will delete all Aisle/Bay/Shelf data, are you sure you want to proceed?")
                            /*==== delete data ===*/
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String aisle = aisle_creation.getText().toString();
                                    if (aisle.trim().matches(regexStr)) {
                                        aislesInt = 0;
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
                                       startActivity(new Intent (AisleBaySetup.this,AisleBaySetup.class));
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
                            })
                            /*==== dont delete data ===*/
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialogChecker = 2;
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                    /*====Don't have any Aisle info in database yet ===*/
                } else {
                    String aisle = aisle_creation.getText().toString();
                    if (aisle.trim().matches(regexStr)) {
                        aislesInt = 0;
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
                    ArrayAdapter arrayAdapter = new ArrayAdapter(AisleBaySetup.this,android.R.layout.simple_list_item_1,listViewArray);
                    listView.setAdapter(arrayAdapter);
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
                    ArrayAdapter arrayAdapter = new ArrayAdapter(AisleBaySetup.this,android.R.layout.simple_list_item_1,listViewArray);
                    listView.setAdapter(arrayAdapter);
                }

                    //Spinner Initialization
                    getAisle_Spinner = (Spinner) findViewById(R.id.aisle_num_spinner);
                    final String aisleSpinnerTxt = getAisle_Spinner.getSelectedItem().toString();
                    currentSpinnerInt = Integer.parseInt(aisleSpinnerTxt);
                    //EditText value
                    final String advBay = adv_bay_creation.getText().toString();
                    /* Assigning a single bay value to a single aisle value in the DB to "BaySetup" */
                    myRef.child(userID).child("BaySetup").child("AisleBays" + currentSpinnerInt).child("aisle").setValue(aisleSpinnerTxt);
                    myRef.child(userID).child("BaySetup").child("AisleBays" + currentSpinnerInt).child("bays").setValue(advBay);

                String aHolder = myRef.child(userID).child("BaySetup").child("AisleBays" + currentSpinnerInt).child("aisle").child(aisleSpinnerTxt).toString();
                String bHolder = myRef.child(userID).child("BaySetup").child("AisleBays" + currentSpinnerInt).child("bays").child(advBay).toString();


                aisleBayToAdd = new ArrayList<>();

                String a = "";
                String alphabet = "abcdefghijklmnopqrstuvwxyz";
                /*==== Auto - Generate Shelves for this specific Aisle&Bay ====*/
                for (int i = 0; i < Integer.parseInt(aisleSpinnerTxt) + 1; i++) {
                    String[] alphabetSplitter = alphabet.split("");
                    a = alphabetSplitter[i + 1].toString();
                    if (i == Integer.parseInt(aisleSpinnerTxt)) {
                        for (int j = 0; j < Integer.parseInt(advBay); j++) {
                            myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).setValue(String.valueOf(j));
                            aisleBayToAdd.add(a+"AisleID"+j);
                            myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child("aisle_num").setValue(String.valueOf(i));
                            myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child("bay_num").setValue(String.valueOf(j));
                            myRef.child(userID).child("ShelfSetup").child(a + "AisleID" + j).child("num_of_shelves").setValue(String.valueOf(0));
                        }
                    }
                }
                ArrayAdapter arrayAdapter = new ArrayAdapter(AisleBaySetup.this,android.R.layout.simple_list_item_1,listViewArray);
                listView.setAdapter(arrayAdapter);
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
    public ArrayList<String> combineAisleBay (String ABstring){
        String newString = ABstring.replaceAll("[a-zA-Z]","");
        String newestString = newString.replaceAll("[^a-zA-Z0-9]","");
        String spacing = "\t\t\t\t\t";
        ArrayList<String>tempbay = new ArrayList<>();
        ArrayList<String>tempaisle = new ArrayList<>();

        ArrayList<String>holdAisle = new ArrayList<>();
        System.out.println("SXCnew " +newestString);
        String aisleNew= "";
        String bayNew= "";
        String combine = "";
        int x = 0;
        int y = 1;
        int z = 2;
        String[] arrHold = new String[newestString.length()/3];
        for (int i = 0; i < newestString.length()/3 ; i++) {
            aisleNew = String.valueOf(newestString.charAt(z));
            bayNew = String.valueOf(newestString.charAt(y));
            x+=3;
            y+=3;
            z+=3;
            combine =  spacing + "Aisles: " + aisleNew + spacing +"Bays: " + bayNew;
            int checker = Integer.parseInt(aisleNew);
            arrHold[checker] = combine;

        }
        for (int i = 0; i < arrHold.length; i++) {
            holdAisle.add(arrHold[i]);
        }

        return holdAisle;
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


