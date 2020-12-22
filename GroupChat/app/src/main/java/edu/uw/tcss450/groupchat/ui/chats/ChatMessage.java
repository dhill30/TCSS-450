package edu.uw.tcss450.groupchat.ui.chats;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * The ChatMessage class represents a single chat in a room.
 *
 * @version November 19, 2020
 */
public final class ChatMessage implements Serializable, Comparable<ChatMessage> {

    private final int mMessageId;
    private final String mMessage;
    private final String mSender;
    private final String mTimeStamp;

    /**
     * Constructor initialize the object's fields.
     *
     * @param messageId message id as an integer
     * @param message the message content as a string
     * @param sender the sender
     * @param timeStamp the time message was sent
     */
    public ChatMessage(int messageId, String message, String sender, String timeStamp) {
        mMessageId = messageId;
        mMessage = message;
        mSender = sender;
        mTimeStamp = timeStamp;
    }

    /**
     * Static factory method to turn a properly formatted JSON String into a
     * ChatMessage object.
     * @param cmAsJson the String to be parsed into a ChatMessage Object.
     * @return a ChatMessage Object with the details contained in the JSON String.
     * @throws JSONException when cmAsString cannot be parsed into a ChatMessage.
     */
    public static ChatMessage createFromJsonString(final String cmAsJson) throws JSONException {
        final JSONObject msg = new JSONObject(cmAsJson);
        return new ChatMessage(msg.getInt("messageid"),
                msg.getString("message"),
                msg.getString("username"),
                msg.getString("timestamp"));
    }

    /**
     * Return the message content.
     *
     * @return message content the object is holding as a string
     */
    public String getMessage() {
        return mMessage;
    }

    /**
     * Return the sender of this message
     *
     * @return sender string
     */
    public String getSender() {
        return mSender;
    }

    /**
     * Return the timestamp when the message was sent
     *
     * @return timestamp as a string
     */
    public String getTimeStamp() {
        return mTimeStamp;
    }

    /**
     * Return the message id.
     *
     * @return id as an integer
     */
    public int getMessageId() {
        return mMessageId;
    }

    /**
     * Check if message is image.
     *
     * @return true if message is only image, false otherwise
     */
    public static boolean isImage(String message) {
        return (message.endsWith(".gif") || message.endsWith(".png")
                || message.endsWith(".jpg") || message.endsWith(".jpeg"))
                & message.trim().split(" ").length < 2;
    }

    /**
     * Provides equality solely based on MessageId.
     * @param other the other object to check for equality
     * @return true if other message ID matches this message ID, false otherwise
     */
    @Override
    public boolean equals(@Nullable Object other) {
        boolean result = false;
        if (other instanceof ChatMessage) {
            result = mMessageId == ((ChatMessage) other).mMessageId;
        }
        return result;
    }

    @Override
    public int compareTo(ChatMessage other) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        Date d1 = null, d2 = null;
        try {
            d1 = sdf.parse(mTimeStamp);
            d2 = sdf.parse(other.getTimeStamp());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d2.compareTo(d1);
    }
}
