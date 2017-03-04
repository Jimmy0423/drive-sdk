package com.plotagon.Drive;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.drive.DriveId;
import com.plotagon.Drive.model.Plot;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mitra on 2017-03-02.
 */

public class ManuscriptsListView extends ListFragment {

    private List<Plot> manuscriptsList;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        manuscriptsList = new ArrayList<>();
        setListAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, manuscriptsList));

        ((BaseDrive) getActivity()).setListener(new BaseDrive.OnFragmentInteractionListener() {
            @Override
            public void onFragmentInteraction(List<Plot> plots) {
                Log.d("Callback", plots.toString());
                manuscriptsList.clear();
                manuscriptsList.addAll(plots);
                ArrayAdapter<Plot> adapter = (ArrayAdapter<Plot>) getListAdapter();
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
            Plot selectedPlot = (Plot) getListAdapter().getItem(position);
            boolean isLocal = selectedPlot.isLocal();
            Log.d("Selected", selectedPlot.getDriveId().encodeToString());
            DriveId driveId = selectedPlot.getDriveId();
        ((BaseDrive) getActivity()).openPlot(driveId);
    }
}

