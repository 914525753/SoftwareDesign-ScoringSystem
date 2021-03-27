import java.nio.charset.Charset;
import java.util.Base64;

public class Base64Utils
{

    /**
     * Base64�е��ַ� + / = ��url�д����������壬�������url�лᱻת�塣
     */
    public static class Base64Test {
        public static void main(String[] args) {
            //���ֽ���������Base64�ַ���
            String src = "wx11bcb2ca804aa5ef";
            String enc = Base64.getEncoder().encodeToString(src.getBytes());
            //��ָ�������ʹ��Ĭ�ϵı��� Charset.defaultCharset().name()
            System.out.println(enc);

            //��Base64�ַ���������ֽ�����
            byte[] decode = Base64.getDecoder().decode(enc);
            String aSrc = new String(decode);
            System.out.println(aSrc);

            String defaultCharset = Charset.defaultCharset().name();
            System.out.println(defaultCharset);//UTF-8
        }
    }
}
