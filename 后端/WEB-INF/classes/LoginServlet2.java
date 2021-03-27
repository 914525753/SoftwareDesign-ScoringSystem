import com.arronlong.httpclientutil.HttpClientUtil;
import com.arronlong.httpclientutil.common.HttpConfig;
import com.arronlong.httpclientutil.exception.HttpProcessException;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet("/LoginServlet2")
public class LoginServlet2 extends HttpServlet {
    private static final long serialVersionUID = 1L;
    // JDBC �����������ݿ� URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql:///bearcome?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false";

    // ���ݿ���û��������룬��Ҫ�����Լ�������
    static final String USER = "root";
    static final String PASS = "qertyiop1a";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO �Զ����ɵķ������
        String method = req.getParameter("method");
        if (method.equals( "WeChatLogin") && method != null) {
            this.WeChatLogin(req, resp);
        }else if (method.equals( "PasswordLogin") && method != null) {
            this.PasswordLogin(req, resp);
        }
        //��������else if���
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    private void WeChatLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        Connection conn = null;
        PreparedStatement pstmt = null;
        // ������Ӧ��������
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try{
            // ע�� JDBC ������
            Class.forName(JDBC_DRIVER);

            // ��һ������
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            String appId = "wx11bcb2ca804aa5ef";
            String appSecret = "f2a18068fb6a0bf19dead1995e7b8657";

            //��ȡ���롢�ǳ�
            String nickname = request.getParameter("nickname");
            String code = request.getParameter("code");
            String username = nickname;

            //��code��ȡopenID
            String open = new WeChatRequest().open(appId,appSecret,code);

            //jsonת��ʵ��WeChat��
            Gson gson = new Gson();
            WeChatPojo WeChat = gson.fromJson(open, WeChatPojo.class);
            String openid = WeChat.getOpenid();

            if(WeChat.getErrcode()!=0)
            {
                out.print("Errcode:"+WeChat.getErrcode()+"\n");
                out.print("Errmsg:"+WeChat.getErrmsg()+"\n");
            }
            else
            {
                String SelectSql = "select * from users where WeChatCode=?;";
                pstmt = conn.prepareStatement(SelectSql);
                pstmt.setString(1,openid);
                ResultSet rs = pstmt.executeQuery();
                if(!rs.first()) {
                    // ִ�� SQL
                    String ReplaceSql = "insert into users(nickname,WeChatCode,level,name) values(?,?,?,?);";
                    pstmt = conn.prepareStatement(ReplaceSql);
                    pstmt.setString(1,nickname);
                    pstmt.setString(2,openid);
                    pstmt.setInt(3,1);
                    pstmt.setString(4,username);
                    int updateRows = pstmt.executeUpdate();
                    if(updateRows > 0){
                        String SelectIdSql = "select userid from users where nickname=?;";
                        pstmt = conn.prepareStatement(SelectIdSql);
                        pstmt.setString(1,nickname);
                        ResultSet SelectIdRs = pstmt.executeQuery();
                        while (SelectIdRs.next())
                        {
                            request.getSession().setAttribute("userid", SelectIdRs.getString("userid"));
                        }
                        request.getSession().setAttribute("level", 1);
                        out.write("3"); //1����ע��ɹ�
                        //��ɺ�ر�
                        SelectIdRs.close();
                    }else{
                        out.write("4"); //2�����Ѿ�������ע
                    }
                }
                else
                {
                    String SelectIdSql = "select userid from users where nickname=?;";
                    pstmt = conn.prepareStatement(SelectIdSql);
                    pstmt.setString(1,nickname);
                    ResultSet SelectIdRs = pstmt.executeQuery();
                    while (SelectIdRs.next())
                    {
                        request.getSession().setAttribute("userid", SelectIdRs.getString("userid"));
                    }
                    request.getSession().setAttribute("level", 1);
                    out.write("1"); //1����ע��ɹ�
                }
                rs.close();
            }


            // ��ɺ�ر�
            pstmt.close();
            conn.close();
        } catch(SQLException se) {
            // ���� JDBC ����
            se.printStackTrace();
        } catch(Exception e) {
            // ���� Class.forName ����
            e.printStackTrace();
        }finally{
            // ��������ڹر���Դ�Ŀ�
            try{
                if(pstmt!=null)
                    pstmt.close();
            }catch(SQLException se2){
            }
            try{
                if(conn!=null)
                    conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
    }

    private void PasswordLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        Connection conn = null;
        PreparedStatement pstmt = null;
        //�ж��Ƿ��¼�ɹ�
        boolean IsLogin=false;


        // ������Ӧ��������
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();
        try{
            // ע�� JDBC ������
            Class.forName(JDBC_DRIVER);
            // ��һ������
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            //��ȡ�û���������
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            System.out.print(username);
            System.out.print(password);
            //MD5����
            String code = MD5Utils.stringToMD5(password);

            // ִ�� SQL ��ѯ
            String sql;
            sql = "select * from users where name=?;";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,username);
            ResultSet rs = pstmt.executeQuery();

            while(rs.next()) {
                // ͨ���ֶμ���
                if (rs.getString("password").equals(code)) {
                    request.getSession().setAttribute("userid", rs.getString("userid"));
                    request.getSession().setAttribute("level", rs.getString("level"));
                    out.write("1");
                    IsLogin=true;
                    break;
                }
            }

            if(IsLogin==false)
            {
                out.write("2");
            }

            // ��ɺ�ر�
            System.out.print(request.getSession().getAttribute("userid"));
            System.out.print(request.getSession().getAttribute("level"));
            rs.close();
            pstmt.close();
            conn.close();
        } catch(SQLException se) {
            // ���� JDBC ����
            se.printStackTrace();
        } catch(Exception e) {
            // ���� Class.forName ����
            e.printStackTrace();
        }finally{
            // ��������ڹر���Դ�Ŀ�
            try{
                if(pstmt!=null)
                    pstmt.close();
            }catch(SQLException se2){
            }
            try{
                if(conn!=null)
                    conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
    }

    public String open(String appId,String appSecret,String code) throws HttpProcessException {
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid="+appId+"&secret="+appSecret+"&js_code="+code+"&grant_type=authorization_code";

        //���ʽ���������������ַ��������������롢client��
        HttpConfig config = HttpConfig.custom()
//                .headers(headers)	//����headers������Ҫʱ����������
                .timeout(1000) 		//��ʱ
                .url(url)           //���������url
//                .map(map)			//�������������û������������
                .encoding("utf-8")  //��������ͷ��ر��룬Ĭ�Ͼ���Charset.defaultCharset()
//                .client(client)     //���ֻ�Ǽ�ʹ�ã��������ã����Զ���ȡĬ�ϵ�һ��client����
                .inenc("utf-8")   //����������룬������󷵻�һֱ������Ҫ�ٵ�������
                .inenc("utf-8")   //���÷��ر��룬������󷵻�һֱ������Ҫ�ٵ�������
//                .json("json�ַ���") //json��ʽ����Ļ����Ͳ�������map��������Ȼ���߿��Թ��á�
//                .context(HttpCookies.custom().getContext())      //����cookie���������Я��cookie�Ĳ���
//                .out(new FileOutputStream("�����ַ"))              //���صĻ��������������,����Ҫ����
//                .files(new String[]{"d:/1.txt","d:/2.txt"})      //�ϴ��Ļ��������ļ�·����һ�㻹��map���ã����÷���������·��
                ;

        //ʹ�÷�ʽ��
        String result = HttpClientUtil.get(config);    //get����
//    String result2 = HttpClientUtil.post(config);   //post����
        return result;
    }
}
