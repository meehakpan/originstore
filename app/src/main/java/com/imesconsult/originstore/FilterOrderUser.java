package com.imesconsult.originstore;

import android.widget.Filter;

import com.imesconsult.originstore.adapters.AdapterOrderUser;
import com.imesconsult.originstore.models.ModelOrderUser;

import java.util.ArrayList;

public class FilterOrderUser extends Filter {
    private AdapterOrderUser adapter;
    private ArrayList<ModelOrderUser> filterList;

    public FilterOrderUser(AdapterOrderUser adapter, ArrayList<ModelOrderUser> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results=new FilterResults();
        //validate data for search query
        if(constraint!=null && constraint.length()>0){
            //search field not empty, searching something, perform search

            //change to uppercase, to make case insensitive
            constraint=constraint.toString().toUpperCase();
            //store our filtered list
            ArrayList<ModelOrderUser>filteredModels=new ArrayList<>();
            for(int i=0;i<filterList.size();i++){
                //check, search by title and category
                if(filterList.get(i).getOrderStatus().toUpperCase().contains(constraint)){

                    //add filtered data to list
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count=filteredModels.size();
            results.values=filteredModels;
        }else{
            //search field empty, not searching ,return original / all/ complete list
            results.count=filterList.size();
            results.values=filterList;

        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.orderUserList=(ArrayList<ModelOrderUser>)results.values;
        //refresh adapter
        adapter.notifyDataSetChanged();
    }
}
