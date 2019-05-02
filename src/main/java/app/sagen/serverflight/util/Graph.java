package app.sagen.serverflight.util;

import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class Graph {
    private String world;
    private Map<Vertex, List<Vertex>> adjVertices = new HashMap<>();

    public void addVertex(Vertex vertex) {
        adjVertices.putIfAbsent(vertex, new ArrayList<>());
    }

    public void removeVertex(Vertex vertex) {
        adjVertices.values().forEach(e -> e.remove(vertex));
        adjVertices.remove(vertex);
    }

    public void addEdge(Vertex vertex1, Vertex vertex2) {
        adjVertices.get(vertex1).add(vertex2);
        adjVertices.get(vertex2).add(vertex1);
    }

    public void removeEdge(Vertex vertex1, Vertex vertex2) {
        List<Vertex> eV1 = adjVertices.get(vertex1);
        List<Vertex> eV2 = adjVertices.get(vertex2);
        if (eV1 != null)
            eV1.remove(vertex2);
        if (eV2 != null)
            eV2.remove(vertex1);
    }

    public List<Vertex> getAdjVertices(Vertex vertex) {
        return adjVertices.get(vertex);
    }

    public Optional<Vertex> getByName(String name) {
        return adjVertices.keySet().stream()
                .filter(v -> v.name.equalsIgnoreCase(name))
                .findFirst();
    }

    public Optional<Vertex> getByLocation(float x, float y, float z) {
        return adjVertices.keySet().stream()
                .filter(v -> v.x == x && v.y == y && v.z == z)
                .findFirst();
    }

    public Set<Vertex> allReachable(Vertex vertex) {
        Set<Vertex> reachable = new HashSet<>();
        allReachable(reachable, vertex);
        reachable.remove(vertex);
        System.out.println("All reachable vertices from " + vertex + " is " + reachable);
        return reachable;
    }

    public Optional<LinkedList<Vertex>> shortestPath(Vertex source, Vertex target) {
        // source: https://en.wikipedia.org/wiki/Dijkstra's_algorithm#Pseudocode

        HashMap<Vertex, Vertex> prev = new HashMap<>();
        HashMap<Vertex, Float> dist = new HashMap<>();

        Set<Vertex> Q = new HashSet<>();

        for(Vertex v : adjVertices.keySet()) {
            prev.put(v, null);
            dist.put(v, Float.MAX_VALUE);
            Q.add(v);
        }

        dist.put(source, 0f);

        while(!Q.isEmpty()) {

            Vertex u = dist.entrySet().stream()
                    .filter(e -> Q.contains(e.getKey()))
                    .min((e1, e2) -> Float.compare(e1.getValue(), e2.getValue())).get().getKey();

            Q.remove(u);

            // if finished
            if(u == target) {
                LinkedList<Vertex> S = new LinkedList<>();
                if(prev.containsKey(u) || u == source) {
                    while(u != null) {
                        S.addFirst(u);
                        u = prev.get(u);
                    }
                    return Optional.of(S);
                }
            }

            for(Vertex v : adjVertices.get(u).stream()
                    .filter(Q::contains)
                    .collect(Collectors.toSet())) {

                System.out.println("Sammenlikner " + u.getName() + "(h=" + dist.get(u) + ") og " + v.getName() + "(h=" + dist.get(v) + "), heuristics mellom dem er " + u.heristic(v));

                float alt = dist.get(u) + u.heristic(v);
                if(alt <= dist.get(v)) {
                    System.out.println("    Ny kortere heuristic er " + alt);
                    dist.put(v, alt);
                    prev.put(v, u);
                }
            }
        }

        return Optional.empty();
    }

    private void allReachable(Set<Vertex> vertices, Vertex vertex) {
        for (Vertex neighbor : adjVertices.get(vertex)) {
            if (!vertices.contains(neighbor)) {
                vertices.add(neighbor);
                allReachable(vertices, neighbor);
            }
        }
    }

    private <T> LinkedList<T> reverse(LinkedList<T> list) {
        LinkedList<T> revLinkedList = new LinkedList<>();
        for (int i = list.size() - 1; i >= 0; i--) {
            revLinkedList.add(list.get(i));
        }
        return revLinkedList;
    }
}
