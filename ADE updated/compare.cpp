#include<bits/stdc++.h>
using namespace std;

const int N = 1000;
int main_graph[N][N];
int original_graph[N][N];
int modified_graph[N][N];

bool is_cluster(int graph[N][N]){
    for(int i=0;i<N;i++){
        for(int j=0;j<N;j++){
            if(graph[i][j] == 1){
                for(int k=0;k<N;k++){

                    if( graph[j][k] != graph[i][k] and i!=j and j!=k and i!=k){
                        cout<<i<<" "<<j<<" "<<k<<endl;
                        return false;
                    }
                }
            }
        }
    }
    return true;
}


void checkFor3Cycle(int graph[N][N]){
    vector<int> components;
    for(int i=0;i<N;i++){
        components.clear();
        components.push_back(i);
        for(int j=0;j<N;j++){
            if(graph[i][j] == 1){
                components.push_back(j);
            }
        }
    }
    if( components.size() == 3 ){
        cout<<components[0]<<" "<<components[1]<<" "<<components[2]<<endl;
    }
}

int main(){

    ifstream main_graph_file("main_graph.txt");
    ifstream original_graph_file("original_graph.txt");
    ifstream modified_graph_file("modified_graph.txt");

    int u,v;
    while(main_graph_file >> u >> v){
        main_graph[u][v] = 1;
        main_graph[v][u] = 1;
    }


    while(original_graph_file >> u >> v){
        original_graph[u][v] = 1;
        original_graph[v][u] = 1;
    }

    while(modified_graph_file >> u >> v){
        modified_graph[u][v] = 1;
        modified_graph[v][u] = 1;
    }

    int edge_add = 0;
    int edge_del = 0;

    for(int i=0;i<N;i++){
        for(int j=0;j<N;j++){
            original_graph[i][j] ^= main_graph[i][j];
            //if( original_graph[i][j] == 1 and i<j ) cout<<i<<" "<<j<<endl;
            if(main_graph[i][j] != original_graph[i][j]){
                if(main_graph[i][j] == 1){
                    edge_del++;
                }else{
                    edge_add++;
                }
            }
        }
    }

    cout<<endl;
    cout << "in original graph:" << endl;
    cout << "Edge Add: " << edge_add/2 << endl;
    cout << "Edge Del: " << edge_del/2 << endl;
    cout << "Total Mod: " << edge_add/2 + edge_del/2 << endl;

    edge_add = 0;
    edge_del = 0;

    for(int i=0;i<N;i++){
        for(int j=0;j<N;j++){
            modified_graph[i][j] ^= main_graph[i][j];
            //if( modified_graph[i][j] == 1 and i<j ) cout<<i<<" "<<j<<endl;
            if(main_graph[i][j] != modified_graph[i][j]){
                if(main_graph[i][j] == 1){
                    edge_del++;
                }else{
                    edge_add++;
                }
            }
        }
    }

    cout << "in modified graph:" << endl;
    cout << "Edge Add: " << edge_add/2 << endl;
    cout << "Edge Del: " << edge_del/2 << endl;
    cout << "Total Mod: " << edge_add/2 + edge_del/2 << endl;


    // cout<<is_cluster(main_graph)<<endl;
    // cout<<is_cluster(original_graph)<<endl;
    // cout<<is_cluster(modified_graph)<<endl;
    if( !is_cluster(original_graph) ){
        cout<<"Original Not a cluster"<<endl;
    }

    if( !is_cluster(modified_graph) ){
        cout<<"Modified Not a cluster"<<endl;
    }

    checkFor3Cycle(modified_graph);


}
