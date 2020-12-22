package edu.uw.tcss450.groupchat.ui.contacts;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Object for storing user contact information.
 * @author Dylan Hill
 * @version November 2020
 */
public class Contact implements Comparable<Contact> {

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
     * @param username the username of the contact
     * @param name the name (first and last) of the contact
     * @param email the email of the contact
     */
    public Contact(final String username, final String name, final String email, final String image, final int type) {
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
        return new Contact(msg.getString("username"),
                msg.getString("name"),
                msg.getString("email"),
                msg.getString("image"),
                0);
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

    public void setUsername(final String username) {
        mUsername = username;
    }

    public void setName(final String name) {
        mName = name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Contact)) return false;
        if (mUsername.equals(((Contact) o).getName())) return true;
        if (mName.equals(((Contact) o).getUsername())) return true;
        if (!mUsername.equals(((Contact) o).getUsername())) return false;
        if (!mEmail.equals(((Contact) o).getEmail())) return false;
        return mName.equals(((Contact) o).getName());
    }

    @Override
    public int hashCode() {
        return mUsername.hashCode() + mName.hashCode() + mEmail.hashCode() + mType;
    }

    @Override
    public int compareTo(Contact other) {
        if (mType == other.getType()) return mUsername.compareToIgnoreCase(other.getUsername());
        return Integer.compare(mType, other.getType());
    }
}
