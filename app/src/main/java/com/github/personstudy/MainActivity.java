package com.github.personstudy;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.piegraphview)
    public PieGraphView pieGraphView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        PieGraphView.ItemGroup[] itemGroups = new PieGraphView.ItemGroup[1];
        PieGraphView.ItemGroup item = new PieGraphView.ItemGroup();
        item.id = System.currentTimeMillis()+"";

        PieGraphView.Item[] items = new PieGraphView.Item[3];
        items[0] = new PieGraphView.Item();
        items[0].color = Color.RED;
        items[0].value = 50;
        items[1] = new PieGraphView.Item();
        items[1].color = Color.YELLOW;
        items[1].value = 50;
        items[2] = new PieGraphView.Item();
        items[2].color = Color.BLACK;
        items[2].value = 50;
        item.items = items;

        itemGroups[0] = item ;
        pieGraphView.setData(itemGroups);
    }
}
