package zhe.it_tech613.com.cmpcourier.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ContactPersonModel extends RealmObject {
    @PrimaryKey
    String id;
    String name;
    String telephone;
    String company;

    public ContactPersonModel(){}

    public ContactPersonModel(String id,String name,String telephone,String company){
        this.id=id;
        this.company=company;
        this.name=name;
        this.telephone=telephone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
}
