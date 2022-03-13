/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utility;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * 2021.8.15 パスワードをSHA256で暗号化するクラス
 * @author daidou
 */

public class SHA256Encoder implements Serializable {
    /*private static final long serialVersionUID = 1L; */
    
    /***************************************************************************
     * パスワードをSHA256で暗号化
     * @param password 入力された値
     * @return 暗号化後のパスワード
     **************************************************************************/
    public String encodeBySHA256(String password){
        //エンコード語のパスワードを格納する変数
        String encorded = "";
        try {
            //メッセージダイジェストアルゴリズムの機能を提供する抽象クラスを
            //getInstance()で取得　取得の際にSHA-256を指定
            MessageDigest mdst = MessageDigest.getInstance("SHA-256");
            //パスワードのバイト列を引数にしてハッシュ値を生成
            //結果はbyte型の配列で返される
            byte[] digest =mdst.digest(password.getBytes());
            //StringBuilderオブジェクトを生成
            StringBuilder strBuilder = new StringBuilder();
            for (int i=0; i<digest.length; i++){
                //ハッシュ値digestの各要素の値を16進数の文字列表現として取得
                //その際に下位8桁以外の桁を0で埋める
                String tmp_value = Integer.toHexString(digest[i] & 0xff);
                if (tmp_value.length()==1){
                //長さが1であれば0をstrBuilderの先頭に追加し、
                //さらにtmp_valueを追加する
                    strBuilder.append('0').append(tmp_value);
                } else {
                    //それ以外はstrBuilderに直接tmp_valueを追加
                    strBuilder.append(tmp_value);
                }
            }
        //strBuilder型をString型にキャスト
        encorded = strBuilder.toString();
        
    } catch (NoSuchAlgorithmException ex) {
    Logger.getLogger(SHA256Encoder.class.getName()).log(Level.SEVERE,null,ex);
    }
    return encorded;
    }
}
