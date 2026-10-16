package model;

import enums.Role;
import ui.ManagerMenu;

/** Managers oversee all records and approve cancellations. */
public class Manager extends Employee {

    public Manager(String username, String password, String fullName) {
        super(username, password, fullName);
    }

    @Override
    public Role getRole() {
        return Role.MANAGER;
    }

    @Override
    public void showMenu() {
        new ManagerMenu(this).run();
    }
}
