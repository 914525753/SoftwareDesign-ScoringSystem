import java.nio.charset.Charset;
import java.util.Base64;

public class Base64Utils
{

    /**
     * Base64中的字符 + / = 在url中存在特殊意义，如果放在url中会被转义。
     */
    public static class Base64Test {
        public static void main(String[] args) {
            //将字节数组编码成Base64字符串
            String src = "wx11bcb2ca804aa5ef";
            String enc = Base64.getEncoder().encodeToString(src.getBytes());
            //不指定编码会使用默认的编码 Charset.defaultCharset().name()
            System.out.println(enc);

            //将Base64字符串解码成字节数组
            byte[] decode = Base64.getDecoder().decode(enc);
            String aSrc = new String(decode);
            System.out.println(aSrc);

            String defaultCharset = Charset.defaultCharset().name();
            System.out.println(defaultCharset);//UTF-8
        }
    }
}
