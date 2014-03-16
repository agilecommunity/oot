package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
public class LocalToken extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    public String uuid;

    public String email;

    public Date created_at;

    public Date expire_at;

    public boolean is_sign_up;

    /**
     * Generic query helper for entity with id Long
     */
    public static Finder<String,LocalToken> find = new Finder<String,LocalToken>(String.class, LocalToken.class);

}
