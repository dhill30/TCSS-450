package edu.uw.tcss450.groupchat.ui.contacts;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Object for storing user contact information.
 * @author Dylan Hill
 * @version November 2020
 */
public class Contact implements Comparable<Contact> {

    private int mId;

    private String mUsername;

    private String mName;

    private String mEmail;

    private String mImage;

    private int mType;

    public Contact() {
        // do nothing
    }

    /**
     * Contact class constructor, initializes the contact with the passed arguments.
     * @param id the contact id of the contact
     * @param username the username of the contact
     * @param name the name (first and last) of the contact
     * @param email the email of the contact
     * @param image the url of the profile image
     * @param type the integer representing the type of contact
     */
    public Contact(final int id,
                   final String username,
                   final String name,
                   final String email,
                   final String image,
                   final int type) {
        mId = id;
        mUsername = username;
        mName = name;
        mEmail = email;
        mImage = image;
        mType = type;
    }

    /**
     * Static factory method to turn a properly formatted JSON String into a Contact object.
     * @param conAsJson the String to be parsed into a Contact Object.
     * @return a Contact Object with the details contained in the JSON String.
     * @throws JSONException when conAsString cannot be parsed into a Contact.
     */
    public static Contact createFromJsonString(final String conAsJson) throws JSONException {
        final JSONObject msg = new JSONObject(conAsJson);
        return new Contact(msg.getInt("contactid"),
                msg.getString("username"),
                msg.getString("name"),
                msg.getString("email"),
                msg.getString("image"),
                0);
    }

    /**
     * Returns the contact's id.
      * @return the contact id
     */
    public int getId() {
        return mId;
    }

    /**
     * Returns the contact's username.
     * @return the contact username
     */
    public String getUsername() {
        return mUsername;
    }

    /**
     * Returns the contact's first and last name.
     * @return the contact first and last name
     */
    public String getName() {
        return mName;
    }

    /**
     * Returns the contact's email.
     * @return the contact email
     */
    public String getEmail() {
        return mEmail;
    }

    /**
     * Returns the contact's profile image.
     * @return the contact profile image
     */
    public String getImage() {
        return mImage;
    }

    /**
     * Returns the contact's type.
     * @return the type of contact
     */
    public int getType() {
        return mType;
    }

    /**
     * Changes the contact's username
     * @param username the new username
     */
    public void setUsername(final String username) {
        mUsername = username;
    }

    /**
     * Changes the contact's name
     * @param name the new name
     */
    public void setName(final String name) {
        mName = name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Contact)) return false;
        return (((Contact) o).getId() == mId);
    }

    @Override
    public int hashCode() {
        return mId + mUsername.hashCode() + mName.hashCode() + mEmail.hashCode() + mType;
    }

    @Override
    public int compareTo(Contact other) {
        if (mType == other.getType()) return mUsername.compareToIgnoreCase(other.getUsername());
        return Integer.compare(mType, other.getType());
    }
}
