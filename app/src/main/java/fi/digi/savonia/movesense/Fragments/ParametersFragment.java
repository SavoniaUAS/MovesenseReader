package fi.digi.savonia.movesense.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import fi.digi.savonia.movesense.Models.MeasurementInterval;
import fi.digi.savonia.movesense.R;
import fi.digi.savonia.movesense.Tools.MovesenseHelper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ParametersFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ParametersFragment extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;

    Spinner spinnerTemperature;
    Spinner spinnerBatteryLevel;
    Spinner spinnerLinearAcceleration;
    Spinner spinnerGyroscope;
    Spinner spinnerMagnetometer;
    Spinner spinnerHeartRate;
    Spinner spinnerECG;

    CheckBox checkBoxTemperature;
    CheckBox checkBoxBatteryLevel;
    CheckBox checkBoxLinearAcceleration;
    CheckBox checkBoxGyroscope;
    CheckBox checkBoxMagnetometer;
    CheckBox checkBoxHeartRate;
    CheckBox checkBoxECG;

    TextView title;

    Button startMeasurements;
    boolean measurementOnProgress = false;

    ArrayAdapter<String> arrayAdapterTemperature;
    ArrayAdapter<String> arrayAdapterBatteryLevel;
    ArrayAdapter<String> arrayAdapterLinearAcceleration;
    ArrayAdapter<String> arrayAdapterGyroscope;
    ArrayAdapter<String> arrayAdapterMagnetometer;
    ArrayAdapter<String> arrayAdapterHeartRate;
    ArrayAdapter<String> arrayAdapterECG;

    MeasurementInterval measurementIntervalTemperature;
    MeasurementInterval measurementIntervalBatteryLevel;
    MeasurementInterval measurementIntervalLinearAcceleration;
    MeasurementInterval measurementIntervalGyroscope;
    MeasurementInterval measurementIntervalMagnetometer;
    MeasurementInterval measurementIntervalHeartRate;
    MeasurementInterval measurementIntervalECG;
    MeasurementInterval measurementIntervalImu;


    public ParametersFragment() {

        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_parameters, container, false);

        InitFragment(view);

        return view;
    }

    private void InitFragment(View view)
    {
        startMeasurements = view.findViewById(R.id.parameter_startButton);
        startMeasurements.setOnClickListener(this);
        startMeasurements.setEnabled(false);

        title = view.findViewById(R.id.parameter_title);

        spinnerTemperature = view.findViewById(R.id.parameter_temperature_multiselect);
        spinnerBatteryLevel = view.findViewById(R.id.parameter_battery_level_multiselect);
        spinnerLinearAcceleration = view.findViewById(R.id.parameter_linear_acceleration_multiselect);
        spinnerGyroscope = view.findViewById(R.id.parameter_gyroscope_multiselect);
        spinnerMagnetometer = view.findViewById(R.id.parameter_magnetometer_multiselect);
        spinnerHeartRate = view.findViewById(R.id.parameter_heart_rate_multiselect);
        spinnerECG = view.findViewById(R.id.parameter_ecg_multiselect);
        spinnerTemperature.setEnabled(false);
        spinnerBatteryLevel.setEnabled(false);
        spinnerLinearAcceleration.setEnabled(false);
        spinnerGyroscope.setEnabled(false);
        spinnerMagnetometer.setEnabled(false);
        spinnerHeartRate.setEnabled(false);
        spinnerECG.setEnabled(false);

        checkBoxTemperature = view.findViewById(R.id.parameter_temperature_choice);
        checkBoxBatteryLevel = view.findViewById(R.id.parameter_battery_level_choice);
        checkBoxLinearAcceleration = view.findViewById(R.id.parameter_linear_acceleration_choice);
        checkBoxGyroscope = view.findViewById(R.id.parameter_gyroscope_choice);
        checkBoxMagnetometer = view.findViewById(R.id.parameter_magnetometer_choice);
        checkBoxHeartRate = view.findViewById(R.id.parameter_heart_rate_choice);
        checkBoxECG = view.findViewById(R.id.parameter_ecg_choice);
        checkBoxTemperature.setEnabled(false);
        checkBoxBatteryLevel.setEnabled(false);
        checkBoxLinearAcceleration.setEnabled(false);
        checkBoxGyroscope.setEnabled(false);
        checkBoxMagnetometer.setEnabled(false);
        checkBoxHeartRate.setEnabled(false);
        checkBoxECG.setEnabled(false);

        AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                switch (adapterView.getId())
                {
                    case R.id.parameter_temperature_multiselect:
                        measurementIntervalTemperature.Select(i);
                        break;
                    case R.id.parameter_battery_level_multiselect:
                        measurementIntervalBatteryLevel.Select(i);
                        break;
                    case R.id.parameter_linear_acceleration_multiselect:
                        measurementIntervalLinearAcceleration.Select(i);
                        if(spinnerGyroscope.getSelectedItemPosition()!=i)
                            spinnerGyroscope.setSelection(i);
                        if(spinnerMagnetometer.getSelectedItemPosition()!=i)
                            spinnerMagnetometer.setSelection(i);
                        break;
                    case R.id.parameter_gyroscope_multiselect:
                        measurementIntervalGyroscope.Select(i);
                        if(spinnerLinearAcceleration.getSelectedItemPosition()!=i)
                            spinnerLinearAcceleration.setSelection(i);
                        if(spinnerMagnetometer.getSelectedItemPosition()!=i)
                            spinnerMagnetometer.setSelection(i);
                        break;
                    case R.id.parameter_magnetometer_multiselect:
                        measurementIntervalMagnetometer.Select(i);
                        if(spinnerGyroscope.getSelectedItemPosition()!=i)
                            spinnerGyroscope.setSelection(i);
                        if(spinnerLinearAcceleration.getSelectedItemPosition()!=i)
                            spinnerLinearAcceleration.setSelection(i);
                        break;
                    case R.id.parameter_heart_rate_multiselect:
                        measurementIntervalHeartRate.Select(i);
                        break;
                    case R.id.parameter_ecg_multiselect:
                        measurementIntervalECG.Select(i);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        };

        spinnerTemperature.setOnItemSelectedListener(onItemSelectedListener);
        spinnerBatteryLevel.setOnItemSelectedListener(onItemSelectedListener);
        spinnerLinearAcceleration.setOnItemSelectedListener(onItemSelectedListener);
        spinnerGyroscope.setOnItemSelectedListener(onItemSelectedListener);
        spinnerMagnetometer.setOnItemSelectedListener(onItemSelectedListener);
        spinnerHeartRate.setOnItemSelectedListener(onItemSelectedListener);
        spinnerECG.setOnItemSelectedListener(onItemSelectedListener);

        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                switch (compoundButton.getId())
                {
                    case R.id.parameter_temperature_choice:
                        measurementIntervalTemperature.Enabled = b;
                        break;
                    case R.id.parameter_battery_level_choice:
                        measurementIntervalBatteryLevel.Enabled = b;
                        break;
                    case R.id.parameter_linear_acceleration_choice:
                        measurementIntervalLinearAcceleration.Enabled = b;
                        break;
                    case R.id.parameter_gyroscope_choice:
                        measurementIntervalGyroscope.Enabled = b;
                        break;
                    case R.id.parameter_magnetometer_choice:
                        measurementIntervalMagnetometer.Enabled = b;
                        break;
                    case R.id.parameter_heart_rate_choice:
                        measurementIntervalHeartRate.Enabled = b;
                        break;
                    case R.id.parameter_ecg_choice:
                        measurementIntervalECG.Enabled = b;
                        break;
                }
                if(measurementIntervalTemperature.Enabled || measurementIntervalBatteryLevel.Enabled || measurementIntervalECG.Enabled || measurementIntervalHeartRate.Enabled || measurementIntervalLinearAcceleration.Enabled || measurementIntervalGyroscope.Enabled || measurementIntervalMagnetometer.Enabled)
                {
                    startMeasurements.setEnabled(true);
                }
                else
                {
                    startMeasurements.setEnabled(false);
                }
            }
        };

        checkBoxTemperature.setOnCheckedChangeListener(onCheckedChangeListener);
        checkBoxBatteryLevel.setOnCheckedChangeListener(onCheckedChangeListener);
        checkBoxLinearAcceleration.setOnCheckedChangeListener(onCheckedChangeListener);
        checkBoxGyroscope.setOnCheckedChangeListener(onCheckedChangeListener);
        checkBoxMagnetometer.setOnCheckedChangeListener(onCheckedChangeListener);
        checkBoxHeartRate.setOnCheckedChangeListener(onCheckedChangeListener);
        checkBoxECG.setOnCheckedChangeListener(onCheckedChangeListener);

        mListener.onReady();

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
    public void onClick(View view) {

        List<MeasurementInterval> measurementIntervals = new ArrayList<>();
        measurementIntervals.add(measurementIntervalTemperature);
        measurementIntervals.add(measurementIntervalBatteryLevel);
        measurementIntervals.add(measurementIntervalECG);
        measurementIntervals.add(measurementIntervalHeartRate);
        if(measurementIntervalLinearAcceleration.Enabled && measurementIntervalGyroscope.Enabled && measurementIntervalMagnetometer.Enabled)
        {
            MeasurementInterval copy = measurementIntervalLinearAcceleration.Copy();
            copy.sensor = MovesenseHelper.Sensor.IMU9;
            measurementIntervals.add(copy);
        }
        else if(measurementIntervalLinearAcceleration.Enabled && measurementIntervalGyroscope.Enabled)
        {
            MeasurementInterval copy = measurementIntervalLinearAcceleration.Copy();
            copy.sensor = MovesenseHelper.Sensor.IMU6;
            measurementIntervals.add(copy);
        }
        else if(measurementIntervalLinearAcceleration.Enabled && measurementIntervalMagnetometer.Enabled)
        {
            MeasurementInterval copy = measurementIntervalLinearAcceleration.Copy();
            copy.sensor = MovesenseHelper.Sensor.IMU6m;
            measurementIntervals.add(copy);
        }
        else
        {
            measurementIntervals.add(measurementIntervalLinearAcceleration);
            measurementIntervals.add(measurementIntervalGyroscope);
            measurementIntervals.add(measurementIntervalMagnetometer);
        }

        measurementOnProgress = !measurementOnProgress;
        if(measurementOnProgress)
        {
            startMeasurements.setText(R.string.parameters_parameter_start_button_on_progress);
            title.setText(R.string.parameters_parameter_title_on_progress);
            mListener.onStartButtonPressed(measurementIntervals.toArray(new MeasurementInterval[measurementIntervals.size()]));
            SetFieldsEnabled(false);
        }
        else
        {
            startMeasurements.setText(R.string.parameteres_parameter_start_button);
            title.setText(R.string.parameters_title);
            mListener.onStopButtonPressed();
            SetFieldsEnabled(true);
        }


    }

    private void SetFieldsEnabled(boolean enable)
    {
        spinnerTemperature.setEnabled(enable);
        spinnerBatteryLevel.setEnabled(enable);
        spinnerHeartRate.setEnabled(enable);
        spinnerECG.setEnabled(enable);
        spinnerLinearAcceleration.setEnabled(enable);
        spinnerGyroscope.setEnabled(enable);
        spinnerMagnetometer.setEnabled(enable);

        checkBoxTemperature.setEnabled(enable);
        checkBoxBatteryLevel.setEnabled(enable);
        checkBoxHeartRate.setEnabled(enable);
        checkBoxECG.setEnabled(enable);
        checkBoxLinearAcceleration.setEnabled(enable);
        checkBoxGyroscope.setEnabled(enable);
        checkBoxMagnetometer.setEnabled(enable);

    }

    public void SetMeasurementIntervals(MeasurementInterval measurementInterval, MovesenseHelper.Sensor sensor)
    {

        switch (sensor)
        {
            case Temperature:
                arrayAdapterTemperature = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, measurementInterval.getDataRateArrayString());
                measurementIntervalTemperature = measurementInterval;
                spinnerTemperature.setAdapter(arrayAdapterTemperature);
                spinnerTemperature.setEnabled(true);
                checkBoxTemperature.setEnabled(true);
                break;
            case BatteryVoltage:
                arrayAdapterBatteryLevel = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, measurementInterval.getDataRateArrayString());
                measurementIntervalBatteryLevel = measurementInterval;
                spinnerBatteryLevel.setAdapter(arrayAdapterBatteryLevel);
                spinnerBatteryLevel.setEnabled(true);
                checkBoxBatteryLevel.setEnabled(true);
                break;
            case LinearAcceleration:
                arrayAdapterLinearAcceleration = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, measurementInterval.getDataRateArrayString());
                measurementIntervalLinearAcceleration = measurementInterval;
                spinnerLinearAcceleration.setAdapter(arrayAdapterLinearAcceleration);

                break;
            case Gyroscope:
                arrayAdapterGyroscope = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, measurementInterval.getDataRateArrayString());
                measurementIntervalGyroscope = measurementInterval;
                spinnerGyroscope.setAdapter(arrayAdapterGyroscope);

                break;
            case Magnetometer:
                arrayAdapterMagnetometer = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, measurementInterval.getDataRateArrayString());
                measurementIntervalMagnetometer = measurementInterval;
                spinnerMagnetometer.setAdapter(arrayAdapterMagnetometer);

                break;
            case ECG:
                arrayAdapterECG = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, measurementInterval.getDataRateArrayString());
                measurementIntervalECG = measurementInterval;
                spinnerECG.setAdapter(arrayAdapterECG);
                spinnerECG.setEnabled(true);
                checkBoxECG.setEnabled(true);
                break;
            case IMU6:
            case IMU6m:
            case IMU9:
                //arrayAdapterTemperature = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_dropdown_item, measurementInterval.getDataRateArrayString());
                break;
            case HeartRate:
                arrayAdapterHeartRate = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, measurementInterval.getDataRateArrayString());
                measurementIntervalHeartRate = measurementInterval;
                spinnerHeartRate.setAdapter(arrayAdapterHeartRate);
                spinnerHeartRate.setEnabled(true);
                checkBoxHeartRate.setEnabled(true);
                break;
        }
        if(measurementIntervalTemperature!= null && measurementIntervalGyroscope!= null && measurementIntervalMagnetometer != null)
        {
            spinnerMagnetometer.setEnabled(true);
            checkBoxMagnetometer.setEnabled(true);
            spinnerGyroscope.setEnabled(true);
            checkBoxGyroscope.setEnabled(true);
            spinnerLinearAcceleration.setEnabled(true);
            checkBoxLinearAcceleration.setEnabled(true);
        }
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
        void onStartButtonPressed(MeasurementInterval[] measurementIntervals);

        void onStopButtonPressed();

        void onReady();
    }
}
