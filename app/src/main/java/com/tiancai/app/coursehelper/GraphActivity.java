package com.tiancai.app.coursehelper;

import android.accounts.AccountAuthenticatorResponse;
import android.content.Intent;
import android.os.StrictMode;
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

        Course course = new Course("MATH","241");
        Course course1 = new Course("MATH", "231");
        Course course2 = new Course("MATH", "221");
        List<Course> list1 = new ArrayList<>();
        List<Course> list2 = new ArrayList<>();
        list1.add(course1);
        list2.add(course2);
        course.appendPrerequisite(list1);
        course.appendPrerequisite(list2);

        Log.d("graphactivity", "" + (message));

        course = CourseParser.courseFactory("2019","SPRING", message);

        Log.d("graphactivity", course.getCourseName() + course.getCourseNum());

        final Graph graph = createGraph(course);
        setupFab(graph);
        setupAdapter(graph);
        setupToolbar();
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
                viewHolder.textView.setText(data.toString());
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
        Graph graph = new Graph();

        List<Node> nodeList = new ArrayList<>();

        Node parentNode = new Node(course.getCourseName() + course.getCourseNum());

        parseNode(graph, parentNode, course);

        return graph;
    }

    public void parseNode(Graph graph, Node parentNode, Course course) {

        Node node = new Node(course.getCourseName() + course.getCourseNum());
        if (!parentNode.getData().equals(node.getData())) {
            graph.addEdge(parentNode, node);
        }
        for (List<Course> each : course.getPrerequisites()) {
            for (Course each_c : each) {
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
