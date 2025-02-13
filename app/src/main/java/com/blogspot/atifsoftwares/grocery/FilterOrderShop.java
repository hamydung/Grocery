package com.blogspot.atifsoftwares.grocery;

import android.widget.Filter;

import com.blogspot.atifsoftwares.grocery.adapters.AdapterOrderShop;
import com.blogspot.atifsoftwares.grocery.adapters.AdapterProductSeller;
import com.blogspot.atifsoftwares.grocery.modles.ModelOrderShop;
import com.blogspot.atifsoftwares.grocery.modles.ModelProduct;

import java.util.ArrayList;


public class FilterOrderShop extends Filter {

    private AdapterOrderShop adapter;
    private ArrayList<ModelOrderShop> filterList;

    public FilterOrderShop(AdapterOrderShop adapter, ArrayList<ModelOrderShop> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //validate data for search query
        if(constraint != null && constraint.length()>0) {
            //search field not empty, searching something, perform search


            //change to upper case, to make case insensitive
            constraint = constraint.toString().toUpperCase();
            //store our filtered list
            ArrayList<ModelOrderShop> filteredModels = new ArrayList<>();
            for (int i = 0; i <filterList.size(); i++) {
                //check, search by title and category
                if (filterList.get(i).getOrderStatus().toUpperCase().contains(constraint) ) {
                    //add filtered data to list
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;

        }
        else {
            //search file empty, not searching, return original/all/complete list
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.orderShopArrayList = (ArrayList<ModelOrderShop>) results.values;
        //refresh adapter
        adapter.notifyDataSetChanged();
    }
}
