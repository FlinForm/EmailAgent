package com.epam.androidlab.emailagent.model;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import android.widget.TextView;

import com.epam.androidlab.emailagent.R;
import com.epam.androidlab.emailagent.api.GmailApiHelper;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This class contains methods which are used to format text in RecyclerView cards.
 */

public class AdapterUtils {
    private final String SUBJECT_TAG = "Subject";
    private final String INBOX_TAG = "INBOX";
    private final String TRASH_TAG = "TRASH";
    private final String RECEIVER = "To";
    private final String MAILER = "From";
    private Context context;

    public AdapterUtils(Context context) {
        this.context = context;
    }

    public String formatCardText(String text) {
        if (text == null) {
            return "draft";
        }
        int textLength = 32;
        return text.length() < textLength + 5 ? text : text.substring(0, textLength) + " ...";
    }

    public String getReceiver(Message message) {
        String receiver;
        if (isMailer(message)) {
            receiver = MAILER;
        } else {
            receiver = RECEIVER;
        }
        String result = GmailApiHelper.getMessagePart(receiver, message);
        return "".equals(result) ?
                context.getResources().getString(R.string.no_content) :
                formatCardText(result);
    }

    public String formatReceiverText(String text) {
        String[] result = text.split("<");
        return result[0];
    }

    public String getMessageSubject(Message message) {
        String subject = GmailApiHelper.getMessagePart(SUBJECT_TAG, message);
        return "".equals(subject) ?
                context.getResources().getString(R.string.no_content) :
                formatCardText(subject);
    }

    public boolean isMailer(Message message) {
        for (String labelId : message.getLabelIds()) {
            if (INBOX_TAG.equalsIgnoreCase(labelId) || TRASH_TAG.equals(labelId)) {
                return true;
            }
        }
        return false;
    }

    public String parseDate(String date) {
        DateFormat dateParser = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss zzzz", Locale.ENGLISH);
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        Date parsedDate = null;
        try {
            parsedDate = dateParser.parse(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateFormat.format(parsedDate);
    }

    public String getDate(Message message) {
        for (MessagePartHeader partHeader : message.getPayload().getHeaders()) {
            if ("Date".equalsIgnoreCase(partHeader.getName())) {
                return partHeader.getValue();
            }
        }
        return "";
    }

    public void setImageViewColor(String firstLetter, ImageView imageView) {

        if ("draft".equals(firstLetter)) {
            imageView.setColorFilter(ContextCompat.getColor(context,
                    R.color.colorPink));
        }

        if (firstLetter.matches("[a-e]") || firstLetter.matches("[а-е]")) {
            imageView.setColorFilter(ContextCompat.getColor(context,
                    R.color.colorDeepPurple));
            return;
        }

        if (firstLetter.matches("[f-k]") || firstLetter.matches("[ж-л]")) {
            imageView.setColorFilter(ContextCompat.getColor(context,
                    R.color.colorRed));
            return;
        }

        if (firstLetter.matches("[l-p]") || firstLetter.matches("[м-с]")) {
            imageView.setColorFilter(ContextCompat.getColor(context,
                    R.color.colorGreen));
            return;
        }

        if (firstLetter.matches("[q-u]") || firstLetter.matches("[т-ц]")) {
            imageView.setColorFilter(ContextCompat.getColor(context,
                    R.color.colorAmber));
            return;
        }

        if (firstLetter.matches("[v-z]") || firstLetter.matches("[ч-я]")) {
            imageView.setColorFilter(ContextCompat.getColor(context,
                    R.color.colorIndigo));
            return;
        }

        imageView.setColorFilter(ContextCompat.getColor(context,
                R.color.colorPink));
    }

    public void setImageViewText(String letters, TextView textView) {
        if ("\"".equals(letters.substring(0, 1))) {
            textView.setText(letters.substring(1, 2).toUpperCase());
        } else {
            textView.setText(letters.substring(0, 1).toUpperCase());
        }
    }
}
