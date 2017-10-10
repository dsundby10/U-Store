package accountlogin.registrationapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class AisleBaySetup extends AppCompatActivity {
    private static final String TAG = "Information: ";
    EditText aisle_creation, gen_bay_creation, adv_bay_creation;
    Button aisles_creation_btn, gen_bay_btn, adv_bay_btn, getAssign_Shelving_Btn, getAdd_Products_Btn, getView_Layout_Btn;
    TextView displayCurrentAisles , tvScrollView;

    Spinner getAisle_Spinner;
    List<String> spinnerValues = new ArrayList<String>();
    static List<String> advArrayAssign = new ArrayList<String>();
    static int advCounter = 0;
    static int aislesInt =0;
    static int genBayInt=0;
    static int advBayInt=0;
    static int currentSpinnerInt=0;
    static String advBayList;
    static String advBayString;
    static String tvScrollViewList;

    //add firebase variables
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aisle_bay_setup);

        final Intent intent = getIntent();

        //TextView for ScrolView
        tvScrollView = (TextView) findViewById(R.id.tvScrollView);

        //EditTexts
        aisle_creation = (EditText)findViewById(R.id.aisle_creation);
        gen_bay_creation = (EditText)findViewById(R.id.gen_bay_creation);
        adv_bay_creation = (EditText)findViewById(R.id.adv_bay_creation);

        //Buttons
        aisles_creation_btn = (Button)findViewById(R.id.aisle_creation_btn);
        gen_bay_btn = (Button)findViewById(R.id.gen_bay_btn);
        adv_bay_btn = (Button)findViewById(R.id.adv_bay_btn);
        getAssign_Shelving_Btn = (Button)findViewById(R.id.Assign_Shelving_Btn);
        getAdd_Products_Btn = (Button)findViewById(R.id.Add_Products_Btn);

        //Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        final FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    //startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            }
        };
        /*---------Aisles Button Listener----*/
        aisles_creation_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aislesInt=0;
                String aisle = aisle_creation.getText().toString();
                aislesInt = Integer.parseInt(aisle);
                Log.d("OnClick - A&B Setup", "\nAisles: " + aisle);

                if (!aisle.equals("")){
                    myRef.child(userID).child("aisles").setValue(aisle);
                    createSpinner(); //load spinner up with 'x' amounts of values
                    advBayList = ""; //reset the advBayList

                }else{
                    toastMessage("Aisle Input Must be a positive integer.");
                }
            }
        });
        /*---------General Bay Btn Listener------------*/
        gen_bay_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                genBayInt=0;
                String genBay = gen_bay_creation.getText().toString();
                genBayInt = Integer.parseInt(genBay);
                Log.d("OnClick GEN - A&B Setup", "\nGenBay" + genBay);
                if (!genBay.equals("")){
                    myRef.child(userID).child("genBays").setValue(genBay);

                }else{
                    toastMessage("Gen Bay Input Must be an Integer <= 0");
                }

            }
        });
        /*----------Advanced Bay Btn Listener----------*/
        adv_bay_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Spinner Initialization
                getAisle_Spinner = (Spinner)findViewById(R.id.aisle_num_spinner);
                String spinnerTxt = getAisle_Spinner.getSelectedItem().toString();
                currentSpinnerInt = Integer.parseInt(spinnerTxt);
                String tvSV = "";
                String advBay = adv_bay_creation.getText().toString();
                String temp = advBay + ", ";
                advBayList += temp;

                if (advBayList.charAt(0) == 'n' || advBayList.charAt(1) == 'u') {
                    advBayList = advBayList.replace("n","");
                    advBayList = advBayList.replace("u","");
                    advBayList = advBayList.replace("l","");
                    advBayList = advBayList.replace("l","");

                }else{
                }
                advBayString = advBay;
               /* getBtnCheck(); */
                advArrayAssign.add(advCounter,spinnerTxt);
                advCounter++;
                advArrayAssign.add(advCounter,advBay);

                advBayInt = Integer.parseInt(advBay);
                tvSV = "Aisle: " + spinnerTxt + " Bay: " + advBayString;

                Log.d("OnClick ADV - A&B Setup", "\nAdvBay " + advBay);
                if (!advBay.equals("")){
                    myRef.child(userID).child("advBays").setValue(advBayList);
                    String tempStr = tvSV + "\n";
                    tvScrollViewList+=tempStr;

                    tvScrollView.setText(tvScrollViewList);
                }else{
                    tvSV="";
                    toastMessage("Adv Bay Input Must be an integer <= 0");
                }
                advCounter++;
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
        getAdd_Products_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(AisleBaySetup.this, AddInventory.class);
                startActivity(intent2);
            }
        });

    }
   /* public void getBtnCheck () {
        int count = 0;
        ListIterator<String> itr = advArrayAssign.listIterator();
        String strElement = "";
        while (itr.hasNext()) {
            count++;
            strElement = itr.next();
            int z = Integer.parseInt(strElement);
            if (z%2 == 0 && z == currentSpinnerInt){
                String zz = itr.next();
                itr.set(advBayString);
                toastMessage(".... " + zz);
            }
        }
    }*/
    public void createSpinner() {
        getAisle_Spinner = (Spinner)findViewById(R.id.aisle_num_spinner);
        spinnerValues = new ArrayList<>(); //ensures spinner values wont duplicate
        for (int i = 0; i < aislesInt; i++) {
            String x = String.valueOf(i);
            spinnerValues.add(x);
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item, spinnerValues);
        dataAdapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);
        getAisle_Spinner.setAdapter(dataAdapter);
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
