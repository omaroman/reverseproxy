package models;

import play.data.validation.*;
import play.db.jpa.Model;

import javax.persistence.*;

/**
 * Author: Omar
 * Date: 17/05/11
 * Time: 06:42 PM
 */

@MappedSuperclass
public abstract class RoleAbs extends Model {

    /* ---RELATIONSHIPS--- */

    // Inverse relationship, requires Associations module
	@OneToOne(mappedBy = "role", cascade = CascadeType.ALL)
    public User user;	// has_one :user

	/* ---MODEL FIELDS--- */

    @Required
	public String name;

    @Required
    public String description = "";

    // ++++++++++++++++++++++++++++++ //

	// DO NOT add any constructor use the default one

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Role) {
            Role tmpRole = (Role) obj;
            return name.equals(tmpRole.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
