import java.util.*;

public class Task {

    int id;
    int status; // 0 is un-started, 1 is running, 2 is aborted, 3 is blocked, 4 is terminated
    int timeWait;
    int timeTotal;
    int compute;

    int[] initialR; // initial claim of this task
    int[] currentR; // how many resource currently owned

    //to store its corresponding activities
    ArrayList<Activity> activities;

    //constructor, set all initial value
    public Task(int id, int resourceNum){
        this.status = 0;
        this.timeWait = 0;
        this.timeTotal = 0;
        this.compute = 0;
        this.id = id;
        this.initialR = new int[resourceNum];
        this.currentR = new int[resourceNum];
        this.activities = new ArrayList<>();
    }

    // clone a task for checking if it is at a safe state
    @SuppressWarnings("unchecked")
    public Task(Task t){
        this.status = t.status;
        this.timeWait = t.timeWait;
        this.timeTotal = t.timeTotal;
        this.compute = t.compute;
        this.id = t.id;
        this.initialR = t.initialR.clone();
        this.currentR = t.currentR.clone();
        this.activities = (ArrayList<Activity>) t.activities.clone();
    }

}
