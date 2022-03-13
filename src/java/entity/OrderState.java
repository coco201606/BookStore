/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * 2021.7.31 注文された書籍と数量を扱うORDERSTATEテーブルに対応する
 * エンティティクラス　List型
 * @author daidou
 */

@Entity
@Table(name="ORDERSTATE")

public class OrderState implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;    //注文ごとにIDを割り振る
    @OneToOne
    private BookData bookData;  //BookDataとOneToOneで関連付ける
    private Integer quantity=0; //数量を保持する（デフォルト値は0）
    
    /***************************************************************************
     *  パラメータなしのコンストラクタ
     **************************************************************************/
    public OrderState(){}
    
        
    /***************************************************************************
     * コンストラクタ
     * @param bookData BookDataのオブジェクト
     * @param quantity  数量（Integer）
     **************************************************************************/
    public OrderState(BookData bookData,Integer quantity){
        this.bookData=bookData;
        this.quantity=quantity;
    }

        
    /***************************************************************************
     * quantity（数量を加算する）
     * @return 加算後の数量（Integer）
     **************************************************************************/
    public int adding(){
        ++quantity;
        return quantity;
    }

    /***************************************************************************
     * quantity（数量を減算する）
     * @return 減算後の数量（Integer）
     **************************************************************************/
    public int subtraction(){
        if (quantity>0) --quantity;
        return quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BookData getBookData() {
        return bookData;
    }

    public void setBookData(BookData bookData) {
        this.bookData = bookData;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
}
