package edu.uw.tcss450.groupchat.ui.chats;

import java.io.Serializable;

/**
 * The ChatRoom object represents a chatroom that the user has access to.
 *
 * @version January, 2021
 */
public class ChatRoom implements Serializable, Comparable<ChatRoom> {

    private final int mId;

    private final String mImageUrl;

    private String mName;

    private boolean mAdmin;

    private int mType;

    /**
     * Initialize the object. Sets type to '1' automatically.
     *
     * @param id chatroom id as an integer
     * @param name chatroom name
     * @param url chatroom image url
     */
    public ChatRoom(final int id, final String name, final String url, final boolean admin) {
        mId = id;
        mName = name;
        mImageUrl = url;
        mAdmin = admin;
        mType = 1;
    }

    /**
     * Initialize the object.
     *
     * @param id chatroom id as an integer
     * @param name chatroom name
     * @param url chatroom image url
     * @param type chatroom type
     */
    public ChatRoom(final int id, final String name, final String url, final boolean admin, final int type) {
        mId = id;
        mName = name;
        mImageUrl = url;
        mAdmin = admin;
        mType = type;
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
     * Return the admin status for the chatroom.
     *
     * @return admin status of user
     */
    public boolean getAdmin() {
        return mAdmin;
    }

    /**
     * Return the type of the chatroom.
     *
     * @return chatroom type
     */
    public int getType() {
        return mType;
    }

    /**
     * Set the name of the chatroom.
     *
     * @param name chatroom name
     */
    public void setName(final String name) {
        mName = name;
    }

    /**
     * Set the type of the chatroom.
     *
     * @param type chatroom type
     */
    public void setType(final int type) {
        mType = type;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ChatRoom)) return false;

        ChatRoom o = (ChatRoom) other;
        return mId == o.getId();
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
