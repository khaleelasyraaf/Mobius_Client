package com.example.mobius;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class DialogID extends AppCompatDialogFragment {

    private EditText myEditID;
    private DialogIDListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog_id, null);

        builder.setView(view)
                .setTitle("Login")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String userID = "" + myEditID.getText().toString().replaceAll("\\s+", "");;
                        listener.applyTexts(userID);
                    }
                });

        myEditID = view.findViewById(R.id.editID);

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (DialogIDListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement DialogIDListener");
        }
    }

    public interface DialogIDListener {
        void applyTexts(String userID);
    }
}
