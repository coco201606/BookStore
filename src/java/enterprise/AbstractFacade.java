/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package enterprise;

import java.util.List;
import javax.persistence.EntityManager;

/**
 * 2021.8.1 ジェネリッククラス
 * 　　　　　EJBの型情報を渡すことでCRUD操作を行うメソッドを実行
 * @author daidou
 */
public abstract class AbstractFacade<T> {
//エンティティクラスのインスタンスを保持するフィールド
    private Class<T> entityClass;

    /***************************************************************************
     *  コンストラクタ
     * @param entityClass エンティティクラスのインスタンス
     **************************************************************************/
    public AbstractFacade(Class<T> entityClass) {
        this.entityClass = entityClass;
    }
    /***************************************************************************
     *  エンティティ・マネージャーを取得（抽象メソッド）
     **************************************************************************/
    protected abstract EntityManager getEntityManager();

    /***************************************************************************
     * persist()メソッドを取得して登録処理を行う
     * @param entity エンティティクラスのインスタンス
     **************************************************************************/
    public void create(T entity) {
        getEntityManager().persist(entity);
    }

    /***************************************************************************
     * persist()mergeメソッドを取得して更新処理を行う
     * @param entity エンティティクラスのインスタンス
     **************************************************************************/
    public void edit(T entity) {
        getEntityManager().merge(entity);
    }

    /***************************************************************************
     * remove()メソッドを取得して削除処理を行う
     * @param entity エンティティクラスのインスタンス
     **************************************************************************/
    public void remove(T entity) {
        getEntityManager().remove(getEntityManager().merge(entity));
    }

    /***************************************************************************
     * find()メソッドを取得して検索処理を行う
     * @param id 検索用の主キー
     * @return 検索処理結果が格納されたエンティティ
     **************************************************************************/
    public T find(Object id) {
        return getEntityManager().find(entityClass, id);
    }

    /***************************************************************************
     * テーブルの全件取得処理
     * @return エンティティのリスト
     **************************************************************************/
    public List<T> findAll() {
        javax.persistence.criteria.CriteriaQuery cq
                = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        return getEntityManager().createQuery(cq).getResultList();
    }

    /***************************************************************************
     * 指定した範囲のレコードを取得
     * @param range 取得するレコードの範囲を指定する配列
     * @return range[0]番目からrange[1]番目までのエンティティのリスト
     **************************************************************************/
    public List<T> findRange(int[] range) {
        javax.persistence.criteria.CriteriaQuery cq
                = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        q.setMaxResults(range[1] - range[0] + 1);
        q.setFirstResult(range[0]);
        return q.getResultList();
    }

    /***************************************************************************
     * レコードの総件数を取得
     * @return レコードの総件数を示すLong型の値
     **************************************************************************/
    public int count() {
        javax.persistence.criteria.CriteriaQuery cq 
                = getEntityManager().getCriteriaBuilder().createQuery();
        javax.persistence.criteria.Root<T> rt = cq.from(entityClass);
        cq.select(getEntityManager().getCriteriaBuilder().count(rt));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }
    
}
