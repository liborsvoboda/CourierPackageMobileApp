package zhe.it_tech613.com.cmpcourier.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;
import zhe.it_tech613.com.cmpcourier.R;
import zhe.it_tech613.com.cmpcourier.model.ParcelModel;

public class BarcodeListAdapter extends RealmBaseAdapter<ParcelModel> implements ListAdapter {
    private static class ViewHolder {
        TextView barcode;
        TextView city;
        TextView client;
        TextView count;
    }

    private boolean inDeletionMode = false;
    private Set<Integer> countersToDelete = new HashSet<Integer>();

    public BarcodeListAdapter(OrderedRealmCollection<ParcelModel> realmResults) {
        super(realmResults);
    }

    void enableDeletionMode(boolean enabled) {
        inDeletionMode = enabled;
        if (!enabled) {
            countersToDelete.clear();
        }
        notifyDataSetChanged();
    }

    Set<Integer> getCountersToDelete() {
        return countersToDelete;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_barcode, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.barcode = (TextView) convertView.findViewById(R.id.barcode_disp);
            viewHolder.city = (TextView) convertView.findViewById(R.id.city);
            viewHolder.client =(TextView)convertView.findViewById(R.id.client);
            viewHolder.count=(TextView)convertView.findViewById(R.id.count);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (adapterData != null) {
            final ParcelModel item = adapterData.get(position);
            viewHolder.barcode.setText(item.getBarcode());
            viewHolder.client.setText(item.getClient());
            viewHolder.city.setText(item.getCity());
            viewHolder.count.setText(String.valueOf(item.getCount_box()));
//            if (inDeletionMode) {
//                viewHolder.city.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                        countersToDelete.add(item.getId());
//                    }
//                });
//            } else {
//                viewHolder.city.setOnCheckedChangeListener(null);
//            }
//            viewHolder.city.setChecked(countersToDelete.contains(item.getId()));
//            viewHolder.city.setVisibility(inDeletionMode ? View.VISIBLE : View.GONE);
        }
        return convertView;
    }
}
