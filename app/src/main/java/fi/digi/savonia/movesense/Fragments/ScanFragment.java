package fi.digi.savonia.movesense.Fragments;

import android.content.Context;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import fi.digi.savonia.movesense.R;
import fi.digi.savonia.movesense.Tools.CustomDeviceListAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.polidea.rxandroidble2.RxBleDevice;

import java.sql.Array;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ScanFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ScanFragment extends Fragment implements AdapterView.OnItemLongClickListener, View.OnClickListener {


    public enum VisibilityOption
    {
        Visible,
        Invisible
    }

    private OnFragmentInteractionListener mListener;
    Button scan;
    ListView devices;
    TextView title;
    CustomDeviceListAdapter customDeviceListAdapter;


    public ScanFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scan, container, false);
        InitViews(view);
        return view;
    }

    public void SetScanButtonVisibility(VisibilityOption visibilityOption)
    {
        if(visibilityOption == VisibilityOption.Visible)
        {
            scan.setVisibility(View.VISIBLE);
        }
        else if(visibilityOption == VisibilityOption.Invisible)
        {
            scan.setVisibility(View.INVISIBLE);
        }
    }

    public void AddNewBleDevice(RxBleDevice bleDevice)
    {
        customDeviceListAdapter.add(bleDevice);

    }

    public void ClearList()
    {
        customDeviceListAdapter.clear();
    }

    private void InitListView()
    {
        customDeviceListAdapter = new CustomDeviceListAdapter(getContext(), new ArrayList<RxBleDevice>());
        devices.setAdapter(customDeviceListAdapter);
        devices.setOnItemLongClickListener(this);

    }

    private void InitViews(View view)
    {
        scan = view.findViewById(R.id.scan_buttonScan);
        devices = view.findViewById(R.id.scan_deviceList);
        title = view.findViewById(R.id.scan_title);
        scan.setOnClickListener(this);
        InitListView();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        mListener.onDeviceSelected((RxBleDevice) adapterView.getItemAtPosition(i));
        return false;
    }

    @Override
    public void onClick(View view) {
        mListener.onScanButtonPressed();
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onScanButtonPressed();
        void onDeviceSelected(RxBleDevice bleDevice);
    }
}
