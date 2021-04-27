// pages/examine/accept/accept.js
const app = getApp();
const domain = app.globalData.domain;

Page({
  Accept:function(can,index)
  {
    var that = this;
    wx.request({
      url: domain + '/ExamineJudges',
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
        "Cookie": wx.getStorageSync('JSESSIONID')
      },
      method: "POST",
      data: "action=accept&can=" + can + "&index=" + index + "&type=" + that.data.type,
      success:function(res)
      {
        that.GetInfo();
        switch (res.data){
          case 1:
            wx.showToast({
              title: '批准成功', //弹框内容
              icon: 'success',  //弹框模式
              duration: 2000    //弹框显示时间
            });
            break;
          case 2:
            wx.showToast({
              title: '拒绝成功', //弹框内容
              icon: 'success',  //弹框模式
              duration: 2000    //弹框显示时间
            });
            break;
          case 3:
            wx.showToast({
              title: '已被其他管理员审核', //弹框内容
              icon: 'none',  //弹框模式
              duration: 2000    //弹框显示时间
            });
            break;
          default:
            break;
        }
      }
    })
  },

  Cando:function(event)
  {
    var index = event.currentTarget.dataset.index;
    var can = event.currentTarget.dataset.can;
    var that = this;
    if(can == "yes")
    {
      wx.showModal({
        title: '提示',
        content: '确定允许吗？',
        success: function (res) {
          if (res.confirm) {
            that.Accept(can,index);
          }
        }
      });
    }
    else
    {
      wx.showModal({
        title: '提示',
        content: '确定拒绝吗？',
        success: function (res) {
          if (res.confirm) {
            that.Accept(can,index);
          }
        }
      });
    }
  },

  GetInfo:function(){
    app.Check();
    var that = this;
    wx.request({
      url: domain + '/ExamineJudges',
      header: {
        "Content-Type": "application/x-www-form-urlencoded",
        "Cookie": wx.getStorageSync('JSESSIONID')
      },
      method: "POST",
      data: "action=show&type=" + that.data.type,
      success: function (res) {
        if(res.data != "")
        {
          var jsonRes = res.data.split(";");
          var arrayRes = [];
          for (var i = 0; i <= jsonRes.length - 2; i++) {
            arrayRes.push(JSON.parse(jsonRes[i]));
          }
          that.setData({
            list: arrayRes,          
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
        that.setData({
          list: [],
          none: true,
        });
      }
    });
  },

  /**
   * 页面的初始数据
   */
  data: {
    list: [],
    none:false,
    type: 2,
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    var type = options.type;
    this.setData
    ({
      type: type
    });
    this. GetInfo();
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