package models;

import com.avaje.ebean.annotation.Sql;
import play.db.ebean.Model;

import javax.persistence.*;

@Entity
@Sql
public class DailyOrderAggregate extends Model {

    public java.sql.Date orderDate;

    public Integer menuItemId;

    public String code;

    public Integer numOrders;

}
