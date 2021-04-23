package a3;

import java.util.*;

public class q1 {
    static final int MAX_STATES = 5;
    static final int INVALID_TRANS = -1;

    static Transition top;
    static Transition bottom;
    static Transition decimal;
    static Transition self;
    static Transition accept;

    static DfaState STATE_START;
    static DfaState STATE_2;
    static DfaState STATE_3;
    static DfaState STATE_DECIMAL;
    static DfaState STATE_ACCEPT;

    static char [] characterList = new char[]{'0','1','2','3','4','5','6','7','8','9','.','a'};
    static int strLength = 500000;

    static long startTime;
    static long finishTime;


    public static void main(String[] args) throws InterruptedException {
        // build input string
        int numThread = 1;
        int seed = 1;

        if (args.length>0) {
            numThread = Integer.parseInt(args[0]);
            seed = Integer.parseInt(args[1]);
        }

        StringBuilder strBuilder = new StringBuilder();
        Random random = new Random(seed);

        while(strBuilder.length() < strLength){
            int charIndex = random.nextInt(characterList.length);
            strBuilder.append(characterList[charIndex]);
        }
        System.out.println(strBuilder.toString());

        // partition input string into substrings for every thread
        char[][] textThread = partitionStr(numThread, strBuilder);

        // initialize transitions and states
        initialization();

        // start computing
        startTime = System.currentTimeMillis();
        WorkThread[] threads = new WorkThread[numThread];
        for(int i = 0; i<numThread;i++){
            threads[i] = new WorkThread(STATE_START,STATE_2,STATE_3,STATE_DECIMAL,STATE_ACCEPT, textThread[i]);
        }
        for(WorkThread t: threads){
            t.start();
        }
        for(WorkThread t: threads){
            t.join();
        }
        // start merging
        String processedStr = processString(numThread, threads);
        System.out.println(processedStr);

        System.out.println("\nString length: "+ strLength + ", time: "+ (finishTime-startTime) + " ms");

    }

    static class Transition{
        char start; /* start state */
        char end; /* end state */
        DfaState nextState;

        public Transition(char start, char end, DfaState nextState){
            this.start = start;
            this.end = end;
            this.nextState = nextState;
        }
    }

    static class DfaState{
        Transition[] transitions; // maximum 2 possible transitions
        int pos;
        boolean terminate; // terminate flag

        public DfaState(int pos, boolean terminate, Transition[] transitions){
            this.pos = pos;
            this.transitions = new Transition[transitions.length];
            for(int i = 0; i < transitions.length; i++){
                this.transitions[i] = transitions[i];
            }
            this.terminate = terminate;
        }

        /**
         * check whether a character is valid for the transition
         * if yes, return the next transition index
         * else, return -1
         * @param input
         * @return
         */
        int isValidTransition(char input){
            for(int i = 0; i<this.transitions.length;i++){
                if(input>=this.transitions[i].start && input<=this.transitions[i].end){
                    return i;
                }
            }
            return INVALID_TRANS;
        }

    }

    static class WorkThread extends Thread{
        char[] input; /* input string of each thread */
        LinkedList<char[]>[] output = new LinkedList[MAX_STATES]; /* store outputs of every state*/
        DfaState[] states = new DfaState[MAX_STATES]; /* keep 5 states in an array*/

        public WorkThread(DfaState STATE_START, DfaState STATE_2, DfaState STATE_3, DfaState STATE_DECIMAL, DfaState STATE_ACCEPT, char[] input){
            this.states[0] = STATE_START;
            this.states[1] = STATE_2;
            this.states[2] = STATE_3;
            this.states[3] = STATE_DECIMAL;
            this.states[4] = STATE_ACCEPT;

            this.input = new char[input.length];
            for(int i = 0; i < input.length; i++){
                this.input[i] = input[i];
            }
        }

        @Override
        public void run() {
            // loop through all states for optimistic threads
            for(int i=0; i<MAX_STATES; i++){
                output[i] = new LinkedList<>();
            }
            // map input string for every possible state
            for(DfaState state: states){
                runStateMachine(state,input);
            }
        }

        /**
         * map over input string for specified state
         * @param currState
         * @param input
         */
        void runStateMachine(DfaState currState, char[] input){
            StringBuilder buffer = new StringBuilder();
            int startPos = 0;
            int endPos;

            // use tmp DFA state to store curr_state
            DfaState tmp = currState;


            // loop through input if current state does not terminate and is a valid transition
            for(int i = 0; i < input.length; i++){
                // check whether input string can go to next transition at current state
                if(tmp.isValidTransition(input[i])!= INVALID_TRANS) {
                    buffer.append(input[i]);
                    // if yes, set curr_state to transition.nextState
                    int nextTransitionIndex = tmp.isValidTransition(input[i]);
                    tmp = tmp.transitions[nextTransitionIndex].nextState;

                }
                else{
                    // if no, handle possible situations
                    endPos = i;

                    // input string at middle state and not terminate
                    // replace string with '_'
                    if(((endPos!=input.length-1)||(endPos==input.length-1 && input.length==1)) && !tmp.terminate){
                        char [] underscore = new char[endPos-startPos+1];
                        Arrays.fill(underscore,'_');
                        output[currState.pos].add(underscore);

                    }

                    // input string at middle state and terminate
                    // found a longest string
                    else if(endPos != input.length-1 && tmp.terminate) {
                        output[currState.pos].add(Arrays.copyOfRange(input, startPos, endPos));
                        output[currState.pos].add(new char[]{'_'});
                    }

                    else if(endPos == input.length-1){
                        // if reach termination
                        // use underscore
                        if(tmp.terminate){
                            output[currState.pos].add(Arrays.copyOfRange(input,startPos,endPos));
                            output[currState.pos].add(new char[]{'_'});
                        }
                        // string stop end
                        // add position of stopte to linked list tail
                        else {
                            // add copy of substring of input string
                            output[currState.pos].add(Arrays.copyOfRange(input, startPos, endPos + 1));
                            output[currState.pos].add(new char[]{(char) (tmp.pos + '0')});
                        }
                    }

                    buffer = new StringBuilder();
                    // end of input string
                    if(i==input.length-1) break;

                    tmp = states[0];
                    startPos = i+1;
                }
            }
            if(buffer.length()>0) {
                output[currState.pos].add(buffer.toString().toCharArray());
                output[currState.pos].add(new char[]{(char) (tmp.pos + '0')});
            }
        }
    }




    private static void initialization(){
        // initialize all transitions
        top = new Transition('0','0',null);
        bottom = new Transition('1','9',null);
        decimal = new Transition('.','.',null);
        self = new Transition('0','9',null);
        accept = new Transition('0','9',null);

        // initialize all 5 states
        // map states to transitions
        STATE_START = new DfaState(0,false,new Transition[]{top,bottom});
        STATE_2 = new DfaState(1,false,new Transition[]{decimal});
        STATE_3 = new DfaState(2,false,new Transition[]{decimal,self});
        STATE_DECIMAL = new DfaState(3,false,new Transition[]{accept});
        STATE_ACCEPT = new DfaState(4,true,new Transition[]{accept});

        // set next states for all transitions
        top.nextState = STATE_2;
        bottom.nextState = STATE_3;
        decimal.nextState = STATE_DECIMAL;
        self.nextState = STATE_3;
        accept.nextState = STATE_ACCEPT;
    }

    private static char[][] partitionStr(int numThread, StringBuilder strBuilder){
        // store input string as char array
        char[] text = strBuilder.toString().toCharArray();

        // partition input string to n substrings of partitionSize length
        // store substrings into 2d array textThread
        int partitionSize = strBuilder.length() / numThread;
        char [][] textThread = new char[numThread][];
        for(int i = 0; i < numThread; i++){
            /* set working quantity for each thread */
            // not last thread
            if(i != numThread-1){
                textThread[i] = new char[partitionSize];
            }
            // last thread
            else {
                int qtyLast = strBuilder.length() - (numThread-1) * partitionSize;
                textThread[i] = new char[qtyLast];
            }

            /* assign working text to each thread */
            for(int j = 0; j < partitionSize; j++){
                int workingTextIndex = i * partitionSize + j;
                if(workingTextIndex >= strBuilder.length()) break;
                textThread[i][j] = text[workingTextIndex];
            }
        }
        return textThread;
    }

    private static String processString(int numThread, WorkThread[] threads){
        // stores processed string
        LinkedList<char[]> curr;
        LinkedList<char[]> result = new LinkedList<>();
        int posOfNxt = 0;
        for(int i = 0; i<numThread; i++){
            LinkedList<char[]>[] tmp = threads[i].output;

            // normal thread
            if(i==0){
                curr = tmp[0];
            }
            // optimistic thread
            else{
                curr = tmp[posOfNxt];
            }

            // if last char of current thread contains '_'
            // next thread will start at start state
            if((new String(curr.getLast())).contains("_")){
                posOfNxt = 0;
            }
            // next thread will start at the same state with the end state of previous thread
            else{
                posOfNxt = Integer.parseInt(new String(curr.getLast()));
            }


            if(i<numThread-1) {
                curr.pollLast();

                // if last char of current thread does not terminate and first char of next thread is '_'
                // set last char of curr thread to underscore
                if ((posOfNxt!=0 && posOfNxt!=4) && new String(threads[i + 1].output[posOfNxt].getFirst()).contains("_")) {
                    char[] underScore = new char[curr.pollLast().length];
                    Arrays.fill(underScore, '_');
                    curr.addLast(underScore);
                }

            }
            else{
                curr.pollLast();

                // middle state
                if(posOfNxt!=0 && posOfNxt!=4){
                    char[] underScore = new char[curr.pollLast().length];
                    Arrays.fill(underScore, '_');
                    curr.addLast(underScore);
                }

                // check whether string is valid
                String checker = new String(threads[i].output[posOfNxt].getFirst());
                if(!checker.equals(new String(curr.getFirst()))){
                    int counter = 0;
                    for(int j = result.size(); j>0; j--){
                        if((new String(result.get(j-1))).contains("_")){
                            counter = j;
                            break;
                        }
                    }
                    for(int j = counter; j < result.size(); j++){
                        char [] c = new char[result.get(j).length];
                        Arrays.fill(c, '_');
                        result.set(j, c);
                    }
                }
            }
            result.addAll(curr);
        }
        finishTime = System.currentTimeMillis();

        // load final processed string
        String processedStr = "";
        for(char[] iter: result){
            processedStr += new String(iter);
        }
        return processedStr;
    }

}
