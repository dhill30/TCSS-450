package edu.uw.tcss450.groupchat.services;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import edu.uw.tcss450.groupchat.AuthActivity;
import edu.uw.tcss450.groupchat.R;
import edu.uw.tcss450.groupchat.ui.chats.ChatMessage;
import me.pushy.sdk.Pushy;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;

/**
 * Pushy Receiver class handles chat's notifications.
 *
 * @version November 19 2020.
 */
public class PushReceiver extends BroadcastReceiver {

    public static final String RECEIVED_NEW_MESSAGE = "new message from pushy";

    private static final String CHANNEL_ID = "1";

    @Override
    public void onReceive(Context context, Intent intent) {

        //the following variables are used to store the information sent from Pushy
        //In the WS, you define what gets sent. You can change it there to suit your needs
        //Then here on the Android side, decide what to do with the message you got
        String typeOfMessage = intent.getStringExtra("type");

        switch (typeOfMessage) {
            case "msg": {
                ChatMessage message;
                int chatId;
                try {
                    message = ChatMessage.createFromJsonString(intent.getStringExtra("message"));
                    chatId = intent.getIntExtra("chatid", -1);
                } catch (JSONException e) {
                    //Web service sent us something unexpected...I can't deal with this.
                    throw new IllegalStateException("Error from Web Service. Contact Dev Support");
                }

                ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
                ActivityManager.getMyMemoryState(appProcessInfo);

                if (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE) {
                    //app is in the foreground so send the message to the active Activities
                    Log.d("PUSHY", "Message received in foreground: " + message);

                    //create an Intent to broadcast a message to other parts of the app.
                    Intent i = new Intent(RECEIVED_NEW_MESSAGE);
                    i.putExtra("chatMessage", message);
                    i.putExtra("chatid", chatId);
                    i.putExtras(intent.getExtras());

                    context.sendBroadcast(i);
                } else {
                    //app is in the background so create and post a notification
                    Log.d("PUSHY", "Message received in background: " + message.getMessage());

                    Intent i = new Intent(context, AuthActivity.class);
                    i.putExtras(intent.getExtras());

                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                            i, PendingIntent.FLAG_UPDATE_CURRENT);

                    //research more on notifications the how to display them
                    //https://developer.android.com/guide/topics/ui/notifiers/notifications
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                            .setAutoCancel(true)
                            .setSmallIcon(R.drawable.ic_chat_notification)
                            .setContentTitle("Message from: " + message.getSender())
                            .setContentText(message.getMessage())
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setContentIntent(pendingIntent);

                    // Automatically configure a ChatMessageNotification Channel for devices running Android O+
                    Pushy.setNotificationChannel(builder, context);

                    // Get an instance of the NotificationManager service
                    NotificationManager notificationManager =
                            (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

                    // Build the notification and display it
                    notificationManager.notify(1, builder.build());
                }
                break;
            }
            case "con": {
                String text;
                String email;
                try {
                    JSONObject message = new JSONObject(intent.getStringExtra("message"));
                    text = message.getString("text");
                    email = message.getString("email");
                } catch (JSONException e) {
                    //Web service sent us something unexpected...I can't deal with this.
                    throw new IllegalStateException("Error from Web Service. Contact Dev Support");
                }

                ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
                ActivityManager.getMyMemoryState(appProcessInfo);

                if (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE) {
                    Log.d("PUSHY", "Contact received in foreground: " + text);

                    Intent i = new Intent(RECEIVED_NEW_MESSAGE);
                    i.putExtra("con", text);
                    i.putExtra("request", intent.getStringExtra("request"));
                    if (intent.hasExtra("contact")) {
                        i.putExtra("contact", intent.getStringExtra("contact"));
                    }

                    context.sendBroadcast(i);
                } else {
                    Log.d("PUSHY", "Contact received in background: " + text);

                    Intent i = new Intent(context, AuthActivity.class);
                    i.putExtras(intent.getExtras());

                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                            i, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                            .setAutoCancel(true)
                            .setSmallIcon(R.drawable.ic_contact_notification)
                            .setContentTitle(email)
                            .setContentText(text)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setContentIntent(pendingIntent);

                    Pushy.setNotificationChannel(builder, context);

                    NotificationManager notificationManager =
                            (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

                    notificationManager.notify(1, builder.build());
                }
                break;
            }
            case "chat": {
                String text;
                String email;
                try {
                    JSONObject message = new JSONObject(intent.getStringExtra("message"));
                    text = message.getString("text");
                    email = message.getString("email");
                } catch (JSONException e) {
                    //Web service sent us something unexpected...I can't deal with this.
                    throw new IllegalStateException("Error from Web Service. Contact Dev Support");
                }

                ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
                ActivityManager.getMyMemoryState(appProcessInfo);

                if (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE) {
                    Log.d("PUSHY", "Chat received in foreground: " + text);

                    Intent i = new Intent(RECEIVED_NEW_MESSAGE);
                    i.putExtra("chat", text);
                    i.putExtras(intent.getExtras());

                    context.sendBroadcast(i);
                } else {
                    Log.d("PUSHY", "Chat received in background: " + text);

                    Intent i = new Intent(context, AuthActivity.class);
                    i.putExtras(intent.getExtras());

                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                            i, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                            .setAutoCancel(true)
                            .setSmallIcon(R.drawable.ic_chat_notification)
                            .setContentTitle(email)
                            .setContentText(text)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setContentIntent(pendingIntent);

                    Pushy.setNotificationChannel(builder, context);

                    NotificationManager notificationManager =
                            (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

                    notificationManager.notify(1, builder.build());
                }
                break;
            }
            case "typeStatus": {
                int chatId;
                String username;
                String email;
                String status;
                try {
                    JSONObject message = new JSONObject(intent.getStringExtra("message"));
                    chatId = message.getInt("chatid");
                    email = message.getString("email");
                    username = message.getString("user");
                    status = message.getString("status");
                } catch (JSONException e) {
                    //Web service sent us something unexpected...I can't deal with this.
                    throw new IllegalStateException("Error from Web Service. Contact Dev Support");
                }

                ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
                ActivityManager.getMyMemoryState(appProcessInfo);

                if (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE) {

                    Intent i = new Intent(RECEIVED_NEW_MESSAGE);
                    i.putExtra("chatid", chatId);
                    i.putExtra("email", email);
                    i.putExtra("username", username);
                    i.putExtra("typeStatus", status);
                    i.putExtras(intent.getExtras());

                    context.sendBroadcast(i);
                }
                break;
            }
        }
    }
}
