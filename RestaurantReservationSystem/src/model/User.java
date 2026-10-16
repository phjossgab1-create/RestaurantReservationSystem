package model;

import enums.Role;

/**
 * Abstract base class for everyone who can log in.
 *
 * Abstraction:   you can never create a plain "User", only a Customer, Staff or Manager.
 * Encapsulation: fields are private and only reachable through getters.
 * Polymorphism:  getRole() and showMenu() behave differently in each subclass.
 */
public abstract class User {

    private final String userId;
    private String fullName;

    protected User(String userId, String fullName) {
        this.userId = userId;
        this.fullName = fullName;
    }

    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /** Each subclass says which role it is. */
    public abstract Role getRole();

    /** Each subclass opens its own menu after login. */
    public abstract void showMenu();

    @Override
    public String toString() {
        return getRole().getLabel() + ": " + fullName + " (" + userId + ")";
    }
}
