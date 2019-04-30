package app.sagen.serverflight.util;

import lombok.Data;

import java.util.*;

@Data
public class Graph<D> {
    private String world;
    private Map<Vertex<D>, List<Vertex<D>>> adjVertices = new HashMap<>();

    public void addVertex(Vertex<D> vertex) {
        adjVertices.putIfAbsent(vertex, new ArrayList<>());
    }

    public void removeVertex(Vertex<D> vertex) {
        adjVertices.values().forEach(e -> e.remove(vertex));
        adjVertices.remove(vertex);
    }

    public void addEdge(Vertex<D> vertex1, Vertex<D> vertex2) {
        adjVertices.get(vertex1).add(vertex2);
        adjVertices.get(vertex2).add(vertex1);
    }

    public void removeEdge(Vertex<D> vertex1, Vertex<D> vertex2) {
        List<Vertex<D>> eV1 = adjVertices.get(vertex1);
        List<Vertex<D>> eV2 = adjVertices.get(vertex2);
        if (eV1 != null)
            eV1.remove(vertex2);
        if (eV2 != null)
            eV2.remove(vertex1);
    }

    public List<Vertex<D>> getAdjVertices(Vertex<D> vertex) {
        return adjVertices.get(vertex);
    }

    public Optional<Vertex<D>> getByName(String name) {
        return adjVertices.keySet().stream()
                .filter(v -> v.name.equalsIgnoreCase(name))
                .findFirst();
    }

    public Optional<Vertex<D>> getByLocation(int x, int y, int z) {
        return adjVertices.keySet().stream()
                .filter(v -> v.x == x && v.y == y && v.z == z)
                .findFirst();
    }

    public Optional<LinkedList<Vertex<D>>> shortestPath(Vertex<D> start, Vertex<D> end) {
        HashMap<Vertex<D>, Vertex<D>> vertexParent = new HashMap<>();
        HashMap<Vertex<D>, Double> vertexHeuristics = new HashMap<>();
        Set<Vertex<D>> visited = new HashSet<>();

        vertexHeuristics.put(start, 0d);

        Vertex<D> current = start;
        for (; ; ) {
            double currentHeuristic = vertexHeuristics.get(current);

            // update heuristics of all non-visited neighbors
            for (Vertex<D> neighbor : adjVertices.get(current)) {
                if (visited.contains(neighbor)) continue;
                double neightborHeuristic = currentHeuristic + current.heristic(neighbor);
                if (!vertexHeuristics.containsKey(neighbor)
                        || vertexHeuristics.get(neighbor) > neightborHeuristic) {
                    vertexHeuristics.put(neighbor, neightborHeuristic);
                    vertexParent.put(neighbor, current);
                }
            }

            // mark current as visited
            visited.add(current);

            // check for completion
            if (visited.contains(end)) {
                // we found a path
                LinkedList<Vertex<D>> vertices = new LinkedList<>();
                Vertex<D> cur = end;
                vertices.add(end);
                while (vertexParent.containsKey(cur)) {
                    cur = vertexParent.get(cur);
                    vertices.add(cur);
                }
                return Optional.of(reverse(vertices));
            }

            Vertex<D> tmpCurrent = current;
            Optional<Vertex<D>> min = adjVertices.keySet().stream()
                    .filter(v -> !visited.contains(v))
                    .filter(vertexHeuristics::containsKey)
                    .filter(v -> !tmpCurrent.equals(v))
                    .min((v1, v2) -> {
                        double h1 = vertexHeuristics.get(v1);
                        double h2 = vertexHeuristics.get(v2);
                        return Double.compare(h1, h2);
                    });
            if (!min.isPresent()) {
                // no more nodes to visit
                return Optional.empty();
            }

            current = min.get();
        }
    }

    private <T> LinkedList<T> reverse(LinkedList<T> list) {
        LinkedList<T> revLinkedList = new LinkedList<>();
        for (int i = list.size() - 1; i >= 0; i--) {
            revLinkedList.add(list.get(i));
        }
        return revLinkedList;
    }

    @Data
    public static class Vertex <D> {
        String name;
        double x;
        double y;
        double z;
        D data;

        public Vertex(String name, double x, double y, double z, D data) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
            this.data = data;
        }

        public double heristic(Vertex vertex) {
            return Math.sqrt(
                    Math.pow(this.x - vertex.x, 2)
                            + Math.pow(this.y - vertex.y, 2)
                            + Math.pow(this.z - vertex.z, 2));
        }
    }
}
