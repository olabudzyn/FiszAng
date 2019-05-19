package com.budzynska.fiszang.listview;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.budzynska.fiszang.R;
import com.budzynska.fiszang.basedata.DictionaryElement;

import java.util.List;

public class DictionaryElementList extends ArrayAdapter<DictionaryElement> {

    private Activity context;
    private List<DictionaryElement> elements;

    public DictionaryElementList(Activity context, List<DictionaryElement> elements){
        super(context, R.layout.dictionary_element_list_layout, elements);

        this.context = context;
        this.elements = elements;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();

        View listViewItem = inflater.inflate(R.layout.dictionary_element_list_layout, null, true);
        TextView textViewEnglishWord = listViewItem.findViewById(R.id.textViewEnglishWordList);
        TextView textViewPolishWord = listViewItem.findViewById(R.id.textViewPolishWordList);

        DictionaryElement element = elements.get(position);
        textViewEnglishWord.setText(element.getEnglishWord());
        textViewPolishWord.setText(element.getPolishWord());

        return listViewItem;
    }
}
