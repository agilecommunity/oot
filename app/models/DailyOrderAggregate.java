package models;

import com.avaje.ebean.annotation.Sql;
import play.db.ebean.Model;

import javax.persistence.*;

@Entity
@Sql
public class DailyOrderAggregate extends Model {

    public java.sql.Date order_date;

    public Integer menu_item_id;

    public String code;

    public Integer num_orders;

}
