// pages/score/score.js
// 获取应用实例
const app = getApp()
const domain = app.globalData.domain;

Page({
  /**
   * 页面的初始数据
   */
  data: {
    userList: [],
    ContestId: "",
    ContestName: ""
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    app.Check()

    var ContestId = options.id; // 接收ContestId
    var ContestName = options.name; //接收ContestName

    //var ContestId = "10";
    //var ContestName = "比赛10";

    wx.setStorageSync('ContestId', ContestId)
    this.setData({
      ContestId: ContestId,
      ContestName: ContestName
    })

    this.GetUserList(); //获取选手列表
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady: function () {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function () {

  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide: function () {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload: function () {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh: function () {

  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom: function () {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage: function () {

  },


  /*获取选手列表*/
  GetUserList: function() {
    var that = this;
    
    wx.request({
      url: domain + "/QueryScore",
      data:{
        "ContestId": wx.getStorageSync('ContestId'),
      },
      method: 'POST',
      header: {
        'Content-Type': 'application/x-www-form-urlencoded',
        "Cookie": wx.getStorageSync('JSESSIONID')
      },
      success: function (res) {
        console.log("......Successful Request......");
        console.log(res.data);

        that.setData({
          userList: res.data
        })
      },
    })
  },

  /*按钮事件 - 提交评分*/
  SubmitScore: function(e) {

    console.log(e.detail.value);

    wx.request({
      url: domain + "/AddScore",
      data: e.detail.value,
      method: 'POST',
      header: {
        'content-type': 'application/json',
        "Cookie": wx.getStorageSync('JSESSIONID')
      },
      success: function (res) {
        console.log("......Successful Request......");
        console.log(res.data);

        // 页面交互逻辑
        if (res.data == "1" || res.data == "135") {
          wx.showToast({
            title: "评分成功",
            duration: 1000
          })
          setTimeout(function () {
            wx.navigateBack({
              delta: 1 //返回上一页
            })
          }, 1000)
        }
        else {
          wx.showToast({
            title: "评分失败",
            icon: 'none',
            duration: 3000
          })
        }

      },
    })

  },

})
