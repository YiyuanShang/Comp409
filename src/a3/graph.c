#include <omp.h>
#include <stdio.h>
#include <stdlib.h>

static int n = 10;
static int t = 8;

/* Node*/
typedef struct node
{
    int vertex;                /*Index to adjacency list array*/
    int color;
}node_t, *node_p;

/* Adjacency list */
typedef struct adjlist
{
    int num_members;    /*number of members in the list (for future use)*/
    node_t *self;
    int members[50][2];
}adjlist_t, *adjlist_p;

/* Graph structure. A graph is an array of adjacency lists.
   Size of array will be number of vertices in graph*/
typedef struct graph
{
    int num_vertices;         /*Number of vertices*/
    adjlist_p adjListArr;     /*Adjacency lists' array*/

}graph_t, *graph_p;



/* Exit function to handle fatal errors*/
__inline void err_exit(char* msg)
{
    printf("[Fatal Error]: %s \nExiting...\n", msg);
    exit(1);
}


/* Function to create an adjacency list node*/
node_p createNode(int v, int color)
{
    node_p newNode = (node_p)malloc(sizeof(node_t));
    if(!newNode)
        printf("Unable to allocate memory for new node");

    newNode->vertex = v;
    newNode->color = color;

    return newNode;
}

/* Function to create a graph with n vertices*/
graph_p createGraph(int n)
{
    int i;
    graph_p graph = (graph_p)malloc(sizeof(graph_t));
    if(!graph)
        printf("Unable to allocate memory for graph");
    graph->num_vertices = n;

    /* Create an array of adjacency lists*/
    graph->adjListArr = (adjlist_p)malloc(n * sizeof(adjlist_t));
    if(!graph->adjListArr)
        printf("Unable to allocate memory for adjacency list array");


    for(i = 0; i < n; i++)
    {
        graph->adjListArr[i].self = NULL;
        graph->adjListArr[i].num_members = 0;

    }



    return graph;
}

/*Destroys the graph*/
void destroyGraph(graph_p graph)
{
    if(graph)
    {
        if(graph->adjListArr)
        {
            // int v;
            // /*Free up the nodes*/
            // for (v = 0; v < graph->num_vertices; v++)
            // {
            //     node_p adjListPtr = graph->adjListArr[v].head;
            //     while (adjListPtr)
            //     {
            //         node_p tmp = adjListPtr;
            //         adjListPtr = adjListPtr->next;
            //         free(tmp);
            //     }
            // }
            /*Free the adjacency list array*/
            free(graph->adjListArr);
        }

        /*Free the graph*/
        free(graph);
    }
}

/* Adds an edge to a graph*/
void addEdge(graph_t *graph, int src, int dest)
{
    /* Add an edge from src to dst in the adjacency list*/
    node_p srcNode = graph->adjListArr[src].self;
    node_p destNode = graph->adjListArr[dest].self;

    // destNode->next = graph->adjListArr[src].head;
    // graph->adjListArr[src].head  = destNode;
    int neighbourNum = graph->adjListArr[src].num_members;
    graph->adjListArr[src].members[neighbourNum][0] = destNode->vertex;
    graph->adjListArr[src].members[neighbourNum][1] = destNode->color;
    graph->adjListArr[src].num_members++;

    /* Add an edge from dest to src also*/
    // srcNode->next = graph->adjListArr[dest].head;
    // graph->adjListArr[dest].head  = srcNode;

    neighbourNum = graph->adjListArr[dest].num_members;
    graph->adjListArr[dest].members[neighbourNum][0] = srcNode->vertex;
    graph->adjListArr[dest].members[neighbourNum][1] = (srcNode->color);
    graph->adjListArr[dest].num_members++;

    printf("add edge success between %d and %d\n\n", src, dest);
}

/* Function to print the adjacency list of graph*/
void displayGraph(graph_p graph)
{
    int i;
    for (i = 0; i < graph->num_vertices; i++)
    {
        printf("\n%d(%d): ", graph->adjListArr[i].self->vertex, graph->adjListArr[i].self->color);
        for(int j=0; j<graph->adjListArr[i].num_members; j++){
            int vertex = graph->adjListArr[i].members[j][0];
            int color = graph->adjListArr[i].members[j][1];
            printf(" %d(%d) ->", vertex, color);
        }
        printf(" NULL\n");

        // node_p adjListPtr = graph->adjListArr[i].head;
        // while (adjListPtr)
        // {
        //     printf(" %d(%d) ->", adjListPtr->vertex, adjListPtr->color);
        //     adjListPtr = adjListPtr->next;
        // }
        // printf("NULL\n");
    }
}

int isEdgeExist(graph_p graph, int src, int dest){
    // int length = (int)(sizeof(graph->edges) / sizeof(graph->edges[0]));

    // for (int i=0; i<length; i++){
    //     int v1 = graph->edges[i][0];
    //     int v2 = graph->edges[i][1];

    //     if ((src == v1 && dest == v2) || (src==v2 && dest==v1)){
    //         printf("edge between %d and %d EXISTS\n", src, dest);
    //         return 1;
    //     }
    // }
    // printf("edge between %d and %d NOT exists\n", src, dest);
    // return 0;

    // node_p adjListPtr = graph->adjListArr[src].head;
    // while(adjListPtr){
    //     if(adjListPtr->vertex == dest){
    //         printf("edge between %d and %d EXISTS\n", src, dest);
    //         return 1;
    //     }

    //     adjListPtr = adjListPtr->next;
    // }
    // printf("edge between %d and %d NOT exists\n", src, dest);
    // return 0;

    for(int i=0; i<graph->adjListArr[src].num_members; i++){
        if (graph->adjListArr[src].members[i][0] == dest){
            printf("edge between %d and %d EXISTS\n", src, dest);
            return 1;
        }
    }
    printf("edge between %d and %d NOT exists\n", src, dest);
    return 0;

}

graph_p* partitionGraph(graph_p graph, int numPartitions, int minNodesPerPartition){
    // calculate actual number of partitions
    int numParts = numPartitions;
    if (graph->num_vertices <= numParts * minNodesPerPartition){
        numParts = graph->num_vertices / minNodesPerPartition;
    }
    if (graph->num_vertices % minNodesPerPartition != 0){
        numParts++;
    }
    int nodesPerPart = graph->num_vertices / numParts;

    // partition the graph into numParts subgraphs
    // each subgraph gets nodesPerPart nodes
    graph_p* subgraphs = (graph_p*)malloc(numParts * sizeof(graph_p));
    for (int i=0; i<numParts-1; i++){
        subgraphs[i] = (graph_p)malloc(sizeof(graph_t));

        if(!subgraphs[i])
                printf("Unable to allocate memory for graph");
            subgraphs[i]->num_vertices = nodesPerPart;

            /* Create an array of adjacency lists*/
            subgraphs[i]->adjListArr = (adjlist_p)malloc(n * sizeof(adjlist_t));
            if(!subgraphs[i]->adjListArr)
                printf("Unable to allocate memory for adjacency list array");
            for(j = 0; j < nodesPerPart; j++)
            {
                subgraphs[i]->adjListArr[j].self = graph->adjListArr[j*nodesPerPart].self;
                subgraphs[i]->adjListArr[j].num_members = graph=->adjListArr[j*nodesPerPart].num_members;
                subgraphs[i]->adjListArr[j].members = graph->adjListArr[j*nodesPerPart].members;
            }
    }





}

void assign(graph_p graph){
    printf("assign() thread %d, nthreads %d\n",
               omp_get_thread_num(),
               omp_get_num_threads());
    graph_p conflictings[]

}

adjlist_p detectConflicts(){
    printf("detectConflicts() thread %d, nthreads %d\n",
           omp_get_thread_num(),
           omp_get_num_threads());

    return newComflicts;
}

extern graph_p newComflicts;
extern graph_p conflicting;
int main(int argc, char *argv[])
{
    int i;
    if (argc>1) {
        n = atoi(argv[1]);
        printf("Using %d iterations\n",n);
        if (argc>2) {
            t = atoi(argv[2]);
            printf("Using %d threads\n",t);
        }
    }
    graph_p result_graph = createGraph(n);

    // create n nodes
    for (int i=0; i<n; i++){
        node_p newNode = createNode(i, 0);
        result_graph->adjListArr[i].self = newNode;
    }

    printf("\nAll Vertices:\n");
    for(int i=0; i<n; i++){
        printf("%d(%d)\t", result_graph->adjListArr[i].self->vertex, result_graph->adjListArr[i].self->color);
    }

    //upper is n*(n-1)/2, lower is 1
    int upper = n*(n-1)/2;
    int e = (rand() %(upper-1+1)) +1;
    printf("\ntotal edges:%d\n", e);

    for (int i=0; i<e; i++){

        // add edge between random pairs of different nodes
        // the node with highest vertex id is n-1
        // the node with lowest vertex id is 0
        int src = rand() % n;
        int dest = rand() % n;
        printf("testing src:%d dest:%d\n", src, dest);
        while(src == dest || isEdgeExist(result_graph, src, dest) == 1){
            dest = rand() %n;
        }
        printf("src:%d dest:%d\n", src, dest);
        addEdge(result_graph, src, dest);
        //displayGraph(result_graph);
    }

    displayGraph(result_graph);

    /* Use the openmp api to set the max number of threads dynamically for
       the next parallel region. */
    omp_set_dynamic(0); /* Disable dynamic teams. */
    omp_set_num_threads(t);

    graph_p conflicting = result_graph;

    #pragma omp parallel shared(conflicting, newComflicts)
    if(conflicting->num_vertices > 0){
        assign();
        conflicting = detectConflicts();
    }
    destroyGraph(result_graph);




    return 0;
}