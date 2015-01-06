Ptrace-for-Android
==================

A user level debugging application which can trace a running application in Android.

1.Introduction:

Ptrace is one the most important system call available in linux distributions.Most of the debugging utilities
like gdb and other common debugging practices involve the usage of Ptrace at great extent.On the other hand considering
the fact that android is build on top of linux , android equally enjoys the in built-implementation of Ptrace.But there
are very limited number of ready made tools/utilities available that leverage the efficiency of Ptrace in Android.So in 
this project we design a Application Tracer which essentially works on android platform which has the capability to trace
the application interactions with the kernel.Our tracer lists all the interactions which are basically system calls with
enough information for any debugger.Our tracer provides some other additional functionalities  which enable 
a user to inspect/monitor closely a particular system call,can kill a process when suspicious system call occurs and can 
get the count of machine level instruction executed by any application.

2.Design Principles:

Our design principles are based on the perspective of a typical debugger.


3.Implementation Details:

The application/tool can be available in two forms.One is in the command line form and other as an android app
built on Android kitkat(API 19).Our application supports the android OS running on both ARM and X86 architectures.
Ptrace is complex system call which can perform different operations depending on the type of requests it takes.We have
used some of the Ptrace variants to accomplish our tasks.Below are the features we are supporting from our tool.

	1.Tracing the System calls:
		Given a executable or child process id , tracer app gives list of all system calls executed by that 
		application at given point of time.Along with system call number we retrieve system call arguments
		and return values.

	2.Inspecting or Monitoring a particular system call:
		When a user specifies a system call no of interest , system blocks/waits which enable the user to closely
		monitor that particular system call.

	3.Killing a child:
		When an application is running abruptly and performing suspicious activities , say for example performing
		unlink system call(which application shouldn't),user can input system call number and application gets 
		terminated by SIGKILL when it encounters this system call.

	4.Instruction count:
		By this feature user can count the number of machine level instructions executed by an application.

	5.Smart Search:
		From the given list of system call details , user can search/filter the ones he is interested in.


4.Usage:

	Command line usage:
		1.Tracing system calls:
			-tracer PID/EXEC
		2.Inspect/Monitor a system call:
			- tracer -n systemcallname PID/EXEC
			- tracer -s systemcallno PID/EXEC
		3.Kill a proces:
			-tracer -c PID/EXEC
		4.Instruction count:
			-tracer -k systemcallno PID/EXEC
	
	App Usage:
		User friendly - interactive app

5.Acknowledgements:

	For our implementation we had to collect all system calls metadata like system call numbers,names,arguments
	for different architectures,so we have used existing script available at https://github.com/nelhage/ministrace.
	We did minor changes to the python script to fit into arm architecture. In addition to that we are inspired 
	by the implementation of ministrace.c from same github repository and borrowed some part of code from it by 
	making changes whereever necessary.



		

		


