package com.go4champ.go4champ.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class Friend {
    @Id
    private int friendID;
    @OneToOne
    //eig brauchen wir ja nur seine Id aber ich denke one to one erlaubt nur zwischen Obejcte zu verbinden
    private User sender;
    @OneToOne // muss man umbedingt verstehen wie die hier alle Funktionoren
    private User receiver;
    private boolean status;

    public Friend() {}

    public Friend(int friendID, User sender, User receiver, boolean status) {
        this.friendID = friendID;
        this.sender = sender;
        this.receiver = receiver;
        this.status = status;
    }

    public int getFriendID() {
        return friendID;
    }

    public void setFriendID(int friendID) {
        this.friendID = friendID;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }
}