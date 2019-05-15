/*
 * MIT License
 *
 * Copyright (c) 2019 Alexander Meisdalen Sagen <alexmsagen@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package app.sagen.serverflight.util;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString
public class Graph {
    private String world;
    private Map<Vertex, Set<Vertex>> vertexMap = new HashMap<>();

    public void addVertex(Vertex vertex) {
        vertexMap.putIfAbsent(vertex, new HashSet<>());
    }

    public void removeVertex(Vertex vertex) {
        vertexMap.values().forEach(e -> e.remove(vertex));
        vertexMap.remove(vertex);
    }

    public void addEdge(Vertex vertex1, Vertex vertex2) {
        vertexMap.get(vertex1).add(vertex2);
        vertexMap.get(vertex2).add(vertex1);
    }

    public void removeEdge(Vertex vertex1, Vertex vertex2) {
        Set<Vertex> eV1 = vertexMap.get(vertex1);
        Set<Vertex> eV2 = vertexMap.get(vertex2);
        if (eV1 != null)
            eV1.remove(vertex2);
        if (eV2 != null)
            eV2.remove(vertex1);
    }

    public Set<Vertex> getNeighborsOf(Vertex vertex) {
        return vertexMap.get(vertex);
    }

    public Set<Vertex> getAllVertices() {
        return vertexMap.keySet();
    }

    public Optional<Vertex> getByName(String name) {
        return vertexMap.keySet().stream()
                .filter(v -> v.name.equalsIgnoreCase(name))
                .findFirst();
    }

    public Set<Vertex> allReachableFrom(Vertex vertex) {
        Set<Vertex> reachable = new HashSet<>();
        allReachableFrom(reachable, vertex);
        reachable.remove(vertex);
        return reachable;
    }

    public Optional<LinkedList<Vertex>> shortestPath(Vertex source, Vertex target) {
        // source: https://en.wikipedia.org/wiki/Dijkstra's_algorithm#Pseudocode

        HashMap<Vertex, Vertex> prev = new HashMap<>();
        HashMap<Vertex, Float> dist = new HashMap<>();

        Set<Vertex> Q = new HashSet<>();

        for (Vertex v : vertexMap.keySet()) {
            prev.put(v, null);
            dist.put(v, Float.MAX_VALUE);
            Q.add(v);
        }

        dist.put(source, 0f);

        while (!Q.isEmpty()) {

            Vertex u = dist.entrySet().stream()
                    .filter(e -> Q.contains(e.getKey()))
                    .min((e1, e2) -> Float.compare(e1.getValue(), e2.getValue())).get().getKey();

            Q.remove(u);

            // if finished
            if (u == target) {
                LinkedList<Vertex> S = new LinkedList<>();
                if (prev.containsKey(u) || u == source) {
                    while (u != null) {
                        S.addFirst(u);
                        u = prev.get(u);
                    }
                    return Optional.of(S);
                }
            }

            for (Vertex v : vertexMap.get(u).stream()
                    .filter(Q::contains)
                    .collect(Collectors.toSet())) {
                float alt = dist.get(u) + u.heristic(v);
                if (alt <= dist.get(v)) {
                    dist.put(v, alt);
                    prev.put(v, u);
                }
            }
        }

        return Optional.empty();
    }

    private void allReachableFrom(Set<Vertex> vertices, Vertex vertex) {
        for (Vertex neighbor : vertexMap.get(vertex)) {
            if (!vertices.contains(neighbor)) {
                vertices.add(neighbor);
                allReachableFrom(vertices, neighbor);
            }
        }
    }
}
