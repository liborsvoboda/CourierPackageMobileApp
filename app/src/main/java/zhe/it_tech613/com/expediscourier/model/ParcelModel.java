package zhe.it_tech613.com.cmpcourier.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ParcelModel extends RealmObject {
    @PrimaryKey
    private String barcode;
    private String status,note,cod,type;
    private int count_box;
    private String client;
    private String name,city,parcel_no;
    private boolean is_need;
    private double latitude,longitude;
    private String order, orderId;
    private String color, timeframe,telephone;
    private boolean is_sms_sent;
    private int intType;
    private String davka = "";
    public ParcelModel(){}

    public ParcelModel(String barcode,double latitude,double longitude){
        this.barcode=barcode;
        this.latitude=latitude;
        this.longitude=longitude;
    }
    public ParcelModel( String barcode,String client,String city,int count_box,String status){
        this.barcode=barcode;
        this.status=status;
        this.city=city;
        this.client=client;
        this.count_box=count_box;
    }
    public ParcelModel(String order,
                       String barcode,
                        String client,
                        String city,
                        String color,
                        double latitude,
                        double longitude,
                        String telephone,
                       boolean is_sms_sent){
        this.barcode=barcode;
        this.color=color;
        this.city=city;
        this.client=client;
        this.order=order;
        this.latitude=latitude;
        this.longitude=longitude;
        this.telephone=telephone;
        this.is_sms_sent=is_sms_sent;
    }
    public ParcelModel(String parcel_no,String name, String city, String status){
        this.parcel_no=parcel_no;
        this.name=name;
        this.city=city;
        this.status=status;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getStatus() {
        if (status!=null)return status;
        else return "";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isIs_need() {
        return is_need;
    }

    public void setIs_need(boolean is_need) {
        this.is_need = is_need;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getParcel_no() {
        return parcel_no;
    }

    public void setParcel_no(String parcel_no) {
        this.parcel_no = parcel_no;
    }

    public int getCount_box() {
        return count_box;
    }

    public void setCount_box(int count_box) {
        this.count_box = count_box;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public boolean isIs_sms_sent() {
        return is_sms_sent;
    }

    public void setIs_sms_sent(boolean is_sms_sent) {
        this.is_sms_sent = is_sms_sent;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCod() {
        return cod;
    }

    public void setCod(String cod) {
        this.cod = cod;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getIntType() {
        return intType;
    }

    public void setIntType(int intType) {
        this.intType = intType;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getTimeframe() {
        return timeframe;
    }

    public void setTimeframe(String timeframe) {
        this.timeframe = timeframe;
    }

    public String getDavka() {
        return davka !=null? davka:"";
    }

    public void setDavka(String davka) {
        this.davka = davka;
    }
}
