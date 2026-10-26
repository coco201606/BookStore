/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utility;

import java.util.logging.Logger;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;


/**
 * 2021.8.1 エラー発生時のログを出力するためのロガー
 * @author daidou
 */
@Dependent      //CDIBean
public class GenerateLogger {
    @Inject     //インジェクと先の情報を取得
    InjectionPoint point;
    @Produces    //戻り値をオブジェクトの任意の管理Beanへインジェクト
    public Logger getLogger(){
        String className = point.getMember().getDeclaringClass().getName();
        //取得したクラス名を引数にしてロガーを生成
        Logger logger = Logger.getLogger(className);
        return logger;
    }
    
}
