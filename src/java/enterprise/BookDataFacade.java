/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package enterprise;

import entity.BookData;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * 2021.8.1 BookDataのEJB
 * AbstractFacade（抽象クラス）を継承し、getEntityManager()メソッドを
 * オーバーライドしてエンティティマネージャーを返す。
 * ポイントはスーパークラスのコンストラクターにエンティティクラス（BookData）
 * を渡すようにしていること
 * @author daidou
 */
@Stateless
public class BookDataFacade extends AbstractFacade<BookData> {
    /***************************************************************************
     * エンティティマネージャーをインジェクト
     **************************************************************************/
    @PersistenceContext(unitName = "BookStorePU")
    private EntityManager em;

    /***************************************************************************
     * 抽象クラスのgetEntityManager()をオーバーライド
     * @return エンティティマネージャー
     **************************************************************************/
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /***************************************************************************
     * コンストラクター
     * スーパークラスのコンストラクターにエンティティクラスBookDataを渡す
     **************************************************************************/
    public BookDataFacade() {
        super(BookData.class);
    }
    
}
