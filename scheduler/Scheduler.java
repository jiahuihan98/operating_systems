import java.util.*;
import java.io.*;

public class Scheduler {

    //read in process and store them in four array list
    public  static ArrayList<Process> FCFSarray = new ArrayList<>();
    public  static ArrayList<Process> RRarray = new ArrayList<>();
    public  static ArrayList<Process> SJFarray = new ArrayList<>();
    public  static ArrayList<Process> HPRNarray = new ArrayList<>();
    public  static Scanner inRan = null;

    public static void main(String[] args) {
        File inputFile;
        boolean verbose = false; //for detailed checking

        //check if there is verbose
        if (args.length > 1) {
            inputFile = new File(args[1]);
            verbose = true;
        } else {
            inputFile = new File(args[0]);
        }

        //read in file
        Scanner sc = null;
        try {
            sc = new Scanner (inputFile);
        } catch (FileNotFoundException e) {
            System.err.println("Error: the file "+ inputFile.getAbsolutePath()+ " cannot be opened for reading.\n");
            System.exit(1);
        }


        int numProcess = sc.nextInt();
        while (numProcess > 0) {
            String a = sc.next();
            int A;
            if (a.charAt(0) == '(') {
                A = Integer.parseInt(a.substring(1));
            }
            else {
                A = Integer.parseInt(a);
            }
            int B = sc.nextInt();
            int C = sc.nextInt();
            String m = sc.next();
            int M;
            if (m.charAt(m.length() - 1) == ')') {
                M = Integer.parseInt(m.substring(0, m.length() - 1));
            }
            else {
                M = Integer.parseInt(m);
            }

            FCFSarray.add(new Process(A, B, C, M));
            RRarray.add(new Process(A, B, C, M));
            SJFarray.add(new Process(A, B, C, M));
            HPRNarray.add(new Process(A, B, C, M));
            numProcess--;
        }

        try {
            inRan = new Scanner(new File("random-numbers.txt")); ;
        } catch (FileNotFoundException e) {
            System.err.println("Error: the file cannot be opened for reading.\n");
            System.exit(1);
        }
        FCFS(FCFSarray, verbose);
        inRan.close();

        try {
            inRan = new Scanner(new File("random-numbers.txt")); ;
        } catch (FileNotFoundException e) {
            System.err.println("Error: the file cannot be opened for reading.\n");
            System.exit(1);
        }
        RR(RRarray, verbose);
        inRan.close();

        try {
            inRan = new Scanner(new File("random-numbers.txt")); ;
        } catch (FileNotFoundException e) {
            System.err.println("Error: the file cannot be opened for reading.\n");
            System.exit(1);
        }
        SJF(SJFarray, verbose);
        inRan.close();

        try {
            inRan = new Scanner(new File("random-numbers.txt")); ;
        } catch (FileNotFoundException e) {
            System.err.println("Error: the file cannot be opened for reading.\n");
            System.exit(1);
        }
        HPRN(HPRNarray, verbose);
        inRan.close();
    }

    //random number function
    public static int randomOS(int U) {
        int x = inRan.nextInt();
        //System.out.println("Find burst when choosing ready process to run " + x);
        int result = 1 + (x % U);
        return result;
    }

    public static void FCFS(ArrayList<Process> processes, Boolean verbose) {
        int num = processes.size();
        String s = "";
        for (Process p: processes) {
            s += p.toString() + ' ';
        }
        System.out.println("The original input was: " + num + " " + s);
        Collections.sort(processes);
        String ss = "";
        for (Process p: processes) {
            ss += p.toString() + ' ';
        }
        System.out.println("The (sorted) input is:  " + num + " " + ss);

        //initialize things
        int cycle = 0;
        int CPUutil = 0;
        int IOUtil = 0;
        ArrayList<Process> readyQ = new ArrayList<>();
        ArrayList<Process> toBeReadyQ = new ArrayList<>();
        ArrayList<Process> blockedQ = new ArrayList<>();
        Process curP = null;

        //if we need the verbose version
        if (verbose) {
            System.out.println("\nThis detailed printout gives the state and remaining burst for each process\n");
        }

        while (num > 0) {

            //print format for verbose
            if (verbose) {
                verbosePrint(processes, cycle);
            }

            //check blockedQ
            if (blockedQ.size() > 0) {
                IOUtil++;
                for (Process p: blockedQ) {
                    p.IOburst--;
                    p.IOtime++;
                    if (p.IOburst <= 0) {
                        p.state = 2;
                        toBeReadyQ.add(p);
                    }
                }
                for (Process p: toBeReadyQ) {
                    blockedQ.remove(p);
                }
            }

            //check running
            if (curP != null) {
                curP.CPUburst--;
                curP.CPUtime++;
                CPUutil++;
                if (curP.CPUtime < curP.C && curP.CPUburst <=0) {
                    curP.state = 3;
                    blockedQ.add(curP);
                    curP = null;
                }
                if (curP != null && curP.CPUtime >= curP.C){
                    curP.state = 4;
                    curP.finishedTime = cycle;
                    curP = null;
                    num--;
                }
            }

            //check arriving process
            for (Process p : processes) {
                if (p.A == cycle) {
                    p.state = 2;
                    toBeReadyQ.add(p);
                }
            }

            //after the above, we want to sort the toBeReady queue so that it is in the correct order
            Collections.sort(toBeReadyQ);
            //then check for the Breaking ties rule!
            for (int i = 1; i < toBeReadyQ.size(); i++) {
                if (toBeReadyQ.get(i - 1).A == toBeReadyQ.get(i).A && (processes.indexOf(toBeReadyQ.get(i-1)) > processes.indexOf(toBeReadyQ.get(i)))) {
                    Collections.swap(toBeReadyQ, i-1, i);
                }
            }
            //Since we want to add the
            for (Process p : toBeReadyQ) {
                readyQ.add(p);
            }
            toBeReadyQ.clear();

            //checked ready
            if (readyQ.size() > 0) {
                if (curP == null) {
                    curP = readyQ.get(0);
                    readyQ.remove(0);
                    curP.state = 1;
                    curP.CPUburst = randomOS(curP.B);
                    if (curP.CPUburst > (curP.C - curP.CPUtime)) {
                        curP.CPUburst = curP.C - curP.CPUtime;
                    }
                    curP.IOburst = curP.CPUburst * curP.M;
                }
                for (Process p: readyQ) {
                    p.waitTime++;
                }
            }

            cycle++;


        }

        System.out.println("\nThe scheduling algorithm used was First Come First Serve\n");
        eachProcessSummaryPrint(processes);
        summaryPrint(processes, cycle, CPUutil, IOUtil);

    }

    public static void RR(ArrayList<Process> processes, boolean verbose) {
        int num = processes.size();
        String s = "";
        for (Process p: processes) {
            s += p.toString() + ' ';
        }
        System.out.println("The original input was: " + num + " " + s);
        Collections.sort(processes);
        String ss = "";
        for (Process p: processes) {
            ss += p.toString() + ' ';
        }
        System.out.println("The (sorted) input is:  " + num + " " + ss);

        //initialize things
        int cycle = 0;
        int quantum = 3;
        int CPUutil = 0;
        int IOUtil = 0;
        ArrayList<Process> readyQ = new ArrayList<>();
        ArrayList<Process> toBeReadyQ = new ArrayList<>();
        ArrayList<Process> blockedQ = new ArrayList<>();
        Process curP = null;

        //if we need the verbose version
        if (verbose) {
            System.out.println("\nThis detailed printout gives the state and remaining burst for each process\n");
        }

        while (num > 0) {

            //print format for verbose
            //this print is different from the general print
            if (verbose) {
                System.out.printf("Before Cycle %5d:    ", cycle);
                for (Process p : processes) {
                    switch (p.state) {
                        case 0:
                            System.out.print("      unstarted  0 ");
                            break;
                        case 1:
                            System.out.printf("        running %2d ", Math.min(quantum, p.CPUburst));
                            break;
                        case 2:
                            System.out.print("          ready  0 ");
                            break;
                        case 3:
                            System.out.printf("        blocked %2d ", p.IOburst);
                            break;
                        default:
                            System.out.print("     terminated  0 ");
                            break;
                    }
                }
                System.out.println();
            }

            //check blockedQ
            if (blockedQ.size() > 0) {
                IOUtil++;
                for (Process p: blockedQ) {
                    p.IOburst--;
                    p.IOtime++;
                    if (p.IOburst <= 0) {
                        p.state = 2;
                        toBeReadyQ.add(p);
                    }
                }
                for (Process p: toBeReadyQ) {
                    blockedQ.remove(p);
                }
            }

            quantum--;
            System.out.println(quantum);
            //check running
            if (curP != null) {
                curP.CPUburst--;
                curP.CPUtime++;
                CPUutil++;
                if (curP.CPUtime >= curP.C) { //meaning that it is done no matter what
                    curP.state = 4;
                    curP.finishedTime = cycle;
                    curP = null;
                    num--;
                }
                if (curP != null) {
                    if (quantum == 0) {
                        if (curP.CPUburst <= 0) { //meaning it should be blocked
                            curP.state = 3;
                            blockedQ.add(curP);
                            curP = null;
                        }
                        else { //meaning it should be moved to ready
                            curP.state = 2;
                            toBeReadyQ.add(curP);
                            curP = null;
                        }
                    }
                    if (quantum == 1) {
                        if (curP.CPUburst <= 0) { //meaning it should be blocked
                            curP.state = 3;
                            blockedQ.add(curP);
                            curP = null;
                        }
                    }
                }


            }

            //check arriving process
            for (Process p : processes) {
                if (p.A == cycle) {
                    p.state = 2;
                    toBeReadyQ.add(p);
                }
            }

            //after the above, we want to sort the toBeReady queue so that it is in the correct order
            Collections.sort(toBeReadyQ);
            //then check for the Breaking ties rule!
            for (int i = 1; i < toBeReadyQ.size(); i++) {
                if (toBeReadyQ.get(i - 1).A == toBeReadyQ.get(i).A && (processes.indexOf(toBeReadyQ.get(i-1)) > processes.indexOf(toBeReadyQ.get(i)))) {
                    Collections.swap(toBeReadyQ, i-1, i);
                }
            }
            //Since we want to add the
            for (Process p : toBeReadyQ) {
                readyQ.add(p);
            }
            toBeReadyQ.clear();

            //checked ready
            if (readyQ.size() > 0) {
                if (curP == null) {
                    curP = readyQ.get(0);
                    readyQ.remove(0);
                    curP.state = 1;
                    if (curP.CPUburst == 0) {
                        curP.CPUburst = randomOS(curP.B);
                        if (curP.CPUburst > (curP.C - curP.CPUtime)) {
                            curP.CPUburst = curP.C - curP.CPUtime;
                        }
                        curP.IOburst = curP.CPUburst * curP.M;
                    }
                }
                for (Process p: readyQ) {
                    p.waitTime++;
                }
            }

            if (quantum == 0) {
                quantum = 2;
            }

            cycle++;

        }
        System.out.println("\nThe scheduling algorithm used was Round Robbin\n");
        eachProcessSummaryPrint(processes);
        summaryPrint(processes, cycle, CPUutil, IOUtil);

    }

    public static void SJF(ArrayList<Process> processes, boolean verbose) {
        int num = processes.size();
        String s = "";
        for (Process p: processes) {
            s += p.toString() + ' ';
        }
        System.out.println("The original input was: " + num + " " + s);
        Collections.sort(processes);
        String ss = "";
        for (Process p: processes) {
            ss += p.toString() + ' ';
        }
        System.out.println("The (sorted) input is:  " + num + " " + ss);

        //initialize things
        int cycle = 0;
        int CPUutil = 0;
        int IOUtil = 0;
        ArrayList<Process> readyQ = new ArrayList<>();
        ArrayList<Process> toBeReadyQ = new ArrayList<>();
        ArrayList<Process> blockedQ = new ArrayList<>();
        Process curP = null;

        //if we need the verbose version
        if (verbose) {
            System.out.println("\nThis detailed printout gives the state and remaining burst for each process\n");
        }

        while (num > 0) {

            //print format for verbose
            if (verbose) {
                verbosePrint(processes, cycle);
            }

            //check blockedQ
            if (blockedQ.size() > 0) {
                IOUtil++;
                for (Process p: blockedQ) {
                    p.IOburst--;
                    p.IOtime++;
                    if (p.IOburst <= 0) {
                        p.state = 2;
                        toBeReadyQ.add(p);
                    }
                }
                for (Process p: toBeReadyQ) {
                    blockedQ.remove(p);
                    readyQ.add(p);
                }
                toBeReadyQ.clear();
            }

            //check running
            if (curP != null) {
                curP.CPUburst--;
                curP.CPUtime++;
                CPUutil++;
                if (curP.CPUtime < curP.C && curP.CPUburst <=0) {
                    curP.state = 3;
                    blockedQ.add(curP);
                    curP = null;
                }
                if (curP != null && curP.CPUtime >= curP.C){
                    curP.state = 4;
                    curP.finishedTime = cycle;
                    curP = null;
                    num--;
                }
            }

            //check arriving process
            for (Process p : processes) {
                if (p.A == cycle) {
                    p.state = 2;
                    readyQ.add(p);
                }
            }

            //checked ready
            if (readyQ.size() > 0) {
                //firstly sort by arrival time
                Collections.sort(readyQ);
                if (curP == null) {
                    int min = Integer.MAX_VALUE;
                    Process temp = null;
                    //find the process in ready queue that has the shortest remaining time
                    for (Process p : readyQ){
                        if ((p.C - p.CPUtime) < min) {
                            temp = p;
                            min = (temp.C - temp.CPUtime);
                        }
                    }
                    curP = temp;
                    readyQ.remove(curP);
                    curP.state = 1;
                    if (curP.CPUburst == 0) {
                        curP.CPUburst = randomOS(curP.B);
                        if (curP.CPUburst > (curP.C - curP.CPUtime)) {
                            curP.CPUburst = curP.C - curP.CPUtime;
                        }
                        curP.IOburst = curP.CPUburst * curP.M;
                    }
                }
                for (Process p: readyQ) {
                    p.waitTime++;
                }

            }

            cycle++;

        }

        System.out.println("\nThe scheduling algorithm used was Non-preemptive Shortest Job First\n");
        eachProcessSummaryPrint(processes);
        summaryPrint(processes, cycle, CPUutil, IOUtil);

    }

    public static void HPRN(ArrayList<Process> processes, boolean verbose) {
        int num = processes.size();
        String s = "";
        for (Process p: processes) {
            s += p.toString() + ' ';
        }
        System.out.println("The original input was: " + num + " " + s);
        Collections.sort(processes);
        String ss = "";
        for (Process p: processes) {
            ss += p.toString() + ' ';
        }
        System.out.println("The (sorted) input is:  " + num + " " + ss);

        //initialize things
        int cycle = 0;
        int CPUutil = 0;
        int IOUtil = 0;
        ArrayList<Process> readyQ = new ArrayList<>();
        ArrayList<Process> toBeReadyQ = new ArrayList<>();
        ArrayList<Process> blockedQ = new ArrayList<>();
        Process curP = null;

        //if we need the verbose version
        if (verbose) {
            System.out.println("\nThis detailed printout gives the state and remaining burst for each process\n");
        }

        while (num > 0) {

            //print format for verbose
            if (verbose) {
                verbosePrint(processes, cycle);
            }

            //check blockedQ
            if (blockedQ.size() > 0) {
                IOUtil++;
                for (Process p: blockedQ) {
                    p.IOburst--;
                    p.IOtime++;
                    if (p.IOburst <= 0) {
                        p.state = 2;
                        toBeReadyQ.add(p);
                    }
                }
                for (Process p: toBeReadyQ) {
                    blockedQ.remove(p);
                    readyQ.add(p);
                }
                toBeReadyQ.clear();
            }

            //check running
            if (curP != null) {
                curP.CPUburst--;
                curP.CPUtime++;
                CPUutil++;
                if (curP.CPUtime < curP.C && curP.CPUburst <=0) {
                    curP.state = 3;
                    blockedQ.add(curP);
                    curP = null;
                }
                if (curP != null && curP.CPUtime >= curP.C){
                    curP.state = 4;
                    curP.finishedTime = cycle;
                    curP = null;
                    num--;
                }
            }

            //check arriving process
            for (Process p : processes) {
                if (p.A == cycle) {
                    p.state = 2;
                    readyQ.add(p);
                }
            }

            if (readyQ.size() > 0) {
                //firstly sort by arrival time
                Collections.sort(readyQ);
                if (curP == null) {
                    float max = -1;
                    Process temp = null;

                    for (Process p: readyQ) {
                        int T = (cycle - p.A);
                        int t = Math.max(1, p.CPUtime);
                        float ratio = (float) T / t;
                        if (ratio > max) {
                            temp = p;
                            max = ratio;
                        }
                    }
                    curP = temp;
                    readyQ.remove(curP);
                    curP.state = 1;
                    if (curP.CPUburst == 0) {
                        curP.CPUburst = randomOS(curP.B);
                        if (curP.CPUburst > (curP.C - curP.CPUtime)) {
                            curP.CPUburst = curP.C - curP.CPUtime;
                        }
                        curP.IOburst = curP.CPUburst * curP.M;
                    }
                }
                for (Process p: readyQ) {
                    p.waitTime++;
                }

            }

            cycle++;

        }

        System.out.println("\nThe scheduling algorithm used was Highest Penalty Ratio Next\n");
        eachProcessSummaryPrint(processes);
        summaryPrint(processes, cycle, CPUutil, IOUtil);

    }

    public static void verbosePrint(ArrayList<Process> processes, int cycle) {
        System.out.printf("Before Cycle %5d:    ", cycle);
        for (Process p : processes) {
            switch (p.state) {
                case 0:
                    System.out.print("      unstarted  0 ");
                    break;
                case 1:
                    System.out.printf("        running %2d ", p.CPUburst);
                    break;
                case 2:
                    System.out.print("          ready  0 ");
                    break;
                case 3:
                    System.out.printf("        blocked %2d ", p.IOburst);
                    break;
                default:
                    System.out.print("     terminated  0 ");
                    break;
            }
        }
        System.out.println();
    }

    public static void eachProcessSummaryPrint(ArrayList<Process> processes) {
        for (int i = 0; i < processes.size(); i++) {
            System.out.printf("Process %d:\n", i);
            Process p = processes.get(i);
            System.out.println("    (A,B,C,M) = " + p);
            System.out.println("    Finishing time: " + p.finishedTime);
            System.out.println("    Turnaround time: " + (p.finishedTime - p.A));
            System.out.println("    I/O time: " + p.IOtime);
            System.out.println("    Waiting time: " + p.waitTime);
        }
    }

    public static void summaryPrint(ArrayList<Process> processes, int cycle, int CPUutil, int IOUtil) {
        int turnaroundTime = 0;
        int waitingTime = 0;
        for (Process p: processes) {
            turnaroundTime += (p.finishedTime - p.A);
            waitingTime += p.waitTime;
        }
        //do calculations
        float TotalCPUutil = (float) CPUutil / (cycle - 1);
        float TotalIOUtil = (float) IOUtil / (cycle - 1);

        System.out.println("Summary Data:");
        System.out.println("    Finishing time: " + (cycle - 1));
        System.out.println("    CPU Utilization: " + TotalCPUutil);
        System.out.println("    I/O Utilization: " + TotalIOUtil);
        System.out.println("    Throughput: " + (((float)100 * processes.size())/(cycle-1)) + " processes per hundred cycles");
        System.out.println("    Average turnaround time: " + ((float)turnaroundTime/processes.size()));
        System.out.println("    Average waiting time: " + ((float)waitingTime/processes.size()));
        System.out.println("\n");

    }
}