// pages/login/login/login.js
// 获取应用实例
const app = getApp();
const domain = app.globalData.domain;

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
  
  /*传统登录事件*/
  create_login: function (e) {
    
    console.log(e.detail.value)
    wx.setStorageSync('username', e.detail.value["username"]); // 保存username到Storage

    // 发起传统登录请求
    wx.request({
      url: domain + "/LoginServlet2",
      data: "username=" + e.detail.value["username"] + "&password=" + e.detail.value["password"] + "&method=PasswordLogin",
      method: "POST",
      header: {
        //'content-type': 'application/json' // 默认值
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      success: this.getResult.bind(this) // 处理返回数据
    })

  },
  
  /*返回数据处理事件*/
  getResult: function (res) {

    console.log("......Successful Request......");
    console.log(res.data);
    
    // Cookies
    if (res && res.header && res.header['Set-Cookie']) {
      wx.setStorageSync('JSESSIONID', res.header['Set-Cookie']); // 保存Cookie到Storage
    }
    let cookie = wx.getStorageSync('JSESSIONID'); // 取出Cookie
    let header = { 'Content-Type': 'application/x-www-form-urlencoded'};
    if (cookie) {
        header.Cookie = cookie;
    }
    console.log(cookie)

    // 页面交互逻辑
    if (res.data == "11" || res.data == "12") {
      wx.showToast({
        title: "登录成功",
        duration: 1000
      })
      setTimeout(function () {
        wx.reLaunch({
          url: "/pages/index/index" // 关闭所有页面并打开主页
        })
      }, 1000)
    }
    else if (res.data == "13") {
      wx.showToast({
        title: "管理员登录成功",
        duration: 1000
      })
      setTimeout(function () {
        wx.redirectTo({
          url: "/pages/examine/accept/accept?type=1" // 管理员登录后直接进入accept页
        })
      }, 1000)
    }
    else if (res.data == "2") {
      wx.showToast({
        title: "账号或密码错误",
        icon: 'none',
        duration: 3000
      })
    }

  },
  
  /*按钮事件 - 导航到注册页*/
  goto_signup: function (res) {
    wx.navigateTo({
      url: "/pages/login/signup/signup",
    })
  }

})
