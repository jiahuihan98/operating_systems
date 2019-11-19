public class Process implements Comparable<Process> {
    //has the four properties given
    int A;
    int B;
    int C;
    int M;

    //Built while FCFS
    int CPUburst = 0;
    int CPUtime = 0; //CPU time inorder to compare with C
    int IOburst = 0;
    int IOtime = 0;
    int waitTime = 0;
    int finishedTime = 0;



    //have an integer represent the state process is at, 0 is not started, 1 is running, 2 is ready, 3 is blocked, 4 is finished
    int state = 0;

    //constructor
    public Process(int A, int B, int C, int M) {
        this.A = A;
        this.B = B;
        this.C = C;
        this.M = M;

    }

    @Override
    public int compareTo(Process p) {
        if (this.A > p.A) {
            return 1;
        }
        if (this.A == p.A) {
            return 0;
        }
        else {
            return -1;
        }
    }

    @Override
    public String toString () {
        return ("(" + this.A + ", " + this.B + ", " + this.C + ", " + this.M + ")");
    }
}
