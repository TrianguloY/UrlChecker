//package com.trianguloy.urlchecker.old;
//
//import android.content.Context;
//import android.content.pm.PackageManager;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.ImageView;
//
//import com.trianguloy.urlchecker.R;
//
//import java.util.ArrayList;
//
///**
// * Class that [INSERT DESCRIPTION HERE]
// */
//class CustomAdapter extends BaseAdapter {
//
//    private Context cntx;
//    private ArrayList<String> items = new ArrayList<>();
//
//    // Constructor
//    CustomAdapter(Context cntx) {
//        this.cntx = cntx;
//    }
//
//    void addItem(String packageName) {
//        items.add(packageName);
//    }
//
//    void clearAll() {
//        items.clear();
//    }
//
//
//    @Override
//    public int getCount() {
//        return items.size();
//    }
//
//    @Override
//    public String getItem(int i) {
//        return items.get(i);
//    }
//
//    @Override
//    public long getItemId(int i) {
//        return 0;
//    }
//
//    @Override
//    public View getView(int i, View convertView, ViewGroup viewGroup) {
//        ImageView imageView;
//        if (convertView == null) {
//            // if it's not recycled, initialize some attributes
//            imageView = new ImageView(cntx);
//            imageView.setPadding(8, 8, 8, 8);
//        } else {
//            imageView = (ImageView) convertView;
//        }
//
//        try {
//            imageView.setImageDrawable(cntx.getPackageManager().getApplicationIcon(items.get(i)));
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//            imageView.setImageResource(R.mipmap.ic_launcher);
//        }
//        return imageView;
//    }
//}
