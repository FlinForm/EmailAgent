package com.epam.androidlab.emailagent.model;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.epam.androidlab.emailagent.R;
import com.epam.androidlab.emailagent.activities.MainActivity;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.util.List;

public class MailboxRecycleViewAdapter extends RecyclerView.Adapter<MailboxRecycleViewAdapter.ItemViewHolder> {
    private FragmentActivity activity;
    private final List<Message> messages;
    private final String SUBJECT_TAG = "Subject";
    private final String INBOX_TAG = "INBOX";
    private final String RECEIVER = "To";
    private final String MAILER = "From";
    private View view;
    public static OnMailSelectedListener listener;

    public MailboxRecycleViewAdapter(FragmentActivity activity, List<Message> messages) {
        this.activity = activity;
        this.messages = messages;

        try {
            listener = (OnMailSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity + toString() +
                    " must implement OnMailSelectedListener");
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
        holder.mailerOrReceiver.setText(getMailerOrReceiver(messages.get(position)));
        holder.subject.setText(getMessageSubject(messages.get(position)));
        if ("".equals(messages.get(position).getSnippet())) {
            holder.body.setText(R.string.no_content);
        } else {
            holder.body.setText(messages.get(position).getSnippet());
        }
    }

    private String getMailerOrReceiver(Message message) {
        String mailerOrReceiver;
        String result = "";
        if (isMailer(message)) {
            mailerOrReceiver = MAILER;
        } else {
            mailerOrReceiver = RECEIVER;
        }
        for (MessagePartHeader partHeader : message.getPayload().getHeaders()) {
            if (mailerOrReceiver.equals(partHeader.getName())) {
                if ("".equals(partHeader.getValue())) {
                    result = view.getResources().getString(R.string.no_content);
                } else {
                    result = partHeader.getValue();
                }
            }
        }
        return result;
    }

    private String getMessageSubject(Message message) {
        String subject = "";
        for (MessagePartHeader partHeader : message.getPayload().getHeaders()) {
            if (SUBJECT_TAG.equals(partHeader.getName())) {
                if ("".equals(partHeader.getValue())) {
                    subject = view.getResources().getString(R.string.no_content);
                } else {
                    subject = partHeader.getValue();
                }
            }
        }
        return subject;
    }

    private boolean isMailer(Message message) {
        for (String labelId : message.getLabelIds()) {
            if (INBOX_TAG.equalsIgnoreCase(labelId)) {
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
        private final int MENU_ITEM_DELETE = 1;
        private final TextView mailerOrReceiver;
        private final TextView subject;
        private final TextView body;
        private final TextView newLetter;
        private Message message;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mailerOrReceiver = (TextView) itemView.findViewById(R.id.mailer);
            subject = (TextView) itemView.findViewById(R.id.subject);
            body = (TextView) itemView.findViewById(R.id.body);
            newLetter = (TextView) itemView.findViewById(R.id.newLetter);

            itemView.setOnCreateContextMenuListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu,
                                        View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(0, MENU_ITEM_DELETE, 0, v.getResources().getString(R.string.delete_item));
        }

        @Override
        public void onClick(View v) {
            listener.onMailSelected();
        }
    }
    public interface OnMailSelectedListener {
        void onMailSelected();
    }
}
