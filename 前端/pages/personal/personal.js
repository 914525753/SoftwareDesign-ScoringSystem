// pages/personal/personal.js
const app = getApp();
const domain = app.globalData.domain;

Page({
  /**
   * 页面的初始数据
   */
  data: {
    list: [],
    none: false,
    judge: false,
  },

  Enter:function(event)
  {
    var id = event.currentTarget.dataset.id;
    var name = event.currentTarget.dataset.name;
    wx.redirectTo({
      url: "/pages/score/score?id=" + id + "&&name=" + name
    })
  },
  

  Lock:function(event)
  {
    var id = event.currentTarget.dataset.id;
    var lock = event.currentTarget.dataset.lock;
    var that = this;
    wx.request({
      url: domain + "/LockJudges",
      data:{
        "ContestName":"contest" + id,
        "Lock":lock,
        "Cookie": wx.getStorageSync('JSESSIONID'),
      },
      method: 'POST',
      header: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      success: function (res) {
          switch (res.data){
            case 1:
              wx.showToast({
                title: '评委锁定',
                icon: 'success',
                duration: 2000,
              });             
              break;
            case 2:
              wx.showToast({
                title: '锁定失败',
                icon: 'none',
                duration: 2000,
              });
              break;
            case 3:
              wx.showToast({
                title: '参赛者锁定',
                icon: 'success',
                duration: 2000,
              });
              break;
            case 4:
              wx.showToast({
                title: '锁定失败',
                icon: 'none',
                duration: 2000,
              });
              break;
            default:
              break;
          }
      },
    })
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    app.Check();
    var that = this;
    wx.request({
      url: domain + '/ShowPersonal',
      header: {
        'content-type': 'application/json' ,
        "Cookie": wx.getStorageSync('JSESSIONID')
      },
      data: null,
      success: function (res) {
        var jsonRes = res.data.split(";");
        var arrayRes = [];
        for (var i = 0; i <= jsonRes.length - 2; i++) {
          arrayRes.push(JSON.parse(jsonRes[i]));
        }
        if(jsonRes[jsonRes.length-1].trim() === "isJudge")
        {
          that.setData({
            judge: true
          });
        }
        if(res.data != ""){
          that.setData({
            list: arrayRes
          });
        }
        else
        {
          that.setData(
            {
              list: [],
              none:true,
            }
          );         
        }
      },
      fail: function (res) {
        that.setData(
          {
            list: [],
            none:true,
          }
        );    
      }
    });
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

  }
})
