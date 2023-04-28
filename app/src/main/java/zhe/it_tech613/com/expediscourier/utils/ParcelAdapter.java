package zhe.it_tech613.com.cmpcourier.utils;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;
import zhe.it_tech613.com.cmpcourier.R;
import zhe.it_tech613.com.cmpcourier.model.ParcelModel;

public class ParcelAdapter extends RealmBaseAdapter<ParcelModel> implements ListAdapter {
    private static class ViewHolder {
        TextView id, parcel_no, cod;
        TextView name;
        TextView city;
        LinearLayout lay_id;
    }

    private boolean inDeletionMode = false;
    private Set<Integer> countersToDelete = new HashSet<Integer>();

    public ParcelAdapter(OrderedRealmCollection<ParcelModel> realmResults) {
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

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_parcel, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.id = convertView.findViewById(R.id.id);
            viewHolder.parcel_no = (TextView) convertView.findViewById(R.id.parcel_no);
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            viewHolder.city = (TextView) convertView.findViewById(R.id.city);
            viewHolder.cod = (TextView) convertView.findViewById(R.id.cod);
            viewHolder.lay_id = convertView.findViewById(R.id.lay_id);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (adapterData != null) {
            final ParcelModel item = adapterData.get(position);
            viewHolder.id.setText(item.getOrder());
            //Temporary
            viewHolder.lay_id.setVisibility(View.GONE);

            viewHolder.city.setText(item.getCity());
            viewHolder.parcel_no.setText(item.getBarcode() + " \n " + item.getNote());
            viewHolder.cod.setText(item.getTimeframe() + " \n" + item.getCod() + " \n " + item.getType() + " \n" + item.getOrderId());
            viewHolder.name.setText(item.getClient());
            if (item.getDavka().startsWith("ESH_ESIGN_")) {
                viewHolder.id.setTypeface(null, Typeface.BOLD_ITALIC);
                viewHolder.city.setTypeface(null, Typeface.BOLD_ITALIC);
                viewHolder.parcel_no.setTypeface(null, Typeface.BOLD_ITALIC);
                viewHolder.cod.setTypeface(null, Typeface.BOLD_ITALIC);
                viewHolder.name.setTypeface(null, Typeface.BOLD_ITALIC);
            }else {
                viewHolder.id.setTypeface(null, Typeface.NORMAL);
                viewHolder.city.setTypeface(null, Typeface.NORMAL);
                viewHolder.parcel_no.setTypeface(null, Typeface.NORMAL);
                viewHolder.cod.setTypeface(null, Typeface.NORMAL);
                viewHolder.name.setTypeface(null, Typeface.NORMAL);
            }
            convertView.setBackgroundColor(Color.parseColor(item.getColor()));
        }
        return convertView;
    }
}
