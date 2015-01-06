package cs.sbu.sbapptracer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ViewInfo extends Activity {


    private ExpandableListAdapter expListAdapter;

    private ExpandableListView expListView;
    private TextView editTxt;
    private ArrayList<ExpandableListParentClass> systemCallCollection;


    private int pid;
    private String inputSyscall;
    private int option;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_info );

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        inputSyscall = bundle.getString("input");
        pid = Integer.parseInt(bundle.getString("pid"));
        option = Integer.parseInt(bundle.getString("option"));



        addListenerOnButton();


        systemCallCollection = new ArrayList<ExpandableListParentClass>();
        expListView = (ExpandableListView) findViewById(R.id.syscallList);
        expListAdapter = new ExpandableListAdapter(
                this, systemCallCollection);
        expListView.setAdapter(expListAdapter);
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                final String selected = (String) expListAdapter.getChild(
                        groupPosition, childPosition);
                Toast.makeText(getBaseContext(), selected, Toast.LENGTH_LONG)
                        .show();

                return true;
            }
        });

        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int previousItem = -1;

            @Override
            public void onGroupExpand(int groupPosition) {
                if(groupPosition != previousItem )
                    expListView.collapseGroup(previousItem );
                previousItem = groupPosition;
            }
        });



        editTxt = (TextView) findViewById(R.id.inputSearch);
        editTxt.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                System.out.println("Text ["+s+"]");
                if(s != null)
                    expListAdapter.filterData(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        execute();

    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            ExpandableListParentClass message = (ExpandableListParentClass) msg.obj;
            //Extract the string from the Message
            expListAdapter.add(message);
            expListAdapter.notifyDataSetChanged();

        }
    };

    public void addListenerOnButton() {



        Button clearButton = (Button) findViewById(R.id.btnClear);
        clearButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                expListAdapter.clear();
                System.out.println("Cleared the list");
            }

        });

        Button switchact =(Button)findViewById(R.id.btn2);
        switchact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent act1 = new Intent(view.getContext(),TracerActivity.class);
                startActivity(act1);
            }
        });

    }

    public void execute()
    {
        expListAdapter.clear();

        switch (option)
        {
            case 1:     new Thread(new ExecuteShell(pid,inputSyscall,1,handler)).start();
                        break;

            case 2:     new Thread(new ExecuteShell(pid,inputSyscall,2,handler)).start();
                        break;

            case 3:     new Thread(new ExecuteShell(pid,inputSyscall,3,handler)).start();
                        break;

            default:    new Thread(new ExecuteShell(pid,inputSyscall,0,handler)).start();
                        break;


        }


    }


}
