import com.arronlong.httpclientutil.HttpClientUtil;
import com.arronlong.httpclientutil.common.HttpConfig;
import com.arronlong.httpclientutil.exception.HttpProcessException;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet("/LoginServlet2")
public class LoginServlet2 extends HttpServlet {
    private static final long serialVersionUID = 1L;
    // JDBC 驱动名及数据库 URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql:///bearcome?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false";

    // 数据库的用户名与密码，需要根据自己的设置
    static final String USER = "root";
    static final String PASS = "";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO 自动生成的方法存根
        String method = req.getParameter("method");
        if (method.equals( "WeChatLogin") && method != null) {
            this.WeChatLogin(req, resp);
        }else if (method.equals( "PasswordLogin") && method != null) {
            this.PasswordLogin(req, resp);
        }
        //其他方法else if添加
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
        // 设置响应内容类型
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try{
            // 注册 JDBC 驱动器
            Class.forName(JDBC_DRIVER);

            // 打开一个连接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            String appId2 = ";
            String appSecret2 = "";

            String appId = Base64Utils.decode(appId2);
            String appSecret = Base64Utils.decode(appSecret2);
            //获取密码、昵称
            String nickname = request.getParameter("nickname");
            String code = request.getParameter("code");
            String username = nickname;
            System.out.print(request.getParameter("nickname"));
            //用code换取openID
            String open = new LoginServlet2().open(appId,appSecret,code);

            //json转换实体WeChat类
            Gson gson = new Gson();
            WeChatPojo WeChat = gson.fromJson(open, WeChatPojo.class);
            String openid = WeChat.getOpenid();

            if(WeChat.getErrcode()!=0)
            {
                out.write("5");//code请求失败或无code
            }
            else
            {
                String SelectSql = "select * from users where WeChatCode=?;";
                pstmt = conn.prepareStatement(SelectSql);
                pstmt.setString(1,openid);
                ResultSet rs = pstmt.executeQuery();
                if(!rs.first()) {
                    // 执行 SQL
                    String ReplaceSql = "insert into users(nickname,WeChatCode,level,name) values(?,?,?,?);";
                    pstmt = conn.prepareStatement(ReplaceSql);
                    pstmt.setString(1,nickname);
                    pstmt.setString(2,openid);
                    pstmt.setInt(3,1);
                    pstmt.setString(4,username);
                    int updateRows = pstmt.executeUpdate();
                    if(updateRows > 0){
                        String SelectIdSql = "select userid,level from users where nickname=?;";
                        pstmt = conn.prepareStatement(SelectIdSql);
                        pstmt.setString(1,nickname);
                        ResultSet SelectIdRs = pstmt.executeQuery();
                        while (SelectIdRs.next())
                        {
                            request.getSession().setAttribute("userid", SelectIdRs.getString("userid"));
                            request.getSession().setAttribute("level", SelectIdRs.getString("level"));
                        }
                        out.write("3"); //3代表注册成功
                        //完成后关闭
                        SelectIdRs.close();
                    }else{
                        out.write("4"); //4代表注册失败
                    }
                }
                else
                {
                    String SelectIdSql = "select userid,level from users where nickname=?;";
                    pstmt = conn.prepareStatement(SelectIdSql);
                    pstmt.setString(1,nickname);
                    ResultSet SelectIdRs = pstmt.executeQuery();
                    while (SelectIdRs.next())
                    {
                        HttpSession session = request.getSession();
                        session.setAttribute("userid", SelectIdRs.getString("userid"));
                        session.setAttribute("level", SelectIdRs.getString("level"));
                        out.write("1"); //1代表登录成功
                        out.write(SelectIdRs.getString("level"));
                    }
                    SelectIdRs.close();
                }
                rs.close();
            }


            // 完成后关闭
            pstmt.close();
            conn.close();
        } catch(SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch(Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 最后是用于关闭资源的块
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
        //判断是否登录成功
        boolean IsLogin=false;


        // 设置响应内容类型
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();
        try{
            // 注册 JDBC 驱动器
            Class.forName(JDBC_DRIVER);
            // 打开一个连接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            //获取用户名和密码
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            if(password==null)
            {
                out.write("5");//未输入密码
            }
            else
            {
                //MD5加密
                String code = MD5Utils.stringToMD5(password);
                // 执行 SQL 查询
                String sql;
                sql = "select * from users where name=?;";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1,username);
                ResultSet rs = pstmt.executeQuery();

                while(rs.next()) {
                    // 通过字段检索
                    if (rs.getString("password").equals(code)) {
                        HttpSession session = request.getSession();
                        session.setAttribute("userid", rs.getString("userid"));
                        session.setAttribute("level", rs.getString("level"));
                        out.write("1");
                        out.write(rs.getString("level"));
                        IsLogin=true;
                        break;
                    }
                }

                if(IsLogin==false)
                {
                    out.write("2");
                }
                rs.close();
                pstmt.close();
            }

            // 完成后关闭
            conn.close();
        } catch(SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch(Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 最后是用于关闭资源的块
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

        //插件式配置请求参数（网址、请求参数、编码、client）
        HttpConfig config = HttpConfig.custom()
//                .headers(headers)	//设置headers，不需要时则无需设置
                .timeout(1000) 		//超时
                .url(url)           //设置请求的url
//                .map(map)			//设置请求参数，没有则无需设置
                .encoding("utf-8")  //设置请求和返回编码，默认就是Charset.defaultCharset()
//                .client(client)     //如果只是简单使用，无需设置，会自动获取默认的一个client对象
                .inenc("utf-8")   //设置请求编码，如果请求返回一直，不需要再单独设置
                .inenc("utf-8")   //设置返回编码，如果请求返回一直，不需要再单独设置
//                .json("json字符串") //json方式请求的话，就不用设置map方法，当然二者可以共用。
//                .context(HttpCookies.custom().getContext())      //设置cookie，用于完成携带cookie的操作
//                .out(new FileOutputStream("保存地址"))              //下载的话，设置这个方法,否则不要设置
//                .files(new String[]{"d:/1.txt","d:/2.txt"})      //上传的话，传递文件路径，一般还需map配置，设置服务器保存路径
                ;

        //使用方式：
        String result = HttpClientUtil.get(config);    //get请求
//    String result2 = HttpClientUtil.post(config);   //post请求
        return result;
    }
}
