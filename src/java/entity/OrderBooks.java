/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

/**
 * 2021.7.31 ORDERBOOKテーブルに対応するエンティティクラス
 * @author daidou
 */
@Entity
@Table(name="ORDERBOOK")

@NamedQueries ({
    //指定したIDがACCOUNTテーブルのid順と一致するレコードを抽出する
    @NamedQuery(
        name = OrderBooks.ORDER_HISTORY,
        query = "select o from OrderBooks o where o.account.id=:userId ORDER BY o.po_date DESC")
})

public class OrderBooks implements Serializable {
    //クエリ名を格納した定数を定義
    public static final String ORDER_HISTORY = "OrderBooks.orderHistory";
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private UserAccount account;
    private String cust_name;
    private String cust_mail;
    private String cust_demand;
    private Integer cust_payment;
    private Integer total_amount;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date po_date;
    @OneToMany(cascade={CascadeType.ALL})
    private List<OrderState> orderState;
    
    /***************************************************************************
     *  パラメータなしのコンストラクタ
     **************************************************************************/
    public OrderBooks(){}
    
        
    /***************************************************************************
     * コンストラクタ
     * @param account       UserAccountオブジェクト
     * @param cust_name     顧客の氏名
     * @param cust_mail     顧客のメールアドレス
     * @param cust_payment  支払い方法
     * @param cust_demand   注文時の要望
     * @param total_amount  合計金額
     * @param po_date       注文日     
     * @param orderState    orderStateオブジェクトのリスト
     **************************************************************************/

    public OrderBooks(
        UserAccount account,
        String cust_name,
        String cust_mail,
        Integer cust_payment,
        String cust_demand,
        Date po_date,
        Integer total_amount,
        List<OrderState> orderState
    ){
        this.account=account;
        this.cust_name=cust_name;
        this.cust_mail=cust_mail;
        this.cust_payment=cust_payment;
        this.cust_demand=cust_demand;
        this.po_date=po_date;
        this.total_amount=total_amount;
        this.orderState=orderState;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserAccount getAccount() {
        return account;
    }

    public void setAccount(UserAccount account) {
        this.account = account;
    }

    public String getCust_name() {
        return cust_name;
    }

    public void setCust_name(String cust_name) {
        this.cust_name = cust_name;
    }

    public String getCust_mail() {
        return cust_mail;
    }

    public void setCust_mail(String cust_mail) {
        this.cust_mail = cust_mail;
    }

    public String getCust_demand() {
        return cust_demand;
    }

    public void setCust_demand(String cust_demand) {
        this.cust_demand = cust_demand;
    }

    public Integer getCust_payment() {
        return cust_payment;
    }

    public void setCust_payment(Integer cust_payment) {
        this.cust_payment = cust_payment;
    }

    public Integer getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(Integer total_amount) {
        this.total_amount = total_amount;
    }

    public Date getPo_date() {
        return po_date;
    }

    public void setPo_date(Date po_date) {
        this.po_date = po_date;
    }

    public List<OrderState> getOrderState() {
        return orderState;
    }

    public void setOrderState(List<OrderState> orderState) {
        this.orderState = orderState;
    }
    
    
    
}
