/*
 * Copyright (c) 2018, Tejashwi Kalp Taru
 */

package tejashwi.com.unsplasher.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import tejashwi.com.unsplasher.R;
import tejashwi.com.unsplasher.rest.model.RandomImagesObject;

public class CustomListAdapter extends BaseAdapter {
    private List<RandomImagesObject> listData = new ArrayList<>();
    private LayoutInflater layoutInflater;
    private Context mCtx;

    public CustomListAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
        mCtx = context;
    }

    public void updateDataSet(List<RandomImagesObject> listData){
        this.listData.addAll(listData);
        this.notifyDataSetChanged();
    }

    public List<RandomImagesObject> backUpAndShowThis(List<RandomImagesObject> list){
        List<RandomImagesObject> backup = this.listData;
        this.listData = list;
        this.notifyDataSetChanged();
        return backup;
    }

    public void restorePrevious(List<RandomImagesObject> list){
        this.listData = list;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_row_layout, null);
            holder = new ViewHolder();
            holder.headlineView = convertView.findViewById(R.id.title);
            holder.authorNameView = convertView.findViewById(R.id.author);
            holder.publishDateView = convertView.findViewById(R.id.date);
            holder.imageView = convertView.findViewById(R.id.thumbImage);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        RandomImagesObject wallpaper = listData.get(position);
        holder.headlineView.setText(wallpaper.getId());
        holder.authorNameView.setText("By, " + wallpaper.getUser().getName());
        holder.publishDateView.setText(wallpaper.getCreatedAt());
        if (holder.imageView != null) {
            //new ImageDownloaderTask(holder.imageView).execute(wallpaper.getUrls().getThumb());
            Glide.with(mCtx)
                    .load(wallpaper.getUrls().getThumb())
                    .into(holder.imageView);
        }
        return convertView;
    }

    static class ViewHolder {
        TextView headlineView;
        TextView authorNameView;
        TextView publishDateView;
        ImageView imageView;
    }
}
