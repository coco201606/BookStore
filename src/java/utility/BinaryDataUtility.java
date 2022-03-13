package utility;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;


/**
 * 2021.8.1 バイナリデータを読み込むクラス
 * @author daidou
 */



public class BinaryDataUtility {
	/**********************************************
	* Loggerオブジェクトの生成
	**********************************************/
	static final Logger log = Logger.getLogger(
                                BinaryDataUtility.class.getName());
	
	/**********************************************
	* バイナリデータを読み込む
	* @param relative  読み込むファイルの相対パス
	* @return          バイナリ形式のデータ（配列）
	**********************************************/
	public static byte[] readBinaryData(String relative){
    	String filePath = getRealPath(relative);		// 絶対パスに変換
		Path path       = Paths.get(filePath);	// Pathオブジェクトを作成
		byte[]	binaryData = null;
		try {
			binaryData	= Files.readAllBytes(path);		// 全データを読み出す
		} catch (IOException ex) {
            log.severe(ex.toString());
		}
		return	binaryData;
    }
	/**********************************************
	* バイナリデータをファイルに書き出す
	* @param binary  byte[]型のバイナリデータ
	* @param outFile  出力先のファイルURL
	**********************************************/
	/*public	static	void	putBinary(byte[] binary, String outFile){
    	String filePath = getRealPath(outFile);         // 絶対パスに変換
		Path path       = Paths.get(filePath);  // Pathオブジェクトを作成
		try {
			Files.write(path, binary);      // 全データを書き出す
		} catch (IOException ex) {
            log.severe(ex.toString());
		}
	}*/
	/**********************************************
	* 絶対パスを取得する
	* @param path  絶対パスに変換するパス情報
	* @return      絶対パスを表す文字列
	**********************************************/
	public	static String getRealPath(String path){
		ServletContext context = (ServletContext) FacesContext
                                                          .getCurrentInstance()
                                                          .getExternalContext()
                                                          .getContext();
		return  context.getRealPath(path);
	}
}

