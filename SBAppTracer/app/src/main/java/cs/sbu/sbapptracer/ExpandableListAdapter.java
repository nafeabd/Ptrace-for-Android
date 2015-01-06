package cs.sbu.sbapptracer;

/**
 * Created by gauth on 16/11/14.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

public class ExpandableListAdapter extends BaseExpandableListAdapter implements Filterable {

    private Activity context;
    private ArrayList<ExpandableListParentClass> parent;
    private ArrayList<ExpandableListParentClass> origParent;
    public ArrayList<ExpandableListParentClass> getMParent()
    {
        return parent;
    }

    public ExpandableListAdapter(Activity context,
                                 ArrayList<ExpandableListParentClass> parentList) {
        this.context = context;
        this.parent = parentList;
        this.origParent = new ArrayList<ExpandableListParentClass>(parentList);
    }

    public Object getChild(int groupPosition, int childPosition) {
        return parent.get(groupPosition).getParentChildren().get(childPosition);
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String laptop = (String) getChild(groupPosition, childPosition);
        LayoutInflater inflater = context.getLayoutInflater();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.expndlayout, null);
        }

        TextView item = (TextView) convertView.findViewById(R.id.syscall);

        item.setText(laptop);
        return convertView;
    }

    public int getChildrenCount(int groupPosition) {
        int size = 0;
        if(parent.get(groupPosition).getParentChildren() != null)
            size = parent.get(groupPosition).getParentChildren().size();

        return size;
    }

    public Object getGroup(int groupPosition) {
        return parent.get(groupPosition).getParent();
    }

    public int getGroupCount() {
        return parent.size();
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String laptopName = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.expndlayout,
                    null);
        }
        TextView item = (TextView) convertView.findViewById(R.id.syscall);
        item.setTypeface(null, Typeface.BOLD);
        item.setText(laptopName);
        return convertView;
    }

    public boolean hasStableIds() {
        return true;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public void add(ExpandableListParentClass message) {

        this.parent.add(message);
        this.origParent.add(message);
    }

    public void filterData(String query) {

        query = query.toLowerCase();
        parent.clear();
        if (query.isEmpty())
            parent.addAll(origParent);
        else {
            for (ExpandableListParentClass p : origParent) {

                if (p.getParent().toUpperCase().startsWith(query.toUpperCase()))
                    parent.add(p);
            }
        }
        notifyDataSetChanged();
    }

    public void clear() {

        parent.clear();
        origParent.clear();
        notifyDataSetChanged();
    }
}