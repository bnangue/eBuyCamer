package com.bricenangue.nextgeneration.ebuycamer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by bricenangue on 04/03/16.
 */
public class AlertDialogSetUsername extends DialogFragment {


    public interface OnSetUsername{
       void setUsername(String username);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof OnSetUsername)) {
            throw new ClassCastException(activity.toString() + " must implement OnSetUsername");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final AlertDialog dialoglog = new AlertDialog.Builder(getContext()).create();
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vw = inflater.inflate(R.layout.alertdialog_edittext, null);

        dialoglog.setView(vw);
        final EditText editTextemail=(EditText)vw.findViewById(R.id.editText_alertdialog_name);
        Button btnOKemail=(Button)vw.findViewById(R.id.button_confirm_username_alertdialog);

        btnOKemail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username=editTextemail.getText().toString();
                if(TextUtils.isEmpty(username)){
                    editTextemail.setError("this field cannot be empty");
                }else {

                    ((OnSetUsername) getActivity()).setUsername(username);
                    dialoglog.dismiss();

                }

            }
        });

        dialoglog.setCancelable(false);


        return dialoglog;
    }

}
