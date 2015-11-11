package com.firebase.uidemo;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseLoginBaseActivity;
import com.firebase.ui.FirebaseRecyclerViewAdapter;

public class RecyclerViewDemoActivity extends FirebaseLoginBaseActivity {

    public static String TAG = "FirebaseUI.chat";
    private Firebase mRef;
    private Query mChatRef;
    private AuthData mAuthData;
    private String name;
    private String uid;
    private Button mSendButton;
    private EditText mMessageEdit;

    private RecyclerView mMessages;
    private FirebaseRecyclerViewAdapter<Chat, ChatHolder> mRecycleViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view_demo);

        mSendButton = (Button) findViewById(R.id.sendButton);
        mMessageEdit = (EditText) findViewById(R.id.messageEdit);

        mRef = new Firebase("https://firebaseui.firebaseio.com/chat_3");
        mChatRef = mRef.limitToLast(50);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Chat chat = new Chat(name, mAuthData.getUid(), mMessageEdit.getText().toString());
            mRef.push().setValue(chat, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    if (firebaseError != null) {
                        Log.e(TAG, firebaseError.toString());
                    }
                }
            });
            mMessageEdit.setText("");
            }
        });

        mMessages = (RecyclerView) findViewById(R.id.messagesList);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);

        mMessages.setHasFixedSize(true);
        mMessages.setLayoutManager(manager);

        mRecycleViewAdapter = new FirebaseRecyclerViewAdapter<Chat, ChatHolder>(Chat.class, R.layout.message, ChatHolder.class, mChatRef) {
            @Override
            public void populateViewHolder(ChatHolder chatView, Chat chat) {

                chatView.setName(chat.getName());
                chatView.setText(chat.getText());

                if (mAuthData != null && chat.getUid().equals(mAuthData.getUid())) {
                    // Is me
                    chatView.setSender(true);
                } else {
                    chatView.setSender(false);
                    // Isn't me
                }
            }
        };

        mMessages.setAdapter(mRecycleViewAdapter);

        updateChat();
    }

    protected void updateChat() {
        mRecycleViewAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_login_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.login_menu_item).setVisible(mAuthData == null);
        menu.findItem(R.id.logout_menu_item).setVisible(mAuthData != null);
        mSendButton.setEnabled(mAuthData != null);
        mMessageEdit.setEnabled(mAuthData != null);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.login_menu_item:
                this.showFirebaseLoginPrompt();
                return true;
            case R.id.logout_menu_item:
                this.logout();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFirebaseLoginSuccess(AuthData authData) {
        Log.i(TAG, "Logged in to " + authData.getProvider().toString());
        mAuthData = authData;

        switch (mAuthData.getProvider()) {
            case "password":
                name = (String) mAuthData.getProviderData().get("email");
                break;
            default:
                name = (String) mAuthData.getProviderData().get("displayName");
                break;
        }

        updateChat();
        invalidateOptionsMenu();
    }

    @Override
    public void onFirebaseLogout() {
        Log.i(TAG, "Logged out");
        mAuthData = null;
        name = "";
        invalidateOptionsMenu();
        updateChat();
    }

    @Override
    public void onFirebaseLoginProviderError(FirebaseError firebaseError) {
        Log.i(TAG, "Login provider error: " + firebaseError.toString());
    }

    @Override
    public void onFirebaseLoginUserError(FirebaseError firebaseError) {
        Log.i(TAG, "Login user error: " + firebaseError.toString());
    }

    @Override
    public Firebase getFirebaseRef() {
        return mRef;
    }

    public static class Chat {
        String name;
        String text;
        String uid;

        public Chat() {
        }

        public Chat(String name, String uid, String message) {
            this.name = name;
            this.text = message;
            this.uid = uid;
        }

        public String getName() {
            return name;
        }

        public String getUid() {
            return uid;
        }

        public String getText() {
            return text;
        }
    }

    public static class ChatHolder extends RecyclerView.ViewHolder {
        View mView;

        public ChatHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setSender(Boolean isSender) {
            FrameLayout left_arrow = (FrameLayout) mView.findViewById(R.id.left_arrow);
            FrameLayout right_arrow = (FrameLayout) mView.findViewById(R.id.right_arrow);
            RelativeLayout messageContainer = (RelativeLayout) mView.findViewById(R.id.message_container);

            if (isSender) {
                left_arrow.setVisibility(View.GONE);
                right_arrow.setVisibility(View.VISIBLE);
                messageContainer.setGravity(Gravity.RIGHT);
            } else {
                left_arrow.setVisibility(View.VISIBLE);
                right_arrow.setVisibility(View.GONE);
                messageContainer.setGravity(Gravity.LEFT);
                //GradientDrawable messageBoxBackground= (GradientDrawable) (messageBox.getBackground());
                //messageBoxBackground.setColor(Color.MAGENTA);
                //GradientDrawable messageArrowBackground = (GradientDrawable) (messageArrow.getBackground());
                //messageArrowBackground.setColor(Color.MAGENTA);

            }
        }

        public void setName(String name) {
            TextView field = (TextView) mView.findViewById(R.id.name_text);
            field.setText(name);
        }

        public void setText(String text) {
            TextView field = (TextView) mView.findViewById(R.id.message_text);
            field.setText(text);
        }
    }
}
