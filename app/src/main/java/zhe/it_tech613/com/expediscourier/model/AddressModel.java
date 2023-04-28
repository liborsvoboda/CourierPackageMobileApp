package zhe.it_tech613.com.cmpcourier.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class AddressModel extends RealmObject {

    @PrimaryKey
    String id;
    String address,city, email,company;

    public AddressModel(){
    }
    public AddressModel (String id,String address,String city,String email,String company){
        this.id=id;
        this.address=address;
        this.city=city;
        this.email=email;
        this.company=company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCompany() {
        return company;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
