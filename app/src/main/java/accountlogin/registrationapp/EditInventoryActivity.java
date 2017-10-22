package accountlogin.registrationapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EditInventoryActivity extends AppCompatActivity {
    Spinner product_spinner;
    //Firebase Variables
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    //Database References
    private DatabaseReference myRef;
    private DatabaseReference ProductRef;
    private String userID;

    ArrayList<String> productChecker = new ArrayList<>();
    String productString = "";
    String currentProduct = "";
    ArrayList<String> product_spinnerValues = new ArrayList<>();
    ArrayList<String> currentProductInfo = new ArrayList<>();
    String[] productArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_inventory);
        Intent intent = getIntent();

        product_spinner = (Spinner)findViewById(R.id.product_spinner);

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

                }
            }
        };
          /*==== Product listener - pulling all the stores product info and forming it into an arrayList ====*/
        ProductRef = mFirebaseDatabase.getReference().child(userID).child("Products");
        ProductRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productChecker = new ArrayList<String>();
                productString = "";
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String p_name = data.child("P_Name").getValue().toString();
                    productString = p_name + "¿";
                    productChecker.add(productString);

                }
                createProductSpinner();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        product_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String currentProductSpinner = product_spinner.getSelectedItem().toString();
                for (int i = 0; i < productChecker.size() ; i++) {
                    if (currentProductSpinner.equals(productChecker.get(i))){
                        Log.i("Match", productChecker.get(i));
                    }

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void createProductSpinner(){
        product_spinner = (Spinner)findViewById(R.id.product_spinner);
        String productHold = "";
        ArrayList<String> productSpin = new ArrayList<>();
        /*====Stopped here -- Redo this setup -- */
        String[] productArr;
        for (int i = 0; i <productChecker.size(); i++) {
            productHold += productChecker.get(i);
        }
        productArr = productHold.split("¿");
        for (int i = 0; i < productArr.length ; i++) {
            productSpin.add(productArr[i]);
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,  productSpin);
        dataAdapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);
        product_spinner.setAdapter(dataAdapter);
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
}
