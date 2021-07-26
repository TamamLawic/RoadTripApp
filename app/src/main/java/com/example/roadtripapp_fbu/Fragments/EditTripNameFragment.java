package com.example.roadtripapp_fbu.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.roadtripapp_fbu.R;
/**
 * Fragment Dialog, Allows the user to name the trip they are creating. Uses Parse to create new trip, and collaborator.
 * Takes user to map page to start planning the trip.
 */
public class EditTripNameFragment extends DialogFragment {
    EditText etTripName;
    Button btnRenameTrip;

    public EditTripNameFragment() {
        // Empty constructor is required for DialogFragment
    }

    public static EditTripNameFragment newInstance(String title) {
        EditTripNameFragment frag = new EditTripNameFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_trip_name, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        etTripName = (EditText) view.findViewById(R.id.etTripName);
        btnRenameTrip = view.findViewById(R.id.btnRenameTrip);
        // Fetch arguments from bundle and set title
        String title = getArguments().getString("title", "Enter Name");
        getDialog().setTitle(title);
        // Show soft keyboard automatically and request focus to field
        etTripName.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        //set onclickListener for rename button
        btnRenameTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBackResult();
            }
        });
    }

    // Defines the listener interface
    public interface EditNameDialogListener {
        void onFinishEditDialog(String inputText);
    }

    // Call this method to send the data back to the parent fragment
    public void sendBackResult() {
        // Notice the use of `getTargetFragment` which will be set when the dialog is displayed
        EditNameDialogListener listener = (EditNameDialogListener) getTargetFragment();
        listener.onFinishEditDialog(etTripName.getText().toString());
        dismiss();
    }
}