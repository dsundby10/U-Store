package accountlogin.registrationapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;



public class AddInventory extends AppCompatActivity {
    private static final int RESULT_LOAD_IMAGE = 1;
    static List<String> arrMaxAisles = new ArrayList<>(); //place holder for the Array
    static List<String> arrMaxBays = new ArrayList<>();
    static int MAXAISLES = 0;
    static String DEPTNAMES = "";
    static String[] ARR_DEPTNAMES;
    static String BAY_NUMBERS="";
    static String[] ARR_BAY_NUMBERS;
    static int productCounter=0;
    EditText product_name, num_stock, product_id, product_desc;
    Spinner dept_spinner, aisle_spinner, bay_spinner, shelf_spinner;
    Button add_product_btn, take_image_btn, upload_image_btn;
    ImageView imageView;

    //Firebase Variables
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private DatabaseReference AisleBayShelfRef;
    private DatabaseReference AisleBayRef;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_inventory);
        final Intent intent = getIntent();

         imageView = (ImageView)findViewById(R.id.imageView);
        //Variable initialization
        dept_spinner = (Spinner)findViewById(R.id.dept_spinner);
        aisle_spinner = (Spinner)findViewById(R.id.aisle_spinner);
        bay_spinner = (Spinner)findViewById(R.id.bay_spinner);
        shelf_spinner = (Spinner)findViewById(R.id.shelf_spinner);

        product_name = (EditText)findViewById(R.id.product_name);
        num_stock = (EditText)findViewById(R.id.num_stock);
        product_id = (EditText)findViewById(R.id.product_id);
        product_desc = (EditText)findViewById(R.id.product_desc);

        add_product_btn = (Button)findViewById(R.id.add_product_btn);
        take_image_btn = (Button)findViewById(R.id.take_image_btn);
        upload_image_btn = (Button)findViewById(R.id.upload_image_btn);

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

        /*============Get Max Aisles & All Dept Names===========*/
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            ArrayList<String> DeptNamesList = new ArrayList<String>();
            zAllUserData zInfo = new zAllUserData();
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                zInfo.setNumAisles(dataSnapshot.child(userID).child("aisles").getValue(String.class));
                MAXAISLES = Integer.parseInt(zInfo.getNumAisles());
                Log.i("MAXAISLES ", MAXAISLES + "");

                zInfo.setDeptNames(dataSnapshot.child(userID).child("deptNames").getValue(String.class));
                DEPTNAMES = zInfo.getDeptNames();
                ARR_DEPTNAMES = DEPTNAMES.split("\\s*,\\s*");
                Log.i("MAXDEPTS ", DEPTNAMES);
                String deptHolder = "";
                for (int i = 0; i < ARR_DEPTNAMES.length ; i++) {
                    deptHolder = ARR_DEPTNAMES[i].toString();
                    DeptNamesList.add(deptHolder);
                }
                ArrayAdapter deptAdp = new ArrayAdapter(AddInventory.this,android.R.layout.simple_spinner_dropdown_item, DeptNamesList);
                dept_spinner.setAdapter(deptAdp);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        AisleBayRef = mFirebaseDatabase.getReference().child(userID).child("BaySetup");
        AisleBayRef.addListenerForSingleValueEvent(new ValueEventListener() {
            ArrayList<String>advAarr = new ArrayList<>();
            ArrayList<String>advBarr = new ArrayList<>();
            ArrayList<String> arrayz = new ArrayList<>();
            String totalNumBays="";
            String totalNumAisles = "";
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                arrMaxAisles = new ArrayList<String>();
                //try array creation here
                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {
                    String advAisle = (String) childSnapShot.child("aisle").getValue();
                    String advBay = (String) childSnapShot.child("bays").getValue();
                    Log.i("zCheckCheck: ", advAisle + " " + advBay);

                    String advA = "ADVAisle: " + advAisle + "ADVBays: " + advBay;
                    arrayz.add(advA);
                    arrMaxAisles.add(advAisle);
                    arrMaxBays.add(advBay);
                    BAY_NUMBERS += advBay;
                }
                Log.i("advBarrSize", " " + BAY_NUMBERS);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        AisleBayShelfRef = mFirebaseDatabase.getReference().child(userID).child("ShelfSetup");
        AisleBayShelfRef.addValueEventListener(new ValueEventListener() {
            ArrayList<String> allAisles = new ArrayList<>();
            ArrayList<String> allBays = new ArrayList<>();
            ArrayList<String> allShelves = new ArrayList<>();


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {
                    String aisleNum = (String) childSnapShot.child("aisle_num").getValue();
                    String bayNum = (String) childSnapShot.child("bay_num").getValue();
                    String shelfNum = (String) childSnapShot.child("num_of_shelves").getValue();
                    allAisles.add(aisleNum);
                    allBays.add(bayNum);
                    allShelves.add(shelfNum);
                    String strHold = aisleNum + " " + bayNum + " " + shelfNum;

                }
                ArrayAdapter arrayAdapter1 = new ArrayAdapter(AddInventory.this,android.R.layout.simple_spinner_dropdown_item, arrMaxAisles);
                aisle_spinner.setAdapter(arrayAdapter1);

                //Set Bay Spinner according to the specific Aisle Num Spinner Selected
                aisle_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        int currentAisleSelected = position;
                        currentAisleSelected = aisle_spinner.getSelectedItemPosition();
                        int bayHolder = 0;
                        ArrayList<String> currentNumBays = new ArrayList<String>();
                        for (int i=0; i < arrMaxAisles.size(); i++) {
                            if (i == currentAisleSelected) {
                                bayHolder = Integer.parseInt(arrMaxBays.get(i));
                            }
                        }
                        for (int j = 0; j < bayHolder; j++) {
                            currentNumBays.add(String.valueOf(j));
                        }

                        ArrayAdapter advBadp = new ArrayAdapter(AddInventory.this,android.R.layout.simple_spinner_dropdown_item, currentNumBays);
                        bay_spinner.setAdapter(advBadp);
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                bay_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    ArrayList<String> currentShelfArr = new ArrayList<String>();
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        currentShelfArr=new ArrayList<String>();
                        int currentBaySelected = position;
                        int currentAisleSelected = aisle_spinner.getSelectedItemPosition();
                        currentBaySelected = bay_spinner.getSelectedItemPosition();
                        String currentShelf = "";
                        String zz= "";
                        for (int i = 0; i <= currentAisleSelected; i++) {
                            for (int j = 0; j <= currentBaySelected  ; j++) {
                                if (i==currentAisleSelected && j==currentBaySelected){
                                    currentShelf = allShelves.get(j);
                                   // String k = currentShelf;
                                }
                            }
                        }

                        int shelfCounter = Integer.parseInt(currentShelf);
                        if(Integer.parseInt(currentShelf) == 0){
                            currentShelfArr.add(String.valueOf(0));
                        }else{
                            for (int i = 0; i < shelfCounter; i++) {
                                currentShelfArr.add(String.valueOf(i));
                            }
                        }
                        ArrayAdapter shelfAdp = new ArrayAdapter(AddInventory.this,android.R.layout.simple_spinner_dropdown_item, currentShelfArr);
                        shelf_spinner.setAdapter(shelfAdp);
                        //Log.i("daCurrent: ", "aisle" + currentAisleSelected + " bay" + currentBaySelected + "Shelf " + currentShelf);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        take_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(AddInventory.this, EditStoreAndDepartmentActivity.class);
                startActivity(intent);
            }
        });
        upload_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(gallery,RESULT_LOAD_IMAGE);

            }

        });
        /*Uri file = Uri.fromFile(new File("path/to/images/rivers.jpg"));
        StorageReference riversRef = storageRef.child("images/rivers.jpg");

        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                    }
                });
        */
        /*----------------- Adding Products to Data On Click Listener -----------*/
        add_product_btn.setOnClickListener(new View.OnClickListener() {
            int pCounter=0;
            @Override
            public void onClick(View view) {

                String p_id = product_id.getText().toString();
                String p_name = product_name.getText().toString();
                String p_stock = num_stock.getText().toString();
                String p_desc = product_desc.getText().toString();

                String currentAisleSpinner = aisle_spinner.getSelectedItem().toString();
                String currentBaySpinner = bay_spinner.getSelectedItem().toString();
                String currentShelfSpinner = shelf_spinner.getSelectedItem().toString();
                String currentDeptSpinner = dept_spinner.getSelectedItem().toString();

                String ProductKey = myRef.child(userID).child("Products").child("Product" + productCounter).push().getKey();
               // String ProductKey = "Product" + productCounter;
                myRef.child(userID).child("Products").child(ProductKey).push().getKey();
                myRef.child(userID).child("Products").child(ProductKey).child("P_Name").setValue(p_name);
                myRef.child(userID).child("Products").child(ProductKey).child("P_Stock").setValue(p_stock);
                myRef.child(userID).child("Products").child(ProductKey).child("P_ID").setValue(p_id);
                myRef.child(userID).child("Products").child(ProductKey).child("P_Desc").setValue(p_desc);
                myRef.child(userID).child("Products").child(ProductKey).child("P_Dept").setValue(currentDeptSpinner);
                myRef.child(userID).child("Products").child(ProductKey).child("P_Aisle").setValue(currentAisleSpinner);
                myRef.child(userID).child("Products").child(ProductKey).child("P_Bay").setValue(currentBaySpinner);
                myRef.child(userID).child("Products").child(ProductKey).child("P_Shelf").setValue(currentShelfSpinner);

                Log.i("ProductInfo: ", p_id + " " + p_name + " " + p_stock + " " + p_desc);
                Log.i("SubmitDept ", currentDeptSpinner);
                Log.i("SubmitLocation ", currentAisleSpinner + " " + currentBaySpinner + " "+ currentShelfSpinner);
                productCounter++;
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode== RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data!=null){
            Uri selectedImage = data.getData();
            imageView.setImageURI(selectedImage);
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
