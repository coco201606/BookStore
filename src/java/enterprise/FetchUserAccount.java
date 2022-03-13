/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package enterprise;

import entity.UserAccount;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 * 2021.8.15 ユーザーIDのカウント
 * @author daidou
 */

@Stateless

public class FetchUserAccount {
    @PersistenceContext
    EntityManager em;
    public long countAccount(String id){
        TypedQuery<Long> q;
        //ユーザーIDの件数を取得するクエリを生成
        q = em.createNamedQuery(UserAccount.COUNT_ID,long.class);
        //入力されたユーザーIDをプレースフォルダに設定
        q.setParameter("userId", id);
        //クエリを実行してレコードの件数を取得
        Long countResult = q.getSingleResult();
        return countResult;
    }
    
}
