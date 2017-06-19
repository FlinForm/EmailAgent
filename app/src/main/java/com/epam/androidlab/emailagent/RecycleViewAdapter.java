package com.epam.androidlab.emailagent;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.util.List;

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ItemViewHolder> {
    private final List<Message> messages;
    private final String RECEIVER = "To";
    private final String MAILER = "From";
    private static View view;

    public RecycleViewAdapter(List<Message> messages) {
        this.messages = messages;
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
            holder.body.setText("<No content>");
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
                    result = "<No content>";
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
            if ("Subject".equals(partHeader.getName())) {
                if ("".equals(partHeader.getValue())) {
                    subject = "<No content>";
                } else {
                    subject = partHeader.getValue();
                }
            }
        }
        return subject;
    }

    private boolean isMailer(Message message) {
        for (String labelId : message.getLabelIds()) {
            if ("INBOX".equalsIgnoreCase(labelId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
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
        }
    }
}
