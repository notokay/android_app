package com.example.myfirstapp.app;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by tommy on 7/24/14.
 */
public class BtChatFrag extends Fragment {
    //Debugging
    private static final String TAG = "BtChatFrag";
    private TextView.OnEditorActionListener mWriteListener =
            new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                    // If the action is a key-up event on the return key, send the message
                    if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                        String message = view.getText().toString();
                        sendMessage(message);
                    }
                    Log.i(TAG, "END onEditorAction");
                    return true;
                }
            };
    private StringBuffer mOutStringBuffer;
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    private OnBtChatInteractionListener mListener;

    public BtChatFrag() {
        // Required empty public constructor
    }

    public static BtChatFrag newInstance() {
        BtChatFrag fragment = new BtChatFrag();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_btchat, container, false);

        // Initialize the array adapter for the conversation thread and attaches it to the message layout
        mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message);
        mConversationView = (ListView) view.findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText = (EditText) view.findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener for click events
        mSendButton = (Button) view.findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                Log.e(TAG, "send button clicked");
                TextView textView = (TextView) view.findViewById(R.id.edit_text_out);
                String message = textView.getText().toString();
                sendMessage(message);
            }
        });

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnBtChatInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnBtChatInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void sendMessage(String message) {

        if (mListener != null) {
            // onBtChatInteraction() returns true if message sent successfully
            if (mListener.onBtChatInteraction(message)) {
                // Reset out string buffer to zero and clear the edit text field
                mOutStringBuffer.setLength(0);
                mOutEditText.setText(mOutStringBuffer);
                mConversationArrayAdapter.add("Me:  " + message);
            }
            ;
        }
    }

    public void receive_message(String device_name, String received_message) {
        mConversationArrayAdapter.add(device_name + ":  " + received_message);
    }

    public interface OnBtChatInteractionListener {
        public boolean onBtChatInteraction(String message);
    }
}
