/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package enterprise;

import entity.OrderBooks;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 * 2021.8.1 指定したユーザーIDに一致するレコードをORDERBOOKテーブルから
 * 読み込むためのクラス
 * @author daidou
 */
@Stateless

public class FetchOrderBooks {
    @PersistenceContext
    EntityManager em;
    
    /***************************************************************************
     * 指定したユーザーIDの購入履歴を取得
     * @param id ユーザーID
     * @return ORDERテーブルから取得したレコードのList
     **************************************************************************/
    public List<OrderBooks> getHistory(String id){
        TypedQuery<OrderBooks> q = em.createNamedQuery
                            (OrderBooks.ORDER_HISTORY,OrderBooks.class);
        //名前付きパラメータにユーザーIDをセット
        q.setParameter("user_id",id);
        //クエリをジックしてListオブジェクトを返す
        return q.getResultList();
    }
    
}
