package com.epam.androidlab.emailagent.model;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.epam.androidlab.emailagent.R;
import com.epam.androidlab.emailagent.api.GmailApiRequests;
import com.epam.androidlab.emailagent.api.RequestHandler;
import com.epam.androidlab.emailagent.api.RequestType;
import com.google.api.services.gmail.model.Message;

import java.util.List;

public class MailboxRecycleViewAdapter
        extends RecyclerView.Adapter<MailboxRecycleViewAdapter.ItemViewHolder> {
    private final List<Message> messages;
    private final String EXCEPTION_TEXT = " must implement OnMailSelectedListener";
    private final int ITEM = 0;
    private final int PROGRESS = 1;

    private AdapterUtils helper;
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
        if (viewType == PROGRESS) {
            view = LayoutInflater.from(parent
                    .getContext())
                    .inflate(R.layout.item_loading, parent, false);
            helper = new AdapterUtils(view.getContext());
        } else {
            view = LayoutInflater.from(parent
                    .getContext())
                    .inflate(R.layout.material_card, parent, false);
            helper = new AdapterUtils(view.getContext());
        }
        return new ItemViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position) == null ? PROGRESS : ITEM;
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        if (messages.get(position) != null) {
            Message message = messages.get(position);
            holder.MENU_ITEM_ID = position;
            holder.mailerOrReceiver
                    .setText(helper.formatReceiverText(helper.getReceiver(message)));
            holder.subject
                    .setText(helper.getMessageSubject(message));
            holder.message = message;
            if ("".equals(message.getSnippet())) {
                holder.body.setText(R.string.no_content);
            } else {
                holder.body.setText(helper.formatCardText(message.getSnippet()));
            }
            holder.date.setText(helper.parseDate(helper.getDate(message)));
            helper.setImageViewColor(helper.getReceiver(message).substring(0, 1).toLowerCase(),
                    holder.imageView);
            helper.setImageViewText(helper.getReceiver(message).substring(0, 2),
                    holder.imageViewText);
        }
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
        private final TextView date;
        private final ImageView imageView;
        private final TextView imageViewText;
        private int MENU_ITEM_ID;
        private Message message;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mailerOrReceiver = (TextView) itemView.findViewById(R.id.mailer);
            subject = (TextView) itemView.findViewById(R.id.subject);
            body = (TextView) itemView.findViewById(R.id.body);
            date = (TextView) itemView.findViewById(R.id.date);
            imageView = (ImageView) itemView.findViewById(R.id.cardImageView);
            imageViewText = (TextView) itemView.findViewById(R.id.imageViewText);

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
