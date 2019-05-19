package com.budzynska.fiszang.listview;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.budzynska.fiszang.R;
import com.budzynska.fiszang.basedata.Dictionary;

import java.util.List;

public class DictionaryList  extends ArrayAdapter<Dictionary> {

    private Activity context;
    private List<Dictionary> dictionaries;

    public DictionaryList(Activity context, List<Dictionary> dictionaries){
        super(context, R.layout.dictionary_list_layout, dictionaries);

        this.context = context;
        this.dictionaries = dictionaries;
    }

    @Override
    public View getView(int position, View convertView,  ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();

        View listViewItem = inflater.inflate(R.layout.dictionary_list_layout, null, true);
        TextView textViewDictionaryName = listViewItem.findViewById(R.id.textViewDictionaryName);

        Dictionary dictionary = dictionaries.get(position);
        textViewDictionaryName.setText(dictionary.getDictionaryName());

        return listViewItem;
    }
}
