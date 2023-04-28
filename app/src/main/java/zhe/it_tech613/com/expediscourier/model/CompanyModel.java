package zhe.it_tech613.com.cmpcourier.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class CompanyModel extends RealmObject {
    @PrimaryKey
    String company;
    public CompanyModel(){}
    public CompanyModel(String company){
        this.company=company;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }
}
