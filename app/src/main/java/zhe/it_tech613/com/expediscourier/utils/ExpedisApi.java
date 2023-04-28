package zhe.it_tech613.com.cmpcourier.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Base64;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import zhe.it_tech613.com.cmpcourier.model.AddressModel;
import zhe.it_tech613.com.cmpcourier.model.CompanyModel;
import zhe.it_tech613.com.cmpcourier.model.ContactPersonModel;
import zhe.it_tech613.com.cmpcourier.model.ParcelModel;

public class cmpApi {

//    private Realm realmCustomerUser;
//    private Realm realmClinicUser;
//    private Realm realmSpecialty;
//    private Realm realmCity;

    private Realm realm;
    private Activity activity;
    private Context context;

    public KProgressHUD kpHUD;

    public cmpApi(Activity activity) {
        this.activity = activity;
        this.realm = PreferenceManager.realm;
//        this.realmCustomerUser=RealmController.with(activity).getRealmCustomerUser();
//        this.realmClinicUser=RealmController.with(activity).getRealmClinicUser();
//        this.realmSpecialty=RealmController.with(activity).getRealmSpecialty();
//        this.realmCity=RealmController.with(activity).getRealmCity();

        kpHUD = KProgressHUD.create(activity)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(true)
//                .setLabel("Login")
                .setAnimationSpeed(1)
                .setDimAmount(0.5f);
    }

    public cmpApi(Context context) {
        this.context = context;
        this.realm = PreferenceManager.realm;
//        this.realmCustomerUser=RealmController.with(activity).getRealmCustomerUser();
//        this.realmClinicUser=RealmController.with(activity).getRealmClinicUser();
//        this.realmSpecialty=RealmController.with(activity).getRealmSpecialty();
//        this.realmCity=RealmController.with(activity).getRealmCity();

        kpHUD = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(true)
//                .setLabel("Login")
                .setAnimationSpeed(1)
                .setDimAmount(0.5f);
    }

    //New
    public boolean parseLogin(JSONObject responseJsonObject) {

        try {
            String status = responseJsonObject.getString("value");
            switch (status) {
                case "0":
                    PreferenceManager.setID(responseJsonObject.getLong("ID"));
                    PreferenceManager.setName(responseJsonObject.getString("surname") + " " + responseJsonObject.getString("name"));
                    PreferenceManager.setLocation(responseJsonObject.getString("location"));
                    PreferenceManager.setLoginStatus(Constant.LoginStatus.UserLoggedIn.toString());
                    PreferenceManager.setFtpIp(responseJsonObject.getString("ftp_ip"));
                    PreferenceManager.setFtpLogin(responseJsonObject.getString("ftp_login"));
                    PreferenceManager.setFtpPass(responseJsonObject.getString("ftp_password"));
                    PreferenceManager.setFtpPath(responseJsonObject.getString("ftp_path"));
                    PreferenceManager.setFtpESIGN_Ip(responseJsonObject.getString("ftp_esign_ip"));
                    PreferenceManager.setFtpESIGN_Login(responseJsonObject.getString("ftp_esign_login"));
                    PreferenceManager.setFtpESIGN_Pass(responseJsonObject.getString("ftp_esign_password"));
                    PreferenceManager.setFtpESIGN_Path(responseJsonObject.getString("ftp_esign_path"));
                    return true;
                case "-1":
                    Toast.makeText(activity, Constant.czlanguageStrings.getLOGIN_ALERT(), Toast.LENGTH_LONG).show();
                    break;
                case "-2":
                    Toast.makeText(activity, responseJsonObject.getString("msg"), Toast.LENGTH_LONG).show();
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }


    public RealmResults<ParcelModel> parseChangeParcelStatus(JSONObject responseJsonObject) {
        final RealmResults<ParcelModel> result = PreferenceManager.realm.where(ParcelModel.class).findAll().sort("barcode", Sort.ASCENDING);
        PreferenceManager.realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                result.deleteAllFromRealm();
            }
        });
        try {
            String status = responseJsonObject.getString("value");
            if (status.equals("0")) {
                int x = responseJsonObject.getInt("X");
                int y = responseJsonObject.getInt("Y");
                JSONArray list = responseJsonObject.getJSONArray("list");
                for (int i = 0; i < list.length(); i++) {
                    JSONObject row = list.getJSONObject(i);
                    String ean_code = row.getString("ean_code");
                    String client = row.getString("client");
                    String city = row.getString("city");
                    int count_box = 0;
                    try {
                        count_box = row.getInt("count_box");
                    } catch (JSONException ignore) {
                    }
                    final ParcelModel parcelModel = new ParcelModel(ean_code, client, city, count_box, "Assigned");
                    PreferenceManager.realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.copyToRealmOrUpdate(parcelModel);
                        }
                    });
                }
                Constant.x = x;
                Constant.y = y;
                Toast.makeText(activity, Constant.czlanguageStrings.getSUCCESS_CHANGE_PARCEL_STATUS(), Toast.LENGTH_LONG).show();
                return result;
            } else if (status.equals("-1")) {
                Toast.makeText(activity, Constant.czlanguageStrings.getFAIL_CHANGE_PARCEL_ALERT(), Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public RealmResults<ParcelModel> parseInitialData(JSONObject responseJsonObject) {
        final RealmResults<ParcelModel> result = PreferenceManager.realm.where(ParcelModel.class).findAll().sort("barcode", Sort.ASCENDING);
        PreferenceManager.realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                result.deleteAllFromRealm();
            }
        });
        try {
            String status = responseJsonObject.getString("value");
            if (status.equals("0")) {
                int x = responseJsonObject.getInt("X");
                int y = responseJsonObject.getInt("Y");
                JSONArray list = responseJsonObject.getJSONArray("list");
                for (int i = 0; i < list.length(); i++) {
                    JSONObject row = list.getJSONObject(i);
                    String ean_code = row.getString("ean_code");
                    String client = row.getString("client");
                    String city = row.getString("city");
                    int count_box = 0;
                    try {
                        count_box = row.getInt("count_box");
                    } catch (JSONException ignore) {
                    }
                    final ParcelModel parcelModel = new ParcelModel(ean_code, client, city, count_box, "Assigned");
                    PreferenceManager.realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.copyToRealmOrUpdate(parcelModel);
                        }
                    });
                }
                Constant.x = x;
                Constant.y = y;
//                Toast.makeText(activity, Constant.czlanguageStrings.getSUCCESS_CHANGE_PARCEL_STATUS(),Toast.LENGTH_LONG).show();
                return result;
            } else if (status.equals("-1")) {
                Toast.makeText(activity, Constant.czlanguageStrings.getFAIL_GET_INITIAL_DATA(), Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public RealmResults<ParcelModel> parseGetOrders(JSONObject responseJsonObject, int intType) {
        final RealmResults<ParcelModel> result = PreferenceManager.realm.where(ParcelModel.class).findAll();
        PreferenceManager.realm.executeTransaction(realm -> result.deleteAllFromRealm());
        try {
            String status = responseJsonObject.getString("value");
            if (status.equals("0")) {
                JSONArray list = responseJsonObject.getJSONArray("orders");
                for (int i = 0; i < list.length(); i++) {
                    JSONObject row = list.getJSONObject(i);
                    String order = row.getString("order");
                    String orderId = row.getString("orderId");
                    String ean_code = row.getString("ean_code");
                    String client = row.getString("client");
                    String note = row.getString("note");
                    String cod = row.getString("cod");
                    String type = row.getString("type");
                    String city = row.getString("city");
                    String color = row.getString("color");
                    String sms = row.getString("sms");
                    String telephone = row.getString("telephone");
                    String timeFrame = row.getString("timeframe");
                    String davka = "";
                    if (row.has("davka")) davka = row.getString("davka");
                    double latitude = 0;
                    try {
                        latitude = row.getDouble("latitude");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    double longitude = 0;
                    try {
                        longitude = row.getDouble("longitude");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    final ParcelModel parcelModel = new ParcelModel(order, ean_code, client, city, color, latitude, longitude, telephone, sms.equals("1"));
                    parcelModel.setCod(cod);
                    parcelModel.setNote(note);
                    parcelModel.setType(type);
                    parcelModel.setIntType(intType);
                    parcelModel.setOrderId(orderId);
                    parcelModel.setTimeframe(timeFrame);
                    parcelModel.setDavka(davka);
                    PreferenceManager.realm.executeTransaction(realm -> realm.copyToRealmOrUpdate(parcelModel));
                }
//                Toast.makeText(activity, Constant.czlanguageStrings.getSUCCESS_CHANGE_PARCEL_STATUS(),Toast.LENGTH_LONG).show();
                return result;
            } else if (status.equals("-1")) {
                Toast.makeText(activity, Constant.czlanguageStrings.getFAIL_GET_INITIAL_DATA(), Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public boolean parseCheckBarcode(JSONObject responseJsonObject) {
        try {
            String status = responseJsonObject.getString("value");
            if (status.equals("0")) {
                return true;
            } else if (status.equals("-1")) {
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    //New
    public boolean parseChangeOrderStatus(JSONObject responseJsonObject) {
        try {
            String status = responseJsonObject.getString("value");
            if (status.equals("0")) {
//                int process_type=responseJsonObject.getInt("process_type");
//                final ParcelModel parcelModel=new ParcelModel(update_date,process_type,barcode,true);
//                PreferenceManager.realm.executeTransaction(new Realm.Transaction() {
//                    @Override
//                    public void execute(Realm realm) {
//                        realm.copyToRealmOrUpdate(parcelModel);
//                    }
//                });
                return true;
            } else if (status.equals("-1")) {
//                Toast.makeText(activity, Constant.czlanguageStrings.getNO_BARCODE_ALERT(),Toast.LENGTH_LONG).show();
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    //New
    public boolean parseLanguage(JSONObject responseJsonObject) {
        try {
            String status = responseJsonObject.getString("value");
            if (status.equals("0")) {
                Gson gson = new Gson();
                JSONObject result = responseJsonObject.getJSONObject("result");
                JSONObject enString = result.getJSONObject("en");
                JSONObject czString = result.getJSONObject("cz");
                LanguageStrings languageStrings = gson.fromJson(enString.toString(), LanguageStrings.class);
                Constant.languageStrings = languageStrings;
                LanguageStrings czlanguageStrings = gson.fromJson(czString.toString(), LanguageStrings.class);
                Constant.czlanguageStrings = czlanguageStrings;
//                Toast.makeText(activity, Constant.czlanguageStrings.getCH,Toast.LENGTH_LONG).show();
                return true;
            } else if (status.equals("-1")) {
                Toast.makeText(activity, Constant.czlanguageStrings.getFAIL_GET_LANGUAGE_ALERT(), Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public ArrayList<ParcelModel> parseLocations(JSONObject responseJsonObject) {
        ArrayList<ParcelModel> parcelModels = new ArrayList<>();
        try {
            String status = responseJsonObject.getString("value");
            if (status.equals("0")) {
                JSONArray list = responseJsonObject.getJSONArray("locations");
                for (int i = 0; i < list.length(); i++) {
                    JSONObject row = list.getJSONObject(i);
                    double latitude = 0;
                    try {
                        latitude = row.getDouble("latitude");
                    } catch (JSONException ignore) {
                    }
                    double longitude = 0;
                    try {
                        longitude = row.getDouble("longitude");
                    } catch (JSONException ignore) {
                    }
                    String barcode = row.getString("barcode");
                    if (latitude != 0 && longitude != 0) {
                        final ParcelModel parcelModel = new ParcelModel(barcode, latitude, longitude);
                        PreferenceManager.realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.insertOrUpdate(parcelModel);
                            }
                        });
                        parcelModels.add(parcelModel);
                    }
                }
                return parcelModels;
            } else if (status.equals("-1")) {
                Toast.makeText(activity, Constant.czlanguageStrings.getFAIL_GET_INITIAL_DATA(), Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
}
