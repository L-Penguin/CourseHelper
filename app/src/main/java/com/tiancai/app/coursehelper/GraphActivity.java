package com.tiancai.app.coursehelper;

import android.accounts.AccountAuthenticatorResponse;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.security.keystore.StrongBoxUnavailableException;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.tiancai.app.lib.Course;
import com.tiancai.app.lib.CourseParser;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import de.blox.graphview.Node;
import de.blox.graphview.BaseGraphAdapter;
import de.blox.graphview.Graph;
import de.blox.graphview.GraphAdapter;
import de.blox.graphview.GraphView;
import de.blox.graphview.tree.BuchheimWalkerAlgorithm;
import de.blox.graphview.tree.BuchheimWalkerConfiguration;

public class GraphActivity extends AppCompatActivity {
    private int nodeCount = 1;
    private Node currentNode;
    protected BaseGraphAdapter<ViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);


        Log.d("graphactivity", "" + (message));

        try {
            new GetCourseTask().execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class GetCourseTask extends AsyncTask<String, Void, Course> {
        protected Course doInBackground(String... task_url) {
            return CourseParser.courseFactory("2019","SPRING", task_url[0]);
        }

        protected void onPostExecute(Course result) {
            final Graph graph = createGraph(result);
            setupFab(graph);
            setupAdapter(graph);
            setupToolbar();
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupAdapter(Graph graph) {
        final GraphView graphView = findViewById(R.id.graph);

        adapter = new BaseGraphAdapter<ViewHolder>(this, R.layout.node, graph) {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(View view) {
                return new ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(ViewHolder viewHolder, Object data, int position) {
                if (data == null) {
                    viewHolder.textView.setText("No Course Found");
                } else {
                    viewHolder.textView.setText(data.toString());
                }
            }
        };

        setAlgorithm(adapter);

        graphView.setAdapter(adapter);
        graphView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentNode = adapter.getNode(position);
                Snackbar.make(graphView, "Clicked on " + currentNode.getData().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFab(final Graph graph) {

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final BuchheimWalkerConfiguration.Builder builder = new BuchheimWalkerConfiguration.Builder()
                .setSiblingSeparation(100)
                .setLevelSeparation(300)
                .setSubtreeSeparation(300);
        builder.setOrientation(BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM);
        adapter.setAlgorithm(new BuchheimWalkerAlgorithm(builder.build()));
        adapter.notifyInvalidated();
        return true;
    }

    public Graph createGraph(Course course) {
        System.out.println("creating graph");
        Graph graph = new Graph();

        Node parentNode = new Node(new UniqueClass(course.getCourseName() + course.getCourseNum()));

        parseNode(graph, parentNode, course);

        return graph;
    }
    private class UniqueClass {
        private String string;

        UniqueClass(String setStr) {
            string = setStr;
        }

        @Override
        public String toString() {
            return string;
        }

        public String getString() {
            return string;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }
    public void parseNode(Graph graph, Node parentNode, Course course) {
        Node node = new Node(new UniqueClass(course.getCourseName() + course.getCourseNum()));
        node.setSize(10,20);
        System.out.println(parentNode.getData().getClass());
        if (!(((UniqueClass) parentNode.getData()).getString().equals(((UniqueClass) node.getData()).getString()))) {
            graph.addEdge(parentNode, node);
        }

        if (course.getCourseName().equals(CourseParser.COURSE_NOT_FOUND)) {
            return;
        } else if (course.getCourseName().equals(CourseParser.NO_PREREQUISITE)) {
            return;
        }



        for (List<Course> each : course.getPrerequisites()) {
            for (Course each_c : each) {
                if (each_c.getPrerequisites().size() == 0) {
                    continue;
                }
                if (each_c.getCourseName().equals("MATH") && each_c.getCourseNum().equals("221")) {
                    continue;
                }

                parseNode(graph, node, each_c);
            }
        }
    }

    public void setAlgorithm(GraphAdapter adapter) {
        final BuchheimWalkerConfiguration configuration = new BuchheimWalkerConfiguration.Builder()
                .setSiblingSeparation(100)
                .setLevelSeparation(300)
                .setSubtreeSeparation(300)
                .setOrientation(BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM)
                .build();
        adapter.setAlgorithm(new BuchheimWalkerAlgorithm(configuration));
    }

    public class ViewHolder {
        TextView textView;
        ViewHolder(View view) {
            textView = view.findViewById(R.id.textView);
        }
    }

    protected String getNodeText() {
        return "Node" + nodeCount++;
    }
}
