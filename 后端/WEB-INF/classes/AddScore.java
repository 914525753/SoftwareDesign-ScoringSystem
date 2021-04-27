import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

@WebServlet("/AddScore")
public class AddScore extends HttpServlet{
    private static final long serialVersionUID = 1L;
    // JDBC 驱动名及数据库 URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql:///bearcome?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false";

    // 数据库的用户名与密码，需要根据自己的设置
    static final String USER = "root";
    static final String PASS = "";

    public  String getRequestPayload(HttpServletRequest req) {
        StringBuilder sb = new StringBuilder();
        try(BufferedReader reader = req.getReader()) {
            char[]buff = new char[1024];
            int len;
            while((len = reader.read(buff)) != -1) {
                sb.append(buff,0, len);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;
        PreparedStatement pstmt = null;

        //判断是否所有评委都评完分数
        boolean IsScore = true;

        // 设置响应内容类型
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            // 注册 JDBC 驱动器
            Class.forName(JDBC_DRIVER);

            // 打开一个连接
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            //插入比赛和平均分的Flag
            boolean IsAdd=true;
            boolean IsContest=true;
            boolean IsAVG=true;
            //获取session
            HttpSession session = request.getSession();
            int level = Integer.parseInt(session.getAttribute("level").toString());
            int JudgeId = Integer.parseInt(session.getAttribute("userid").toString());

            //读取json字符串
            String requestPayload = getRequestPayload(request);

            //json转化为Map
            GsonBuilder gb = new GsonBuilder();
            Gson g = gb.create();
            Map<String, String> map = g.fromJson(requestPayload, new TypeToken<Map<String, String>>() {
            }.getType());

            //排序用map
            Map<Integer, String> TreeMap = new TreeMap<Integer, String>();

            //列名称Vector
            Vector vec = new Vector();

//            //获取比赛名称
//            String ContestName = request.getParameter("ContestName");

            if(level==2||level==3)
            {
                //获取列名称
                String sql = "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'bearcome' AND TABLE_NAME = ?;";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, map.get("ContestName"));
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    if(!rs.getString("COLUMN_NAME").equals("userid"))
                        vec.add(rs.getString("COLUMN_NAME"));
//                String column_name = rs.getString("COLUMN_NAME");
                }

                //添加评分
                for(int i=0;i<vec.size();i++)
                {
                    String UpdateSql = "Update " + map.get("ContestName") + " set `" + vec.get(i) + "`=? where userid=?;";
                    pstmt = conn.prepareStatement(UpdateSql);
                    pstmt.setInt(1, Integer.parseInt(map.get(vec.get(i))));
                    pstmt.setInt(2, JudgeId);
                    int updateRows = pstmt.executeUpdate();
                    if (updateRows > 0) {

                    } else {
                        IsAdd=false;
                    }
                }

                //判断是否评委都评分
                ResultSet IsRs=null;
                for(int i=0;i<vec.size();i++)
                {
                    String IsSql="select * from "+map.get("ContestName")+" where `"+vec.get(i)+"` is null ;";
                    pstmt = conn.prepareStatement(IsSql);
                    IsRs = pstmt.executeQuery();
                    if(IsRs.isBeforeFirst()){
                        IsScore=false;
                        break;
                    }
                }
                IsRs.close();

                //所有评委是否都评分
                if(IsScore)
                {
                    //排名
                    int ranking = vec.size();
                    //比赛序号
                    String[] SplitContest = map.get("ContestName").split("t");

                    for(int i=0;i<vec.size();i++)
                    {
                        int length=0;
                        String IsNullSql = "select json_length(score) as length FROM competitor where userid = ?;";
                        pstmt = conn.prepareStatement(IsNullSql);
                        pstmt.setInt(1,Integer.parseInt(String.valueOf(vec.get(i))));
                        ResultSet IsNullRs = pstmt.executeQuery();
                        while (IsNullRs.next())
                        {
                            length=IsNullRs.getInt("length");
                            if(Objects.equals(length, 0))
                            {
                                String BracketSql ="update competitor set score = '[]' where userid = ?;";
                                pstmt = conn.prepareStatement(BracketSql);
                                pstmt.setInt(1, Integer.parseInt(String.valueOf(vec.get(i))));
                                int BracketRows = pstmt.executeUpdate();
                            }
                            System.out.print(length);
                        }
                        //插入比赛
                        String InsertSql="update competitor set score = JSON_ARRAY_INSERT(score,'$["+length+"]','"+SplitContest[2]+"') where userid = ?;";
                        pstmt = conn.prepareStatement(InsertSql);
                        pstmt.setInt(1, Integer.parseInt(String.valueOf(vec.get(i))));
                        int InsertRows = pstmt.executeUpdate();
                        if (InsertRows > 0) {

                        } else {
                            IsContest=false;
                        }


                        //求分数平均数
                        String SelectSql="SELECT AVG(`"+vec.get(i)+"`) AS AVG_Score FROM "+map.get("ContestName")+";";
                        pstmt = conn.prepareStatement(SelectSql);
                        ResultSet AVG_rs = pstmt.executeQuery();
                        while (AVG_rs.next())
                        {
                            int AVG_Score = AVG_rs.getInt("AVG_Score");
                            //插入TreeMap中key自动从大到小排序
                            TreeMap.put(AVG_Score, String.valueOf(vec.get(i)));
                        }
                        IsNullRs.close();
                        AVG_rs.close();
                    }

                    //遍历元素：
                    Set<Map.Entry<Integer,String>> entrySet = TreeMap.entrySet();
                    for(Map.Entry<Integer,String> entry : entrySet){
                        Integer key = entry.getKey();
                        String value = entry.getValue();
                        String IsNullSql2 = "select json_length(score) as length FROM competitor where userid = ?;";
                        pstmt = conn.prepareStatement(IsNullSql2);
                        pstmt.setInt(1,Integer.parseInt(value));
                        ResultSet IsNullRs2 = pstmt.executeQuery();
                        while (IsNullRs2.next()) {
                            int length2 = IsNullRs2.getInt("length");
                            length2--;
                            //插入分数和排名
                            String AppendSql="update competitor set score = JSON_ARRAY_APPEND(score,'$["+length2+"]','"+key+"'),score= JSON_ARRAY_APPEND(score,'$["+length2+"]','"+ranking+"') where userid = ?;";
                            pstmt = conn.prepareStatement(AppendSql);
                            pstmt.setInt(1, Integer.parseInt(value));
                            int AppendRows = pstmt.executeUpdate();
                            if(AppendRows > 0)
                            {

                            }
                            else
                            {
                                IsAVG=false;
                            }
                            ranking--;
                        }
                    }
                }
                if(IsAdd==true)
                {
                    out.write("1");
                }
                else
                {
                    out.write("2");
                }
                if(IsContest==true)
                {
                    out.write("3");
                }
                else
                {
                    out.write("4");
                }
                if (IsAVG==true)
                {
                    out.write("5");
                }
                else
                {
                    out.write("6");
                }


//             完成后关闭
                rs.close();
                pstmt.close();
                conn.close();
            }
            else
            {
                out.print("9");
            }

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
