import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Servlet implementation class ExamineJudges
 */
@WebServlet("/ExamineJudges")
public class ExamineJudges extends HttpServlet {
    private static final long serialVersionUID = 1L;
    // JDBC 驱动名及数据库 URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";  
    static final String DB_URL = "jdbc:mysql://localhost:3306/bearcome?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=GMT%2B8";
    
    // 数据库的用户名与密码，需要根据自己的设置
    static final String USER = "root";
    static final String PASS = ""; 
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ExamineJudges() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        PrintWriter out = response.getWriter();
        request.setCharacterEncoding("UTF-8");
        ResultSet rs = null;
        // 设置响应内容类型
        response.setContentType("text/html;charset=UTF-8");
        try{
            // 注册 JDBC 驱动器
            Class.forName(JDBC_DRIVER);
            // 打开一个连接
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            if(request.getParameter("action") != null)
            {
                int type = Integer.parseInt(request.getParameter("type"));
                String action = request.getParameter("action");
                String sql;
                int userid = -1;
                String description = "";
                int id = -1;
                String nickname;
                int level = 0;
                String partake;
                String partake2 = "";
                int index = -1;
                HttpSession session = request.getSession();
                if(action.equals("show")) //显示申请信息
                {
                    level = Integer.parseInt(session.getAttribute("level").toString());
                    if(level >= 2)
                    {
                        if(type == 2)
                        {
                            userid = Integer.parseInt(session.getAttribute("userid").toString());
                            sql = "SELECT createpartake from judge where userid=?";
                            pstmt = conn.prepareStatement(sql);
                            pstmt.setInt(1,userid);
                            rs = pstmt.executeQuery();
                            while(rs.next())
                            {
                                String createpartake = rs.getString("createpartake");
                                String createpartake2 = createpartake.substring(0,createpartake.length() - 1);
                                sql = "SELECT * from examine where type=2 and FIND_IN_SET(id,?)";
                                pstmt = conn.prepareStatement(sql);
                                pstmt.setString(1,createpartake2);
                                rs = pstmt.executeQuery();
                                while(rs.next())
                                {
                                    userid = rs.getInt("userid");
                                    description = rs.getString("description");
                                    id = rs.getInt("id");
                                    index = rs.getInt("index1");
                                }
                            }
                        }
                        else if(type == 1)
                        {
                            sql = "SELECT * from examine where type=1;";
                            pstmt = conn.prepareStatement(sql);
                            rs = pstmt.executeQuery();
                            while(rs.next())
                            {
                                userid = rs.getInt("userid");
                                description = rs.getString("description");
                                index = rs.getInt("index1");
                            }
                        }
                        sql = "SELECT name from users where userid=?";
                        pstmt = conn.prepareStatement(sql);
                        pstmt.setInt(1,userid);
                        rs = pstmt.executeQuery();
                        while(rs.next())
                        {
                            if(userid !=-1 && description != "" && index != -1) //初值不具有任何意义，仅绕过java的编译检查
                            {
                                String name = rs.getString("name");
                                if(type == 2)
                                {
                                    sql = "SELECT name from contest where id=?";
                                    pstmt = conn.prepareStatement(sql);
                                    pstmt.setInt(1,id);
                                    rs = pstmt.executeQuery();
                                    while(rs.next())
                                    {
                                        String contestname = rs.getString("name");
                                        out.println("{\"index\":\"" + index + 
                                        "\",\"userid\":\"" + userid +
                                        "\",\"name\":\"" + name +
                                        "\",\"description\":\"" + description +
                                        "\",\"id\":\"" + id +
                                        "\",\"contestname\":\"" + contestname +
                                        "\"};");
                                    }                
                                }
                                else if(type == 1)
                                {
                                    out.println("{\"index\":\"" + index + 
                                    "\",\"userid\":\"" + userid +
                                    "\",\"name\":\"" + name +
                                    "\",\"description\":\"" + description +
                                    "\"};");
                                }
                            }
                        }
                    }
                }
                else if(action.equals("request")) //用户申请
                {
                    description = request.getParameter("description");
                    userid = Integer.parseInt(session.getAttribute("userid").toString());
                    level = Integer.parseInt(session.getAttribute("level").toString());
                    if(userid != -1)
                    {
                        if(type == 2)
                        {
                            id = Integer.parseInt(request.getParameter("id"));
                            sql = "SELECT * from examine where userid=? and type=? and id=?";
                            pstmt = conn.prepareStatement(sql);
                            pstmt.setInt(1,userid);
                            pstmt.setInt(2,type);
                            pstmt.setInt(3,id);
                        }
                        else if(type == 1)
                        {
                            sql = "SELECT * from examine where userid=? and type=?";
                            pstmt = conn.prepareStatement(sql);
                            pstmt.setInt(1,userid);
                            pstmt.setInt(2,type);
                        }
                        rs = pstmt.executeQuery(); 
                        if(!rs.first())
                        {
                            if(type == 2 && level == 2)
                            {
                                sql = "INSERT INTO examine(userid,description,type,id) VALUES(?,?,?,?)";
                                pstmt = conn.prepareStatement(sql);
                                pstmt.setInt(1,userid);
                                pstmt.setString(2,description);
                                pstmt.setInt(3,2);
                                pstmt.setInt(4,id);
                                pstmt.executeUpdate();

                                out.println(1); //提交成功
                            }
                            else if(type == 1 && level == 1)
                            {
                                sql = "INSERT INTO examine(userid,description,type) VALUES(?,?,?);";
                                pstmt = conn.prepareStatement(sql);
                                pstmt.setInt(1,userid);
                                pstmt.setString(2,description);
                                pstmt.setInt(3,1);
                                pstmt.executeUpdate();

                                out.println(1); //提交成功
                            }
                            else out.println(5); //错误提交
                        }
                        else out.println(2); //已经提交
                    }
                }
                else if(action.equals("accept")) //审核申请
                {
                    level = Integer.parseInt(session.getAttribute("level").toString());
                    if(level >= 2)
                    {
                        index = Integer.parseInt(request.getParameter("index"));
                        sql = "SELECT * FROM examine where index1=? and type=?";
                        pstmt = conn.prepareStatement(sql);
                        pstmt.setInt(1,index);
                        pstmt.setInt(2,type);
                        rs = pstmt.executeQuery();
                        while(rs.next())
                        {
                            userid = rs.getInt("userid");
                            String description2 = rs.getString("description");
                            id = rs.getInt("id");
                            if(description2 != "")
                            {
                                String can = request.getParameter("can");
                                if(can.equals("yes"))
                                {
                                    if(type == 1)
                                    {
                                        sql = "UPDATE users SET level=2 where userid=?";
                                        pstmt = conn.prepareStatement(sql);
                                        pstmt.setInt(1,userid);
                                        pstmt.executeUpdate();

                                        sql = "INSERT INTO judge(userid) VALUES(?);";
                                        pstmt = conn.prepareStatement(sql);
                                        pstmt.setInt(1,userid);
                                        pstmt.executeUpdate();
                                    }
                                    else
                                    {
                                        sql = "SELECT partake from judge where userid=?";
                                        pstmt = conn.prepareStatement(sql);
                                        pstmt.setInt(1,userid);
                                        rs = pstmt.executeQuery();
                                        while(rs.next())
                                        {
                                            partake = rs.getString("partake");
                                            partake2 = partake + id + ",";
                                            sql = "UPDATE judge SET partake=? where userid=?";
                                            pstmt = conn.prepareStatement(sql);
                                            pstmt.setString(1,partake2);
                                            pstmt.setInt(2,userid);
                                            pstmt.executeUpdate();
                                        }

                                        sql = "SELECT judgeid from contest where id=?";
                                        pstmt = conn.prepareStatement(sql);
                                        pstmt.setInt(1,id);
                                        pstmt.executeQuery();
                                        while(rs.next())
                                        {
                                            String judgeid = rs.getString("judgeid");
                                            String judgeid2 = judgeid + userid + ",";
                                            sql = "UPDATE contest SET judgeid=? where id=?";
                                            pstmt = conn.prepareStatement(sql);
                                            pstmt.setString(1,judgeid2);
                                            pstmt.setInt(2,id);
                                            pstmt.executeUpdate();
                                        }

                                        String contest = "contest" + id;
                                        sql = "INSERT INTO " + contest + "(userid) VALUES(?)";
                                        pstmt = conn.prepareStatement(sql);
                                        pstmt.setInt(1,userid);
                                        pstmt.executeUpdate();
                                    }
                                    out.println(1); //通过审核
                                }
                                else if(can.equals("no"))
                                {
                                    out.println(2); //拒绝通过
                                } 
                                sql = "DELETE FROM examine WHERE index1=? and type=?";
                                pstmt = conn.prepareStatement(sql);
                                pstmt.setInt(1,index);
                                pstmt.setInt(2,type);
                                pstmt.executeUpdate();   
                            }
                            else out.println(3); //已经被处理
                        }
                    }
                }
            }
            // 完成后关闭
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

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
        doGet(request, response);
    }
}
