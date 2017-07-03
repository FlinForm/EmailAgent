package com.epam.androidlab.emailagent.model;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.epam.androidlab.emailagent.R;
import com.epam.androidlab.emailagent.api.GmailApiHelper;
import com.google.api.services.gmail.model.Message;

import java.util.List;

public class MailboxRecycleViewAdapter
        extends RecyclerView.Adapter<MailboxRecycleViewAdapter.ItemViewHolder> {
    private final List<Message> messages;
    private final String EXCEPTION_TEXT = " must implement OnMailSelectedListener";
    private final String SUBJECT_TAG = "Subject";
    private final String INBOX_TAG = "INBOX";
    private final String TRASH_TAG = "TRASH";
    private final String RECEIVER = "To";
    private final String MAILER = "From";

    private View view;
    public static OnMailSelectedListener listener;

    public MailboxRecycleViewAdapter(FragmentActivity activity, List<Message> messages) {
        this.messages = messages;

        try {
            listener = (OnMailSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity + toString() + EXCEPTION_TEXT);
        }
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent
                .getContext())
                .inflate(R.layout.email_card, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.MENU_ITEM_ID = position;
        holder.mailerOrReceiver.setText(getReceiver(messages.get(position)));
        holder.subject.setText(getMessageSubject(messages.get(position)));
        holder.message = messages.get(position);
        if ("".equals(messages.get(position).getSnippet())) {
            holder.body.setText(R.string.no_content);
        } else {
            holder.body.setText(formatCardText(messages.get(position).getSnippet(), false));
        }
    }

    private String formatCardText(String text, boolean isSubject) {
        if (text == null) {
            return null;
        }
        int textLength = 35;
        if (isSubject) {
            textLength = 25;
        }
        return text.length() < textLength + 5 ? text : text.substring(0, textLength) + " ...";
    }

    private String getReceiver(Message message) {
        String receiver;
        if (isMailer(message)) {
            receiver = MAILER;
        } else {
            receiver = RECEIVER;
        }
        String result = GmailApiHelper.getMessagePart(receiver, message);
        return "".equals(result) ?
                view.getResources().getString(R.string.no_content) :
                formatCardText(result, false);
    }

    private String getMessageSubject(Message message) {
        String subject = GmailApiHelper.getMessagePart(SUBJECT_TAG, message);
        return "".equals(subject) ?
                view.getResources().getString(R.string.no_content) :
                formatCardText(subject, true);
    }

    private boolean isMailer(Message message) {
        for (String labelId : message.getLabelIds()) {
            if (INBOX_TAG.equalsIgnoreCase(labelId) || TRASH_TAG.equals(labelId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder
    implements View.OnCreateContextMenuListener, View.OnClickListener {
        private final TextView mailerOrReceiver;
        private final TextView subject;
        private final TextView body;
        private int MENU_ITEM_ID;
        private Message message;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mailerOrReceiver = (TextView) itemView.findViewById(R.id.mailer);
            subject = (TextView) itemView.findViewById(R.id.subject);
            body = (TextView) itemView.findViewById(R.id.body);

            itemView.setOnCreateContextMenuListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu,
                                        View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(0, MENU_ITEM_ID, 0, v.getResources().getString(R.string.delete_item));
        }

        @Override
        public void onClick(View v) {
            Mailbox.setMessage(message);
            listener.onLetterSelected();
        }
    }

    public interface OnMailSelectedListener {
        void onLetterSelected();
    }
}
