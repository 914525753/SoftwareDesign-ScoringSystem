import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    // JDBC 驱动名及数据库 URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql:///bearcome?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false";

    // 数据库的用户名与密码，需要根据自己的设置
    static final String USER = "root";
    static final String PASS = "qertyiop1a";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

            //获取用户名、密码、权限、昵称
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String nickname = username;

            //MD5加密
            String code = MD5Utils.stringToMD5(password);

            String SelectSql = "select * from users where name=?;";
            pstmt = conn.prepareStatement(SelectSql);
            pstmt.setString(1,username);
            ResultSet rs = pstmt.executeQuery();
            if(!rs.first()) {
                // 执行 SQL
                String ReplaceSql = "insert into users(name,nickname,password,level) values(?,?,?,?);";
                pstmt = conn.prepareStatement(ReplaceSql);
                pstmt.setString(1,username);
                pstmt.setString(2,nickname);
                pstmt.setString(3,code);
                pstmt.setInt(4,1);
                int updateRows = pstmt.executeUpdate();
                if(updateRows > 0){
                    String SelectIdSql = "select userid from users where name=?;";
                    pstmt = conn.prepareStatement(SelectIdSql);
                    pstmt.setString(1,username);
                    ResultSet SelectIdRs = pstmt.executeQuery();
                    while (SelectIdRs.next())
                    {
                        request.getSession().setAttribute("userid", SelectIdRs.getString("userid"));
                    }
                    request.getSession().setAttribute("level", "1");
                    out.write("1"); //1代表注册成功
                    //完成后关闭
                    SelectIdRs.close();
                }else{
                    out.write("2"); //2代表已经被人抢注
                }
            }
            else
            {
                out.write("3"); //3代表帐号已存在
            }

//             完成后关闭
            rs.close();
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

}
