package edu.uw.tcss450.groupchat.ui.chats;

import java.io.Serializable;

/**
 * The ChatRoom object represents a chatroom that the user has access to.
 *
 * @version January, 2021
 */
public class ChatRoom implements Serializable, Comparable<ChatRoom> {

    private int mId;

    private String mName;

    private String mImageUrl;

    /**
     * Initialize the object.
     *
     * @param id chatroom id as an integer
     * @param name chatroom name
     * @param url chatroom image url
     */
    public ChatRoom(final int id, final String name, final String url) {
        mId = id;
        mName = name;
        mImageUrl = url;
    }

    /**
     * Return the id of chatroom.
     *
     * @return id of chatroom as an integer
     */
    public int getId() {
        return mId;
    }

    /**
     * Return the name of the chatroom.
     *
     * @return chatroom name string
     */
    public String getName() {
        return mName;
    }

    /**
     * Return the image url of the chatroom.
     *
     * @return chatroom image url
     */
    public String getImageUrl() {
        return mImageUrl;
    }

    /**
     * Set the image url of the chatroom.
     *
     * @param url chatroom image url
     */
    public void setImageUrl(final String url) {
        mImageUrl = url;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ChatRoom)) return false;

        ChatRoom o = (ChatRoom) other;
        if (mId != o.getId()) return false;
        return mName.equals(o.getName());
    }

    @Override
    public int hashCode() {
        return mId + mName.hashCode();
    }

    @Override
    public int compareTo(ChatRoom other) {
        return Integer.compare(mId, other.getId());
    }
}
