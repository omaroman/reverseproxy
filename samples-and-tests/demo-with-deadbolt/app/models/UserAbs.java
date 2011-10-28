package models;

import play.data.validation.Email;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

@MappedSuperclass
public abstract class UserAbs extends Model {

	/* ---RELATIONSHIPS--- */

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "role_id")
    public Role role;	

	/* ---MODEL FIELDS--- */
	
	@Required(message = "req pass")
    public String password;

	@Required(message = "username error...")
//    @UniqueValue
    public String username;
	
    @Required(message = "rep mail")
    @Email
//    @UniqueValue
    public String email;

	// DO NOT add any constructor use the default one

    @Override
    public String toString() {
        return "";
    }
}
