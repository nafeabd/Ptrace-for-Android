package cs.sbu.sbapptracer;

import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by gauth on 16/11/14.
 */
public class ExecuteShell implements Runnable{

    private int id;
    private Handler h;
    private String inputVal;
    private int option;
    ExecuteShell(int id, String inputVal,int option,Handler h)
    {
        this.id = id;
        this.inputVal = inputVal;
        this.h = h;
        this.option = option;
    }
    @Override
    public void run() {

        executeCommandLine();

    }

    int executeCommandLine()
    {
        System.out.println("App id = " + id);
        System.out.println("App id = " + inputVal);
        Runtime r = Runtime.getRuntime();
        Message m =new Message();
        String []temp;
        try {

            Process p = null;
            if(inputVal.isEmpty() && option == 0) {
                System.out.println("no name given");
                p = r.exec(new String[]{"/system/bin/sh", "-c", "su -c 'tracer " + id + " & sleep 5'"});
            }
            else if(option == 1) {

                System.out.println("Get the count of instructions");
                p = r.exec(new String[]{"/system/bin/sh", "-c", "su -c 'tracer -c " + id + " & sleep 5'"});
            }
            else if (option == 2){
                System.out.println("kill the app when at first occurence of syscall"+inputVal);
                try
                {
                    int syscallNo = Integer.parseInt(inputVal);
                    p = r.exec(new String[]{"/system/bin/sh","-c","su -c 'tracer -ks "+syscallNo+
                                "  " + id + " & sleep 5'"});
                }
                catch(Exception e)
                {
                    p = r.exec(new String[]{"/system/bin/sh","-c","su -c 'tracer -kn "+inputVal+
                                "  " + id + " & sleep 5'"});
                }
            }else
            {
                try
                {
                    System.out.println(" name given");
                    int syscallNo = Integer.parseInt(inputVal);
                    p = r.exec(new String[]{"/system/bin/sh","-c","su -c 'tracer -s "+syscallNo+" "
                            + id + " & sleep 5'"});
                }
                catch(Exception e)
                {
                    p = r.exec(new String[]{"/system/bin/sh","-c","su -c 'tracer -n "+inputVal+" "+id + " & sleep 5'"});
                }
            }
            p.waitFor();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line = null;
            ExpandableListParentClass syscallInfo;
            while ((line = in.readLine()) != null) {
                System.out.println(line);

                String pattern = "(\\w+)\\((.*)\\)\\s+=\\s*(.*)";
                Pattern pat = Pattern.compile(pattern);

                Matcher mat = pat.matcher(line);
                if (mat.find()) {
                       String syscallName = mat.group(1);
                       final String arguments = mat.group(2);
                       final String retval = mat.group(3);

                    syscallInfo = new ExpandableListParentClass();

                    System.out.println("Syscall_name:"+syscallName);
                    System.out.println("arguments:"+arguments);
                    System.out.println("retval:"+retval);

                    syscallInfo.setParent(syscallName);
                    final String finalArguments = arguments;
                    syscallInfo.setParentChildren(new ArrayList<String>() {{
                        add("Arguments: "+finalArguments);
                        add("Return: "+retval);
                    }});
                    m =new Message();
                    m.obj = syscallInfo;
                    m.setTarget(h);
                    m.sendToTarget();
                }
                else {
                    System.out.println("NO MATCH");
                    if (option == 1)
                    {

                        pattern = "Counter value:(\\d+)";
                        pat = Pattern.compile(pattern);
                        System.out.println("Checking for Counter Value: number");

                        mat = pat.matcher(line);
                        if(mat.find())
                        {
                            System.out.println("Match Found");

                            syscallInfo = new ExpandableListParentClass();
                            syscallInfo.setParent("TOTAL INSTRUCTION COUNT");
                            final String cnt = mat.group(1);
                            syscallInfo.setParentChildren((new ArrayList<String>(){{add(cnt);}} ));
                            m =new Message();
                            m.obj = syscallInfo;
                            m.setTarget(h);
                            m.sendToTarget();
                        }
                    }
                }
            }
            System.out.println("Path:"+System.getenv("PATH"));
            System.out.println("current dir:"+System.getProperty("user.dir"));
            System.out.println("after strace ls");

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return 1;
    }
}
