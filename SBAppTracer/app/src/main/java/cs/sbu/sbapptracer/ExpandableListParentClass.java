package cs.sbu.sbapptracer;

import java.util.ArrayList;

/**
 * Created by gauth on 16/11/14.
 */
public class ExpandableListParentClass{

    private String parent;
    private ArrayList<String> parentChildren;


    public ExpandableListParentClass() {
    }
    public ExpandableListParentClass(String parent, ArrayList<String> parentChildren) {
        this.parent = parent;
        this.parentChildren = parentChildren;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public ArrayList<String> getParentChildren() {
        return parentChildren;
    }

    public void setParentChildren(ArrayList<String> parentChildren) {
        this.parentChildren = parentChildren;
    }
}
