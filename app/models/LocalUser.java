package models;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.joda.time.DateTime;
import play.db.ebean.Model;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class LocalUser extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    public String id;

    public String provider;

    public String firstName;

    public String lastName;

    public String email;

    public Boolean isAdmin;

    @JsonIgnore
    public String password;

    @JsonIgnore
    @CreatedTimestamp
    public Date createdAt;

    @JsonIgnore
    @UpdatedTimestamp
    public Date updatedAt;

    /**
     * Generic query helper for entity with id Long
     */
    public static Finder<String,LocalUser> find = new Finder<String,LocalUser>(String.class, LocalUser.class);

}
