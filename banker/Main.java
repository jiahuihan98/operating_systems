import java.io.*;
import java.util.*;

public class Main {

    //initialization to store information
    public static int numTask, numResource;
    public static ArrayList<Task> taskQ = new ArrayList<>(); // all the tasks (running,blocked,finished)
    public static ArrayList<Integer> unitResource = new ArrayList<>();

    public static void main(String[] args) {
        //read in file, error checking
        File inputFile = new File(args[0]);
        read(inputFile);
        FIFO();

        //clear everything from FIFO, for BANKER
        numTask = 0;
        numResource = 0;
        taskQ.clear();
        unitResource.clear();

        read(inputFile);
        Banker();
    }

    //this method read in information from the input file than process and store
    public static void read(File inputFile) {

        Scanner sc = null;
        try {
            sc = new Scanner (inputFile);
        } catch (FileNotFoundException e) {
            System.err.println("Error: the file "+ inputFile.getAbsolutePath()+ " cannot be opened for reading.\n");
            System.exit(1);
        }

        numTask = sc.nextInt();
        numResource = sc.nextInt();
        while (sc.hasNextInt()){
            unitResource.add(sc.nextInt());
        }

        // a hash-map checking whether a task has been initialized before
        HashMap<Integer,Task> checkTask = new HashMap<>();
        for (int i = 1; i < numTask + 1; i++){
            checkTask.put(i, null);
        }

        //store activities for each different task
        while(sc.hasNext()){
            Activity curActivity = new Activity(sc.next(), sc.nextInt(), sc.nextInt(), sc.nextInt());
            int curID = curActivity.taskID;
            if (checkTask.get(curID) == null){
                Task t = new Task(curID, numResource);
                taskQ.add(t);
                t.activities.add(curActivity);
                checkTask.remove(curID);
                checkTask.put(curID, t);
            }else{
                Task t = checkTask.get(curID);
                t.activities.add(curActivity);
            }
        }
    }

    //Do optimistic resource manager, FIFO algo here
    @SuppressWarnings("unchecked")
    public static void FIFO() {
        int cycle = 0;

        //Queues to keep track of the current stage
        ArrayList<Task> runningQ;
        ArrayList<Task> blockedQ = new ArrayList<>();
        ArrayList<Task> unblockedQ = new ArrayList<>();
        ArrayList<Task> finishedQ = new ArrayList<>();

        //To keep track of the available resources, and resources that are freed in each cycle
        ArrayList<Integer> availableR = new ArrayList<>();
        ArrayList<Integer> freeR = new ArrayList<>();

        //clone the queue of all task as running queue first
        runningQ = (ArrayList<Task>) taskQ.clone();

        //Initialize available and freed resource
        for (int i = 0; i < unitResource.size(); i++){
            int temp = unitResource.get(i);
            availableR.add(temp);
        }
        for (int i = 0; i < unitResource.size(); i++){
            freeR.add(0);
        }

        //start looping
        while (runningQ.size() + blockedQ.size() != 0) {

            //unblock if possible
            for (int i = 0; i < blockedQ.size(); i++){
                Task curTask = blockedQ.get(i);
                Activity curActivity = curTask.activities.get(0);
                if (curActivity.actNum <= availableR.get(curActivity.resourceType - 1)){
                    //it is now possible to satisfy its request, so we update available resources, the resources it holds, set status, finish this cycle's activity
                    availableR.set(curActivity.resourceType - 1, availableR.get(curActivity.resourceType - 1) - curActivity.actNum);
                    curTask.currentR[curActivity.resourceType - 1] += curActivity.actNum;
                    curTask.status = 1;
                    curTask.activities.remove(curActivity);
                    blockedQ.remove(curTask);
                    unblockedQ.add(curTask);
                    //need to decrement i because we remove something from the queue we are looping on
                    i--;
                }else{
                    //cannot unblock the current task, so it has to wait
                    curTask.timeWait++;
                    curTask.status = 3;
                }
            }

            //run this cycle
            for (int i = 0; i < runningQ.size(); i++) {
                Task curTask = runningQ.get(i);
                if (curTask.compute == 0) {
                    //meaning that is not computing
                    Activity curActivity = curTask.activities.get(0);
                    switch (curActivity.type) {
                        case "initiate":
                            //this case is not a problem for FIFO
                            curTask.activities.remove(0);
                            break;

                        case "request":
                            // abort the task if the request number exceeds total number of units
                            if (curActivity.actNum > unitResource.get(curActivity.resourceType - 1)){
                                curTask.status = 2;
                                runningQ.remove(curTask);
                                finishedQ.add(curTask);
                                i--;
                                //if abort, free all its resources
                                for (int j = 0; j < availableR.size(); j++){
                                    availableR.set(j, availableR.get(j) + curTask.currentR[j]);
                                    curTask.currentR[j] = 0;
                                }
                            }
                            else {
                                //block the task if cannot satisfy it
                                if (curActivity.actNum > availableR.get(curActivity.resourceType - 1)) {
                                    curTask.status = 3;
                                    curTask.timeWait++;
                                    blockedQ.add(curTask);
                                    runningQ.remove(curTask);
                                    i--;
                                }

                                //allocate the resource
                                else {
                                    curTask.currentR[curActivity.resourceType - 1] += curActivity.actNum;
                                    availableR.set(curActivity.resourceType - 1, availableR.get(curActivity.resourceType - 1) - curActivity.actNum);
                                    curTask.activities.remove(0);
                                }
                            }
                            break;

                        case "release":
                            //store resources freed in this cycle, for later ACTUAL release
                            freeR.set(curActivity.resourceType - 1, freeR.get(curActivity.resourceType - 1) + curActivity.actNum);
                            curTask.currentR[curActivity.resourceType - 1] -= curActivity.actNum;
                            curTask.activities.remove(0);
                            break;

                        case  "compute":
                            //set its compute data field
                            curTask.compute = curActivity.resourceType - 1;
                            curTask.activities.remove(0);
                            break;

                        case "terminate":
                            curTask.status = 4;
                            //free its resources
                            for (int j = 0; j < availableR.size(); j++){
                                availableR.set(j, availableR.get(j) + curTask.currentR[j]);
                                curTask.currentR[j] = 0;
                            }
                            runningQ.remove(curTask);
                            finishedQ.add(curTask);
                            i--;
                            curTask.timeTotal = cycle;
                            break;

                        default:
                            System.out.print("No such command!\n");
                            break;
                    }
                }
                else {
                    curTask.compute--;
                    //System.out.println(curTask.id + "compute" + curTask.compute);
                }

            }

            //unblock task from this cycle
            for (int i = 0; i < unblockedQ.size(); i++){
                runningQ.add(unblockedQ.get(i));
            }
            unblockedQ.clear();

            //check deadlock
            if (runningQ.isEmpty() && !blockedQ.isEmpty()) {
                //call the function to check deadlock
                boolean deadlock = checkDeadlock(blockedQ, availableR);
                while (deadlock) {
                    System.out.println("Deadlock detected, abort a task!\n");
                    int low = 1000000000;
                    int index = 0;
                    //find the task to abort in the required order
                    for (int i = 0;i < blockedQ.size(); i++){
                        if (blockedQ.get(i).id < low){
                            low = blockedQ.get(i).id;
                             index = i;
                        }
                    }
                    Task abortedTask = blockedQ.get(index);
                    abortedTask.status = 2;
                    blockedQ.remove(abortedTask);
                    finishedQ.add(abortedTask); // need to terminate the aborted task
                    //free its resources, then check deadlock again
                    for (int j = 0; j < availableR.size(); j++){
                        availableR.set(j, availableR.get(j) + abortedTask.currentR[j]);
                        abortedTask.currentR[j] = 0;
                    }
                    deadlock = checkDeadlock(blockedQ, availableR);
                }
            }

            //put the released resource back
            for (int i = 0; i < availableR.size(); i++){
                availableR.set(i, availableR.get(i) + freeR.get(i));
                freeR.set(i, 0);
            }

            cycle++;

        }

        //print the output
        System.out.println("              FIFO");
        int totalTime = 0;
        int waitTime = 0;

        // sort the finished tasks based on their id, then store in an array-list
        ArrayList<Task> out = new ArrayList<>();

        for (int i = 1; i < numTask + 1; i++){
            for (int j = 0; j < finishedQ.size(); j++){
                if(finishedQ.get(j).id == i){
                    out.add(finishedQ.get(j));
                }
            }
        }

        //format output
        for (int i = 0; i < out.size(); i++) {
            Task curTask = out.get(i);
            if (curTask.status == 4){
                float percentWait =(float) curTask.timeWait /(float) curTask.timeTotal;
                int percentage = Math.round(percentWait * 100);
                System.out.println("Task " + curTask.id + "\t" + curTask.timeTotal + "\t" + curTask.timeWait + "\t" + percentage + "%");
                totalTime += curTask.timeTotal;
                waitTime += curTask.timeWait;
            }
            else if (curTask.status == 2) {
                System.out.println("Task " + curTask.id + "\t" + "aborted" + "\t" );
            }
            else {
                System.out.println("something wrong");
            }
        }

        float overallPercent = (float) waitTime / (float) totalTime;
        int percentage = Math.round(overallPercent * 100);
        System.out.println("total " + "\t" + totalTime + "\t" + waitTime + "\t" + percentage +"%\n");

    }

    @SuppressWarnings("unchecked")
    public static void Banker() {
        int cycle = 0;

        //Queues to keep track of the current cycle situation
        ArrayList<Task> runningQ;
        ArrayList<Task> blockedQ = new ArrayList<>();
        ArrayList<Task> unblockedQ = new ArrayList<>();
        ArrayList<Task> finishedQ = new ArrayList<>();

        //To keep track of the resources
        ArrayList<Integer> availableR = new ArrayList<>();
        ArrayList<Integer> freeR = new ArrayList<>();

        //clone the queue of all task as running queue first
        runningQ = (ArrayList<Task>) taskQ.clone();

        //Initialize available and freed resource
        runningQ = (ArrayList<Task>) taskQ.clone();
        for (int i = 0; i < unitResource.size(); i++){
            int temp = unitResource.get(i);
            availableR.add(temp);
        }
        for (int i = 0; i < unitResource.size(); i++){
            freeR.add(0);
        }

        //start looping
        while (runningQ.size() + blockedQ.size() != 0) {

            //unblock if possible
            for (int i = 0; i < blockedQ.size(); i++) {
                Task curTask = blockedQ.get(i);
                Activity curActivity = curTask.activities.get(0);

                //then we have to check if unblocking this will not lead to unsafe state
                //since we want to do the checking without changing the current state, we need to make clone copies
                ArrayList<Task> testQ = new ArrayList<>(); //cannot clone here
                for (int j = 0; j < taskQ.size(); j++){
                    Task t = taskQ.get(j);
                    Task t_clone = new Task(t);
                    testQ.add(t_clone);
                }

                //then want to find the current Task inside the test task queue, so that it is also "cloned"
                Task testTask = null;
                int index = curTask.id;
                for (int j = 0; j < testQ.size(); j++) {
                    Task t = testQ.get(j);
                    if (t.id == index) {
                        testTask = t;
                    }
                }
                ArrayList<Integer> testAvailableR = (ArrayList<Integer>) availableR.clone();

                //then we can do the safe state check with the cloned data
                boolean safestate = checkSafeState(testTask, testQ, testAvailableR);
                if (safestate) {
                    //means that it is safe to grant it what it wants to unblock it
                    availableR.set(curActivity.resourceType - 1, availableR.get(curActivity.resourceType - 1) - curActivity.actNum);
                    curTask.currentR[curActivity.resourceType - 1] += curActivity.actNum;
                    curTask.status = 1;
                    curTask.activities.remove(0);
                    blockedQ.remove(curTask);
                    unblockedQ.add(curTask);
                    i--;
                }
                else {
                    //means that is unsafe
                    curTask.status = 3;
                    curTask.timeWait++;
                }
            }

            //run this cycle
            for (int i = 0; i < runningQ.size(); i++) {
                Task curTask = runningQ.get(i);
                if (curTask.compute == 0) {
                    Activity curActivity = curTask.activities.get(0);
                    switch (curActivity.type) {
                        case "initiate":
                            //check if it is possible to satisfy the initial claim
                            if (curActivity.actNum <= unitResource.get(curActivity.resourceType - 1)){
                                curTask.status = 1;
                                curTask.initialR[curActivity.resourceType - 1] = curActivity.actNum;
                                curTask.activities.remove(0);
                            }
                            else {
                                //abort if the inital claim cannot be satisfied
                                curTask.status = 2;
                                runningQ.remove(curTask);
                                finishedQ.add(curTask);
                            }
                            break;

                        case "request":
                            //abort the task if it ask for more than its initial claim
                            int maxRequest = curTask.initialR[curActivity.resourceType - 1] - curTask.currentR[curActivity.resourceType - 1];
                            if (maxRequest < curActivity.actNum) {
                                //it is illegal to do so, abort it
                                curTask.status = 2;
                                runningQ.remove(curTask);
                                finishedQ.add(curTask);
                                i--;
                                //free its resources
                                for (int j = 0; j < availableR.size(); j++) {
                                    availableR.set(j, availableR.get(j) + curTask.currentR[j]);
                                    curTask.currentR[j] = 0;
                                }
                            }
                            else {
                                //its request is legal, then we want to do the similar safestate check as above
                                //since we want to do the checking without changing the current state, we need to make clone copies
                                ArrayList<Task> testQ = new ArrayList<>(); //cannot clone here
                                for (int j = 0; j < taskQ.size(); j++){
                                    Task t = taskQ.get(j);
                                    Task t_clone = new Task(t);
                                    testQ.add(t_clone);
                                }

                                //then want to find the current Task inside the test task queue, so that it is also "cloned"
                                Task testTask = null;
                                int index = curTask.id;
                                for (int j = 0; j < testQ.size(); j++) {
                                    Task t = testQ.get(j);
                                    if (t.id == index) {
                                        testTask = t;
                                    }
                                }
                                ArrayList<Integer> testAvailableR = (ArrayList<Integer>) availableR.clone();

                                //then we can do the safe state check with the cloned data
                                boolean safestate = checkSafeState(testTask, testQ, testAvailableR);
                                if (safestate) {
                                    //means that it is safe to grand the resource
                                    curTask.currentR[curActivity.resourceType - 1] += curActivity.actNum;
                                    availableR.set(curActivity.resourceType - 1, availableR.get(curActivity.resourceType - 1) - curActivity.actNum);
                                    curTask.activities.remove(0);
                                }
                                else {
                                    //means that it is unsafe to grand the resource, so we block the task for now
                                    curTask.status = 3;
                                    curTask.timeWait++;
                                    runningQ.remove(curTask);
                                    blockedQ.add(curTask);
                                    i--;
                                }
                            }
                            break;

                        case "release":
                            freeR.set(curActivity.resourceType - 1, freeR.get(curActivity.resourceType - 1) + curActivity.actNum);
                            curTask.currentR[curActivity.resourceType - 1] -= curActivity.actNum;
                            curTask.activities.remove(0);
                            break;

                        case  "compute":
                            curTask.compute = curActivity.resourceType - 1;
                            curTask.activities.remove(0);
                            break;

                        case "terminate":
                            curTask.status = 4;
                            //free its resources
                            for (int j = 0; j < availableR.size(); j++){
                                availableR.set(j, availableR.get(j) + curTask.currentR[j]);
                                curTask.currentR[j] = 0;
                            }
                            runningQ.remove(curTask);
                            finishedQ.add(curTask);
                            i--;
                            curTask.activities.remove(0);
                            curTask.timeTotal = cycle;
                            break;

                        default:
                            System.out.print("No such command!\n");
                            break;
                    }
                }
                else {
                    curTask.compute--;
                    //System.out.println(curTask.id + "compute" + curTask.compute);
                }
            }

            //unblock task from this cycle
            for (int i = 0; i < unblockedQ.size(); i++){
                runningQ.add(unblockedQ.get(i));
            }
            unblockedQ.clear();

            //put the released resource back
            for (int i = 0; i < availableR.size(); i++){
                availableR.set(i, availableR.get(i) + freeR.get(i));
                freeR.set(i, 0);
            }

            cycle++;
        }

        //print the output
        System.out.println("              BANKER'S");
        int totalTime = 0;
        int waitTime = 0;

        // sort the finished tasks based on their id, then store in an array-list
        ArrayList<Task> out = new ArrayList<>();

        for (int i = 1; i < numTask + 1; i++){
            for (int j = 0; j < finishedQ.size(); j++){
                if(finishedQ.get(j).id == i){
                    out.add(finishedQ.get(j));
                }
            }
        }

        for (int i = 0; i < out.size(); i++) {
            Task curTask = out.get(i);
            if (curTask.status == 4){
                float percentWait =(float) curTask.timeWait /(float) curTask.timeTotal;
                int percentage = Math.round(percentWait * 100);
                System.out.println("Task " + curTask.id + "\t" + curTask.timeTotal + "\t" + curTask.timeWait + "\t" + percentage + "%");
                totalTime += curTask.timeTotal;
                waitTime += curTask.timeWait;
            }
            else if (curTask.status == 2) {
                System.out.println("Task " + curTask.id + "\t" + "aborted" + "\t" );
            }
            else {
                System.out.println("something wrong");
            }
        }

        float overallPercent = (float) waitTime / (float) totalTime;
        int percentage = Math.round(overallPercent * 100);
        System.out.println("total " + "\t" + totalTime + "\t" + waitTime + "\t" + percentage +"%\n");
    }

    // check if we can unblock from the blocked queue, if we cannot, means we are in a deadlock and everyprocess is bloacked in the blocked list
    public static boolean checkDeadlock(ArrayList<Task> blockedQ, ArrayList<Integer> availableR){
        Task curTask = blockedQ.get(0);
        Activity curActivity = curTask.activities.get(0);
        if (curActivity.actNum > availableR.get(curActivity.resourceType - 1)) {
            return true;
        }
        return false;
    }

    //check if we are in a safe state
    public static boolean checkSafeState(Task t, ArrayList<Task> testQ, ArrayList<Integer> testAvailableR){
        //it is of course safe if there is nothing
        if (t == null) {
            return true;
        }
        if (testQ.size() == 0) {
            return true;
        }
        Activity curActivity = t.activities.get(0);
        if (curActivity.actNum > testAvailableR.get(curActivity.resourceType - 1)) {
            //we cannot satisfy this task
            return false;
        }

        //else meaning that it can be satisfied, but need to see if it is safe to do so
        //assume we give it what it wants
        testAvailableR.set(curActivity.resourceType - 1, testAvailableR.get(curActivity.resourceType - 1) - curActivity.actNum);
        t.currentR[curActivity.resourceType - 1] += curActivity.actNum;
        //then check if we can terminate everything eventually still
        while (!testQ.isEmpty()) {
            int signal = 0;
            for (Task tt: testQ) {
                if (checkResource(tt, testAvailableR)) {
                    //update information after we terminate tt
                    for (int i = 0; i < testAvailableR.size(); i++){
                        //tt release resources
                        testAvailableR.set(i, testAvailableR.get(i) + tt.currentR[i]);
                        tt.currentR[i] = 0;
                        testQ.remove(tt);
                    }
                    signal++;
                }
                //means that we can satisfy tt's claim, so we check for the next task in the queue
                if (signal != 0) {break;}
            }
            //means nothing happened in the for loop, it is unsafe to satisfy SOME task at least
            if (signal == 0) {
                return false;
            }
        }
        return true;

    }

    // check if can satisfy one specific task's initial claim, the maximum of its request
    public static boolean checkResource(Task t, ArrayList<Integer> testAvailableR){
        for (int i = 0; i < testAvailableR.size(); i++){
            int temp = t.initialR[i] - t.currentR[i];
            if (temp > testAvailableR.get(i)){
                return false;
            }
        }
        return true;
    }

}





























