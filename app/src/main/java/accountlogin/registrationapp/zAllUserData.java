package accountlogin.registrationapp;

/**
 * Created by ds on 10/3/2017.
 */

public class zAllUserData {
    private String DeptNames;
    private String NumDepartments;
    private String StoreName;
    private String NumAisles;
    private String NumBays;
    private String ShelfSetup;
    private String AisleID;
    private String aisle_num;
    private String bay_num;
    private String num_of_shelves;



    public zAllUserData(){
        getDeptNames();
        getStoreName();
        getNumDepartments();
        getNumAisles();
        getNumBays();
        getShelfSetup();
        getAisleID();
        //setAisleID(this.getAisleID());
    }
    public String getDeptNames() {
        return DeptNames;
    }

    public void setDeptNames(String DeptNames) {
        this.DeptNames = DeptNames;
    }

    public String getNumDepartments() {
        return NumDepartments;
    }

    public void setNumDepartments(String NumDepartments) {
        this.NumDepartments = NumDepartments;
    }

    public String getStoreName() {
        return StoreName;
    }

    public void setStoreName(String StoreName) {
        this.StoreName = StoreName;
    }

    public String getNumAisles() {
        return NumAisles;
    }

    public void setNumAisles(String numAisles) {
        NumAisles = numAisles;
    }

    public String getNumBays() {
        return NumBays;
    }

    public void setNumBays(String numBays) {
        NumBays = numBays;
    }

    public String getShelfSetup() {
        return ShelfSetup;
    }

    public void setShelfSetup(String shelfSetup) {
        ShelfSetup = shelfSetup;
    }

    public String getAisleID() {
        return AisleID;
    }

    public void setAisleID(String aisleID) {
        AisleID = aisleID;
    }

    public String getAisle_num() {
        return aisle_num;
    }

    public void setAisle_num(String aisle_num) {
        this.aisle_num = aisle_num;
    }

    public String getBay_num() {
        return bay_num;
    }

    public void setBay_num(String bay_num) {
        this.bay_num = bay_num;
    }

    public String getNum_of_shelves() {
        return num_of_shelves;
    }

    public void setNum_of_shelves(String num_of_shelves) {
        this.num_of_shelves = num_of_shelves;
    }
}
