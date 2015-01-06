package cs.sbu.sbapptracer;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class TracerActivity extends Activity {

    private Spinner spinner1;
    private Button btnSubmit;
    private List<String> list1;
    private List<ActivityManager.RunningAppProcessInfo> procInfos;

    private ActivityManager actvityManager;
    private ArrayAdapter<String> dataAdapter;

    private final Context context = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracer);
        actvityManager = (ActivityManager)
                this.getSystemService( ACTIVITY_SERVICE );
        addItemsOnSpinner2();
        addListenerOnButton();


        Button refreshList = (Button) findViewById(R.id.referesh);
        refreshList.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                procInfos = actvityManager.getRunningAppProcesses();

                dataAdapter.clear();
                for(int i = 0; i < procInfos.size(); i++)
                    dataAdapter.add(procInfos.get(i).processName);

                dataAdapter.notifyDataSetChanged();

            }

        });
    }

    // add items into spinner dynamically
    public void addItemsOnSpinner2() {

        spinner1 = (Spinner) findViewById(R.id.spinner1);
        list1 = new ArrayList<String>();

        procInfos = actvityManager.getRunningAppProcesses();

        for(int i = 0; i < procInfos.size(); i++)
            list1.add(procInfos.get(i).processName);

        dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list1);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner1.setAdapter(dataAdapter);



    }

    // get the selected dropdown list value
    public void addListenerOnButton() {

        spinner1 = (Spinner) findViewById(R.id.spinner1);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                System.out.println("selected item = " + spinner1.getSelectedItem().toString());
                String processName = spinner1.getSelectedItem().toString();
                int pid = 0;
                for (int i = 0; i < procInfos.size(); i++)
                    if (procInfos.get(i).processName.equals(processName)) {
                        pid = (procInfos.get(i).pid);
                        break;
                    }
                System.out.println("selected Process Name = " + processName);
                System.out.println("selected Process ID = " + pid);
                System.out.println("creating new thread ");

                Bundle params = new Bundle();
                params.putString("pid", String.valueOf(pid));
                params.putString("option", String.valueOf(0));
                params.putString("input", "");

                invokeActivity(params);
                Toast.makeText(TracerActivity.this,
                        "OnClickListener : " +
                                "\nSpinner 2 : "+ String.valueOf(spinner1.getSelectedItem()),
                        Toast.LENGTH_SHORT).show();
            }

        });

        Button getCountButton = (Button) findViewById(R.id.btnCount);
        getCountButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                System.out.println("selected item = " + spinner1.getSelectedItem().toString());
                String processName = spinner1.getSelectedItem().toString();
                String inputSysCall = "";
                int pid = 0;
                for (int i = 0; i < procInfos.size(); i++)
                    if (procInfos.get(i).processName.equals(processName)) {
                        pid = (procInfos.get(i).pid);
                        break;
                    }
                System.out.println("selected Process Name = " + processName);
                System.out.println("selected Process ID = " + pid);
                System.out.println("selected Process syscall name = " + inputSysCall);
                System.out.println("creating new thread ");

                Bundle params = new Bundle();
                params.putString("pid", String.valueOf(pid));
                params.putString("option", String.valueOf(1));
                params.putString("input", inputSysCall);

                invokeActivity(params);
                Toast.makeText(TracerActivity.this,
                        "OnClickListener : " +
                                "\nSpinner 2 : "+ String.valueOf(spinner1.getSelectedItem()),
                        Toast.LENGTH_SHORT).show();
            }

        });



        Button killBy = (Button) findViewById(R.id.btn2);
        killBy.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                System.out.println("selected item = " + spinner1.getSelectedItem().toString());
                String processName = spinner1.getSelectedItem().toString();
                int pid = 0;
                for (int i = 0; i < procInfos.size(); i++)
                    if (procInfos.get(i).processName.equals(processName)) {
                        pid = (procInfos.get(i).pid);
                        break;
                    }
                System.out.println("selected Process Name = " + processName);
                System.out.println("selected Process ID = " + pid);
                System.out.println("creating new thread ");


                LayoutInflater li = LayoutInflater.from(context);
                View promptView = li.inflate(R.layout.promptwindow, null);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                alertDialog.setView(promptView);
                final EditText userInput = (EditText) promptView.
                                    findViewById(R.id.editTextDialogUserInput);
                final Bundle params = new Bundle();
                params.putString("pid", String.valueOf(pid));
                params.putString("option", String.valueOf(2));

                alertDialog
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        // get user input and set it to result
                                        // edit text
                                        params.putString("input", userInput.getText().toString());
                                        invokeActivity(params);
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog showDialog = alertDialog.create();

                // show it
                showDialog.show();
                Toast.makeText(TracerActivity.this,
                        "OnClickListener : " +
                                "\nSpinner 2 : "+ String.valueOf(spinner1.getSelectedItem()),
                        Toast.LENGTH_SHORT).show();
            }

        });

        Button inspect = (Button) findViewById(R.id.btnInspect);
        inspect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                System.out.println("selected item = " + spinner1.getSelectedItem().toString());
                String processName = spinner1.getSelectedItem().toString();
                int pid = 0;
                for (int i = 0; i < procInfos.size(); i++)
                    if (procInfos.get(i).processName.equals(processName)) {
                        pid = (procInfos.get(i).pid);
                        break;
                    }
                System.out.println("selected Process Name = " + processName);
                System.out.println("selected Process ID = " + pid);
                System.out.println("creating new thread ");

                final Bundle params = new Bundle();
                params.putString("pid", String.valueOf(pid));
                params.putString("option", String.valueOf(3));


                LayoutInflater li = LayoutInflater.from(context);
                View promptView = li.inflate(R.layout.promptwindow, null);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                alertDialog.setView(promptView);
                final EditText userInput = (EditText) promptView.
                        findViewById(R.id.editTextDialogUserInput);

                alertDialog
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        // get user input and set it to result
                                        // edit text
                                        params.putString("input", userInput.getText().toString());
                                        invokeActivity(params);
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog showDialog = alertDialog.create();

                showDialog.show();

                Toast.makeText(TracerActivity.this,
                        "OnClickListener : " +
                                "\nSpinner 2 : "+ String.valueOf(spinner1.getSelectedItem()),
                        Toast.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tracer, menu);
        return true;
    }

    public void invokeActivity(Bundle b)
    {
        Intent intent = new Intent(getApplicationContext(), ViewInfo.class);
        intent.putExtras(b);
        startActivity(intent);

    }
}




