// pages/login/login.js
Page({

  /**
   * 页面的初始数据
   */
  data: {

  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    console.log(options)
    wx.getUserInfo({
      success: this.setUserInfo.bind(this)
    })
    this.setData({
      
    })
  },
  setUserInfo: function (res) {
    this.setData({ user: res.userInfo })
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
  create_login: function (e) {
    console.log(e.detail.value)
    wx.request({
      url: 'http://localhost:8080/LoginServlet',
      data:"username=" + e.detail.value["username"] + "&password=" + e.detail.value["password"],
      method: 'POST',
      header: {
        //'content-type': 'application/json' // 默认值
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      success: this.getResult.bind(this)
    })
  },
  getResult: function (res) {
    console.log(res.data);
    if(res.data == "true"){
    wx.showToast({
      title: "登录成功",
      duration: 2000
    })
      wx.switchTab({
        url: '../index/index',
      })
    setTimeout(function () {
      wx.navigateBack({
        delta: 2
      })
    }, 1000)
  }

  if(res.data == "false"){
    wx.showToast({
      title: "账号或密码不对",
      icon: 'none',
      duration: 3000
    })
    setTimeout(function () {
      wx.navigateBack({
        delta: 2
      })
    }, 1000)
  }
  },
  
  goto_index:function(res){
  },
  goto_signup: function (res) {
    wx.navigateTo({
      url: '../signup/signup',
    })
  }

})