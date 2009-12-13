package com.tinkerpop.gremlin.db.neo.old;

import com.tinkerpop.gremlin.model.parser.GraphMLWriter;
import org.neo4j.api.core.*;
import org.neo4j.util.index.IndexService;
import org.neo4j.util.index.LuceneIndexService;

import java.io.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version 0.1
 */
public class GratefulNeoGraph {

    private NeoService neo;
    private IndexService index;

    public static enum DeadRelationships implements RelationshipType {
        followed_by, written_by, sung_by
    }

    public static final String NEO_DIRECTORY = "/tmp/grateful_neo_graph";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String SONG_TYPE = "song_type";
    public static final String PERFORMANCES = "performances";
    public static final String WEIGHT = "weight";

    public GratefulNeoGraph() {
        neo = new EmbeddedNeo(NEO_DIRECTORY);
        index = new LuceneIndexService(neo);
    }

    public NeoService getNeo() {
        return this.neo;
    }

    public IndexService getIndex() {
        return this.index;
    }

   /* public void loadGratefulDeadGraph() throws Exception {
        deleteGraphDirectory(new File(NEO_DIRECTORY));
        neo = new EmbeddedNeo(NEO_DIRECTORY);
        index = new LuceneIndexService(neo);
        // LOAD SONG FOLLOWS GRAPH
        Transaction tx = neo.beginTx();
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(GratefulNeoGraph.class.getResourceAsStream("../../model/parser/raw/song-follows-net.txt")));
            String line = input.readLine();
            while (line != null) {
                String[] edge = line.split("\t");
                Node startSong = index.getSingleNode(NAME, edge[0]);
                if (null == startSong) {
                    startSong = neo.createNode();
                    startSong.setProperty(NAME, edge[0]);
                    startSong.setProperty(TYPE, "song");
                    index.index(startSong, NAME, edge[0]);
                }

                Node endSong = index.getSingleNode(NAME, edge[1]);
                if (null == endSong) {
                    endSong = neo.createNode();
                    endSong.setProperty(NAME, edge[1]);
                    endSong.setProperty(TYPE, "song");
                    index.index(endSong, NAME, edge[1]);

                }
                if (!startSong.getProperty(NAME).equals(endSong.getProperty(NAME))) {
                    System.out.println(startSong.getProperty(NAME) + "--FOLLOWED_BY[" + new Float(edge[2]).intValue() + "]-->" + endSong.getProperty(NAME));
                    Relationship r = startSong.createRelationshipTo(endSong, DeadRelationships.followed_by);
                    r.setProperty(WEIGHT, new Float(edge[2]).intValue());
                }

                line = input.readLine();
            }
            input.close();
            tx.success();
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            tx.finish();
        }

        // LOAD SONG AUTHOR/SINGER NETWORK
        tx = neo.beginTx();
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(GratefulNeoGraph.class.getResourceAsStream("../../model/parser/raw/author-singer-net.txt")));
            input.readLine();
            String line = input.readLine();
            while (line != null) {
                String[] data = line.split("\t");
                Node song = index.getSingleNode(NAME, data[0]);
                if (null == song) {
                    song = neo.createNode();
                    song.setProperty(NAME, data[0]);
                    song.setProperty(PERFORMANCES, new Integer(data[3]));
                    song.setProperty(SONG_TYPE, data[4]);
                    song.setProperty(TYPE, "song");
                    index.index(song, NAME, data[0]);
                } else {
                    //System.out.println(data[3]);
                    song.setProperty(PERFORMANCES, new Integer(data[3]));
                    song.setProperty(SONG_TYPE, data[4]);
                    song.setProperty(TYPE, "song");
                }

                Node author = index.getSingleNode(NAME, data[1]);
                if (null == author) {
                    author = neo.createNode();
                    author.setProperty(NAME, data[1]);
                    author.setProperty(TYPE, "artist");
                    index.index(author, NAME, data[1]);
                }

                Node singer = index.getSingleNode(NAME, data[2]);
                if (null == singer) {
                    singer = neo.createNode();
                    singer.setProperty(NAME, data[2]);
                    singer.setProperty(TYPE, "artist");
                    index.index(singer, NAME, data[2]);
                }

                System.out.println(song.getProperty(NAME) + "--WRITTEN_BY-->" + author.getProperty(NAME));
                song.createRelationshipTo(author, DeadRelationships.written_by);
                System.out.println(song.getProperty(NAME) + "--SUNG_BY-->" + singer.getProperty(NAME));
                song.createRelationshipTo(singer, DeadRelationships.sung_by);

                line = input.readLine();
            }
            input.close();
            tx.success();

        } catch (IOException e) {
            System.out.println(e);
        } finally {
            tx.finish();
        }


        tx = neo.beginTx();
        try {

            NeoGraph g = new NeoGraph(neo);
            GraphMLWriter.outputGraph(g, new FileOutputStream("/Users/marko/software/gremlin/trunk/src/test/resources/com/tinkerpop/gremlin/model/parser/graph-example-2.xml"));
            tx.success();
        } finally {
           tx.finish();
        }
        neo.shutdown();
        index.shutdown();

        /*tx = neo.beginTx();
        try {
            int counter = 0;
            for(Node node : neo.getAllNodes()) {
                counter++;
                System.out.println(node.getProperty(NAME));
            }
            System.out.println("Total number of nodes: " + counter);
            tx.success();

        } finally {
            tx.finish();
        }
    } */

    public void shutdown() {
        neo.shutdown();
        index.shutdown();
    }

    private void deleteGraphDirectory(File directory) {
        if (directory.exists()) {
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    deleteGraphDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
    }
}