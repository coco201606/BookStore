/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package enterprise;

import utility.Pagenate;
import entity.BookData;
import entity.Genre;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 * 2021.8.1 Bookテーブルに登録されている書籍データを取得
 * @author daidou
 */
@Stateless

public class FetchBookData {
    @PersistenceContext
    EntityManager em;
    
    /***************************************************************************
     * Bookテーブルのデータを取得
     * @param sortItem 並べ替えの条件を指定するint型の値
     * @param genreItem 表示するジャンルを指定するGenre列挙型の値
     * @param pagenate Pagenationクラスのインスタンス
     * @return 並び替え後の書籍データのリスト
     **************************************************************************/
    public List<BookData> getBookList(
        int sortItem,
        Genre genreItem,
        Pagenate pagenate
    ){
        //TypedQuery型の変数（扱うBookDataに限定）
        TypedQuery<BookData> q;
        //ジャンルが未選択の場合
        if(genreItem==Genre.NONE){
            if(sortItem==1){
                //ID順で並べたレコードを全件取得
                q = em.createNamedQuery(BookData.FAIND_BY_ID,BookData.class);
            }else if(sortItem==2){
                //価格（昇順）で並べたレコードを全件取得
                q = em.createNamedQuery(BookData.FAIND_BY_PRICE,BookData.class);
            }else{
                //価格（降順）で並べたレコードを全件取得
                q = em.createNamedQuery
                    (BookData.FAIND_BY_PRICE_DESC,BookData.class);
            }
            
        //ジャンルが選択されている場合の処理
        }else{
            if(sortItem==1){
                //選択されたジャンルのレコードをID順で並べて取得
                q = em.createNamedQuery(BookData.FAIND_GENRE,BookData.class);
            }else if(sortItem==2){
                //選択されたジャンルのレコードを価格（昇順）で並べて取得
                q = em.createNamedQuery(BookData.GENRE_BY_PRICE,BookData.class);
            }else{
                //選択されたジャンルのレコードを価格（降順）で並べて取得
                q = em.createNamedQuery
                    (BookData.GENRE_BY_PRICE_DESC,BookData.class);
            }
            //名前付きパラメータに値を設定
            q.setParameter("genreItem",genreItem);
        }
        //取得するレコードの先頭位置を指定する
        q.setFirstResult(pagenate.findTopData());
        //取得するレコードの件数を指定する
        q.setMaxResults(pagenate.getPer());
        //クエリを実行して結果を返す
        return q.getResultList();
    }

    /***************************************************************************
     * Pagenationクラスのフィールドの初期化
     * @param genre 選択中のジャンル
     * @param pagenate Pagenationクラスのインスタンス
     **************************************************************************/
     public void preparePage(Genre genre,Pagenate pagenate){
         TypedQuery<Long> q;
         if(genre==Genre.NONE){
             //レコードの総件数を取得
             q = em.createNamedQuery(BookData.COUNT_ALL,Long.class);
         }else{
             //ジャンルごとのレコードの件数を取得
             q = em.createNamedQuery(BookData.COUNT_GENRE,Long.class);
             //選択されたジャンルをプレースフォルダに設定
             q.setParameter("genreItem",genre);
         }
         //クエリを実行してレコードの件数を取得
         long countResult = q.getSingleResult();
         //レコードの件数と1ページあたりの表示件数を指定してPagenateを初期化
         pagenate.initPagination((int)countResult,5);
         
     }
             
             
    
}
