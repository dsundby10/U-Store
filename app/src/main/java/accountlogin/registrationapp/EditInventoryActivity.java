package accountlogin.registrationapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class EditInventoryActivity extends AppCompatActivity {
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int RESULT_TAKE_IMAGE = 2;
    static int PICTURE_OPTION = 0;

    //Layout Variables
    ImageView imageView;
    Spinner product_spinner;
    Spinner dept_spinner, aisle_spinner, bay_spinner, shelf_spinner;
    EditText product_name, product_id, num_stock, product_desc;
    Button take_image_btn, upload_image_btn, delete_image_btn, main_menu_btn, save_changes_btn, delete_product_btn;

    /*-Aisle Spinner Vars-*/
    int maxAisles = 0;
    String aisleChecker = "";
    ArrayList<String> aisle_spinnerValues = new ArrayList<>();

    /*-Bay Spinner Vars-*/
    String bayChecker = "";
    ArrayList<String> bay_spinnerValues = new ArrayList<>();
    ArrayList<String> bayCheckerList = new ArrayList<>();

    /*-Shelf Spinner Vars-*/
    ArrayList<String> shelf_spinnerValues = new ArrayList<>();
    ArrayList<String> shelfCheckerList = new ArrayList<>();

    /*-Dept Spinner Vars-*/
    String deptString = "";
    String[] deptArr;
    ArrayList<String> dept_spinnerValues = new ArrayList<>();

    ArrayList<String> allABS = new ArrayList<>();

    //Firebase Variables
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //Database References
    private DatabaseReference myRef;
    private DatabaseReference AisleRef;
    private DatabaseReference AisleBayRef;
    private DatabaseReference ShelfRef;
    private DatabaseReference ProductRef;
    private String userID;

    //Firebase Image StorageReference
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://accountloginregistrationapp.appspot.com");
    StorageReference productImg;
    Uri cur_ur;
    Bitmap cur_bitmap;
    ArrayList<String> productChecker = new ArrayList<>();
    String productString = "";

    //Place holders for the selected products current spinner values
    String p_curAisle = "";
    String p_curBay="";
    String p_curShelf="";
    String p_curDept="";
    String p_curKey= "";
    String imgPath="";
    String p_curImg="";
    String old_product = "";

    //Intent Data Variables
    String getStoreName = "";
    String employeeID;
    String getUserPermissions="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Edit Inventory");
        setContentView(R.layout.activity_edit_inventory);
        Intent intent = getIntent();

        getStoreName = intent.getStringExtra("STORE_NAME");
        getUserPermissions = intent.getStringExtra("USER_PERMISSIONS");
        employeeID = intent.getStringExtra("STORE_USER");

        imageView = (ImageView)findViewById(R.id.imageView);

        //Variable initialization
        initializeLayoutVariables();

        //Firebase initialization
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

        /*===== Adding the new changes to the database under the current product key =====*/
        save_changes_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (product_name.getText().toString().length()==0){
                    toastMessage("You must assign a product name!");
                } else {
                    myRef.child(userID).child("Products").child(p_curKey).child("P_ID").setValue(product_id.getText().toString());
                    myRef.child(userID).child("Products").child(p_curKey).child("P_Aisle").setValue(String.valueOf(aisle_spinner.getSelectedItem()));
                    myRef.child(userID).child("Products").child(p_curKey).child("P_Bay").setValue(String.valueOf(bay_spinner.getSelectedItem()));
                    myRef.child(userID).child("Products").child(p_curKey).child("P_Shelf").setValue(String.valueOf(shelf_spinner.getSelectedItem()));
                    myRef.child(userID).child("Products").child(p_curKey).child("P_Dept").setValue(String.valueOf(dept_spinner.getSelectedItem()));
                    myRef.child(userID).child("Products").child(p_curKey).child("P_Desc").setValue(product_desc.getText().toString());
                    myRef.child(userID).child("Products").child(p_curKey).child("P_Name").setValue(product_name.getText().toString());
                    myRef.child(userID).child("Products").child(p_curKey).child("P_Stock").setValue(num_stock.getText().toString());

                    /*==User Took a picture with camera ====*/
                    if (PICTURE_OPTION == 2) {
                        if (p_curImg.equals("n/a")) { //There's no image to delete from the storage bucket
                             /*-Create a new imageKey & assign p_name+imageKey+.jpg to the new imgPath-*/
                            String imageKey = myRef.child(userID).child("Products").child(p_curKey).child("P_ImagePath").push().getKey();
                            productImg = storageRef.child(userID + ".images/" + product_name.getText().toString() + "ú" + imageKey + ".jpg");
                            imgPath = product_name.getText().toString() + "ú" + imageKey + ".jpg";
                            myRef.child(userID).child("Products").child(p_curKey).child("P_ImagePath").setValue(imgPath);
                        } else {
                            /*-Retrieve old image from storage bucket & delete it-*/
                            productImg = storageRef.child(userID + ".images/" + p_curImg);
                            productImg.delete();

                            /*-Create a new imageKey & assign p_name+imageKey+.jpg to the new imgPath-*/
                            String imageKey = myRef.child(userID).child("Products").child(p_curKey).child("P_ImagePath").push().getKey();
                            productImg = storageRef.child(userID + ".images/" + product_name.getText().toString() + "ú" + imageKey + ".jpg");
                            imgPath = product_name.getText().toString() + "ú" + imageKey + ".jpg";
                            myRef.child(userID).child("Products").child(p_curKey).child("P_ImagePath").setValue(imgPath);
                        }
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        cur_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] dataz = baos.toByteArray();
                        UploadTask uploadTask = productImg.putBytes(dataz);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            }
                        });
                    }
                    /*===User uploaded image from gallery====*/
                    if (PICTURE_OPTION == 1) {
                        if (p_curImg.equals("n/a")){ //There's no image to delete from the storage bucket
                            /*-Create a new imageKey & assign p_name+imageKey+.jpg to the new imgPath-*/
                            String imageKey = myRef.child(userID).child("Products").child(p_curKey).child("P_ImagePath").push().getKey();
                            productImg = storageRef.child(userID + ".images/" + product_name.getText().toString() + "ú" + imageKey + ".jpg");
                            imgPath = product_name.getText().toString() + "ú" + imageKey + ".jpg";
                            myRef.child(userID).child("Products").child(p_curKey).child("P_ImagePath").setValue(imgPath);
                        } else {
                            /*-Retrieve old image from storage bucket & delete it-*/
                            productImg = storageRef.child(userID + ".images/" + p_curImg);
                            productImg.delete();

                            /*-Create a new imageKey & assign p_name+imageKey+.jpg to the new imgPath-*/
                            String imageKey = myRef.child(userID).child("Products").child(p_curKey).child("P_ImagePath").push().getKey();
                            productImg = storageRef.child(userID + ".images/" + product_name.getText().toString() + "ú" + imageKey + ".jpg");
                            imgPath = product_name.getText().toString() + "ú" + imageKey + ".jpg";
                            myRef.child(userID).child("Products").child(p_curKey).child("P_ImagePath").setValue(imgPath);
                        }
                        UploadTask uploadTask = productImg.putFile(cur_ur);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            }
                        });

                    }
                    PICTURE_OPTION = 0;
                    //Refresh activity
                    Intent intent = new Intent(EditInventoryActivity.this, EditInventoryActivity.class);
                    sendIntentData(intent);
                    toastMessage("Product has been updated!");
                }
            }
        });

          /*=== Product listener - pulling all the stores product info
               and forming it into an arrayList ===*/
        productListenerOnCreate();

        /*====== On Product Selected Listener ====*/
        product_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] myProdArr;

                for (int i = 0; i < productChecker.size() ; i++) {
                    String strHold = productChecker.get(i);
                    myProdArr = strHold.split("¿");
                    //Getting the current products variables & assigning them to their corresponding position
                    if (product_spinner.getSelectedItem().toString().equals(myProdArr[0])){
                        old_product = myProdArr[0]; //For comparing
                        p_curImg = myProdArr[9];
                        p_curKey = myProdArr[8]; //current product key
                        product_name.setText(myProdArr[0]);
                        p_curAisle = myProdArr[1];
                        p_curBay = myProdArr[2];
                        p_curShelf = myProdArr[3];
                        /*Handle the empty fields -- to avoid error on display results based on whats in the myProdArr*/
                        if (myProdArr[4].equals("None")){
                            p_curDept = "None";
                        } else {
                            p_curDept = myProdArr[4];
                        }
                        if (myProdArr[5].equals("N/A")){
                            product_id.setText("");
                        } else {
                            product_id.setText(myProdArr[5]);
                        }
                        if (myProdArr[6].equals("N/A")){
                            product_desc.setText("");
                        } else {
                            product_desc.setText(myProdArr[6]);
                        }

                        if (myProdArr[7].equals("N/A")){
                            num_stock.setText("");
                        }else {
                            num_stock.setText(myProdArr[7]);
                        }
                        if (!myProdArr[9].equals("n/a")) {
                            productImg = storageRef.child(userID+".images/"+myProdArr[9]);
                            imageView.findViewById(R.id.imageView);
                            Glide.with(EditInventoryActivity.this).using(new FirebaseImageLoader()).load(productImg).into(imageView);
                        } else {
                            //No image exists -- set null
                            Glide.with(EditInventoryActivity.this).using(new FirebaseImageLoader()).load(null).into(imageView);
                        }
                    }
                }
                /*==Generate Aisle/Bay/Shelf/Dept Spinners====*/
                createSpinner(maxAisles);
                createDeptSpinner();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        /*=== Camera on Click Listener ==*/
        take_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, RESULT_TAKE_IMAGE);
            }
        });
        /*===Upload image On Click Listener ====*/
        upload_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(gallery,RESULT_LOAD_IMAGE);
            }

        });
        main_menu_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditInventoryActivity.this, MainMenu.class);
                sendIntentData(intent);
            }
        });
        delete_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!p_curImg.equals("n/a")){
                    storageRef.child(userID+ ".images/" + p_curImg).delete();
                    myRef.child(userID).child("Products").child(p_curKey).child("P_ImagePath").setValue("n/a");
                    //No image exists -- set null
                    Glide.with(EditInventoryActivity.this).using(new FirebaseImageLoader()).load(null).into(imageView);
                    toastMessage("Image has been removed.");
                }else {
                    toastMessage("There's no image to remove.");
                }
            }
        });
        delete_product_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Remove image from storage bucket
                productImg = storageRef.child(userID + ".images/" + p_curImg);
                productImg.delete();

                //Remove product data from database
                myRef.child(userID).child("Products").child(p_curKey).removeValue();

                toastMessage("Product has been deleted.");
                Intent intent = new Intent(EditInventoryActivity.this, EditInventoryActivity.class);
                sendIntentData(intent);
            }
        });

        /*===Reset the createShelfSpinner * BaySpinner Positions ====*/
        aisle_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                createBaySpinner(maxAisles);
                createShelfSpinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        /*===Reset the ShelfSpinner Positions===*/
        bay_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                createShelfSpinner();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
         /*======= Pulling Aisle & Dept from DB=======*/
        AisleRef = mFirebaseDatabase.getReference().child(userID);
        AisleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                deptString = "";
                aisleChecker="";
                dept_spinnerValues = new ArrayList<String>();
                for(DataSnapshot data: dataSnapshot.getChildren()) {
                    /*=== If User has an Existing Aisle Entry in the Database ===*/
                    if (data.getKey().equals("aisles") && !data.getValue().toString().trim().equals("")) {
                        aisleChecker = data.getValue().toString();
                        maxAisles = Integer.parseInt(aisleChecker);
                    }
                    if (data.getKey().equals("deptNames") && !data.getValue().toString().trim().equals("")) {
                        deptString = data.getValue().toString();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        /*===== ShelfSetup DB reference to generate spinner ===*/
        ShelfRef = mFirebaseDatabase.getReference().child(userID).child("ShelfSetup");
        ShelfRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!this.getClass().equals(EditInventoryActivity.class)){
                    ShelfRef.removeEventListener(this);
                }
                shelfCheckerList = new ArrayList<String>();
                allABS = new ArrayList<String>();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String a = data.child("aisle_num").getValue().toString();
                    String b = data.child("bay_num").getValue().toString();
                    String s = data.child("num_of_shelves").getValue().toString();
                    allABS.add(a + "¿" + b + "¿" + s);
                    shelfCheckerList.add(data.child("num_of_shelves").getValue().toString());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        /*==== Pulling Bay Number out of "BaySetup" in the Database ===*/
        AisleBayRef = mFirebaseDatabase.getReference().child(userID).child("BaySetup");
        AisleBayRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                bayCheckerList = new ArrayList<>();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    bayCheckerList.add(data.child("bays").getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    /*== product listener used for when user removes a product image ==*/
    public void productListenerUpdateData(){
        ProductRef = mFirebaseDatabase.getReference().child(userID).child("Products");
        ProductRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!this.getClass().equals(EditInventoryActivity.class)){
                    ProductRef.removeEventListener(this);
                }
                productChecker = new ArrayList<String>();
                productString = "";
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String p_key = data.getKey();
                    old_product = data.child("P_Name").getValue().toString();

                    String p_aisle = data.child("P_Aisle").getValue().toString();
                    String p_bay = data.child("P_Bay").getValue().toString();
                    String p_shelf = data.child("P_Shelf").getValue().toString();
                    String p_dept = data.child("P_Dept").getValue().toString();
                    String p_name = data.child("P_Name").getValue().toString();
                    String p_id = data.child("P_ID").getValue().toString();
                    String p_desc = data.child("P_Desc").getValue().toString();
                    String p_stock = data.child("P_Stock").getValue().toString();
                    String p_ImagePath = data.child("P_ImagePath").getValue().toString();

                    /*==Probably should auto assign null values on AddInventory..==*/
                    if (p_ImagePath.isEmpty()){ p_ImagePath = "n/a"; }
                    if (p_id.isEmpty()){ p_id="N/A"; }
                    if (p_desc.isEmpty()){ p_desc="N/A"; }
                    if (p_stock.isEmpty()){ p_stock="N/A"; }
                    if (p_aisle.isEmpty()){ p_aisle="¿"; }
                    if (p_bay.isEmpty()){ p_bay="¿"; }
                    if (p_shelf.isEmpty()){ p_shelf="¿"; }
                    if (p_dept.isEmpty()){ p_dept="None"; }

                    productString = p_name + "¿" + p_aisle + "¿" + p_bay + "¿" + p_shelf + "¿" + p_dept + "¿" + p_id + "¿" + p_desc + "¿" + p_stock + "¿" + p_key + "¿"+ p_ImagePath;
                    productChecker.add(productString);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    public void productListenerOnCreate(){
        ProductRef = mFirebaseDatabase.getReference().child(userID).child("Products");
        ProductRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!this.getClass().equals(EditInventoryActivity.class)){
                    ProductRef.removeEventListener(this);
                }
                productChecker = new ArrayList<String>();
                productString = "";
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String p_key = data.getKey();
                    old_product = data.child("P_Name").getValue().toString();

                    String p_aisle = data.child("P_Aisle").getValue().toString();
                    String p_bay = data.child("P_Bay").getValue().toString();
                    String p_shelf = data.child("P_Shelf").getValue().toString();
                    String p_dept = data.child("P_Dept").getValue().toString();

                    String p_name = data.child("P_Name").getValue().toString();
                    String p_id = data.child("P_ID").getValue().toString();
                    String p_desc = data.child("P_Desc").getValue().toString();
                    String p_stock = data.child("P_Stock").getValue().toString();
                    String p_ImagePath = data.child("P_ImagePath").getValue().toString();

                    /*==Probably should auto assign null values on AddInventory..==*/
                    if (p_ImagePath.isEmpty()){
                        p_ImagePath = "n/a";
                    }
                    if (p_id.isEmpty()){
                        p_id="N/A";
                    }
                    if (p_desc.isEmpty()){
                        p_desc="N/A";
                    }
                    if (p_stock.isEmpty()){
                        p_stock="N/A";
                    }
                    if (p_aisle.isEmpty()){
                        p_aisle="¿";
                    }
                    if (p_bay.isEmpty()){
                        p_bay="¿";
                    }
                    if (p_shelf.isEmpty()){
                        p_shelf="¿";
                    }
                    if (p_dept.isEmpty()){
                        p_dept="None";
                    }
                    productString = p_name + "¿" + p_aisle + "¿" + p_bay + "¿" + p_shelf + "¿" + p_dept + "¿" + p_id + "¿" + p_desc + "¿" + p_stock + "¿" + p_key + "¿"+ p_ImagePath;
                    Log.i("P.OptionPimage: ", p_ImagePath);
                    productChecker.add(productString);
                }
                //Genereate product spinner
                createProductSpinner();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /*========Create Product Spinner ==========*/
    public void createProductSpinner(){
        product_spinner = (Spinner)findViewById(R.id.product_spinner);
        String productHold = "";
        String[] productHoldArr;
        ArrayList<String> productSpin = new ArrayList<>();
        for (int i = 0; i <productChecker.size(); i++) {
            productHold = productChecker.get(i);
            productHoldArr = productHold.split("¿");
            productSpin.add(i,productHoldArr[0]);
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,  productSpin);
        dataAdapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);
        product_spinner.setAdapter(dataAdapter);
    }

    /*============Create Aisle Spinner==========*/
    public void createSpinner(int maxAisles) {
        aisle_spinner = (Spinner) findViewById(R.id.aisle_spinner);
        int cur_selection = 0;
        aisle_spinnerValues = new ArrayList<>();
            //Generate Spinner
            for (int i = 1; i <= maxAisles; i++) {
                /*--Get Current Product Aisle Position--*/
                if (i == Integer.parseInt(p_curAisle)){
                    cur_selection = i-1;
                }
                    aisle_spinnerValues.add(String.valueOf(i));
            }
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                    (this, android.R.layout.simple_spinner_item, aisle_spinnerValues);
            dataAdapter.setDropDownViewResource
                    (android.R.layout.simple_spinner_dropdown_item);
            aisle_spinner.setAdapter(dataAdapter);
            aisle_spinner.setSelection(cur_selection);

        }
    /*========= Create Bay Spinner==========*/
    public void createBaySpinner(int maxAisles){
        int cur_selection = 0;
        bay_spinner = (Spinner)findViewById(R.id.bay_spinner);
        String currentAisleSpinner = aisle_spinner.getSelectedItem().toString();
        bay_spinnerValues = new ArrayList<>();
        for (int i = 1; i <= maxAisles; i++) {
            if (i == Integer.parseInt(currentAisleSpinner)) {
                bayChecker = bayCheckerList.get(i-1);
                for (int j = 1; j <= Integer.parseInt(bayChecker); j++) {
                    /*--Get Current Product Bay Position--*/
                    if (j == Integer.parseInt(p_curBay)){
                       cur_selection=j-1;
                    }
                    bay_spinnerValues.add(String.valueOf(j));
                }
            }
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, bay_spinnerValues);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bay_spinner.setAdapter(dataAdapter);
        bay_spinner.setSelection(cur_selection);
    }
    /*=============Create Shelf Spinner==========*/
    public void createShelfSpinner(){
        String currentShelf = "0";
        int cur_selection = 0;
        shelf_spinner = (Spinner)findViewById(R.id.shelf_spinner);
        String crntA = aisle_spinner.getSelectedItem().toString();
        String crntB = bay_spinner.getSelectedItem().toString();

        String abshold = "";
        String[] absArr;
        for (int i = 0; i < allABS.size(); i++) {
            abshold = allABS.get(i);
            absArr = abshold.split("¿");
            if (absArr[0].equals(crntA) && absArr[1].equals(crntB)){
                currentShelf = absArr[2];
            }
        }
        shelf_spinnerValues = new ArrayList<>();
            for (int i = 0; i <= Integer.parseInt(currentShelf); i++) {
                /*--Get Current Product Shelf Position--*/
                if (i == Integer.parseInt(p_curShelf)){
                    cur_selection = i;
                }
            }
            for (int i = 0; i <= Integer.parseInt(currentShelf) ; i++) {
                shelf_spinnerValues.add(String.valueOf(i));
            }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, shelf_spinnerValues);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shelf_spinner.setAdapter(dataAdapter);
        shelf_spinner.setSelection(cur_selection);
    }
    /*===Generate Dept Spinner Values ====*/
    public void createDeptSpinner() {
        dept_spinner = (Spinner)findViewById(R.id.dept_spinner);
        dept_spinnerValues = new ArrayList<>();
        deptArr = deptString.split("\\s*,\\s*");
        int cur_selection = deptArr.length;
        /*== Auto Generate a None place holder of None into dept list
             to let user know which products aren't assigned.    ==*/
        for (int i = 0; i <= deptArr.length; i++) {
            if (i==deptArr.length){
                dept_spinnerValues.add("None");
            } else {
                dept_spinnerValues.add(deptArr[i]);
                if (deptArr[i].equals(p_curDept)){
                    cur_selection=i;
                }
            }
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,  dept_spinnerValues);
        dataAdapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);
        dept_spinner.setAdapter(dataAdapter);
        dept_spinner.setSelection(cur_selection);
    }
    /*=== Load image / Take image intent results ===*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode==RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data!=null){
            PICTURE_OPTION=1;
            Uri selectedImage = data.getData();
            cur_ur = selectedImage;
            Glide.with(EditInventoryActivity.this).load(cur_ur).into(imageView);
        }
        if (requestCode==RESULT_TAKE_IMAGE && resultCode == RESULT_OK && data!=null){
            PICTURE_OPTION=2;
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
            imageView.setDrawingCacheEnabled(true);
            imageView.buildDrawingCache();
            bitmap = imageView.getDrawingCache();
            imageView.setImageBitmap(bitmap);
            cur_bitmap = bitmap;
        }
    }
    public void initializeLayoutVariables(){
        //Spinners
        product_spinner = (Spinner)findViewById(R.id.product_spinner);
        dept_spinner = (Spinner)findViewById(R.id.dept_spinner);
        aisle_spinner = (Spinner)findViewById(R.id.aisle_spinner);
        bay_spinner = (Spinner)findViewById(R.id.bay_spinner);
        shelf_spinner = (Spinner)findViewById(R.id.shelf_spinner);

        //EditTexts
        product_name = (EditText)findViewById(R.id.product_name);
        num_stock = (EditText)findViewById(R.id.num_stock);
        product_id = (EditText)findViewById(R.id.product_id);
        product_desc = (EditText)findViewById(R.id.product_desc);

        //Buttons
        save_changes_btn = (Button)findViewById(R.id.save_changes_btn);
        take_image_btn   = (Button)findViewById(R.id.take_image_btn);
        upload_image_btn = (Button)findViewById(R.id.upload_image_btn);
        delete_image_btn = (Button)findViewById(R.id.delete_image_btn);
        main_menu_btn =    (Button)findViewById(R.id.main_menu_btn);
        delete_product_btn = (Button)findViewById(R.id.delete_product_btn);
    }
    public void sendIntentData(Intent intent){
        intent.putExtra("STORE_USER", employeeID);
        intent.putExtra("STORE_NAME", getStoreName);
        intent.putExtra("USER_PERMISSIONS", getUserPermissions);
        startActivity(intent);
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
