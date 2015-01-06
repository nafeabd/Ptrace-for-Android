#include <sys/ptrace.h>
#include <sys/user.h>
#include <sys/wait.h>
#include <sys/types.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <errno.h>
#include <assert.h>
#include <string.h>
#include <ctype.h>
#include <stdlib.h>
#include "syscalls.h"

#define SYSCALL_TOT 381

#ifdef __amd64__
#include "syscallents_x86-64.h"
#elif __arm__
#include "syscallents_arm.h"
#else
#include "syscallents_x86.h"
#endif

#ifdef __amd64__
#define eax rax
#define orig_eax orig_rax
#elif __arm__
#define r0 0
#define r1 1
#define r2 2
#define r3 3
#define r4 4
#define r5 5
#define r7 7
#else
#endif

#define offsetof(a, b) __builtin_offsetof(a,b)
#define get_reg(child, name) __get_reg(child, offsetof(struct user, regs.name))

FILE *fp;
int counter = 0;
int request = 0;

long __get_reg(pid_t child, int off) {
    long val = ptrace(PTRACE_PEEKUSER, child, off);
    return val;
}
//To get register value when architecture is ARM
long get_reg_arm(pid_t child,int num){
    long val = ptrace(PTRACE_PEEKUSER, child, sizeof(long)*num);
    return val;
}

int wait_for_syscall(pid_t child) {

    int status;
    while (1) {
    if(request == 1)
    	ptrace(PTRACE_SINGLESTEP,child,0,0);
    else
    	ptrace(PTRACE_SYSCALL,child,0,0);
        waitpid(child, &status, 0);
        counter = counter + 1;
        if (WIFSTOPPED(status) && WSTOPSIG(status) & 0x80){
            return 0;
        }
        if (WIFEXITED(status))
            return 1;
        if (WIFSIGNALED(status) && WTERMSIG(status) == SIGKILL){
             fprintf(fp, "[stopped %d (%x)]\n", status, WSTOPSIG(status));
	         fprintf(stderr, "[stopped %d (%x)]\n", status, WSTOPSIG(status));
             return 1;
        }
    }
}

const char *syscall_name(int scn) {
    struct syscall_entry *ent;
    static char buf[128];
    if (scn <= MAX_SYSCALL_NUM) {
        ent = &syscalls[scn];
        if (ent->name)
            return ent->name;
    }
    snprintf(buf, sizeof buf, "sys_%d", scn);
    return buf;
}

long get_syscall_arg(pid_t child, int which) {
    switch (which) {
#ifdef __amd64__
    case 0: return get_reg(child, rdi);
    case 1: return get_reg(child, rsi);
    case 2: return get_reg(child, rdx);
    case 3: return get_reg(child, r10);
    case 4: return get_reg(child, r8);
    case 5: return get_reg(child, r9);

#elif __arm__
    case 0: return get_reg_arm(child,r0);
    case 1: return get_reg_arm(child,r1);
    case 2: return get_reg_arm(child,r2);
    case 3: return get_reg_arm(child,r3);
    case 4: return get_reg_arm(child,r4);
    case 5: return get_reg_arm(child,r5);
#else
    case 0: return get_reg(child, ebx);
    case 1: return get_reg(child, ecx);
    case 2: return get_reg(child, edx);
    case 3: return get_reg(child, esi);
    case 4: return get_reg(child, edi);
    case 5: return get_reg(child, ebp);


#endif
    default: return -1L;
    }
}

char *read_string(pid_t child, unsigned long addr) {
    char *val = malloc(4096);
    int allocated = 4096;
    int read = 0;
    unsigned long tmp;
    while (1) {
        if (read + sizeof tmp > allocated) {
            allocated *= 2;
            val = realloc(val, allocated);
        }
        tmp = ptrace(PTRACE_PEEKDATA, child, addr + read);
        if(errno != 0) {
            val[read] = 0;
            break;
        }
        memcpy(val + read, &tmp, sizeof tmp);
        if (memchr(&tmp, 0, sizeof tmp) != NULL)
            break;
        read += sizeof tmp;
    }
    return val;
}

void print_syscall_args(pid_t child, int num) {
    struct syscall_entry *ent = NULL;
    int nargs = SYSCALL_MAXARGS;
    int i;
    char *strval;

    if (num <= MAX_SYSCALL_NUM && syscalls[num].name) {
        ent = &syscalls[num];
        nargs = ent->nargs;
    }
    for (i = 0; i < nargs; i++) {
        long arg = get_syscall_arg(child, i);
        int type = ent ? ent->args[i] : ARG_PTR;
        switch (type) {
        case ARG_INT:
            fprintf(fp, "%ld", arg);
	        fprintf(stderr, "%ld", arg);
            break;
        case ARG_STR:
            strval = read_string(child, arg);
            fprintf(fp, "\"%s\"", strval);
	        fprintf(stderr, "\"%s\"", strval);
            free(strval);
            break;
        default:
            fprintf(fp, "0x%lx", arg);
	        fprintf(stderr, "0x%lx", arg);
            break;
        }
        if (i != nargs - 1)
            fprintf(fp, ", ");
	        fprintf(stderr, ", ");
    }
}

void print_syscall(pid_t child,int check_syscall,int killsyscall) {
    int num;
#ifdef __arm__
    num = get_reg_arm(child,r7);
#else
    num = get_reg(child, orig_eax);
#endif

    if(killsyscall == num){
	    ptrace(PTRACE_KILL, child, NULL, NULL);
    }
    fprintf(fp, "%s(", syscall_name(num));
    fprintf(stderr, "%s(", syscall_name(num));
    print_syscall_args(child, num);
    fprintf(fp, ") = ");
    fprintf(stderr, ") = ");

   if(check_syscall == num) {
	sleep(4);
    }

}

int do_trace(pid_t child,int check_syscall,int killsyscall) {
    int status;
    int retval;
    int e;
    e = waitpid(child, &status, 0);
    //wait(NULL);
    assert(WIFSTOPPED(status));
    ptrace(PTRACE_SETOPTIONS, child, 0, PTRACE_O_TRACESYSGOOD);
    while(1) {
        if (wait_for_syscall(child) != 0)
            break;
        print_syscall(child,check_syscall,killsyscall);
        if (wait_for_syscall(child) != 0)
            break;
#ifdef __arm__
    retval = get_reg_arm(child,r0);
#else
    retval = get_reg(child, eax);
#endif
    fprintf(fp, "%d\n", retval);
	fprintf(stderr, "%d\n", retval);
    }

    return 0;
}

int do_child(int argc, char **argv) {
    char *args [argc+1];
    int i;
    for (i=0;i<argc;i++)
        args[i] = argv[i];
    args[argc] = NULL;
    ptrace(PTRACE_TRACEME);
    kill(getpid(), SIGSTOP);
    return execvp(args[0], args);
}

int main(int argc, char **argv) {
    pid_t child;
    int j = 1;
    int flag = 0;
    int len,i,ret,k;
    int m = 1;
    char input[20];
    int syscall = -1;
    int killsyscall = -1;
    fp = fopen("/storage/sdcard/ptrace_log.txt","w");
    //fp = fopen("ptrace_log.txt","w");
    if (argc < 2) {
        fprintf(stderr, "Usage: %s [-s <syscall int>|-n <syscall name> |-c {count machine instructions}] <program> <args>\n", argv[0]);
        exit(1);
    }

  if(strcmp(argv[1], "-s") == 0) {
        syscall = atoi(argv[2]);
        j = 3;
        m = 3;
    }
     if(strcmp(argv[1], "-n") == 0) {
        char *syscallname = argv[2];
        struct syscall_entry *ent;
        int p;
        for(p = 0; p < SYSCALL_TOT; p++) {
            ent = &syscalls[p];

            if(strcmp(syscallname, ent->name) == 0) {
                syscall = p;
                break;
            }
        }

        if(syscall == -1) {
            fprintf(stderr, "Error: %s is an invalid syscall\n", argv[2]);
	    fprintf(fp, "Error: %s is an invalid syscall\n", argv[2]);
            exit(1);
        }

        j = 3;
	m = 3;
    }
    if(strcmp(argv[1],"-c")== 0){
        request = 1;
        j = 2;
	    m = 2;

    }
    else{
        request = 0;

    }

    if(strcmp(argv[1],"-ks")==0){
    	killsyscall = atoi(argv[2]);
	j = 3;
	m = 3;

    }

    if(strcmp(argv[1],"-kn")==0){
       char *syscallname = argv[2];
       struct syscall_entry *ent;
       int p;
       for(p = 0; p < SYSCALL_TOT; p++) {
           ent = &syscalls[p];

           if(strcmp(syscallname, ent->name) == 0) {
               killsyscall = p;
               break;
           }
       }

       j = 3;
       m = 3;

   }




    len = strlen(argv[m]);
    strcpy(input,argv[m]);
    for(i=0;i<len;i++)
    {
       if (!isdigit(input[i]))
        {
          flag = 1;
          break;

        }
    }


    if(flag == 1){
    	child = fork();
    if (child == 0) {
        return do_child(argc-j, argv+j);
    }
    else{
        k =  do_trace(child,syscall,killsyscall);
	fclose(fp);
	fprintf(stderr,"Counter value:%d\n",counter);
	return k;
    }
    }
    else
    {
	child = atoi(argv[m]);
    	ret = ptrace(PTRACE_ATTACH, child,NULL, NULL);
    	k= do_trace(child,syscall,killsyscall);
    	ptrace(child, PTRACE_DETACH, NULL, NULL);
	fprintf(stderr,"Counter value:%d\n",counter);
	   fclose(fp);
    	return k;

    }

}
