/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import enterprise.UserAccountFacade;
import enterprise.FetchUserAccount;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import entity.*;
import utility.*;

/**
 * 2021.8.15 ユーザーアカウント情報を登録するバッキングビーン
 * @author daidou
 */
@RequestScoped
@Named

public class RegisterBean extends BaseBean implements Serializable{
    @EJB
    UserAccountFacade entity;
    @EJB
    FetchUserAccount fetchUserAccount;  //アカウント情報に関するクエリを実行するEJB
    
    private String id;                  //ユーザーID
    private String passwd;              //パスワード
    private String name;                //氏名
    private String mail;                //メールアドレス
    private String type_id ="user";     //ユーザーのタイプはuserとする

    /***************************************************************************
     * ユーザー登録画面で入力された値をデータベースに登録する
     * @return cart.xhtmlにリダイレクト
     **************************************************************************/
    public String registerAccount(){
        if (0!=countAccount()){
            setMessage("指定したユーザーはすでに使用されています。");
            return null;
        }else{
            //複合キークラスをインスタンス化
            CompositeTypeId key = new CompositeTypeId(this.type_id,this.id);
            //エンティティTypeAndIdをインスタンス化
            TypeAndId type = new TypeAndId(key,null);
            //UserAccountクラスをインスタンス化
            //パスワードはSHA256で暗号化
            UserAccount account = new UserAccount(
                                    this.id,
                                    getEncode(this.passwd),
                                    this.name,this.mail,type);
            //TypeAndIdクラスのセッターでUserAccountオブジェクトをセット
            type.setUser_account(account);
            //アカウント情報をデータベースｎ登録
            entity.create(account);
            //トップページにリダイレクト
            return "index?faces-redirect=true";
        }
    }
    
    public long countAccount(){
        long account=0;
        try{
            account = fetchUserAccount.countAccount(this.id);
        }catch (Exception e) {
            setMessage("アカウント情報の取得に失敗しました");
        }
        return account;
    }

    /***************************************************************************
     * パスワードの暗号化
     * @param pass 入力された値
     * @return 暗号化後のパスワード
     **************************************************************************/
    public String getEncode(String pass){
        //SHA256Encoderクラスをインスタンス化
        SHA256Encoder encoder = new SHA256Encoder();
        //encoderBySHA256()でエンコードした結果を返す
        return encoder.encodeBySHA256(pass);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getType_id() {
        return type_id;
    }

    public void setType_id(String type_id) {
        this.type_id = type_id;
    }


    
}
