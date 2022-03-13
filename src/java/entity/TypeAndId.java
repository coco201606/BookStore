/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import javax.persistence.*;

/**
 * 2021.7.31 複合キークラス
 * @author daidou
 */
@Entity
@Table(name="USERTYPE")
public class TypeAndId implements Serializable{
    private static final long serialVersionUID = 1L;
    //CompoiteTypeIdの複合キー（user_type,user_idフィールド）を主キーとして設定
    @EmbeddedId
    private CompositeTypeId comp_type_id;
    //UserAccountクラスとマッピングする
    @OneToOne(mappedBy="type_id")
    private UserAccount user_account;
    
    /***************************************************************************
     *  パラメータなしのコンストラクタ
     **************************************************************************/
    public TypeAndId(){}

    /***************************************************************************
     * コンストラクタ
     * @param type          ユーザーのタイプを示す文字列
     * @param user_account   UserAccountオブジェクト
     **************************************************************************/
    public TypeAndId(String type,UserAccount user_account){
        this.comp_type_id = new CompositeTypeId(type, user_account.getId());
        this.user_account =user_account;
    }

    /***************************************************************************
     * コンストラクタ
     * @param comp_type_id   CompositTypeIdオブジェクト
     * @param user_account   UserAccountオブジェクト
     **************************************************************************/
    public TypeAndId(CompositeTypeId comp_type_id,UserAccount user_account){
        this.comp_type_id = comp_type_id;
        this.user_account =user_account;
    }

    public CompositeTypeId getComp_type_id() {
        return comp_type_id;
    }

    public void setComp_type_id(CompositeTypeId comp_type_id) {
        this.comp_type_id = comp_type_id;
    }

    public UserAccount getUser_account() {
        return user_account;
    }

    public void setUser_account(UserAccount user_account) {
        this.user_account = user_account;
    }
   
}
