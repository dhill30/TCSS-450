package edu.uw.tcss450.groupchat.ui.chats;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.resources.TextAppearance;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import edu.uw.tcss450.groupchat.R;


/**
 * @author Sukhraj Kaur (Sofia)
 * The class describes how each Messages should look in a chat room and mange the list
 * of messages.
 *
 * @version November 27, 2020
 */
public class ChatMessageRecyclerViewAdapter extends RecyclerView.Adapter {

    private final List<ChatMessage> mMessages;

    private final String mUsername;

    private Context mContext;

    /**
     * Constructor to initialize fields.
     *
     * @param messages the List of messages in the chat room
     * @param username username of the current user
     */
    public ChatMessageRecyclerViewAdapter(List<ChatMessage> messages, String username, Context context) {
        this.mMessages = messages;
        mUsername = username;
        mContext = context;
    }

    @Override
    public int getItemViewType(int position) {
        if(mMessages.get(position).getSender().equals(mUsername)) {
            //this is message from user (sent)
            return 0;
        }
        return 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view;

        if (viewType == 0) {
            view = layoutInflater.inflate(R.layout.fragment_chat_sent, parent, false);
            return new ViewHolderSent(view);
        } else {
            view = layoutInflater.inflate(R.layout.fragment_chat_received, parent, false);
            return new ViewHolderReceived(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String timeStamp = getLocalTime(mMessages.get(position).getTimeStamp());

        if(mMessages.get(position).getSender().equals(mUsername)) {
            //sent
            ViewHolderSent viewHolderSent = (ViewHolderSent) holder;

            viewHolderSent.sentImage.setImageDrawable(null);
            String msg = mMessages.get(position).getMessage().trim();
            // if message is an image
            // this method could use some refactoring
            if (ChatMessage.isImage(msg)) {
                viewHolderSent.sentMessage.setVisibility(View.GONE);
                // get the size
                final int[] width = {-1};
                final int[] height = {-1};
                Glide.with(viewHolderSent.sentImage.getContext())
                        .asBitmap()
                        .load(msg)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap bitmap,
                                                        Transition<? super Bitmap> transition) {
                                width[0] = bitmap.getWidth();
                                height[0] = bitmap.getHeight();
                            }
                        });
                int max = 400;
                double ratio = width[0] > height[0] ? width[0] * 1.0 / max : height[0] * 1.0 / max;
                width[0] = (int) (width[0] / ratio);
                height[0] = (int) (height[0] / ratio);

                // display
                if (msg.endsWith("gif"))
                    Glide.with(viewHolderSent.sentImage.getContext()).asGif().load(msg)
                            .apply(new RequestOptions()
                                    .override(width[0]*5/3, height[0]*5/3))
                            .placeholder(R.drawable.ic_image_placeholder)
                            .into(viewHolderSent.sentImage);
                else
                    Glide.with(viewHolderSent.sentImage.getContext()).load(msg)
                            .apply(new RequestOptions().override(width[0], height[0]))
                            .placeholder(R.drawable.ic_image_placeholder)
                            .into(viewHolderSent.sentImage);

                viewHolderSent.sentMessage.setClickable(true);
                viewHolderSent.sentMessage.setMovementMethod(LinkMovementMethod.getInstance());
                viewHolderSent.sentMessage.setText(Html.fromHtml(
                        "<a href=" + msg + ">" + msg + "<a/>", Html.FROM_HTML_MODE_COMPACT));
                viewHolderSent.sentMessage.setTextSize(10f);
                viewHolderSent.sentImage.setOnClickListener(click -> {
                    if (viewHolderSent.sentMessage.getVisibility() == View.VISIBLE) {
                        viewHolderSent.sentMessage.setVisibility(View.GONE);
                    } else {
                        viewHolderSent.sentMessage.setVisibility(View.VISIBLE);
                    }
                });
            } else {
                viewHolderSent.sentMessage.setTextSize(14f);
                viewHolderSent.sentMessage.setText(msg);
                viewHolderSent.sentMessage.setVisibility(View.VISIBLE);
            }
            viewHolderSent.sentTime.setText(timeStamp);
        } else {
            //received
            ViewHolderReceived viewHolderReceived = (ViewHolderReceived) holder;

            viewHolderReceived.receivedImage.setImageDrawable(null);
            String msg = mMessages.get(position).getMessage().trim();
            // if message is an image
            if (ChatMessage.isImage(msg)) {
                viewHolderReceived.receivedMessage.setVisibility(View.GONE);
                setLeftToRightConstraint(viewHolderReceived.itemView,
                        R.id.text_message_time, R.id.image_message_box);

                // get the size
                final int[] width = {-1};
                final int[] height = {-1};
                Glide.with(viewHolderReceived.receivedImage.getContext())
                        .asBitmap()
                        .load(msg)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap bitmap,
                                                        Transition<? super Bitmap> transition) {
                                width[0] = bitmap.getWidth();
                                height[0] = bitmap.getHeight();
                            }
                        });
                int max = 400;
                double ratio = width[0] > height[0] ? width[0] * 1.0 / max : height[0] * 1.0 / max;
                width[0] = (int) (width[0] / ratio);
                height[0] = (int) (height[0] / ratio);

                // display
                if (msg.endsWith("gif"))
                    Glide.with(viewHolderReceived.receivedMessage.getContext()).asGif().load(msg)
                            .apply(new RequestOptions()
                                    .override(width[0]*5/3, height[0]*5/3))
                            .placeholder(R.drawable.ic_image_placeholder)
                            .into(viewHolderReceived.receivedImage);
                else
                    Glide.with(viewHolderReceived.receivedMessage.getContext()).load(msg)
                            .apply(new RequestOptions().override(width[0], height[0]))
                            .placeholder(R.drawable.ic_image_placeholder)
                            .into(viewHolderReceived.receivedImage);

                viewHolderReceived.receivedMessage.setClickable(true);
                viewHolderReceived.receivedMessage.setMovementMethod(LinkMovementMethod.getInstance());
                viewHolderReceived.receivedMessage.setText(Html.fromHtml(
                        "<a href=" + msg + ">" + msg + "<a/>", Html.FROM_HTML_MODE_COMPACT));
                viewHolderReceived.receivedMessage.setTextSize(10f);
                viewHolderReceived.receivedImage.setOnClickListener(click -> {
                    if (viewHolderReceived.receivedMessage.getVisibility() == View.VISIBLE) {
                        viewHolderReceived.receivedMessage.setVisibility(View.GONE);
                    } else {
                        viewHolderReceived.receivedMessage.setVisibility(View.VISIBLE);
                    }
                });
            } else {
                viewHolderReceived.receivedMessage.setTextSize(14f);
                viewHolderReceived.receivedMessage.setText(msg);
                setLeftToRightConstraint(viewHolderReceived.itemView,
                        R.id.text_message_time, R.id.text_message_body);
                viewHolderReceived.receivedMessage.setVisibility(View.VISIBLE);
            }
            viewHolderReceived.senderName.setText(mMessages.get(position).getSender());
            viewHolderReceived.receivedTime.setText(timeStamp);
        }
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    private String getLocalTime(final String timeStamp) {
        SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        SimpleDateFormat other = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
        SimpleDateFormat today = new SimpleDateFormat("h:mm a", Locale.getDefault());
        in.setTimeZone(TimeZone.getTimeZone("UTC"));
        other.setTimeZone(TimeZone.getDefault());
        today.setTimeZone(TimeZone.getDefault());

        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();

        String time = "";
        try {
            Date temp = in.parse(timeStamp);
            in.setTimeZone(TimeZone.getDefault());
            Date date = in.parse(in.format(temp));
            if (now.getTime() - date.getTime() > (24 * 60 * 60 * 1000)) {
                time = other.format(date);
            } else {
                time = today.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }

    private void setLeftToRightConstraint(View view, int startElement, int endElement) {
        ConstraintLayout constraintLayout = (ConstraintLayout) view;
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(startElement, ConstraintSet.LEFT, endElement, ConstraintSet.RIGHT,0);
        constraintSet.applyTo(constraintLayout);
    }

    /**
     * ViewHolder for received messages only
     */
    class ViewHolderReceived extends RecyclerView.ViewHolder {

        TextView senderName, receivedMessage, receivedTime;
        ImageView receivedImage;

        /**
         * Constructor for received message view holder
         * @param itemView
         */
        public ViewHolderReceived(@NonNull View itemView) {
            super(itemView);

            senderName = itemView.findViewById(R.id.text_message_name);
            receivedMessage = itemView.findViewById(R.id.text_message_body);
            receivedImage = itemView.findViewById(R.id.image_message_box);
            receivedTime = itemView.findViewById(R.id.text_message_time);
        }
    }

    /**
     * View Holder sent messages
     */
    class ViewHolderSent extends RecyclerView.ViewHolder {

        TextView sentMessage, sentTime;
        ImageView sentImage;

        /**
         * Constructor for sent messages view holder
         * @param itemView
         */
        public ViewHolderSent(@NonNull View itemView) {
            super(itemView);

            sentMessage = itemView.findViewById(R.id.text_message_body);
            sentImage = itemView.findViewById(R.id.image_message_box);
            sentTime = itemView.findViewById(R.id.text_message_time);
        }
    }
}
