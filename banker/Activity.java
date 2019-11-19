public class Activity {
    String type;     // four types: initiate, request, release, terminate
    int taskID;     // task id of the current activity
    int resourceType; // resource id of current activity
    int actNum;      // operation number of this activity

    //constructor, to set initial values
    public Activity(String type, int task_id, int resourceType, int actNum) {
        this.type = type;
        this.taskID = task_id;
        this.resourceType = resourceType;
        this.actNum = actNum;
    }

}
