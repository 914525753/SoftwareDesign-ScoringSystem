import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

@WebServlet("/ScoreServlet")
public class ScoreServlet extends HttpServlet {
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
                pstmt.setInt(2, Integer.parseInt(map.get("JudgeId")));
                int updateRows = pstmt.executeUpdate();
                if (updateRows > 0) {
                    out.write("1");//添加评分成功
                } else {
                    out.write("2");//添加评分失败
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
                    //插入比赛
                    String InsertSql="update competitor set information = JSON_ARRAY_INSERT(information,'$[0]','"+SplitContest[2]+"') where userid = ?;";
                    pstmt = conn.prepareStatement(InsertSql);
                    pstmt.setInt(1, Integer.parseInt(String.valueOf(vec.get(i))));
                    int InsertRows = pstmt.executeUpdate();
                    if (InsertRows > 0) {
                        out.write("3");//插入比赛成功
                    } else {
                        out.write("4");//插入比赛失败
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
                    AVG_rs.close();
                }

                //遍历元素：
                Set<Map.Entry<Integer,String>> entrySet = TreeMap.entrySet();
                for(Map.Entry<Integer,String> entry : entrySet){
                    Integer key = entry.getKey();
                    String value = entry.getValue();

                    //插入分数和排名
                    String AppendSql="update competitor set information = JSON_ARRAY_APPEND(information,'$[0]','"+key+"'),information= JSON_ARRAY_APPEND(information,'$[0]','"+ranking+"') where userid = ?;";
                    pstmt = conn.prepareStatement(AppendSql);
                    pstmt.setInt(1, Integer.parseInt(value));
                    int AppendRows = pstmt.executeUpdate();
                    if(AppendRows > 0)
                    {
                        out.write("5");//插入分数和排名成功
                    }
                    else
                    {
                        out.write("6");//插入失败
                    }
                    ranking--;
                }
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
}
