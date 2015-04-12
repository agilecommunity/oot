package models;

import org.joda.time.DateTime;
import play.db.ebean.Model;

import javax.persistence.Entity;

/**
 * DBとDailyOrderStatを直接マッピングする方法が分からないため、
 * 一度このクラスにマッピングしてから、DailyOrderStatを作成する
 */
@Entity
public class DailyOrderStatForDB extends Model {

    public DateTime orderDate;
    public String menuStatus;

    public Integer allNumUsers;
    public Integer allNumOrders;
    public Integer allTotalFixedOnOrder;
    public Integer allTotalDiscountOnOrder;

    public Integer bentoNumUsers;
    public Integer bentoNumOrders;
    public Integer bentoTotalFixedOnOrder;
    public Integer bentoTotalDiscountOnOrder;

    public Integer sideNumUsers;
    public Integer sideNumOrders;
    public Integer sideTotalFixedOnOrder;
    public Integer sideTotalDiscountOnOrder;

    public DailyOrderStat createOrderStat() {
        DailyOrderStat object = new DailyOrderStat();

        object.orderDate = this.orderDate;
        object.menuStatus = this.menuStatus;

        object.allStat = new DailyOrderStatItem();
        object.bentoStat = new DailyOrderStatItem();
        object.sideStat = new DailyOrderStatItem();

        object.allStat.numUsers = this.allNumUsers;
        object.allStat.numOrders = this.allNumOrders;
        object.allStat.totalFixedOnOrder = this.allTotalFixedOnOrder;
        object.allStat.totalDiscountOnOrder = this.allTotalDiscountOnOrder;

        object.bentoStat.numUsers = this.bentoNumUsers;
        object.bentoStat.numOrders = this.bentoNumOrders;
        object.bentoStat.totalFixedOnOrder = this.bentoTotalFixedOnOrder;
        object.bentoStat.totalDiscountOnOrder = this.bentoTotalDiscountOnOrder;

        object.sideStat.numUsers = this.sideNumUsers;
        object.sideStat.numOrders = this.sideNumOrders;
        object.sideStat.totalFixedOnOrder = this.sideTotalFixedOnOrder;
        object.sideStat.totalDiscountOnOrder = this.sideTotalDiscountOnOrder;

        return object;
    }
}
